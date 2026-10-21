package dev.adventurecraft.awakening.extension.client.particle;

import dev.adventurecraft.awakening.client.render.ParticleRenderContext;
import dev.adventurecraft.awakening.client.render.VertexSink;

public interface ExParticle {
    void ac$render(ParticleRenderContext context, VertexSink vertices);
    default void ac$renderLit(ParticleRenderContext context) {
        throw new IllegalStateException("Particle does not supply a separate lit renderer");
    }
}
