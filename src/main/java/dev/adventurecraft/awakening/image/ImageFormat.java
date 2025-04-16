package dev.adventurecraft.awakening.image;

import dev.adventurecraft.awakening.primitives.IntType;
import dev.adventurecraft.awakening.primitives.PrimitiveType;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.util.List;

public final class ImageFormat {

    public static final ImageFormat RGBA_U8 = new ImageFormat(Channel.rgba(IntType.U8));
    public static final ImageFormat RGBA_U16 = new ImageFormat(Channel.rgba(IntType.U16));
    public static final ImageFormat RGBA_U32 = new ImageFormat(Channel.rgba(IntType.U32));

    public static final ImageFormat BGRA_U8 = new ImageFormat(Channel.bgra(IntType.U8));
    public static final ImageFormat BGRA_U16 = new ImageFormat(Channel.bgra(IntType.U16));

    public static final ImageFormat RGB_U8 = new ImageFormat(Channel.rgb(IntType.U8));
    public static final ImageFormat RGB_U16 = new ImageFormat(Channel.rgb(IntType.U16));
    public static final ImageFormat RGB_U32 = new ImageFormat(Channel.rgb(IntType.U32));

    public static final ImageFormat BGR_U8 = new ImageFormat(Channel.bgr(IntType.U8));
    public static final ImageFormat BGR_U16 = new ImageFormat(Channel.bgr(IntType.U16));

    private static final ImageFormat STB_LA_U8 = new ImageFormat(Channel.bt601_a(IntType.U8));
    private static final ImageFormat STB_L_U8 = new ImageFormat(List.of(Channel.bt601(IntType.U8)));

    private static final ImageFormat STB_LA_U16 = new ImageFormat(Channel.bt601_a(IntType.U16));
    private static final ImageFormat STB_L_U16 = new ImageFormat(List.of(Channel.bt601(IntType.U16)));

    private final List<Channel> channels;
    private final int bitSize;
    private final int bitDepth;
    private final boolean hasAlpha;
    private final boolean hasLuma;

    public ImageFormat(List<Channel> channels) {
        this.channels = List.copyOf(channels);

        int bitSize = 0;
        boolean hasAlpha = false;
        boolean hasLuma = false;

        for (Channel c : this.channels) {
            bitSize += c.type().bits();

            if (c instanceof AlphaChannel) {
                hasAlpha = true;
            } else if (c instanceof LumaChannel) {
                hasLuma = true;
            }
        }
        this.bitSize = bitSize;
        this.hasAlpha = hasAlpha;
        this.hasLuma = hasLuma;

        this.bitDepth = (int) Math.ceil(bitSize / (double) this.channels.size());
    }

    @Nullable
    public static ImageFormat fromStb(int channels, int bitDepth) {
        return switch (bitDepth) {
            case 8 -> switch (channels) {
                case 4 -> RGBA_U8;
                case 3 -> RGB_U8;
                case 2 -> STB_LA_U8;
                case 1 -> STB_L_U8;
                default -> null;
            };
            case 16 -> switch (channels) {
                case 4 -> RGBA_U16;
                case 3 -> RGB_U16;
                case 2 -> STB_LA_U16;
                case 1 -> STB_L_U16;
                default -> null;
            };
            default -> null;
        };
    }

    @Nullable
    public static ImageFormat fromAwt(ColorModel model) {
        var colorSpace = model.getColorSpace();
        if (colorSpace.getType() != ColorSpace.TYPE_RGB) {
            return null;
        }

        int transferType = model.getTransferType();
        if (transferType != DataBuffer.TYPE_BYTE &&
            transferType != DataBuffer.TYPE_SHORT &&
            transferType != DataBuffer.TYPE_INT) {
            return null;
        }

        if (model instanceof DirectColorModel directModel) {
            int gMask = directModel.getGreenMask();
            if (gMask != 0xff00) {
                return null;
            }

            int rMask = directModel.getRedMask();
            int bMask = directModel.getBlueMask();
            boolean bgr;
            if (rMask == 0xff && bMask == 0xff0000) {
                bgr = false;
            } else if (rMask == 0xff0000 && bMask == 0xff) {
                bgr = true;
            } else {
                return null;
            }

            int aMask = directModel.getAlphaMask();
            if (aMask == 0) {
                return bgr ? BGR_U8 : RGB_U8;
            } else if (aMask == 0xff000000) {
                return bgr ? BGRA_U8 : RGBA_U8;
            }
        } else if (model instanceof ComponentColorModel compModel) {
            int colorCount = compModel.getNumColorComponents();
            if (colorCount != 3) {
                return null;
            }

            int transparency = compModel.getTransparency();
            if (transparency == Transparency.OPAQUE) {
                return RGB_U8;
            } else if (transparency == Transparency.TRANSLUCENT) {
                return RGBA_U8;
            }
        }
        return null;
    }

    public int bitSize() {
        return this.bitSize;
    }

    public int bitDepth() {
        return this.bitDepth;
    }

    public int channelCount() {
        return this.channels.size();
    }

    public boolean hasAlpha() {
        return this.hasAlpha;
    }

    public boolean hasLuma() {
        return this.hasLuma;
    }

    public long getBitStride(int width) {
        int bpp = bitDepth() * channelCount();
        return bpp * (long) width;
    }

    public int getByteStride(int width) {
        long bytes = (getBitStride(width) + 7) / 8;
        return (int) bytes;
    }

    public String toShortString() {
        return String.join("_", this.channels.stream().map(Channel::toShortName).toList());
    }

    public String toLongString() {
        return String.join(", ", this.channels.stream().map(Channel::toLongName).toList());
    }

    @Override
    public String toString() {
        return "ImageFormat{" + toLongString() + '}';
    }

    public static class Channel {

        private final String shortName;
        private final String longName;
        private final PrimitiveType type;

        public Channel(String shortName, String longName, PrimitiveType type) {
            this.shortName = shortName;
            this.longName = longName;
            this.type = type;
        }

        public String shortName() {
            return this.shortName;
        }

        public String longName() {
            return this.longName;
        }

        public PrimitiveType type() {
            return this.type;
        }

        public String toShortName() {
            return this.shortName + this.type.bits();
        }

        public String toLongName() {
            return this.longName + ":" + this.type.toLongName();
        }

        @Override
        public String toString() {
            return "Channel{" + this.toLongName() + '}';
        }

        public static Channel red(PrimitiveType type) {
            return new Channel("R", "Red", type);
        }

        public static Channel green(PrimitiveType type) {
            return new Channel("G", "Green", type);
        }

        public static Channel blue(PrimitiveType type) {
            return new Channel("B", "Blue", type);
        }

        public static Channel alpha(PrimitiveType type) {
            return new AlphaChannel("A", "Alpha", type);
        }

        public static Channel bt601(PrimitiveType type) {
            return new LumaChannel("BT.601", type);
        }

        public static List<Channel> rgba(PrimitiveType type) {
            return List.of(red(type), green(type), blue(type), alpha(type));
        }

        public static List<Channel> bgra(PrimitiveType type) {
            return List.of(blue(type), green(type), red(type), alpha(type));
        }

        public static List<Channel> rgb(PrimitiveType type) {
            return List.of(red(type), green(type), blue(type));
        }

        public static List<Channel> bgr(PrimitiveType type) {
            return List.of(blue(type), green(type), red(type));
        }

        public static List<Channel> bt601_a(PrimitiveType type) {
            return List.of(bt601(type), alpha(type));
        }
    }

    public static class AlphaChannel extends Channel {

        public AlphaChannel(String shortName, String longName, PrimitiveType type) {
            super(shortName, longName, type);
        }
    }

    public static class LumaChannel extends Channel {
        private final String variant;

        public LumaChannel(String variant, PrimitiveType type) {
            super("L", "Luma", type);
            this.variant = variant;
        }

        public String variant() {
            return this.variant;
        }
    }
}
