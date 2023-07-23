package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import dev.adventurecraft.awakening.common.AC_UtilBullet;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("unused")
public class ScriptEntityLiving extends ScriptEntity {
    LivingEntity entityLiving;

    ScriptEntityLiving(LivingEntity var1) {
        super(var1);
        this.entityLiving = var1;
    }

    public void playLivingSound() {
        this.entityLiving.method_918();
    }

    public void spawnExplosionParticle() {
        this.entityLiving.onSpawnedFromSpawner();
    }

    public void heal(int var1) {
        this.entityLiving.addHealth(var1);
    }

    public boolean isOnLadder() {
        return this.entityLiving.method_932();
    }

    public ScriptEntity getLookTarget() {
        Entity var1 = this.entityLiving.getTarget();
        return ScriptEntity.getEntityClass(var1);
    }

    public void setLookTarget(ScriptEntity var1) {
        this.entityLiving.target = var1.entity;
    }

    public int getHealth() {
        return this.entityLiving.health;
    }

    public void setHealth(int var1) {
        this.entityLiving.health = var1;
    }

    public int getMaxHealth() {
        return ((ExLivingEntity) this.entityLiving).getMaxHealth();
    }

    public void setMaxHealth(int var1) {
        ((ExLivingEntity) this.entityLiving).setMaxHealth(var1);
    }

    public String getTexture() {
        return this.entityLiving.getTextured();
    }

    public void setTexture(String var1) {
        ((ExLivingEntity) this.entityLiving).setTexture(var1);
    }

    public int getHurtTime() {
        return this.entityLiving.hurtTime;
    }

    public int getHurtTimeResistance() {
        return this.entityLiving.field_1058;
    }

    public void setHurtTime(int var1) {
        this.entityLiving.hurtTime = var1;
    }

    public void setHurtTimeResistance(int var1) {
        this.entityLiving.field_1058 = var1;
    }

    public ScriptItem getHeldItem() {
        return new ScriptItem(((ExLivingEntity) this.entityLiving).getHeldItem());
    }

    public void setHeldItem(ScriptItem var1) {
        ((ExLivingEntity) this.entityLiving).setHeldItem(var1.item);
    }

    public float getMoveSpeed() {
        return ((ExLivingEntity) this.entityLiving).getMovementSpeed();
    }

    public void setMoveSpeed(float var1) {
        ((ExLivingEntity) this.entityLiving).setMovementSpeed(var1);
    }

    public int getTimesCanJumpInAir() {
        return ((ExLivingEntity) this.entityLiving).getTimesCanJumpInAir();
    }

    public void setTimesCanJumpInAir(int var1) {
        ((ExLivingEntity) this.entityLiving).setTimesCanJumpInAir(var1);
    }

    public boolean getCanWallJump() {
        return ((ExLivingEntity) this.entityLiving).getCanWallJump();
    }

    public void setCanWallJump(boolean var1) {
        ((ExLivingEntity) this.entityLiving).setCanWallJump(var1);
    }

    public int getJumpsInAirLeft() {
        return ((ExLivingEntity) this.entityLiving).getJumpsLeft();
    }

    public void setJumpsInAirLeft(int var1) {
        ((ExLivingEntity) this.entityLiving).setJumpsLeft(var1);
    }

    public double getGravity() {
        return ((ExLivingEntity) this.entityLiving).getGravity();
    }

    public void setGravity(double var1) {
        ((ExLivingEntity) this.entityLiving).setGravity(var1);
    }

    public double getJumpVelocity() {
        return ((ExLivingEntity) this.entityLiving).getJumpVelocity();
    }

    public void setJumpVelocity(double var1) {
        ((ExLivingEntity) this.entityLiving).setJumpVelocity(var1);
    }

    public double getJumpWallMultiplier() {
        return ((ExLivingEntity) this.entityLiving).getJumpWallMultiplier();
    }

    public void setJumpWallMultiplier(double var1) {
        ((ExLivingEntity) this.entityLiving).setJumpWallMultiplier(var1);
    }

    public double getJumpInAirMultiplier() {
        return ((ExLivingEntity) this.entityLiving).getJumpInAirMultiplier();
    }

    public void setJumpInAirMultiplier(double var1) {
        ((ExLivingEntity) this.entityLiving).setJumpInAirMultiplier(var1);
    }

    public boolean getShouldJump() {
        return this.entityLiving.jumping;
    }

    public void setShouldJump(boolean var1) {
        this.entityLiving.jumping = var1;
    }

    public float getAirControl() {
        return ((ExLivingEntity) this.entityLiving).getAirControl();
    }

    public void setAirControl(float var1) {
        ((ExLivingEntity) this.entityLiving).setAirControl(var1);
    }

    public void fireBullet(float var1, int var2) {
        AC_UtilBullet.fireBullet(this.entityLiving.world, this.entityLiving, var1, var2);
    }

    public float getFov() {
        return ((ExLivingEntity) this.entityLiving).getFov();
    }

    public void setFov(float var1) {
        ((ExLivingEntity) this.entityLiving).setFov(var1);
    }

    public boolean getCanLookRandomly() {
        return ((ExLivingEntity) this.entityLiving).getCanLookRandomly();
    }

    public void setCanLookRandomly(boolean var1) {
        ((ExLivingEntity) this.entityLiving).setCanLookRandomly(var1);
    }

    public float getRandomLookVelocity() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookVelocity();
    }

    public void setRandomLookVelocity(float var1) {
        ((ExLivingEntity) this.entityLiving).setRandomLookVelocity(var1);
    }

    public int getRandomLookNext() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookNext();
    }

    public void setRandomLookNext(int var1) {
        ((ExLivingEntity) this.entityLiving).setRandomLookNext(var1);
    }

    public int getRandomLookRate() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookRate();
    }

    public void setRandomLookRate(int var1) {
        ((ExLivingEntity) this.entityLiving).setRandomLookRate(var1);
    }

    public int getRandomLookRateVariation() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookRateVariation();
    }

    public void setRandomLookRateVariation(int var1) {
        ((ExLivingEntity) this.entityLiving).setRandomLookRateVariation(var1);
    }
}
