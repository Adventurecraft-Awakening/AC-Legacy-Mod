package dev.adventurecraft.awakening.script;

import java.util.ArrayList;
import java.util.List;

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
import dev.adventurecraft.awakening.common.AC_EntityLivingScript;
import dev.adventurecraft.awakening.common.AC_EntityNPC;
import dev.adventurecraft.awakening.common.AC_UtilBullet;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("unused")
public class ScriptEntity {

    Entity entity;

    ScriptEntity(Entity var1) {
        this.entity = var1;
    }

    public static ScriptEntity getEntityClass(Entity var0) {
        if (var0 == null) return null;
        if (var0 instanceof PlayerEntity) return new ScriptEntityPlayer((PlayerEntity) var0);
        if (var0 instanceof MonsterEntity) return new ScriptEntityMob((MonsterEntity) var0);
        if (var0 instanceof WolfEntity) return new ScriptEntityWolf((WolfEntity) var0);
        if (var0 instanceof MobEntity) return new ScriptEntityCreature((MobEntity) var0);
        if (var0 instanceof FlyingEntity) return new ScriptEntityFlying((FlyingEntity) var0);
        if (var0 instanceof AC_EntityNPC) return new ScriptEntityNPC((AC_EntityNPC) var0);
        if (var0 instanceof AC_EntityLivingScript) return new ScriptEntityLivingScript((AC_EntityLivingScript) var0);
        if (var0 instanceof SlimeEntity) return new ScriptEntitySlime((SlimeEntity) var0);
        if (var0 instanceof LivingEntity) return new ScriptEntityLiving((LivingEntity) var0);
        if (var0 instanceof ArrowEntity) return new ScriptEntityArrow((ArrowEntity) var0);
        return new ScriptEntity(var0);
    }

    public int getEntityID() {
        return this.entity.entityId;
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(this.entity.x, this.entity.y, this.entity.z);
    }

    ScriptVec3 getPosition(float var1) {
        float var2 = 1.0F - var1;
        return new ScriptVec3((double) var2 * this.entity.prevX + (double) var1 * this.entity.x, (double) var2 * this.entity.prevY + (double) var1 * this.entity.y, (double) var2 * this.entity.prevZ + (double) var1 * this.entity.z);
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

    public void setRotation(float var1, float var2) {
        this.entity.setRotation(var1, var2);
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

    public ScriptEntity[] getEntitiesWithinRange(double var1) {
        AxixAlignedBoundingBox var3 = AxixAlignedBoundingBox.createAndAddToList(this.entity.x - var1, this.entity.y - var1, this.entity.z - var1, this.entity.x + var1, this.entity.y + var1, this.entity.z + var1);
        var var4 = (List<Entity>) this.entity.world.getEntities(this.entity, var3);
        ArrayList<ScriptEntity> var5 = new ArrayList<>();
        double var6 = var1 * var1;

        for (Entity var10 : var4) {
            if (var10.method_1352(this.entity) < var6) {
                var5.add(getEntityClass(var10));
            }
        }

        int var13 = 0;
        ScriptEntity[] var14 = new ScriptEntity[var5.size()];

        for (ScriptEntity var11 : var5) {
            var14[var13++] = var11;
        }

        return var14;
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

    public Object[] rayTrace(ScriptVec3 var1, ScriptVec3 var2) {
        return this.rayTrace(var1.x, var1.y, var1.z, var2.x, var2.y, var2.z);
    }

    public Object[] rayTrace(double var1, double var3, double var5, double var7, double var9, double var11) {
        Object[] var13 = new Object[3];
        HitResult var14 = AC_UtilBullet.rayTrace(this.entity.world, this.entity, Vec3d.from(var1, var3, var5), Vec3d.from(var7, var9, var11));
        if (var14 != null) {
            var13[0] = new ScriptVec3(var14.field_1988.x, var14.field_1988.y, var14.field_1988.z);
            if (var14.type == HitType.field_789) {
                var13[1] = new ScriptVec3((float) var14.x, (float) var14.y, (float) var14.z);
            } else {
                var13[2] = getEntityClass(var14.field_1989);
            }
        }

        return var13;
    }

    public float getyOffset() {
        return this.entity.standingEyeHeight;
    }

    public void setyOffset(float var1) {
        this.entity.standingEyeHeight = var1;
    }
}
