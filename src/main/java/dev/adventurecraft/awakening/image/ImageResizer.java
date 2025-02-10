package dev.adventurecraft.awakening.image;

import dev.adventurecraft.awakening.util.BufferUtil;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;

public final class ImageResizer {

    public static boolean resizeUint8(
        Buffer src, int srcW, int srcH, int srcByteStride,
        Buffer dst, int dstW, int dstH, int dstByteStride, int channels) {

        if (Checks.CHECKS) {
            int srcPxSize = BufferUtil.bytesPerElement(src) * channels;
            BufferUtil.checkBuffer(
                src.remaining() * srcPxSize,
                srcH * (srcByteStride == 0 ? srcW * srcPxSize : srcByteStride));

            int dstPxSize = BufferUtil.bytesPerElement(dst) * channels;
            BufferUtil.checkBuffer(
                dst.remaining() * dstPxSize,
                dstH * (dstByteStride == 0 ? dstW * dstPxSize : dstByteStride));
        }

        return STBImageResize.nstbir_resize_uint8_linear(
            MemoryUtil.memAddress(src), srcW, srcH, srcByteStride,
            MemoryUtil.memAddress(dst), dstW, dstH, dstByteStride, channels) != 0;
    }
}
