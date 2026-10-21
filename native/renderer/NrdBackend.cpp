#include "NrdBackend.hpp"
#include <NRI.h>
#include <Extensions/NRIHelper.h>
#include <Extensions/NRIRayTracing.h>
#include <Extensions/NRIWrapperVK.h>
#include <NRD.h>
#include <algorithm>
#include <cmath>
#include <cstdio>
#include <cstring>
#include <limits>
#include <stdexcept>

// Convert official integration failures into a rejected recording so the caller
// can abandon its command buffer and invalidate the native context.
#define NRD_INTEGRATION_ASSERT(expression, message) do { if (!(expression)) throw std::runtime_error(message); } while (false)
#include <NRDIntegration.hpp>

namespace ac {
namespace {
constexpr nrd::Identifier DIRECT = 0;
constexpr nrd::Identifier INDIRECT = 1;
void check(nrd::Result result, const char* operation) {
    if (result != nrd::Result::SUCCESS)
        throw std::runtime_error(std::string(operation) + " failed: " + std::to_string(static_cast<uint32_t>(result)));
}
bool validImage(const Image& image, uint32_t width, uint32_t height) {
    return image.image && image.width == width && image.height == height && image.layout == VK_IMAGE_LAYOUT_GENERAL;
}
bool radianceFormat(VkFormat format) {
    return format == VK_FORMAT_R16G16B16A16_SFLOAT || format == VK_FORMAT_R32G32B32A32_SFLOAT;
}
nrd::Resource resource(const Image& image) {
    nrd::Resource result{};
    result.vk.image = reinterpret_cast<uint64_t>(image.image);
    result.vk.format = static_cast<int32_t>(image.format);
    result.state = {nri::AccessBits::SHADER_RESOURCE_STORAGE, nri::Layout::SHADER_RESOURCE_STORAGE,
        nri::StageBits::COMPUTE_SHADER};
    return result;
}
void bindGuides(nrd::ResourceSnapshot& resources, const Image& motion, const Image& viewZ, const Image& normalRoughness) {
    resources.restoreInitialState = true;
    resources.SetResource(nrd::ResourceType::IN_MV, resource(motion));
    resources.SetResource(nrd::ResourceType::IN_VIEWZ, resource(viewZ));
    resources.SetResource(nrd::ResourceType::IN_NORMAL_ROUGHNESS, resource(normalRoughness));
}
}

struct NrdBackend::Impl {
    DeviceInfo device;
    nrd::Integration integration;
    uint32_t width = 0, height = 0, previousFrameIndex = 0;
    bool ready = false, reset = true, poisoned = false;
    bool previousDirect = false, previousIndirect = false;
    std::string nativeError;

    explicit Impl(const DeviceInfo& info) : device(info) {}

    void configure(uint32_t w, uint32_t h) {
        if (ready && !poisoned && width == w && height == h) return;
        if ((ready || poisoned) && vkDeviceWaitIdle(device.device) != VK_SUCCESS)
            throw std::runtime_error("NRD could not retire the previous context");
        ready = false;
        integration.Destroy();
        const nrd::DenoiserDesc denoisers[]{
            {DIRECT, nrd::Denoiser::RELAX_DIFFUSE_SPECULAR},
            {INDIRECT, nrd::Denoiser::RELAX_DIFFUSE}};
        nrd::InstanceCreationDesc instance{};
        instance.denoisers = denoisers;
        instance.denoisersNum = 2;
        nrd::IntegrationCreationDesc description{};
        std::strcpy(description.name, "AdventureCraft RELAX");
        description.resourceWidth = static_cast<uint16_t>(w);
        description.resourceHeight = static_cast<uint16_t>(h);
        description.queuedFrameNum = 3;
        description.autoWaitForIdle = false;
        description.enableWholeLifetimeDescriptorCaching = false;
        nri::QueueFamilyVKDesc queue{1, nri::QueueType::GRAPHICS, device.queueFamily};
        nri::DeviceCreationVKDesc native{};
        // NRI's default error callback traps the process. Surface an SDK
        // failure to the renderer instead so it can abandon this recording.
        native.callbackInterface.userArg = this;
        native.callbackInterface.MessageCallback = [](nri::Message kind, const char*, uint32_t, const char* message, void* argument) {
            if (kind == nri::Message::ERROR) {
                auto* self = static_cast<Impl*>(argument);
                self->nativeError = message ? message : "NRD Vulkan failure";
                std::fprintf(stderr, "AdventureCraft NRD: %s\n", self->nativeError.c_str());
            }
        };
        native.callbackInterface.AbortExecution = [](void*) {};
        native.vkInstance = device.instance;
        native.vkPhysicalDevice = device.physicalDevice;
        native.vkDevice = device.device;
        native.queueFamilies = &queue;
        native.queueFamilyNum = 1;
        const char* extensions[]{VK_KHR_PUSH_DESCRIPTOR_EXTENSION_NAME};
        native.vkExtensions.deviceExtensions = extensions;
        native.vkExtensions.deviceExtensionNum = 1;
        // The renderer enables these promoted features on its Vulkan 1.3
        // logical device; do not advertise unenabled KHR compatibility paths.
        native.minorVersion = 3;
        nativeError.clear();
        nrd::Result creation = integration.RecreateVK(description, instance, native);
        if (creation != nrd::Result::SUCCESS && !nativeError.empty())
            throw std::runtime_error("NRD Vulkan creation failed: " + nativeError);
        check(creation, "NRD Vulkan creation");
        nrd::RelaxSettings settings{};
        settings.enableAntiFirefly = true;
        check(integration.SetDenoiserSettings(DIRECT, &settings), "NRD direct RELAX settings");
        check(integration.SetDenoiserSettings(INDIRECT, &settings), "NRD indirect RELAX settings");
        width = w;
        height = h;
        ready = true;
        reset = true;
        poisoned = false;
    }
};

bool NrdBackend::supported(const DeviceInfo& device, std::string& reason) {
    if (!device.instance || !device.device || !device.physicalDevice || !device.queue) {
        reason = "NRD requires an initialized Vulkan graphics device"; return false;
    }
    if (!device.storageExtended || !device.storageRead || !device.storageWrite) {
        reason = "NRD requires enabled extended storage formats and formatless image reads/writes"; return false;
    }
    if (device.apiVersion < VK_API_VERSION_1_3 || !device.partiallyBound
        || !device.synchronization2 || !device.dynamicRendering
        || !device.maintenance4 || !device.bufferDeviceAddress || !device.pushDescriptor) {
        reason = "NRD requires Vulkan 1.3 with enabled partially bound descriptors, synchronization2, dynamic rendering, maintenance4, buffer device addresses, and KHR push descriptors";
        return false;
    }
    VkPhysicalDeviceProperties properties;
    vkGetPhysicalDeviceProperties(device.physicalDevice, &properties);
    if (properties.apiVersion < VK_API_VERSION_1_3) { reason = "NRD requires Vulkan 1.3"; return false; }
    const nrd::LibraryDesc* library = nrd::GetLibraryDesc();
    if (library->normalEncoding != nrd::NormalEncoding::RGBA16_SNORM || library->roughnessEncoding != nrd::RoughnessEncoding::LINEAR) {
        reason = "NRD library normal/roughness encodings do not match the renderer"; return false;
    }
    reason.clear();
    return true;
}

NrdBackend::NrdBackend(const DeviceInfo& device) {
    std::string reason;
    if (!supported(device, reason)) throw std::runtime_error(reason);
    impl = std::make_unique<Impl>(device);
}
NrdBackend::~NrdBackend() = default;

bool NrdBackend::denoise(VkCommandBuffer command, const FrameData& frame, bool direct, bool indirect,
    const Image& motion, const Image& viewZ, const Image& normalRoughness,
    const Image& diffuse, const Image& specular, const Image& indirectDiffuse,
    const Image& outDiffuse, const Image& outSpecular, const Image& outIndirect,
    std::string& error) {
    if (!direct && !indirect) { impl->reset = true; error.clear(); return true; }
    uint32_t width = frame.renderWidth, height = frame.renderHeight;
    if (!command || !width || !height || width > UINT16_MAX || height > UINT16_MAX
        || !std::isfinite(frame.deltaMs) || frame.deltaMs < 0) {
        error = "NRD needs a valid command buffer, timing, and dimensions in 1..65535"; return false;
    }
    for (const auto* image : {&motion, &viewZ, &normalRoughness}) if (!validImage(*image, width, height)) {
        error = "NRD guides must be initialized GENERAL images matching render resolution"; return false;
    }
    if (!radianceFormat(motion.format) || !radianceFormat(normalRoughness.format) || viewZ.format != VK_FORMAT_R32_SFLOAT) {
        error = "NRD needs RGBA float motion/normal guides and R32 float positive view depth"; return false;
    }
    for (const auto* matrix : {&frame.worldToView, &frame.previousWorldToView, &frame.viewToClip, &frame.previousViewToClip})
        for (float value : *matrix) if (!std::isfinite(value)) { error = "NRD camera matrices must be finite"; return false; }
    if (!std::isfinite(frame.jitterX) || !std::isfinite(frame.jitterY)
        || !std::isfinite(frame.previousJitterX) || !std::isfinite(frame.previousJitterY)) {
        error = "NRD jitter must be finite"; return false;
    }
    std::vector<const Image*> signals;
    if (direct) signals.insert(signals.end(), {&diffuse, &specular, &outDiffuse, &outSpecular});
    if (indirect) signals.insert(signals.end(), {&indirectDiffuse, &outIndirect});
    for (const auto* image : signals) if (!validImage(*image, width, height) || !radianceFormat(image->format)) {
        error = "NRD radiance/hit-distance signals must be GENERAL RGBA float images at render resolution"; return false;
    }
    try {
        impl->configure(width, height);
        impl->nativeError.clear();
        nrd::CommonSettings common{};
        std::copy(frame.worldToView.begin(), frame.worldToView.end(), common.worldToViewMatrix);
        std::copy(frame.previousWorldToView.begin(), frame.previousWorldToView.end(), common.worldToViewMatrixPrev);
        std::copy(frame.viewToClip.begin(), frame.viewToClip.end(), common.viewToClipMatrix);
        std::copy(frame.previousViewToClip.begin(), frame.previousViewToClip.end(), common.viewToClipMatrixPrev);
        common.motionVectorScale[0] = 1.0f / width;
        common.motionVectorScale[1] = 1.0f / height;
        common.motionVectorScale[2] = 0;
        common.isMotionVectorInWorldSpace = false;
        common.cameraJitter[0] = frame.jitterX;
        common.cameraJitter[1] = frame.jitterY;
        common.cameraJitterPrev[0] = frame.previousJitterX;
        common.cameraJitterPrev[1] = frame.previousJitterY;
        common.resourceSize[0] = common.resourceSizePrev[0] = common.rectSize[0] = common.rectSizePrev[0] = static_cast<uint16_t>(width);
        common.resourceSize[1] = common.resourceSizePrev[1] = common.rectSize[1] = common.rectSizePrev[1] = static_cast<uint16_t>(height);
        common.frameIndex = frame.frameIndex;
        common.timeDeltaBetweenFrames = std::clamp(frame.deltaMs, 0.001f, 1000.0f);
        common.denoisingRange = std::isfinite(frame.farPlane) ? std::max(frame.farPlane, 1.0f) : 1024.0f;
        bool restart = frame.reset || impl->reset || frame.frameIndex != impl->previousFrameIndex + 1
            || direct != impl->previousDirect || indirect != impl->previousIndirect;
        common.accumulationMode = restart ? nrd::AccumulationMode::CLEAR_AND_RESTART : nrd::AccumulationMode::CONTINUE;
        impl->integration.NewFrame();
        check(impl->integration.SetCommonSettings(common), "NRD common camera settings");
        nri::CommandBufferVKDesc native{};
        native.vkCommandBuffer = command;
        native.queueType = nri::QueueType::GRAPHICS;
        if (direct) {
            nrd::ResourceSnapshot resources;
            bindGuides(resources, motion, viewZ, normalRoughness);
            resources.SetResource(nrd::ResourceType::IN_DIFF_RADIANCE_HITDIST, resource(diffuse));
            resources.SetResource(nrd::ResourceType::IN_SPEC_RADIANCE_HITDIST, resource(specular));
            resources.SetResource(nrd::ResourceType::OUT_DIFF_RADIANCE_HITDIST, resource(outDiffuse));
            resources.SetResource(nrd::ResourceType::OUT_SPEC_RADIANCE_HITDIST, resource(outSpecular));
            impl->integration.DenoiseVK(&DIRECT, 1, native, resources);
        }
        if (indirect) {
            nrd::ResourceSnapshot resources;
            bindGuides(resources, motion, viewZ, normalRoughness);
            resources.SetResource(nrd::ResourceType::IN_DIFF_RADIANCE_HITDIST, resource(indirectDiffuse));
            resources.SetResource(nrd::ResourceType::OUT_DIFF_RADIANCE_HITDIST, resource(outIndirect));
            impl->integration.DenoiseVK(&INDIRECT, 1, native, resources);
        }
        if (!impl->nativeError.empty()) throw std::runtime_error(impl->nativeError);
        impl->reset = false;
        impl->previousFrameIndex = frame.frameIndex;
        impl->previousDirect = direct;
        impl->previousIndirect = indirect;
        error.clear();
        return true;
    } catch (const std::exception& exception) {
        impl->poisoned = true;
        impl->reset = true;
        error = exception.what();
        return false;
    }
}

void NrdBackend::invalidate() { impl->reset = true; }
void NrdBackend::abort() {
    if (vkDeviceWaitIdle(impl->device.device) != VK_SUCCESS)
        throw std::runtime_error("NRD could not retire an abandoned recording");
    impl->integration.Destroy();
    impl->ready = false;
    impl->reset = true;
    impl->poisoned = false;
}
}
