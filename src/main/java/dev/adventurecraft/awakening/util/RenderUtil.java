package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.layout.Rect;
import org.lwjgl.util.vector.Matrix4f;

public final class RenderUtil {

    /** Compatibility entry point; new UI code projects through its explicit UiDrawContext. */
    public static Rect projectModelViewProj(Rect rect) {
        var pixels = LegacyPassBridge.cameraView().projectRect(rect);
        return new Rect(pixels.x(), RenderSystem.height() - pixels.y() - pixels.height(), pixels.width(), pixels.height());
    }

    public static Matrix4f getModelViewMatrix(Matrix4f matrix) {
        var state = LegacyPassBridge.capture();
        return matrix.load(java.nio.FloatBuffer.wrap(state.view().view().multiply(state.object().model()).copyArray()));
    }

    /** Compatibility delegate; native vertex writers depend only on VertexPacking. */
    public static int packByteNormal(float x, float y, float z) {
        return dev.adventurecraft.awakening.client.render.VertexPacking.packByteNormal(x,y,z);
    }
}
