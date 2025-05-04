package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.SandTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingTile.class)
public abstract class MixinFallingBlockEntity extends MixinEntity implements ExFallingBlockEntity {

    @Shadow
    public int tileId;

    @Shadow
    public int time;

    @Unique
    public int metadata;
    @Unique
    public double startX;
    @Unique
    public double startZ;

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;)V", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.setSize(0.98F, 0.98F);
        this.bbHeight = 0.98F;
        this.heightOffset = this.bbHeight / 2.0F;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDI)V", at = @At("TAIL"))
    private void init(Level var1, double x, double y, double z, int var5, CallbackInfo ci) {
        init(var1, ci);
        this.startX = x;
        this.startZ = z;
    }

    public void tick() {
        if (this.tileId == 0) {
            this.remove();
        } else {
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            ++this.time;
            this.yd -= 0.04F;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.98F;
            this.yd *= 0.98F;
            this.zd *= 0.98F;
            int var1 = Mth.floor(this.x);
            int var2 = Mth.floor(this.y);
            int var3 = Mth.floor(this.z);
            if (this.level.getTile(var1, var2, var3) == this.tileId) {
                this.level.setTile(var1, var2, var3, 0);
            }

            if (this.onGround && Math.abs(this.xd) < 0.01D && Math.abs(this.zd) < 0.01D) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
                this.yd *= -0.5D;
                if (!SandTile.isFree(this.level, var1, var2 - 1, var3)) {
                    this.remove();
                    if (!this.level.isClientSide) {
                        if (!this.level.mayPlace(this.tileId, var1, var2, var3, true, 1) ||
                            !this.level.setTileAndData(var1, var2, var3, this.tileId, this.metadata)) {
                            this.spawnAtLocation(this.tileId, 1);
                        }
                    }
                } else {
                    this.setPos((double) var1 + 0.5D, this.y, (double) var3 + 0.5D);
                    this.xd = 0.0D;
                    this.zd = 0.0D;
                }
            } else if (this.time > 100 && !this.level.isClientSide) {
                this.spawnAtLocation(this.tileId, 1);
                this.remove();
            }

            if (Math.abs(this.x - this.startX) >= 1.0D) {
                this.xd = 0.0D;
                this.setPos((double) var1 + 0.5D, this.y, this.z);
            }

            if (Math.abs(this.z - this.startZ) >= 1.0D) {
                this.zd = 0.0D;
                this.setPos(this.x, this.y, (double) var3 + 0.5D);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("EntityID", this.id);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        ((ExCompoundTag) tag).findInt("EntityID").ifPresent(id -> this.id = id);
    }

    @Override
    public int getMetadata() {
        return this.metadata;
    }

    @Override
    public void setMetadata(int value) {
        this.metadata = value;
    }
}
