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

    public AC_EntityArrowBomb(Level level) {
        super(level);
    }

    public AC_EntityArrowBomb(Level level, double x, double y, double z) {
        super(level, x, y, z);
    }

    public AC_EntityArrowBomb(Level level, Mob owner) {
        super(level, owner);
    }

    public @Override void tick() {
        super.tick();
        --this.fuse;
        if (this.fuse == 0) {
            AC_EntityBomb.explode(this, this.owner, this.level, this.x, this.y, this.z);
            this.remove();
        }
        else {
            this.level.addParticle("smoke", this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
        }
    }

    // TODO: what is this?
    public void handleHitEntity(HitResult var1) {
        this.xd *= -0.1F;
        this.yd *= -0.1F;
        this.zd *= -0.1F;
        this.yRot += 180.0F;
        this.yRotO += 180.0F;
        this.flightTime = 0;
    }

    public @Override void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("fuse", (byte) this.fuse);
    }

    public @Override void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuse = tag.getByte("fuse") & 255;
    }

    public @Override void playerTouch(Player var1) {
    }

    public @Override boolean hurt(Entity var1, int var2) {
        if (!this.removed) {
            this.markHurt();
            AC_EntityBomb.explode(this, this.owner, this.level, this.x, this.y, this.z);
        }
        return false;
    }
}
