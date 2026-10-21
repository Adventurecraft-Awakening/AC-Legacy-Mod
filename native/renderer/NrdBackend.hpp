#pragma once

#include "NativeTypes.hpp"
#include <memory>

namespace ac {

/** Native RELAX radiance denoising through the licensed NRD/NRI Vulkan integration. */
class NrdBackend {
public:
    static bool supported(const DeviceInfo& device, std::string& reason);
    explicit NrdBackend(const DeviceInfo& device);
    // Caller must retire submitted GPU work before destruction.
    ~NrdBackend();
    NrdBackend(const NrdBackend&) = delete;
    NrdBackend& operator=(const NrdBackend&) = delete;
    bool denoise(VkCommandBuffer command, const FrameData& frame, bool direct, bool indirect,
        const Image& motion, const Image& viewZ, const Image& normalRoughness,
        const Image& diffuse, const Image& specular, const Image& indirectDiffuse,
        const Image& outDiffuse, const Image& outSpecular, const Image& outIndirect,
        std::string& error);
    void invalidate();
    // An abandoned command buffer cannot advance the SDK's CPU-side resource state.
    void abort();
private:
    struct Impl;
    std::unique_ptr<Impl> impl;
};
}
