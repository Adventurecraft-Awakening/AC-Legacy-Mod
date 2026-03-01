package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import dev.adventurecraft.awakening.common.AC_UtilBullet;

@SuppressWarnings("unused")
public class ScriptEntityLiving extends ScriptEntity {

    Mob entityLiving;

    ScriptEntityLiving(Mob entity) {
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
        return ((ExMob) this.entityLiving).getMaxHealth();
    }

    public void setMaxHealth(int value) {
        ((ExMob) this.entityLiving).setMaxHealth(value);
    }

    public String getTexture() {
        return this.entityLiving.getTexture();
    }

    public void setTexture(String name) {
        ((ExMob) this.entityLiving).setTexture(name);
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
        return new ScriptItem(((ExMob) this.entityLiving).getSelectedItem());
    }

    public void setHeldItem(ScriptItem item) {
        ((ExMob) this.entityLiving).setHeldItem(item.item);
    }

    public float getMoveSpeed() {
        return ((ExMob) this.entityLiving).getMovementSpeed();
    }

    public void setMoveSpeed(float value) {
        ((ExMob) this.entityLiving).setMovementSpeed(value);
    }

    public int getTimesCanJumpInAir() {
        return ((ExMob) this.entityLiving).getTimesCanJumpInAir();
    }

    public void setTimesCanJumpInAir(int value) {
        ((ExMob) this.entityLiving).setTimesCanJumpInAir(value);
    }

    public boolean getCanWallJump() {
        return ((ExMob) this.entityLiving).getCanWallJump();
    }

    public void setCanWallJump(boolean value) {
        ((ExMob) this.entityLiving).setCanWallJump(value);
    }

    public int getJumpsInAirLeft() {
        return ((ExMob) this.entityLiving).getJumpsLeft();
    }

    public void setJumpsInAirLeft(int value) {
        ((ExMob) this.entityLiving).setJumpsLeft(value);
    }

    public double getGravity() {
        return ((ExMob) this.entityLiving).getGravity();
    }

    public void setGravity(double value) {
        ((ExMob) this.entityLiving).setGravity(value);
    }

    public double getJumpVelocity() {
        return ((ExMob) this.entityLiving).getJumpVelocity();
    }

    public void setJumpVelocity(double value) {
        ((ExMob) this.entityLiving).setJumpVelocity(value);
    }

    public double getJumpWallMultiplier() {
        return ((ExMob) this.entityLiving).getJumpWallMultiplier();
    }

    public void setJumpWallMultiplier(double value) {
        ((ExMob) this.entityLiving).setJumpWallMultiplier(value);
    }

    public double getJumpInAirMultiplier() {
        return ((ExMob) this.entityLiving).getJumpInAirMultiplier();
    }

    public void setJumpInAirMultiplier(double value) {
        ((ExMob) this.entityLiving).setJumpInAirMultiplier(value);
    }

    public boolean getShouldJump() {
        return this.entityLiving.jumping;
    }

    public void setShouldJump(boolean value) {
        this.entityLiving.jumping = value;
    }

    public float getAirControl() {
        return ((ExMob) this.entityLiving).getAirControl();
    }

    public void setAirControl(float value) {
        ((ExMob) this.entityLiving).setAirControl(value);
    }

    public void fireBullet(float spread, int damage) {
        AC_UtilBullet.fireBullet(this.entityLiving.level, this.entityLiving, spread, damage);
    }

    public float getFov() {
        return ((ExMob) this.entityLiving).getFov();
    }

    public void setFov(float value) {
        ((ExMob) this.entityLiving).setFov(value);
    }

    public boolean getCanLookRandomly() {
        return ((ExMob) this.entityLiving).getCanLookRandomly();
    }

    public void setCanLookRandomly(boolean value) {
        ((ExMob) this.entityLiving).setCanLookRandomly(value);
    }

    public float getRandomLookVelocity() {
        return ((ExMob) this.entityLiving).getRandomLookVelocity();
    }

    public void setRandomLookVelocity(float var1) {
        ((ExMob) this.entityLiving).setRandomLookVelocity(var1);
    }

    public int getRandomLookNext() {
        return ((ExMob) this.entityLiving).getRandomLookNext();
    }

    public void setRandomLookNext(int value) {
        ((ExMob) this.entityLiving).setRandomLookNext(value);
    }

    public int getRandomLookRate() {
        return ((ExMob) this.entityLiving).getRandomLookRate();
    }

    public void setRandomLookRate(int value) {
        ((ExMob) this.entityLiving).setRandomLookRate(value);
    }

    public int getRandomLookRateVariation() {
        return ((ExMob) this.entityLiving).getRandomLookRateVariation();
    }

    public void setRandomLookRateVariation(int value) {
        ((ExMob) this.entityLiving).setRandomLookRateVariation(value);
    }
}
