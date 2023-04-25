package dev.adventurecraft.awakening.extension.entity;

public interface ExLivingEntity {

    float getFov();

    public void setFov(float value);

    float getExtraFov();

    void setExtraFov(float value);

    int getMaxHealth();

    void setMaxHealth(int value);

    boolean getCanWallJump();

    void setCanWallJump(boolean value);

    int getTimesCanJumpInAir();

    void setTimesCanJumpInAir(int value);
}
