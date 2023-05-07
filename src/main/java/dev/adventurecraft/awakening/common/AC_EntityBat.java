package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FlyingEntity;
import net.minecraft.entity.monster.Monster;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AC_EntityBat extends FlyingEntity implements Monster {

    public int courseChangeCooldown = 0;
    public double waypointX;
    public double waypointY;
    public double waypointZ;
    private Entity targetedEntity = null;
    private int aggroCooldown = 0;
    boolean movingToTarget;
    int attackCooldown;

    public AC_EntityBat(World var1) {
        super(var1);
        this.texture = "/mob/bat.png";
        this.rand.setSeed(System.currentTimeMillis() * 10L + (long) this.entityId);
        this.movingToTarget = false;
        this.setSize(0.5F, 0.5F);
        this.health = 5;
        ((ExLivingEntity) this).setMaxHealth(5);
    }

    @Override
    protected void tickHandSwing() {
        if (this.world.difficulty == 0) {
            this.remove();
        }

        if (this.targetedEntity != null && this.targetedEntity.removed) {
            this.targetedEntity = null;
            this.movingToTarget = false;
        }

        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.world.getClosestPlayerTo(this, 100.0D);
            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }

        double var1 = this.waypointX - this.x;
        double var3 = this.waypointY - this.y;
        double var5 = this.waypointZ - this.z;
        double var7 = (double) MathHelper.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
        if (var7 < 1.0D || var7 > 60.0D || this.rand.nextInt(20) == 0) {
            if (this.targetedEntity != null && this.rand.nextInt(3) != 0) {
                this.movingToTarget = true;
                this.waypointX = this.targetedEntity.x;
                this.waypointY = this.targetedEntity.y;
                this.waypointZ = this.targetedEntity.z;
            } else {
                this.movingToTarget = false;
                this.waypointX = this.x + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 4.0F);
                this.waypointY = this.y + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 1.0F);
                this.waypointZ = this.z + (double) ((this.rand.nextFloat() * 2.0F - 1.0F) * 4.0F);
            }
        }

        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, var7)) {
                this.xVelocity += var1 / var7 * 0.08D;
                this.yVelocity += var3 / var7 * 0.08D;
                this.zVelocity += var5 / var7 * 0.08D;
                this.yaw = -((float) Math.atan2(this.xVelocity, this.zVelocity)) * 180.0F / 3.141593F;
            } else {
                this.waypointX = this.x;
                this.waypointY = this.y;
                this.waypointZ = this.z;
            }
        }

        if (this.targetedEntity != null) {
            double var9 = this.targetedEntity.x - this.x;
            double var11 = this.targetedEntity.z - this.z;
            this.field_1012 = -((float) Math.atan2(var9, var11)) * 180.0F / 3.141593F;
            if (this.movingToTarget && this.targetedEntity.method_1352(this) < 2.25D) {
                this.xVelocity = 0.0D;
                this.yVelocity = 0.0D;
                this.zVelocity = 0.0D;
                this.yaw = this.field_1012;
                if (this.attackCooldown <= 0) {
                    this.targetedEntity.damage(this, ((ExFlyingEntity) this).getAttackStrength());
                    this.attackCooldown = 10;
                    this.targetedEntity = null;
                }
            }
        }

        if (this.attackCooldown > 0) {
            --this.attackCooldown;
        }
    }

    private boolean isCourseTraversable(double var1, double var3, double var5, double var7) {
        double var9 = (this.waypointX - this.x) / var7;
        double var11 = (this.waypointY - this.y) / var7;
        double var13 = (this.waypointZ - this.z) / var7;
        AxixAlignedBoundingBox var15 = this.boundingBox.method_92();

        for (int var16 = 1; (double) var16 < var7; ++var16) {
            var15.addPos(var9, var11, var13);
            if (this.world.method_190(this, var15).size() > 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    @Override
    protected String getAmbientSound() {
        return "mob.bat.ambient";
    }

    @Override
    protected String getHurtSound() {
        return "mob.bat.pain";
    }

    @Override
    protected String getDeathSound() {
        return "mob.bat.death";
    }
}
