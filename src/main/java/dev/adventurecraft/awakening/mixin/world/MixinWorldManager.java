package dev.adventurecraft.awakening.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldManager.class)
public abstract class MixinWorldManager {

    @Inject(method = "method_1480", at = @At("TAIL"))
    private static void markAsAC(Chunk chunk, World world, CompoundTag tag, CallbackInfo ci) {
        tag.put("acVersion", 0);
    }

    @Inject(
        method = "method_1479",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/chunk/Chunk;blocks:[B",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private static void importBlocksFromAC(
        World world,
        CompoundTag tag,
        CallbackInfoReturnable<Chunk> cir,
        @Local Chunk chunk) {
        if (!tag.containsKey("acVersion") && ((ExWorldProperties) world.properties).isOriginallyFromAC()) {
            AC_Blocks.convertACVersion(chunk.blocks);
        }
    }
}
