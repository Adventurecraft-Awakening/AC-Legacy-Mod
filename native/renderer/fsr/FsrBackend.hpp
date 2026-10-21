#pragma once

#include <vulkan/vulkan.h>
#include <cstdint>
#include <memory>
#include <string>

namespace ac {

struct FsrDevice {
    VkDevice device = VK_NULL_HANDLE;
    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
    VkQueue graphicsQueue = VK_NULL_HANDLE;
    uint32_t queueFamily = 0;
    PFN_vkGetDeviceProcAddr getDeviceProcAddr = nullptr;
    // These describe features enabled on the logical device, not merely supported.
    bool storageImageExtendedFormats = false;
    bool storageImageReadWithoutFormat = false;
    bool storageImageWriteWithoutFormat = false;
};

struct FsrImage {
    VkImage image = VK_NULL_HANDLE;
    VkFormat format = VK_FORMAT_UNDEFINED;
    uint32_t width = 0, height = 0;
    VkImageLayout layout = VK_IMAGE_LAYOUT_UNDEFINED;
    VkImageUsageFlags usage = 0;
};

struct FsrFrame {
    VkCommandBuffer commandBuffer = VK_NULL_HANDLE;
    FsrImage color, depth, motion, reactive, output;
    uint32_t renderWidth = 0, renderHeight = 0;
    uint32_t outputWidth = 0, outputHeight = 0;
    float jitterX = 0, jitterY = 0; // Raster offsets in render pixels.
    // Current-to-previous motion excludes jitter. Use scale 1 for pixel motion;
    // normalized UV motion instead needs renderWidth/renderHeight here.
    float motionScaleX = 1, motionScaleY = 1;
    float deltaMilliseconds = 16.6667f;
    float preExposure = 1;
    float cameraNear = 0.05f, cameraFar = 1024, verticalFovRadians = 1.22173f;
    float sharpness = 0;
    bool hdr = true;
    bool reset = false;
};

/** Actual FidelityFX FSR 3.1.4 temporal SR. One graphics queue owns dispatches. */
class FsrBackend {
public:
    static bool supported(const FsrDevice& device, std::string& reason);
    explicit FsrBackend(const FsrDevice& device);
    // GPU work using this context must have finished before destruction.
    ~FsrBackend();
    FsrBackend(const FsrBackend&) = delete;
    FsrBackend& operator=(const FsrBackend&) = delete;
    // Reconfiguration waits for device completion; ordinary dispatches never wait.
    // All external images return to their supplied layout after a successful dispatch.
    bool dispatch(const FsrFrame& frame, std::string& error);
    // Call when a command buffer containing an SDK dispatch is abandoned.
    void abort();
private:
    struct Impl;
    std::unique_ptr<Impl> impl;
};

}
