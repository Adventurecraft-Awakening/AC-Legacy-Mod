package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.vulkan.*;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Presents the game's Vulkan color target directly to a GLFW surface. The render target stays on
 * the GPU: presentation performs a format-converting blit and never reads pixels through the CPU.
 * Two reusable submission slots overlap CPU recording with GPU work. Presentation completion
 * semaphores remain associated with swapchain images rather than CPU frame slots.
 */
public final class VulkanSwapchain implements AutoCloseable {

    private final VulkanDevice context;
    private long swapchain;
    private long[] images = new long[0];
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private final VulkanCommandSlot[] slots = new VulkanCommandSlot[2];
    private final long[] imageAvailable = new long[2];
    private int nextSlot;
    private long[] renderFinished = new long[0];
    private int swapInterval;
    private int presentMode = VK_PRESENT_MODE_FIFO_KHR;
    private int nonFifoPresentMode = VK_PRESENT_MODE_FIFO_KHR;
    private boolean dirty = true;
    private boolean closed;

    public VulkanSwapchain(VulkanDevice context) {
        if (context.surface() == VK_NULL_HANDLE) {
            throw new IllegalArgumentException("Vulkan presentation requires a window surface");
        }
        this.context = context;
        try (var stack = stackPush()) {
            var sourceFormat = VkFormatProperties.malloc(stack);
            vkGetPhysicalDeviceFormatProperties(context.physicalDevice(), VK_FORMAT_R8G8B8A8_UNORM, sourceFormat);
            if ((sourceFormat.optimalTilingFeatures() & VK_FORMAT_FEATURE_BLIT_SRC_BIT) == 0) {
                throw new IllegalStateException("Vulkan device cannot blit the game's RGBA8 color target");
            }
            var handle = stack.mallocLong(1);
            var semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack).sType$Default();
            for (int i = 0; i < slots.length; i++) {
                slots[i] = new VulkanCommandSlot(context);
                check(vkCreateSemaphore(context.device(), semaphoreInfo, null, handle), "create image acquisition semaphore");
                imageAvailable[i] = handle.get(0);
            }
            recreate();
        } catch (Throwable error) {
            try {
                close();
            } catch (Throwable cleanupError) {
                error.addSuppressed(cleanupError);
            }
            throw error;
        }
    }

    public synchronized void resize(int width, int height) {
        this.dirty = true;
    }

    public synchronized void setSwapInterval(int interval) {
        this.swapInterval = interval;
    }

    /** Source must be VK_FORMAT_R8G8B8A8_UNORM in VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL. */
    public synchronized void present(long sourceImage, int sourceWidth, int sourceHeight) {
        presentImage(sourceImage, sourceWidth, sourceHeight);
    }

    private int requestedPresentMode() {
        return swapInterval != 0 ? VK_PRESENT_MODE_FIFO_KHR : nonFifoPresentMode;
    }

    private void presentImage(long sourceImage, int sourceWidth, int sourceHeight) {
        if (this.closed) {
            throw new IllegalStateException("Vulkan swapchain is closed");
        }
        if (sourceImage == VK_NULL_HANDLE || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }
        synchronized (this.context) {
            try (var stack = stackPush()) {
                var framebufferWidth = stack.mallocInt(1);
                var framebufferHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(this.context.window(), framebufferWidth, framebufferHeight);
                if (framebufferWidth.get(0) == 0 || framebufferHeight.get(0) == 0) {
                    this.dirty = true;
                    return;
                }
                if (this.dirty || this.swapchain == VK_NULL_HANDLE || this.presentMode != requestedPresentMode() ||
                    this.framebufferWidth != framebufferWidth.get(0) || this.framebufferHeight != framebufferHeight.get(0)) {
                    if (!recreate()) {
                        return;
                    }
                }
                VulkanCommandSlot slot = slots[nextSlot];
                slot.await();
                var imageIndex = stack.mallocInt(1);
                int acquired = vkAcquireNextImageKHR(this.context.device(), this.swapchain, -1L,
                    this.imageAvailable[nextSlot], VK_NULL_HANDLE, imageIndex);
                if (acquired == VK_ERROR_OUT_OF_DATE_KHR) {
                    this.dirty = true;
                    return;
                }
                if (acquired != VK_SUCCESS && acquired != VK_SUBOPTIMAL_KHR) {
                    check(acquired, "acquire Vulkan presentation image");
                }
                this.dirty = acquired == VK_SUBOPTIMAL_KHR;
                VkCommandBuffer command = slot.begin();

                long destination = this.images[imageIndex.get(0)];
                long finished = this.renderFinished[imageIndex.get(0)];
                var barrier = VkImageMemoryBarrier.calloc(1, stack);
                barrier.get(0).sType$Default()
                    .oldLayout(VK_IMAGE_LAYOUT_UNDEFINED).newLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
                    .srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED).dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
                    .srcAccessMask(0).dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT).image(destination);
                barrier.get(0).subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0).levelCount(1).baseArrayLayer(0).layerCount(1);
                // Chain the transition after the acquire semaphore's TRANSFER wait stage.
                vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_TRANSFER_BIT, 0, null, null, barrier);

                var blit = VkImageBlit.calloc(1, stack);
                blit.get(0).srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0)
                    .baseArrayLayer(0).layerCount(1);
                blit.get(0).srcOffsets(0).set(0, 0, 0);
                blit.get(0).srcOffsets(1).set(sourceWidth, sourceHeight, 1);
                blit.get(0).dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).mipLevel(0)
                    .baseArrayLayer(0).layerCount(1);
                blit.get(0).dstOffsets(0).set(0, 0, 0);
                blit.get(0).dstOffsets(1).set(this.width, this.height, 1);
                vkCmdBlitImage(command, sourceImage, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    destination, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, blit, VK_FILTER_NEAREST);

                barrier.get(0).oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL).newLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
                    .srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT).dstAccessMask(0);
                vkCmdPipelineBarrier(command, VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, 0, null, null, barrier);
                slot.submit(imageAvailable[nextSlot], VK_PIPELINE_STAGE_TRANSFER_BIT, finished);
                nextSlot = (nextSlot + 1) % slots.length;
                var presentInfo = VkPresentInfoKHR.calloc(stack).sType$Default()
                    .pWaitSemaphores(stack.longs(finished))
                    .swapchainCount(1).pSwapchains(stack.longs(this.swapchain)).pImageIndices(imageIndex);
                int presented = vkQueuePresentKHR(this.context.presentQueue(), presentInfo);
                // Presentation semaphores belong to swapchain images. Reacquiring an image and
                // waiting on imageAvailable protects reuse of that image's finished semaphore.
                // A graphics fence or queue-idle wait alone cannot guarantee presentation reuse.
                if (presented == VK_ERROR_OUT_OF_DATE_KHR || presented == VK_SUBOPTIMAL_KHR) {
                    this.dirty = true;
                } else {
                    check(presented, "present Vulkan frame");
                }
            }
        }
    }

    private boolean recreate() {
        try (var stack = stackPush()) {
            var framebufferWidth = stack.mallocInt(1);
            var framebufferHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(this.context.window(), framebufferWidth, framebufferHeight);
            if (framebufferWidth.get(0) <= 0 || framebufferHeight.get(0) <= 0) {
                return false;
            }
            this.context.waitIdle();
            var capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
            check(vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.context.physicalDevice(), this.context.surface(), capabilities),
                "query Vulkan surface capabilities");
            if ((capabilities.supportedUsageFlags() & VK_IMAGE_USAGE_TRANSFER_DST_BIT) == 0) {
                throw new IllegalStateException("Vulkan surface does not support GPU transfer presentation");
            }
            var count = stack.mallocInt(1);
            check(vkGetPhysicalDeviceSurfaceFormatsKHR(this.context.physicalDevice(), this.context.surface(), count, null),
                "count Vulkan surface formats");
            if (count.get(0) == 0) {
                throw new IllegalStateException("No Vulkan surface format is available");
            }
            var formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
            check(vkGetPhysicalDeviceSurfaceFormatsKHR(this.context.physicalDevice(), this.context.surface(), count, formats),
                "query Vulkan surface formats");
            int format = VK_FORMAT_UNDEFINED;
            int colorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
            int bestFormatScore = -1;
            var formatProperties = VkFormatProperties.malloc(stack);
            for (int i = 0; i < count.get(0); i++) {
                int[] candidates = formats.get(i).format() == VK_FORMAT_UNDEFINED ?
                    new int[]{VK_FORMAT_B8G8R8A8_UNORM, VK_FORMAT_R8G8B8A8_UNORM} : new int[]{formats.get(i).format()};
                for (int candidate : candidates) {
                    vkGetPhysicalDeviceFormatProperties(this.context.physicalDevice(), candidate, formatProperties);
                    if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_BLIT_DST_BIT) == 0) {
                        continue;
                    }
                    int score = (candidate == VK_FORMAT_B8G8R8A8_UNORM || candidate == VK_FORMAT_R8G8B8A8_UNORM) ? 2 : 0;
                    if (formats.get(i).colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                        score++;
                    }
                    if (score > bestFormatScore) {
                        bestFormatScore = score;
                        format = candidate;
                        colorSpace = formats.get(i).colorSpace();
                    }
                }
            }
            if (format == VK_FORMAT_UNDEFINED) {
                throw new IllegalStateException("Vulkan surface format cannot receive a GPU color blit");
            }
            int targetWidth = capabilities.currentExtent().width();
            int targetHeight = capabilities.currentExtent().height();
            if (targetWidth == -1) {
                targetWidth = Math.clamp(framebufferWidth.get(0), capabilities.minImageExtent().width(), capabilities.maxImageExtent().width());
                targetHeight = Math.clamp(framebufferHeight.get(0), capabilities.minImageExtent().height(), capabilities.maxImageExtent().height());
            }
            if (targetWidth == 0 || targetHeight == 0) {
                return false;
            }
            int imageCount = capabilities.minImageCount() + 1;
            if (capabilities.maxImageCount() > 0) {
                imageCount = Math.min(imageCount, capabilities.maxImageCount());
            }
            int compositeAlpha = Integer.lowestOneBit(capabilities.supportedCompositeAlpha());
            int preferredMode = VK_PRESENT_MODE_FIFO_KHR;
            {
                while (true) {
                    check(vkGetPhysicalDeviceSurfacePresentModesKHR(this.context.physicalDevice(), this.context.surface(), count, null),
                        "count Vulkan surface presentation modes");
                    var modes = stack.mallocInt(count.get(0));
                    int result = vkGetPhysicalDeviceSurfacePresentModesKHR(this.context.physicalDevice(), this.context.surface(), count, modes);
                    if (result == VK_INCOMPLETE) {
                        continue;
                    }
                    check(result, "query Vulkan surface presentation modes");
                    for (int i = 0; i < count.get(0); i++) {
                        if (modes.get(i) == VK_PRESENT_MODE_IMMEDIATE_KHR) {
                            preferredMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
                            break;
                        }
                        if (modes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                            preferredMode = VK_PRESENT_MODE_MAILBOX_KHR;
                        }
                    }
                    break;
                }
            }
            int selectedMode = swapInterval != 0 ? VK_PRESENT_MODE_FIFO_KHR : preferredMode;
            var createInfo = VkSwapchainCreateInfoKHR.calloc(stack).sType$Default()
                .surface(this.context.surface()).minImageCount(imageCount)
                .imageFormat(format).imageColorSpace(colorSpace).imageArrayLayers(1)
                .imageUsage(VK_IMAGE_USAGE_TRANSFER_DST_BIT)
                .imageSharingMode(VK_SHARING_MODE_EXCLUSIVE)
                .preTransform(capabilities.currentTransform()).compositeAlpha(compositeAlpha)
                .presentMode(selectedMode).clipped(true).oldSwapchain(this.swapchain);
            createInfo.imageExtent().set(targetWidth, targetHeight);
            if (this.context.queueFamily() != this.context.presentFamily()) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT)
                    .pQueueFamilyIndices(stack.ints(this.context.queueFamily(), this.context.presentFamily()));
            }
            var handle = stack.mallocLong(1);
            check(vkCreateSwapchainKHR(this.context.device(), createInfo, null, handle), "create Vulkan swapchain");
            long previous = this.swapchain;
            this.swapchain = handle.get(0);
            this.presentMode = selectedMode;
            this.nonFifoPresentMode = preferredMode;
            if (previous != VK_NULL_HANDLE) {
                vkDestroySwapchainKHR(this.context.device(), previous, null);
                // Replacing the swapchain also resets its acquisition synchronization.
                var semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack).sType$Default();
                for (int i = 0; i < imageAvailable.length; i++) {
                    vkDestroySemaphore(this.context.device(), imageAvailable[i], null);
                    imageAvailable[i] = VK_NULL_HANDLE;
                    check(vkCreateSemaphore(this.context.device(), semaphoreInfo, null, handle), "recreate image acquisition semaphore");
                    imageAvailable[i] = handle.get(0);
                }
            }
            check(vkGetSwapchainImagesKHR(this.context.device(), this.swapchain, count, null), "count Vulkan swapchain images");
            var imageHandles = stack.mallocLong(count.get(0));
            check(vkGetSwapchainImagesKHR(this.context.device(), this.swapchain, count, imageHandles), "query Vulkan swapchain images");
            this.images = new long[count.get(0)];
            imageHandles.get(this.images);
            for (long semaphore : this.renderFinished) {
                vkDestroySemaphore(this.context.device(), semaphore, null);
            }
            this.renderFinished = new long[this.images.length];
            var semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack).sType$Default();
            for (int i = 0; i < this.renderFinished.length; i++) {
                check(vkCreateSemaphore(this.context.device(), semaphoreInfo, null, handle), "create image presentation semaphore");
                this.renderFinished[i] = handle.get(0);
            }
            this.width = targetWidth;
            this.height = targetHeight;
            this.framebufferWidth = framebufferWidth.get(0);
            this.framebufferHeight = framebufferHeight.get(0);
            this.dirty = false;
            return true;
        }
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        synchronized (this.context) {
            this.context.waitIdle();
            if (this.swapchain != VK_NULL_HANDLE) {
                vkDestroySwapchainKHR(this.context.device(), this.swapchain, null);
            }
            for (long semaphore : this.renderFinished) {
                if (semaphore != VK_NULL_HANDLE) {
                    vkDestroySemaphore(this.context.device(), semaphore, null);
                }
            }
            for (long semaphore : imageAvailable) if (semaphore != VK_NULL_HANDLE) {
                vkDestroySemaphore(context.device(), semaphore, null);
            }
            for (VulkanCommandSlot slot : slots) if (slot != null) slot.close();
            this.closed = true;
        }
    }
}
