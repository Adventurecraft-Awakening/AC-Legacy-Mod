package dev.adventurecraft.awakening.primitives;

public class FloatType extends PrimitiveType {

    public static final PrimitiveType F16 = new FloatType(16);
    public static final PrimitiveType F32 = new FloatType(32);
    public static final PrimitiveType F64 = new FloatType(64);

    public FloatType(int bits) {
        super(bits, true);
    }

    @Override
    public String toShortPrefix() {
        return "F";
    }

    @Override
    public String toLongPrefix() {
        return "Float";
    }
}
