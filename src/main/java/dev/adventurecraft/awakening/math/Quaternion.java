package dev.adventurecraft.awakening.math;

import dev.adventurecraft.awakening.util.MathF;

public class Quaternion implements Cloneable {

    public float w;
    public float x;
    public float y;
    public float z;

    public Quaternion() {
        this.w = 1;
    }

    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Quaternion identity() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 1;
        return this;
    }

    public Quaternion rotationX(float angle) {
        float sin = MathF.sin(angle * 0.5f);
        float cos = MathF.cosFromSin(sin, angle * 0.5f);
        return this.set(sin, 0, 0, cos);
    }

    public Quaternion rotationY(float angle) {
        float sin = MathF.sin(angle * 0.5f);
        float cos = MathF.cosFromSin(sin, angle * 0.5f);
        return this.set(0, sin, 0, cos);
    }

    public Quaternion rotationZ(float angle) {
        float sin = MathF.sin(angle * 0.5f);
        float cos = MathF.cosFromSin(sin, angle * 0.5f);
        return this.set(0, 0, sin, cos);
    }

    public Quaternion rotationXYZ(float angleX, float angleY, float angleZ) {
        float sx = MathF.sin(angleX * 0.5f);
        float cx = MathF.cosFromSin(sx, angleX * 0.5f);
        float sy = MathF.sin(angleY * 0.5f);
        float cy = MathF.cosFromSin(sy, angleY * 0.5f);
        float sz = MathF.sin(angleZ * 0.5f);
        float cz = MathF.cosFromSin(sz, angleZ * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        this.w = cx * cycz - sx * sysz;
        this.x = sx * cycz + cx * sysz;
        this.y = cx * sycz - sx * cysz;
        this.z = cx * cysz + sx * sycz;
        return this;
    }

    public @Override Quaternion clone() {
        try {
            return (Quaternion) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
