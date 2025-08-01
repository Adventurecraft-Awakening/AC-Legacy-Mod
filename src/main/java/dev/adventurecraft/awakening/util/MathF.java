package dev.adventurecraft.awakening.util;

import com.google.common.primitives.UnsignedLong;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class MathF {

    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
    private static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;

    private static final double LOG2_INVERSE = 1.44269504088896340735992468100189213743;

    // The `Math.clamp` built-ins perform expensive checks, we skip them here.

    public static int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(value, min));
    }

    public static int clamp(long value, int min, int max) {
        return (int) Math.min(max, Math.max(value, min));
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(value, min));
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(value, min));
    }

    public static float cubicInterpolation(float mu, float y0, float y1, float y2, float y3) {
        float a0 = -0.5F * y0 + 1.5F * y1 - 1.5F * y2 + 0.5F * y3;
        float a1 = y0 - 2.5F * y1 + 2.0F * y2 - 0.5F * y3;
        float a2 = -0.5F * y0 + 0.5F * y2;
        float mu2 = mu * mu;
        return (a0 * mu) * mu2 + a1 * mu2 + a2 * mu + y1;
    }

    public static float fastLerp(float amount, float start, float end) {
        return (end - start) * amount + start;
    }

    public static float lerp(float amount, float start, float end) {
        return (1.0F - amount) * start + amount * end;
    }

    public static double lerp(double amount, double start, double end) {
        return (1.0 - amount) * start + amount * end;
    }

    public static double round(double value, int decimals) {
        return Math.round(value * decimals) / (double) decimals;
    }

    public static double roundToZero(double value) {
        return value < 0 ? Math.ceil(value) : Math.floor(value);
    }

    /**
     * Returns the logarithm of a number using a specified base.
     *
     * @param a The number.
     * @param base The base.
     * @return The logarithm.
     */
    public static double log(double a, double base) {
        return Math.log(a) / Math.log(base);
    }

    public static double log2(double a) {
        return Math.log(a) * LOG2_INVERSE;
    }

    /**
     * Returns the logarithm of a number using a specified base.
     *
     * @param a The number.
     * @param base The base.
     * @return The logarithm.
     * @implNote Uses {@link Number#doubleValue()}
     * when number is not {@link BigDecimal} or {@link BigInteger}.
     */
    public static double log(Number a, double base) {
        if (a instanceof BigDecimal bigDec) {
            return BigMath.log(bigDec, base);
        }
        else if (a instanceof BigInteger bigInt) {
            return BigMath.log(bigInt, base);
        }
        return log(a.doubleValue(), base);
    }

    public static float sin(float a) {
        return (float) Math.sin(a);
    }

    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    public static float toRadians(float degrees) {
        return degrees * (float) DEGREES_TO_RADIANS;
    }

    public static float toRadians(double degrees) {
        return (float) (degrees * DEGREES_TO_RADIANS);
    }

    /// @see UnsignedLong#doubleValue()
    /// @see <a href="https://github.com/WebAssembly/spec/blob/1041527d508218acc40f0278d4abc3be9ba5e3bd/interpreter/exec/f64_convert.ml#L27-L37">f64_convert.ml</a>
    public static double unsignedLongToDouble(long value) {
        if (value >= 0) {
            return (double) value;
        }
        return ((value >>> 1) | (value & 1)) * 2.0;
    }

    public static double normalizeAngle(double a) {
        while (a < -180.0D) {
            a += 360.0D;
        }
        while (a >= 180.0D) {
            a -= 360.0D;
        }
        return a;
    }

    public static float normalizeAngle(float a) {
        while (a < -180.0F) {
            a += 360.0F;
        }
        while (a >= 180.0F) {
            a -= 360.0F;
        }
        return a;
    }

    public static float clampAngle(float a, float min, float max) {
        return clamp(normalizeAngle(a), min, max);
    }
}
