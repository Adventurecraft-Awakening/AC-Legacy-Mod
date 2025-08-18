package dev.adventurecraft.awakening.extension.client.render.block;

import net.minecraft.client.renderer.Tesselator;
import net.minecraft.world.level.Level;

public interface ExBlockRenderer {

    void startRenderingBlocks(Level var1);

    void stopRenderingBlocks();

    Tesselator ac$getTesselator();

    void ac$setTesselator(Tesselator tesselator);
}
