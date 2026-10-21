#pragma once

#include "../NativeTypes.hpp"
#include <nvsdk_ngx_vk.h>
#include <memory>

namespace ac {

struct FrameGenerationOutputs {
    Image interpolated;
    Image real;
    VkBuffer disableInterpolation = VK_NULL_HANDLE;
    uint32_t slot = 0;
    bool reset = true;
};

// NGX must already be initialized for DeviceInfo::device. Dispatch/configuration
// must be externally synchronized with other queue users. Configuration submits
// one initialization command; normal frames use the caller's command buffer.
// The caller presents interpolated, then real, at evenly spaced times.
class NgxFrameGeneration {
public:
    explicit NgxFrameGeneration(const DeviceInfo& device);
    ~NgxFrameGeneration();
    NgxFrameGeneration(const NgxFrameGeneration&) = delete;
    NgxFrameGeneration& operator=(const NgxFrameGeneration&) = delete;

    static bool supported(NVSDK_NGX_Parameter* capabilities, std::string& reason);
    bool dispatch(VkCommandBuffer command, uint32_t slot, const Image& backbuffer,
                  const Image& hudless, const Image& depth, const Image& motion,
                  const FrameData& frame, FrameGenerationOutputs& outputs, std::string& error);

    // Call only after this slot's submission fence has completed. Reading the
    // SDK's GPU-written disable flag before completion is not a support check.
    bool interpolationAllowed(uint32_t slot, std::string* error = nullptr) const;
    void invalidate() noexcept;

private:
    struct Impl;
    std::unique_ptr<Impl> impl_;
};

}
