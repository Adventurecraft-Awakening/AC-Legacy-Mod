package dev.adventurecraft.awakening.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public final class BigMath {

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
        if (value.bitLength() <= 64) {
            long bits = value.longValue();
            return MathF.log(MathF.unsignedLongToDouble(bits), base);
        }
        return getTopBits(value).log(base);
    }

    public static double log(BigDecimal value, double base) {
        return log(value.unscaledValue(), base) - value.scale() / Math.log10(base);
    }

    private static TopBitMeta getTopBits(BigInteger value) {
        byte[] bytes = value.toByteArray();
        int h = getInt(bytes, 0);
        int m = getInt(bytes, 4);
        int l = getInt(bytes, 8);
        return new TopBitMeta(bytes.length, h, m, l);
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

    private record TopBitMeta(int byteCount, int high, int mid, int low) {

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
            long b = this.byteCount * 8L - c;

            // Extract most significant bits
            long sx = (h << 32 + c) | (m << c) | (l >>> 32 - c);
            double x = MathF.unsignedLongToDouble(sx);

            // Let v = value, b = bit count, x = v/2^b-64
            // log ( v/2^b-64 * 2^b-64 ) = log ( x ) + log ( 2^b-64 )
            return MathF.log(x, base) + (b - 64) / MathF.log2(base);
        }
    }
}
