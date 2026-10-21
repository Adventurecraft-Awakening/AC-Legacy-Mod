package dev.adventurecraft.awakening.client.render.vulkan;

import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;

/** Reusable graphics submission and the resources that must outlive it. */
final class VulkanCommandSlot implements AutoCloseable {
    private final VulkanDevice context;
    private final long pool;
    private final VkCommandBuffer command;
    private final long fence;
    private final List<Runnable> retired = new ArrayList<>();
    private boolean submitted;
    private boolean closed;

    VulkanCommandSlot(VulkanDevice context) {
        this.context = context;
        long createdPool = VK_NULL_HANDLE;
        long createdFence = VK_NULL_HANDLE;
        try (var stack = stackPush()) {
            var handle = stack.mallocLong(1);
            var poolInfo = VkCommandPoolCreateInfo.calloc(stack).sType$Default()
                .flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT).queueFamilyIndex(context.queueFamily());
            check(vkCreateCommandPool(context.device(), poolInfo, null, handle), "create frame command pool");
            createdPool = handle.get(0);
            var allocation = VkCommandBufferAllocateInfo.calloc(stack).sType$Default()
                .commandPool(createdPool).level(VK_COMMAND_BUFFER_LEVEL_PRIMARY).commandBufferCount(1);
            var pointer = stack.mallocPointer(1);
            check(vkAllocateCommandBuffers(context.device(), allocation, pointer), "allocate frame command buffer");
            this.command = new VkCommandBuffer(pointer.get(0), context.device());
            var fenceInfo = VkFenceCreateInfo.calloc(stack).sType$Default();
            check(vkCreateFence(context.device(), fenceInfo, null, handle), "create frame fence");
            createdFence = handle.get(0);
            this.pool = createdPool;
            this.fence = createdFence;
        } catch (Throwable error) {
            if (createdFence != VK_NULL_HANDLE) vkDestroyFence(context.device(), createdFence, null);
            if (createdPool != VK_NULL_HANDLE) vkDestroyCommandPool(context.device(), createdPool, null);
            throw error;
        }
    }

    VkCommandBuffer begin() {
        synchronized (context) {
            await();
            check(vkResetCommandPool(context.device(), pool, 0), "reset frame command pool");
            try (var stack = stackPush()) {
                var begin = VkCommandBufferBeginInfo.calloc(stack).sType$Default()
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
                check(vkBeginCommandBuffer(command, begin), "begin frame command buffer");
            }
            return command;
        }
    }

    void submit() { submit(VK_NULL_HANDLE, 0, VK_NULL_HANDLE); }

    void submit(long waitSemaphore, int waitStage, long signalSemaphore) {
        synchronized (context) {
            check(vkEndCommandBuffer(command), "end frame command buffer");
            check(vkResetFences(context.device(), fence), "reset frame fence");
            try (var stack = stackPush()) {
                var submit = VkSubmitInfo.calloc(stack).sType$Default().pCommandBuffers(stack.pointers(command));
                if (waitSemaphore != VK_NULL_HANDLE) {
                    // These two pointers share a count, so LWJGL does not infer it from either buffer.
                    submit.pWaitSemaphores(stack.longs(waitSemaphore)).pWaitDstStageMask(stack.ints(waitStage)).waitSemaphoreCount(1);
                }
                if (signalSemaphore != VK_NULL_HANDLE) submit.pSignalSemaphores(stack.longs(signalSemaphore));
                check(vkQueueSubmit(context.queue(), submit, fence), "submit Vulkan frame");
                submitted = true;
            }
        }
    }

    void defer(Runnable action) { retired.add(action); }

    void retire(Runnable action) {
        synchronized (context) {
            if (!submitted || vkGetFenceStatus(context.device(), fence) == VK_SUCCESS) action.run();
            else retired.add(action);
        }
    }

    void await() {
        synchronized (context) {
            if (submitted) {
                check(vkWaitForFences(context.device(), fence, true, -1L), "wait for reusable frame slot");
                submitted = false;
            }
            for (Runnable action : retired) action.run();
            retired.clear();
        }
    }

    @Override public void close() {
        if (closed) return;
        await();
        vkDestroyFence(context.device(), fence, null);
        vkDestroyCommandPool(context.device(), pool, null);
        closed = true;
    }
}
