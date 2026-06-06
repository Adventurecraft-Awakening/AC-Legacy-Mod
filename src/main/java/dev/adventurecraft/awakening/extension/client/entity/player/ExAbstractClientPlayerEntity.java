package dev.adventurecraft.awakening.extension.client.entity.player;

import com.mojang.brigadier.CommandDispatcher;
import dev.adventurecraft.awakening.common.ServerCommandSource;

public interface ExAbstractClientPlayerEntity {

    void displayGUIPalette();

    CommandDispatcher<ServerCommandSource> getCommandDispatcher();

    ServerCommandSource createCommandSource();
}
