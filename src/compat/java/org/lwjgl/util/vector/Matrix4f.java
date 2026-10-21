package org.lwjgl.util.vector;

import java.io.Serializable;
import java.nio.FloatBuffer;

import static org.lwjgl.util.FastMath.*;

/**
 * Holds a 4x4 float matrix.
 *
 * @author foo
 */
public final class Matrix4f extends Matrix<Matrix4f> implements Serializable {

    private static final long serialVersionUID = 1L;

    public float m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33;

    /**
     * Construct a new matrix, initialized to the identity.
     */
    public Matrix4f() {
        super();
        setIdentity();
    }

    public Matrix4f(Matrix4f src) {
        super();
        load(src);
    }

    public Matrix4f(FloatBuffer src) {
        super();
        load(src);
    }

    /**
     * Returns a string representation of this matrix
     */
    public String toString() {
        var buf = new StringBuilder();
        buf.append(m00).append(' ').append(m10).append(' ').append(m20).append(' ').append(m30).append('\n');
        buf.append(m01).append(' ').append(m11).append(' ').append(m21).append(' ').append(m31).append('\n');
        buf.append(m02).append(' ').append(m12).append(' ').append(m22).append(' ').append(m32).append('\n');
        buf.append(m03).append(' ').append(m13).append(' ').append(m23).append(' ').append(m33).append('\n');
        return buf.toString();
    }

    /**
     * Set this matrix to be the identity matrix.
     *
     * @return this
     */
    public Matrix4f setIdentity() {
        return setIdentity(this);
    }

    /**
     * Set the given matrix to be the identity matrix.
     *
     * @param m The matrix to set to the identity
     * @return m
     */
    public static Matrix4f setIdentity(Matrix4f m) {
        m.m00 = 1.0f;
        m.m01 = 0.0f;
        m.m02 = 0.0f;
        m.m03 = 0.0f;
        m.m10 = 0.0f;
        m.m11 = 1.0f;
        m.m12 = 0.0f;
        m.m13 = 0.0f;
        m.m20 = 0.0f;
        m.m21 = 0.0f;
        m.m22 = 1.0f;
        m.m23 = 0.0f;
        m.m30 = 0.0f;
        m.m31 = 0.0f;
        m.m32 = 0.0f;
        m.m33 = 1.0f;
        return m;
    }

    /**
     * Set this matrix to 0.
     *
     * @return this
     */
    public Matrix4f setZero() {
        return setZero(this);
    }

    /**
     * Set the given matrix to 0.
     *
     * @param m The matrix to set to 0
     * @return m
     */
    public static Matrix4f setZero(Matrix4f m) {
        m.m00 = 0.0f;
        m.m01 = 0.0f;
        m.m02 = 0.0f;
        m.m03 = 0.0f;
        m.m10 = 0.0f;
        m.m11 = 0.0f;
        m.m12 = 0.0f;
        m.m13 = 0.0f;
        m.m20 = 0.0f;
        m.m21 = 0.0f;
        m.m22 = 0.0f;
        m.m23 = 0.0f;
        m.m30 = 0.0f;
        m.m31 = 0.0f;
        m.m32 = 0.0f;
        m.m33 = 0.0f;
        return m;
    }

    /**
     * Load from another matrix4f
     *
     * @param src The source matrix
     * @return this
     */
    public Matrix4f load(Matrix4f src) {
        return load(src, this);
    }

    /**
     * Copy the source matrix to the destination matrix
     *
     * @param src  The source matrix
     * @param dest The destination matrix, or null of a new one is to be created
     * @return The copied matrix
     */
    public static Matrix4f load(Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m02 = src.m02;
        dest.m03 = src.m03;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        dest.m12 = src.m12;
        dest.m13 = src.m13;
        dest.m20 = src.m20;
        dest.m21 = src.m21;
        dest.m22 = src.m22;
        dest.m23 = src.m23;
        dest.m30 = src.m30;
        dest.m31 = src.m31;
        dest.m32 = src.m32;
        dest.m33 = src.m33;
        return dest;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in column major
     * (OpenGL) order.
     *
     * @param buf A float buffer to read from
     * @return this
     */
    public Matrix4f load(FloatBuffer buf) {
        m00 = buf.get();
        m01 = buf.get();
        m02 = buf.get();
        m03 = buf.get();
        m10 = buf.get();
        m11 = buf.get();
        m12 = buf.get();
        m13 = buf.get();
        m20 = buf.get();
        m21 = buf.get();
        m22 = buf.get();
        m23 = buf.get();
        m30 = buf.get();
        m31 = buf.get();
        m32 = buf.get();
        m33 = buf.get();
        return this;
    }

    /**
     * Load from a float buffer. The buffer stores the matrix in row major
     * (maths) order.
     *
     * @param buf A float buffer to read from
     * @return this
     */
    public Matrix4f loadTranspose(FloatBuffer buf) {
        m00 = buf.get();
        m10 = buf.get();
        m20 = buf.get();
        m30 = buf.get();
        m01 = buf.get();
        m11 = buf.get();
        m21 = buf.get();
        m31 = buf.get();
        m02 = buf.get();
        m12 = buf.get();
        m22 = buf.get();
        m32 = buf.get();
        m03 = buf.get();
        m13 = buf.get();
        m23 = buf.get();
        m33 = buf.get();
        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in column
     * major (openGL) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix4f store(FloatBuffer buf) {
        buf.put(m00);
        buf.put(m01);
        buf.put(m02);
        buf.put(m03);
        buf.put(m10);
        buf.put(m11);
        buf.put(m12);
        buf.put(m13);
        buf.put(m20);
        buf.put(m21);
        buf.put(m22);
        buf.put(m23);
        buf.put(m30);
        buf.put(m31);
        buf.put(m32);
        buf.put(m33);
        return this;
    }

    /**
     * Store this matrix in a float buffer. The matrix is stored in row
     * major (maths) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix4f storeTranspose(FloatBuffer buf) {
        buf.put(m00);
        buf.put(m10);
        buf.put(m20);
        buf.put(m30);
        buf.put(m01);
        buf.put(m11);
        buf.put(m21);
        buf.put(m31);
        buf.put(m02);
        buf.put(m12);
        buf.put(m22);
        buf.put(m32);
        buf.put(m03);
        buf.put(m13);
        buf.put(m23);
        buf.put(m33);
        return this;
    }

    /**
     * Store the rotation portion of this matrix in a float buffer. The matrix is stored in column
     * major (openGL) order.
     *
     * @param buf The buffer to store this matrix in
     */
    public Matrix4f store3f(FloatBuffer buf) {
        buf.put(m00);
        buf.put(m01);
        buf.put(m02);
        buf.put(m10);
        buf.put(m11);
        buf.put(m12);
        buf.put(m20);
        buf.put(m21);
        buf.put(m22);
        return this;
    }

    /**
     * Add two matrices together and place the result in a third matrix.
     *
     * @param left  The left source matrix
     * @param right The right source matrix
     * @param dest  The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f add(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();

        dest.m00 = left.m00 + right.m00;
        dest.m01 = left.m01 + right.m01;
        dest.m02 = left.m02 + right.m02;
        dest.m03 = left.m03 + right.m03;
        dest.m10 = left.m10 + right.m10;
        dest.m11 = left.m11 + right.m11;
        dest.m12 = left.m12 + right.m12;
        dest.m13 = left.m13 + right.m13;
        dest.m20 = left.m20 + right.m20;
        dest.m21 = left.m21 + right.m21;
        dest.m22 = left.m22 + right.m22;
        dest.m23 = left.m23 + right.m23;
        dest.m30 = left.m30 + right.m30;
        dest.m31 = left.m31 + right.m31;
        dest.m32 = left.m32 + right.m32;
        dest.m33 = left.m33 + right.m33;
        return dest;
    }

    /**
     * Subtract the right matrix from the left and place the result in a third matrix.
     *
     * @param left  The left source matrix
     * @param right The right source matrix
     * @param dest  The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f sub(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();

        dest.m00 = left.m00 - right.m00;
        dest.m01 = left.m01 - right.m01;
        dest.m02 = left.m02 - right.m02;
        dest.m03 = left.m03 - right.m03;
        dest.m10 = left.m10 - right.m10;
        dest.m11 = left.m11 - right.m11;
        dest.m12 = left.m12 - right.m12;
        dest.m13 = left.m13 - right.m13;
        dest.m20 = left.m20 - right.m20;
        dest.m21 = left.m21 - right.m21;
        dest.m22 = left.m22 - right.m22;
        dest.m23 = left.m23 - right.m23;
        dest.m30 = left.m30 - right.m30;
        dest.m31 = left.m31 - right.m31;
        dest.m32 = left.m32 - right.m32;
        dest.m33 = left.m33 - right.m33;
        return dest;
    }

    /**
     * Multiply the right matrix by the left and place the result in a third matrix.
     *
     * @param left  The left source matrix
     * @param right The right source matrix
     * @param dest  The destination matrix, or null if a new one is to be created
     * @return the destination matrix
     */
    public static Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();

        float l00 = left.m00, l01 = left.m01, l02 = left.m02, l03 = left.m03;
        float l10 = left.m10, l11 = left.m11, l12 = left.m12, l13 = left.m13;
        float l20 = left.m20, l21 = left.m21, l22 = left.m22, l23 = left.m23;
        float l30 = left.m30, l31 = left.m31, l32 = left.m32, l33 = left.m33;

        float r00 = right.m00, r01 = right.m01, r02 = right.m02, r03 = right.m03;
        float r10 = right.m10, r11 = right.m11, r12 = right.m12, r13 = right.m13;
        float r20 = right.m20, r21 = right.m21, r22 = right.m22, r23 = right.m23;
        float r30 = right.m30, r31 = right.m31, r32 = right.m32, r33 = right.m33;

        float m00 = mulAdd(l00, r00, mulAdd(l10, r01, mulAdd(l20, r02, l30 * r03)));
        float m01 = mulAdd(l01, r00, mulAdd(l11, r01, mulAdd(l21, r02, l31 * r03)));
        float m02 = mulAdd(l02, r00, mulAdd(l12, r01, mulAdd(l22, r02, l32 * r03)));
        float m03 = mulAdd(l03, r00, mulAdd(l13, r01, mulAdd(l23, r02, l33 * r03)));
        float m10 = mulAdd(l00, r10, mulAdd(l10, r11, mulAdd(l20, r12, l30 * r13)));
        float m11 = mulAdd(l01, r10, mulAdd(l11, r11, mulAdd(l21, r12, l31 * r13)));
        float m12 = mulAdd(l02, r10, mulAdd(l12, r11, mulAdd(l22, r12, l32 * r13)));
        float m13 = mulAdd(l03, r10, mulAdd(l13, r11, mulAdd(l23, r12, l33 * r13)));
        float m20 = mulAdd(l00, r20, mulAdd(l10, r21, mulAdd(l20, r22, l30 * r23)));
        float m21 = mulAdd(l01, r20, mulAdd(l11, r21, mulAdd(l21, r22, l31 * r23)));
        float m22 = mulAdd(l02, r20, mulAdd(l12, r21, mulAdd(l22, r22, l32 * r23)));
        float m23 = mulAdd(l03, r20, mulAdd(l13, r21, mulAdd(l23, r22, l33 * r23)));
        float m30 = mulAdd(l00, r30, mulAdd(l10, r31, mulAdd(l20, r32, l30 * r33)));
        float m31 = mulAdd(l01, r30, mulAdd(l11, r31, mulAdd(l21, r32, l31 * r33)));
        float m32 = mulAdd(l02, r30, mulAdd(l12, r31, mulAdd(l22, r32, l32 * r33)));
        float m33 = mulAdd(l03, r30, mulAdd(l13, r31, mulAdd(l23, r32, l33 * r33)));

        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m03 = m03;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m13 = m13;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m23 = m23;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.m33 = m33;
        return dest;
    }

    /**
     * Transform a Vector by a matrix and return the result in a destination
     * vector.
     *
     * @param left  The left matrix
     * @param right The right vector
     * @param dest  The destination vector, or null if a new one is to be created
     * @return the destination vector
     */
    public static Vector4f transform(Matrix4f left, Vector4f right, Vector4f dest) {
        if (dest == null)
            dest = new Vector4f();
        return transform(left, right.x, right.y, right.z, right.w, dest);
    }

    public static Vector4f transform(Matrix4f left, float x, float y, float z, float w, Vector4f dest) {
        dest.x = mulAdd(left.m00, x, mulAdd(left.m10, y, mulAdd(left.m20, z, left.m30 * w)));
        dest.y = mulAdd(left.m01, x, mulAdd(left.m11, y, mulAdd(left.m21, z, left.m31 * w)));
        dest.z = mulAdd(left.m02, x, mulAdd(left.m12, y, mulAdd(left.m22, z, left.m32 * w)));
        dest.w = mulAdd(left.m03, x, mulAdd(left.m13, y, mulAdd(left.m23, z, left.m33 * w)));
        return dest;
    }

    public static Vector3f transform(Matrix4f left, Vector3f right, Vector3f dest) {
        if (dest == null)
            dest = new Vector3f();
        return transform(left, right.x, right.y, right.z, dest);
    }

    public static Vector3f transform(Matrix4f left, float x, float y, float z, Vector3f dest) {
        dest.x = mulAdd(left.m00, x, mulAdd(left.m10, y, mulAdd(left.m20, z, left.m30)));
        dest.y = mulAdd(left.m01, x, mulAdd(left.m11, y, mulAdd(left.m21, z, left.m31)));
        dest.z = mulAdd(left.m02, x, mulAdd(left.m12, y, mulAdd(left.m22, z, left.m32)));
        return dest;
    }

    public static Vector2f transform(Matrix4f left, Vector2f right, Vector2f dest) {
        if (dest == null)
            dest = new Vector2f();
        return transform(left, right.x, right.y, dest);
    }

    public static Vector2f transform(Matrix4f left, float x, float y, Vector2f dest) {
        float dx = mulAdd(left.m00, x, mulAdd(left.m10, y, left.m30));
        float dy = mulAdd(left.m01, x, mulAdd(left.m11, y, left.m31));

        dest.x = dx;
        dest.y = dy;
        return dest;
    }

    /**
     * Transpose this matrix
     *
     * @return this
     */
    public Matrix4f transpose() {
        return transpose(this);
    }

    /**
     * Translate this matrix
     *
     * @param vec The vector to translate by
     * @return this
     */
    public Matrix4f translate(Vector2f vec) {
        return translate(vec, this);
    }

    /**
     * Translate this matrix
     *
     * @param vec The vector to translate by
     * @return this
     */
    public Matrix4f translate(Vector3f vec) {
        return translate(vec, this);
    }

    public Matrix4f translate(float x, float y, float z) {
        return translate(x, y, z, this, this);
    }

    /**
     * Scales this matrix
     *
     * @param vec The vector to scale by
     * @return this
     */
    public Matrix4f scale(Vector3f vec) {
        return scale(vec, this, this);
    }

    /**
     * Scales the source matrix and put the result in the destination matrix
     *
     * @param vec  The vector to scale by
     * @param src  The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be created
     * @return The scaled matrix
     */
    public static Matrix4f scale(Vector3f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        return scale(vec.x, vec.y, vec.z, src, dest);
    }

    public static Matrix4f scale(float x, float y, float z, Matrix4f src, Matrix4f dest) {
        dest.m00 = src.m00 * x;
        dest.m01 = src.m01 * x;
        dest.m02 = src.m02 * x;
        dest.m03 = src.m03 * x;
        dest.m10 = src.m10 * y;
        dest.m11 = src.m11 * y;
        dest.m12 = src.m12 * y;
        dest.m13 = src.m13 * y;
        dest.m20 = src.m20 * z;
        dest.m21 = src.m21 * z;
        dest.m22 = src.m22 * z;
        dest.m23 = src.m23 * z;
        return dest;
    }

    public Matrix4f scale(float x, float y, float z) {
        return scale(x, y, z, this, this);
    }

    public static Matrix4f scaleX(float x, Matrix4f src, Matrix4f dest) {
        dest.m00 = src.m00 * x;
        dest.m01 = src.m01 * x;
        dest.m02 = src.m02 * x;
        dest.m03 = src.m03 * x;
        return dest;
    }

    public Matrix4f scaleX(float x) {
        return scaleX(x, this, this);
    }

    public static Matrix4f scaleY(float y, Matrix4f src, Matrix4f dest) {
        dest.m10 = src.m10 * y;
        dest.m11 = src.m11 * y;
        dest.m12 = src.m12 * y;
        dest.m13 = src.m13 * y;
        return dest;
    }

    public Matrix4f scaleY(float y) {
        return scaleY(y, this, this);
    }

    public static Matrix4f scaleZ(float z, Matrix4f src, Matrix4f dest) {
        dest.m20 = src.m20 * z;
        dest.m21 = src.m21 * z;
        dest.m22 = src.m22 * z;
        dest.m23 = src.m23 * z;
        return dest;
    }

    public Matrix4f scaleZ(float z) {
        return scaleZ(z, this, this);
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle the angle, in radians.
     * @param axis  The vector representing the rotation axis. Must be normalized.
     * @return this
     */
    public Matrix4f rotate(float angle, Vector3f axis) {
        return rotate(angle, axis, this);
    }

    /**
     * Rotates the matrix around the given axis the specified angle
     *
     * @param angle the angle, in radians.
     * @param axis  The vector representing the rotation axis. Must be normalized.
     * @param dest  The matrix to put the result, or null if a new matrix is to be created
     * @return The rotated matrix
     */
    public Matrix4f rotate(float angle, Vector3f axis, Matrix4f dest) {
        return rotate(angle, axis, this, dest);
    }

    /**
     * Rotates the source matrix around the given axis the specified angle and
     * put the result in the destination matrix.
     *
     * @param angle the angle, in radians.
     * @param axis  The vector representing the rotation axis. Must be normalized.
     * @param src   The matrix to rotate
     * @param dest  The matrix to put the result, or null if a new matrix is to be created
     * @return The rotated matrix
     */
    public static Matrix4f rotate(float angle, Vector3f axis, Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        return rotate(angle, axis.x, axis.y, axis.z, src, dest);
    }

    public static Matrix4f rotate(float angle, float x, float y, float z, Matrix4f src, Matrix4f dest) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);
        float oneminusc = 1.0f - c;
        float xy = x * y;
        float yz = y * z;
        float xz = x * z;
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;

        float f00 = mulAdd(x * x, oneminusc, c);
        float f01 = mulAdd(xy, oneminusc, zs);
        float f02 = mulSub(xz, oneminusc, ys);
        // n[3] not used
        float f10 = mulSub(xy, oneminusc, zs);
        float f11 = mulAdd(y * y, oneminusc, c);
        float f12 = mulAdd(yz, oneminusc, xs);
        // n[7] not used
        float f20 = mulAdd(xz, oneminusc, ys);
        float f21 = mulSub(yz, oneminusc, xs);
        float f22 = mulAdd(z * z, oneminusc, c);

        float s00 = src.m00, s01 = src.m01, s02 = src.m02, s03 = src.m03;
        float s10 = src.m10, s11 = src.m11, s12 = src.m12, s13 = src.m13;
        float s20 = src.m20, s21 = src.m21, s22 = src.m22, s23 = src.m23;

        float t00 = mulAdd(s00, f00, mulAdd(s10, f01, s20 * f02));
        float t01 = mulAdd(s01, f00, mulAdd(s11, f01, s21 * f02));
        float t02 = mulAdd(s02, f00, mulAdd(s12, f01, s22 * f02));
        float t03 = mulAdd(s03, f00, mulAdd(s13, f01, s23 * f02));
        float t10 = mulAdd(s00, f10, mulAdd(s10, f11, s20 * f12));
        float t11 = mulAdd(s01, f10, mulAdd(s11, f11, s21 * f12));
        float t12 = mulAdd(s02, f10, mulAdd(s12, f11, s22 * f12));
        float t13 = mulAdd(s03, f10, mulAdd(s13, f11, s23 * f12));
        dest.m20 = mulAdd(s00, f20, mulAdd(s10, f21, s20 * f22));
        dest.m21 = mulAdd(s01, f20, mulAdd(s11, f21, s21 * f22));
        dest.m22 = mulAdd(s02, f20, mulAdd(s12, f21, s22 * f22));
        dest.m23 = mulAdd(s03, f20, mulAdd(s13, f21, s23 * f22));
        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        return dest;
    }

    public static Matrix4f rotateX(float angle, Matrix4f src, Matrix4f dest) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        float s10 = src.m10, s11 = src.m11, s12 = src.m12, s13 = src.m13;
        float s20 = src.m20, s21 = src.m21, s22 = src.m22, s23 = src.m23;

        float t10 = mulAdd(s10, c, s20 * s);
        float t11 = mulAdd(s11, c, s21 * s);
        float t12 = mulAdd(s12, c, s22 * s);
        float t13 = mulAdd(s13, c, s23 * s);
        dest.m20 = negMulAdd(s10, s, s20 * c);
        dest.m21 = negMulAdd(s11, s, s21 * c);
        dest.m22 = negMulAdd(s12, s, s22 * c);
        dest.m23 = negMulAdd(s13, s, s23 * c);
        dest.m00 = src.m00;
        dest.m01 = src.m01;
        dest.m02 = src.m02;
        dest.m03 = src.m03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        return dest;
    }

    public Matrix4f rotateX(float angle) {
        return rotateX(angle, this, this);
    }

    public static Matrix4f rotateY(float angle, Matrix4f src, Matrix4f dest) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        float s00 = src.m00, s01 = src.m01, s02 = src.m02, s03 = src.m03;
        float s20 = src.m20, s21 = src.m21, s22 = src.m22, s23 = src.m23;

        float t00 = mulSub(s00, c, s20 * s);
        float t01 = mulSub(s01, c, s21 * s);
        float t02 = mulSub(s02, c, s22 * s);
        float t03 = mulSub(s03, c, s23 * s);
        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = src.m10;
        dest.m11 = src.m11;
        dest.m12 = src.m12;
        dest.m13 = src.m13;
        dest.m20 = mulAdd(s00, s, s20 * c);
        dest.m21 = mulAdd(s01, s, s21 * c);
        dest.m22 = mulAdd(s02, s, s22 * c);
        dest.m23 = mulAdd(s03, s, s23 * c);
        return dest;
    }

    public Matrix4f rotateY(float angle) {
        return rotateY(angle, this, this);
    }

    public static Matrix4f rotateZ(float angle, Matrix4f src, Matrix4f dest) {
        float c = (float) Math.cos(angle);
        float s = (float) Math.sin(angle);

        float s00 = src.m00, s01 = src.m01, s02 = src.m02, s03 = src.m03;
        float s10 = src.m10, s11 = src.m11, s12 = src.m12, s13 = src.m13;

        float t00 = mulAdd(s00, c, s10 * s);
        float t01 = mulAdd(s01, c, s11 * s);
        float t02 = mulAdd(s02, c, s12 * s);
        float t03 = mulAdd(s03, c, s13 * s);
        float t10 = negMulAdd(s00, s, s10 * c);
        float t11 = negMulAdd(s01, s, s11 * c);
        float t12 = negMulAdd(s02, s, s12 * c);
        float t13 = negMulAdd(s03, s, s13 * c);
        dest.m00 = t00;
        dest.m01 = t01;
        dest.m02 = t02;
        dest.m03 = t03;
        dest.m10 = t10;
        dest.m11 = t11;
        dest.m12 = t12;
        dest.m13 = t13;
        dest.m20 = src.m20;
        dest.m21 = src.m21;
        dest.m22 = src.m22;
        dest.m23 = src.m23;
        return dest;
    }

    public Matrix4f rotateZ(float angle) {
        return rotateZ(angle, this, this);
    }

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec  The vector to translate by
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return the translated matrix
     */
    public Matrix4f translate(Vector3f vec, Matrix4f dest) {
        return translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination matrix
     *
     * @param vec  The vector to translate by
     * @param src  The source matrix
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return The translated matrix
     */
    public static Matrix4f translate(Vector3f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        return translate(vec.x, vec.y, vec.z, src, dest);
    }

    public static Matrix4f translate(float x, float y, float z, Matrix4f src, Matrix4f dest) {
        dest.m30 = mulAdd(src.m00, x, mulAdd(src.m10, y, mulAdd(src.m20, z, dest.m30)));
        dest.m31 = mulAdd(src.m01, x, mulAdd(src.m11, y, mulAdd(src.m21, z, dest.m31)));
        dest.m32 = mulAdd(src.m02, x, mulAdd(src.m12, y, mulAdd(src.m22, z, dest.m32)));
        dest.m33 = mulAdd(src.m03, x, mulAdd(src.m13, y, mulAdd(src.m23, z, dest.m33)));
        return dest;
    }

    /**
     * Translate this matrix and stash the result in another matrix
     *
     * @param vec  The vector to translate by
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return the translated matrix
     */
    public Matrix4f translate(Vector2f vec, Matrix4f dest) {
        return translate(vec, this, dest);
    }

    /**
     * Translate the source matrix and stash the result in the destination matrix
     *
     * @param vec  The vector to translate by
     * @param src  The source matrix
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return The translated matrix
     */
    public static Matrix4f translate(Vector2f vec, Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        return translate(vec.x, vec.y, src, dest);
    }

    public static Matrix4f translate(float x, float y, Matrix4f src, Matrix4f dest) {
        dest.m30 = mulAdd(src.m00, x, mulAdd(src.m10, y, dest.m30));
        dest.m31 = mulAdd(src.m01, x, mulAdd(src.m11, y, dest.m31));
        dest.m32 = mulAdd(src.m02, x, mulAdd(src.m12, y, dest.m32));
        dest.m33 = mulAdd(src.m03, x, mulAdd(src.m13, y, dest.m33));
        return dest;
    }

    /**
     * Transpose this matrix and place the result in another matrix
     *
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return the transposed matrix
     */
    public Matrix4f transpose(Matrix4f dest) {
        return transpose(this, dest);
    }

    /**
     * Transpose the source matrix and place the result in the destination matrix
     *
     * @param src  The source matrix
     * @param dest The destination matrix or null if a new matrix is to be created
     * @return the transposed matrix
     */
    public static Matrix4f transpose(Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();
        float m00 = src.m00;
        float m01 = src.m10;
        float m02 = src.m20;
        float m03 = src.m30;
        float m10 = src.m01;
        float m11 = src.m11;
        float m12 = src.m21;
        float m13 = src.m31;
        float m20 = src.m02;
        float m21 = src.m12;
        float m22 = src.m22;
        float m23 = src.m32;
        float m30 = src.m03;
        float m31 = src.m13;
        float m32 = src.m23;
        float m33 = src.m33;

        dest.m00 = m00;
        dest.m01 = m01;
        dest.m02 = m02;
        dest.m03 = m03;
        dest.m10 = m10;
        dest.m11 = m11;
        dest.m12 = m12;
        dest.m13 = m13;
        dest.m20 = m20;
        dest.m21 = m21;
        dest.m22 = m22;
        dest.m23 = m23;
        dest.m30 = m30;
        dest.m31 = m31;
        dest.m32 = m32;
        dest.m33 = m33;
        return dest;
    }

    /**
     * @return the determinant of the matrix
     */
    public float determinant() {
        float a = this.m00, b = this.m01, c = this.m02, d = this.m03;
        float e = this.m10, f = this.m11, g = this.m12, h = this.m13;
        float i = this.m20, j = this.m21, k = this.m22, l = this.m23;
        float m = this.m30, n = this.m31, o = this.m32, p = this.m33;

        float kp_lo = mulSub(k, p, l * o);
        float jp_ln = mulSub(j, p, l * n);
        float jo_kn = mulSub(j, o, k * n);
        float ip_lm = mulSub(i, p, l * m);
        float io_km = mulSub(i, o, k * m);
        float in_jm = mulSub(i, n, j * m);

        return a * mulAdd(f, kp_lo, negMulAdd(g, jp_ln, h * jo_kn)) -
            b * mulAdd(e, kp_lo, negMulAdd(g, ip_lm, h * io_km)) +
            c * mulAdd(e, jp_ln, negMulAdd(f, ip_lm, h * in_jm)) -
            d * mulAdd(e, jo_kn, negMulAdd(f, io_km, g * in_jm));
    }

    /**
     * Calculate the determinant of a 3x3 matrix
     *
     * @return result
     */
    private static float determinant3x3(
        float t00, float t01, float t02,
        float t10, float t11, float t12,
        float t20, float t21, float t22) {
        float m0 = t00 * mulSub(t11, t22, t12 * t21);
        float m1 = t01 * mulSub(t12, t20, t10 * t22);
        float m2 = t02 * mulSub(t10, t21, t11 * t20);
        return m0 + m1 + m2;
    }

    /**
     * Invert this matrix
     *
     * @return this if successful, null otherwise
     */
    public Matrix4f invert() {
        return invert(this, this);
    }

    /**
     * Invert the source matrix and put the result in the destination
     *
     * @param src  The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be created
     * @return The inverted matrix if successful, null otherwise
     */
    public static Matrix4f invert(Matrix4f src, Matrix4f dest) {
        float determinant = src.determinant();
        if (determinant == 0) {
            return null;
        }

        if (dest == null)
            dest = new Matrix4f();
        float determinant_inv = 1f / determinant;

        float s00 = src.m00, s01 = src.m01, s02 = src.m02, s03 = src.m03;
        float s10 = src.m10, s11 = src.m11, s12 = src.m12, s13 = src.m13;
        float s20 = src.m20, s21 = src.m21, s22 = src.m22, s23 = src.m23;
        float s30 = src.m30, s31 = src.m31, s32 = src.m32, s33 = src.m33;

        // first row
        float t00 = determinant3x3(s11, s12, s13, s21, s22, s23, s31, s32, s33);
        float t01 = -determinant3x3(s10, s12, s13, s20, s22, s23, s30, s32, s33);
        float t02 = determinant3x3(s10, s11, s13, s20, s21, s23, s30, s31, s33);
        float t03 = -determinant3x3(s10, s11, s12, s20, s21, s22, s30, s31, s32);
        // second row
        float t10 = -determinant3x3(s01, s02, s03, s21, s22, s23, s31, s32, s33);
        float t11 = determinant3x3(s00, s02, s03, s20, s22, s23, s30, s32, s33);
        float t12 = -determinant3x3(s00, s01, s03, s20, s21, s23, s30, s31, s33);
        float t13 = determinant3x3(s00, s01, s02, s20, s21, s22, s30, s31, s32);
        // third row
        float t20 = determinant3x3(s01, s02, s03, s11, s12, s13, s31, s32, s33);
        float t21 = -determinant3x3(s00, s02, s03, s10, s12, s13, s30, s32, s33);
        float t22 = determinant3x3(s00, s01, s03, s10, s11, s13, s30, s31, s33);
        float t23 = -determinant3x3(s00, s01, s02, s10, s11, s12, s30, s31, s32);
        // fourth row
        float t30 = -determinant3x3(s01, s02, s03, s11, s12, s13, s21, s22, s23);
        float t31 = determinant3x3(s00, s02, s03, s10, s12, s13, s20, s22, s23);
        float t32 = -determinant3x3(s00, s01, s03, s10, s11, s13, s20, s21, s23);
        float t33 = determinant3x3(s00, s01, s02, s10, s11, s12, s20, s21, s22);

        // transpose and divide by the determinant
        dest.m00 = t00 * determinant_inv;
        dest.m11 = t11 * determinant_inv;
        dest.m22 = t22 * determinant_inv;
        dest.m33 = t33 * determinant_inv;
        dest.m01 = t10 * determinant_inv;
        dest.m10 = t01 * determinant_inv;
        dest.m20 = t02 * determinant_inv;
        dest.m02 = t20 * determinant_inv;
        dest.m12 = t21 * determinant_inv;
        dest.m21 = t12 * determinant_inv;
        dest.m03 = t30 * determinant_inv;
        dest.m30 = t03 * determinant_inv;
        dest.m13 = t31 * determinant_inv;
        dest.m31 = t13 * determinant_inv;
        dest.m32 = t23 * determinant_inv;
        dest.m23 = t32 * determinant_inv;
        return dest;
    }

    /**
     * Negate this matrix
     *
     * @return this
     */
    public Matrix4f negate() {
        return negate(this);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param dest The destination matrix, or null if a new matrix is to be created
     * @return the negated matrix
     */
    public Matrix4f negate(Matrix4f dest) {
        return negate(this, dest);
    }

    /**
     * Negate this matrix and place the result in a destination matrix.
     *
     * @param src  The source matrix
     * @param dest The destination matrix, or null if a new matrix is to be created
     * @return The negated matrix
     */
    public static Matrix4f negate(Matrix4f src, Matrix4f dest) {
        if (dest == null)
            dest = new Matrix4f();

        dest.m00 = -src.m00;
        dest.m01 = -src.m01;
        dest.m02 = -src.m02;
        dest.m03 = -src.m03;
        dest.m10 = -src.m10;
        dest.m11 = -src.m11;
        dest.m12 = -src.m12;
        dest.m13 = -src.m13;
        dest.m20 = -src.m20;
        dest.m21 = -src.m21;
        dest.m22 = -src.m22;
        dest.m23 = -src.m23;
        dest.m30 = -src.m30;
        dest.m31 = -src.m31;
        dest.m32 = -src.m32;
        dest.m33 = -src.m33;
        return dest;
    }
}