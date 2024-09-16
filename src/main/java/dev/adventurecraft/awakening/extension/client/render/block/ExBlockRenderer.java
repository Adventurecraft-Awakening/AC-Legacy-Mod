package dev.adventurecraft.awakening.extension.client.render.block;

import net.minecraft.world.level.Level;

public interface ExBlockRenderer {

    void startRenderingBlocks(Level var1);

    void stopRenderingBlocks();
}
