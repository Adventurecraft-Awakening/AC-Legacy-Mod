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

    public void setPosition(ScriptVec3 vec) {
        this.setPosition(vec.x, vec.y, vec.z);
    }

    public void setPosition(double x, double y, double z) {
        this.entity.setPosition(x, y, z);
    }

    public ScriptVecRot getRotation() {
        return new ScriptVecRot(this.entity.yaw, this.entity.pitch);
    }

    ScriptVecRot getRotation(float deltaTime) {
        float dtSub = 1.0F - deltaTime;
        return new ScriptVecRot(
            dtSub * this.entity.prevYaw + deltaTime * this.entity.yaw,
            dtSub * this.entity.prevPitch + deltaTime * this.entity.pitch);
    }

    public void setRotation(float yaw, float pitch) {
        // Clamp previous values to prevent snapping.
        this.entity.prevYaw %= 360.0f;
        this.entity.prevPitch %= 360.0f;

        this.entity.setRotation(yaw, pitch);
    }

    public ScriptVec3 getLookVec() {
        Vec3d vec = this.entity.getRotation();
        return new ScriptVec3(vec.x, vec.y, vec.z);
    }

    public void setLookVec(double x, double y, double z) {
        ((ExEntity) this.entity).setRotation(x, y, z);
    }

    public void setLookVec(ScriptVec3 vec) {
        this.setLookVec(vec.x, vec.y, vec.z);
    }

    public ScriptVec3 getVelocity() {
        return new ScriptVec3(this.entity.xVelocity, this.entity.yVelocity, this.entity.zVelocity);
    }

    public void setVelocity(ScriptVec3 vec) {
        this.setVelocity(vec.x, vec.y, vec.z);
    }

    public void setVelocity(double x, double y, double z) {
        this.entity.setVelocity(x, y, z);
    }

    public void addVelocity(ScriptVec3 vec) {
        this.addVelocity(vec.x, vec.y, vec.z);
    }

    public void addVelocity(double x, double y, double z) {
        this.entity.accelerate(x, y, z);
    }

    public void setDead() {
        this.entity.remove();
    }

    public void mountEntity(ScriptEntity entity) {
        if (entity != null) {
            this.entity.startRiding(entity.entity);
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

    public ScriptEntity dropItem(ScriptItem item) {
        return getEntityClass(this.entity.dropItem(item.item, 0.0F));
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

    public void setImmuneToFire(boolean value) {
        this.entity.immuneToFire = value;
    }

    public int getFireLevel() {
        return this.entity.fireTicks;
    }

    public void setFireLevel(int value) {
        this.entity.fireTicks = value;
    }

    public int getFireResistance() {
        return this.entity.field_1646;
    }

    public void setFireResistance(int value) {
        this.entity.field_1646 = value;
    }

    public int getAir() {
        return this.entity.air;
    }

    public void setAir(int value) {
        this.entity.air = value;
    }

    public int getMaxAir() {
        return this.entity.field_1648;
    }

    public void setMaxAir(int value) {
        this.entity.field_1648 = value;
    }

    public int getStunned() {
        return ((ExEntity) this.entity).getStunned();
    }

    public void setStunned(int value) {
        ((ExEntity) this.entity).setStunned(value);
    }

    public boolean attackEntityFrom(ScriptEntity entity, int damage) {
        return this.entity.damage(entity.entity, damage);
    }

    public String getClassType() {
        if (this.entity instanceof PlayerEntity) return "Player";
        return ExEntityRegistry.getEntityStringClimbing(this.entity);
    }

    public boolean getCollidesWithClipBlocks() {
        return ((ExEntity) this.entity).getCollidesWithClipBlocks();
    }

    public void setCollidesWithClipBlocks(boolean value) {
        ((ExEntity) this.entity).setCollidesWithClipBlocks(value);
    }

    public float getHeight() {
        return this.entity.height;
    }

    public void setHeight(float value) {
        this.entity.height = value;
        this.entity.setPosition(this.entity.x, this.entity.y, this.entity.z);
    }

    public float getWidth() {
        return this.entity.width;
    }

    public void setWidth(float value) {
        this.entity.width = value;
        this.entity.setPosition(this.entity.x, this.entity.y, this.entity.z);
    }

    public void setIsFlying(boolean value) {
        ((ExEntity) this.entity).setIsFlying(value);
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
       var result = new Object[3];
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

    public void setyOffset(float value) {
        this.entity.standingEyeHeight = value;
    }
}
