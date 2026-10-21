package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import java.nio.ByteBuffer;
import java.util.*;

/** Collects immutable draw inputs, prepares texture revisions, then records one ordered phase. */
final class DrawQueue {
    private final ArrayList<Draw> draws = new ArrayList<>();
    private long submittedDraws, streamedBytes, preparationNanos, recordingNanos;

    void retained(VulkanRenderer.Mesh mesh, RenderTexture texture, DrawParameters parameters, VulkanRenderer.PreparedPass pass, boolean reorderable) {
        var pin = mesh.retain();
        try { this.draws.add(new Draw(pin,null,TextureStore.snapshot(texture),parameters,pass,reorderable)); }
        catch (RuntimeException | Error error) { pin.close(); throw error; }
    }
    void streamed(ByteBuffer vertices,boolean lines,RenderTexture texture,DrawParameters parameters,VulkanRenderer.PreparedPass pass,boolean reorderable) {
        var renderer=RenderSystem.renderer();
        var stream=lines?renderer.streamMeshPacket(vertices,null,VulkanRenderer.PrimitiveTopology.LINES):renderer.streamQuadsPacket(vertices);
        this.streamed(stream,texture,parameters,pass,reorderable);
    }
    void streamed(VulkanRenderer.StreamGeometry stream,RenderTexture texture,DrawParameters parameters,VulkanRenderer.PreparedPass pass,boolean reorderable) {
        var snapshot=TextureStore.snapshot(texture);
        try {
            this.draws.add(new Draw(null,stream,snapshot,parameters,pass,reorderable));
            this.streamedBytes+=stream.vertexBytes();
        } catch(RuntimeException | Error error) { snapshot.close(); throw error; }
    }
    void flush(VulkanRenderer renderer, RenderPhase phase) {
        if (this.draws.isEmpty()) return;
        renderer.drawPhase(phase);
        try {
            long start = System.nanoTime();
            var textures = Collections.newSetFromMap(new IdentityHashMap<TextureStore.Snapshot, Boolean>());
            for (var draw : this.draws) textures.add(draw.texture);
            TextureStore.prepareUploads(textures);
            this.preparationNanos += System.nanoTime() - start;
            if (phase.groupOpaque()) this.groupOpaque();
            start = System.nanoTime();
            for (var draw : this.draws) {
                if (draw.mesh != null) renderer.draw(draw.mesh, draw.texture.nativeTexture(), draw.pass);
                else renderer.draw(draw.stream,draw.texture.nativeTexture(),draw.pass);
            }
            this.recordingNanos += System.nanoTime() - start;
            this.submittedDraws += this.draws.size();
        } finally { this.releaseDraws(); }
    }
    private void groupOpaque() {
        int first = 0;
        while (first < this.draws.size()) {
            if (!this.draws.get(first).opaque()) { first++; continue; }
            int end = first + 1;
            while (end < this.draws.size() && this.draws.get(end).opaque()) end++;
            this.draws.subList(first, end).sort(Comparator.comparingInt((Draw draw) -> draw.parameters.raster().hashCode())
                .thenComparingInt(draw -> System.identityHashCode(draw.texture)));
            first = end;
        }
    }
    void beginFrame() {
        this.releaseDraws();
        this.submittedDraws = this.streamedBytes = this.preparationNanos = this.recordingNanos = 0;
    }
    long draws() { return this.submittedDraws; }
    long bytes() { return this.streamedBytes; }
    long preparationNanos() { return this.preparationNanos; }
    long recordingNanos() { return this.recordingNanos; }
    private void releaseDraws() {
        for (var draw : this.draws) {
            try { draw.texture.close(); }
            finally { if (draw.mesh != null) draw.mesh.close(); }
        }
        this.draws.clear();
    }
    void close() { this.releaseDraws(); }
    private record Draw(VulkanRenderer.Mesh.Pin mesh,VulkanRenderer.StreamGeometry stream,TextureStore.Snapshot texture,
                        DrawParameters parameters, VulkanRenderer.PreparedPass pass, boolean reorderable) {
        boolean opaque() {
            var raster = this.parameters.raster();
            return this.reorderable && !raster.blend().enabled() && raster.depthTest() && raster.depthWrite()
                && raster.depthCompare() == CompareOp.LESS_OR_EQUAL && raster.colorMask().equals(ColorMask.RGBA);
        }
    }
}
