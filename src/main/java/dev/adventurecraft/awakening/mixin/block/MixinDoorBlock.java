package dev.adventurecraft.awakening.mixin.block;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.DoorTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoorTile.class)
public abstract class MixinDoorBlock extends Tile {

    protected MixinDoorBlock(int i, Material arg) {
        super(i, arg);
    }

    @Shadow
    public abstract void updateShape(LevelSource arg, int i, int j, int k);

    @Overwrite
    public HitResult clip(Level var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6) {
        this.updateShape(var1, var2, var3, var4);
        int var7 = var1.getData(var2, var3, var4);
        if (this.material == Material.METAL && (var7 & 8) == 8) {
            this.yy0 = 0.8125D;
        }

        var5 = var5.add(-var2, -var3, -var4);
        var6 = var6.add(-var2, -var3, -var4);
        Vec3 var8 = var5.clipX(var6, this.xx0);
        Vec3 var9 = var5.clipX(var6, this.xx1);
        Vec3 var10 = var5.clipY(var6, this.yy0);
        Vec3 var11 = var5.clipY(var6, this.yy1);
        Vec3 var12 = var5.clipZ(var6, this.zz0);
        Vec3 var13 = var5.clipZ(var6, this.zz1);
        if (!this.containsX(var8)) {
            var8 = null;
        }

        if (!this.containsX(var9)) {
            var9 = null;
        }

        if (!this.containsY(var10)) {
            var10 = null;
        }

        if (!this.containsY(var11)) {
            var11 = null;
        }

        if (!this.containsZ(var12)) {
            var12 = null;
        }

        if (!this.containsZ(var13)) {
            var13 = null;
        }

        Vec3 var14 = null;
        if (var8 != null && (var14 == null || var5.distanceTo(var8) < var5.distanceTo(var14))) {
            var14 = var8;
        }

        if (var9 != null && (var14 == null || var5.distanceTo(var9) < var5.distanceTo(var14))) {
            var14 = var9;
        }

        if (var10 != null && (var14 == null || var5.distanceTo(var10) < var5.distanceTo(var14))) {
            var14 = var10;
        }

        if (var11 != null && (var14 == null || var5.distanceTo(var11) < var5.distanceTo(var14))) {
            var14 = var11;
        }

        if (var12 != null && (var14 == null || var5.distanceTo(var12) < var5.distanceTo(var14))) {
            var14 = var12;
        }

        if (var13 != null && (var14 == null || var5.distanceTo(var13) < var5.distanceTo(var14))) {
            var14 = var13;
        }

        if (var14 == null) {
            return null;
        } else {
            byte var15 = -1;
            if (var14 == var8) {
                var15 = 4;
            }

            if (var14 == var9) {
                var15 = 5;
            }

            if (var14 == var10) {
                var15 = 0;
            }

            if (var14 == var11) {
                var15 = 1;
            }

            if (var14 == var12) {
                var15 = 2;
            }

            if (var14 == var13) {
                var15 = 3;
            }

            return new HitResult(var2, var3, var4, var15, var14.add(var2, var3, var4));
        }
    }
}
