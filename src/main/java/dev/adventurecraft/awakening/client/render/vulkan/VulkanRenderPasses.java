package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.state.AttachmentIntent.LoadOp;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import java.util.HashMap;
import java.util.Map;
import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Compatible render passes differing only in initial contents and final attachment retention. */
final class VulkanRenderPasses implements AutoCloseable {
    private record Key(LoadOp color, LoadOp depth, boolean storeColor, boolean storeDepth) { }
    private final VulkanDevice context;
    private final int colorFormat, depthFormat, reactiveFormat;
    private final Map<Key, Long> passes = new HashMap<>();

    VulkanRenderPasses(VulkanDevice context,int colorFormat,int depthFormat) {
        this(context, colorFormat, depthFormat, 0);
    }

    VulkanRenderPasses(VulkanDevice context,int colorFormat,int depthFormat,int reactiveFormat) {
        this.context=context; this.colorFormat=colorFormat; this.depthFormat=depthFormat; this.reactiveFormat=reactiveFormat;
    }

    long get(LoadOp color,LoadOp depth,boolean storeColor,boolean storeDepth) {
        return passes.computeIfAbsent(new Key(color,depth,storeColor,storeDepth),this::create);
    }

    private long create(Key key) {
        try(var stack=MemoryStack.stackPush()) {
            int colorCount = reactiveFormat == 0 ? 1 : 2;
            var attachments=VkAttachmentDescription.calloc(colorCount + 1,stack);
            attachment(attachments.get(0),colorFormat,key.color,key.storeColor);
            if (reactiveFormat != 0) attachment(attachments.get(1),reactiveFormat,key.color,key.storeColor);
            attachment(attachments.get(colorCount),depthFormat,key.depth,key.storeDepth);
            var colorRef=VkAttachmentReference.calloc(colorCount,stack);
            for (int i=0;i<colorCount;i++) colorRef.get(i).attachment(i).layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            var depthRef=VkAttachmentReference.calloc(stack).attachment(colorCount).layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            var subpass=VkSubpassDescription.calloc(1,stack).pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(colorCount).pColorAttachments(colorRef).pDepthStencilAttachment(depthRef);
            var dependencies=VkSubpassDependency.calloc(2,stack);
            dependencies.get(0).srcSubpass(VK_SUBPASS_EXTERNAL).dstSubpass(0)
                .srcStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT)
                .dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT|VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT|VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT)
                .srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT|VK_ACCESS_TRANSFER_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT|VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT|VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT|VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependencies.get(1).srcSubpass(0).dstSubpass(VK_SUBPASS_EXTERNAL)
                .srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT|VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT|VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT)
                .dstStageMask(VK_PIPELINE_STAGE_TRANSFER_BIT)
                .srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT|VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT)
                .dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
            var info=VkRenderPassCreateInfo.calloc(stack).sType$Default().pAttachments(attachments).pSubpasses(subpass).pDependencies(dependencies);
            var result=stack.mallocLong(1);
            check(vkCreateRenderPass(context.device(),info,null,result),"create attachment render pass");
            return result.get(0);
        }
    }

    private static void attachment(VkAttachmentDescription attachment,int format,LoadOp load,boolean store) {
        attachment.format(format).samples(VK_SAMPLE_COUNT_1_BIT).loadOp(switch(load) {
            case LOAD -> VK_ATTACHMENT_LOAD_OP_LOAD;
            case CLEAR -> VK_ATTACHMENT_LOAD_OP_CLEAR;
            case DISCARD -> VK_ATTACHMENT_LOAD_OP_DONT_CARE;
        }).storeOp(store?VK_ATTACHMENT_STORE_OP_STORE:VK_ATTACHMENT_STORE_OP_DONT_CARE)
            .stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE).stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE)
            .initialLayout(load==LoadOp.LOAD?VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL:VK_IMAGE_LAYOUT_UNDEFINED)
            .finalLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
    }

    @Override public void close() {
        for(long pass:passes.values()) vkDestroyRenderPass(context.device(),pass,null);
        passes.clear();
    }
}
