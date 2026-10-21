package dev.adventurecraft.awakening.client.render.compat;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;



/** Inherited GLU projection conventions and state mutation retained for original Minecraft entry points. */
public final class LegacyProjectionMath {
    private LegacyProjectionMath() { }

    private static final float[] IDENTITY_MATRIX = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };

    /**
     * Make matrix an identity matrix
     */
    private static void makeIdentity(FloatBuffer m) {
        int oldPos = m.position();
        m.put(IDENTITY_MATRIX);
        m.position(oldPos);
    }

    /**
     * Method multiplyVector
     *
     * @param finalMatrix
     * @param in
     * @param out
     */
    private static void multiplyVector(FloatBuffer finalMatrix, float[] in, float[] out) {
        for (int i = 0; i < 4; i++) {
            out[i] = in[0] * finalMatrix.get(finalMatrix.position() + i)
                    + in[1] * finalMatrix.get(finalMatrix.position() + 4 + i)
                    + in[2] * finalMatrix.get(finalMatrix.position() + 2 * 4 + i)
                    + in[3] * finalMatrix.get(finalMatrix.position() + 3 * 4 + i);
        }
    }

    /**
     * @param src
     * @param inverse
     * @return true if the matrix was successfully inverted
     */
    private static boolean invertMatrix(FloatBuffer src, FloatBuffer inverse) {
        int i, j, k, swap;
        float t;
        float[] temp = new float[16];

        for (i = 0; i < 16; i++) {
            temp[i] = src.get(i + src.position());
        }
        makeIdentity(inverse);

        for (i = 0; i < 4; i++) {
            // Look for largest element in column
            swap = i;
            for (j = i + 1; j < 4; j++) {
                if (Math.abs(temp[j * 4 + i]) > Math.abs(temp[i * 4 + i])) {
                    swap = j;
                }
            }

            if (swap != i) {
                // Swap rows.
                for (k = 0; k < 4; k++) {
                    t = temp[i * 4 + k];
                    temp[i * 4 + k] = temp[swap * 4 + k];
                    temp[swap * 4 + k] = t;

                    t = inverse.get(i * 4 + k);
                    inverse.put(i * 4 + k, inverse.get(swap * 4 + k));
                    inverse.put(swap * 4 + k, t);
                }
            }

            if (temp[i * 4 + i] == 0) {
                // No non-zero pivot. The matrix is singular, which shouldn't
                // happen. This means the user gave us a bad matrix.
                return false;
            }

            t = temp[i * 4 + i];
            for (k = 0; k < 4; k++) {
                temp[i * 4 + k] = temp[i * 4 + k] / t;
                inverse.put(i * 4 + k, inverse.get(i * 4 + k) / t);
            }
            for (j = 0; j < 4; j++) {
                if (j != i) {
                    t = temp[j * 4 + i];
                    for (k = 0; k < 4; k++) {
                        temp[j * 4 + k] = temp[j * 4 + k] - temp[i * 4 + k] * t;
                        inverse.put(j * 4 + k, inverse.get(j * 4 + k) - inverse.get(i * 4 + k) * t);
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param a
     * @param b
     * @param r
     */
    private static void multiplyMatrices(FloatBuffer a, FloatBuffer b, FloatBuffer r) {
        for (int i = 0; i < 4; i++) {
            float a0 = a.get(a.position() + i * 4 + 0);
            float a1 = a.get(a.position() + i * 4 + 1);
            float a2 = a.get(a.position() + i * 4 + 2);
            float a3 = a.get(a.position() + i * 4 + 3);

            for (int j = 0; j < 4; j++) {
                float b0 = b.get(b.position() + 0 * 4 + j);
                float b1 = b.get(b.position() + 1 * 4 + j);
                float b2 = b.get(b.position() + 2 * 4 + j);
                float b3 = b.get(b.position() + 3 * 4 + j);

                float v = a0 * b0 + a1 * b1 + a2 * b2 + a3 * b3;
                r.put(r.position() + i * 4 + j, v);
            }
        }
    }

    /**
     * Method perspective.
     *
     * @param fovy
     * @param aspect
     * @param zNear
     * @param zFar
     */
    public static void perspective(float fovy, float aspect, float zNear, float zFar) {
        float sine, cotangent, deltaZ;
        float radians = (float) (fovy / 2 * Math.PI / 180);

        deltaZ = zFar - zNear;
        sine = (float) Math.sin(radians);

        if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
            return;
        }

        cotangent = (float) Math.cos(radians) / sine;

        var matrix = FloatBuffer.allocate(16);
        makeIdentity(matrix);

        matrix.put(0 * 4 + 0, cotangent / aspect);
        matrix.put(1 * 4 + 1, cotangent);
        matrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ);
        matrix.put(2 * 4 + 3, -1);
        matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
        matrix.put(3 * 4 + 3, 0);

        LegacyGlAdapter.multMatrixf(matrix);
    }

    /**
     * Method lookAt
     *
     * @param eyex
     * @param eyey
     * @param eyez
     * @param centerx
     * @param centery
     * @param centerz
     * @param upx
     * @param upy
     * @param upz
     */
    public static void lookAt(
            float eyex,
            float eyey,
            float eyez,
            float centerx,
            float centery,
            float centerz,
            float upx,
            float upy,
            float upz) {
        float[] forward = new float[3];
        float[] side = new float[3];
        float[] up = new float[3];

        forward[0] = centerx - eyex;
        forward[1] = centery - eyey;
        forward[2] = centerz - eyez;

        up[0] = upx;
        up[1] = upy;
        up[2] = upz;

        normalize(forward);

        /* Side = forward x up */
        cross(forward, up, side);
        normalize(side);

        /* Recompute up as: up = side x forward */
        cross(side, forward, up);

        var matrix = FloatBuffer.allocate(16);

        makeIdentity(matrix);
        matrix.put(0 * 4 + 0, side[0]);
        matrix.put(1 * 4 + 0, side[1]);
        matrix.put(2 * 4 + 0, side[2]);

        matrix.put(0 * 4 + 1, up[0]);
        matrix.put(1 * 4 + 1, up[1]);
        matrix.put(2 * 4 + 1, up[2]);

        matrix.put(0 * 4 + 2, -forward[0]);
        matrix.put(1 * 4 + 2, -forward[1]);
        matrix.put(2 * 4 + 2, -forward[2]);

        LegacyGlAdapter.multMatrixf(matrix);
        LegacyGlAdapter.translatef(-eyex, -eyey, -eyez);
    }

    /**
     * Method project
     *
     * @param objx
     * @param objy
     * @param objz
     * @param modelMatrix
     * @param projMatrix
     * @param viewport
     * @param win_pos
     */
    public static boolean project(
            float objx,
            float objy,
            float objz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer win_pos) {

        float[] in = new float[4];
        float[] out = new float[4];

        in[0] = objx;
        in[1] = objy;
        in[2] = objz;
        in[3] = 1.0f;

        multiplyVector(modelMatrix, in, out);
        multiplyVector(projMatrix, out, in);

        if (in[3] == 0.0)
            return false;

        in[3] = (1.0f / in[3]) * 0.5f;

        // Map x, y and z to range 0-1
        in[0] = in[0] * in[3] + 0.5f;
        in[1] = in[1] * in[3] + 0.5f;
        in[2] = in[2] * in[3] + 0.5f;

        // Map x,y to viewport
        win_pos.put(0, in[0] * viewport.get(viewport.position() + 2) + viewport.get(viewport.position() + 0));
        win_pos.put(1, in[1] * viewport.get(viewport.position() + 3) + viewport.get(viewport.position() + 1));
        win_pos.put(2, in[2]);

        return true;
    }

    /**
     * Method unProject
     *
     * @param winx
     * @param winy
     * @param winz
     * @param modelMatrix
     * @param projMatrix
     * @param viewport
     * @param obj_pos
     */
    public static boolean unProject(
            float winx,
            float winy,
            float winz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer obj_pos) {
        float[] in = new float[4];
        float[] out = new float[4];

        var finalMatrix = FloatBuffer.allocate(16);
        multiplyMatrices(modelMatrix, projMatrix, finalMatrix);

        if (!invertMatrix(finalMatrix, finalMatrix))
            return false;

        in[0] = winx;
        in[1] = winy;
        in[2] = winz;
        in[3] = 1.0f;

        // Map x and y from window coordinates
        in[0] = (in[0] - viewport.get(viewport.position() + 0)) / viewport.get(viewport.position() + 2);
        in[1] = (in[1] - viewport.get(viewport.position() + 1)) / viewport.get(viewport.position() + 3);

        // Map to range -1 to 1
        in[0] = in[0] * 2 - 1;
        in[1] = in[1] * 2 - 1;
        in[2] = in[2] * 2 - 1;

        multiplyVector(finalMatrix, in, out);

        if (out[3] == 0.0)
            return false;

        out[3] = 1.0f / out[3];

        obj_pos.put(obj_pos.position() + 0, out[0] * out[3]);
        obj_pos.put(obj_pos.position() + 1, out[1] * out[3]);
        obj_pos.put(obj_pos.position() + 2, out[2] * out[3]);

        return true;
    }

    /**
     * Method pickMatrix
     *
     * @param x
     * @param y
     * @param deltaX
     * @param deltaY
     * @param viewport
     */
    public static void pickMatrix(
            float x,
            float y,
            float deltaX,
            float deltaY,
            IntBuffer viewport) {
        if (deltaX <= 0 || deltaY <= 0) {
            return;
        }

        /* Translate and scale the picked region to the entire window */
        LegacyGlAdapter.translatef(
                (viewport.get(viewport.position() + 2) - 2 * (x - viewport.get(viewport.position() + 0))) / deltaX,
                (viewport.get(viewport.position() + 3) - 2 * (y - viewport.get(viewport.position() + 1))) / deltaY,
                0);
        LegacyGlAdapter.scalef(viewport.get(viewport.position() + 2) / deltaX, viewport.get(viewport.position() + 3) / deltaY, 1.0f);
    }
    private static void normalize(float[] vector) {
        float length = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        if (length == 0) return;
        float inverseLength = 1.0f / length;
        vector[0] *= inverseLength;
        vector[1] *= inverseLength;
        vector[2] *= inverseLength;
    }

    private static void cross(float[] left, float[] right, float[] result) {
        result[0] = left[1] * right[2] - left[2] * right[1];
        result[1] = left[2] * right[0] - left[0] * right[2];
        result[2] = left[0] * right[1] - left[1] * right[0];
    }

}
