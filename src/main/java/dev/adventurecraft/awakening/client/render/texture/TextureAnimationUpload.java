package dev.adventurecraft.awakening.client.render.texture;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.layout.IntRect;
import org.lwjgl.BufferUtils;

/** Region uploads for synchronized animation frames, with separate color and material-data rules. */
public final class TextureAnimationUpload {
    private static final ThreadLocal<java.nio.ByteBuffer> SCRATCH =
        ThreadLocal.withInitial(() -> BufferUtils.createByteBuffer(0));
    private TextureAnimationUpload() { }

    public static void upload(RenderTexture target, IntRect rect, ImageBuffer image, int replicate,
                              TextureLoadOptions.DataType dataType) {
        var size = target.size(0);
        if (rect.w < 1 || rect.h < 1 || rect.x < 0 || rect.y < 0 || replicate < 1
            || (long) rect.x + (long) rect.w * replicate > size.w
            || (long) rect.y + (long) rect.h * replicate > size.h) {
            throw new IllegalArgumentException("Animated texture rectangle is outside its atlas");
        }
        if (image.getWidth() < 1 || image.getHeight() < 1 || image.getFormat() != ImageFormat.RGBA_U8) {
            throw new IllegalArgumentException("Animated texture frame must contain RGBA pixels");
        }
        int mip = Math.min(target.maxLevel(), 31 - Integer.numberOfLeadingZeros(Math.min(rect.w, rect.h)));
        // Stop before a mip would merge an animation's edge into a neighboring tile.
        while (mip > 0 && ((rect.x | rect.y | rect.w | rect.h) & ((1 << mip) - 1)) != 0) mip--;
        int byteSize = TextureUpload.byteSize(rect.w, rect.h, mip);
        var bytes = SCRATCH.get();
        if (bytes.capacity() < byteSize) {
            bytes = BufferUtils.createByteBuffer(byteSize);
            SCRATCH.set(bytes);
        }
        bytes.clear().limit(byteSize);
        var pixels = bytes.asIntBuffer();
        if (dataType == TextureLoadOptions.DataType.COLOR) {
            ImageResizer.resize(image, ImageBuffer.wrap(bytes, rect.w, rect.h, ImageFormat.RGBA_U8));
            if (mip > 0) ImageMipmapper.generate(pixels, rect.w, rect.h, 1, mip);
        } else {
            // Nearest resampling never treats height/emission alpha as pixel opacity.
            for (int y = 0; y < rect.h; y++) {
                var source = image.getRowSlice((int) ((long) y * image.getHeight() / rect.h)).asIntBuffer();
                for (int x = 0; x < rect.w; x++) {
                    pixels.put(y * rect.w + x, source.get((int) ((long) x * image.getWidth() / rect.w)));
                }
            }
            TextureUpload.generateMaterialMips(pixels, rect.w, rect.h, mip, dataType);
        }
        TextureUpload.uploadLevels(target, rect, 0, mip, replicate, pixels);
    }
}
