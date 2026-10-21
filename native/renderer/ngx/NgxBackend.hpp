#pragma once
#include "../NativeTypes.hpp"
#include <nvsdk_ngx_vk.h>
#include <memory>
#include <vector>
namespace ac {
inline constexpr const char* PROJECT_ID = "97de3499-0eda-5eca-af9c-8e4981fdcb30";
std::vector<std::string> ngxExtensions(const std::string& sdkRoot, VkInstance instance = VK_NULL_HANDLE,
                                      VkPhysicalDevice physicalDevice = VK_NULL_HANDLE);
class NgxBackend {
public:
    NgxBackend(const DeviceInfo& device, const std::vector<std::string>& enabledExtensions);
    ~NgxBackend();
    bool supported(int feature) const;
    const std::string& reason(int feature) const;
    NVSDK_NGX_Parameter* capabilities() const { return capabilities_; }
    bool reconstruct(VkCommandBuffer command, int algorithm, const std::vector<Image>& images,
                     const FrameData& frame, std::string& error);
    void abort();
private:
    DeviceInfo device_;
    NVSDK_NGX_Parameter* capabilities_ = nullptr;
    NVSDK_NGX_Parameter* parameters_ = nullptr;
    NVSDK_NGX_Handle* feature_ = nullptr;
    std::array<std::string,3> reasons_;
    int algorithm_ = 0;
    uint32_t renderWidth_ = 0, renderHeight_ = 0, outputWidth_ = 0, outputHeight_ = 0;
    bool hdr_ = false, initialized_ = false;
    bool createFeature(int algorithm, const FrameData& frame, std::string& error);
    void destroyFeature();
};
}
