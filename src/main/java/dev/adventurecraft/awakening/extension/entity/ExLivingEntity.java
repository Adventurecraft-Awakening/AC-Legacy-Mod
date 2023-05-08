package dev.adventurecraft.awakening.extension.entity;

import net.minecraft.item.ItemStack;

public interface ExLivingEntity extends ExEntity {

    boolean protectedByShield(double x, double y, double z);

    float getFov();

    void setFov(float value);

    float getExtraFov();

    void setExtraFov(float value);

    double getGravity();

    void setGravity(double value);

    int getMaxHealth();

    void setMaxHealth(int value);

    ItemStack getHeldItem();

    void setHeldItem(ItemStack value);

    boolean getCanWallJump();

    void setCanWallJump(boolean value);

    int getTimesCanJumpInAir();

    void setTimesCanJumpInAir(int value);

    void setTexture(String value);


    float getMovementSpeed();

    void setMovementSpeed(float value);

    int getJumpsLeft();

    void setJumpsLeft(int value);

    double getJumpVelocity();

    void setJumpVelocity(double value);

    double getJumpWallMultiplier();

    void setJumpWallMultiplier(double value);

    double getJumpInAirMultiplier();

    void setJumpInAirMultiplier(double value);

    float getAirControl();

    void setAirControl(float value);

    boolean getCanLookRandomly();

    void setCanLookRandomly(boolean value);

    float getRandomLookVelocity();

    void setRandomLookVelocity(float value);

    int getRandomLookNext();

    void setRandomLookNext(int value);

    int getRandomLookRate();

    void setRandomLookRate(int value);

    int getRandomLookRateVariation();

    void setRandomLookRateVariation(int value);
}
