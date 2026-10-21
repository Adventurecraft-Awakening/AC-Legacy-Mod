#include "FsrFrameGeneration.hpp"
#include "FsrBackend.hpp"
#include <FidelityFX/host/backends/vk/ffx_vk.h>
#include <FidelityFX/host/ffx_frameinterpolation.h>
#include <FidelityFX/host/ffx_opticalflow.h>
#include <algorithm>
#include <array>
#include <cmath>
#include <cstddef>
#include <cstring>
#include <stdexcept>
#include <vector>

namespace ac {
namespace {
void check(VkResult result, const char* operation) {
    if (result != VK_SUCCESS) throw std::runtime_error(std::string(operation) + ": Vulkan " + std::to_string(result));
}
void checkFfx(FfxErrorCode result, const char* operation) {
    if (result != FFX_OK) throw std::runtime_error(std::string(operation) + ": FidelityFX " + std::to_string(result));
}
VKAPI_ATTR PFN_vkVoidFunction VKAPI_CALL deviceProcedure(VkDevice device, const char* name) {
    auto result = vkGetDeviceProcAddr(device, name);
    if (!result && name) {
        std::string core(name);
        if (core.size() > 3 && core.compare(core.size() - 3, 3, "KHR") == 0) {
            core.resize(core.size() - 3);
            result = vkGetDeviceProcAddr(device, core.c_str());
        }
    }
    return result;
}
void transition(VkCommandBuffer command, const Image& image, VkImageLayout before, VkImageLayout after) {
    VkImageMemoryBarrier barrier{VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER};
    barrier.srcAccessMask = before == VK_IMAGE_LAYOUT_UNDEFINED ? 0 : VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT;
    barrier.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT | VK_ACCESS_MEMORY_WRITE_BIT;
    barrier.oldLayout = before; barrier.newLayout = after;
    barrier.srcQueueFamilyIndex = barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image.image;
    barrier.subresourceRange = {image.format == VK_FORMAT_D32_SFLOAT ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT, 0, 1, 0, 1};
    vkCmdPipelineBarrier(command, before == VK_IMAGE_LAYOUT_UNDEFINED ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
        VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, 0, 0, nullptr, 0, nullptr, 1, &barrier);
}
FfxResource resource(const Image& image, FfxResourceStates state, const wchar_t* name) {
    VkImageCreateInfo description{VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO};
    description.imageType = VK_IMAGE_TYPE_2D; description.format = image.format;
    description.extent = {image.width, image.height, 1}; description.mipLevels = description.arrayLayers = 1;
    description.usage = VK_IMAGE_USAGE_SAMPLED_BIT | (state == FFX_RESOURCE_STATE_UNORDERED_ACCESS ? VK_IMAGE_USAGE_STORAGE_BIT : 0);
    return ffxGetResourceVK(reinterpret_cast<void*>(image.image), ffxGetImageResourceDescriptionVK(image.image, description), name, state);
}
VkFormat format(FfxSurfaceFormat value) {
    switch (value) {
        case FFX_SURFACE_FORMAT_R32_FLOAT: return VK_FORMAT_R32_SFLOAT;
        case FFX_SURFACE_FORMAT_R32_UINT: return VK_FORMAT_R32_UINT;
        case FFX_SURFACE_FORMAT_R16G16_FLOAT: return VK_FORMAT_R16G16_SFLOAT;
        case FFX_SURFACE_FORMAT_R16G16_SINT: return VK_FORMAT_R16G16_SINT;
        default: throw std::runtime_error("Unexpected FidelityFX frame-generation shared resource format");
    }
}
struct alignas(32) ScratchBlock { std::byte bytes[32]; };
struct ContextCreation;
thread_local ContextCreation* activeCreation = nullptr;
struct ContextCreation {
    FfxCreateBackendContextFunc original;
    bool& created;
    ContextCreation* previous;
    ContextCreation(FfxCreateBackendContextFunc function, bool& result)
        : original(function), created(result), previous(activeCreation) { activeCreation = this; }
    ~ContextCreation() { activeCreation = previous; }
};
FfxErrorCode trackCreation(FfxInterface* interface, FfxEffect effect, FfxEffectBindlessConfig* config, FfxUInt32* id) {
    if (!activeCreation) return FFX_ERROR_BACKEND_API_ERROR;
    auto result = activeCreation->original(interface, effect, config, id);
    if (result == FFX_OK) activeCreation->created = true;
    return result;
}
struct Backend {
    VkDeviceContext native{};
    std::vector<ScratchBlock> scratch;
    FfxInterface interface{};
    void initialize(const DeviceInfo& device) {
        native = {device.device, device.physicalDevice, deviceProcedure};
        size_t bytes = ffxGetScratchMemorySizeVK(device.physicalDevice, 1);
        scratch.resize((bytes + sizeof(ScratchBlock) - 1) / sizeof(ScratchBlock));
        std::memset(scratch.data(), 0, scratch.size() * sizeof(ScratchBlock));
        checkFfx(ffxGetInterfaceVK(&interface, reinterpret_cast<FfxDevice>(&native), scratch.data(),
            scratch.size() * sizeof(ScratchBlock), 1), "Frame-generation Vulkan interface");
    }
};
bool dimensions(const Image& image, uint32_t width, uint32_t height) {
    return image.image && image.width == width && image.height == height && image.layout != VK_IMAGE_LAYOUT_UNDEFINED;
}
void camera(const FrameData& frame, FfxFrameInterpolationPrepareDescription& result) {
    double rows[4][8]{};
    for (int row=0; row<4; ++row) {
        for (int column=0; column<4; ++column) {
            float value=frame.worldToView[column*4+row];
            if (!std::isfinite(value)) throw std::runtime_error("Non-finite frame-generation camera");
            rows[row][column]=value;
        }
        rows[row][row+4]=1;
    }
    for (int column=0; column<4; ++column) {
        int pivot=column;
        for (int row=column+1; row<4; ++row) if (std::abs(rows[row][column])>std::abs(rows[pivot][column])) pivot=row;
        if (std::abs(rows[pivot][column])<1e-12) throw std::runtime_error("Singular frame-generation camera");
        for (int i=0; i<8; ++i) std::swap(rows[column][i],rows[pivot][i]);
        double divisor=rows[column][column]; for (double& value:rows[column]) value/=divisor;
        for (int row=0; row<4; ++row) if (row!=column) {
            double factor=rows[row][column]; for (int i=0; i<8; ++i) rows[row][i]-=factor*rows[column][i];
        }
    }
    for (int axis=0; axis<3; ++axis) {
        double length=0; for(int row=0;row<3;++row) length+=rows[row][axis+4]*rows[row][axis+4];
        length=std::sqrt(length); if (!std::isfinite(length)||length<1e-12) throw std::runtime_error("Invalid frame-generation camera basis");
        for (int row=0; row<3; ++row) {
            float value=static_cast<float>(rows[row][axis+4]/length);
            if(axis==0) result.cameraRight[row]=value;
            else if(axis==1) result.cameraUp[row]=value;
            else result.cameraForward[row]=-value;
        }
    }
    for (int row=0; row<3; ++row) result.cameraPosition[row]=static_cast<float>(rows[row][7]);
}
}

struct FsrFrameGeneration::Impl {
    struct Owned { Image image; VkDeviceMemory memory = VK_NULL_HANDLE; bool fresh = true; };
    struct Slot { Owned output; bool valid = false, reset = true; };
    DeviceInfo device;
    Backend flowBackend, interpolationBackend;
    FfxOpticalflowContext flow{};
    FfxFrameInterpolationContext interpolation{};
    bool flowCreated = false, interpolationCreated = false, ready = false, reset = true, poisoned = false;
    uint32_t width = 0, height = 0, renderWidth = 0, renderHeight = 0, lastFrame = 0;
    std::array<Owned, 5> shared{};
    std::array<Slot, 3> slots{};
    explicit Impl(const DeviceInfo& info) : device(info) {}
    ~Impl() { if (ready || poisoned) vkDeviceWaitIdle(device.device); release(); }
    void destroy(Owned& value) {
        if (value.image.view) vkDestroyImageView(device.device, value.image.view, nullptr);
        if (value.image.image) vkDestroyImage(device.device, value.image.image, nullptr);
        if (value.memory) vkFreeMemory(device.device, value.memory, nullptr);
        value = {};
    }
    void release() {
        if (interpolationCreated) ffxFrameInterpolationContextDestroy(&interpolation);
        if (flowCreated) ffxOpticalflowContextDestroy(&flow);
        interpolationCreated = flowCreated = ready = false;
        for (auto& value : shared) destroy(value);
        for (auto& slot : slots) { destroy(slot.output); slot.valid = false; slot.reset = true; }
        std::memset(&flow, 0, sizeof(flow)); std::memset(&interpolation, 0, sizeof(interpolation));
        flowBackend.scratch.clear(); interpolationBackend.scratch.clear();
        reset = true; poisoned = false;
    }
    void allocate(Owned& result, VkFormat imageFormat, uint32_t w, uint32_t h) {
        result.image.format = imageFormat; result.image.width = w; result.image.height = h;
        result.image.layout = VK_IMAGE_LAYOUT_GENERAL;
        VkImageCreateInfo description{VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO};
        description.imageType = VK_IMAGE_TYPE_2D; description.format = imageFormat;
        description.extent = {w,h,1}; description.mipLevels = description.arrayLayers = 1;
        description.samples = VK_SAMPLE_COUNT_1_BIT; description.tiling = VK_IMAGE_TILING_OPTIMAL;
        description.usage = VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        check(vkCreateImage(device.device, &description, nullptr, &result.image.image), "Frame-generation image creation");
        VkMemoryRequirements requirements; vkGetImageMemoryRequirements(device.device, result.image.image, &requirements);
        VkPhysicalDeviceMemoryProperties memory; vkGetPhysicalDeviceMemoryProperties(device.physicalDevice, &memory);
        uint32_t type = UINT32_MAX;
        for (uint32_t i=0; i<memory.memoryTypeCount; ++i) if (requirements.memoryTypeBits & (1u<<i)) {
            if (type == UINT32_MAX) type = i;
            if (memory.memoryTypes[i].propertyFlags & VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT) { type=i; break; }
        }
        if (type == UINT32_MAX) throw std::runtime_error("No frame-generation image memory type");
        VkMemoryAllocateInfo allocation{VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO}; allocation.allocationSize=requirements.size; allocation.memoryTypeIndex=type;
        check(vkAllocateMemory(device.device, &allocation, nullptr, &result.memory), "Frame-generation image allocation");
        check(vkBindImageMemory(device.device, result.image.image, result.memory, 0), "Frame-generation image binding");
        VkImageViewCreateInfo view{VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO}; view.image=result.image.image; view.viewType=VK_IMAGE_VIEW_TYPE_2D;
        view.format=imageFormat; view.subresourceRange={VK_IMAGE_ASPECT_COLOR_BIT,0,1,0,1};
        check(vkCreateImageView(device.device, &view, nullptr, &result.image.view), "Frame-generation image view");
    }
    void configure(const FrameData& frame) {
        if (ready && !poisoned && width==frame.outputWidth && height==frame.outputHeight
            && renderWidth==frame.renderWidth && renderHeight==frame.renderHeight) return;
        if (ready || poisoned) check(vkDeviceWaitIdle(device.device), "Frame-generation resize retirement");
        release(); width=frame.outputWidth; height=frame.outputHeight; renderWidth=frame.renderWidth; renderHeight=frame.renderHeight;
        flowBackend.initialize(device); interpolationBackend.initialize(device);
        FfxOpticalflowContextDescription flowDescription{};
        flowDescription.backendInterface=flowBackend.interface; flowDescription.resolution={width,height};
        {
            ContextCreation tracking(flowDescription.backendInterface.fpCreateBackendContext, flowCreated);
            flowDescription.backendInterface.fpCreateBackendContext=trackCreation;
            checkFfx(ffxOpticalflowContextCreate(&flow, &flowDescription), "Optical-flow context creation");
        }
        FfxFrameInterpolationContextDescription interpolationDescription{};
        interpolationDescription.backendInterface=interpolationBackend.interface;
        interpolationDescription.maxRenderSize={renderWidth,renderHeight}; interpolationDescription.displaySize={width,height};
        interpolationDescription.backBufferFormat=interpolationDescription.previousInterpolationSourceFormat=FFX_SURFACE_FORMAT_R8G8B8A8_UNORM;
        {
            ContextCreation tracking(interpolationDescription.backendInterface.fpCreateBackendContext, interpolationCreated);
            interpolationDescription.backendInterface.fpCreateBackendContext=trackCreation;
            checkFfx(ffxFrameInterpolationContextCreate(&interpolation, &interpolationDescription), "Frame-interpolation context creation");
        }
        FfxOpticalflowSharedResourceDescriptions opticalResources{};
        FfxFrameInterpolationSharedResourceDescriptions interpolationResources{};
        checkFfx(ffxOpticalflowGetSharedResourceDescriptions(&flow, &opticalResources), "Optical-flow shared descriptions");
        checkFfx(ffxFrameInterpolationGetSharedResourceDescriptions(&interpolation, &interpolationResources), "Interpolation shared descriptions");
        const FfxCreateResourceDescription* descriptions[]{&opticalResources.opticalFlowVector,&opticalResources.opticalFlowSCD,
            &interpolationResources.dilatedDepth,&interpolationResources.dilatedMotionVectors,&interpolationResources.reconstructedPrevNearestDepth};
        for (size_t i=0; i<shared.size(); ++i) {
            const auto& desc=descriptions[i]->resourceDescription;
            allocate(shared[i],format(desc.format),desc.width,desc.height);
        }
        ready=true;
    }
};

bool FsrFrameGeneration::supported(const DeviceInfo& device, std::string& reason) {
    FsrDevice sr{device.device,device.physicalDevice,device.queue,device.queueFamily,vkGetDeviceProcAddr,
        device.storageExtended,device.storageRead,device.storageWrite};
    if (!FsrBackend::supported(sr,reason)) return false;
    if (!device.storageRead) { reason="FSR Frame Generation requires enabled formatless storage-image reads"; return false; }
    VkPhysicalDeviceSubgroupProperties subgroup{VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES};
    VkPhysicalDeviceProperties2 properties{VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROPERTIES_2}; properties.pNext=&subgroup;
    vkGetPhysicalDeviceProperties2(device.physicalDevice,&properties);
    if (!(subgroup.supportedOperations & VK_SUBGROUP_FEATURE_ARITHMETIC_BIT)
        || properties.properties.limits.maxComputeWorkGroupInvocations < 256
        || properties.properties.limits.maxComputeWorkGroupSize[0] < 256) {
        reason="FSR Frame Generation requires compute subgroup arithmetic and 256-invocation workgroups"; return false;
    }
    reason.clear(); return true;
}
FsrFrameGeneration::FsrFrameGeneration(const DeviceInfo& device) {
    std::string reason; if (!supported(device,reason)) throw std::runtime_error(reason);
    impl=std::make_unique<Impl>(device);
}
FsrFrameGeneration::~FsrFrameGeneration() = default;
bool FsrFrameGeneration::dispatch(VkCommandBuffer command, uint32_t slotIndex, const Image& backbuffer,
    const Image& hudless, const Image& depth, const Image& motion, const FrameData& frame,
    FsrFrameGenerationOutputs& outputs, std::string& error) {
    outputs={};
    if (slotIndex<impl->slots.size()) impl->slots[slotIndex].valid=false;
    if (!command || slotIndex>=impl->slots.size() || !frame.renderWidth || !frame.renderHeight
        || !frame.outputWidth || !frame.outputHeight || frame.renderWidth>frame.outputWidth || frame.renderHeight>frame.outputHeight
        || !dimensions(backbuffer,frame.outputWidth,frame.outputHeight) || !dimensions(hudless,frame.outputWidth,frame.outputHeight)
        || !dimensions(depth,frame.renderWidth,frame.renderHeight) || !dimensions(motion,frame.renderWidth,frame.renderHeight)
        || backbuffer.format!=VK_FORMAT_R8G8B8A8_UNORM || hudless.format!=backbuffer.format || depth.format!=VK_FORMAT_D32_SFLOAT
        || motion.format!=VK_FORMAT_R16G16B16A16_SFLOAT || !std::isfinite(frame.deltaMs) || frame.deltaMs<0
        || (backbuffer.image==hudless.image && backbuffer.layout!=hudless.layout)
        || !std::isfinite(frame.nearPlane) || !std::isfinite(frame.farPlane) || frame.nearPlane<=0 || frame.farPlane<=frame.nearPlane
        || !std::isfinite(frame.verticalFov) || frame.verticalFov<=0 || frame.verticalFov>=3.14159265f
        || !std::isfinite(frame.jitterX) || !std::isfinite(frame.jitterY)) {
        error="Invalid FSR Frame Generation command, image, or timing"; return false;
    }
    try {
        impl->configure(frame);
        auto& slot=impl->slots[slotIndex];
        if (!slot.output.image.image) impl->allocate(slot.output,VK_FORMAT_R8G8B8A8_UNORM,frame.outputWidth,frame.outputHeight);
        bool restart=impl->reset || frame.reset || frame.frameIndex!=impl->lastFrame+1 || frame.deltaMs>250;
        for (auto& shared : impl->shared) transition(command,shared.image,shared.fresh?VK_IMAGE_LAYOUT_UNDEFINED:VK_IMAGE_LAYOUT_GENERAL,VK_IMAGE_LAYOUT_GENERAL);
        transition(command,slot.output.image,slot.output.fresh?VK_IMAGE_LAYOUT_UNDEFINED:VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,VK_IMAGE_LAYOUT_GENERAL);
        std::array<const Image*,4> inputs{&backbuffer,&hudless,&depth,&motion};
        auto firstImage=[&](size_t index) {
            for(size_t earlier=0; earlier<index; ++earlier) if(inputs[earlier]->image==inputs[index]->image) return false;
            return true;
        };
        for(size_t i=0;i<inputs.size();++i) if(firstImage(i))
            transition(command,*inputs[i],inputs[i]->layout,VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        auto shared=[&](size_t index,const wchar_t* name){return resource(impl->shared[index].image,FFX_RESOURCE_STATE_UNORDERED_ACCESS,name);};
        FfxFrameInterpolationPrepareDescription preparation{};
        preparation.commandList=ffxGetCommandListVK(command); preparation.renderSize={frame.renderWidth,frame.renderHeight};
        preparation.jitterOffset={frame.jitterX,frame.jitterY}; preparation.motionVectorScale={1,1};
        preparation.frameTimeDelta=std::clamp(frame.deltaMs,.001f,1000.0f); preparation.cameraNear=frame.nearPlane; preparation.cameraFar=frame.farPlane;
        preparation.cameraFovAngleVertical=frame.verticalFov; preparation.viewSpaceToMetersFactor=1; preparation.frameID=frame.frameIndex;
        preparation.depth=resource(depth,FFX_RESOURCE_STATE_COMPUTE_READ,L"AC FG depth");
        preparation.motionVectors=resource(motion,FFX_RESOURCE_STATE_COMPUTE_READ,L"AC FG motion");
        preparation.dilatedDepth=shared(2,L"AC FG dilated depth"); preparation.dilatedMotionVectors=shared(3,L"AC FG dilated motion");
        preparation.reconstructedPrevDepth=shared(4,L"AC FG reconstructed depth");
        camera(frame,preparation);
        checkFfx(ffxFrameInterpolationPrepare(&impl->interpolation,&preparation),"Frame-interpolation prepare");
        FfxOpticalflowDispatchDescription optical{}; optical.commandList=preparation.commandList;
        optical.color=resource(hudless,FFX_RESOURCE_STATE_COMPUTE_READ,L"AC FG HUDless"); optical.reset=restart;
        optical.backbufferTransferFunction=FFX_BACKBUFFER_TRANSFER_FUNCTION_SRGB; optical.minMaxLuminance={0,1};
        optical.opticalFlowVector=shared(0,L"AC FG optical flow"); optical.opticalFlowSCD=shared(1,L"AC FG scene change");
        checkFfx(ffxOpticalflowContextDispatch(&impl->flow,&optical),"Optical-flow dispatch");
        FfxFrameInterpolationDispatchDescription interpolation{}; interpolation.commandList=preparation.commandList;
        interpolation.displaySize={frame.outputWidth,frame.outputHeight}; interpolation.renderSize=preparation.renderSize;
        interpolation.currentBackBuffer=resource(backbuffer,FFX_RESOURCE_STATE_COMPUTE_READ,L"AC FG presentation");
        interpolation.currentBackBuffer_HUDLess=optical.color;
        interpolation.output=resource(slot.output.image,FFX_RESOURCE_STATE_UNORDERED_ACCESS,L"AC FG interpolated");
        interpolation.interpolationRect={0,0,static_cast<int32_t>(frame.outputWidth),static_cast<int32_t>(frame.outputHeight)};
        interpolation.opticalFlowVector=optical.opticalFlowVector; interpolation.opticalFlowSceneChangeDetection=optical.opticalFlowSCD;
        interpolation.opticalFlowBufferSize={impl->shared[0].image.width,impl->shared[0].image.height};
        interpolation.opticalFlowScale={1.0f/frame.outputWidth,1.0f/frame.outputHeight}; interpolation.opticalFlowBlockSize=8;
        interpolation.cameraNear=frame.nearPlane; interpolation.cameraFar=frame.farPlane; interpolation.cameraFovAngleVertical=frame.verticalFov;
        interpolation.viewSpaceToMetersFactor=1; interpolation.frameTimeDelta=preparation.frameTimeDelta; interpolation.reset=restart;
        interpolation.backBufferTransferFunction=FFX_BACKBUFFER_TRANSFER_FUNCTION_SRGB; interpolation.minMaxLuminance[1]=1;
        interpolation.frameID=frame.frameIndex; interpolation.dilatedDepth=preparation.dilatedDepth;
        interpolation.dilatedMotionVectors=preparation.dilatedMotionVectors; interpolation.reconstructedPrevDepth=preparation.reconstructedPrevDepth;
        checkFfx(ffxFrameInterpolationDispatch(&impl->interpolation,&interpolation),"Frame-interpolation dispatch");
        for(size_t i=0;i<inputs.size();++i) if(firstImage(i))
            transition(command,*inputs[i],VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,inputs[i]->layout);
        transition(command,slot.output.image,VK_IMAGE_LAYOUT_GENERAL,VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
        for (auto& shared : impl->shared) shared.fresh=false;
        slot.output.fresh=false; slot.output.image.layout=VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL; slot.valid=true; slot.reset=restart;
        impl->reset=false; impl->lastFrame=frame.frameIndex;
        outputs={slot.output.image,backbuffer,slotIndex,restart}; error.clear(); return true;
    } catch (const std::exception& exception) {
        impl->poisoned=true; impl->reset=true; error=exception.what(); return false;
    }
}
bool FsrFrameGeneration::interpolationAllowed(uint32_t slot, std::string* error) const {
    if (error) error->clear();
    return !impl->poisoned && slot<impl->slots.size() && impl->slots[slot].valid && !impl->slots[slot].reset;
}
void FsrFrameGeneration::invalidate() noexcept {
    impl->reset=true; for(auto& slot:impl->slots) slot.valid=false;
}
void FsrFrameGeneration::abort() {
    check(vkDeviceWaitIdle(impl->device.device),"Frame-generation aborted-recording retirement"); impl->release();
}
}
