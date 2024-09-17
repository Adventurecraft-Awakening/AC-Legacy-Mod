package dev.adventurecraft.awakening.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightUpdate;
import net.minecraft.world.level.tile.Tile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightUpdate.class)
public abstract class MixinClass_417 {

    @Redirect(
        method = "update",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/tile/Tile;lightEmission:[I",
            opcode = Opcodes.GETSTATIC,
            args = "array=get"))
    private int useBlockLightValue(
        int[] array,
        int index,
        @Local(argsOnly = true) Level world,
        @Local(ordinal = 8) int x,
        @Local(ordinal = 13) int y,
        @Local(ordinal = 9) int z) {

        Tile block = Tile.tiles[index];
        if (block != null) {
            return ((ExBlock) block).getBlockLightValue(world, x, y, z);
        } else {
            return array[index];
        }
    }
}
