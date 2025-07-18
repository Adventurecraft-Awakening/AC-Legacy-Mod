package dev.adventurecraft.awakening.mixin.server;

import dev.adventurecraft.awakening.extension.server.ExServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelIO;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @ModifyConstant(
        method = "ensureLevelConversion",
        constant = @Constant(
            intValue = 2,
            ordinal = 0
        )
    )
    private int setLevelCount(int original) {
        return 1;
    }

    @Redirect(
        method = "ensureLevelConversion",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/level/storage/LevelIO;Ljava/lang/String;IJ)Lnet/minecraft/server/level/ServerLevel;"
        )
    )
    private ServerLevel doInitWorld(MinecraftServer server, LevelIO levelIo, String name, int dimension, long seed) {
        return ExServerLevel.create(server, "ac_map", levelIo, name, dimension, seed, null);
    }

    // TODO: initWorld for ParentedServerLevel
}
