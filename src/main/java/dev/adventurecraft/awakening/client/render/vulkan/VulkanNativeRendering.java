package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.NativeRenderingOptions;
import dev.adventurecraft.awakening.client.render.state.Matrix4;
import dev.adventurecraft.awakening.client.render.state.Vector4;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.vulkan.VK10.*;

/** Coordinates optional SDK dispatches without transferring ownership of renderer resources. */
final class VulkanNativeRendering implements AutoCloseable {
    static final class NativeRecordingFailure extends RuntimeException {
        NativeRecordingFailure(String message, Throwable cause) { super(message, cause); }
    }
    record Plan(int reconstruction, int algorithm, int generation) {
        static final Plan NONE = new Plan(0, 0, 0);
        boolean active() { return reconstruction != 0 || algorithm != 0 || generation != 0; }
        boolean temporal() { return algorithm != 0; }
        boolean exportLobes() { return reconstruction != 0; }
    }
    record Selection(int reconstruction, int upscaler, int generation) { }
    private static final class Frame {
        final ByteBuffer parameters = MemoryUtil.memAlloc(320).order(ByteOrder.nativeOrder());
        VulkanImage output;
        VulkanImage hudless;
        VulkanImage depth;
        VulkanImage linearInput;
        Plan plan = Plan.NONE;
        long time;
        long generated;
        long real;
        boolean outputFresh = true, hudlessFresh = true, depthFresh = true;
        boolean linearFresh = true;
        boolean outputRecorded, hudlessRecorded, depthRecorded, guidesRecorded;
        boolean denoised, reconstructed, generationRecorded;
        boolean failed;
        boolean hdr;
        boolean classicFsr;
        boolean linearRecorded;
    }
    private final VulkanDevice context;
    private final Frame[] frames;
    private final String[] failures = new String[6];
    private final boolean guideSupport;
    private NativeRenderingBridge bridge;
    private VulkanReconstructionGuides guides;
    private VulkanReconstructionColor colorConversion;
    private String initializationFailure;
    private int reconstructionRequested, upscalerRequested, generationRequested;
    private int effectiveReconstruction, effectiveUpscaler, effectiveGeneration;
    private int renderWidth, renderHeight, outputWidth, outputHeight;
    private long sequence;
    private long previousTime;
    private boolean continuity;
    private boolean previousHdr;

    VulkanNativeRendering(VulkanDevice context, int frameCount) {
        this.context = context;
        guideSupport = VulkanReconstructionGuides.supported(context);
        frames = new Frame[frameCount];
        for (int i = 0; i < frameCount; i++) frames[i] = new Frame();
    }

    void setReconstruction(int mode) { if (reconstructionRequested != mode) { reconstructionRequested = mode; settingsChanged(); } }
    void setUpscaler(int mode) { if (upscalerRequested != mode) { upscalerRequested = mode; settingsChanged(); } }
    void setGeneration(int mode) { if (generationRequested != mode) { generationRequested = mode; settingsChanged(); } }
    Selection selection() { return new Selection(reconstructionRequested, upscalerRequested, generationRequested); }
    Vector4 jitter(long sequence) {
        double ratio = Math.max((double) outputWidth / Math.max(renderWidth, 1), (double) outputHeight / Math.max(renderHeight, 1));
        int phases = Math.clamp((int) Math.ceil(8 * ratio * ratio), 8, 64);
        int index = (int) Math.floorMod(sequence, phases) + 1;
        return new Vector4(halton(index,2)-.5f,halton(index,3)-.5f,0,0);
    }
    private void settingsChanged() {
        java.util.Arrays.fill(failures, null);
        invalidate();
    }

    NativeRenderingOptions.Status status(NativeRenderingOptions.Group group, int mode) {
        if (mode == 0) return NativeRenderingOptions.Status.ready();
        if (mode < 0 || mode > 2) return NativeRenderingOptions.Status.unavailable("Unknown native rendering mode");
        if (!guideSupport) return NativeRenderingOptions.Status.unavailable("Native reconstruction guide formats and device features are unavailable");
        if (group == NativeRenderingOptions.Group.FRAME_GENERATION && context.window() == 0)
            return NativeRenderingOptions.Status.unavailable("Frame generation requires a Vulkan presentation window");
        int feature = feature(group, mode);
        if (failures[feature] != null) return NativeRenderingOptions.Status.unavailable(failures[feature]);
        NativeRenderingBridge runtime = runtime();
        if (runtime == null) return NativeRenderingOptions.Status.unavailable(initializationFailure);
        return runtime.supported(feature) ? NativeRenderingOptions.Status.ready()
            : NativeRenderingOptions.Status.unavailable(runtime.reason(feature));
    }

    int effective(NativeRenderingOptions.Group group) {
        return switch (group) {
            case RECONSTRUCTION -> effectiveReconstruction;
            case UPSCALER -> effectiveUpscaler;
            case FRAME_GENERATION -> effectiveGeneration;
        };
    }

    Plan plan(Selection selection, boolean eligible, boolean linearWorld, boolean tracedWorld) {
        if (!eligible) return Plan.NONE;
        int reconstruction = tracedWorld && linearWorld && status(NativeRenderingOptions.Group.RECONSTRUCTION, selection.reconstruction).available()
            ? selection.reconstruction : 0;
        int algorithm = reconstruction == 1 ? 2 : selection.upscaler != 0
            && status(NativeRenderingOptions.Group.UPSCALER, selection.upscaler).available() ? selection.upscaler == 1 ? 1 : 3 : 0;
        int generation = selection.generation != 0 && status(NativeRenderingOptions.Group.FRAME_GENERATION, selection.generation).available()
            ? selection.generation : 0;
        return new Plan(reconstruction, algorithm, generation);
    }

    void prepare(int slot, Plan plan, int renderWidth, int renderHeight, int outputWidth, int outputHeight, boolean linearWorld) {
        Frame frame = frame(slot);
        frame.plan = plan;
        frame.generated = frame.real = 0;
        frame.outputRecorded = frame.hudlessRecorded = frame.depthRecorded = frame.guidesRecorded = false;
        frame.denoised = frame.reconstructed = frame.generationRecorded = false;
        frame.failed = false;
        frame.linearRecorded = false;
        frame.classicFsr = plan.algorithm == 3 && !linearWorld;
        if (!plan.active()) {
            if (continuity) invalidate();
            // Capability-only UI queries can create a lightweight bridge while all
            // options are off. Retire it only after actual guide allocations existed.
            if (guides != null && reconstructionRequested == 0 && upscalerRequested == 0 && generationRequested == 0) {
                context.waitIdle();
                if (bridge != null) bridge.close();
                bridge = null;
                guides.close(); guides = null;
                if (colorConversion != null) colorConversion.close();
                colorConversion = null;
                closeImages();
                this.renderWidth = this.renderHeight = this.outputWidth = this.outputHeight = 0;
            }
            return;
        }
        if (guides == null) guides = new VulkanReconstructionGuides(context, frames.length);
        if (this.renderWidth != renderWidth || this.renderHeight != renderHeight
            || this.outputWidth != outputWidth || this.outputHeight != outputHeight) {
            context.waitIdle();
            closeImages();
            invalidate();
            guides.resize(renderWidth, renderHeight);
            this.renderWidth = renderWidth; this.renderHeight = renderHeight;
            this.outputWidth = outputWidth; this.outputHeight = outputHeight;
        }
        int transfer = VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        if (plan.temporal() && frame.output == null) frame.output = VulkanImage.create(context, outputWidth, outputHeight,
            VK_FORMAT_R16G16B16A16_SFLOAT, transfer | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT, VK_IMAGE_ASPECT_COLOR_BIT, 1);
        if (frame.classicFsr) {
            if (colorConversion == null) colorConversion = new VulkanReconstructionColor(context, frames.length);
            if (frame.linearInput == null) frame.linearInput = VulkanImage.create(context, renderWidth, renderHeight,
                VK_FORMAT_R16G16B16A16_SFLOAT, transfer | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT, VK_IMAGE_ASPECT_COLOR_BIT, 1);
        }
        if (plan.generation != 0) {
            if (frame.hudless == null) frame.hudless = VulkanImage.create(context, outputWidth, outputHeight,
                VK_FORMAT_R8G8B8A8_UNORM, transfer | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT, VK_IMAGE_ASPECT_COLOR_BIT, 1);
            if (frame.depth == null) frame.depth = VulkanImage.create(context, renderWidth, renderHeight,
                VK_FORMAT_D32_SFLOAT, transfer | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK_IMAGE_ASPECT_DEPTH_BIT, 1);
        }
    }

    void recordGuides(VkCommandBuffer command, int slot, long[] captureViews, Matrix4 jitteredViewProjection,
                      Matrix4 worldToView, Vector4 origin, float jitterX, float jitterY, boolean reset,
                      long directDiffuse, long directSpecular, long indirectDiffuse, boolean hdr) {
        Frame frame = frame(slot);
        if (!frame.plan.active()) return;
        frame.time = System.nanoTime();
        frame.hdr = hdr;
        guides.recordCapture(command, slot, captureViews, jitteredViewProjection, worldToView, origin,
            jitterX, jitterY, reset || !continuity || previousHdr != hdr, directDiffuse, directSpecular, indirectDiffuse);
        writeParameters(frame, guides.camera(slot), hdr, slot);
        frame.guidesRecorded = true;
    }

    boolean denoise(VkCommandBuffer command, int slot, long directResult, long indirectResult) {
        Frame frame = frame(slot);
        if (frame.plan.reconstruction != 2 || !frame.guidesRecorded || directResult == 0 && indirectResult == 0) return false;
        boolean direct = directResult != 0, indirect = indirectResult != 0;
        long[] images = NativeRenderingBridge.images(guides.motion(slot), guides.viewZ(slot), guides.normalRoughness(slot),
            guides.diffuseRadiance(slot), guides.specularRadiance(slot), guides.indirectRadiance(slot),
            guides.denoisedDiffuse(slot), guides.denoisedSpecular(slot), guides.denoisedIndirect(slot));
        try {
            if (!runtime().denoise(command, images, frame.parameters, direct, indirect)) throw failure(NativeRenderingBridge.NRD, null);
        } catch (NativeRecordingFailure error) { throw error; }
        catch (RuntimeException | LinkageError error) { throw failure(NativeRenderingBridge.NRD, error); }
        guides.applyDenoised(command, slot, directResult, indirectResult, direct, indirect);
        frame.denoised = true;
        return true;
    }

    void recordWorldMotion(VkCommandBuffer command, int slot, VulkanImage depth, VulkanImage reactive) {
        Frame frame = frame(slot);
        if (!frame.guidesRecorded) return;
        guides.recordWorldMotion(command, slot, depth, reactive);
        if (frame.plan.generation != 0) {
            copy(command, depth, frame.depth, frame.depthFresh);
            frame.depthRecorded = true;
        }
    }

    /** A successful native resolve replaces custom TAA and spatial enlargement for this frame. */
    VulkanImage reconstruct(VkCommandBuffer command, int slot, VulkanImage color, VulkanImage depth) {
        Frame frame = frame(slot);
        if (!frame.plan.temporal() || !frame.guidesRecorded) return null;
        VulkanImage output = frame.output;
        VulkanImage nativeColor = color;
        if (frame.classicFsr) {
            colorConversion.toLinear(command, slot, color, frame.linearInput, frame.linearFresh);
            frame.linearRecorded = true;
            nativeColor = frame.linearInput;
        }
        VulkanImage.barrier(command, output, frame.outputFresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_IMAGE_LAYOUT_GENERAL, frame.outputFresh ? 0 : VK_ACCESS_MEMORY_READ_BIT, VK_ACCESS_SHADER_WRITE_BIT,
            frame.outputFresh ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        long[] images = NativeRenderingBridge.images(nativeColor, depth, guides.motion(slot), guides.reactive(slot), guides.normalRoughness(slot),
            guides.diffuseAlbedo(slot), guides.specularAlbedo(slot), guides.specularHitDistance(slot), output);
        images[5] = frame.classicFsr ? VK_IMAGE_LAYOUT_GENERAL : VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
        images[11] = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
        int feature = frame.plan.algorithm == 2 ? NativeRenderingBridge.DLSS_RR
            : frame.plan.algorithm == 1 ? NativeRenderingBridge.DLSS_SR : NativeRenderingBridge.FSR_SR;
        try {
            if (!runtime().reconstruct(command, frame.plan.algorithm, images, frame.parameters)) throw failure(feature, null);
        } catch (NativeRecordingFailure error) { throw error; }
        catch (RuntimeException | LinkageError error) { throw failure(feature, error); }
        if (frame.classicFsr) colorConversion.toDisplay(command, slot, output);
        VulkanImage.barrier(command, output, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
            VK_ACCESS_SHADER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        frame.outputRecorded = true;
        frame.reconstructed = true;
        return output;
    }

    void recordHudless(VkCommandBuffer command, int slot, VulkanImage color) {
        Frame frame = frame(slot);
        if (!frame.guidesRecorded || frame.plan.generation == 0) return;
        copy(command, color, frame.hudless, frame.hudlessFresh);
        frame.hudlessRecorded = true;
    }

    void generate(VkCommandBuffer command, int slot, VulkanImage backbuffer) {
        Frame frame = frame(slot);
        if (!frame.hudlessRecorded || !frame.depthRecorded) return;
        long[] images = NativeRenderingBridge.images(backbuffer, frame.hudless, frame.depth, guides.motion(slot));
        images[5] = images[11] = images[17] = VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL;
        // Frame generation consumes the tone-mapped presentation image.
        frame.parameters.putInt(78 * 4, 0);
        int feature = frame.plan.generation == 1 ? NativeRenderingBridge.DLSS_FG : NativeRenderingBridge.FSR_FG;
        long[] pair;
        try {
            pair = runtime().generate(command, frame.plan.generation, slot, images, frame.parameters);
        } catch (RuntimeException | LinkageError error) { throw failure(feature, error); }
        frame.generationRecorded = true;
        if (pair != null && pair.length >= 2 && pair[0] != 0 && pair[1] != 0) {
            frame.generated = pair[0]; frame.real = pair[1];
        } else throw failure(feature, null);
    }

    long realImage(int slot, long original) { long real = frame(slot).real; return real != 0 ? real : original; }
    long generatedImage(int slot) { return frame(slot).generated; }
    boolean interpolationAllowed(int slot) { return bridge != null && frame(slot).generated != 0 && bridge.interpolationAllowed(slot); }

    void submitted(int slot) {
        Frame frame = frame(slot);
        if (frame.guidesRecorded) {
            guides.submitted(slot);
            previousTime = frame.time;
            continuity = !frame.failed;
            previousHdr = frame.hdr;
            sequence++;
        }
        if (frame.outputRecorded) frame.outputFresh = false;
        if (frame.hudlessRecorded) frame.hudlessFresh = false;
        if (frame.depthRecorded) frame.depthFresh = false;
        if (frame.linearRecorded) frame.linearFresh = false;
        effectiveReconstruction = frame.denoised ? 2 : frame.reconstructed && frame.plan.algorithm == 2 ? 1 : 0;
        effectiveUpscaler = frame.reconstructed ? frame.plan.algorithm == 3 ? 2 : 1 : 0;
        effectiveGeneration = frame.generated != 0 ? frame.plan.generation : 0;
    }

    void abort(int slot) {
        if (guides != null && frame(slot).guidesRecorded) guides.abort(slot);
        frame(slot).generated = frame(slot).real = 0;
        invalidate();
    }

    void invalidate() {
        continuity = false;
        previousTime = 0;
        effectiveReconstruction = effectiveUpscaler = effectiveGeneration = 0;
        if (guides != null) guides.invalidateHistory();
        if (bridge != null) bridge.abort();
    }
    void disable(String reason) { java.util.Arrays.fill(failures, reason); invalidate(); }

    private void writeParameters(Frame frame, VulkanReconstructionGuides.Camera camera, boolean hdr, int slot) {
        Matrix4 projection = camera.viewProjection().multiply(VulkanTemporalAa.inverse(camera.worldToView()));
        Matrix4 previousProjection = camera.previousViewProjection().multiply(VulkanTemporalAa.inverse(camera.previousWorldToView()));
        float near = Math.abs(projection.get(14) / projection.get(10));
        float far = Math.abs(projection.get(14) / (projection.get(10) + 1));
        if (!Float.isFinite(near) || near <= 0) near = .05f;
        if (!Float.isFinite(far) || far <= near) far = 10000;
        float fov = (float) (2 * Math.atan(1 / Math.max(Math.abs(projection.get(5)), 1e-6f)));
        ByteBuffer bytes = frame.parameters.clear();
        matrix(bytes, camera.worldToView()); matrix(bytes, camera.previousWorldToView()); matrix(bytes, projection); matrix(bytes, previousProjection);
        bytes.putFloat(camera.jitterPixelsX()).putFloat(camera.jitterPixelsY()).putFloat(camera.previousJitterPixelsX()).putFloat(camera.previousJitterPixelsY())
            .putFloat(previousTime == 0 ? 16.6667f : Math.clamp((frame.time - previousTime) * 1e-6f, .01f, 1000))
            .putFloat(near).putFloat(far).putFloat(fov);
        bytes.putInt(renderWidth).putInt(renderHeight).putInt(outputWidth).putInt(outputHeight)
            .putInt((int) sequence).putInt(camera.reset() ? 1 : 0).putInt(hdr ? 1 : 0).putInt(slot).flip();
    }

    private NativeRenderingBridge runtime() {
        if (bridge == null && initializationFailure == null) {
            try { bridge = new NativeRenderingBridge(context); }
            catch (RuntimeException | LinkageError error) { initializationFailure = "Native rendering initialization failed: " + error.getMessage(); }
        }
        return bridge;
    }
    private NativeRecordingFailure failure(int feature, Throwable cause) {
        String reason;
        try { reason = bridge == null ? initializationFailure : bridge.reason(feature); }
        catch (RuntimeException | LinkageError ignored) { reason = null; }
        failures[feature] = "Native evaluation failed; using the built-in renderer" + (reason == null || reason.isBlank() ? "" : ": " + reason);
        continuity = false;
        // The caller must discard this entire command recording before retiring
        // SDK objects. A failed dispatch may already have referenced them.
        return new NativeRecordingFailure(failures[feature], cause);
    }
    private static int feature(NativeRenderingOptions.Group group, int mode) {
        return switch (group) {
            case RECONSTRUCTION -> mode == 1 ? NativeRenderingBridge.DLSS_RR : NativeRenderingBridge.NRD;
            case UPSCALER -> mode == 1 ? NativeRenderingBridge.DLSS_SR : NativeRenderingBridge.FSR_SR;
            case FRAME_GENERATION -> mode == 1 ? NativeRenderingBridge.DLSS_FG : NativeRenderingBridge.FSR_FG;
        };
    }
    private static void matrix(ByteBuffer bytes, Matrix4 matrix) { for (float value : matrix.copyArray()) bytes.putFloat(value); }
    private static float halton(int index, int base) {
        float sum = 0, weight = 1;
        for (; index > 0; index /= base) { weight /= base; sum += weight * (index % base); }
        return sum;
    }
    private static void copy(VkCommandBuffer command, VulkanImage source, VulkanImage target, boolean fresh) {
        try (var stack = MemoryStack.stackPush()) {
            VulkanImage.barrier(command, target, fresh ? VK_IMAGE_LAYOUT_UNDEFINED : VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, fresh ? 0 : VK_ACCESS_MEMORY_READ_BIT, VK_ACCESS_TRANSFER_WRITE_BIT,
                fresh ? VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT : VK_PIPELINE_STAGE_ALL_COMMANDS_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
            var region = VkImageCopy.calloc(1, stack);
            region.srcSubresource().aspectMask(source.aspect).layerCount(1);
            region.dstSubresource().aspectMask(target.aspect).layerCount(1);
            region.extent().set(source.width, source.height, 1);
            vkCmdCopyImage(command, source.handle, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, target.handle, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);
            VulkanImage.barrier(command, target, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_ACCESS_TRANSFER_WRITE_BIT, VK_ACCESS_TRANSFER_READ_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT);
        }
    }
    private Frame frame(int slot) { if (slot < 0 || slot >= frames.length) throw new IndexOutOfBoundsException(slot); return frames[slot]; }
    private void closeImages() {
        for (Frame frame : frames) {
            if (frame.output != null) frame.output.close(); if (frame.hudless != null) frame.hudless.close(); if (frame.depth != null) frame.depth.close();
            if (frame.linearInput != null) frame.linearInput.close();
            frame.linearInput = null;
            frame.output = frame.hudless = frame.depth = null;
            frame.outputFresh = frame.hudlessFresh = frame.depthFresh = true;
            frame.linearFresh = true;
            frame.generated = frame.real = 0;
        }
    }
    @Override public void close() {
        if (bridge != null) bridge.close();
        bridge = null;
        if (guides != null) guides.close();
        guides = null;
        if (colorConversion != null) colorConversion.close();
        colorConversion = null;
        closeImages();
        for (Frame frame : frames) MemoryUtil.memFree(frame.parameters);
    }
}
