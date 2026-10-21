package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.vulkan.VkCommandBuffer;
import java.nio.ByteBuffer;
import java.nio.file.Path;

/** Narrow borrowed-resource ABI to the optional, separately built SDK integration. */
final class NativeRenderingBridge implements AutoCloseable {
    static final int DLSS_SR = 0, DLSS_RR = 1, NRD = 2, FSR_SR = 3, DLSS_FG = 4, FSR_FG = 5;
    private static final String SDK_ROOT = System.getProperty("ac.nativeRendering.sdkRoot", "");
    private static final String LOAD_FAILURE;
    private final VulkanDevice context;
    private long handle;
    static {
        String failure = null;
        String path = System.getProperty("ac.nativeRendering.library", "");
        if (path.isBlank()) failure = "Native rendering runtime is not installed";
        else {
            try { System.load(Path.of(path).toAbsolutePath().toString()); }
            catch (UnsatisfiedLinkError | SecurityException error) { failure = "Cannot load native rendering runtime: " + error.getMessage(); }
        }
        LOAD_FAILURE = failure;
    }
    static String loadFailure() { return LOAD_FAILURE; }
    static String[] instanceExtensions() {
        if (LOAD_FAILURE != null) return new String[0];
        try { return nInstanceExtensions(SDK_ROOT); } catch (LinkageError | RuntimeException error) { return new String[0]; }
    }
    static String[] deviceExtensions(long instance, long physicalDevice) {
        if (LOAD_FAILURE != null) return new String[0];
        try { return nDeviceExtensions(instance, physicalDevice, SDK_ROOT); } catch (LinkageError | RuntimeException error) { return new String[0]; }
    }
    NativeRenderingBridge(VulkanDevice context) {
        this.context = context;
        synchronized (context) {
            if (LOAD_FAILURE == null) handle = nCreate(context.instance().address(), context.physicalDevice().address(),
                context.device().address(), context.queue().address(), context.queueFamily(), SDK_ROOT,
                context.nativeEnabledExtensions(), context.nativeStorageExtended(), context.nativeStorageRead(), context.nativeStorageWrite(),
                context.nativePartiallyBound(), context.nativeSynchronization2(), context.nativeDynamicRendering(),
                context.nativeMaintenance4(), context.nativeBufferDeviceAddress(), context.nativeApiVersion());
        }
    }
    boolean supported(int feature) { return handle != 0 && nSupported(handle, feature); }
    String reason(int feature) {
        return handle == 0 ? LOAD_FAILURE != null ? LOAD_FAILURE : "Native rendering initialization failed" : nReason(handle, feature);
    }
    boolean denoise(VkCommandBuffer command, long[] images, ByteBuffer frame, boolean direct, boolean indirect) {
        synchronized (context) { return handle != 0 && nDenoise(handle, command.address(), images, frame, direct, indirect); }
    }
    boolean reconstruct(VkCommandBuffer command, int algorithm, long[] images, ByteBuffer frame) {
        synchronized (context) { return handle != 0 && nReconstruct(handle, command.address(), algorithm, images, frame); }
    }
    long[] generate(VkCommandBuffer command, int algorithm, int slot, long[] images, ByteBuffer frame) {
        synchronized (context) { return handle == 0 ? new long[0] : nGenerate(handle, command.address(), algorithm, slot, images, frame); }
    }
    boolean interpolationAllowed(int slot) { return handle != 0 && nInterpolationAllowed(handle, slot); }
    void abort() { synchronized (context) { if (handle != 0) nAbort(handle); } }
    static long[] images(VulkanImage... images) {
        long[] values = new long[images.length * 6];
        for (int i = 0; i < images.length; i++) {
            VulkanImage image = images[i];
            if (image == null) continue;
            int j = i * 6;
            values[j] = image.handle; values[j+1] = image.view; values[j+2] = image.format;
            values[j+3] = image.width; values[j+4] = image.height; values[j+5] = org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;
        }
        return values;
    }
    @Override public void close() { synchronized (context) { if (handle != 0) { nDestroy(handle); handle = 0; } } }
    private static native String[] nInstanceExtensions(String sdkRoot);
    private static native String[] nDeviceExtensions(long instance, long physicalDevice, String sdkRoot);
    private static native long nCreate(long instance, long physicalDevice, long device, long queue, int family, String sdkRoot,
                                       String[] extensions, boolean extended, boolean read, boolean write, boolean partiallyBound,
                                       boolean synchronization2, boolean dynamicRendering, boolean maintenance4, boolean bufferDeviceAddress, int apiVersion);
    private static native boolean nSupported(long handle, int feature);
    private static native String nReason(long handle, int feature);
    private static native boolean nDenoise(long handle, long command, long[] images, ByteBuffer frame, boolean direct, boolean indirect);
    private static native boolean nReconstruct(long handle, long command, int algorithm, long[] images, ByteBuffer frame);
    private static native long[] nGenerate(long handle, long command, int algorithm, int slot, long[] images, ByteBuffer frame);
    private static native boolean nInterpolationAllowed(long handle, int slot);
    private static native void nAbort(long handle);
    private static native void nDestroy(long handle);
}
