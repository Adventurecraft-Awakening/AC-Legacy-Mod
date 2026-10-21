package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Owns driver pipeline data, rejecting incompatible cache headers before passing bytes to Vulkan. */
final class VulkanPipelineCache implements AutoCloseable {
    private static final System.Logger LOGGER = System.getLogger(VulkanPipelineCache.class.getName());
    private final VulkanDevice context;
    private long handle;
    private Path path;

    VulkanPipelineCache(VulkanDevice context) {
        this.context = context;
        create();
    }

    long handle() { return handle; }

    private void create() {
        byte[] cached = null;
        try (var stack = MemoryStack.stackPush()) {
            var properties = VkPhysicalDeviceProperties.malloc(stack);
            vkGetPhysicalDeviceProperties(context.physicalDevice(), properties);
            String name = Integer.toHexString(properties.vendorID()) + "-" + Integer.toHexString(properties.deviceID()) +
                "-" + Integer.toHexString(properties.driverVersion()) + ".bin";
            path = Path.of(System.getProperty("user.home"), ".cache", "adventurecraft", "vulkan", name);
            try {
                if (Files.isRegularFile(path) && Files.size(path) <= 64L * 1024 * 1024) {
                    byte[] bytes = Files.readAllBytes(path);
                    if (validPipelineCache(bytes, properties)) cached = bytes;
                }
            } catch (IOException | SecurityException error) { LOGGER.log(System.Logger.Level.DEBUG, "Cannot load Vulkan pipeline cache", error); }
            ByteBuffer data = cached == null ? null : MemoryUtil.memAlloc(cached.length).put(cached).flip();
            try {
                var info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default().pInitialData(data);
                var result = stack.mallocLong(1);
                int status = vkCreatePipelineCache(context.device(), info, null, result);
                if (status != VK_SUCCESS && data != null) {
                    info.pInitialData(null);
                    status = vkCreatePipelineCache(context.device(), info, null, result);
                }
                check(status, "create pipeline cache");
                handle = result.get(0);
            } finally { if (data != null) MemoryUtil.memFree(data); }
        }
    }

    private static boolean validPipelineCache(byte[] bytes, VkPhysicalDeviceProperties properties) {
        if (bytes.length < 32) return false;
        ByteBuffer header = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (header.getInt(0) < 32 || header.getInt(0) > bytes.length || header.getInt(4) != VK_PIPELINE_CACHE_HEADER_VERSION_ONE ||
            header.getInt(8) != properties.vendorID() || header.getInt(12) != properties.deviceID()) return false;
        for (int i = 0; i < VK_UUID_SIZE; i++) if (bytes[16 + i] != properties.pipelineCacheUUID(i)) return false;
        return true;
    }

    private void save() {
        if (handle == VK_NULL_HANDLE || path == null) return;
        try (var stack = MemoryStack.stackPush()) {
            var size = stack.mallocPointer(1);
            if (vkGetPipelineCacheData(context.device(), handle, size, null) != VK_SUCCESS || size.get(0) > 64L * 1024 * 1024) return;
            ByteBuffer data = MemoryUtil.memAlloc(Math.toIntExact(size.get(0)));
            try {
                if (vkGetPipelineCacheData(context.device(), handle, size, data) != VK_SUCCESS) return;
                byte[] bytes = new byte[Math.toIntExact(size.get(0))];
                data.get(bytes);
                Files.createDirectories(path.getParent());
                Path temporary = Files.createTempFile(path.getParent(), "pipeline-", ".tmp");
                try {
                    Files.write(temporary, bytes);
                    Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } finally { Files.deleteIfExists(temporary); }
            } finally { MemoryUtil.memFree(data); }
        } catch (IOException | SecurityException error) { LOGGER.log(System.Logger.Level.DEBUG, "Cannot save Vulkan pipeline cache", error); }
    }

    @Override public void close() {
        if (handle == VK_NULL_HANDLE) return;
        try { save(); }
        finally {
            vkDestroyPipelineCache(context.device(), handle, null);
            handle = VK_NULL_HANDLE;
        }
    }
}
