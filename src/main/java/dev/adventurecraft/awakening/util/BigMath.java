package dev.adventurecraft.awakening.util;

import com.google.common.primitives.UnsignedLong;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class BigMath {

    private static final Field magField;

    static {
        try {
            magField = BigInteger.class.getDeclaredField("mag");
        }
        catch (java.lang.NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static double log(BigInteger value, double base) {
        if (value.signum() < 0 || base == 1.0D) {
            return Double.NaN;
        }
        if (base == Double.POSITIVE_INFINITY) {
            return value.equals(BigInteger.ONE) ? 0.0D : Double.NaN;
        }
        if (base == 0.0D && !value.equals(BigInteger.ONE)) {
            return Double.NaN;
        }
        return getTopBits(value).log(base);
    }

    public static double log(BigDecimal value, double base) {
        return log(value.unscaledValue(), base) - value.scale() / Math.log10(base);
    }

    private static TopBitMeta getTopBits(BigInteger value) {
        try {
            int[] bits = (int[]) magField.get(value);
            int h = bits[0];
            int m = bits.length > 1 ? bits[1] : 0;
            int l = bits.length > 2 ? bits[2] : 0;
            return new TopBitMeta((long) bits.length * 32, h, m, l);
        }
        catch (IllegalAccessException e) {
            return getTopBitsFallback(value);
        }
    }

    private static TopBitMeta getTopBitsFallback(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int h = getInt(bytes, 0);
        int m = getInt(bytes, 4);
        int l = getInt(bytes, 8);
        return new TopBitMeta((long) bytes.length * 8, h, m, l);
    }

    private static int getInt(byte[] bytes, int off) {
        int b0 = getByte(bytes, off);
        int b1 = getByte(bytes, off + 1);
        int b2 = getByte(bytes, off + 2);
        int b3 = getByte(bytes, off + 3);
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    private static int getByte(byte[] bytes, int i) {
        return (i < bytes.length ? bytes[i] : 0) & 0xFF;
    }

    private record TopBitMeta(long bitCount, int high, int mid, int low) {

        /**
         * @see <a href="https://github.com/dotnet/runtime/blob/1d1bf92fcf43aa6981804dc53c5174445069c9e4/src/libraries/System.Runtime.Numerics/src/System/Numerics/BigInteger.cs#L832-L857">BigInteger.cs</a>
         */
        public double log(double base) {
            // Licensed to the .NET Foundation under one or more agreements.
            // The .NET Foundation licenses this file to you under the MIT license.

            long h = Integer.toUnsignedLong(this.high);
            long m = Integer.toUnsignedLong(this.mid);
            long l = Integer.toUnsignedLong(this.low);

            // Measure the exact bit count
            int c = Integer.numberOfLeadingZeros((int) h);
            long b = this.bitCount - c;

            // Extract most significant bits
            long sx = (h << 32 + c) | (m << c) | (l >>> 32 - c);
            double x = UnsignedLong.fromLongBits(sx).doubleValue();

            // Let v = value, b = bit count, x = v/2^b-64
            // log ( v/2^b-64 * 2^b-64 ) = log ( x ) + log ( 2^b-64 )
            return MathF.log(x, base) + (b - 64) / MathF.log(base, 2);
        }
    }
}
