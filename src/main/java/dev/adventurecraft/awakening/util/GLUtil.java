package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.layout.Rect;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.glu.GLU;

public final class GLUtil {

    public static Rect projectModelViewProj(Rect rect) {
        try (var stack = MemoryStack.stackPush()) {
            var viewport = stack.mallocInt(16);
            var modelViewMatrix = stack.mallocFloat(16);
            var projMatrix = stack.mallocFloat(16);
            var objPos = stack.mallocFloat(3);

            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

            GLU.gluProject(
                (float) rect.left(), (float) rect.top(), 1.0F, modelViewMatrix, projMatrix, viewport, objPos);
            double x0 = objPos.get(0);
            double y0 = objPos.get(1);

            GLU.gluProject(
                (float) rect.right(), (float) rect.bot(), 1.0F, modelViewMatrix, projMatrix, viewport, objPos);
            double x1 = objPos.get(0);
            double y1 = objPos.get(1);

            return Rect.fromEdges(x0, y0, x1, y1);
        }
    }
}
