package dev.adventurecraft.awakening.script;

public class ScriptVec4 {

    public double x;
    public double y;
    public double z;
    public double w;

    public ScriptVec4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public ScriptVec4(double value) {
        this.x = value;
        this.y = value;
        this.z = value;
        this.w = value;
    }

    public ScriptVec4(ScriptVec3 xyz, double w) {
        this.x = xyz.x;
        this.y = xyz.y;
        this.z = xyz.z;
        this.w = w;
    }

    public ScriptVec4 add(ScriptVec4 vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        this.w += vec.w;
        return this;
    }

    public ScriptVec4 subtract(ScriptVec4 vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        this.w -= vec.w;
        return this;
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w);
    }

    public double distance(ScriptVec4 vec) {
        double dX = this.x - vec.x;
        double dY = this.y - vec.y;
        double dZ = this.z - vec.z;
        double dW = this.w - vec.w;
        return Math.sqrt(dX * dX + dY * dY + dZ * dZ + dW * dW);
    }

    public ScriptVec4 scale(double value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
        return this;
    }

    @Override
    public ScriptVec4 clone() {
        return new ScriptVec4(this.x, this.y, this.z, this.w);
    }

    public ScriptVec3 getXyz() {
        return new ScriptVec3(this.x, this.y, this.z);
    }

    public void setXyz(ScriptVec3 values) {
        this.x = values.x;
        this.y = values.y;
        this.z = values.z;
    }

    public void setXyz(ScriptVec4 values) {
        this.x = values.x;
        this.y = values.y;
        this.z = values.z;
    }

    public double getR() {
        return this.x;
    }

    public void setR(double value) {
        this.x = value;
    }

    public double getG() {
        return this.y;
    }

    public void setG(double value) {
        this.y = value;
    }

    public double getB() {
        return this.z;
    }

    public void setB(double value) {
        this.z = value;
    }

    public double getA() {
        return this.w;
    }

    public void setA(double value) {
        this.w = value;
    }
}
