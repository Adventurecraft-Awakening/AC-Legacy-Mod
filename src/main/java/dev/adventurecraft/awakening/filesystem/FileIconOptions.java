package dev.adventurecraft.awakening.filesystem;

import dev.adventurecraft.awakening.image.ImageFormat;

import java.util.List;

public record FileIconOptions(ImageFormat format, int width, int height, int scale, List<FileIconFlags> flags) {

    public FileIconOptions {
        flags = List.copyOf(flags);
    }
}
