package dev.adventurecraft.awakening.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class AC_EntityArrowBomb extends Arrow {
    private int fuse = 45;

    public AC_EntityArrowBomb(Level var1) {
        super(var1);
    }

    public AC_EntityArrowBomb(Level var1, double var2, double var4, double var6) {
        super(var1, var2, var4, var6);
    }

    public AC_EntityArrowBomb(Level var1, Mob var2) {
        super(var1, var2);
    }

    public void tick() {
        super.tick();
        --this.fuse;
        if (this.fuse == 0) {
            AC_EntityBomb.explode(this, this.owner, this.level, this.x, this.y, this.z);
            this.remove();
        } else {
            this.level.addParticle("smoke", this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
        }

    }

    public void handleHitEntity(HitResult var1) {
        this.xd *= -0.1F;
        this.yd *= -0.1F;
        this.zd *= -0.1F;
        this.yRot += 180.0F;
        this.yRotO += 180.0F;
        this.flightTime = 0;
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("fuse", (byte) this.fuse);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuse = tag.getByte("fuse") & 255;
    }

    public void playerTouch(Player var1) {
    }

    public boolean hurt(Entity var1, int var2) {
        if (!this.removed) {
            this.markHurt();
            AC_EntityBomb.explode(this, this.owner, this.level, this.x, this.y, this.z);
        }

        return false;
    }
}
