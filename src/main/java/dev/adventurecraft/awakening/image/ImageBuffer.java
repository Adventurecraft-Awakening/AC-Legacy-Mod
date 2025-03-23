package dev.adventurecraft.awakening.image;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public final class ImageBuffer {

    private final ByteBuffer buffer;
    private final int width;
    private final int height;
    private final int stride;
    private final ImageFormat format;

    ImageBuffer(ByteBuffer buffer, int width, int height, int stride, ImageFormat format) {
        if (format == null)
            throw new IllegalArgumentException();

        this.buffer = buffer;
        this.width = width;
        this.height = height;
        this.stride = stride;
        this.format = format;
    }

    public Rect getBounds() {
        return new Rect(0, 0, this.width, this.height);
    }

    public Size getSize() {
        return new Size(this.width, this.height);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getByteStride() {
        return this.stride;
    }

    public ImageFormat getFormat() {
        return this.format;
    }

    public ByteBuffer getBuffer() {
        return this.buffer;
    }

    public ByteBuffer getRowSlice(int y) {
        return this.buffer.slice(y * this.stride, this.stride).order(ByteOrder.nativeOrder());
    }

    public void copyTo(ImageBuffer dst, Point dstPoint, Point srcPoint, Size size) {
        var src = this;

        int srcStart = src.format.getByteStride(srcPoint.x);
        int dstStart = dst.format.getByteStride(dstPoint.x);
        int srcSize = src.format.getByteStride(size.w);
        int dstSize = dst.format.getByteStride(size.w);

        if (src.format == dst.format) {
            for (int y = 0; y < size.h; y++) {
                var srcSpan = src.getRowSlice(y + srcPoint.y).slice(srcStart, srcSize);
                var dstSpan = dst.getRowSlice(y + dstPoint.y).slice(dstStart, dstSize);
                dstSpan.put(srcSpan);
            }
        } else {
            var srcBuffer = BufferUtils.createByteBuffer(src.format.getByteStride(size.w));
            var dstBuffer = BufferUtils.createByteBuffer(dst.format.getByteStride(size.w));
            throw new NotImplementedException();
        }
    }

    public void copyTo(ImageBuffer dst) {
        copyTo(dst, Point.zero, Point.zero, this.getSize());
    }

    public void copyTo(IntBuffer dst, Point dstPoint, Point srcPoint, Size size, ImageFormat dstFormat) {
        var src = this;
        if (src.format == dstFormat) {
            blitTo(dst, dstPoint, srcPoint, size);
            return;
        }

        if (src.format == ImageFormat.RGBA_U8) {
            if (dstFormat == ImageFormat.BGRA_U8) {
                rgba8ToBgra8(dst, dstPoint, srcPoint, size);
                return;
            }
        }
        throw new NotImplementedException();
    }

    private void blitTo(IntBuffer dst, Point dstPoint, Point srcPoint, Size size) {
        var src = this;
        int dstOffset = dstPoint.y * size.w + dstPoint.x;
        for (int i = 0; i < size.h; i++) {
            var srcSpan = src.getRowSlice(i + srcPoint.y).asIntBuffer().slice(srcPoint.x, size.w);
            var dstSpan = dst.slice(dstOffset, size.w);

            dstSpan.put(srcSpan);
            dstOffset += size.w;
        }
    }

    private void rgba8ToBgra8(IntBuffer dst, Point dstPoint, Point srcPoint, Size size) {
        var src = this;
        int dstOffset = dstPoint.y * size.w + dstPoint.x;
        for (int i = 0; i < size.h; i++) {
            var srcSpan = src.getRowSlice(i + srcPoint.y).asIntBuffer().slice(srcPoint.x, size.w);
            var dstSpan = dst.slice(dstOffset, size.w);

            for (int j = 0; j < size.w; j++) {
                dstSpan.put(j, Rgba.fromBgra(srcSpan.get(j)));
            }
            dstOffset += size.w;
        }
    }

    public void copyTo(IntBuffer dst, ImageFormat format) {
        this.copyTo(dst, Point.zero, Point.zero, this.getSize(), format);
    }

    public static ImageBuffer create(int width, int height, int stride, ImageFormat format) {
        int capacity = stride * height;
        var buffer = BufferUtils.createByteBuffer(capacity).limit(capacity);
        return new ImageBuffer(buffer, width, height, stride, format);
    }

    public static ImageBuffer create(int width, int height, ImageFormat format) {
        int stride = format.getByteStride(width);
        return create(width, height, stride, format);
    }

    public static ImageBuffer from(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        var format = ImageFormat.fromAwt(image.getColorModel());
        if (format != null) {
            int stride = format.getByteStride(width);
            var raster = image.getRaster();
            //var rasterBuf = raster.getDataBuffer();
            //if (rasterBuf instanceof DataBufferByte byteBuffer) {
            //    var nioBuffer = ByteBuffer.wrap(byteBuffer.getData());
            //    if (nioBuffer.capacity() != stride * height) {
            //        throw new RuntimeException("Unexpected byte buffer size.");
            //    }
            //    return new ImageBuffer(nioBuffer, width, height, stride, format);
            //}
        }

        var result = ImageBuffer.create(width, height, ImageFormat.RGBA_U8);
        var rgbBuf = new int[width];
        for (int y = 0; y < height; y++) {
            image.getRGB(0, y, width, 1, rgbBuf, 0, width);
            for (int x = 0; x < width; x++) {
                rgbBuf[x] = Rgba.fromBgra(rgbBuf[x]);
            }
            result.getRowSlice(y).asIntBuffer().put(rgbBuf);
        }
        return result;
    }

    public static ImageBuffer wrap(ByteBuffer buffer, int width, int height, int stride, ImageFormat format) {
        return new ImageBuffer(buffer, width, height, stride, format);
    }

    public static ImageBuffer wrap(ByteBuffer buffer, int width, int height, ImageFormat format) {
        int stride = format.getByteStride(width);
        return wrap(buffer, width, height, stride, format);
    }
}
