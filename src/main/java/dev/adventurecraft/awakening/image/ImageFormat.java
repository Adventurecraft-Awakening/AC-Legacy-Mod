package dev.adventurecraft.awakening.image;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;

public final class ImageFormat {

    public static final int UNDEFINED = 0;

    public static final int RGBA_U8 = 1;
    public static final int BGRA_U8 = 4;

    public static final int RGB_U8 = 2;
    public static final int BGR_U8 = 5;

    public static final int RGBA_U32 = 3;

    public static void check(int format) {
        if (channels(format) == 0) {
            throw new IllegalArgumentException("Unsupported image format: " + format);
        }
    }

    public static int fromStb(int channels, int bitDepth) {
        return switch (bitDepth) {
            case 8 -> switch (channels) {
                case 4 -> RGBA_U8;
                case 3 -> RGB_U8;
                default -> UNDEFINED;
            };
            case 32 -> switch (channels) {
                case 4 -> RGBA_U32;
                default -> UNDEFINED;
            };
            default -> UNDEFINED;
        };
    }

    public static int fromAwt(ColorModel model) {
        var colorSpace = model.getColorSpace();
        if (colorSpace.getType() != ColorSpace.TYPE_RGB) {
            return UNDEFINED;
        }

        int transferType = model.getTransferType();
        if (transferType != DataBuffer.TYPE_BYTE &&
            transferType != DataBuffer.TYPE_SHORT &&
            transferType != DataBuffer.TYPE_INT) {
            return UNDEFINED;
        }

        if (model instanceof DirectColorModel directModel) {
            int gMask = directModel.getGreenMask();
            if (gMask != 0xff00) {
                return UNDEFINED;
            }

            int rMask = directModel.getRedMask();
            int bMask = directModel.getBlueMask();
            boolean bgr;
            if (rMask == 0xff && bMask == 0xff0000) {
                bgr = false;
            } else if (rMask == 0xff0000 && bMask == 0xff) {
                bgr = true;
            } else {
                return UNDEFINED;
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
                return UNDEFINED;
            }

            int transparency = compModel.getTransparency();
            if (transparency == Transparency.OPAQUE) {
                return RGB_U8;
            } else if (transparency == Transparency.TRANSLUCENT) {
                return RGBA_U8;
            }
        }
        return UNDEFINED;
    }

    public static int bitDepth(int format) {
        return switch (format) {
            case RGBA_U8, BGRA_U8 -> 8;
            case RGB_U8 -> 8;

            case RGBA_U32 -> 32;

            default -> 0;
        };
    }

    public static int channels(int format) {
        return switch (format) {
            case RGBA_U8, BGRA_U8 -> 4;
            case RGBA_U32 -> 4;

            case RGB_U8 -> 3;

            default -> 0;
        };
    }

    public static boolean hasAlpha(int format) {
        return switch (format) {
            case RGBA_U8, BGRA_U8 -> true;
            case RGBA_U32 -> true;
            default -> false;
        };
    }

    public static long bitStride(int width, int format) {
        int bpp = bitDepth(format) * channels(format);
        return bpp * (long) width;
    }

    public static int byteStride(int width, int format) {
        long bytes = (bitStride(width, format) + 7) / 8;
        return (int) bytes;
    }
}
