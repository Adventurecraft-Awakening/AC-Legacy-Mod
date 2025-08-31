package dev.adventurecraft.awakening.script;

public class ScriptVecRot {

    public double yaw;
    public double pitch;

    public ScriptVecRot(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public int getFacing() {
        return ((int) Math.floor((yaw * 4.0F / 360.0F) + 0.5D) & 3);
    }
}
