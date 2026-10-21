package dev.adventurecraft.awakening.extension.client.render.entity;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.state.DrawParameters;

public interface ExPlayerRenderer {
    void renderHand(DrawParameters parameters,RenderTexture skin);
}
