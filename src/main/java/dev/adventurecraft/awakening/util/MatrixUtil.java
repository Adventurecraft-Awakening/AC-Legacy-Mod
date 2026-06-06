package dev.adventurecraft.awakening.util;

import org.lwjgl.util.vector.Matrix3f;

public final class MatrixUtil {

    public static void set(Matrix3f matrix, int column, int row, float value)
        throws IndexOutOfBoundsException {
        switch (row) {
            case 0 -> {
                switch (column) {
                    case 0 -> matrix.m00 = value;
                    case 1 -> matrix.m01 = value;
                    case 2 -> matrix.m02 = value;
                    default -> columnOutOfRange(column);
                }
            }
            case 1 -> {
                switch (column) {
                    case 0 -> matrix.m10 = value;
                    case 1 -> matrix.m11 = value;
                    case 2 -> matrix.m12 = value;
                    default -> columnOutOfRange(column);
                }
            }
            case 2 -> {
                switch (column) {
                    case 0 -> matrix.m20 = value;
                    case 1 -> matrix.m21 = value;
                    case 2 -> matrix.m22 = value;
                    default -> columnOutOfRange(column);
                }
            }
            default -> rowOutOfRange(row);
        }
    }

    private static void rowOutOfRange(int row) {
        throw new IndexOutOfBoundsException("Row out of range: " + row);
    }

    private static void columnOutOfRange(int column) {
        throw new IndexOutOfBoundsException("Column out of range: " + column);
    }
}
