package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

/** Shared immutable entity geometry; entity positions, lighting and tint remain draw inputs. */
public final class EntityMeshes {
    private static final RetainedMeshCache<String> MESHES = new RetainedMeshCache<>();
    private EntityMeshes() { }
    public static VulkanRenderer.Mesh mesh(String key, Supplier<ByteBuffer> build) {
        return MESHES.quads(key, build);
    }
    public static VulkanRenderer.Mesh arrow(boolean reversedFeatherNormal) {
        return mesh("arrow:" + reversedFeatherNormal, () -> {
            var out = new QuadBuilder(24);
            float normal = reversedFeatherNormal ? -1 : 1;
            out.normal(normal,0,0);
            out.vertexUV(-7,-2,-2,0,5f/32); out.vertexUV(-7,-2,2,5f/32,5f/32);
            out.vertexUV(-7,2,2,5f/32,10f/32); out.vertexUV(-7,2,-2,0,10f/32);
            out.normal(-normal,0,0);
            out.vertexUV(-7,2,-2,0,5f/32); out.vertexUV(-7,2,2,5f/32,5f/32);
            out.vertexUV(-7,-2,2,5f/32,10f/32); out.vertexUV(-7,-2,-2,0,10f/32);
            for (int face=0; face<4; face++) {
                double angle = Math.PI * face / 2, c = Math.cos(angle), s = Math.sin(angle);
                out.normal(0,(float)-s,(float)c);
                out.vertexUV(-8,-2*c,-2*s,0,0); out.vertexUV(8,-2*c,-2*s,.5,0);
                out.vertexUV(8,2*c,2*s,.5,5f/32); out.vertexUV(-8,2*c,2*s,0,5f/32);
            }
            return out.vertices();
        });
    }
}
