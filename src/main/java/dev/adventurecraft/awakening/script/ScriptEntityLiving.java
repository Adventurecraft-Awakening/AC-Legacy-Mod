package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import dev.adventurecraft.awakening.common.AC_UtilBullet;

@SuppressWarnings("unused")
public class ScriptEntityLiving extends ScriptEntity {

    LivingEntity entityLiving;

    ScriptEntityLiving(LivingEntity entity) {
        super(entity);
        this.entityLiving = entity;
    }

    public boolean isTouchingWater(){
        return entityLiving.isInWaterOrRain();
    }

    public void playLivingSound() {
        this.entityLiving.playAmbientSound();
    }

    public void spawnExplosionParticle() {
        this.entityLiving.spawnAnim();
    }

    public void heal(int health) {
        this.entityLiving.heal(health);
    }

    public boolean isOnLadder() {
        return this.entityLiving.onLadder();
    }

    public ScriptEntity getLookTarget() {
        Entity var1 = this.entityLiving.getLookingAt();
        return ScriptEntity.getEntityClass(var1);
    }

    public void setLookTarget(ScriptEntity entity) {
        this.entityLiving.lookAt = entity.entity;
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

    public void setMaxHealth(int value) {
        ((ExLivingEntity) this.entityLiving).setMaxHealth(value);
    }

    public String getTexture() {
        return this.entityLiving.getTexture();
    }

    public void setTexture(String name) {
        ((ExLivingEntity) this.entityLiving).setTexture(name);
    }

    public int getHurtTime() {
        return this.entityLiving.hurtTime;
    }

    public int getHurtTimeResistance() {
        return this.entityLiving.lastHurt;
    }

    public void setHurtTime(int value) {
        this.entityLiving.hurtTime = value;
    }

    public void setHurtTimeResistance(int value) {
        this.entityLiving.lastHurt = value;
    }

    public ScriptItem getHeldItem() {
        return new ScriptItem(((ExLivingEntity) this.entityLiving).getHeldItem());
    }

    public void setHeldItem(ScriptItem item) {
        ((ExLivingEntity) this.entityLiving).setHeldItem(item.item);
    }

    public float getMoveSpeed() {
        return ((ExLivingEntity) this.entityLiving).getMovementSpeed();
    }

    public void setMoveSpeed(float value) {
        ((ExLivingEntity) this.entityLiving).setMovementSpeed(value);
    }

    public int getTimesCanJumpInAir() {
        return ((ExLivingEntity) this.entityLiving).getTimesCanJumpInAir();
    }

    public void setTimesCanJumpInAir(int value) {
        ((ExLivingEntity) this.entityLiving).setTimesCanJumpInAir(value);
    }

    public boolean getCanWallJump() {
        return ((ExLivingEntity) this.entityLiving).getCanWallJump();
    }

    public void setCanWallJump(boolean value) {
        ((ExLivingEntity) this.entityLiving).setCanWallJump(value);
    }

    public int getJumpsInAirLeft() {
        return ((ExLivingEntity) this.entityLiving).getJumpsLeft();
    }

    public void setJumpsInAirLeft(int value) {
        ((ExLivingEntity) this.entityLiving).setJumpsLeft(value);
    }

    public double getGravity() {
        return ((ExLivingEntity) this.entityLiving).getGravity();
    }

    public void setGravity(double value) {
        ((ExLivingEntity) this.entityLiving).setGravity(value);
    }

    public double getJumpVelocity() {
        return ((ExLivingEntity) this.entityLiving).getJumpVelocity();
    }

    public void setJumpVelocity(double value) {
        ((ExLivingEntity) this.entityLiving).setJumpVelocity(value);
    }

    public double getJumpWallMultiplier() {
        return ((ExLivingEntity) this.entityLiving).getJumpWallMultiplier();
    }

    public void setJumpWallMultiplier(double value) {
        ((ExLivingEntity) this.entityLiving).setJumpWallMultiplier(value);
    }

    public double getJumpInAirMultiplier() {
        return ((ExLivingEntity) this.entityLiving).getJumpInAirMultiplier();
    }

    public void setJumpInAirMultiplier(double value) {
        ((ExLivingEntity) this.entityLiving).setJumpInAirMultiplier(value);
    }

    public boolean getShouldJump() {
        return this.entityLiving.jumping;
    }

    public void setShouldJump(boolean value) {
        this.entityLiving.jumping = value;
    }

    public float getAirControl() {
        return ((ExLivingEntity) this.entityLiving).getAirControl();
    }

    public void setAirControl(float value) {
        ((ExLivingEntity) this.entityLiving).setAirControl(value);
    }

    public void fireBullet(float spread, int damage) {
        AC_UtilBullet.fireBullet(this.entityLiving.level, this.entityLiving, spread, damage);
    }

    public float getFov() {
        return ((ExLivingEntity) this.entityLiving).getFov();
    }

    public void setFov(float value) {
        ((ExLivingEntity) this.entityLiving).setFov(value);
    }

    public boolean getCanLookRandomly() {
        return ((ExLivingEntity) this.entityLiving).getCanLookRandomly();
    }

    public void setCanLookRandomly(boolean value) {
        ((ExLivingEntity) this.entityLiving).setCanLookRandomly(value);
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

    public void setRandomLookNext(int value) {
        ((ExLivingEntity) this.entityLiving).setRandomLookNext(value);
    }

    public int getRandomLookRate() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookRate();
    }

    public void setRandomLookRate(int value) {
        ((ExLivingEntity) this.entityLiving).setRandomLookRate(value);
    }

    public int getRandomLookRateVariation() {
        return ((ExLivingEntity) this.entityLiving).getRandomLookRateVariation();
    }

    public void setRandomLookRateVariation(int value) {
        ((ExLivingEntity) this.entityLiving).setRandomLookRateVariation(value);
    }
}
