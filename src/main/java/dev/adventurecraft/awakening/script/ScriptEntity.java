package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.AC_EntityLivingScript;
import dev.adventurecraft.awakening.common.AC_EntityNPC;
import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExEntityRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.animal.WolfEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptEntity {

    Entity entity;

    ScriptEntity(Entity entity) {
        this.entity = entity;
    }

    public static ScriptEntity getEntityClass(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof PlayerEntity e) return new ScriptEntityPlayer(e);
        if (entity instanceof MonsterEntity e) return new ScriptEntityMob(e);
        if (entity instanceof WolfEntity e) return new ScriptEntityWolf(e);
        if (entity instanceof MobEntity e) return new ScriptEntityCreature(e);
        if (entity instanceof FlyingEntity e) return new ScriptEntityFlying(e);
        if (entity instanceof AC_EntityNPC e) return new ScriptEntityNPC(e);
        if (entity instanceof AC_EntityLivingScript e) return new ScriptEntityLivingScript(e);
        if (entity instanceof SlimeEntity e) return new ScriptEntitySlime(e);
        if (entity instanceof LivingEntity e) return new ScriptEntityLiving(e);
        if (entity instanceof ArrowEntity e) return new ScriptEntityArrow(e);
        return new ScriptEntity(entity);
    }

    public int getEntityID() {
        return this.entity.entityId;
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(this.entity.x, this.entity.y, this.entity.z);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    ScriptVec3 getPosition(float deltaTime) {
        double dt = deltaTime;
        double dtSub = 1.0 - dt;
        return new ScriptVec3(
            dtSub * this.entity.prevX + dt * this.entity.x,
            dtSub * this.entity.prevY + dt * this.entity.y,
            dtSub * this.entity.prevZ + dt * this.entity.z);
    }

    public void setPosition(ScriptVec3 var1) {
        this.setPosition(var1.x, var1.y, var1.z);
    }

    public void setPosition(double var1, double var3, double var5) {
        this.entity.setPosition(var1, var3, var5);
    }

    public ScriptVecRot getRotation() {
        return new ScriptVecRot(this.entity.yaw, this.entity.pitch);
    }

    ScriptVecRot getRotation(float var1) {
        float var2 = 1.0F - var1;
        return new ScriptVecRot(var2 * this.entity.prevYaw + var1 * this.entity.yaw, var2 * this.entity.prevPitch + var1 * this.entity.pitch);
    }

    public void setRotation(float yaw, float pitch) {
        this.entity.setRotation(yaw, pitch);
    }

    public ScriptVec3 getVelocity() {
        return new ScriptVec3(this.entity.xVelocity, this.entity.yVelocity, this.entity.zVelocity);
    }

    public void setVelocity(ScriptVec3 var1) {
        this.setVelocity(var1.x, var1.y, var1.z);
    }

    public void setVelocity(double var1, double var3, double var5) {
        this.entity.setVelocity(var1, var3, var5);
    }

    public void addVelocity(ScriptVec3 var1) {
        this.addVelocity(var1.x, var1.y, var1.z);
    }

    public void addVelocity(double var1, double var3, double var5) {
        this.entity.accelerate(var1, var3, var5);
    }

    public void setDead() {
        this.entity.remove();
    }

    public void mountEntity(ScriptEntity var1) {
        if (var1 != null) {
            this.entity.startRiding(var1.entity);
        } else {
            this.entity.startRiding(null);
        }

    }

    public ScriptEntity getMountedEntity() {
        return getEntityClass(this.entity.vehicle);
    }

    public boolean isBurning() {
        return this.entity.isOnFire();
    }

    public boolean isAlive() {
        return this.entity.isAlive();
    }

    public boolean isRiding() {
        return this.entity.hasVehicle();
    }

    public boolean isSneaking() {
        return this.entity.method_1373();
    }

    public ScriptEntity[] getEntitiesWithinRange(double range) {
        var aabb = AxixAlignedBoundingBox.createAndAddToList(
            this.entity.x - range, this.entity.y - range, this.entity.z - range,
            this.entity.x + range, this.entity.y + range, this.entity.z + range);
        var entities = (List<Entity>) this.entity.world.getEntities(this.entity, aabb);
        ArrayList<ScriptEntity> list = new ArrayList<>();
        double rangeSqr = range * range;

        for (Entity entity : entities) {
            if (entity.method_1352(this.entity) < rangeSqr) {
                list.add(getEntityClass(entity));
            }
        }

        return list.toArray(new ScriptEntity[0]);
    }

    public ScriptEntity dropItem(ScriptItem var1) {
        return getEntityClass(this.entity.dropItem(var1.item, 0.0F));
    }

    public boolean isInsideOfWater() {
        return this.entity.isInFluid(Material.WATER);
    }

    public boolean isInsideOfLava() {
        return this.entity.isInFluid(Material.LAVA);
    }

    public boolean getImmuneToFire() {
        return this.entity.immuneToFire;
    }

    public void setImmuneToFire(boolean var1) {
        this.entity.immuneToFire = var1;
    }

    public int getFireLevel() {
        return this.entity.fireTicks;
    }

    public void setFireLevel(int var1) {
        this.entity.fireTicks = var1;
    }

    public int getFireResistance() {
        return this.entity.field_1646;
    }

    public void setFireResistance(int var1) {
        this.entity.field_1646 = var1;
    }

    public int getAir() {
        return this.entity.air;
    }

    public void setAir(int var1) {
        this.entity.air = var1;
    }

    public int getMaxAir() {
        return this.entity.field_1648;
    }

    public void setMaxAir(int var1) {
        this.entity.field_1648 = var1;
    }

    public int getStunned() {
        return ((ExEntity) this.entity).getStunned();
    }

    public void setStunned(int var1) {
        ((ExEntity) this.entity).setStunned(var1);
    }

    public boolean attackEntityFrom(ScriptEntity var1, int var2) {
        return this.entity.damage(var1.entity, var2);
    }

    public String getClassType() {
        if (this.entity instanceof PlayerEntity) return "Player";
        return ExEntityRegistry.getEntityStringClimbing(this.entity);
    }

    public boolean getCollidesWithClipBlocks() {
        return ((ExEntity) this.entity).getCollidesWithClipBlocks();
    }

    public void setCollidesWithClipBlocks(boolean var1) {
        ((ExEntity) this.entity).setCollidesWithClipBlocks(var1);
    }

    public float getHeight() {
        return this.entity.height;
    }

    public void setHeight(float var1) {
        this.entity.height = var1;
        this.entity.setPosition(this.entity.x, this.entity.y, this.entity.z);
    }

    public float getWidth() {
        return this.entity.width;
    }

    public void setWidth(float var1) {
        this.entity.width = var1;
        this.entity.setPosition(this.entity.x, this.entity.y, this.entity.z);
    }

    public void setIsFlying(boolean var1) {
        ((ExEntity) this.entity).setIsFlying(var1);
    }

    public boolean getIsFlying() {
        return ((ExEntity) this.entity).handleFlying();
    }

    public boolean getOnGround() {
        return this.entity.onGround;
    }

    public Object[] rayTrace(ScriptVec3 pointA, ScriptVec3 pointB) {
        return this.rayTrace(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public Object[] rayTrace(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        Object[] result = new Object[3];
        HitResult hit = AC_UtilBullet.rayTrace(this.entity.world, this.entity, Vec3d.from(aX, aY, aZ), Vec3d.from(bX, bY, bZ));
        if (hit != null) {
            result[0] = new ScriptVec3(hit.field_1988.x, hit.field_1988.y, hit.field_1988.z);
            if (hit.type == HitType.field_789) {
                result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
            } else {
                result[2] = getEntityClass(hit.field_1989);
            }
        }
        return result;
    }

    public float getyOffset() {
        return this.entity.standingEyeHeight;
    }

    public void setyOffset(float var1) {
        this.entity.standingEyeHeight = var1;
    }
}
