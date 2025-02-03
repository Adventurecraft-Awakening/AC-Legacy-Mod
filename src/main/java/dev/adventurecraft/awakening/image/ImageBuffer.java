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
    private final int format;

    ImageBuffer(ByteBuffer buffer, int width, int height, int stride, int format) {
        ImageFormat.check(format);

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

    public int getFormat() {
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

        int srcStart = ImageFormat.byteStride(srcPoint.x, src.format);
        int dstStart = ImageFormat.byteStride(dstPoint.x, dst.format);
        int srcSize = ImageFormat.byteStride(size.w, src.format);
        int dstSize = ImageFormat.byteStride(size.w, dst.format);

        if (src.format == dst.format) {
            for (int y = 0; y < size.h; y++) {
                var srcSpan = src.getRowSlice(y + srcPoint.y).slice(srcStart, srcSize);
                var dstSpan = dst.getRowSlice(y + dstPoint.y).slice(dstStart, dstSize);
                dstSpan.put(srcSpan);
            }
        } else {
            var srcBuffer = BufferUtils.createByteBuffer(ImageFormat.byteStride(size.w, src.format));
            var dstBuffer = BufferUtils.createByteBuffer(ImageFormat.byteStride(size.w, dst.format));
            throw new NotImplementedException();
        }
    }

    public void copyTo(ImageBuffer dst) {
        copyTo(dst, Point.zero, Point.zero, this.getSize());
    }

    public void copyTo(IntBuffer dst, Point dstPoint, Point srcPoint, Size size, int dstFormat) {
        var src = this;
        if (src.format == dstFormat) {
            int dstOffset = dstPoint.y * size.w + dstPoint.x;
            for (int i = 0; i < size.h; i++) {
                var srcSpan = src.getRowSlice(i + srcPoint.y).asIntBuffer().slice(srcPoint.x, size.w);
                var dstSpan = dst.slice(dstOffset, size.w);
                dstSpan.put(srcSpan);
                dstOffset += size.w;

            }
        } else {
            throw new NotImplementedException();
        }
    }

    public void copyTo(IntBuffer dst, int format) {
        this.copyTo(dst, Point.zero, Point.zero, this.getSize(), format);
    }

    public static ImageBuffer create(int width, int height, int stride, int format) {
        int capacity = stride * height;
        var buffer = BufferUtils.createByteBuffer(capacity).limit(capacity);
        return new ImageBuffer(buffer, width, height, stride, format);
    }

    public static ImageBuffer create(int width, int height, int format) {
        int stride = ImageFormat.byteStride(width, format);
        return create(width, height, stride, format);
    }

    public static ImageBuffer from(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int format = ImageFormat.fromAwt(image.getColorModel());
        if (format != ImageFormat.UNDEFINED) {
            int stride = ImageFormat.byteStride(width, format);
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

    public static ImageBuffer wrap(ByteBuffer buffer, int width, int height, int stride, int format) {
        return new ImageBuffer(buffer, width, height, stride, format);
    }

    public static ImageBuffer wrap(ByteBuffer buffer, int width, int height, int format) {
        int stride = ImageFormat.byteStride(width, format);
        return wrap(buffer, width, height, stride, format);
    }
}
