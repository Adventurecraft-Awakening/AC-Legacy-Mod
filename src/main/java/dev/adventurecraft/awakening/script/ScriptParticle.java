package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;

public class ScriptParticle {

    LevelRenderer renderGlobal;

    ScriptParticle(LevelRenderer renderer) {
        this.renderGlobal = renderer;
    }

    public ScriptEntity create(String type, double x, double y, double z) {
        Particle particle = ((ExWorldEventRenderer) this.renderGlobal).spawnParticleR(type, x, y, z, 0, 0, 0);

        return ScriptEntity.getEntityClass(particle);
    }
}
