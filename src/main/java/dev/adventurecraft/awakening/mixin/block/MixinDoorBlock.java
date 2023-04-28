package dev.adventurecraft.awakening.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DoorBlock.class)
public abstract class MixinDoorBlock extends Block {

    protected MixinDoorBlock(int i, Material arg) {
        super(i, arg);
    }

    @Shadow
    public abstract void updateBoundingBox(BlockView arg, int i, int j, int k);

    @Overwrite
    public HitResult method_1564(World var1, int var2, int var3, int var4, Vec3d var5, Vec3d var6) {
        this.updateBoundingBox(var1, var2, var3, var4);
        int var7 = var1.getBlockMeta(var2, var3, var4);
        if (this.material == Material.METAL && (var7 & 8) == 8) {
            this.minY = 0.8125D;
        }

        var5 = var5.translate(-var2, -var3, -var4);
        var6 = var6.translate(-var2, -var3, -var4);
        Vec3d var8 = var5.method_1295(var6, this.minX);
        Vec3d var9 = var5.method_1295(var6, this.maxX);
        Vec3d var10 = var5.method_1299(var6, this.minY);
        Vec3d var11 = var5.method_1299(var6, this.maxY);
        Vec3d var12 = var5.method_1302(var6, this.minZ);
        Vec3d var13 = var5.method_1302(var6, this.maxZ);
        if (!this.isInsideYZ(var8)) {
            var8 = null;
        }

        if (!this.isInsideYZ(var9)) {
            var9 = null;
        }

        if (!this.isInsideXZ(var10)) {
            var10 = null;
        }

        if (!this.isInsideXZ(var11)) {
            var11 = null;
        }

        if (!this.isInsideXY(var12)) {
            var12 = null;
        }

        if (!this.isInsideXY(var13)) {
            var13 = null;
        }

        Vec3d var14 = null;
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

            return new HitResult(var2, var3, var4, var15, var14.translate(var2, var3, var4));
        }
    }
}
