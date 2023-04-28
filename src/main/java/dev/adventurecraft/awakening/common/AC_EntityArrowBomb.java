package dev.adventurecraft.awakening.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_EntityArrowBomb extends ArrowEntity {
    private int fuse = 45;

    public AC_EntityArrowBomb(World var1) {
        super(var1);
    }

    public AC_EntityArrowBomb(World var1, double var2, double var4, double var6) {
        super(var1, var2, var4, var6);
    }

    public AC_EntityArrowBomb(World var1, LivingEntity var2) {
        super(var1, var2);
    }

    public void tick() {
        super.tick();
        --this.fuse;
        if (this.fuse == 0) {
            AC_EntityBomb.explode(this, this.owner, this.world, this.x, this.y, this.z);
            this.remove();
        } else {
            this.world.addParticle("smoke", this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
        }

    }

    public void handleHitEntity(HitResult var1) {
        this.xVelocity *= -0.1F;
        this.yVelocity *= -0.1F;
        this.zVelocity *= -0.1F;
        this.yaw += 180.0F;
        this.prevYaw += 180.0F;
        this.ticksFlying = 0;
    }

    public void writeAdditional(CompoundTag var1) {
        super.writeAdditional(var1);
        var1.put("fuse", (byte) this.fuse);
    }

    public void readAdditional(CompoundTag var1) {
        super.readAdditional(var1);
        this.fuse = var1.getByte("fuse") & 255;
    }

    public void onPlayerCollision(PlayerEntity var1) {
    }

    public boolean damage(Entity var1, int var2) {
        if (!this.removed) {
            this.setAttacked();
            AC_EntityBomb.explode(this, this.owner, this.world, this.x, this.y, this.z);
        }

        return false;
    }
}
