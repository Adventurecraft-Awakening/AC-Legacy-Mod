package dev.adventurecraft.awakening.extension.entity;

public interface ExLivingEntity extends ExEntity {

    boolean protectedByShield(double x, double y, double z);

    float getFov();

    public void setFov(float value);

    float getExtraFov();

    void setExtraFov(float value);

    double getGravity();

    int getMaxHealth();

    void setMaxHealth(int value);

    boolean getCanWallJump();

    void setCanWallJump(boolean value);

    int getTimesCanJumpInAir();

    void setTimesCanJumpInAir(int value);
}
