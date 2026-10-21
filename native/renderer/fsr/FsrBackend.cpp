#include "FsrBackend.hpp"
#include <FidelityFX/host/ffx_fsr3upscaler.h>
#include <FidelityFX/host/backends/vk/ffx_vk.h>
#include <array>
#include <cmath>
#include <cstddef>
#include <stdexcept>
#include <vector>
#include <map>
#include <mutex>

// The SDK backend exposes swapchain configuration even for standalone compute.
// Presentation belongs to the renderer; neither SR nor our FG compute path uses
// the SDK's platform-specific swapchain replacement.
extern "C" FfxErrorCode ffxSetFrameGenerationConfigToSwapchainVK(const FfxFrameGenerationConfig*) {
    return FFX_ERROR_BACKEND_API_ERROR;
}

namespace ac {
namespace {
struct Resolver { PFN_vkGetDeviceProcAddr function; size_t references; };
std::map<VkDevice, Resolver> resolvers;
std::mutex resolverMutex;
// Official context creation may fail after its backend has allocated resources.
// Track that milestone without inspecting the SDK's private context layout.
struct BackendCreation;
thread_local BackendCreation* activeCreation = nullptr;
struct BackendCreation {
    FfxCreateBackendContextFunc original;
    bool& created;
    BackendCreation* previous;
    BackendCreation(FfxCreateBackendContextFunc function, bool& state)
        : original(function), created(state), previous(activeCreation) { activeCreation = this; }
    ~BackendCreation() { activeCreation = previous; }
};
FfxErrorCode trackBackendCreation(FfxInterface* interface, FfxEffect effect,
    FfxEffectBindlessConfig* configuration, FfxUInt32* id) {
    if (!activeCreation) return FFX_ERROR_BACKEND_API_ERROR;
    FfxErrorCode result = activeCreation->original(interface, effect, configuration, id);
    if (result == FFX_OK) activeCreation->created = true;
    return result;
}
VKAPI_ATTR PFN_vkVoidFunction VKAPI_CALL compatibleDeviceProcAddr(VkDevice device, const char* name) {
    PFN_vkGetDeviceProcAddr resolver = vkGetDeviceProcAddr;
    {
        std::lock_guard<std::mutex> lock(resolverMutex);
        auto entry = resolvers.find(device);
        if (entry != resolvers.end()) resolver = entry->second.function;
    }
    PFN_vkVoidFunction result = resolver(device, name);
    // Promoted KHR aliases need not be exposed unless their old extension was
    // enabled. The SDK queries extension support and then uses those aliases.
    if (!result && name) {
        std::string core(name);
        if (core.size() > 3 && core.compare(core.size() - 3, 3, "KHR") == 0) {
            core.resize(core.size() - 3);
            result = resolver(device, core.c_str());
        }
    }
    return result;
}
void check(VkResult result, const char* operation) {
    if (result != VK_SUCCESS) throw std::runtime_error(std::string(operation) + " failed: " + std::to_string(result));
}
void checkFfx(FfxErrorCode result, const char* operation) {
    if (result != FFX_OK) throw std::runtime_error(std::string(operation) + " failed: " + std::to_string(result));
}
bool depthFormat(VkFormat format) { return format == VK_FORMAT_D32_SFLOAT; }
void transition(VkCommandBuffer command, const FsrImage& image, VkImageLayout before, VkImageLayout after) {
    VkImageMemoryBarrier barrier{VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER};
    barrier.srcAccessMask = before == VK_IMAGE_LAYOUT_UNDEFINED ? 0 : VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT;
    barrier.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT;
    barrier.oldLayout = before;
    barrier.newLayout = after;
    barrier.srcQueueFamilyIndex = barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image.image;
    barrier.subresourceRange = {depthFormat(image.format) ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1};
    vkCmdPipelineBarrier(command, before == VK_IMAGE_LAYOUT_UNDEFINED ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
        VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, 0, nullptr, 0, nullptr, 1, &barrier);
}
FfxResource resource(const FsrImage& image, FfxResourceStates state, const wchar_t* name) {
    if (image.image == VK_NULL_HANDLE) return {};
    VkImageCreateInfo description{VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO};
    description.imageType = VK_IMAGE_TYPE_2D;
    description.format = image.format;
    description.extent = {image.width, image.height, 1};
    description.mipLevels = description.arrayLayers = 1;
    description.usage = image.usage;
    return ffxGetResourceVK(reinterpret_cast<void*>(image.image),
        ffxGetImageResourceDescriptionVK(image.image, description), name, state);
}
bool finiteFrame(const FsrFrame& frame) {
    const float values[]{frame.jitterX, frame.jitterY, frame.motionScaleX, frame.motionScaleY,
        frame.deltaMilliseconds, frame.preExposure, frame.cameraNear, frame.cameraFar,
        frame.verticalFovRadians, frame.sharpness};
    for (float value : values) if (!std::isfinite(value)) return false;
    return frame.deltaMilliseconds >= 0 && frame.preExposure > 0 && frame.cameraNear > 0
        && frame.cameraFar > frame.cameraNear && frame.verticalFovRadians > 0 && frame.verticalFovRadians < 3.14159265f;
}
struct alignas(32) ScratchBlock { std::byte storage[32]; };
}

struct FsrBackend::Impl {
    FsrDevice device;
    FfxFsr3UpscalerContext context{};
    VkDeviceContext vkContext{};
    std::vector<ScratchBlock> scratch;
    std::array<FsrImage, 3> shared{};
    std::array<VkDeviceMemory, 3> sharedMemory{};
    uint32_t renderWidth = 0, renderHeight = 0, outputWidth = 0, outputHeight = 0;
    bool hdr = false, ready = false, backendCreated = false, sharedFresh = true, poisoned = false;

    explicit Impl(const FsrDevice& info) : device(info) {
        std::lock_guard<std::mutex> lock(resolverMutex);
        auto& entry = resolvers[device.device];
        entry.function = device.getDeviceProcAddr;
        entry.references++;
    }
    ~Impl() {
        release();
        std::lock_guard<std::mutex> lock(resolverMutex);
        auto entry = resolvers.find(device.device);
        if (entry != resolvers.end() && --entry->second.references == 0) resolvers.erase(entry);
    }

    void release() {
        if (backendCreated) ffxFsr3UpscalerContextDestroy(&context);
        ready = false;
        backendCreated = false;
        for (size_t index = 0; index < shared.size(); index++) {
            if (shared[index].image) vkDestroyImage(device.device, shared[index].image, nullptr);
            if (sharedMemory[index]) vkFreeMemory(device.device, sharedMemory[index], nullptr);
            shared[index] = {};
            sharedMemory[index] = VK_NULL_HANDLE;
        }
        std::memset(&context, 0, sizeof(context));
        scratch.clear();
        sharedFresh = true;
        poisoned = false;
    }

    void allocateShared(size_t index, VkFormat format, uint32_t width, uint32_t height) {
        auto& image = shared[index];
        image.format = format;
        image.width = width;
        image.height = height;
        image.layout = VK_IMAGE_LAYOUT_GENERAL;
        image.usage = VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        VkImageCreateInfo description{VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO};
        description.imageType = VK_IMAGE_TYPE_2D;
        description.format = format;
        description.extent = {width, height, 1};
        description.mipLevels = description.arrayLayers = 1;
        description.samples = VK_SAMPLE_COUNT_1_BIT;
        description.tiling = VK_IMAGE_TILING_OPTIMAL;
        description.usage = image.usage;
        description.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
        check(vkCreateImage(device.device, &description, nullptr, &image.image), "FSR shared image creation");
        VkMemoryRequirements requirements;
        vkGetImageMemoryRequirements(device.device, image.image, &requirements);
        VkPhysicalDeviceMemoryProperties memory;
        vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, &memory);
        uint32_t memoryType = UINT32_MAX;
        for (uint32_t type = 0; type < memory.memoryTypeCount; type++) {
            if ((requirements.memoryTypeBits & (1u << type)) == 0) continue;
            if (memoryType == UINT32_MAX) memoryType = type;
            if (memory.memoryTypes[type].propertyFlags & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) { memoryType = type; break; }
        }
        if (memoryType == UINT32_MAX) throw std::runtime_error("No FSR shared image memory type");
        VkMemoryAllocateInfo allocation{VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO};
        allocation.allocationSize = requirements.size;
        allocation.memoryTypeIndex = memoryType;
        check(vkAllocateMemory(device.device, &allocation, nullptr, &sharedMemory[index]), "FSR shared image allocation");
        check(vkBindImageMemory(device.device, image.image, sharedMemory[index], 0), "FSR shared image binding");
    }

    void configure(const FsrFrame& frame) {
        if (ready && !poisoned && renderWidth == frame.renderWidth && renderHeight == frame.renderHeight
            && outputWidth == frame.outputWidth && outputHeight == frame.outputHeight && hdr == frame.hdr) return;
        if (ready) check(vkDeviceWaitIdle(device.device), "FSR reconfiguration wait");
        release();
        renderWidth = frame.renderWidth;
        renderHeight = frame.renderHeight;
        outputWidth = frame.outputWidth;
        outputHeight = frame.outputHeight;
        hdr = frame.hdr;
        vkContext = {device.device, device.physicalDevice, compatibleDeviceProcAddr};
        const size_t bytes = ffxGetScratchMemorySizeVK(device.physicalDevice, FFX_FSR3UPSCALER_CONTEXT_COUNT);
        scratch.resize((bytes + sizeof(ScratchBlock) - 1) / sizeof(ScratchBlock));
        std::memset(scratch.data(), 0, scratch.size() * sizeof(ScratchBlock));
        FfxFsr3UpscalerContextDescription description{};
        description.flags = hdr ? FFX_FSR3UPSCALER_ENABLE_HIGH_DYNAMIC_RANGE : 0;
        description.maxRenderSize = {renderWidth, renderHeight};
        description.maxUpscaleSize = {outputWidth, outputHeight};
        checkFfx(ffxGetInterfaceVK(&description.backendInterface, reinterpret_cast<FfxDevice>(&vkContext),
            scratch.data(), scratch.size() * sizeof(ScratchBlock), FFX_FSR3UPSCALER_CONTEXT_COUNT), "FSR Vulkan interface");
        {
            BackendCreation tracking(description.backendInterface.fpCreateBackendContext, backendCreated);
            description.backendInterface.fpCreateBackendContext = trackBackendCreation;
            checkFfx(ffxFsr3UpscalerContextCreate(&context, &description), "FSR3 context creation");
        }
        ready = true;
        allocateShared(0, VK_FORMAT_R32_SFLOAT, renderWidth, renderHeight);
        allocateShared(1, VK_FORMAT_R16G16_SFLOAT, renderWidth, renderHeight);
        allocateShared(2, VK_FORMAT_R32_UINT, renderWidth, renderHeight);
    }
};

bool FsrBackend::supported(const FsrDevice& device, std::string& reason) {
    if (!device.device || !device.physicalDevice || !device.graphicsQueue || !device.getDeviceProcAddr) {
        reason = "FSR requires an initialized Vulkan graphics device"; return false;
    }
    if (!device.storageImageExtendedFormats || !device.storageImageWriteWithoutFormat) {
        reason = "FSR requires enabled extended storage-image formats and formatless image writes"; return false;
    }
    VkPhysicalDeviceSubgroupProperties subgroup{VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES};
    VkPhysicalDeviceProperties2 properties{VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2};
    properties.pNext = &subgroup;
    vkGetPhysicalDeviceProperties2(device.physicalDevice, &properties);
    if (properties.properties.apiVersion < VK_API_VERSION_1_2) { reason = "FSR requires Vulkan 1.2"; return false; }
    const VkSubgroupFeatureFlags required = VK_SUBGROUP_FEATURE_BASIC_BIT | VK_SUBGROUP_FEATURE_QUAD_BIT;
    if (!(subgroup.supportedStages & VK_SHADER_STAGE_COMPUTE_BIT) || (subgroup.supportedOperations & required) != required) {
        reason = "FSR requires compute subgroup quad operations"; return false;
    }
    uint32_t count = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, &count, nullptr);
    std::vector<VkQueueFamilyProperties> families(count);
    vkGetPhysicalDeviceQueueFamilyProperties(device.physicalDevice, &count, families.data());
    if (device.queueFamily >= count || !(families[device.queueFamily].queueFlags & VK_QUEUE_COMPUTE_BIT)) {
        reason = "FSR requires compute on the graphics queue"; return false;
    }
    reason.clear();
    return true;
}

FsrBackend::FsrBackend(const FsrDevice& device) {
    std::string reason;
    if (!supported(device, reason)) throw std::runtime_error(reason);
    impl = std::make_unique<Impl>(device);
}
FsrBackend::~FsrBackend() = default;

bool FsrBackend::dispatch(const FsrFrame& frame, std::string& error) {
    const FsrImage* inputs[]{&frame.color, &frame.depth, &frame.motion, &frame.reactive};
    if (!frame.commandBuffer || !frame.renderWidth || !frame.renderHeight || !frame.outputWidth || !frame.outputHeight
        || frame.renderWidth > frame.outputWidth || frame.renderHeight > frame.outputHeight || !finiteFrame(frame)) {
        error = "Invalid FSR dispatch camera, size, or timing"; return false;
    }
    for (size_t index = 0; index < 4; index++) {
        const auto& image = *inputs[index];
        if (index == 3 && !image.image) continue;
        if (!image.image || image.width < frame.renderWidth || image.height < frame.renderHeight
            || !(image.usage & VK_IMAGE_USAGE_SAMPLED_BIT) || image.layout == VK_IMAGE_LAYOUT_UNDEFINED) {
            error = "FSR inputs must be initialized sampled images at render resolution"; return false;
        }
    }
    if (!frame.output.image || frame.output.width != frame.outputWidth || frame.output.height != frame.outputHeight
        || !(frame.output.usage & VK_IMAGE_USAGE_STORAGE_BIT) || frame.output.layout == VK_IMAGE_LAYOUT_UNDEFINED) {
        error = "FSR output must be an initialized storage image at display resolution"; return false;
    }
    for (const auto* input : inputs) if (input->image && input->image == frame.output.image) {
        error = "FSR output cannot alias an input image"; return false;
    }
    try {
        impl->configure(frame);
        for (const auto* image : inputs) if (image->image)
            transition(frame.commandBuffer, *image, image->layout, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        transition(frame.commandBuffer, frame.output, frame.output.layout, VK_IMAGE_LAYOUT_GENERAL);
        if (impl->sharedFresh) {
            for (const auto& image : impl->shared) transition(frame.commandBuffer, image, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_GENERAL);
        } else {
            for (const auto& image : impl->shared) transition(frame.commandBuffer, image, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_GENERAL);
        }
        FfxFsr3UpscalerDispatchDescription description{};
        description.commandList = ffxGetCommandListVK(frame.commandBuffer);
        description.color = resource(frame.color, FFX_RESOURCE_STATE_COMPUTE_READ, L"AC color");
        description.depth = resource(frame.depth, FFX_RESOURCE_STATE_COMPUTE_READ, L"AC depth");
        description.motionVectors = resource(frame.motion, FFX_RESOURCE_STATE_COMPUTE_READ, L"AC motion");
        description.reactive = resource(frame.reactive, FFX_RESOURCE_STATE_COMPUTE_READ, L"AC reactive");
        description.output = resource(frame.output, FFX_RESOURCE_STATE_UNORDERED_ACCESS, L"AC output");
        description.dilatedDepth = resource(impl->shared[0], FFX_RESOURCE_STATE_UNORDERED_ACCESS, L"AC FSR dilated depth");
        description.dilatedMotionVectors = resource(impl->shared[1], FFX_RESOURCE_STATE_UNORDERED_ACCESS, L"AC FSR dilated motion");
        description.reconstructedPrevNearestDepth = resource(impl->shared[2], FFX_RESOURCE_STATE_UNORDERED_ACCESS, L"AC FSR previous depth");
        description.jitterOffset = {frame.jitterX, frame.jitterY};
        description.motionVectorScale = {frame.motionScaleX, frame.motionScaleY};
        description.renderSize = {frame.renderWidth, frame.renderHeight};
        description.upscaleSize = {frame.outputWidth, frame.outputHeight};
        description.enableSharpening = frame.sharpness > 0;
        description.sharpness = std::clamp(frame.sharpness, 0.0f, 1.0f);
        description.frameTimeDelta = std::clamp(frame.deltaMilliseconds, 0.0f, 1000.0f);
        description.preExposure = frame.preExposure;
        description.reset = frame.reset || impl->sharedFresh;
        description.cameraNear = frame.cameraNear;
        description.cameraFar = frame.cameraFar;
        description.cameraFovAngleVertical = frame.verticalFovRadians;
        description.viewSpaceToMetersFactor = 1;
        FfxErrorCode result = ffxFsr3UpscalerContextDispatch(&impl->context, &description);
        for (const auto* image : inputs) if (image->image)
            transition(frame.commandBuffer, *image, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, image->layout);
        transition(frame.commandBuffer, frame.output, VK_IMAGE_LAYOUT_GENERAL, frame.output.layout);
        checkFfx(result, "FSR3 temporal dispatch");
        impl->sharedFresh = false;
        error.clear();
        return true;
    } catch (const std::exception& exception) {
        impl->poisoned = true;
        error = exception.what();
        return false;
    }
}

void FsrBackend::abort() {
    check(vkDeviceWaitIdle(impl->device.device), "FSR aborted-frame retirement");
    impl->release();
}
}
