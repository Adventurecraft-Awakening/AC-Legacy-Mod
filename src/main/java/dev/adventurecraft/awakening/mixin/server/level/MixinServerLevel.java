package dev.adventurecraft.awakening.mixin.server.level;

import dev.adventurecraft.awakening.extension.server.ExServerLevel;
import dev.adventurecraft.awakening.mixin.world.MixinWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.IntHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel extends MixinWorld implements ExServerLevel {

    @Shadow private MinecraftServer server;
    @Shadow private IntHashMap entitiesById;

    public @Override void init(MinecraftServer server) {
        this.server = server;
        this.entitiesById = new IntHashMap();
    }
}
