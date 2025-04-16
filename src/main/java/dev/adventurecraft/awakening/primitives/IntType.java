package dev.adventurecraft.awakening.primitives;

public class IntType extends PrimitiveType {

    public static final IntType U1 = new IntType(1, false);
    public static final IntType U2 = new IntType(2, false);
    public static final IntType U4 = new IntType(4, false);
    public static final IntType U8 = new IntType(8, false);
    public static final IntType U16 = new IntType(16, false);
    public static final IntType U32 = new IntType(32, false);
    public static final IntType U64 = new IntType(64, false);

    public static final IntType S1 = new IntType(1, true);
    public static final IntType S2 = new IntType(2, true);
    public static final IntType S4 = new IntType(4, true);
    public static final IntType S8 = new IntType(8, true);
    public static final IntType S16 = new IntType(16, true);
    public static final IntType S32 = new IntType(32, true);
    public static final IntType S64 = new IntType(64, true);

    public IntType(int bits, boolean signed) {
        super(bits, signed);
    }

    @Override
    public String toLongPrefix() {
        return this.signed() ? "Int" : "UInt";
    }
}
