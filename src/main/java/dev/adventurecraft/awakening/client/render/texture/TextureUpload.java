package dev.adventurecraft.awakening.client.render.texture;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.image.ImageMipmapper;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntRect;

import java.nio.IntBuffer;

/** CPU image preparation and explicit mip uploads shared by texture loaders. */
public final class TextureUpload {
    private TextureUpload() { }

    public static int byteSize(int width, int height, int maxLevel) {
        if (width < 1 || height < 1 || maxLevel < 0
            || maxLevel >= 32 - Integer.numberOfLeadingZeros(Math.max(width, height))) throw new IllegalArgumentException("Invalid mip scratch dimensions");
        int pixels = 0;
        for (int level = 0; level <= maxLevel; level++)
            pixels = Math.addExact(pixels, Math.multiplyExact(Math.max(1, width >> level), Math.max(1, height >> level)));
        return Math.multiplyExact(pixels, 4);
    }

    public static void prepare(ImageBuffer image, IntBuffer levels, int maxLevel, boolean anaglyph) {
        IntBuffer base = levels.slice(0, Math.multiplyExact(image.getWidth(), image.getHeight()));
        image.copyTo(base, ImageFormat.RGBA_U8);
        if (image.getFormat().hasAlpha() || anaglyph) replaceTransparentRgb(base, anaglyph);
        if (maxLevel > 0) ImageMipmapper.generate(levels, image.getWidth(), image.getHeight(), 1, maxLevel);
    }

    /** Material RGBA carries independent linear data, including RGB where alpha is zero. */
    public static void prepare(ImageBuffer image, IntBuffer levels, int maxLevel, boolean anaglyph,
                               TextureLoadOptions.DataType dataType) {
        if (dataType == TextureLoadOptions.DataType.COLOR) {
            prepare(image, levels, maxLevel, anaglyph);
            return;
        }
        image.copyTo(levels.slice(0, Math.multiplyExact(image.getWidth(), image.getHeight())), ImageFormat.RGBA_U8);
        generateMaterialMips(levels, image.getWidth(), image.getHeight(), maxLevel, dataType);
    }

    public static void generateMaterialMips(IntBuffer levels, int width, int height, int maxLevel,
                                             TextureLoadOptions.DataType dataType) {
        for (int level = 1; level <= maxLevel; level++) {
            int srcWidth = Math.max(1, width >> (level - 1));
            int srcHeight = Math.max(1, height >> (level - 1));
            int dstWidth = Math.max(1, width >> level), dstHeight = Math.max(1, height >> level);
            int src = ImageMipmapper.getPixelOffset(width, height, level - 1);
            int dst = ImageMipmapper.getPixelOffset(width, height, level);
            for (int y = 0; y < dstHeight; y++) for (int x = 0; x < dstWidth; x++) {
                int left = Math.min(2 * x, srcWidth - 1), right = Math.min(left + 1, srcWidth - 1);
                int top = Math.min(2 * y, srcHeight - 1), bottom = Math.min(top + 1, srcHeight - 1);
                int a = levels.get(src + top * srcWidth + left), b = levels.get(src + top * srcWidth + right);
                int c = levels.get(src + bottom * srcWidth + left), d = levels.get(src + bottom * srcWidth + right);
                int red = average(a & 255, b & 255, c & 255, d & 255);
                int green = average(a >>> 8 & 255, b >>> 8 & 255, c >>> 8 & 255, d >>> 8 & 255);
                int blue = average(a >>> 16 & 255, b >>> 16 & 255, c >>> 16 & 255, d >>> 16 & 255);
                int alpha;
                if (dataType == TextureLoadOptions.DataType.SPECULAR) {
                    alpha = average(emission(a), emission(b), emission(c), emission(d));
                    if (alpha == 0) alpha = 255;
                } else alpha = average(a >>> 24, b >>> 24, c >>> 24, d >>> 24);
                levels.put(dst + y * dstWidth + x, Rgba.fromRgba8(red, green, blue, alpha));
            }
        }
    }

    private static int average(int a, int b, int c, int d) { return (a + b + c + d + 2) / 4; }
    private static int emission(int pixel) { int alpha = pixel >>> 24; return alpha == 255 ? 0 : alpha; }

    private static void replaceTransparentRgb(IntBuffer pixels, boolean anaglyph) {
        int average = 0;
        for (int i = 0; i < pixels.limit(); i++) {
            int color = pixels.get(i);
            int alpha = color >>> 24;
            if (anaglyph) {
                int red = color & 255, green = color >>> 8 & 255, blue = color >>> 16 & 255;
                color = Rgba.fromRgba8((red * 30 + green * 59 + blue * 11) / 100,
                    (red * 30 + green * 70) / 100, (red * 30 + blue * 70) / 100, alpha);
            }
            if (alpha == 0) {
                if (average == 0) average = ImageMipmapper.getAverageRgb(pixels);
                color = Rgba.withAlpha(average, alpha);
            }
            pixels.put(i, color);
        }
    }

    public static void uploadLevels(RenderTexture texture, IntRect rect, int minLevel, int maxLevel,
                                    int replicate, IntBuffer levels) {
        if (replicate < 1 || minLevel < 0 || maxLevel < minLevel) throw new IllegalArgumentException("Invalid mip upload");
        int offset = ImageMipmapper.getPixelOffset(rect.w, rect.h, minLevel);
        for (int level = minLevel; level <= maxLevel; level++) {
            int width = Math.max(1, rect.w >> level), height = Math.max(1, rect.h >> level);
            IntBuffer pixels = levels.slice(offset, Math.multiplyExact(width, height));
            offset += pixels.remaining();
            for (int tileY = 0; tileY < replicate; tileY++) {
                for (int tileX = 0; tileX < replicate; tileX++) {
                    texture.upload(level, (rect.x >> level) + tileX * width, (rect.y >> level) + tileY * height,
                        width, height, pixels);
                }
            }
        }
    }
}
