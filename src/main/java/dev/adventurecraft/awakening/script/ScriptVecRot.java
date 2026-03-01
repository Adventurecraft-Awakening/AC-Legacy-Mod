package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.util.FacingUtil;

public class ScriptVecRot {

    public double yaw;
    public double pitch;

    public ScriptVecRot(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public int getFacing() {
        return FacingUtil.getFacing(this.yaw);
    }
}
