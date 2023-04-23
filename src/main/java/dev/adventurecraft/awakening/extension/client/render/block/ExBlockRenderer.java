package dev.adventurecraft.awakening.extension.client.render.block;

import net.minecraft.world.World;

public interface ExBlockRenderer {

    void startRenderingBlocks(World var1);

    void stopRenderingBlocks();
}
