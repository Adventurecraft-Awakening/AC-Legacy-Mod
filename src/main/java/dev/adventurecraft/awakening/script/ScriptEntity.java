package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.AC_UtilBullet;
import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;
import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.entity.AC_Particle;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExEntityRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public class ScriptEntity {

    Entity entity;

    ScriptEntity(Entity entity) {
        this.entity = entity;
    }

    public static ScriptEntity getEntityClass(Entity entity) {
        return switch (entity) {
            case null -> null;
            case Player e -> new ScriptEntityPlayer(e);
            case Monster e -> new ScriptEntityMob(e);
            case Wolf e -> new ScriptEntityWolf(e);
            case PathfinderMob e -> new ScriptEntityCreature(e);
            case FlyingMob e -> new ScriptEntityFlying(e);
            case AC_EntityNPC e -> new ScriptEntityNPC(e);
            case AC_EntityLivingScript e -> new ScriptEntityLivingScript(e);
            case Slime e -> new ScriptEntitySlime(e);
            case Mob e -> new ScriptEntityLiving(e);
            case Arrow e -> new ScriptEntityArrow(e);
            case ItemEntity e -> new ScriptEntityItem(e);
            case AC_Particle e -> new ScriptParticleEntity(e);
            default -> new ScriptEntity(entity);
        };
    }

    public void setCanGetFallDamage(boolean arg) {
        ((ExEntity) this.entity).setCanGetFallDamage(arg);
    }

    public boolean getCanGetFallDamage() {
        return ((ExEntity) this.entity).getCanGetFallDamage();
    }

    public int getEntityID() {
        return this.entity.id;
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(this.entity.x, this.entity.y, this.entity.z);
    }

    ScriptVec3 getPosition(float deltaTime) {
        float invDelta = 1.0f - deltaTime;
        return new ScriptVec3(
            invDelta * this.entity.xo + deltaTime * this.entity.x,
            invDelta * this.entity.yo + deltaTime * this.entity.y,
            invDelta * this.entity.zo + deltaTime * this.entity.z
        );
    }

    public void setPosition(ScriptVec3 vec) {
        this.setPosition(vec.x, vec.y, vec.z);
    }

    public void setPosition(double x, double y, double z) {
        this.entity.setPos(x, y, z);
    }

    public ScriptVecRot getRotation() {
        return new ScriptVecRot(this.entity.yRot, this.entity.xRot);
    }

    ScriptVecRot getRotation(float deltaTime) {
        float dtSub = 1.0F - deltaTime;
        return new ScriptVecRot(
            dtSub * this.entity.yRotO + deltaTime * this.entity.yRot,
            dtSub * this.entity.xRotO + deltaTime * this.entity.xRot
        );
    }

    public void setRotation(float yaw, float pitch) {
        // Clamp previous values to prevent snapping.
        this.entity.yRotO %= 360.0f;
        this.entity.xRotO %= 360.0f;

        this.entity.setRot(yaw, pitch);
    }

    public ScriptVec3 getLookVec() {
        Vec3 vec = this.entity.getLookAngle();
        return new ScriptVec3(vec.x, vec.y, vec.z);
    }

    public void setLookVec(double x, double y, double z) {
        ((ExEntity) this.entity).setRotation(x, y, z);
    }

    public void setLookVec(ScriptVec3 vec) {
        this.setLookVec(vec.x, vec.y, vec.z);
    }

    public ScriptVec3 getVelocity() {
        return new ScriptVec3(this.entity.xd, this.entity.yd, this.entity.zd);
    }

    public void setVelocity(ScriptVec3 vec) {
        this.setVelocity(vec.x, vec.y, vec.z);
    }

    public void setVelocity(double x, double y, double z) {
        this.entity.lerpMotion(x, y, z);
    }

    public void addVelocity(ScriptVec3 vec) {
        this.addVelocity(vec.x, vec.y, vec.z);
    }

    public void addVelocity(double x, double y, double z) {
        this.entity.push(x, y, z);
    }

    public void setDead() {
        this.entity.remove();
    }

    public void mountEntity(ScriptEntity entity) {
        if (entity != null) {
            this.entity.ride(entity.entity);
        }
        else {
            this.entity.ride(null);
        }
    }

    public ScriptEntity getMountedEntity() {
        return getEntityClass(this.entity.riding);
    }

    public boolean isBurning() {
        return this.entity.displayFireAnimation();
    }

    public boolean isAlive() {
        return this.entity.isAlive();
    }

    public boolean isRiding() {
        return this.entity.isRiding();
    }

    public boolean isSneaking() {
        return this.entity.isSneaking();
    }

    public ScriptEntity[] getEntitiesWithinRange(double range) {
        var aabb = AABB.newTemp(
            this.entity.x - range,
            this.entity.y - range,
            this.entity.z - range,
            this.entity.x + range,
            this.entity.y + range,
            this.entity.z + range
        );
        var entities = (List<Entity>) this.entity.level.getEntities(this.entity, aabb);
        ArrayList<ScriptEntity> list = new ArrayList<>();
        double rangeSqr = range * range;

        for (Entity entity : entities) {
            if (entity.distanceToSqr(this.entity) < rangeSqr) {
                list.add(getEntityClass(entity));
            }
        }

        return list.toArray(new ScriptEntity[0]);
    }

    public ScriptEntity dropItem(ScriptItem item) {
        return getEntityClass(this.entity.spawnAtLocation(item.item, 0.0F));
    }

    public boolean isInsideOfWater() {
        return this.entity.isUnderLiquid(Material.WATER);
    }

    public boolean isInsideOfLava() {
        return this.entity.isUnderLiquid(Material.LAVA);
    }

    public boolean getImmuneToFire() {
        return this.entity.fireImmune;
    }

    public void setImmuneToFire(boolean value) {
        this.entity.fireImmune = value;
    }

    public int getFireLevel() {
        return this.entity.onFire;
    }

    public void setFireLevel(int value) {
        this.entity.onFire = value;
    }

    public int getFireResistance() {
        return this.entity.flameTime;
    }

    public void setFireResistance(int value) {
        this.entity.flameTime = value;
    }

    public int getAir() {
        return this.entity.airSupply;
    }

    public void setAir(int value) {
        this.entity.airSupply = value;
    }

    public int getMaxAir() {
        return this.entity.maxAirSupply;
    }

    public void setMaxAir(int value) {
        this.entity.maxAirSupply = value;
    }

    public int getStunned() {
        return ((ExEntity) this.entity).getStunned();
    }

    public void setStunned(int value) {
        ((ExEntity) this.entity).setStunned(value);
    }

    public boolean attackEntityFrom(ScriptEntity entity, int damage) {
        return this.entity.hurt(entity.entity, damage);
    }

    public String getClassType() {
        if (this.entity instanceof Player) {
            return "Player";
        }
        return ExEntityRegistry.getEntityStringClimbing(this.entity);
    }

    public boolean getCollidesWithClipBlocks() {
        return ((ExEntity) this.entity).getCollidesWithClipBlocks();
    }

    public void setCollidesWithClipBlocks(boolean value) {
        ((ExEntity) this.entity).setCollidesWithClipBlocks(value);
    }

    public void setIgnoreCobwebCollision(boolean value) {
        ((ExEntity) this.entity).setIgnoreCobwebCollision(value);
    }

    public boolean isIgnoreCobwebCollision() {
        return ((ExEntity) this.entity).isIgnoreCobwebCollision();
    }

    public float getHeight() {
        return this.entity.bbHeight;
    }

    public void setHeight(float value) {
        this.entity.bbHeight = value;
        this.entity.setPos(this.entity.x, this.entity.y, this.entity.z);
    }

    public float getWidth() {
        return this.entity.bbWidth;
    }

    public void setWidth(float value) {
        this.entity.bbWidth = value;
        this.entity.setPos(this.entity.x, this.entity.y, this.entity.z);
    }

    public void setIsFlying(boolean value) {
        ((ExEntity) this.entity).setIsFlying(value);
    }

    public boolean getIsFlying() {
        return ((ExEntity) this.entity).getIsFlying();
    }

    public void setNoPhysics(boolean value) {
        ((ExEntity) this.entity).setNoPhysics(value);
    }

    public boolean getNoPhysics() {
        return ((ExEntity) this.entity).getNoPhysics();
    }

    public boolean getOnGround() {
        return this.entity.onGround;
    }

    public Object[] rayTrace(ScriptVec3 pointA, ScriptVec3 pointB) {
        return this.rayTrace(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public Object[] rayTrace(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        var result = new Object[3];
        HitResult hit = AC_UtilBullet.rayTrace(
            this.entity.level,
            this.entity,
            Vec3.newTemp(aX, aY, aZ),
            Vec3.newTemp(bX, bY, bZ)
        );
        if (hit != null) {
            result[0] = new ScriptVec3(hit.pos.x, hit.pos.y, hit.pos.z);
            if (hit.hitType == HitType.TILE) {
                result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
            }
            else {
                result[2] = getEntityClass(hit.entity);
            }
        }
        return result;
    }

    public float getyOffset() {
        return this.entity.heightOffset;
    }

    public void setyOffset(float value) {
        this.entity.heightOffset = value;
    }

    public boolean hasTag(String key) {
        return ((ExEntity) this.entity).hasTag(key);
    }

    public Object getTag(String key) {
        return ((ExEntity) this.entity).getTag(key);
    }

    public Object setTag(String key, Object value) {
        return ((ExEntity) this.entity).setTag(key, value);
    }
}
