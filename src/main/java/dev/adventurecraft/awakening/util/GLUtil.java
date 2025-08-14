package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.layout.Rect;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;

public final class GLUtil {

    public static Rect projectModelViewProj(Rect rect) {
        try (var stack = MemoryStack.stackPush()) {
            var modelViewMatrix = stack.mallocFloat(16);
            var projMatrix = stack.mallocFloat(16);
            var viewport = stack.mallocInt(4);
            var objPos0 = stack.mallocFloat(3);
            var objPos1 = stack.mallocFloat(3);

            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

            float left = (float) rect.left();
            float top = (float) rect.top();
            GLU.gluProject(left, top, 1.0F, modelViewMatrix, projMatrix, viewport, objPos0);

            float right = (float) rect.right();
            float bot = (float) rect.bot();
            GLU.gluProject(right, bot, 1.0F, modelViewMatrix, projMatrix, viewport, objPos1);

            double x0 = objPos0.get(0);
            double y0 = objPos0.get(1);
            double x1 = objPos1.get(0);
            double y1 = objPos1.get(1);
            return Rect.fromEdges(x0, y0, x1, y1);
        }
    }

    public static Matrix4f getModelViewMatrix(Matrix4f matrix) {
        try (var stack = MemoryStack.stackPush()) {
            var matBuf = stack.mallocFloat(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matBuf);
            return matrix.load(matBuf);
        }
    }

    public static int packByteNormal(float x, float y, float z) {
        int bx = floatToByte(x);
        int by = floatToByte(y);
        int bz = floatToByte(z);
        return bx | (by << 8) | (bz << 16);
    }

    private static int floatToByte(float value) {
        // https://www.khronos.org/opengl/wiki/Normalized_Integer#Signed
        // Range depends on GL version... so use the somewhat sane, equal distribution:
        return (int) (MathF.clamp(value, -1f, 1f) * 127f);
    }
}
