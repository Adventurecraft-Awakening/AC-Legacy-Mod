package dev.adventurecraft.awakening.client.render.material;

import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import org.lwjgl.stb.STBImageWrite;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/** Minimal RGBA PNG writer used for generated map material templates. */
final class PngWriter {

    private PngWriter() { }

    static void write(File file, ImageBuffer image) throws IOException {
        if (image.getFormat() != ImageFormat.RGBA_U8) {
            throw new IOException("PNG material output requires RGBA_U8");
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory " + parent);
        }
        ByteBuffer pixels = image.getBuffer().duplicate();
        pixels.position(0);
        boolean ok = STBImageWrite.stbi_write_png(file.getAbsolutePath(), image.getWidth(), image.getHeight(),
            4, pixels, image.getByteStride());
        if (!ok) {
            throw new IOException("stb_image_write failed for " + file);
        }
    }
}
