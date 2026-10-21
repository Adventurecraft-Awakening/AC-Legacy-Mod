package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.Matrix4;

/** Pure matrix inversion and homogeneous projection, independent of render state and API conventions. */
public final class RenderMath {
    private RenderMath() { }

    public static double[] transform(double[] m, double x, double y, double z) {
        double w = m[3] * x + m[7] * y + m[11] * z + m[15];
        if (Math.abs(w) < 1e-14) throw new IllegalStateException("Projection has zero W");
        return new double[] {(m[0] * x + m[4] * y + m[8] * z + m[12]) / w,
            (m[1] * x + m[5] * y + m[9] * z + m[13]) / w,
            (m[2] * x + m[6] * y + m[10] * z + m[14]) / w};
    }

    public static double[] inverse(Matrix4 matrix) {
        double[][] work = new double[4][8];
        for (int row = 0; row < 4; row++) for (int col = 0; col < 4; col++) {
            work[row][col] = matrix.get(col * 4 + row);
            work[row][col + 4] = row == col ? 1 : 0;
        }
        for (int col = 0; col < 4; col++) {
            int pivot = col;
            for (int row = col + 1; row < 4; row++) if (Math.abs(work[row][col]) > Math.abs(work[pivot][col])) pivot = row;
            if (Math.abs(work[pivot][col]) < 1e-14) throw new IllegalStateException("Singular camera matrix");
            double[] swap = work[pivot]; work[pivot] = work[col]; work[col] = swap;
            double scale = work[col][col];
            for (int j = 0; j < 8; j++) work[col][j] /= scale;
            for (int row = 0; row < 4; row++) if (row != col) {
                double factor = work[row][col];
                for (int j = 0; j < 8; j++) work[row][j] -= factor * work[col][j];
            }
        }
        double[] inverse = new double[16];
        for (int row = 0; row < 4; row++) for (int col = 0; col < 4; col++) inverse[col * 4 + row] = work[row][col + 4];
        return inverse;
    }
}
