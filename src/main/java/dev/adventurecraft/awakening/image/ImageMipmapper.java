package dev.adventurecraft.awakening.image;

import dev.adventurecraft.awakening.layout.Size;
import dev.adventurecraft.awakening.util.MathF;

import java.nio.IntBuffer;

public final class ImageMipmapper {

    public static int clampLevel(int width, int height, int level) {
        return Math.min(level, MathF.log2(Math.max(width, height)));
    }

    public static int clampLevel(Size size, int level) {
        return clampLevel(size.w, size.h, level);
    }

    public static int getPixelOffset(int width, int height, int level) {
        int offset = 0;
        // TODO: clamp level to given width/height
        for (int l = 0; l < level; l++) {
            int mipW = Math.max(1, width >> l);
            int mipH = Math.max(1, height >> l);
            offset += mipW * mipH;
        }
        return offset;
    }

    public static void generate(IntBuffer levelBuffer, int width, int height, int minLevel, int maxLevel) {
        if (minLevel < 1) {
            throw new IllegalArgumentException();
        }
        minLevel = clampLevel(width, height, minLevel);
        maxLevel = clampLevel(width, height, maxLevel);

        int srcOffset = getPixelOffset(width, height, minLevel - 1);
        int mipOffset = getPixelOffset(width, height, minLevel);

        for (int level = minLevel; level <= maxLevel; ++level) {
            int levelWidth = Math.max(1, width >> level);
            int levelHeight = Math.max(1, height >> level);

            int prevWidth = width >> (level - 1);
            int prevHeight = height >> (level - 1);

            IntBuffer srcSpan = levelBuffer.slice(srcOffset, prevWidth * prevHeight);
            srcOffset += srcSpan.limit();

            IntBuffer levelSpan = levelBuffer.slice(mipOffset, levelWidth * levelHeight);
            mipOffset += levelSpan.limit();

            generateMip(srcSpan, prevWidth, prevHeight, levelSpan, levelWidth, levelHeight);
        }
    }

    public static void generateMip(IntBuffer src, int srcW, int srcH, IntBuffer dst, int dstW, int dstH) {
        if (Math.max(1, srcW >> 1) != dstW || Math.max(1, srcH >> 1) != dstH) {
            throw new IllegalArgumentException();
        }

        int y = 0;
        for (; y + 1 < srcH; y += 2) {
            int y0 = srcW * y;
            int y1 = srcW * (y + 1);
            int y2 = dstW * y;

            int x = 0;
            for (; x + 1 < srcW; x += 2) {
                int tR = src.get(y0 + x + 1);
                int tL = src.get(y0 + x);
                int bR = src.get(y1 + x + 1);
                int bL = src.get(y1 + x);

                int color = Rgba.weightedAverageColor(tL, tR, bR, bL);
                dst.put((y2 + x) >> 1, color);
            }
        }
    }

    public static int getAverageRgb(IntBuffer buffer) {
        long totalR = 0L;
        long totalG = 0L;
        long totalB = 0L;
        long colorCount = 0L;

        int len = buffer.limit();
        for (int i = 0; i < len; i++) {
            int color = buffer.get(i);
            int a = (color >>> 24) & 255;
            if (a != 0) {
                int r = color & 255;
                int g = (color >>> 8) & 255;
                int b = (color >>> 16) & 255;
                totalR += r;
                totalG += g;
                totalB += b;
                colorCount++;
            }
        }

        if (colorCount <= 0L) {
            return -1;
        }
        int avgR = (int) (totalR / colorCount);
        int avgG = (int) (totalG / colorCount);
        int avgB = (int) (totalB / colorCount);
        return Rgba.fromRgb8(avgR, avgG, avgB);
    }
}
