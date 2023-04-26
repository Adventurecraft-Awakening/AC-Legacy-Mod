package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.render.HeldItemRenderer;

public interface ExGameRenderer {

    void updateWorldLightLevels();

    HeldItemRenderer getOffHandItemRenderer();
}
