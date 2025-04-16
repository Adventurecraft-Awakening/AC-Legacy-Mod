package dev.adventurecraft.awakening.image;

import javax.annotation.Nullable;

public class ImageLoadOptions {

    private int desiredChannelCount = 0;
    private int desiredBitDepth = 0;

    @Nullable
    private ImageFormat desiredFormat;

    public int desiredChannelCount() {
        return this.desiredChannelCount;
    }

    public ImageLoadOptions withDesiredChannelCount(int count) {
        this.desiredChannelCount = count;
        return this;
    }

    public int desiredBitDepth() {
        return this.desiredBitDepth;
    }

    public ImageLoadOptions withDesiredBitDepth(int bitDepth) {
        this.desiredBitDepth = bitDepth;
        return this;
    }

    @Nullable
    public ImageFormat desiredFormat() {
        return this.desiredFormat;
    }

    public ImageLoadOptions withDesiredFormat(@Nullable ImageFormat format) {
        this.desiredFormat = format;
        return this;
    }

    public int getTargetChannelCount() {
        if (this.desiredFormat != null) {
            return this.desiredFormat.channelCount();
        }
        return this.desiredChannelCount;
    }

    public int getTargetBitDepth() {
        if (this.desiredFormat != null) {
            return this.desiredFormat.bitDepth();
        }
        return this.desiredBitDepth;
    }

    public static ImageLoadOptions withFormat(ImageFormat format) {
        return new ImageLoadOptions().withDesiredFormat(format);
    }
}
