package org.lwjgl.util.glu;

import dev.adventurecraft.awakening.client.render.compat.LegacyProjectionMath;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** Binary entry points retained for Minecraft's original camera code. */
public final class GLU {
    private GLU() { }

    public static void gluPerspective(float fovy, float aspect, float zNear, float zFar) {
        LegacyProjectionMath.perspective(fovy, aspect, zNear, zFar);
    }

    public static void gluLookAt(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ,
                                 float upX, float upY, float upZ) {
        LegacyProjectionMath.lookAt(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public static boolean gluProject(float x, float y, float z, FloatBuffer model, FloatBuffer projection,
                                      IntBuffer viewport, FloatBuffer result) {
        return LegacyProjectionMath.project(x, y, z, model, projection, viewport, result);
    }

    public static boolean gluUnProject(float x, float y, float z, FloatBuffer model, FloatBuffer projection,
                                        IntBuffer viewport, FloatBuffer result) {
        return LegacyProjectionMath.unProject(x, y, z, model, projection, viewport, result);
    }

    public static void gluPickMatrix(float x, float y, float width, float height, IntBuffer viewport) {
        LegacyProjectionMath.pickMatrix(x, y, width, height, viewport);
    }

    public static void gluOrtho2D(float left, float right, float bottom, float top) {
        LegacyGlAdapter.ortho(left, right, bottom, top, -1, 1);
    }

    public static String gluErrorString(int error) {
        return switch (error) {
            case 0 -> "No error";
            case 1280 -> "Invalid enum";
            case 1281 -> "Invalid value";
            case 1282 -> "Invalid operation";
            case 1283 -> "Stack overflow";
            case 1284 -> "Stack underflow";
            case 1285 -> "Out of memory";
            case 1286 -> "Invalid framebuffer operation";
            case 100900 -> "Invalid enum (glu)";
            case 100901 -> "Invalid value (glu)";
            case 100902 -> "Out of memory (glu)";
            case 100903 -> "Incompatible version (glu)";
            default -> null;
        };
    }
}
