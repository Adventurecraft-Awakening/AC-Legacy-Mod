package dev.adventurecraft.awakening.util;

import dev.adventurecraft.awakening.text.TextBuffer;

public final class HexConvert {
    // Licensed to the .NET Foundation under one or more agreements.

    // Output [ '0' .. '9' ] and [ 'A' .. 'F' ].
    public static final int CASING_UPPER = 0;

    // Output [ '0' .. '9' ] and [ 'a' .. 'f' ].
    // This works because values in the range [ 0x30 .. 0x39 ] ([ '0' .. '9' ])
    // already have the 0x20 bit set, so ORing them with 0x20 is a no-op,
    // while outputs in the range [ 0x41 .. 0x46 ] ([ 'A' .. 'F' ])
    // don't have the 0x20 bit set, so ORing them maps to
    // [ 0x61 .. 0x66 ] ([ 'a' .. 'f' ]), which is what we want.
    public static final int CASING_LOWER = 0x2020;

    // Analysis has shown that generating the whole array allows the JIT to generate
    // better code compared to a slimmed down array, such as one cutting off after 'f'
    private static final byte[] DIGITS = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1,           //

        -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, //
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1  //
    };

    public static int fromHexDigit(int ch) {
        int value;
        if ((ch >>> 7) == 0 && (value = DIGITS[ch]) >= 0) {
            return value;
        }
        return -1;
    }

    public static void appendHex4(short value, TextBuffer output, int casing) {
        //output.append(String.format("%04x", (int) value));

        appendHex2((byte) (value & 0xff), output, casing);
        appendHex2((byte) ((value >>> 8) & 0xff), output, casing);
    }

    public static void appendHex2(byte value, TextBuffer output, int casing) {
        int difference = ((value & 0xF0) << 4) + (value & 0x0F) - 0x8989;
        int packedResult = ((((-difference) & 0x7070) >> 4) + difference + 0xB9B9) | casing;

        output.append((char) (packedResult & 0xff));
        output.append((char) ((packedResult >> 8) & 0xff));
    }
}
