package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.util.MatrixUtil;
import dev.adventurecraft.awakening.util.ObjectUtil;
import org.lwjgl.util.vector.Matrix3f;

import java.util.Arrays;

public enum SymmetricGroup3 {

    P123(0, 1, 2),
    P213(1, 0, 2),
    P132(0, 2, 1),
    P231(1, 2, 0),
    P312(2, 0, 1),
    P321(2, 1, 0);

    private final int x;
    private final int y;
    private final int z;

    private static final int ORDER = 3;
    private static final SymmetricGroup3[][] CAYLEY_TABLE = ObjectUtil.make(
        new SymmetricGroup3[values().length][values().length], table -> {
            for (SymmetricGroup3 sg1 : values()) {
                for (SymmetricGroup3 sg2 : values()) {
                    int[] is = new int[ORDER];
                    for (int i = 0; i < is.length; i++) {
                        is[i] = sg1.permutation(sg2.permutation(i));
                    }

                    table[sg1.ordinal()][sg2.ordinal()] = Arrays
                        .stream(values())
                        .filter(g -> g.equals(is[0], is[1], is[2]))
                        .findFirst()
                        .orElseThrow();
                }
            }
        }
    );

    SymmetricGroup3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private boolean equals(int x, int y, int z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public SymmetricGroup3 compose(SymmetricGroup3 other) {
        return CAYLEY_TABLE[this.ordinal()][other.ordinal()];
    }

    public int permutation(int i)
        throws IndexOutOfBoundsException {
        return switch (i) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> throw new IndexOutOfBoundsException(i);
        };
    }

    public void storeTransform(Matrix3f matrix) {
        matrix.setZero();
        for (int i = 0; i < ORDER; i++) {
            MatrixUtil.set(matrix, this.permutation(i), i, 1.0F);
        }
    }
}
