package dev.adventurecraft.awakening.client.render.resource;

/** Game-owned terrain allocation statistics. Native resources live in the Vulkan renderer. */
public final class RenderDevice {

    private final DeviceInfo info = new DeviceInfo();

    public DeviceInfo getDeviceInfo() {
        return this.info;
    }

    public void meshAllocated(long sizeInBytes) {
        this.info.bufferAllocatedBytes += sizeInBytes;
        this.info.bufferCount++;
    }

    public void meshDeleted(long sizeInBytes) {
        this.info.bufferAllocatedBytes -= sizeInBytes;
        this.info.bufferCount--;
    }

    public static final class DeviceInfo {
        private long bufferAllocatedBytes;
        private long bufferCount;

        public long bufferAllocatedBytes() {
            return this.bufferAllocatedBytes;
        }

        public long bufferCount() {
            return this.bufferCount;
        }
    }
}
