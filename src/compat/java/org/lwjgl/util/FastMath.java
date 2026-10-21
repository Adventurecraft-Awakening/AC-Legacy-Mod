package org.lwjgl.util;

public final class FastMath {

    public static final boolean USE_FMA;

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    public static float mulAdd(float a, float b, float c) {
        if (USE_FMA) {
            return Math.fma(a, b, c);
        } else {
            return a * b + c;
        }
    }

    public static float mulSub(float a, float b, float c) {
        if (USE_FMA) {
            return Math.fma(a, b, -c);
        } else {
            return a * b - c;
        }
    }

    public static float negMulAdd(float a, float b, float c) {
        if (USE_FMA) {
            return Math.fma(-a, b, c);
        } else {
            return -(a * b) + c;
        }
    }

    public static float negMulSub(float a, float b, float c) {
        if (USE_FMA) {
            return Math.fma(-a, b, -c);
        } else {
            return -(a * b) - c;
        }
    }

    static {
        float[] v = new float[1024 * 16];

        final int sampleCount = 1024;
        int fastSamples = 0;
        for (int j = 0; j < sampleCount; j++) {
            v[0] = 2.0f;
            v[1] = 2.0f;
            v[2] = 2.0f;
            var fmaStart = System.nanoTime();
            benchFma(v);
            var fmaDuration = System.nanoTime() - fmaStart;

            v[0] = 2.0f;
            v[1] = 2.0f;
            v[2] = 2.0f;
            var scalarStart = System.nanoTime();
            benchScalar(v);
            var scalarDuration = System.nanoTime() - scalarStart;

            // Only count samples after some warmup.
            if (j > (sampleCount / 2)) {
                if (fmaDuration < scalarDuration) {
                    fastSamples++;
                }
            }
        }
        USE_FMA = fastSamples > (sampleCount / 2);
    }

    private static void benchFma(float[] v) {
        for (int i = 2; i < v.length; i++) {
            v[i] = Math.fma(v[i], v[i - 1], v[i - 2]);
        }
    }

    private static void benchScalar(float[] v) {
        for (int i = 2; i < v.length; i++) {
            v[i] = (v[i] * v[i - 1]) + v[i - 2];
        }
    }
}
