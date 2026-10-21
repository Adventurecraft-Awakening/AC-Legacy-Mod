#include "NgxBackend.hpp"
#include "NgxFrameGeneration.hpp"
#include <nvsdk_ngx_helpers_vk.h>
#include <nvsdk_ngx_helpers_dlssd_vk.h>
#include <algorithm>
#include <filesystem>
#include <sstream>
#include <stdexcept>

namespace ac {
namespace {
std::string ngxError(const char* operation, NVSDK_NGX_Result result) {
    std::ostringstream s; s << operation << " (NGX 0x" << std::hex << uint32_t(result) << ')'; return s.str();
}
struct Discovery {
    std::wstring path, logs;
    const wchar_t* paths[1];
    NVSDK_NGX_FeatureCommonInfo common{};
    NVSDK_NGX_FeatureDiscoveryInfo info{};
    explicit Discovery(const std::string& root) {
#ifdef _WIN32
        path = (std::filesystem::path(root)/"dlss/lib/Windows_x86_64/rel").wstring();
#else
        path = (std::filesystem::path(root)/"dlss/lib/Linux_x86_64/rel").wstring();
#endif
        auto directory = std::filesystem::path(root)/"runtime/ngx";
        std::filesystem::create_directories(directory);
        logs = directory.wstring(); paths[0] = path.c_str(); common.PathListInfo = {paths,1};
        info.SDKVersion = NVSDK_NGX_Version_API;
        info.Identifier.IdentifierType = NVSDK_NGX_Application_Identifier_Type_Project_Id;
        info.Identifier.v.ProjectDesc = {PROJECT_ID,NVSDK_NGX_ENGINE_TYPE_CUSTOM,"0.7.0"};
        info.ApplicationDataPath = logs.c_str(); info.FeatureInfo = &common;
    }
};
NVSDK_NGX_Resource_VK resource(const Image& image, bool writable = false) {
    return NVSDK_NGX_Create_ImageView_Resource_VK(image.view,image.image,
        {image.format == VK_FORMAT_D32_SFLOAT ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT,0,1,0,1},
        image.format,image.width,image.height,writable);
}
NVSDK_NGX_PerfQuality_Value quality(const FrameData& f) {
    float ratio = float(f.renderWidth)/f.outputWidth;
    return ratio >= 0.99f ? NVSDK_NGX_PerfQuality_Value_DLAA : ratio >= 0.65f ? NVSDK_NGX_PerfQuality_Value_MaxQuality
        : ratio >= 0.55f ? NVSDK_NGX_PerfQuality_Value_Balanced : NVSDK_NGX_PerfQuality_Value_MaxPerf;
}
}
std::vector<std::string> ngxExtensions(const std::string& root, VkInstance instance, VkPhysicalDevice physical) {
    std::vector<std::string> names;
    try {
        Discovery discovery(root);
        for (auto type : {NVSDK_NGX_Feature_SuperSampling,NVSDK_NGX_Feature_RayReconstruction,NVSDK_NGX_Feature_FrameGeneration}) {
            discovery.info.FeatureID = type; uint32_t count = 0; VkExtensionProperties* extensions = nullptr;
            auto result = physical ? NVSDK_NGX_VULKAN_GetFeatureDeviceExtensionRequirements(instance,physical,&discovery.info,&count,&extensions)
                : NVSDK_NGX_VULKAN_GetFeatureInstanceExtensionRequirements(&discovery.info,&count,&extensions);
            if (NVSDK_NGX_SUCCEED(result)) for (uint32_t i=0;i<count;i++) {
                std::string name = extensions[i].extensionName;
                if (std::find(names.begin(),names.end(),name)==names.end()) names.push_back(name);
            }
        }
    } catch (...) { /* An unavailable optional runtime must not prevent raster device creation. */ }
    return names;
}
NgxBackend::NgxBackend(const DeviceInfo& device, const std::vector<std::string>& enabled) : device_(device) {
    reasons_.fill("NVIDIA NGX is unavailable");
    VkPhysicalDeviceProperties properties{}; vkGetPhysicalDeviceProperties(device.physicalDevice,&properties);
    if (properties.vendorID != 0x10de) { reasons_.fill("DLSS requires a compatible NVIDIA GPU"); return; }
    if (!device.bufferDeviceAddress || !device.storageRead || !device.storageWrite) {
        reasons_.fill("DLSS requires enabled buffer device addresses and formatless storage-image access"); return;
    }
    Discovery discovery(device.sdkRoot);
    for (auto& extension : ngxExtensions(device.sdkRoot,device.instance,device.physicalDevice)) {
        if (std::find(enabled.begin(),enabled.end(),extension)==enabled.end()) {
            reasons_.fill("Missing enabled Vulkan extension " + extension); return;
        }
    }
    auto result = NVSDK_NGX_VULKAN_Init_with_ProjectID(PROJECT_ID,NVSDK_NGX_ENGINE_TYPE_CUSTOM,"0.7.0",
        discovery.logs.c_str(),device.instance,device.physicalDevice,device.device,vkGetInstanceProcAddr,vkGetDeviceProcAddr,&discovery.common);
    if (NVSDK_NGX_FAILED(result)) { reasons_.fill(ngxError("Initialize NVIDIA NGX",result)); return; }
    initialized_ = true;
    result = NVSDK_NGX_VULKAN_GetCapabilityParameters(&capabilities_);
    if (NVSDK_NGX_FAILED(result)) { reasons_.fill(ngxError("Query NVIDIA NGX capabilities",result)); return; }
    const char* keys[] = {NVSDK_NGX_Parameter_SuperSampling_Available,NVSDK_NGX_Parameter_SuperSamplingDenoising_Available};
    for (int i=0;i<2;i++) { int available = 0; result = capabilities_->Get(keys[i],&available);
        reasons_[i] = NVSDK_NGX_SUCCEED(result) && available ? "" : "GPU or driver does not support this DLSS feature"; }
    NgxFrameGeneration::supported(capabilities_,reasons_[2]);
}
bool NgxBackend::supported(int feature) const { return feature>=0 && feature<3 && reasons_[feature].empty(); }
const std::string& NgxBackend::reason(int feature) const { return reasons_[feature>=0 && feature<3 ? feature : 0]; }
void NgxBackend::destroyFeature() {
    if (feature_) { NVSDK_NGX_VULKAN_ReleaseFeature(feature_); feature_ = nullptr; }
    if (parameters_) { NVSDK_NGX_VULKAN_DestroyParameters(parameters_); parameters_ = nullptr; }
    algorithm_ = 0;
}
NgxBackend::~NgxBackend() {
    destroyFeature();
    if (capabilities_) NVSDK_NGX_VULKAN_DestroyParameters(capabilities_);
    if (initialized_) NVSDK_NGX_VULKAN_Shutdown1(device_.device);
}
void NgxBackend::abort() { vkDeviceWaitIdle(device_.device); destroyFeature(); }
bool NgxBackend::createFeature(int algorithm, const FrameData& f, std::string& error) {
    if (feature_ && algorithm_==algorithm && renderWidth_==f.renderWidth && renderHeight_==f.renderHeight
        && outputWidth_==f.outputWidth && outputHeight_==f.outputHeight && hdr_==f.hdr) return true;
    vkDeviceWaitIdle(device_.device); destroyFeature();
    auto result = NVSDK_NGX_VULKAN_AllocateParameters(&parameters_);
    if (NVSDK_NGX_FAILED(result)) { error = ngxError("Allocate NGX parameters",result); return false; }
    VkCommandPool pool{}; VkCommandBuffer command{}; VkFence fence{};
    auto cleanup = [&] { if (fence) vkDestroyFence(device_.device,fence,nullptr); if (pool) vkDestroyCommandPool(device_.device,pool,nullptr); };
    VkCommandPoolCreateInfo pi{VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO}; pi.queueFamilyIndex = device_.queueFamily;
    if (vkCreateCommandPool(device_.device,&pi,nullptr,&pool)!=VK_SUCCESS) { error="Cannot create NGX initialization command pool"; cleanup(); return false; }
    VkCommandBufferAllocateInfo ai{VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO}; ai.commandPool=pool; ai.level=VK_COMMAND_BUFFER_LEVEL_PRIMARY; ai.commandBufferCount=1;
    VkCommandBufferBeginInfo bi{VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO}; bi.flags=VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
    if (vkAllocateCommandBuffers(device_.device,&ai,&command)!=VK_SUCCESS || vkBeginCommandBuffer(command,&bi)!=VK_SUCCESS) {
        error="Cannot begin NGX initialization"; cleanup(); return false; }
    int flags = NVSDK_NGX_DLSS_Feature_Flags_MVLowRes | (f.hdr ? NVSDK_NGX_DLSS_Feature_Flags_IsHDR : 0);
    if (algorithm==2) {
        NVSDK_NGX_DLSSD_Create_Params create{}; create.InWidth=f.renderWidth; create.InHeight=f.renderHeight;
        create.InTargetWidth=f.outputWidth; create.InTargetHeight=f.outputHeight; create.InPerfQualityValue=quality(f);
        create.InFeatureCreateFlags=flags; create.InDenoiseMode=NVSDK_NGX_DLSS_Denoise_Mode_DLUnified;
        create.InRoughnessMode=NVSDK_NGX_DLSS_Roughness_Mode_Packed; create.InUseHWDepth=NVSDK_NGX_DLSS_Depth_Type_HW;
        result=NGX_VULKAN_CREATE_DLSSD_EXT1(device_.device,command,1,1,&feature_,parameters_,&create);
    } else {
        NVSDK_NGX_DLSS_Create_Params create{}; create.Feature.InWidth=f.renderWidth; create.Feature.InHeight=f.renderHeight;
        create.Feature.InTargetWidth=f.outputWidth; create.Feature.InTargetHeight=f.outputHeight;
        create.Feature.InPerfQualityValue=quality(f); create.InFeatureCreateFlags=flags;
        result=NGX_VULKAN_CREATE_DLSS_EXT(command,1,1,&feature_,parameters_,&create);
    }
    if (NVSDK_NGX_FAILED(result)) { error=ngxError("Create DLSS reconstruction",result); cleanup(); destroyFeature(); return false; }
    VkFenceCreateInfo fi{VK_STRUCTURE_TYPE_FENCE_CREATE_INFO};
    VkSubmitInfo submit{VK_STRUCTURE_TYPE_SUBMIT_INFO}; submit.commandBufferCount=1; submit.pCommandBuffers=&command;
    if (vkEndCommandBuffer(command)!=VK_SUCCESS || vkCreateFence(device_.device,&fi,nullptr,&fence)!=VK_SUCCESS
        || vkQueueSubmit(device_.queue,1,&submit,fence)!=VK_SUCCESS || vkWaitForFences(device_.device,1,&fence,VK_TRUE,UINT64_MAX)!=VK_SUCCESS) {
        error="DLSS initialization submission failed"; vkDeviceWaitIdle(device_.device); cleanup(); destroyFeature(); return false; }
    cleanup(); algorithm_=algorithm; renderWidth_=f.renderWidth; renderHeight_=f.renderHeight;
    outputWidth_=f.outputWidth; outputHeight_=f.outputHeight; hdr_=f.hdr; return true;
}
bool NgxBackend::reconstruct(VkCommandBuffer command, int algorithm, const std::vector<Image>& images,
                             const FrameData& f, std::string& error) {
    if (images.size()!=9 || !supported(algorithm==2 ? 1 : 0)) { error="DLSS reconstruction is unavailable or its inputs are incomplete"; return false; }
    for (const Image& image : images) if (!image.image || !image.view || !image.width || !image.height) {
        error="DLSS received an incomplete borrowed image"; return false;
    }
    if (!createFeature(algorithm,f,error)) { reasons_[algorithm==2 ? 1 : 0]=error; return false; }
    std::array<NVSDK_NGX_Resource_VK,9> resources{};
    for (size_t i=0;i<images.size();i++) {
        imageBarrier(command,images[i],images[i].layout,i==8 ? VK_IMAGE_LAYOUT_GENERAL : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
            VK_ACCESS_MEMORY_WRITE_BIT|VK_ACCESS_MEMORY_READ_BIT,i==8 ? VK_ACCESS_SHADER_WRITE_BIT : VK_ACCESS_SHADER_READ_BIT);
        resources[i]=resource(images[i],i==8);
    }
    NVSDK_NGX_Result result;
    if (algorithm==2) {
        NVSDK_NGX_VK_DLSSD_Eval_Params eval{};
        eval.pInColor=&resources[0]; eval.pInDepth=&resources[1]; eval.pInMotionVectors=&resources[2];
        eval.pInBiasCurrentColorMask=&resources[3]; eval.pInNormals=&resources[4];
        eval.pInDiffuseAlbedo=&resources[5]; eval.pInSpecularAlbedo=&resources[6]; eval.pInSpecularHitDistance=&resources[7];
        eval.pInOutput=&resources[8]; eval.InJitterOffsetX=-f.jitterX; eval.InJitterOffsetY=-f.jitterY;
        eval.InRenderSubrectDimensions={f.renderWidth,f.renderHeight}; eval.InReset=f.reset;
        eval.InMVScaleX=eval.InMVScaleY=1; eval.InPreExposure=eval.InExposureScale=1;
        eval.pInWorldToViewMatrix=const_cast<float*>(f.worldToView.data()); eval.pInViewToClipMatrix=const_cast<float*>(f.viewToClip.data());
        eval.InFrameTimeDeltaInMsec=f.deltaMs;
        result=NGX_VULKAN_EVALUATE_DLSSD_EXT(command,feature_,parameters_,&eval);
    } else {
        NVSDK_NGX_VK_DLSS_Eval_Params eval{}; eval.Feature.pInColor=&resources[0]; eval.Feature.pInOutput=&resources[8];
        eval.pInDepth=&resources[1]; eval.pInMotionVectors=&resources[2]; eval.pInBiasCurrentColorMask=&resources[3];
        eval.InJitterOffsetX=-f.jitterX; eval.InJitterOffsetY=-f.jitterY; eval.InRenderSubrectDimensions={f.renderWidth,f.renderHeight};
        eval.InReset=f.reset; eval.InMVScaleX=eval.InMVScaleY=1; eval.InPreExposure=eval.InExposureScale=1;
        eval.InFrameTimeDeltaInMsec=f.deltaMs;
        result=NGX_VULKAN_EVALUATE_DLSS_EXT(command,feature_,parameters_,&eval);
    }
    for (size_t i=0;i<images.size();i++) imageBarrier(command,images[i],i==8 ? VK_IMAGE_LAYOUT_GENERAL : VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,images[i].layout,
        i==8 ? VK_ACCESS_SHADER_WRITE_BIT : VK_ACCESS_SHADER_READ_BIT,VK_ACCESS_MEMORY_READ_BIT|VK_ACCESS_MEMORY_WRITE_BIT);
    if (NVSDK_NGX_FAILED(result)) { error=ngxError("Evaluate DLSS reconstruction",result); reasons_[algorithm==2 ? 1 : 0]=error; return false; }
    return true;
}
}
