package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.SandTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingTile.class)
public abstract class MixinFallingBlockEntity extends MixinEntity implements ExFallingBlockEntity {

    @Shadow
    public int blockId;

    @Shadow
    public int blockMeta;

    public int metadata;
    public double startX;
    public double startZ;

    @Inject(method = "<init>(Lnet/minecraft/world/World;)V", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.setSize(0.98F, 0.98F);
        this.height = 0.98F;
        this.standingEyeHeight = this.height / 2.0F;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;DDDI)V", at = @At("TAIL"))
    private void init(Level var1, double x, double y, double z, int var5, CallbackInfo ci) {
        init(var1, ci);
        this.startX = x;
        this.startZ = z;
    }

    public void tick() {
        if (this.blockId == 0) {
            this.remove();
        } else {
            this.prevX = this.x;
            this.prevY = this.y;
            this.prevZ = this.z;
            ++this.blockMeta;
            this.yVelocity -= 0.04F;
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            this.xVelocity *= 0.98F;
            this.yVelocity *= 0.98F;
            this.zVelocity *= 0.98F;
            int var1 = Mth.floor(this.x);
            int var2 = Mth.floor(this.y);
            int var3 = Mth.floor(this.z);
            if (this.world.getTile(var1, var2, var3) == this.blockId) {
                this.world.setTile(var1, var2, var3, 0);
            }

            if (this.onGround && Math.abs(this.xVelocity) < 0.01D && Math.abs(this.zVelocity) < 0.01D) {
                this.xVelocity *= 0.7F;
                this.zVelocity *= 0.7F;
                this.yVelocity *= -0.5D;
                if (!SandTile.isFree(this.world, var1, var2 - 1, var3)) {
                    this.remove();
                    if (!this.world.isClientSide) {
                        if (!this.world.mayPlace(this.blockId, var1, var2, var3, true, 1) ||
                            !this.world.setTileAndData(var1, var2, var3, this.blockId, this.metadata)) {
                            this.dropItem(this.blockId, 1);
                        }
                    }
                } else {
                    this.setPosition((double) var1 + 0.5D, this.y, (double) var3 + 0.5D);
                    this.xVelocity = 0.0D;
                    this.zVelocity = 0.0D;
                }
            } else if (this.blockMeta > 100 && !this.world.isClientSide) {
                this.dropItem(this.blockId, 1);
                this.remove();
            }

            if (Math.abs(this.x - this.startX) >= 1.0D) {
                this.xVelocity = 0.0D;
                this.setPosition((double) var1 + 0.5D, this.y, this.z);
            }

            if (Math.abs(this.z - this.startZ) >= 1.0D) {
                this.zVelocity = 0.0D;
                this.setPosition(this.x, this.y, (double) var3 + 0.5D);
            }
        }
    }

    @Inject(method = "writeAdditional", at = @At("TAIL"))
    private void writeAc(CompoundTag var1, CallbackInfo ci) {
        var1.putInt("EntityID", this.entityId);
    }

    @Inject(method = "readAdditional", at = @At("TAIL"))
    private void readAc(CompoundTag var1, CallbackInfo ci) {
        if (var1.hasKey("EntityID")) {
            this.entityId = var1.getInt("EntityID");
        }
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
