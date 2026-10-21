package dev.adventurecraft.awakening.client.render.compat;

import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** The only legacy primitive-number decoder. Native geometry uses final topology and indices. */
public final class LegacyPrimitiveConverter {
    private LegacyPrimitiveConverter() { }
    public static VulkanRenderer.Mesh upload(VulkanRenderer renderer, ByteBuffer vertices, int mode) { return convert(renderer, vertices, mode, false); }
    public static VulkanRenderer.Mesh stream(VulkanRenderer renderer, ByteBuffer vertices, int mode) { return convert(renderer, vertices, mode, true); }
    private static VulkanRenderer.Mesh convert(VulkanRenderer renderer, ByteBuffer vertices, int mode, boolean stream) {
        if (mode == 7) return stream ? renderer.streamQuads(vertices) : renderer.uploadQuads(vertices);
        var topology = switch(mode) {
            case 0 -> VulkanRenderer.PrimitiveTopology.POINTS;
            case 1,2,3 -> VulkanRenderer.PrimitiveTopology.LINES;
            case 4,5,6,8,9 -> VulkanRenderer.PrimitiveTopology.TRIANGLES;
            default -> throw new IllegalArgumentException("Unsupported legacy primitive " + mode);
        };
        ByteBuffer indices = null;
        try {
            if (mode != 0 && mode != 1 && mode != 4) indices = indices(vertices.remaining() / VulkanRenderer.VERTEX_STRIDE, mode);
            return stream ? renderer.streamMesh(vertices, indices, topology) : renderer.uploadMesh(vertices, indices, topology);
        } finally { if (indices != null) MemoryUtil.memFree(indices); }
    }
    private static ByteBuffer indices(int count, int mode) {
        int max = switch(mode) {
            case 2,3 -> Math.multiplyExact(count,2);
            case 5,6,9 -> Math.multiplyExact(Math.max(0,count-2),3);
            case 8 -> Math.multiplyExact(Math.max(0,count/2-1),6);
            default -> throw new IllegalArgumentException("Unsupported primitive");
        };
        ByteBuffer out = MemoryUtil.memAlloc(Math.multiplyExact(max,4)).order(ByteOrder.nativeOrder());
        switch(mode) {
            case 2,3 -> { for(int i=0;i+1<count;i++) out.putInt(i).putInt(i+1); if(mode==2 && count>1) out.putInt(count-1).putInt(0); }
            case 5 -> { for(int i=0;i+2<count;i++) out.putInt(i+(i&1)).putInt(i+1-(i&1)).putInt(i+2); }
            case 6,9 -> { for(int i=1;i+1<count;i++) out.putInt(0).putInt(i).putInt(i+1); }
            case 8 -> { for(int i=0;i+3<count;i+=2) out.putInt(i).putInt(i+1).putInt(i+3).putInt(i).putInt(i+3).putInt(i+2); }
        }
        return out.flip();
    }
}
