package dev.adventurecraft.awakening.extension.server;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.storage.LevelIO;

import javax.annotation.Nullable;

public interface ExServerLevel extends ExWorld {

    void init(MinecraftServer server);

    static ServerLevel create(
        MinecraftServer server,
        String mapName,
        LevelIO levelIo,
        String name,
        int dimension,
        long seed,
        @Nullable ProgressListener progressListener
    ) {
        try {
            var instance = (ServerLevel) ACMod.UNSAFE.allocateInstance(ServerLevel.class);
            // TODO: deal with dimension
            ((ExWorld) instance).initWorld(mapName, levelIo, name, seed, null, progressListener);
            ((ExServerLevel) instance).init(server);
            return instance;
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
