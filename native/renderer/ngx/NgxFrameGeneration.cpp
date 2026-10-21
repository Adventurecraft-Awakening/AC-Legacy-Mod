#include "NgxFrameGeneration.hpp"
#include <nvsdk_ngx_helpers_dlssg_vk.h>

#include <algorithm>
#include <array>
#include <cmath>
#include <cstring>
#include <limits>
#include <sstream>
#include <stdexcept>
#include <vector>

namespace ac {
namespace {

using Matrix = std::array<float, 16>;

std::string ngxError(const char* operation, NVSDK_NGX_Result result) {
    std::ostringstream message;
    message << operation << " failed (NGX 0x" << std::hex << static_cast<uint32_t>(result) << ')';
    return message.str();
}

void check(VkResult result, const char* operation) {
    if (result != VK_SUCCESS) throw std::runtime_error(std::string(operation) + " failed (Vulkan " + std::to_string(result) + ')');
}

Matrix multiply(const Matrix& a, const Matrix& b) {
    Matrix result{};
    for (int column = 0; column < 4; ++column)
        for (int row = 0; row < 4; ++row)
            for (int k = 0; k < 4; ++k)
                result[column * 4 + row] += a[k * 4 + row] * b[column * 4 + k];
    return result;
}

bool inverse(const Matrix& matrix, Matrix& result) {
    double rows[4][8]{};
    for (int row = 0; row < 4; ++row) {
        for (int column = 0; column < 4; ++column) {
            float value = matrix[column * 4 + row];
            if (!std::isfinite(value)) return false;
            rows[row][column] = value;
        }
        rows[row][row + 4] = 1.0;
    }
    for (int column = 0; column < 4; ++column) {
        int pivot = column;
        for (int row = column + 1; row < 4; ++row)
            if (std::abs(rows[row][column]) > std::abs(rows[pivot][column])) pivot = row;
        if (std::abs(rows[pivot][column]) < 1e-12) return false;
        for (int k = 0; k < 8; ++k) std::swap(rows[column][k], rows[pivot][k]);
        double divisor = rows[column][column];
        for (double& value : rows[column]) value /= divisor;
        for (int row = 0; row < 4; ++row) {
            if (row == column) continue;
            double scale = rows[row][column];
            for (int k = 0; k < 8; ++k) rows[row][k] -= scale * rows[column][k];
        }
    }
    for (int row = 0; row < 4; ++row)
        for (int column = 0; column < 4; ++column) {
            double value = rows[row][column + 4];
            if (!std::isfinite(value) || std::abs(value) > std::numeric_limits<float>::max()) return false;
            result[column * 4 + row] = static_cast<float>(value);
        }
    return true;
}

NVSDK_NGX_Resource_VK resource(const Image& image, bool writable) {
    NVSDK_NGX_Resource_VK result{};
    result.Type = NVSDK_NGX_RESOURCE_VK_TYPE_VK_IMAGEVIEW;
    result.ReadWrite = writable;
    result.Resource.ImageViewInfo = {image.view, image.image,
        {image.format == VK_FORMAT_D32_SFLOAT ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1},
        image.format, image.width, image.height};
    return result;
}

}

struct NgxFrameGeneration::Impl {
    struct OwnedImage {
        Image image;
        VkDeviceMemory memory = VK_NULL_HANDLE;
    };
    struct Slot {
        OwnedImage interpolated;
        OwnedImage real;
        VkBuffer disable = VK_NULL_HANDLE;
        VkDeviceMemory disableMemory = VK_NULL_HANDLE;
        void* mapped = nullptr;
        bool coherent = false;
        bool valid = false;
        bool reset = true;
    };

    DeviceInfo device;
    NVSDK_NGX_Handle* feature = nullptr;
    NVSDK_NGX_Parameter* parameters = nullptr;
    std::vector<Slot> slots;
    uint32_t width = 0, height = 0, renderWidth = 0, renderHeight = 0;
    VkFormat format = VK_FORMAT_UNDEFINED;
    bool reset = true;
    bool featureReady = false;
    bool recorded = false;
    uint32_t lastFrame = 0;
    std::array<double, 3> cameraPosition{};

    explicit Impl(const DeviceInfo& info) : device(info) {
        auto result = NVSDK_NGX_VULKAN_AllocateParameters(&parameters);
        if (NVSDK_NGX_FAILED(result) || !parameters) throw std::runtime_error(ngxError("Allocate DLSS Frame Generation parameters", result));
    }

    ~Impl() {
        // Destruction/configuration may wait; dispatch never waits in steady state.
        if (device.device && (feature || !slots.empty())) vkDeviceWaitIdle(device.device);
        release();
        if (parameters) NVSDK_NGX_VULKAN_DestroyParameters(parameters);
    }

    uint32_t memoryType(uint32_t bits, VkMemoryPropertyFlags required, VkMemoryPropertyFlags preferred = 0) const {
        VkPhysicalDeviceMemoryProperties properties;
        vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, &properties);
        uint32_t fallback = UINT32_MAX;
        for (uint32_t i = 0; i < properties.memoryTypeCount; ++i) {
            auto flags = properties.memoryTypes[i].propertyFlags;
            if (!(bits & (1u << i)) || (flags & required) != required) continue;
            if ((flags & preferred) == preferred) return i;
            fallback = i;
        }
        if (fallback == UINT32_MAX) throw std::runtime_error("DLSS Frame Generation cannot allocate compatible Vulkan memory");
        return fallback;
    }

    void destroy(OwnedImage& owned) {
        if (owned.image.view) vkDestroyImageView(device.device, owned.image.view, nullptr);
        if (owned.image.image) vkDestroyImage(device.device, owned.image.image, nullptr);
        if (owned.memory) vkFreeMemory(device.device, owned.memory, nullptr);
        owned = {};
    }

    void destroy(Slot& slot) {
        destroy(slot.interpolated);
        destroy(slot.real);
        if (slot.mapped) vkUnmapMemory(device.device, slot.disableMemory);
        if (slot.disable) vkDestroyBuffer(device.device, slot.disable, nullptr);
        if (slot.disableMemory) vkFreeMemory(device.device, slot.disableMemory, nullptr);
        slot = {};
    }

    void release() {
        if (feature) NVSDK_NGX_VULKAN_ReleaseFeature(feature);
        feature = nullptr;
        featureReady = false;
        for (auto& slot : slots) destroy(slot);
        slots.clear();
        reset = true;
        recorded = false;
        cameraPosition = {};
    }

    void createImage(OwnedImage& owned) {
        owned.image.format = format;
        owned.image.width = width;
        owned.image.height = height;
        owned.image.layout = VK_IMAGE_LAYOUT_UNDEFINED;
        VkImageCreateInfo create{VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO};
        create.imageType = VK_IMAGE_TYPE_2D;
        create.format = format;
        create.extent = {width, height, 1};
        create.mipLevels = create.arrayLayers = 1;
        create.samples = VK_SAMPLE_COUNT_1_BIT;
        create.tiling = VK_IMAGE_TILING_OPTIMAL;
        create.usage = VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        check(vkCreateImage(device.device, &create, nullptr, &owned.image.image), "Create DLSS Frame Generation output");
        VkMemoryRequirements requirements;
        vkGetImageMemoryRequirements(device.device, owned.image.image, &requirements);
        VkMemoryAllocateInfo allocate{VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO};
        allocate.allocationSize = requirements.size;
        allocate.memoryTypeIndex = memoryType(requirements.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
        check(vkAllocateMemory(device.device, &allocate, nullptr, &owned.memory), "Allocate DLSS Frame Generation output");
        check(vkBindImageMemory(device.device, owned.image.image, owned.memory, 0), "Bind DLSS Frame Generation output");
        VkImageViewCreateInfo view{VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO};
        view.image = owned.image.image;
        view.viewType = VK_IMAGE_VIEW_TYPE_2D;
        view.format = format;
        view.subresourceRange = {VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1};
        check(vkCreateImageView(device.device, &view, nullptr, &owned.image.view), "Create DLSS Frame Generation output view");
    }

    void createSlot(Slot& slot) {
        try {
            createImage(slot.interpolated);
            createImage(slot.real);
            VkBufferCreateInfo create{VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO};
            create.size = 4;
            create.usage = VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;
            check(vkCreateBuffer(device.device, &create, nullptr, &slot.disable), "Create DLSS Frame Generation disable buffer");
            VkMemoryRequirements requirements;
            vkGetBufferMemoryRequirements(device.device, slot.disable, &requirements);
            uint32_t index = memoryType(requirements.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
            VkPhysicalDeviceMemoryProperties properties;
            vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, &properties);
            slot.coherent = (properties.memoryTypes[index].propertyFlags & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) != 0;
            VkMemoryAllocateInfo allocate{VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO};
            VkMemoryAllocateFlagsInfo flags{VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_FLAGS_INFO};
            flags.flags = VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT;
            allocate.pNext = &flags;
            allocate.allocationSize = requirements.size;
            allocate.memoryTypeIndex = index;
            check(vkAllocateMemory(device.device, &allocate, nullptr, &slot.disableMemory), "Allocate DLSS Frame Generation disable buffer");
            check(vkBindBufferMemory(device.device, slot.disable, slot.disableMemory, 0), "Bind DLSS Frame Generation disable buffer");
            check(vkMapMemory(device.device, slot.disableMemory, 0, VK_WHOLE_SIZE, 0, &slot.mapped), "Map DLSS Frame Generation disable buffer");
        } catch (...) {
            destroy(slot);
            throw;
        }
    }

    bool createFeature(NVSDK_NGX_DLSSG_Create_Params& create, std::string& error) {
        VkCommandPool pool = VK_NULL_HANDLE;
        VkFence fence = VK_NULL_HANDLE;
        bool submitted = false;
        auto cleanup = [&] {
            if (fence) vkDestroyFence(device.device, fence, nullptr);
            if (pool) vkDestroyCommandPool(device.device, pool, nullptr);
        };
        try {
            VkCommandPoolCreateInfo poolInfo{VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO};
            poolInfo.queueFamilyIndex = device.queueFamily;
            check(vkCreateCommandPool(device.device, &poolInfo, nullptr, &pool), "Create DLSS Frame Generation initialization pool");
            VkCommandBufferAllocateInfo allocate{VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO};
            allocate.commandPool = pool;
            allocate.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
            allocate.commandBufferCount = 1;
            VkCommandBuffer command;
            check(vkAllocateCommandBuffers(device.device, &allocate, &command), "Allocate DLSS Frame Generation initialization command");
            VkCommandBufferBeginInfo begin{VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO};
            begin.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
            check(vkBeginCommandBuffer(command, &begin), "Begin DLSS Frame Generation initialization");
            auto result = NGX_VK_CREATE_DLSSG(command, 1, 1, &feature, parameters, &create);
            if (NVSDK_NGX_FAILED(result) || !feature) {
                error = ngxError("Create DLSS Frame Generation", result);
                cleanup();
                return false;
            }
            check(vkEndCommandBuffer(command), "End DLSS Frame Generation initialization");
            VkFenceCreateInfo fenceInfo{VK_STRUCTURE_TYPE_FENCE_CREATE_INFO};
            check(vkCreateFence(device.device, &fenceInfo, nullptr, &fence), "Create DLSS Frame Generation initialization fence");
            VkSubmitInfo submit{VK_STRUCTURE_TYPE_SUBMIT_INFO};
            submit.commandBufferCount = 1;
            submit.pCommandBuffers = &command;
            check(vkQueueSubmit(device.queue, 1, &submit, fence), "Submit DLSS Frame Generation initialization");
            submitted = true;
            check(vkWaitForFences(device.device, 1, &fence, VK_TRUE, UINT64_MAX), "Wait for DLSS Frame Generation initialization");
        } catch (...) {
            if (submitted) vkDeviceWaitIdle(device.device);
            cleanup();
            throw;
        }
        cleanup();
        featureReady = true;
        return true;
    }

    bool configure(const Image& backbuffer, const Image& depth, std::string& error) {
        if (feature && featureReady && width == backbuffer.width && height == backbuffer.height && format == backbuffer.format
            && renderWidth == depth.width && renderHeight == depth.height) return true;
        if (feature || !slots.empty()) check(vkDeviceWaitIdle(device.device), "Wait for DLSS Frame Generation configuration change");
        release();
        width = backbuffer.width;
        height = backbuffer.height;
        renderWidth = depth.width;
        renderHeight = depth.height;
        format = backbuffer.format;
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_InternalWidth, renderWidth);
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_InternalHeight, renderHeight);
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_DynamicResolution, 0u);
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_ResourceAlwaysProvided_Flags, static_cast<unsigned int>(
            NVSDK_NGX_DLSSG_ResourceFlags_Backbuffer | NVSDK_NGX_DLSSG_ResourceFlags_MVecs | NVSDK_NGX_DLSSG_ResourceFlags_Depth |
            NVSDK_NGX_DLSSG_ResourceFlags_HUDLess | NVSDK_NGX_DLSSG_ResourceFlags_OutputInterpolated |
            NVSDK_NGX_DLSSG_ResourceFlags_OutputReal | NVSDK_NGX_DLSSG_ResourceFlags_OutputDisableInterpolation));
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_ResourceNeverProvided_Flags, static_cast<unsigned int>(
            NVSDK_NGX_DLSSG_ResourceFlags_UI | NVSDK_NGX_DLSSG_ResourceFlags_UIAlpha | NVSDK_NGX_DLSSG_ResourceFlags_BidirectionalDistortionField));
        // HUDless enables the SDK's ordinary HUD handling. The separate UIR mode
        // requires real UI alpha/color and must not be enabled with inferred data.
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_UserInterfaceRecompositionEnabled, 0u);
        parameters->Set(NVSDK_NGX_DLSSG_Parameter_AsyncCreateEnabled, 0u);
        NVSDK_NGX_DLSSG_Create_Params create{};
        create.Width = width;
        create.Height = height;
        create.RenderWidth = renderWidth;
        create.RenderHeight = renderHeight;
        create.NativeBackbufferFormat = format;
        return createFeature(create, error);
    }
};

NgxFrameGeneration::NgxFrameGeneration(const DeviceInfo& device) : impl_(std::make_unique<Impl>(device)) {}
NgxFrameGeneration::~NgxFrameGeneration() = default;

bool NgxFrameGeneration::supported(NVSDK_NGX_Parameter* capabilities, std::string& reason) {
    reason.clear();
    if (!capabilities) {
        reason = "NGX capability parameters are unavailable";
        return false;
    }
    int updateRequired = 0;
    capabilities->Get(NVSDK_NGX_Parameter_FrameGeneration_NeedsUpdatedDriver, &updateRequired);
    if (updateRequired) {
        unsigned int major = 0, minor = 0;
        capabilities->Get(NVSDK_NGX_Parameter_FrameGeneration_MinDriverVersionMajor, &major);
        capabilities->Get(NVSDK_NGX_Parameter_FrameGeneration_MinDriverVersionMinor, &minor);
        reason = "DLSS Frame Generation requires NVIDIA driver " + std::to_string(major) + '.' + std::to_string(minor) + " or newer";
        return false;
    }
    int available = 0;
    auto result = capabilities->Get(NVSDK_NGX_Parameter_FrameGeneration_Available, &available);
    if (NVSDK_NGX_FAILED(result) || !available) {
        int initialization = 0;
        capabilities->Get(NVSDK_NGX_Parameter_FrameGeneration_FeatureInitResult, &initialization);
        reason = initialization ? ngxError("Initialize DLSS Frame Generation", static_cast<NVSDK_NGX_Result>(initialization))
            : NVSDK_NGX_FAILED(result) ? ngxError("Query DLSS Frame Generation support", result)
            : "DLSS Frame Generation is not available on this device/platform";
        return false;
    }
    return true;
}

bool NgxFrameGeneration::dispatch(VkCommandBuffer command, uint32_t slotIndex, const Image& backbuffer,
                                 const Image& hudless, const Image& depth, const Image& motion,
                                 const FrameData& frame, FrameGenerationOutputs& outputs, std::string& error) {
    outputs = {};
    error.clear();
    auto& state = *impl_;
    if (slotIndex < state.slots.size()) state.slots[slotIndex].valid = false;
    try {
        if (!command || slotIndex >= 16 || !backbuffer.image || !backbuffer.view || !hudless.image || !hudless.view
            || !depth.image || !depth.view || !motion.image || !motion.view) throw std::runtime_error("Incomplete DLSS Frame Generation resources");
        if (!state.device.storageRead || !state.device.storageWrite) throw std::runtime_error("DLSS Frame Generation requires storage-image reads and writes without a format");
        if (!state.device.bufferDeviceAddress) throw std::runtime_error("DLSS Frame Generation requires enabled bufferDeviceAddress");
        if (!backbuffer.width || !backbuffer.height || hudless.width != backbuffer.width || hudless.height != backbuffer.height
            || hudless.format != backbuffer.format || depth.width != motion.width || depth.height != motion.height
            || !depth.width || !depth.height || frame.renderWidth != depth.width || frame.renderHeight != depth.height
            || frame.outputWidth != backbuffer.width || frame.outputHeight != backbuffer.height) throw std::runtime_error("DLSS Frame Generation viewport/resources do not match");
        VkFormatProperties properties;
        vkGetPhysicalDeviceFormatProperties(state.device.physicalDevice, backbuffer.format, &properties);
        if (!(properties.optimalTilingFeatures & VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT)) throw std::runtime_error("DLSS Frame Generation backbuffer format cannot be used as a storage image");
        Matrix inverseProjection, inverseView, inversePreviousView, inverseCurrent, inversePrevious;
        Matrix current = multiply(frame.viewToClip, frame.worldToView);
        Matrix previous = multiply(frame.previousViewToClip, frame.previousWorldToView);
        if (!inverse(frame.viewToClip, inverseProjection) || !inverse(frame.worldToView, inverseView)
            || !inverse(frame.previousWorldToView, inversePreviousView) || !inverse(current, inverseCurrent)
            || !inverse(previous, inversePrevious)) throw std::runtime_error("DLSS Frame Generation camera matrices are not invertible");
        if (!std::isfinite(frame.verticalFov) || !std::isfinite(frame.nearPlane) || !std::isfinite(frame.farPlane)
            || frame.nearPlane <= 0 || frame.farPlane <= frame.nearPlane) throw std::runtime_error("DLSS Frame Generation camera range is invalid");
        if (!state.configure(backbuffer, depth, error)) return false;
        if (state.slots.size() <= slotIndex) state.slots.resize(slotIndex + 1);
        auto& slot = state.slots[slotIndex];
        slot.valid = false;
        if (!slot.disable) state.createSlot(slot);
        bool reset = frame.reset || state.reset || !state.recorded || frame.frameIndex != state.lastFrame + 1u;
        auto position = state.cameraPosition;
        for (size_t i = 0; i < 3; ++i) position[i] = reset ? 0.0 : position[i] + inverseView[12 + i] - inversePreviousView[12 + i];
        NVSDK_NGX_DLSSG_Opt_Eval_Params camera{};
        // Column-major/column-vector engine storage is identical to the bytes of
        // the transposed row-vector matrices required by NGX.
        auto copy = [](float (&target)[4][4], const Matrix& source) { std::memcpy(target, source.data(), sizeof(target)); };
        copy(camera.cameraViewToClip, frame.viewToClip);
        copy(camera.clipToCameraView, inverseProjection);
        copy(camera.clipToPrevClip, multiply(previous, inverseCurrent));
        copy(camera.prevClipToClip, multiply(current, inversePrevious));
        for (int i = 0; i < 4; ++i) camera.clipToLensClip[i][i] = 1;
        camera.jitterOffset[0] = 2.0f * frame.jitterX / frame.renderWidth;
        camera.jitterOffset[1] = 2.0f * frame.jitterY / frame.renderHeight;
        camera.mvecScale[0] = camera.mvecScale[1] = 1;
        for (size_t i = 0; i < 3; ++i) {
            camera.cameraPos[i] = static_cast<float>(position[i]);
            camera.cameraRight[i] = inverseView[i];
            camera.cameraUp[i] = inverseView[4 + i];
            camera.cameraFwd[i] = -inverseView[8 + i];
        }
        camera.cameraNear = frame.nearPlane;
        camera.cameraFar = frame.farPlane;
        camera.cameraFOV = frame.verticalFov;
        camera.cameraAspectRatio = static_cast<float>(frame.outputWidth) / frame.outputHeight;
        camera.cameraMotionIncluded = true;
        camera.depthInverted = false;
        camera.colorBuffersHDR = false; // Input is the display-encoded post-composite image.
        camera.reset = reset;
        camera.orthoProjection = std::abs(frame.viewToClip[15]) > 0.5f;
        camera.motionVectorsInvalidValue = std::numeric_limits<float>::max();
        camera.motionVectorsDilated = false;
        camera.menuDetectionEnabled = true;
        camera.mvecsSubrectSize = {motion.width, motion.height};
        camera.depthSubrectSize = {depth.width, depth.height};
        camera.hudLessSubrectSize = {hudless.width, hudless.height};
        camera.backbufferSubrectSize = {backbuffer.width, backbuffer.height};
        camera.outputInterpSubrectSize = camera.outputRealSubrectSize = camera.backbufferSubrectSize;
        state.parameters->Set(NVSDK_NGX_DLSSG_Parameter_BackbufferFrameID, static_cast<unsigned long long>(frame.frameIndex));
        state.parameters->Set(NVSDK_NGX_DLSSG_Parameter_MvecJittered, 0u);

        // NGX receives GENERAL resources and does not own the engine's layouts.
        // Restore every borrowed input, including failure paths, before returning.
        std::vector<const Image*> inputs;
        for (const Image* image : {&backbuffer, &hudless, &depth, &motion}) {
            if (std::none_of(inputs.begin(), inputs.end(), [&](const Image* other) { return other->image == image->image; })) {
                inputs.push_back(image);
                imageBarrier(command, *image, image->layout, VK_IMAGE_LAYOUT_GENERAL,
                    VK_ACCESS_MEMORY_WRITE_BIT | VK_ACCESS_MEMORY_READ_BIT, VK_ACCESS_SHADER_READ_BIT);
            }
        }
        for (auto* owned : {&slot.interpolated, &slot.real}) {
            imageBarrier(command, owned->image, owned->image.layout, VK_IMAGE_LAYOUT_GENERAL,
                owned->image.layout == VK_IMAGE_LAYOUT_UNDEFINED ? 0 : VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT,
                VK_ACCESS_SHADER_WRITE_BIT);
            owned->image.layout = VK_IMAGE_LAYOUT_GENERAL;
        }
        VkBufferMemoryBarrier buffer{VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER};
        buffer.srcAccessMask = VK_ACCESS_SHADER_WRITE_BIT | VK_ACCESS_HOST_READ_BIT;
        buffer.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        buffer.srcQueueFamilyIndex = buffer.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
        buffer.buffer = slot.disable;
        buffer.offset = 0;
        buffer.size = VK_WHOLE_SIZE;
        vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT | VK_PIPELINE_STAGE_HOST_BIT,
            VK_PIPELINE_STAGE_TRANSFER_BIT, 0, 0, nullptr, 1, &buffer, 0, nullptr);
        vkCmdFillBuffer(command, slot.disable, 0, 4, 1u);
        buffer.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        buffer.dstAccessMask = VK_ACCESS_SHADER_WRITE_BIT;
        vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            0, 0, nullptr, 1, &buffer, 0, nullptr);
        auto inputColor = resource(backbuffer, false), inputHudless = resource(hudless, false);
        auto inputDepth = resource(depth, false), inputMotion = resource(motion, false);
        auto outputInterpolated = resource(slot.interpolated.image, true), outputReal = resource(slot.real.image, true);
        NVSDK_NGX_Resource_VK disable{};
        disable.Type = NVSDK_NGX_RESOURCE_VK_TYPE_VK_BUFFER;
        disable.ReadWrite = true;
        disable.Resource.BufferInfo = {slot.disable, 4};
        NVSDK_NGX_VK_DLSSG_Eval_Params resources{};
        resources.pBackbuffer = &inputColor;
        resources.pHudless = &inputHudless;
        resources.pDepth = &inputDepth;
        resources.pMVecs = &inputMotion;
        resources.pOutputInterpFrame = &outputInterpolated;
        resources.pOutputRealFrame = &outputReal;
        resources.pOutputDisableInterpolation = &disable;
        auto result = NGX_VK_EVALUATE_DLSSG(command, state.feature, state.parameters, &resources, &camera);
        for (const auto* image : inputs) imageBarrier(command, *image, VK_IMAGE_LAYOUT_GENERAL, image->layout,
            VK_ACCESS_SHADER_READ_BIT, VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT);
        for (auto* owned : {&slot.interpolated, &slot.real}) {
            imageBarrier(command, owned->image, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT);
            owned->image.layout = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
        }
        buffer.srcAccessMask = VK_ACCESS_SHADER_WRITE_BIT;
        buffer.dstAccessMask = VK_ACCESS_HOST_READ_BIT;
        vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_HOST_BIT,
            0, 0, nullptr, 1, &buffer, 0, nullptr);
        if (NVSDK_NGX_FAILED(result)) {
            state.reset = true;
            error = ngxError("Evaluate DLSS Frame Generation", result);
            return false;
        }
        state.cameraPosition = position;
        state.recorded = true;
        state.reset = false;
        state.lastFrame = frame.frameIndex;
        slot.valid = true;
        slot.reset = reset;
        outputs = {slot.interpolated.image, slot.real.image, slot.disable, slotIndex, reset};
        return true;
    } catch (const std::exception& failure) {
        state.reset = true;
        error = failure.what();
        return false;
    }
}

bool NgxFrameGeneration::interpolationAllowed(uint32_t index, std::string* error) const {
    if (error) error->clear();
    const auto& state = *impl_;
    if (index >= state.slots.size()) return false;
    const auto& slot = state.slots[index];
    if (!slot.valid || slot.reset || !slot.mapped) return false;
    if (!slot.coherent) {
        VkMappedMemoryRange range{VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE};
        range.memory = slot.disableMemory;
        range.offset = 0;
        range.size = VK_WHOLE_SIZE;
        auto result = vkInvalidateMappedMemoryRanges(state.device.device, 1, &range);
        if (result != VK_SUCCESS) {
            if (error) *error = "Could not invalidate the completed DLSS Frame Generation disable flag";
            return false;
        }
    }
    return static_cast<const uint8_t*>(slot.mapped)[0] == 0;
}

void NgxFrameGeneration::invalidate() noexcept {
    impl_->reset = true;
    for (auto& slot : impl_->slots) slot.valid = false;
}

}
