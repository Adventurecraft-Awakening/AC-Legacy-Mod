package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import dev.adventurecraft.awakening.util.MathF;
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

        double dX = this.waypointX - this.x;
        double dY = this.waypointY - this.y;
        double dZ = this.waypointZ - this.z;
        double len = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        if (len < 1.0D || len > 60.0D || this.random.nextInt(20) == 0) {
            if (this.targetedEntity != null && this.random.nextInt(3) != 0) {
                this.movingToTarget = true;
                this.waypointX = this.targetedEntity.x;
                this.waypointY = this.targetedEntity.y;
                this.waypointZ = this.targetedEntity.z;
            } else {
                this.movingToTarget = false;
                this.waypointX = this.x + MathF.nextSignedFloat(this.random) * 4.0F;
                this.waypointY = this.y + MathF.nextSignedFloat(this.random);
                this.waypointZ = this.z + MathF.nextSignedFloat(this.random) * 4.0F;
            }
        }

        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.random.nextInt(5) + 2;
            if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, len)) {
                this.xd += dX / len * 0.08D;
                this.yd += dY / len * 0.08D;
                this.zd += dZ / len * 0.08D;
                this.yRot = -MathF.toDegrees((float) Math.atan2(this.xd, this.zd));
            } else {
                this.waypointX = this.x;
                this.waypointY = this.y;
                this.waypointZ = this.z;
            }
        }

        if (this.targetedEntity != null) {
            double var9 = this.targetedEntity.x - this.x;
            double var11 = this.targetedEntity.z - this.z;
            this.yHeadRot = -MathF.toDegrees((float) Math.atan2(var9, var11));
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

    private boolean isCourseTraversable(double wX, double wY, double wZ, double len) {
        double dX = (wX - this.x) / len;
        double dY = (wY - this.y) / len;
        double dZ = (wZ - this.z) / len;
        AABB aabb = this.bb.copy();

        for (int i = 1; (double) i < len; ++i) {
            aabb.grow(dX, dY, dZ);
            if (!this.level.getCubes(this, aabb).isEmpty()) {
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
