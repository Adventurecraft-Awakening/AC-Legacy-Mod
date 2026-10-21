package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.state.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Owns graphics pipelines, shader modules and their persistent driver cache. */
final class VulkanPipelines implements AutoCloseable {
    private static final int LIGHTING_SPECIALIZATION_ID = 0;
    private static final int FOG_SPECIALIZATION_ID = 1;
    private static final int REFLECTION_SPECIALIZATION_ID = 2;
    private static final int WRITE_COLOR_SPECIALIZATION_ID = 3;
    private static final int LINEAR_OUTPUT_SPECIALIZATION_ID = 4;

    private final VulkanDevice context;
    private final long pipelineLayout;
    private final long renderPass;
    private final boolean linearOutput;
    private final boolean reactiveOutput;
    private final boolean restirCapture;
    private final Map<Key, Long> pipelines = new HashMap<>();
    private VulkanPipelineCache pipelineCache;
    private long vertexShader;
    private long fragmentShader;
    private long rayVertexShader;
    private long rayFragmentShader;

    VulkanPipelines(VulkanDevice context, long pipelineLayout, long renderPass, boolean linearOutput) {
        this(context, pipelineLayout, renderPass, linearOutput, false);
    }

    VulkanPipelines(VulkanDevice context, long pipelineLayout, long renderPass, boolean linearOutput, boolean restirCapture) {
        this(context, pipelineLayout, renderPass, linearOutput, restirCapture, linearOutput && !restirCapture);
    }

    VulkanPipelines(VulkanDevice context, long pipelineLayout, long renderPass, boolean linearOutput,
                    boolean restirCapture, boolean reactiveOutput) {
        this.context = context;
        this.pipelineLayout = pipelineLayout;
        this.renderPass = renderPass;
        this.linearOutput = linearOutput;
        this.reactiveOutput = reactiveOutput;
        this.restirCapture = restirCapture;
        try {
            this.vertexShader = loadShader("terrain.vert");
            this.fragmentShader = loadShader(reactiveOutput ? "terrain_hdr.frag" : "terrain.frag");
            if (context.rayTracingSupported()) {
                this.rayVertexShader = loadShader("terrain_ray.vert");
                this.rayFragmentShader = loadShader(restirCapture ? "terrain_restir.frag" : reactiveOutput ? "terrain_ray_hdr.frag" : "terrain_ray.frag");
            }
            this.pipelineCache = new VulkanPipelineCache(context);
            if (!restirCapture) prewarmPipelines();
        }
        catch (RuntimeException | Error error) {
            close();
            throw error;
        }
    }

    int size() {
        return this.pipelines.size();
    }

    long get(Key key) {
        return this.pipelines.computeIfAbsent(normalize(key), this::createPipeline);
    }

    static Key key(int topology, RasterState raster, Variant variant) {
        return normalize(new Key(
            topology,
            raster.depthTest(), raster.depthWrite(), compareOp(raster.depthCompare()),
            raster.blend().enabled(), blendFactor(raster.blend().source()), blendFactor(raster.blend().destination()),
            cullMode(raster.cull()),
            raster.winding() == Winding.CLOCKWISE ? VK_FRONT_FACE_CLOCKWISE : VK_FRONT_FACE_COUNTER_CLOCKWISE,
            raster.colorMask().bits(), raster.depthBias().enabled(), variant));
    }

    record Key(int topology, boolean depthTest, boolean depthWrite, int depthFunc,
               boolean blend, int blendSrc, int blendDst, int cullMode, int frontFace,
               int colorMask, boolean depthBias, Variant variant) { }

    enum Variant {
        UNLIT(false, false),
        UNLIT_FOG(false, true),
        LIT(true, false),
        LIT_FOG(true, true),
        RAY(false, false, true),
        RAY_FOG(false, true, true);

        final boolean lighting;
        final boolean fog;
        final boolean ray;

        Variant(boolean lighting, boolean fog) {
            this(lighting, fog, false);
        }

        Variant(boolean lighting, boolean fog, boolean ray) {
            this.lighting = lighting;
            this.fog = fog;
            this.ray = ray;
        }

        static Variant of(boolean lighting, boolean fog) {
            if (lighting) {
                return fog ? LIT_FOG : LIT;
            }
            return fog ? UNLIT_FOG : UNLIT;
        }

        Variant traced() { return fog ? RAY_FOG : RAY; }
    }

    private static int compareOp(CompareOp value) {
        return switch (value) {
            case NEVER -> VK_COMPARE_OP_NEVER;
            case LESS -> VK_COMPARE_OP_LESS;
            case EQUAL -> VK_COMPARE_OP_EQUAL;
            case LESS_OR_EQUAL -> VK_COMPARE_OP_LESS_OR_EQUAL;
            case GREATER -> VK_COMPARE_OP_GREATER;
            case NOT_EQUAL -> VK_COMPARE_OP_NOT_EQUAL;
            case GREATER_OR_EQUAL -> VK_COMPARE_OP_GREATER_OR_EQUAL;
            case ALWAYS -> VK_COMPARE_OP_ALWAYS;
        };
    }

    private static int cullMode(CullMode value) {
        return switch (value) {
            case NONE -> VK_CULL_MODE_NONE;
            case FRONT -> VK_CULL_MODE_FRONT_BIT;
            case BACK -> VK_CULL_MODE_BACK_BIT;
            case FRONT_AND_BACK -> VK_CULL_MODE_FRONT_AND_BACK;
        };
    }

    private static int blendFactor(BlendFactor value) {
        return switch (value) {
            case ZERO -> VK_BLEND_FACTOR_ZERO;
            case ONE -> VK_BLEND_FACTOR_ONE;
            case SRC_COLOR -> VK_BLEND_FACTOR_SRC_COLOR;
            case ONE_MINUS_SRC_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR;
            case DST_COLOR -> VK_BLEND_FACTOR_DST_COLOR;
            case ONE_MINUS_DST_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR;
            case SRC_ALPHA -> VK_BLEND_FACTOR_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA -> VK_BLEND_FACTOR_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA;
            case CONSTANT_COLOR -> VK_BLEND_FACTOR_CONSTANT_COLOR;
            case ONE_MINUS_CONSTANT_COLOR -> VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR;
            case CONSTANT_ALPHA -> VK_BLEND_FACTOR_CONSTANT_ALPHA;
            case ONE_MINUS_CONSTANT_ALPHA -> VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_ALPHA;
            case SRC_ALPHA_SATURATE -> VK_BLEND_FACTOR_SRC_ALPHA_SATURATE;
        };
    }

    private long loadShader(String name) {
        String path = "/assets/adventurecraft/shaders/vulkan/" + name + ".spv";
        try (InputStream stream = VulkanPipelines.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing compiled Vulkan shader " + path);
            }
            byte[] bytes = stream.readAllBytes();
            if (bytes.length == 0 || bytes.length % 4 != 0) {
                throw new IllegalStateException("Invalid SPIR-V shader " + path);
            }
            ByteBuffer code = MemoryUtil.memAlloc(bytes.length).order(ByteOrder.nativeOrder());
            try (var stack = MemoryStack.stackPush()) {
                code.put(bytes).flip();
                var info = VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(code);
                var handle = stack.mallocLong(1);
                check(vkCreateShaderModule(this.context.device(), info, null, handle), "create shader " + name);
                return handle.get(0);
            }
            finally {
                MemoryUtil.memFree(code);
            }
        }
        catch (IOException error) {
            throw new IllegalStateException("Cannot read shader " + path, error);
        }
    }

    static Key normalize(Key key) {
        boolean blend = key.blend && key.colorMask != 0;
        return new Key(
            key.topology,
            key.depthTest, key.depthTest && key.depthWrite,
            key.depthTest ? key.depthFunc : VK_COMPARE_OP_ALWAYS,
            blend,
            blend ? key.blendSrc : VK_BLEND_FACTOR_ONE,
            blend ? key.blendDst : VK_BLEND_FACTOR_ZERO,
            key.cullMode,
            key.cullMode == VK_CULL_MODE_NONE ? VK_FRONT_FACE_COUNTER_CLOCKWISE : key.frontFace,
            key.colorMask, key.depthBias, key.variant);
    }

    private void prewarmPipelines() {
        // Compile actual named native policies, including fogged and lit world variants,
        // during renderer loading instead of their first visible draw.
        for (MaterialRecipe recipe : MaterialRecipe.values()) {
            int topology = recipe == MaterialRecipe.DEBUG_LINES
                ? VK_PRIMITIVE_TOPOLOGY_LINE_LIST : VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
            RasterState raster = recipe.raster(RasterState.defaults());
            get(key(topology, raster, Variant.of(recipe.material().lighting(), false)));
            if (recipe.supportsFog()) {
                get(key(topology, raster, Variant.of(recipe.material().lighting(), true)));
            }
            if (recipe == MaterialRecipe.TERRAIN_TRANSLUCENT) {
                // The inherited two-pass water/cloud paths retain depth-writing and depth-only variants.
                for (boolean fog : new boolean[]{false, true}) {
                    var variant = Variant.of(false, fog);
                    get(key(topology, raster.withDepth(true, true), variant));
                    get(key(topology, raster.withDepth(true, true).withColorMask(ColorMask.NONE), variant));
                }
            }
        }
        // Preserve common compatibility combinations without expanding every possible state.
        for (boolean depth : new boolean[]{false, true}) {
            for (boolean blend : new boolean[]{false, true}) {
                for (int cull : new int[]{VK_CULL_MODE_NONE, VK_CULL_MODE_BACK_BIT}) {
                    get(new Key(
                        VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, depth, depth && !blend, VK_COMPARE_OP_LESS_OR_EQUAL,
                        blend, VK_BLEND_FACTOR_SRC_ALPHA, VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA,
                        cull, VK_FRONT_FACE_COUNTER_CLOCKWISE, ColorMask.RGBA.bits(), false, Variant.UNLIT));
                }
            }
        }
    }

    private VkPipelineShaderStageCreateInfo.Buffer shaderStages(Key key, MemoryStack stack) {
        var stages = VkPipelineShaderStageCreateInfo.calloc(2, stack);
        stages.get(0).sType$Default().stage(VK_SHADER_STAGE_VERTEX_BIT)
            .module(key.variant.ray ? this.rayVertexShader : this.vertexShader).pName(stack.UTF8("main"));
        stages.get(1).sType$Default().stage(VK_SHADER_STAGE_FRAGMENT_BIT)
            .module(key.variant.ray ? this.rayFragmentShader : this.fragmentShader).pName(stack.UTF8("main"));
        var specialization = specialization(key, stack);
        stages.get(0).pSpecializationInfo(specialization);
        stages.get(1).pSpecializationInfo(specialization);
        return stages;
    }

    private VkSpecializationInfo specialization(Key key, MemoryStack stack) {
        int count = 6;
        var mappings = VkSpecializationMapEntry.calloc(count, stack);
        mappings.get(0).constantID(LIGHTING_SPECIALIZATION_ID).offset(0).size(Integer.BYTES);
        mappings.get(1).constantID(FOG_SPECIALIZATION_ID).offset(Integer.BYTES).size(Integer.BYTES);
        var data = stack.malloc(count * Integer.BYTES)
            .putInt(0, key.variant.lighting ? 1 : 0)
            .putInt(Integer.BYTES, key.variant.fog ? 1 : 0);
        mappings.get(2).constantID(REFLECTION_SPECIALIZATION_ID)
            .offset(2 * Integer.BYTES).size(Integer.BYTES);
        data.putInt(2 * Integer.BYTES, key.blend ? 1 : 0);
        mappings.get(3).constantID(WRITE_COLOR_SPECIALIZATION_ID)
            .offset(3 * Integer.BYTES).size(Integer.BYTES);
        data.putInt(3 * Integer.BYTES, key.colorMask != 0 ? 1 : 0);
        mappings.get(4).constantID(LINEAR_OUTPUT_SPECIALIZATION_ID).offset(4 * Integer.BYTES).size(Integer.BYTES);
        data.putInt(4 * Integer.BYTES, linearOutput ? 1 : 0);
        mappings.get(5).constantID(5).offset(5 * Integer.BYTES).size(Integer.BYTES);
        data.putInt(5 * Integer.BYTES, restirCapture ? 1 : 0);
        return VkSpecializationInfo.calloc(stack).pMapEntries(mappings).pData(data);
    }

    private static VkPipelineVertexInputStateCreateInfo vertexInput(MemoryStack stack, boolean ray) {
        var binding = VkVertexInputBindingDescription.calloc(1, stack)
            .binding(0).stride(VertexFormat.STRIDE).inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        var attributes = VkVertexInputAttributeDescription.calloc(4, stack);
        attributes.get(0).location(0).binding(0).format(VK_FORMAT_R32G32B32_SFLOAT).offset(VertexFormat.POSITION_OFFSET);
        attributes.get(1).location(1).binding(0).format(VK_FORMAT_R32G32_SFLOAT).offset(VertexFormat.UV_OFFSET);
        attributes.get(2).location(2).binding(0).format(VK_FORMAT_R8G8B8A8_UNORM).offset(VertexFormat.COLOR_OFFSET);
        if (ray) attributes.get(3).location(4).binding(0).format(VK_FORMAT_R32_UINT).offset(VertexFormat.MATERIAL_OFFSET);
        else attributes.get(3).location(3).binding(0).format(VK_FORMAT_R8G8B8A8_SNORM).offset(VertexFormat.NORMAL_OFFSET);
        return VkPipelineVertexInputStateCreateInfo.calloc(stack)
            .sType$Default().pVertexBindingDescriptions(binding).pVertexAttributeDescriptions(attributes);
    }

    private long createPipeline(Key key) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var stages = shaderStages(key, stack);
            var vertex = vertexInput(stack, key.variant.ray);
            var assembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType$Default().topology(key.topology);
            var viewport = VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default().viewportCount(1).scissorCount(1);
            var raster = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType$Default().polygonMode(VK_POLYGON_MODE_FILL).lineWidth(1)
                .cullMode(key.cullMode).frontFace(key.frontFace).depthBiasEnable(key.depthBias);
            var multisample = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType$Default().rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);
            var depthState = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                .sType$Default().depthTestEnable(key.depthTest).depthWriteEnable(key.depthWrite)
                .depthCompareOp(key.depthFunc).minDepthBounds(0).maxDepthBounds(1);
            var blendAttachment = VkPipelineColorBlendAttachmentState.calloc(restirCapture ? 4 : reactiveOutput ? 2 : 1, stack);
            for (int i = 0; i < blendAttachment.capacity(); i++) blendAttachment.get(i)
                .blendEnable(!restirCapture && key.blend)
                .srcColorBlendFactor(key.blendSrc).dstColorBlendFactor(key.blendDst)
                .colorBlendOp(VK_BLEND_OP_ADD)
                .srcAlphaBlendFactor(key.blendSrc).dstAlphaBlendFactor(key.blendDst)
                .alphaBlendOp(VK_BLEND_OP_ADD)
                .colorWriteMask(restirCapture ? ColorMask.RGBA.bits() : key.colorMask);
            // Opaque geometry replaces the sky mask; translucent geometry preserves reactive coverage.
            if (reactiveOutput && context.independentBlendSupported()) {
                blendAttachment.get(1).blendEnable(key.blend)
                    .srcColorBlendFactor(VK_BLEND_FACTOR_ONE).dstColorBlendFactor(VK_BLEND_FACTOR_ONE)
                    .colorBlendOp(VK_BLEND_OP_MAX)
                    .srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE).dstAlphaBlendFactor(VK_BLEND_FACTOR_ONE)
                    .alphaBlendOp(VK_BLEND_OP_MAX)
                    .colorWriteMask(key.colorMask == 0 ? 0 : ColorMask.RGBA.bits());
            }
            var blendState = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType$Default().pAttachments(blendAttachment);
            var dynamic = VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType$Default().pDynamicStates(stack.ints(
                    VK_DYNAMIC_STATE_VIEWPORT, VK_DYNAMIC_STATE_SCISSOR, VK_DYNAMIC_STATE_LINE_WIDTH,
                    VK_DYNAMIC_STATE_DEPTH_BIAS, VK_DYNAMIC_STATE_BLEND_CONSTANTS));
            var info = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                .sType$Default().pStages(stages).pVertexInputState(vertex).pInputAssemblyState(assembly)
                .pViewportState(viewport).pRasterizationState(raster).pMultisampleState(multisample)
                .pDepthStencilState(depthState).pColorBlendState(blendState).pDynamicState(dynamic)
                .layout(this.pipelineLayout).renderPass(this.renderPass).subpass(0).basePipelineIndex(-1);
            LongBuffer result = stack.callocLong(1);
            int status = vkCreateGraphicsPipelines(this.context.device(), this.pipelineCache.handle(), info, null, result);
            if (status != VK_SUCCESS) {
                if (result.get(0) != VK_NULL_HANDLE) {
                    vkDestroyPipeline(this.context.device(), result.get(0), null);
                }
                check(status, "create terrain pipeline");
            }
            return result.get(0);
        }
    }

    @Override
    public void close() {
        for (long pipeline : this.pipelines.values()) {
            vkDestroyPipeline(this.context.device(), pipeline, null);
        }
        this.pipelines.clear();
        if (this.vertexShader != VK_NULL_HANDLE) {
            vkDestroyShaderModule(this.context.device(), this.vertexShader, null);
        }
        if (this.fragmentShader != VK_NULL_HANDLE) {
            vkDestroyShaderModule(this.context.device(), this.fragmentShader, null);
        }
        this.vertexShader = VK_NULL_HANDLE;
        this.fragmentShader = VK_NULL_HANDLE;
        if (this.rayVertexShader != VK_NULL_HANDLE) vkDestroyShaderModule(this.context.device(), this.rayVertexShader, null);
        if (this.rayFragmentShader != VK_NULL_HANDLE) vkDestroyShaderModule(this.context.device(), this.rayFragmentShader, null);
        this.rayVertexShader = VK_NULL_HANDLE;
        this.rayFragmentShader = VK_NULL_HANDLE;
        if (this.pipelineCache != null) {
            this.pipelineCache.close();
            this.pipelineCache = null;
        }
    }
}
