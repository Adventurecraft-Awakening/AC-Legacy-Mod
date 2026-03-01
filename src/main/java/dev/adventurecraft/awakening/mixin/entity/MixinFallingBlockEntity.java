package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.SandTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
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

    @Overwrite
    public void tick() {
        if (this.tileId == 0) {
            this.remove();
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        ++this.time;
        this.yd -= 0.04F;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98F;
        this.yd *= 0.98F;
        this.zd *= 0.98F;

        int ix = Mth.floor(this.x);
        int iy = Mth.floor(this.y);
        int iz = Mth.floor(this.z);
        if (this.level.getTile(ix, iy, iz) == this.tileId) {
            this.level.setTile(ix, iy, iz, 0);
        }

        if (this.onGround && Math.abs(this.xd) < 0.01D && Math.abs(this.zd) < 0.01D) {
            this.xd *= 0.7F;
            this.zd *= 0.7F;
            this.yd *= -0.5D;
            if (!SandTile.isFree(this.level, ix, iy - 1, iz)) {
                this.remove();
                if (!this.level.isClientSide) {
                    if (!this.level.mayPlace(this.tileId, ix, iy, iz, true, 1) ||
                        !this.level.setTileAndData(ix, iy, iz, this.tileId, this.metadata)) {
                        this.spawnAtLocation(this.tileId, 1);
                    }
                }
            } else {
                this.setPos((double) ix + 0.5D, this.y, (double) iz + 0.5D);
                this.xd = 0.0D;
                this.zd = 0.0D;
            }
        } else if (this.time > 100 && !this.level.isClientSide) {
            this.spawnAtLocation(this.tileId, 1);
            this.remove();
        }

        if (Math.abs(this.x - this.startX) >= 1.0D) {
            this.xd = 0.0D;
            this.setPos((double) ix + 0.5D, this.y, this.z);
        }

        if (Math.abs(this.z - this.startZ) >= 1.0D) {
            this.zd = 0.0D;
            this.setPos(this.x, this.y, (double) iz + 0.5D);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("EntityID", this.id);
        tag.putInt("Metadata", this.metadata);
        tag.putDouble("StartX", this.startX);
        tag.putDouble("StartZ", this.startZ);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        var exTag = (ExCompoundTag) tag;
        exTag.findInt("EntityID").ifPresent(id -> this.id = id);
        exTag.findInt("Metadata").ifPresent(meta -> this.metadata = meta);

        this.startX = exTag.findDouble("StartX").orElse(this.x);
        this.startZ = exTag.findDouble("StartZ").orElse(this.z);
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
