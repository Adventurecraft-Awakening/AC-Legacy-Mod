#pragma once
#include <vulkan/vulkan.h>
#include <array>
#include <cstdint>
#include <string>

namespace ac {
struct DeviceInfo {
    VkInstance instance = VK_NULL_HANDLE;
    VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
    VkDevice device = VK_NULL_HANDLE;
    VkQueue queue = VK_NULL_HANDLE;
    uint32_t queueFamily = 0;
    std::string sdkRoot;
    bool storageExtended = false, storageRead = false, storageWrite = false;
    bool partiallyBound = false, synchronization2 = false, dynamicRendering = false;
    bool maintenance4 = false, bufferDeviceAddress = false, pushDescriptor = false;
    uint32_t apiVersion = VK_API_VERSION_1_0;
};
struct Image {
    VkImage image = VK_NULL_HANDLE;
    VkImageView view = VK_NULL_HANDLE;
    VkFormat format = VK_FORMAT_UNDEFINED;
    uint32_t width = 0, height = 0;
    VkImageLayout layout = VK_IMAGE_LAYOUT_GENERAL;
};
// Column-major matrices, positive view depth, current-to-previous pixel motion.
struct FrameData {
    std::array<float,16> worldToView{}, previousWorldToView{}, viewToClip{}, previousViewToClip{};
    float jitterX = 0, jitterY = 0, previousJitterX = 0, previousJitterY = 0;
    float deltaMs = 16.6667f, nearPlane = 0.05f, farPlane = 1024, verticalFov = 1.22173f;
    uint32_t renderWidth = 0, renderHeight = 0, outputWidth = 0, outputHeight = 0, frameIndex = 0;
    bool reset = true, hdr = true;
};
inline void imageBarrier(VkCommandBuffer cmd, const Image& image, VkImageLayout before, VkImageLayout after,
                         VkAccessFlags src, VkAccessFlags dst) {
    VkImageMemoryBarrier barrier{VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER};
    barrier.srcAccessMask = src; barrier.dstAccessMask = dst;
    barrier.oldLayout = before; barrier.newLayout = after;
    barrier.srcQueueFamilyIndex = barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image.image;
    barrier.subresourceRange = {image.format == VK_FORMAT_D32_SFLOAT ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT,0,1,0,1};
    vkCmdPipelineBarrier(cmd,VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,0,0,nullptr,0,nullptr,1,&barrier);
}
}
