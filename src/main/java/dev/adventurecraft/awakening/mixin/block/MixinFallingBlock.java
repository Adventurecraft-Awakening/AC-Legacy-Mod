package dev.adventurecraft.awakening.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_IBlockColor;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.SandTile;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SandTile.class)
public abstract class MixinFallingBlock extends Tile implements AC_IBlockColor {

    protected MixinFallingBlock(int i, Material arg) {
        super(i, arg);
    }

    @Inject(
        method = "checkSlide",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addEntity(Lnet/minecraft/world/entity/Entity;)Z",
            shift = At.Shift.BEFORE))
    private void setMetaOnEntity(
        Level world, int x, int y, int z, CallbackInfo ci,
        @Local FallingTile entity) {

        int meta = world.getData(x, y, z);
        ((ExFallingBlockEntity) entity).setMetadata(meta);
    }

    @Override
    public int getTexture(int var1, int meta) {
        return meta == 0 ? this.tex : 228 + meta - 1;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
