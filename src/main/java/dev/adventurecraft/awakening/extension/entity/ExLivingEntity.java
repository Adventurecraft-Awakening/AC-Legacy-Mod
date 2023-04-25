package dev.adventurecraft.awakening.extension.entity;

public interface ExLivingEntity {

    int getMaxHealth();

    void setMaxHealth(int value);

    boolean getCanWallJump();

    void setCanWallJump(boolean value);

    int getTimesCanJumpInAir();

    void setTimesCanJumpInAir(int value);
}
