#pragma once
#include "../NativeTypes.hpp"
#include <memory>

namespace ac {
struct FsrFrameGenerationOutputs {
    Image interpolated;
    Image real;
    uint32_t slot = 0;
    bool reset = true;
};

/** Official FidelityFX optical-flow and interpolation compute, with host-owned presentation. */
class FsrFrameGeneration {
public:
    static bool supported(const DeviceInfo& device, std::string& reason);
    explicit FsrFrameGeneration(const DeviceInfo& device);
    ~FsrFrameGeneration();
    FsrFrameGeneration(const FsrFrameGeneration&) = delete;
    FsrFrameGeneration& operator=(const FsrFrameGeneration&) = delete;
    bool dispatch(VkCommandBuffer command, uint32_t slot, const Image& backbuffer,
        const Image& hudless, const Image& depth, const Image& motion,
        const FrameData& frame, FsrFrameGenerationOutputs& outputs, std::string& error);
    // A false result may follow partial recording: discard that command buffer
    // and call abort before recording or submitting another native frame.
    // The owning submission must have completed before presenting this slot.
    bool interpolationAllowed(uint32_t slot, std::string* error = nullptr) const;
    void invalidate() noexcept;
    void abort();
private:
    struct Impl;
    std::unique_ptr<Impl> impl;
};
}
