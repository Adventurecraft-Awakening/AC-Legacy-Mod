package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity implements ExLivingEntity {

    @Shadow
    protected float horizontalVelocity;
    @Shadow
    protected float forwardVelocity;
    @Shadow
    public boolean jumping;
    @Shadow
    protected float movementSpeed;

    public int maxHealth;
    public ItemStack heldItem;
    private long hurtTick;
    public int timesCanJumpInAir = 0;
    public int jumpsLeft = 0;
    public boolean canWallJump = false;
    private long tickBeforeNextJump;
    public double jumpVelocity = 0.42D;
    public double jumpWallMultiplier = 1.0D;
    public double jumpInAirMultiplier = 1.0D;
    public float airControl = 0.9259F;
    public double gravity = 0.08D;
    public float fov = 140.0F;
    public float extraFov = 0.0F;
    public boolean canLookRandomly = true;
    public float randomLookVelocity = 20.0F;
    public int randomLookNext = 0;
    public int randomLookRate = 100;
    public int randomLookRateVariation = 40;

    @Shadow
    protected void tickHandSwing() {
        throw new AssertionError();
    }

    @Shadow
    public abstract boolean method_928(Entity arg);

    @Shadow
    public abstract void lookAt(Entity arg, float f, float g);

    @Shadow
    public void writeAdditional(CompoundTag arg) {
        throw new AssertionError();
    }

    @Shadow
    public void readAdditional(CompoundTag arg) {
        throw new AssertionError();
    }

    @Override
    public int getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public void setMaxHealth(int value) {
        this.maxHealth = value;
    }

    @Override
    public boolean getCanWallJump() {
        return this.canWallJump;
    }

    @Override
    public void setCanWallJump(boolean value) {
        this.canWallJump = value;
    }

    @Override
    public int getTimesCanJumpInAir() {
        return this.timesCanJumpInAir;
    }

    @Override
    public void setTimesCanJumpInAir(int value) {
        this.timesCanJumpInAir = value;
    }

    @Override
    public float getFov() {
        return this.fov;
    }

    @Override
    public void setFov(float value) {
        this.fov = value;
    }

    @Override
    public float getExtraFov() {
        return this.extraFov;
    }

    @Override
    public void setExtraFov(float value) {
        this.extraFov = value;
    }
}
