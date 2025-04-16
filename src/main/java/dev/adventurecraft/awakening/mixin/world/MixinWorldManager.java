package dev.adventurecraft.awakening.mixin.world;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OldChunkStorage.class)
public abstract class MixinWorldManager {

    @Inject(method = "save(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/Level;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private static void markAsAC(LevelChunk chunk, Level world, CompoundTag tag, CallbackInfo ci) {
        tag.putInt("acVersion", 0);
    }

    @Inject(
        method = "load(Lnet/minecraft/world/level/Level;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/chunk/LevelChunk;",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/chunk/LevelChunk;blocks:[B",
            shift = At.Shift.AFTER,
            ordinal = 0))
    private static void importBlocksFromAC(
        Level world,
        CompoundTag tag,
        CallbackInfoReturnable<LevelChunk> cir,
        @Local LevelChunk chunk) {
        if (!tag.hasKey("acVersion") && ((ExWorldProperties) world.levelData).isOriginallyFromAC()) {
            AC_Blocks.convertACVersion(chunk.blocks);
        }
    }
}
