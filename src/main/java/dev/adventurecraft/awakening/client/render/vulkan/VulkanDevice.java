package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.glfw.GLFWVulkan.*;
import static org.lwjgl.vulkan.EXTDebugUtils.*;
import static org.lwjgl.vulkan.KHRAccelerationStructure.VK_KHR_ACCELERATION_STRUCTURE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRDeferredHostOperations.VK_KHR_DEFERRED_HOST_OPERATIONS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRGetPhysicalDeviceProperties2.VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRPortabilityEnumeration.*;
import static org.lwjgl.vulkan.KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRRayQuery.VK_KHR_RAY_QUERY_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceFeatures2;
import static org.lwjgl.vulkan.VK11.vkGetPhysicalDeviceProperties2;

/**
 * Owns a Vulkan graphics device and command pool, optionally including a GLFW presentation surface.
 * Offscreen and windowed rendering share the same resource and command submission infrastructure.
 * Resources using {@link #device()} must be destroyed before closing this object. Calls that submit
 * directly to {@link #queue()} must be externally synchronized with {@link #immediate(Consumer)}.
 */
public final class VulkanDevice implements AutoCloseable {

    private static final System.Logger LOGGER = System.getLogger(VulkanDevice.class.getName());
    private static final String VALIDATION_LAYER = "VK_LAYER_KHRONOS_validation";
    private static final int MAX_VALIDATION_MESSAGES = 256;
    private static final int RAY_TEXTURE_COUNT = 16;
    private static final int RAY_DESCRIPTOR_SETS = 5;

    private final Set<VulkanBuffer> buffers = ConcurrentHashMap.newKeySet();
    private final List<String> validationMessages = new ArrayList<>();
    private final AtomicInteger validationErrorCount = new AtomicInteger();
    private VkInstance instance;
    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private VkQueue queue;
    private int queueFamily;
    private VkQueue presentQueue;
    private int presentFamily;
    private long window;
    private long surface;
    private long commandPool;
    private long debugMessenger;
    private VkDebugUtilsMessengerCallbackEXT debugCallback;
    private String deviceName;
    private VulkanMemoryProfile memoryProfile;
    private VulkanImageAllocator imageAllocator;
    private int instanceApiVersion;
    private RayTracingSupport rayTracing = RayTracingSupport.unavailable("device is not initialized");
    private SharcSupport sharc = SharcSupport.unavailable("device is not initialized");
    private boolean validationEnabled;
    private boolean wideLines;
    private boolean independentBlend;
    private String[] nativeEnabledExtensions = new String[0];
    private boolean nativeStorageExtended, nativeStorageRead, nativeStorageWrite;
    private boolean nativePartiallyBound, nativeSynchronization2, nativeDynamicRendering, nativeMaintenance4;
    private boolean nativeBufferDeviceAddress;
    private boolean nativePushDescriptor;
    private int nativeApiVersion;
    private volatile boolean closed;

    private VulkanDevice() {
    }

    public static VulkanDevice create() {
        return create(Boolean.getBoolean("ac.vulkan.validation"));
    }

    /** Enables validation when the Khronos validation layer is installed. */
    public static VulkanDevice create(boolean validation) {
        return createForWindow(0L, validation);
    }

    /** Creates a graphics device with a presentation surface for an existing GLFW_NO_API window. */
    public static VulkanDevice createForWindow(long window, boolean validation) {
        var result = new VulkanDevice();
        result.window = window;
        try {
            result.initialize(validation);
            return result;
        }
        catch (Throwable error) {
            try {
                result.close();
            }
            catch (Throwable cleanupError) {
                error.addSuppressed(cleanupError);
            }
            throw error;
        }
    }

    private void initialize(boolean validation) {
        try (var stack = stackPush()) {
            createInstance(validation, stack);
            createSurface(stack);
            selectPhysicalDevice(stack);
            this.memoryProfile = new VulkanMemoryProfile(this.physicalDevice);
            createLogicalDevice(stack);
            createCommandPool(stack);
        }
    }

    private Set<String> selectInstanceExtensions(Set<String> available) {
        Set<String> enabled = new LinkedHashSet<>();
        if (this.window != 0L) {
            var required = glfwGetRequiredInstanceExtensions();
            if (required == null) {
                throw new IllegalStateException("GLFW cannot provide Vulkan presentation extensions");
            }
            for (int i = required.position(); i < required.limit(); i++) {
                String extension = org.lwjgl.system.MemoryUtil.memUTF8(required.get(i));
                if (!available.contains(extension)) {
                    throw new IllegalStateException("Missing Vulkan presentation extension " + extension);
                }
                enabled.add(extension);
            }
        }
        if (available.contains(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME)) {
            enabled.add(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
            if (available.contains(VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME)) {
                enabled.add(VK_KHR_GET_PHYSICAL_DEVICE_PROPERTIES_2_EXTENSION_NAME);
            }
        }
        for (String extension : NativeRenderingBridge.instanceExtensions()) {
            if (available.contains(extension)) enabled.add(extension);
            else LOGGER.log(System.Logger.Level.INFO, "Optional native rendering instance extension is unavailable: {0}", extension);
        }
        return enabled;
    }

    private boolean configureValidation(boolean requested, Set<String> extensions, MemoryStack stack) {
        this.validationEnabled = requested && instanceLayers(stack).contains(VALIDATION_LAYER);
        if (requested && !this.validationEnabled) {
            LOGGER.log(System.Logger.Level.WARNING,
                "Vulkan validation was requested, but VK_LAYER_KHRONOS_validation is not installed");
        }
        boolean debug = this.validationEnabled && extensions.contains(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        if (debug) {
            this.debugCallback = VkDebugUtilsMessengerCallbackEXT.create((severity, type, data, userData) ->
                recordValidationMessage(severity, data));
        }
        return debug;
    }

    private int recordValidationMessage(int severity, long data) {
        String message = VkDebugUtilsMessengerCallbackDataEXT.create(data).pMessageString();
        synchronized (this.validationMessages) {
            if (this.validationMessages.size() == MAX_VALIDATION_MESSAGES) {
                this.validationMessages.removeFirst();
            }
            this.validationMessages.add(message);
        }
        boolean error = (severity & VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT) != 0;
        if (error) {
            this.validationErrorCount.incrementAndGet();
        }
        LOGGER.log(error ? System.Logger.Level.ERROR : System.Logger.Level.WARNING, message);
        return VK_FALSE;
    }

    private void createInstance(boolean validation, MemoryStack stack) {
        Set<String> availableExtensions = instanceExtensions(stack);
        Set<String> extensions = selectInstanceExtensions(availableExtensions);
        boolean portability = extensions.contains(VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
        boolean debug = configureValidation(validation, availableExtensions, stack);
        if (debug) {
            extensions.add(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
        }
        this.instanceApiVersion = Math.min(VK.getInstanceVersionSupported(), VK_MAKE_VERSION(1, 3, 0));
        var application = VkApplicationInfo.calloc(stack)
            .sType$Default()
            .pApplicationName(stack.UTF8("AdventureCraft"))
            .applicationVersion(VK_MAKE_VERSION(0, 7, 0))
            .pEngineName(stack.UTF8("AdventureCraft Vulkan"))
            .engineVersion(VK_MAKE_VERSION(0, 1, 0))
            .apiVersion(this.instanceApiVersion);
        var instanceInfo = VkInstanceCreateInfo.calloc(stack)
            .sType$Default()
            .pApplicationInfo(application)
            .flags(portability ? VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR : 0)
            .ppEnabledExtensionNames(strings(stack, extensions));
        if (this.validationEnabled) {
            instanceInfo.ppEnabledLayerNames(stack.pointers(stack.UTF8(VALIDATION_LAYER)));
        }
        VkDebugUtilsMessengerCreateInfoEXT debugInfo = null;
        if (debug) {
            debugInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack)
                .sType$Default()
                .messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                    VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                .messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                    VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                    VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
                .pfnUserCallback(this.debugCallback);
            instanceInfo.pNext(debugInfo.address());
        }
        var pointer = stack.mallocPointer(1);
        check(vkCreateInstance(instanceInfo, null, pointer), "create Vulkan instance");
        this.instance = new VkInstance(pointer.get(0), instanceInfo);
        if (debugInfo != null) {
            var handle = stack.mallocLong(1);
            check(vkCreateDebugUtilsMessengerEXT(this.instance, debugInfo, null, handle),
                "create Vulkan validation messenger");
            this.debugMessenger = handle.get(0);
        }
    }

    private void createSurface(MemoryStack stack) {
        if (this.window == 0L) {
            return;
        }
        var handle = stack.mallocLong(1);
        check(glfwCreateWindowSurface(this.instance, this.window, null, handle), "create Vulkan window surface");
        this.surface = handle.get(0);
    }

    private void createLogicalDevice(MemoryStack stack) {
        var queueInfo = VkDeviceQueueCreateInfo.calloc(this.queueFamily == this.presentFamily ? 1 : 2, stack);
        queueInfo.get(0).sType$Default().queueFamilyIndex(this.queueFamily).pQueuePriorities(stack.floats(1.0F));
        if (this.queueFamily != this.presentFamily) {
            queueInfo.get(1).sType$Default().queueFamilyIndex(this.presentFamily).pQueuePriorities(stack.floats(1.0F));
        }
        var deviceInfo = VkDeviceCreateInfo.calloc(stack).sType$Default().pQueueCreateInfos(queueInfo);
        var properties = VkPhysicalDeviceProperties.malloc(stack);
        vkGetPhysicalDeviceProperties(this.physicalDevice, properties);
        this.nativeApiVersion = Math.min(this.instanceApiVersion, properties.apiVersion());
        boolean core12 = this.nativeApiVersion >= VK_MAKE_VERSION(1, 2, 0);
        boolean core13 = this.nativeApiVersion >= VK_MAKE_VERSION(1, 3, 0);
        var supportedFeatures = VkPhysicalDeviceFeatures.malloc(stack);
        vkGetPhysicalDeviceFeatures(this.physicalDevice, supportedFeatures);
        this.wideLines = supportedFeatures.wideLines();
        this.independentBlend = supportedFeatures.independentBlend();
        var enabledFeatures = VkPhysicalDeviceFeatures.calloc(stack).wideLines(this.wideLines)
            .independentBlend(this.independentBlend);
        deviceInfo.pEnabledFeatures(enabledFeatures);
        Set<String> availableExtensions = deviceExtensions(this.physicalDevice, stack);
        Set<String> baseExtensions = new LinkedHashSet<>();
        if (this.surface != VK_NULL_HANDLE) baseExtensions.add(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
        if (availableExtensions.contains(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME)) {
            baseExtensions.add(VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME);
        }
        Set<String> nativeExtensions = new LinkedHashSet<>();
        for (String extension : NativeRenderingBridge.deviceExtensions(this.instance.address(), this.physicalDevice.address())) {
            if (availableExtensions.contains(extension)) nativeExtensions.add(extension);
            else LOGGER.log(System.Logger.Level.INFO, "Optional native rendering device extension is unavailable: {0}", extension);
        }
        // NRI uses push descriptors on Vulkan 1.3 even on non-NVIDIA devices,
        // where the independent NGX extension query can legitimately be empty.
        if (availableExtensions.contains("VK_KHR_push_descriptor")) nativeExtensions.add("VK_KHR_push_descriptor");
        boolean nativeFeatures = NativeRenderingBridge.loadFailure() == null;
        var supported11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default();
        var supported12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        var supported13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
        var supportedBda = VkPhysicalDeviceBufferDeviceAddressFeaturesKHR.calloc(stack).sType$Default();
        boolean extensionBda = !core12 && this.nativeApiVersion >= VK_MAKE_VERSION(1, 1, 0)
            && availableExtensions.contains("VK_KHR_buffer_device_address");
        if (core12) {
            supported11.pNext(supported12.address());
            if (core13) supported12.pNext(supported13.address());
            var features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(supported11.address());
            vkGetPhysicalDeviceFeatures2(this.physicalDevice, features);
        } else if (extensionBda) {
            var features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(supportedBda.address());
            vkGetPhysicalDeviceFeatures2(this.physicalDevice, features);
        }
        this.rayTracing = inspectRayTracing(availableExtensions, supportedFeatures, stack);
        this.sharc = inspectSharc(stack);
        var vulkan11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default();
        var vulkan12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        var vulkan13 = VkPhysicalDeviceVulkan13Features.calloc(stack).sType$Default();
        var bda = VkPhysicalDeviceBufferDeviceAddressFeaturesKHR.calloc(stack).sType$Default();
        var rayQuery = VkPhysicalDeviceRayQueryFeaturesKHR.calloc(stack).sType$Default();
        var acceleration = VkPhysicalDeviceAccelerationStructureFeaturesKHR.calloc(stack).sType$Default();
        var pointer = stack.mallocPointer(1);
        Set<String> enabledExtensions = new LinkedHashSet<>();
        int result;
        while (true) {
            boolean ray = this.rayTracing.available();
            boolean cache = this.sharc.available();
            this.nativeStorageExtended = nativeFeatures && supportedFeatures.shaderStorageImageExtendedFormats();
            this.nativeStorageRead = nativeFeatures && supportedFeatures.shaderStorageImageReadWithoutFormat();
            this.nativeStorageWrite = nativeFeatures && supportedFeatures.shaderStorageImageWriteWithoutFormat();
            this.nativePartiallyBound = nativeFeatures && core12 && supported12.descriptorBindingPartiallyBound();
            this.nativePushDescriptor = nativeFeatures && nativeExtensions.contains("VK_KHR_push_descriptor");
            this.nativeSynchronization2 = nativeFeatures && core13 && supported13.synchronization2();
            this.nativeDynamicRendering = nativeFeatures && core13 && supported13.dynamicRendering();
            this.nativeMaintenance4 = nativeFeatures && core13 && supported13.maintenance4();
            this.nativeBufferDeviceAddress = ray || nativeFeatures &&
                (core12 && supported12.bufferDeviceAddress() || extensionBda && supportedBda.bufferDeviceAddress());
            enabledFeatures.shaderStorageImageExtendedFormats(this.nativeStorageExtended)
                .shaderStorageImageReadWithoutFormat(this.nativeStorageRead)
                .shaderStorageImageWriteWithoutFormat(this.nativeStorageWrite)
                .shaderInt64(ray || nativeFeatures && supportedFeatures.shaderInt64())
                .shaderInt16(nativeFeatures && supportedFeatures.shaderInt16());
            long head = 0L;
            if (ray) {
                rayQuery.rayQuery(true).pNext(0L);
                acceleration.accelerationStructure(true).pNext(rayQuery.address());
                head = acceleration.address();
            }
            if (core13 && nativeFeatures) {
                vulkan13.synchronization2(this.nativeSynchronization2).dynamicRendering(this.nativeDynamicRendering)
                    .maintenance4(this.nativeMaintenance4).pNext(head);
                head = vulkan13.address();
            }
            if (core12 && (nativeFeatures || ray)) {
                vulkan12.bufferDeviceAddress(this.nativeBufferDeviceAddress)
                    .shaderSampledImageArrayNonUniformIndexing(ray)
                    .descriptorBindingPartiallyBound(this.nativePartiallyBound)
                    .shaderFloat16(cache || nativeFeatures && supported12.shaderFloat16())
                    .shaderBufferInt64Atomics(cache)
                    .timelineSemaphore(nativeFeatures && supported12.timelineSemaphore())
                    .scalarBlockLayout(nativeFeatures && supported12.scalarBlockLayout()).pNext(head);
                head = vulkan12.address();
                boolean storage16 = cache || nativeFeatures && supported11.storageBuffer16BitAccess();
                if (storage16) {
                    vulkan11.storageBuffer16BitAccess(true).pNext(head);
                    head = vulkan11.address();
                }
            } else if (nativeFeatures && extensionBda) {
                bda.bufferDeviceAddress(this.nativeBufferDeviceAddress).pNext(head);
                head = bda.address();
            }
            deviceInfo.pNext(head);
            enabledExtensions.clear();
            enabledExtensions.addAll(baseExtensions);
            if (ray) {
                enabledExtensions.add(VK_KHR_ACCELERATION_STRUCTURE_EXTENSION_NAME);
                enabledExtensions.add(VK_KHR_RAY_QUERY_EXTENSION_NAME);
                enabledExtensions.add(VK_KHR_DEFERRED_HOST_OPERATIONS_EXTENSION_NAME);
            }
            if (nativeFeatures) {
                enabledExtensions.addAll(nativeExtensions);
                if (extensionBda && this.nativeBufferDeviceAddress) enabledExtensions.add("VK_KHR_buffer_device_address");
            }
            deviceInfo.ppEnabledExtensionNames(strings(stack, enabledExtensions));
            result = vkCreateDevice(this.physicalDevice, deviceInfo, null, pointer);
            if (!optionalFeaturesRejected(result)) break;
            if (cache) {
                this.sharc = this.sharc.unavailableWith("driver rejected optional SHARC features (VkResult " + result + ")");
            } else if (nativeFeatures) {
                // Preserve ray queries when a separately installed SDK's optional features are rejected.
                nativeFeatures = false;
                LOGGER.log(System.Logger.Level.WARNING, "Driver rejected optional native rendering features (VkResult {0}); retrying the built-in renderer", result);
            } else if (ray) {
                this.rayTracing = RayTracingSupport.unavailable("driver rejected optional ray-query features (VkResult " + result + ")");
                this.sharc = this.sharc.unavailableWith("ray queries are unavailable: " + this.rayTracing.unavailableReason());
            } else break;
        }
        check(result, "create Vulkan graphics device");
        this.nativeEnabledExtensions = enabledExtensions.toArray(String[]::new);
        this.device = new VkDevice(pointer.get(0), this.physicalDevice, deviceInfo);
        vkGetDeviceQueue(this.device, this.queueFamily, 0, pointer);
        this.queue = new VkQueue(pointer.get(0), this.device);
        vkGetDeviceQueue(this.device, this.presentFamily, 0, pointer);
        this.presentQueue = new VkQueue(pointer.get(0), this.device);
        if (this.rayTracing.available()) {
            LOGGER.log(System.Logger.Level.INFO, "Vulkan KHR ray queries available on {0} ({1} ray textures; opt-in video setting)",
                this.deviceName, RAY_TEXTURE_COUNT);
        } else {
            LOGGER.log(System.Logger.Level.INFO, "Vulkan ray queries unavailable on {0}: {1}. Using raster lighting.",
                this.deviceName, this.rayTracing.unavailableReason());
        }
        if (this.sharc.available()) {
            LOGGER.log(System.Logger.Level.INFO,
                "Vulkan SHARC device features available on {0} (native fp16, 16-bit storage, buffer int64 atomics; subgroup {1})",
                this.deviceName, this.sharc.subgroupSize());
        } else LOGGER.log(System.Logger.Level.INFO, "Vulkan SHARC unavailable on {0}: {1}",
            this.deviceName, this.sharc.unavailableReason());
    }

    private static boolean optionalFeaturesRejected(int result) {
        return result == VK_ERROR_FEATURE_NOT_PRESENT || result == VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    /** The NVIDIA SDK baseline does not use subgroup or shared-memory atomics; expose subgroup limits without requiring them. */
    private SharcSupport inspectSharc(MemoryStack stack) {
        if (!this.rayTracing.available()) return SharcSupport.unavailable("ray queries are unavailable: " + this.rayTracing.unavailableReason());
        var vulkan12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default();
        var vulkan11 = VkPhysicalDeviceVulkan11Features.calloc(stack).sType$Default().pNext(vulkan12.address());
        var features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(vulkan11.address());
        vkGetPhysicalDeviceFeatures2(this.physicalDevice, features);
        var subgroup = VkPhysicalDeviceSubgroupProperties.calloc(stack).sType$Default();
        var properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(subgroup.address());
        vkGetPhysicalDeviceProperties2(this.physicalDevice, properties);
        SharcSupport support = evaluateSharcSupport(true, vulkan11.storageBuffer16BitAccess(),
            vulkan12.shaderFloat16(), vulkan12.shaderBufferInt64Atomics(),
            subgroup.subgroupSize(), subgroup.supportedStages(), subgroup.supportedOperations());
        return Boolean.getBoolean("ac.vulkan.disableSharcFeatures")
            ? support.unavailableWith("disabled by ac.vulkan.disableSharcFeatures") : support;
    }

    /** Missing SHARC features never change the independent ray-query decision. */
    static SharcSupport evaluateSharcSupport(boolean rayQueries, boolean storageBuffer16BitAccess,
                                            boolean shaderFloat16, boolean shaderBufferInt64Atomics,
                                            int subgroupSize, int subgroupStages, int subgroupOperations) {
        String reason = !rayQueries ? "ray queries are unavailable"
            : !storageBuffer16BitAccess ? "storageBuffer16BitAccess is unsupported"
            : !shaderFloat16 ? "shaderFloat16 is unsupported"
            : !shaderBufferInt64Atomics ? "shaderBufferInt64Atomics is unsupported" : null;
        return new SharcSupport(subgroupSize, subgroupStages, subgroupOperations, reason);
    }

    /** Negotiated optional SHARC device support. SDK shader availability and the selected GI quality are independent. */
    public record SharcSupport(int subgroupSize, int subgroupStages, int subgroupOperations, String unavailableReason) {
        static SharcSupport unavailable(String reason) { return new SharcSupport(0, 0, 0, reason); }
        SharcSupport unavailableWith(String reason) {
            return new SharcSupport(subgroupSize, subgroupStages, subgroupOperations, reason);
        }
        public boolean available() { return unavailableReason == null; }
    }

    /** The raster path remains available when any optional ray-query requirement is missing. */
    private RayTracingSupport inspectRayTracing(Set<String> extensions, VkPhysicalDeviceFeatures coreFeatures,
                                                MemoryStack stack) {
        var properties = VkPhysicalDeviceProperties.malloc(stack);
        vkGetPhysicalDeviceProperties(this.physicalDevice, properties);
        if (Math.min(this.instanceApiVersion, properties.apiVersion()) < VK_MAKE_VERSION(1, 2, 0)) {
            return RayTracingSupport.unavailable("Vulkan 1.2 is required");
        }
        var queueCount = stack.mallocInt(1);
        vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, queueCount, null);
        var queueProperties = VkQueueFamilyProperties.malloc(queueCount.get(0), stack);
        vkGetPhysicalDeviceQueueFamilyProperties(this.physicalDevice, queueCount, queueProperties);
        if ((queueProperties.get(this.queueFamily).queueFlags() & VK_QUEUE_COMPUTE_BIT) == 0) {
            return RayTracingSupport.unavailable("the graphics queue cannot build acceleration structures");
        }
        for (String extension : List.of(VK_KHR_ACCELERATION_STRUCTURE_EXTENSION_NAME,
            VK_KHR_RAY_QUERY_EXTENSION_NAME, VK_KHR_DEFERRED_HOST_OPERATIONS_EXTENSION_NAME)) {
            if (!extensions.contains(extension)) {
                return RayTracingSupport.unavailable("missing " + extension);
            }
        }
        if (!coreFeatures.shaderInt64()) {
            return RayTracingSupport.unavailable("shaderInt64 is unsupported");
        }
        var rayQuery = VkPhysicalDeviceRayQueryFeaturesKHR.calloc(stack).sType$Default();
        var acceleration = VkPhysicalDeviceAccelerationStructureFeaturesKHR.calloc(stack)
            .sType$Default().pNext(rayQuery.address());
        var vulkan12 = VkPhysicalDeviceVulkan12Features.calloc(stack).sType$Default().pNext(acceleration.address());
        var features = VkPhysicalDeviceFeatures2.calloc(stack).sType$Default().pNext(vulkan12.address());
        vkGetPhysicalDeviceFeatures2(this.physicalDevice, features);
        if (!acceleration.accelerationStructure()) {
            return RayTracingSupport.unavailable("accelerationStructure is unsupported");
        }
        if (!rayQuery.rayQuery()) {
            return RayTracingSupport.unavailable("rayQuery is unsupported");
        }
        if (!vulkan12.bufferDeviceAddress()) {
            return RayTracingSupport.unavailable("bufferDeviceAddress is unsupported");
        }
        if (!vulkan12.shaderSampledImageArrayNonUniformIndexing()) {
            return RayTracingSupport.unavailable("nonuniform sampled-image indexing is unsupported");
        }
        var limits = properties.limits();
        if (limits.maxBoundDescriptorSets() < RAY_DESCRIPTOR_SETS) {
            return RayTracingSupport.unavailable("fewer than " + RAY_DESCRIPTOR_SETS + " descriptor sets are supported");
        }
        if (limits.maxPerStageDescriptorStorageBuffers() < 3 || limits.maxDescriptorSetStorageBuffers() < 3) {
            return RayTracingSupport.unavailable("fewer than three ray-scene storage buffers are supported");
        }
        int sampledImages = RAY_TEXTURE_COUNT + 1;
        if (limits.maxPerStageDescriptorSamplers() < sampledImages || limits.maxPerStageDescriptorSampledImages() < sampledImages ||
            limits.maxDescriptorSetSamplers() < sampledImages || limits.maxDescriptorSetSampledImages() < sampledImages) {
            return RayTracingSupport.unavailable("fewer than " + sampledImages + " fragment samplers and sampled images are supported");
        }
        var accelerationProperties = VkPhysicalDeviceAccelerationStructurePropertiesKHR.calloc(stack).sType$Default();
        var extendedProperties = VkPhysicalDeviceProperties2.calloc(stack)
            .sType$Default().pNext(accelerationProperties.address());
        vkGetPhysicalDeviceProperties2(this.physicalDevice, extendedProperties);
        if (accelerationProperties.maxPerStageDescriptorAccelerationStructures() < 1 ||
            accelerationProperties.maxDescriptorSetAccelerationStructures() < 1 ||
            accelerationProperties.maxInstanceCount() < 1 || accelerationProperties.maxGeometryCount() < 1 ||
            accelerationProperties.maxPrimitiveCount() < 1 || accelerationProperties.minAccelerationStructureScratchOffsetAlignment() < 1) {
            return RayTracingSupport.unavailable("acceleration-structure limits are insufficient");
        }
        return new RayTracingSupport(accelerationProperties.minAccelerationStructureScratchOffsetAlignment(),
            accelerationProperties.maxInstanceCount(), accelerationProperties.maxPrimitiveCount(), null);
    }

    private record RayTracingSupport(int scratchAlignment, long maxInstances, long maxPrimitives,
                                     String unavailableReason) {
        static RayTracingSupport unavailable(String reason) {
            return new RayTracingSupport(1, 0, 0, reason);
        }

        boolean available() {
            return unavailableReason == null;
        }
    }

    private void createCommandPool(MemoryStack stack) {
        var poolInfo = VkCommandPoolCreateInfo.calloc(stack)
            .sType$Default()
            .queueFamilyIndex(this.queueFamily)
            .flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT | VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        var handle = stack.mallocLong(1);
        check(vkCreateCommandPool(this.device, poolInfo, null, handle), "create Vulkan command pool");
        this.commandPool = handle.get(0);
    }

    private void selectPhysicalDevice(MemoryStack stack) {
        var count = stack.mallocInt(1);
        PointerBuffer handles;
        int result;
        do {
            check(vkEnumeratePhysicalDevices(this.instance, count, null), "count Vulkan physical devices");
            if (count.get(0) == 0) {
                throw new IllegalStateException("No Vulkan physical device is available; install a Vulkan driver");
            }
            handles = stack.mallocPointer(count.get(0));
            result = vkEnumeratePhysicalDevices(this.instance, count, handles);
        } while (result == VK_INCOMPLETE);
        check(result, "enumerate Vulkan physical devices");

        int bestScore = -1;
        var properties = VkPhysicalDeviceProperties.malloc(stack);
        for (int i = 0; i < count.get(0); i++) {
            var candidate = new VkPhysicalDevice(handles.get(i), this.instance);
            QueueFamilies families = queueFamilies(candidate, stack);
            int family = families.graphics();
            if (family < 0) {
                continue;
            }
            if (this.surface != VK_NULL_HANDLE &&
                (families.present() < 0 || !supportsSwapchain(candidate, stack))) {
                continue;
            }
            vkGetPhysicalDeviceProperties(candidate, properties);
            int score = switch (properties.deviceType()) {
                case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU -> 400;
                case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU -> 300;
                case VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU -> 200;
                case VK_PHYSICAL_DEVICE_TYPE_CPU -> 100;
                default -> 0;
            };
            if (score > bestScore) {
                bestScore = score;
                this.physicalDevice = candidate;
                this.queueFamily = family;
                this.presentFamily = families.present();
                this.deviceName = properties.deviceNameString();
            }
        }
        if (this.physicalDevice == null) {
            throw new IllegalStateException("No Vulkan device provides compatible graphics and presentation queues");
        }
    }

    private boolean supportsSwapchain(VkPhysicalDevice candidate, MemoryStack stack) {
        if (!deviceExtensions(candidate, stack).contains(VK_KHR_SWAPCHAIN_EXTENSION_NAME)) {
            return false;
        }
        var capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
        check(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(candidate, this.surface, capabilities),
            "query Vulkan device surface capabilities");
        if ((capabilities.supportedUsageFlags() & VK_IMAGE_USAGE_TRANSFER_DST_BIT) == 0) {
            return false;
        }
        var format = VkFormatProperties.malloc(stack);
        vkGetPhysicalDeviceFormatProperties(candidate, VK_FORMAT_R8G8B8A8_UNORM, format);
        if ((format.optimalTilingFeatures() & VK_FORMAT_FEATURE_BLIT_SRC_BIT) == 0) {
            return false;
        }
        var count = stack.mallocInt(1);
        check(vkGetPhysicalDeviceSurfaceFormatsKHR(candidate, this.surface, count, null),
            "count Vulkan device surface formats");
        return count.get(0) > 0;
    }

    private record QueueFamilies(int graphics, int present) { }

    private QueueFamilies queueFamilies(VkPhysicalDevice physicalDevice, MemoryStack stack) {
        var count = stack.mallocInt(1);
        var supported = stack.mallocInt(1);
        int graphics = -1;
        int present = -1;
        vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, count, null);
        try (var properties = VkQueueFamilyProperties.malloc(count.get(0))) {
            vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, count, properties);
            for (int i = 0; i < count.get(0); i++) {
                var family = properties.get(i);
                if (family.queueCount() == 0) {
                    continue;
                }
                boolean hasGraphics = (family.queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0;
                boolean hasPresent = this.surface == VK_NULL_HANDLE;
                if (this.surface != VK_NULL_HANDLE) {
                    check(vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, this.surface, supported),
                        "query Vulkan presentation support");
                    hasPresent = supported.get(0) == VK_TRUE;
                }
                if (hasGraphics && hasPresent) {
                    return new QueueFamilies(i, i);
                }
                if (hasGraphics && graphics < 0) {
                    graphics = i;
                }
                if (hasPresent && present < 0) {
                    present = i;
                }
            }
        }
        return new QueueFamilies(graphics, present);
    }

    private static Set<String> instanceExtensions(MemoryStack stack) {
        var count = stack.mallocInt(1);
        while (true) {
            check(vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, null), "count Vulkan instance extensions");
            // Extension lists on current drivers can exceed MemoryStack's default 64 KiB.
            try (var properties = VkExtensionProperties.malloc(count.get(0))) {
                int result = vkEnumerateInstanceExtensionProperties((ByteBuffer) null, count, properties);
                if (result == VK_INCOMPLETE) {
                    continue;
                }
                check(result, "enumerate Vulkan instance extensions");
                var extensions = new HashSet<String>();
                for (int i = 0; i < count.get(0); i++) {
                    extensions.add(properties.get(i).extensionNameString());
                }
                return extensions;
            }
        }
    }

    private static Set<String> deviceExtensions(VkPhysicalDevice device, MemoryStack stack) {
        var count = stack.mallocInt(1);
        while (true) {
            check(vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, count, null), "count Vulkan device extensions");
            try (var properties = VkExtensionProperties.malloc(count.get(0))) {
                int result = vkEnumerateDeviceExtensionProperties(device, (ByteBuffer) null, count, properties);
                if (result == VK_INCOMPLETE) {
                    continue;
                }
                check(result, "enumerate Vulkan device extensions");
                var extensions = new HashSet<String>();
                for (int i = 0; i < count.get(0); i++) {
                    extensions.add(properties.get(i).extensionNameString());
                }
                return extensions;
            }
        }
    }

    private static Set<String> instanceLayers(MemoryStack stack) {
        var count = stack.mallocInt(1);
        while (true) {
            check(vkEnumerateInstanceLayerProperties(count, null), "count Vulkan instance layers");
            try (var properties = VkLayerProperties.malloc(count.get(0))) {
                int result = vkEnumerateInstanceLayerProperties(count, properties);
                if (result == VK_INCOMPLETE) {
                    continue;
                }
                check(result, "enumerate Vulkan instance layers");
                var layers = new HashSet<String>();
                for (int i = 0; i < count.get(0); i++) {
                    layers.add(properties.get(i).layerNameString());
                }
                return layers;
            }
        }
    }

    private static PointerBuffer strings(MemoryStack stack, Set<String> values) {
        if (values.isEmpty()) {
            return null;
        }
        var result = stack.mallocPointer(values.size());
        for (String value : values) {
            result.put(stack.UTF8(value));
        }
        return result.flip();
    }

    public VkInstance instance() {
        ensureOpen();
        return this.instance;
    }

    public VkPhysicalDevice physicalDevice() {
        ensureOpen();
        return this.physicalDevice;
    }

    public VkDevice device() {
        ensureOpen();
        return this.device;
    }

    public VkQueue queue() {
        ensureOpen();
        return this.queue;
    }

    public int queueFamily() {
        ensureOpen();
        return this.queueFamily;
    }

    public long window() {
        ensureOpen();
        return this.window;
    }

    public long surface() {
        ensureOpen();
        return this.surface;
    }

    public VkQueue presentQueue() {
        ensureOpen();
        return this.presentQueue;
    }

    public int presentFamily() {
        ensureOpen();
        return this.presentFamily;
    }

    public long commandPool() {
        ensureOpen();
        return this.commandPool;
    }

    public String deviceName() {
        return this.deviceName;
    }

    public boolean validationEnabled() {
        return this.validationEnabled;
    }

    public boolean independentBlendSupported() { return independentBlend; }

    /** Actual logical-device capabilities passed to the optional native integrations. */
    public String[] nativeEnabledExtensions() { return this.nativeEnabledExtensions.clone(); }
    public boolean nativeStorageExtended() { return this.nativeStorageExtended; }
    public boolean nativeStorageRead() { return this.nativeStorageRead; }
    public boolean nativeStorageWrite() { return this.nativeStorageWrite; }
    public boolean nativePartiallyBound() { return this.nativePartiallyBound; }
    public boolean nativeSynchronization2() { return this.nativeSynchronization2; }
    public boolean nativeDynamicRendering() { return this.nativeDynamicRendering; }
    public boolean nativeMaintenance4() { return this.nativeMaintenance4; }
    public boolean nativeBufferDeviceAddress() { return this.nativeBufferDeviceAddress; }
    public boolean nativePushDescriptorEnabled() { return this.nativePushDescriptor; }
    public int nativeApiVersion() { return this.nativeApiVersion; }

    public boolean wideLines() {
        ensureOpen();
        return this.wideLines;
    }

    /** Hardware and negotiated driver features, independent of the user's rendering choice. */
    public boolean rayTracingAvailable() {
        ensureOpen();
        return this.rayTracing.available();
    }

    /** Compatibility alias for device capability checks in descriptor/pipeline creation. */
    public boolean rayTracingSupported() { return rayTracingAvailable(); }

    public boolean sharcAvailable() {
        ensureOpen();
        return this.sharc.available();
    }

    public String sharcUnavailableReason() {
        ensureOpen();
        return this.sharc.unavailableReason();
    }

    public SharcSupport sharcSupport() {
        ensureOpen();
        return this.sharc;
    }

    /** Buffer addresses may be enabled by native reconstruction without ray-query support. */
    public boolean bufferDeviceAddressSupported() {
        ensureOpen();
        return this.nativeBufferDeviceAddress || this.rayTracing.available();
    }

    public int accelerationScratchAlignment() {
        ensureOpen();
        return this.rayTracing.scratchAlignment();
    }

    public long maxRayInstances() {
        ensureOpen();
        return this.rayTracing.maxInstances();
    }

    public long maxRayPrimitives() {
        ensureOpen();
        return this.rayTracing.maxPrimitives();
    }

    public int validationErrorCount() {
        return this.validationErrorCount.get();
    }

    public List<String> validationMessages() {
        synchronized (this.validationMessages) {
            return List.copyOf(this.validationMessages);
        }
    }

    public VulkanMemoryProfile memoryProfile() {
        ensureOpen();
        return this.memoryProfile;
    }

    public int memoryType(int allowedTypes, int requiredFlags) {
        return memoryType(allowedTypes, requiredFlags, 0);
    }

    int memoryType(int allowedTypes, int requiredFlags, int preferredFlags) {
        ensureOpen();
        return this.memoryProfile.select(allowedTypes, requiredFlags, preferredFlags);
    }

    synchronized VulkanImageAllocator imageAllocator() {
        ensureOpen();
        if (this.imageAllocator == null) {
            this.imageAllocator = new VulkanImageAllocator(this);
        }
        return this.imageAllocator;
    }

    public synchronized VulkanBuffer buffer(long size, int usage, int memoryFlags) {
        ensureOpen();
        var buffer = VulkanBuffer.create(this, size, usage, memoryFlags);
        this.buffers.add(buffer);
        return buffer;
    }

    public VulkanBuffer hostBuffer(long size, int usage) {
        return buffer(size, usage, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
    }

    void release(VulkanBuffer buffer) {
        this.buffers.remove(buffer);
    }

    /**
     * Records, submits, and waits for a single command buffer. Intended for initialization,
     * explicit blocking readbacks, and small initialization jobs. Steady-state rendering and
     * resource uploads use reusable frame submissions instead of this blocking path.
     */
    public synchronized void immediate(Consumer<VkCommandBuffer> commands) {
        Objects.requireNonNull(commands, "commands");
        ensureOpen();
        try (var stack = stackPush()) {
            var allocation = VkCommandBufferAllocateInfo.calloc(stack)
                .sType$Default()
                .commandPool(this.commandPool)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(1);
            var pointer = stack.mallocPointer(1);
            check(vkAllocateCommandBuffers(this.device, allocation, pointer), "allocate Vulkan command buffer");
            var command = new VkCommandBuffer(pointer.get(0), this.device);
            long fence = VK_NULL_HANDLE;
            boolean submitted = false;
            try {
                var begin = VkCommandBufferBeginInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
                check(vkBeginCommandBuffer(command, begin), "begin Vulkan command buffer");
                commands.accept(command);
                check(vkEndCommandBuffer(command), "end Vulkan command buffer");
                var fenceInfo = VkFenceCreateInfo.calloc(stack).sType$Default();
                var handle = stack.mallocLong(1);
                check(vkCreateFence(this.device, fenceInfo, null, handle), "create Vulkan submission fence");
                fence = handle.get(0);
                var submit = VkSubmitInfo.calloc(stack)
                    .sType$Default()
                    .pCommandBuffers(stack.pointers(command));
                check(vkQueueSubmit(this.queue, submit, fence), "submit Vulkan command buffer");
                submitted = true;
                check(vkWaitForFences(this.device, fence, true, -1L), "wait for Vulkan submission");
                submitted = false;
            }
            finally {
                if (submitted) {
                    // A failed fence wait must not leave a live command buffer being freed.
                    vkDeviceWaitIdle(this.device);
                }
                if (fence != VK_NULL_HANDLE) {
                    vkDestroyFence(this.device, fence, null);
                }
                vkFreeCommandBuffers(this.device, this.commandPool, command);
            }
        }
    }

    public synchronized void waitIdle() {
        ensureOpen();
        check(vkDeviceWaitIdle(this.device), "wait for Vulkan device");
    }

    void ensureOpen() {
        if (this.closed) {
            throw new IllegalStateException("Vulkan device is closed");
        }
    }

    public static void check(int result, String operation) {
        if (result != VK_SUCCESS) {
            throw new IllegalStateException("Failed to " + operation + " (VkResult " + result + ")");
        }
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        int idleResult = VK_SUCCESS;
        if (this.device != null) {
            idleResult = vkDeviceWaitIdle(this.device);
            // close() removes buffers from the set, so iterate over a stable copy.
            for (var buffer : List.copyOf(this.buffers)) {
                buffer.close();
            }
            if (this.imageAllocator != null) {
                this.imageAllocator.close();
            }
            if (this.commandPool != VK_NULL_HANDLE) {
                vkDestroyCommandPool(this.device, this.commandPool, null);
            }
            vkDestroyDevice(this.device, null);
        }
        if (this.debugMessenger != VK_NULL_HANDLE) {
            vkDestroyDebugUtilsMessengerEXT(this.instance, this.debugMessenger, null);
        }
        if (this.surface != VK_NULL_HANDLE) {
            vkDestroySurfaceKHR(this.instance, this.surface, null);
        }
        if (this.instance != null) {
            vkDestroyInstance(this.instance, null);
        }
        if (this.debugCallback != null) {
            this.debugCallback.free();
        }
        this.closed = true;
        check(idleResult, "wait for Vulkan device during shutdown");
    }
}
