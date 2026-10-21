package dev.adventurecraft.awakening.client.render;

import java.nio.ByteBuffer;

/** One packed position/UV/RGBA/snorm-normal ABI shared by every geometry writer. */
public final class VertexFormat {
    public static final int STRIDE = 32;

    public static final int POSITION_OFFSET = 0;
    public static final int UV_OFFSET = 12;
    public static final int COLOR_OFFSET = 20;
    public static final int NORMAL_OFFSET = 24;
    public static final int MATERIAL_OFFSET = 28;
    public static final int PADDING_OFFSET = MATERIAL_OFFSET;
    public static final int THIN_FOLIAGE = 0x20000000;
    public static final int DECORATIVE_GRASS = 0x40000000;
    public static final int LEAF_VOLUME = 0x80000000;
    /** Reserved emission code31: a black, opaque light blocker with no raster surface. */
    public static final int SOLID_OCCLUDER = 0x1f000000;
    /** Flags in the normal word's high byte, above the valid skylight nibble. */
    public static final int WATER = 0x20;
    public static final int RAIN_EXPOSED = 0x40;
    public static final int TRANSMISSIVE = 0x80;

    private VertexFormat() { }

    public static void write(ByteBuffer target, int at, float x, float y, float z, float u, float v,
                             int rgba, int normal) {
        write(target, at, x, y, z, u, v, rgba, normal, 0);
    }

    /** Low 24 bits are unlit 0xRRGGBB tint; bits 24..28 hold emission + 1. Zero denotes a legacy vertex. */
    public static int packMaterial(int tint, int emission) {
        return (tint & 0xFFFFFF) | ((Math.clamp(emission, 0, 15) + 1) << 24);
    }

    /** Attaches supported surface flags without changing the tint or five-bit emission field. */
    public static int withFlags(int material, int flags) {
        int flagMask = THIN_FOLIAGE | DECORATIVE_GRASS | LEAF_VOLUME;
        int supported = flags & flagMask;
        if (material == 0 && supported != 0) material = packMaterial(0xFFFFFF, 0);
        return (material & ~flagMask) | supported;
    }

    public static void write(ByteBuffer target, int at, float x, float y, float z, float u, float v,
                             int rgba, int normal, int material) {
        write(target, at, x, y, z, u, v, rgba, normal, material, -1);
    }

    /** The high byte holds valid skylight in bits0..4 and water/rain flags in bits5..6. */
    public static void write(ByteBuffer target, int at, float x, float y, float z, float u, float v,
                             int rgba, int normal, int material, int skyLight) {
        write(target, at, x, y, z, u, v, rgba, normal, material, skyLight, 0);
    }

    public static void write(ByteBuffer target, int at, float x, float y, float z, float u, float v,
                             int rgba, int normal, int material, int skyLight, int surfaceFlags) {
        int positionOffset = at + POSITION_OFFSET;
        target.putFloat(positionOffset, x).putFloat(positionOffset + 4, y).putFloat(positionOffset + 8, z);
        int uvOffset = at + UV_OFFSET;
        target.putFloat(uvOffset, u).putFloat(uvOffset + 4, v);
        int colorOffset = at + COLOR_OFFSET;
        target.put(colorOffset, (byte) rgba).put(colorOffset + 1, (byte) (rgba >>> 8))
            .put(colorOffset + 2, (byte) (rgba >>> 16)).put(colorOffset + 3, (byte) (rgba >>> 24));
        int normalOffset = at + NORMAL_OFFSET;
        int packedNormal = normal & 0x00FFFFFF;
        if (skyLight >= 0) packedNormal |= (16 + Math.clamp(skyLight, 0, 15)) << 24;
        packedNormal |= (surfaceFlags & (WATER | RAIN_EXPOSED | TRANSMISSIVE)) << 24;
        target.put(normalOffset, (byte) packedNormal).put(normalOffset + 1, (byte) (packedNormal >>> 8))
            .put(normalOffset + 2, (byte) (packedNormal >>> 16)).put(normalOffset + 3, (byte) (packedNormal >>> 24));
        target.putInt(at + MATERIAL_OFFSET, material);
    }
}
