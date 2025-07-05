package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class AC_EntityBat extends FlyingMob implements Enemy {

    public int courseChangeCooldown = 0;
    public double waypointX;
    public double waypointY;
    public double waypointZ;
    private Entity targetedEntity = null;
    private int aggroCooldown = 0;
    boolean movingToTarget;
    int attackCooldown;

    public AC_EntityBat(Level var1) {
        super(var1);
        this.textureName = "/mob/bat.png";
        this.movingToTarget = false;
        this.setSize(0.5F, 0.5F);
        this.health = 5;
        ((ExMob) this).setMaxHealth(5);
    }

    @Override
    protected void serverAiStep() {
        if (this.level.difficulty == 0) {
            this.remove();
        }

        if (this.targetedEntity != null && this.targetedEntity.removed) {
            this.targetedEntity = null;
            this.movingToTarget = false;
        }

        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            this.targetedEntity = this.level.getNearestPlayer(this, 100.0D);
            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }

        double var1 = this.waypointX - this.x;
        double var3 = this.waypointY - this.y;
        double var5 = this.waypointZ - this.z;
        double var7 = (double) Mth.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
        if (var7 < 1.0D || var7 > 60.0D || this.random.nextInt(20) == 0) {
            if (this.targetedEntity != null && this.random.nextInt(3) != 0) {
                this.movingToTarget = true;
                this.waypointX = this.targetedEntity.x;
                this.waypointY = this.targetedEntity.y;
                this.waypointZ = this.targetedEntity.z;
            } else {
                this.movingToTarget = false;
                this.waypointX = this.x + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 4.0F);
                this.waypointY = this.y + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 1.0F);
                this.waypointZ = this.z + (double) ((this.random.nextFloat() * 2.0F - 1.0F) * 4.0F);
            }
        }

        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5) + 2;
            if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, var7)) {
                this.xd += var1 / var7 * 0.08D;
                this.yd += var3 / var7 * 0.08D;
                this.zd += var5 / var7 * 0.08D;
                this.yRot = -((float) Math.atan2(this.xd, this.zd)) * 180.0F / 3.141593F;
            } else {
                this.waypointX = this.x;
                this.waypointY = this.y;
                this.waypointZ = this.z;
            }
        }

        if (this.targetedEntity != null) {
            double var9 = this.targetedEntity.x - this.x;
            double var11 = this.targetedEntity.z - this.z;
            this.yHeadRot = -((float) Math.atan2(var9, var11)) * 180.0F / 3.141593F;
            if (this.movingToTarget && this.targetedEntity.distanceToSqr(this) < 2.25D) {
                this.xd = 0.0D;
                this.yd = 0.0D;
                this.zd = 0.0D;
                this.yRot = this.yHeadRot;
                if (this.attackCooldown <= 0) {
                    this.targetedEntity.hurt(this, ((ExFlyingEntity) this).getAttackStrength());
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
        AABB var15 = this.bb.copy();

        for (int var16 = 1; (double) var16 < var7; ++var16) {
            var15.grow(var9, var11, var13);
            if (this.level.getCubes(this, var15).size() > 0) {
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
