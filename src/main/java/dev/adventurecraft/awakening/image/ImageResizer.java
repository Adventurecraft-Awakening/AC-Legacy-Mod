package dev.adventurecraft.awakening.image;

import dev.adventurecraft.awakening.util.BufferUtil;
import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;

public final class ImageResizer {

    public static void resize(ImageBuffer src, ImageBuffer dst) {
        var format = src.getFormat();
        var dstFormat = dst.getFormat();
        if (format != dstFormat) {
            throw new NotImplementedException(String.format(
                "source format \"{}\" differs from destination \"{}\"",
                format.toShortString(),
                dstFormat.toShortString()
            ));
        }

        if (format != ImageFormat.RGBA_U8) {
            throw new NotImplementedException(String.format(
                "source format \"{}\" is not RGBA_U8",
                format.toShortString()
            ));
        }

        if (resizeUint8(
            src.getBuffer(),
            src.getWidth(),
            src.getHeight(),
            src.getByteStride(),
            dst.getBuffer(),
            dst.getWidth(),
            dst.getHeight(),
            dst.getByteStride(),
            4
        )) {
            return;
        }
        throw new AssertionError("failed to resize");
    }

    public static boolean resizeUint8(
        Buffer src,
        int srcW,
        int srcH,
        int srcByteStride,
        Buffer dst,
        int dstW,
        int dstH,
        int dstByteStride,
        int channels
    ) {
        int srcPxSize = BufferUtil.bytesPerElement(src) * channels;
        BufferUtil.checkBuffer(
            src.remaining() * srcPxSize,
            srcH * (srcByteStride == 0 ? srcW * srcPxSize : srcByteStride)
        );

        int dstPxSize = BufferUtil.bytesPerElement(dst) * channels;
        BufferUtil.checkBuffer(
            dst.remaining() * dstPxSize,
            dstH * (dstByteStride == 0 ? dstW * dstPxSize : dstByteStride)
        );

        return STBImageResize.nstbir_resize(
            MemoryUtil.memAddress(src),
            srcW,
            srcH,
            srcByteStride,
            MemoryUtil.memAddress(dst),
            dstW,
            dstH,
            dstByteStride,
            STBImageResize.STBIR_RGBA,
            STBImageResize.STBIR_TYPE_UINT8,
            STBImageResize.STBIR_EDGE_CLAMP,
            STBImageResize.STBIR_FILTER_DEFAULT
        ) != 0;
    }
}
