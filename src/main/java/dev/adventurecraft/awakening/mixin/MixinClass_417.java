package dev.adventurecraft.awakening.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.class_417;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(class_417.class)
public abstract class MixinClass_417 {

    @Redirect(
        method = "method_1402",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/block/Block;EMITTANCE:[I",
            opcode = Opcodes.GETSTATIC,
            args = "array=get"))
    private int useBlockLightValue(
        int[] array,
        int index,
        @Local(argsOnly = true) World world,
        @Local(ordinal = 8) int x,
        @Local(ordinal = 13) int y,
        @Local(ordinal = 9) int z) {

        Block block = Block.BY_ID[index];
        if (block != null) {
            return ((ExBlock) block).getBlockLightValue(world, x, y, z);
        } else {
            return array[index];
        }
    }
}
