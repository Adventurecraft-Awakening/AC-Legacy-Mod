package dev.adventurecraft.awakening.script;

public interface ScriptColor {

    ScriptVec4 getColor();

    void setColor(ScriptVec4 value);

    @Deprecated
    default double getRed() {
        return this.getColor().getR();
    }

    @Deprecated
    default void setRed(double value) {
        this.getColor().setR(value);
    }

    @Deprecated
    default double getGreen() {
        return this.getColor().getG();
    }

    @Deprecated
    default void setGreen(double value) {
        this.getColor().setG(value);
    }

    @Deprecated
    default double getBlue() {
        return this.getColor().getB();
    }

    @Deprecated
    default void setBlue(double value) {
        this.getColor().setB(value);
    }

    @Deprecated
    default double getAlpha() {
        return this.getColor().getA();
    }

    @Deprecated
    default void setAlpha(double value) {
        this.getColor().setA(value);
    }
}
