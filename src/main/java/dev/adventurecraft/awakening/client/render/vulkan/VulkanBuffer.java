package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkBufferDeviceAddressInfo;
import org.lwjgl.vulkan.VkMappedMemoryRange;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryAllocateFlagsInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.vulkan.VK12.VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT;
import static org.lwjgl.vulkan.VK12.vkGetBufferDeviceAddress;

/** A dedicated Vulkan buffer allocation. The owning device also closes outstanding buffers. */
public final class VulkanBuffer implements AutoCloseable {

    private final VulkanDevice owner;
    private final long handle;
    private final long memory;
    private final long size;
    private final int memoryFlags;
    private final boolean deviceAddressEnabled;
    private ByteBuffer mapping;
    private boolean closed;

    private VulkanBuffer(VulkanDevice owner, long handle, long memory, long size, int memoryFlags,
                         boolean deviceAddressEnabled) {
        this.owner = owner;
        this.handle = handle;
        this.memory = memory;
        this.size = size;
        this.memoryFlags = memoryFlags;
        this.deviceAddressEnabled = deviceAddressEnabled;
    }

    static VulkanBuffer create(VulkanDevice owner, long size, int usage, int memoryFlags) {
        if (size <= 0) {
            throw new IllegalArgumentException("Vulkan buffer size must be positive");
        }
        if (usage == 0) {
            throw new IllegalArgumentException("Vulkan buffer usage must not be empty");
        }
        boolean deviceAddress = (usage & VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT) != 0;
        if (deviceAddress && !owner.bufferDeviceAddressSupported()) {
            throw new IllegalStateException("Vulkan buffer device addresses were not enabled on this device");
        }
        long buffer = VK_NULL_HANDLE;
        long memory = VK_NULL_HANDLE;
        try (var stack = stackPush()) {
            var info = VkBufferCreateInfo.calloc(stack)
                .sType$Default()
                .size(size)
                .usage(usage)
                .sharingMode(VK_SHARING_MODE_EXCLUSIVE);
            var handle = stack.mallocLong(1);
            VulkanDevice.check(vkCreateBuffer(owner.device(), info, null, handle), "create Vulkan buffer");
            buffer = handle.get(0);
            var requirements = VkMemoryRequirements.malloc(stack);
            vkGetBufferMemoryRequirements(owner.device(), buffer, requirements);
            int preferred=0;
            if(owner.memoryProfile().unified()) preferred|=VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT;
            if((memoryFlags&VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)!=0 && owner.memoryProfile().unified())
                preferred|=VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT|VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
            if(usage==VK_BUFFER_USAGE_TRANSFER_DST_BIT) preferred|=VK_MEMORY_PROPERTY_HOST_CACHED_BIT;
            int memoryType=owner.memoryType(requirements.memoryTypeBits(),memoryFlags,preferred);
            var allocation = VkMemoryAllocateInfo.calloc(stack)
                .sType$Default()
                .allocationSize(requirements.size())
                .memoryTypeIndex(memoryType);
            if (deviceAddress) {
                var flags = VkMemoryAllocateFlagsInfo.calloc(stack).sType$Default()
                    .flags(VK_MEMORY_ALLOCATE_DEVICE_ADDRESS_BIT);
                allocation.pNext(flags.address());
            }
            VulkanDevice.check(vkAllocateMemory(owner.device(), allocation, null, handle), "allocate Vulkan buffer memory");
            memory = handle.get(0);
            VulkanDevice.check(vkBindBufferMemory(owner.device(), buffer, memory, 0), "bind Vulkan buffer memory");
            return new VulkanBuffer(owner, buffer, memory, size, owner.memoryProfile().flags(memoryType), deviceAddress);
        }
        catch (Throwable error) {
            if (buffer != VK_NULL_HANDLE) {
                vkDestroyBuffer(owner.device(), buffer, null);
            }
            if (memory != VK_NULL_HANDLE) {
                vkFreeMemory(owner.device(), memory, null);
            }
            throw error;
        }
    }

    public long handle() {
        ensureOpen();
        return this.handle;
    }

    public long memory() {
        ensureOpen();
        return this.memory;
    }

    public long size() {
        return this.size;
    }

    /** Address of an allocation created with VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT. */
    public long deviceAddress() {
        ensureOpen();
        if (!this.deviceAddressEnabled) {
            throw new IllegalStateException("Vulkan buffer was not created with shader device-address usage");
        }
        try (var stack = stackPush()) {
            var info = VkBufferDeviceAddressInfo.calloc(stack).sType$Default().buffer(this.handle);
            return vkGetBufferDeviceAddress(this.owner.device(), info);
        }
    }

    boolean hostCoherent() {
        int flags=VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT|VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
        return (memoryFlags&flags)==flags;
    }

    /** Copies the remaining source bytes at offset zero without changing its position. */
    public synchronized void upload(ByteBuffer source) {
        upload(0, source);
    }

    public synchronized void upload(long offset, ByteBuffer source) {
        Objects.requireNonNull(source, "source");
        ensureRange(offset, source.remaining());
        var target = mapping().duplicate().order(ByteOrder.nativeOrder());
        target.position(Math.toIntExact(offset));
        target.put(source.duplicate());
        flush();
    }

    /** Maps host coherent memory for direct access. This view remains valid until close(). */
    public synchronized ByteBuffer mapped() {
        if ((this.memoryFlags & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) == 0) {
            throw new IllegalStateException("Direct Vulkan buffer mapping requires host coherent memory; use read/write instead");
        }
        return mapping().duplicate().order(ByteOrder.nativeOrder());
    }

    /**
     * Returns a read-only view of the mapped bytes. Complete GPU work before reading; for
     * example, {@link VulkanDevice#immediate(Consumer)} waits for its submission to finish.
     * The view remains valid until close() and reflects later writes to the buffer.
     */
    public synchronized ByteBuffer read() {
        var bytes = mapping();
        if ((this.memoryFlags & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) == 0) {
            try (var stack = stackPush()) {
                var range = VkMappedMemoryRange.calloc(1, stack)
                    .sType$Default()
                    .memory(this.memory)
                    .offset(0)
                    .size(VK_WHOLE_SIZE);
                VulkanDevice.check(vkInvalidateMappedMemoryRanges(this.owner.device(), range), "invalidate Vulkan buffer memory");
            }
        }
        return bytes.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
    }

    /** Completes writes and flushes noncoherent memory before the next GPU submission. */
    public synchronized void write(Consumer<ByteBuffer> writer) {
        Objects.requireNonNull(writer, "writer");
        try {
            writer.accept(mapping().duplicate().order(ByteOrder.nativeOrder()));
        }
        finally {
            // The writer may have changed memory before throwing.
            if (this.mapping != null) {
                flush();
            }
        }
    }

    private ByteBuffer mapping() {
        ensureOpen();
        if ((this.memoryFlags & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == 0) {
            throw new IllegalStateException("Vulkan buffer memory is not host visible");
        }
        if (this.size > Integer.MAX_VALUE) {
            throw new IllegalStateException("Vulkan buffer is too large for a Java ByteBuffer");
        }
        if (this.mapping == null) {
            try (var stack = stackPush()) {
                var pointer = stack.mallocPointer(1);
                // Map the entire dedicated allocation so whole-allocation flush/invalidate
                // ranges satisfy nonCoherentAtomSize alignment without additional padding.
                VulkanDevice.check(vkMapMemory(this.owner.device(), this.memory, 0, VK_WHOLE_SIZE, 0, pointer),
                    "map Vulkan buffer memory");
                this.mapping = MemoryUtil.memByteBuffer(pointer.get(0), Math.toIntExact(this.size))
                    .order(ByteOrder.nativeOrder());
            }
        }
        return this.mapping;
    }

    private void flush() {
        if ((this.memoryFlags & VK_MEMORY_PROPERTY_HOST_COHERENT_BIT) == 0) {
            try (var stack = stackPush()) {
                var range = VkMappedMemoryRange.calloc(1, stack)
                    .sType$Default()
                    .memory(this.memory)
                    .offset(0)
                    .size(VK_WHOLE_SIZE);
                VulkanDevice.check(vkFlushMappedMemoryRanges(this.owner.device(), range), "flush Vulkan buffer memory");
            }
        }
    }

    private void ensureRange(long offset, int length) {
        ensureOpen();
        if (offset < 0 || offset > this.size || length > this.size - offset) {
            throw new IndexOutOfBoundsException("Vulkan buffer write exceeds allocation: offset=" + offset +
                ", length=" + length + ", size=" + this.size);
        }
    }

    private void ensureOpen() {
        this.owner.ensureOpen();
        if (this.closed) {
            throw new IllegalStateException("Vulkan buffer is closed");
        }
    }

    /** Caller must complete all commands using this buffer before closing it. */
    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        if (this.mapping != null) {
            vkUnmapMemory(this.owner.device(), this.memory);
            this.mapping = null;
        }
        vkDestroyBuffer(this.owner.device(), this.handle, null);
        vkFreeMemory(this.owner.device(), this.memory, null);
        this.closed = true;
        this.owner.release(this);
    }
}
