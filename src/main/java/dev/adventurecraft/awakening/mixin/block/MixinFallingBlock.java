package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlock.class)
public abstract class MixinFallingBlock extends Block implements AC_IBlockColor {

    protected MixinFallingBlock(int i, Material arg) {
        super(i, arg);
    }

    @Inject(
        method = "method_436",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
            shift = At.Shift.BEFORE))
    private void setMetaOnEntity(
        World world, int x, int y, int z, CallbackInfo ci,
        @Local FallingBlockEntity entity) {

        int meta = world.getBlockMeta(x, y, z);
        ((ExFallingBlockEntity) entity).setBlockMeta(meta);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return var2 == 0 ? this.texture : 228 + var2 - 1;
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        if (ExBlock.subTypes[this.id] > 0) {
            int var5 = world.getBlockMeta(x, y, z);
            world.setBlockMeta(x, y, z, (var5 + 1) % ExBlock.subTypes[this.id]);
        }
    }
}
