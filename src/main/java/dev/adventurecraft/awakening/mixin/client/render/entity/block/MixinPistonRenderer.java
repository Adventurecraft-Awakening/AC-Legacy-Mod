package dev.adventurecraft.awakening.mixin.client.render.entity.block;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.renderer.tileentity.PistonRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.piston.PistonMovingTileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PistonRenderer.class)
public abstract class MixinPistonRenderer {

    @ModifyArg(
        method = "render(Lnet/minecraft/world/level/tile/piston/PistonMovingTileEntity;DDDF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/tileentity/PistonRenderer;bindTexture(Ljava/lang/String;)V"))
    private String useTerrainTexture(String value, @Local(argsOnly = true) PistonMovingTileEntity entity) {
        Tile tile = Tile.tiles[entity.getTileId()];
        if (tile == null) {
            return value;
        }

        int texture = ((ExBlock) tile).getTextureNum();
        String path;
        if (texture == 0) {
            path = value;
        } else {
            path = String.format("/terrain%d.png", texture);
        }
        return path;
    }
}
