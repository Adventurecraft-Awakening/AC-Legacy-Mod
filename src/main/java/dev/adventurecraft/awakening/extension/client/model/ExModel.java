package dev.adventurecraft.awakening.extension.client.model;

import dev.adventurecraft.awakening.client.render.ModelRenderContext;
import dev.adventurecraft.awakening.client.render.state.*;

public interface ExModel {
    default boolean ac$nativeModel() { return false; }
    default void ac$render(ModelRenderContext context,Matrix4 pose,Color4 tint,
                          float time,float strength,float bob,float yaw,float pitch,float scale) {
        throw new UnsupportedOperationException("Model does not supply native geometry");
    }
}
