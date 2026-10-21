package dev.adventurecraft.awakening.client.render.state;

/** Immutable column-major matrix. Native projections already produce Vulkan clip coordinates. */
public final class Matrix4 {
    private static final Matrix4 IDENTITY = new Matrix4(new float[]{1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1}, true);
    private final float[] values;
    private final int hash;
    public Matrix4(float[] values) { this(values, false); }
    private Matrix4(float[] values, boolean owned) {
        if (values.length != 16) throw new IllegalArgumentException("Expected a 4x4 matrix");
        this.values = owned ? values : values.clone();
        this.hash = java.util.Arrays.hashCode(this.values);
    }
    public static Matrix4 identity() { return IDENTITY; }
    public static Matrix4 of(float[] values) { return new Matrix4(values); }
    public float[] copyArray() { return values.clone(); }
    public float get(int index) { return values[index]; }
    public void copyTo(float[] target, int offset) { System.arraycopy(values, 0, target, offset, 16); }
    public Matrix4 multiply(Matrix4 right) {
        float[] result = new float[16];
        for (int column = 0; column < 4; column++) for (int row = 0; row < 4; row++) {
            for (int k = 0; k < 4; k++) result[column * 4 + row] += values[k * 4 + row] * right.values[column * 4 + k];
        }
        return new Matrix4(result, true);
    }
    public Matrix4 translated(float x, float y, float z) {
        float[] result = values.clone();
        for (int row = 0; row < 4; row++) result[12 + row] += values[row] * x + values[4 + row] * y + values[8 + row] * z;
        return new Matrix4(result, true);
    }
    public Matrix4 scaled(float x, float y, float z) {
        float[] result = values.clone();
        for (int row = 0; row < 4; row++) { result[row] *= x; result[4 + row] *= y; result[8 + row] *= z; }
        return new Matrix4(result, true);
    }
    public Matrix4 rotated(float degrees, float x, float y, float z) {
        float length = (float) Math.sqrt(x*x + y*y + z*z);
        if (length < 1.0e-12f) return this;
        x /= length; y /= length; z /= length;
        float angle = (float) Math.toRadians(degrees), c = (float) Math.cos(angle), s = (float) Math.sin(angle), t = 1-c;
        return multiply(new Matrix4(new float[]{t*x*x+c,t*x*y+s*z,t*x*z-s*y,0,t*x*y-s*z,t*y*y+c,t*y*z+s*x,0,t*x*z+s*y,t*y*z-s*x,t*z*z+c,0,0,0,0,1}, true));
    }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof Matrix4 matrix && java.util.Arrays.equals(this.values, matrix.values);
    }
    @Override public int hashCode() { return this.hash; }
    public static Matrix4 orthographicTopLeft(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Viewport size must be positive");
        return new Matrix4(new float[]{2f/width,0,0,0,0,2f/height,0,0,0,0,1,0,-1,-1,0,1}, true);
    }
}
