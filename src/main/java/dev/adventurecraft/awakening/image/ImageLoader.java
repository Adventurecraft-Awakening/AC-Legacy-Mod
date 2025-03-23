package dev.adventurecraft.awakening.image;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.jni.JNINativeInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

public class ImageLoader {

    public static ImageBuffer load(ReadableByteChannel channel, ImageLoadOptions options)
        throws IOException {
        var callbacks = STBIIOCallbacks.create().set(
            (user, data, size) -> {
                ChannelContext ctx = MemoryUtil.memGlobalRefToObject(user);
                if (ctx.eof) {
                    return 0;
                }
                try {
                    int n = ctx.channel.read(STBIReadCallback.getData(data, size));
                    if (n >= 0) {
                        return n;
                    }
                } catch (IOException e) {
                    ctx.ioe = e;
                }
                ctx.eof = true;
                return 0;
            },
            (user, size) -> {
                ChannelContext ctx = MemoryUtil.memGlobalRefToObject(user);
                if (ctx.eof) {
                    return;
                }
                try (var stack = MemoryStack.stackPush()) {
                    if (ctx.channel instanceof SeekableByteChannel seekable) {
                        seekable.position(seekable.position() + size);
                        return;
                    }

                    ByteBuffer buffer = stack.malloc(1024);
                    while (size > 0) {
                        int toSkip = Math.min(size, buffer.capacity());
                        int n = ctx.channel.read(buffer.slice(0, toSkip));
                        if (n <= 0) {
                            if (n < 0) {
                                ctx.eof = true;
                            }
                            break;
                        }
                        size -= n;
                    }
                } catch (IOException e) {
                    ctx.ioe = e;
                    ctx.eof = true;
                }
            },
            (user) -> {
                ChannelContext ctx = MemoryUtil.memGlobalRefToObject(user);
                return ctx.eof ? 1 : 0;
            });

        var metaBuffer = BufferUtils.createIntBuffer(3);
        var context = new ChannelContext(channel);
        var user = JNINativeInterface.NewGlobalRef(context);
        try {
            int targetChannels = options.getTargetChannelCount();
            int targetBitDepth = options.getTargetBitDepth();

            var pixelBuffer = STBImage.stbi_load_from_callbacks(
                callbacks,
                user,
                metaBuffer.slice(0, 1),
                metaBuffer.slice(1, 1),
                metaBuffer.slice(2, 1),
                targetChannels);
            if (pixelBuffer == null && context.ioe != null) {
                throw context.ioe;
            }
            return fromMeta(metaBuffer, pixelBuffer, targetChannels, targetBitDepth);
        } finally {
            JNINativeInterface.DeleteGlobalRef(user);
        }
    }

    public static ImageBuffer load(URL url, ImageLoadOptions options)
        throws IOException {
        return load(Channels.newChannel(url.openStream()), options);
    }

    public static ImageBuffer load(InputStream stream, ImageLoadOptions options)
        throws IOException {
        return load(Channels.newChannel(stream), options);
    }

    public static ImageBuffer load(File file, ImageLoadOptions options)
        throws IOException {
        try (var stream = new FileInputStream(file)) {
            return load(stream.getChannel(), options);
        }
    }

    private static ImageBuffer fromMeta(
        IntBuffer metaData, ByteBuffer pixelData, int desiredChannels, int bitDepth) {
        if (pixelData == null) {
            return null;
        }
        int width = metaData.get(0);
        int height = metaData.get(1);
        int channels = desiredChannels != 0 ? desiredChannels : metaData.get(2);

        var format = ImageFormat.fromStb(channels, bitDepth);
        if (format == null) {
            throw new RuntimeException("Unsupported image format.");
        }

        int stride = format.getByteStride(width);
        if (stride * height != pixelData.limit()) {
            throw new RuntimeException("Unexpected pixel buffer size.");
        }

        return new ImageBuffer(pixelData, width, height, stride, format);
    }

    static class ChannelContext {
        final ReadableByteChannel channel;
        boolean eof = false;
        IOException ioe = null;

        ChannelContext(ReadableByteChannel channel) {
            this.channel = channel;
        }
    }
}
