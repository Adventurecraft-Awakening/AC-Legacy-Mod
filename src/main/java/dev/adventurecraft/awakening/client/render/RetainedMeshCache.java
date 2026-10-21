package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Render-thread owner of shared geometry across device and texture reloads. */
final class RetainedMeshCache<K> {
    private final Map<K, VulkanRenderer.Mesh> meshes = new HashMap<>();
    private long rendererGeneration = -1;
    private long resourceGeneration = -1;

    RetainedMeshCache() { NativeRenderer.registerCache(this); }

    VulkanRenderer.Mesh quads(K key, Supplier<ByteBuffer> build) {
        long renderer = NativeRenderer.generation();
        long resources = TextureStore.reloadGeneration();
        if (rendererGeneration != renderer || resourceGeneration != resources) {
            clear();
            rendererGeneration = renderer;
            resourceGeneration = resources;
        }
        return meshes.computeIfAbsent(Objects.requireNonNull(key), ignored -> NativeRenderer.uploadQuads(build.get()));
    }

    /** Mesh.close requests retirement; existing draw-queue pins retain their storage. */
    void clear() {
        for (var mesh : meshes.values()) mesh.close();
        meshes.clear();
    }
}
