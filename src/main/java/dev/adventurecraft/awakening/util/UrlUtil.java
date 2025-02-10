package dev.adventurecraft.awakening.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.HexFormat;

public final class UrlUtil {

    private static boolean isSafeForPath(char c) {
        return c >= 0x21 && c <= 0x7F;
    }

    private static int getSafePathLength(CharSequence value) {
        int length = value.length();
        for (int i = 0; i < length; i++) {
            if (!isSafeForPath(value.charAt(i))) {
                return i;
            }
        }
        return length;
    }

    public static String encodePath(String value, Charset charset) {
        int safeLength = getSafePathLength(value);
        if (safeLength == value.length()) {
            return value;
        }
        return encodePath(value, safeLength, charset);
    }

    private static String encodePath(String value, int safeLength, Charset charset) {
        var hex = HexFormat.of().withUpperCase();
        var encoder = charset.newEncoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE);

        int length = value.length();
        var builder = new StringBuilder((int) Math.ceil(length * encoder.averageBytesPerChar()));
        builder.append(value, 0, safeLength);

        var charBuf = CharBuffer.allocate(2);
        var byteBuf = ByteBuffer.allocate((int) Math.ceil(charBuf.capacity() * encoder.maxBytesPerChar()));

        for (int i = safeLength; i < length; i++) {
            char c = value.charAt(i);
            if (isSafeForPath(c)) {
                builder.append(c);
                continue;
            }

            charBuf.put(c);
            if (Character.isHighSurrogate(c) && (i + 1) < length) {
                char c1 = value.charAt(i + 1);
                if (Character.isLowSurrogate(c1)) {
                    charBuf.put(c1);
                    i++;
                }
            }

            charBuf.flip();
            try {
                CoderResult er = encoder.encode(charBuf, byteBuf, true);
                if (!er.isUnderflow()) {
                    er.throwException();
                }
            } catch (CharacterCodingException e) {
                throw new Error(e);
            }

            for (int j = 0; j < byteBuf.position(); j++) {
                builder.append('%');
                hex.toHexDigits(builder, byteBuf.get(j));
            }
            charBuf.clear();
            byteBuf.clear();
        }
        return builder.toString();
    }
}
