package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.client.render.WorldEventRenderer;

public class ScriptParticle {

    WorldEventRenderer renderGlobal;

    ScriptParticle(WorldEventRenderer renderer) {
        this.renderGlobal = renderer;
    }

    public ScriptEntity create(String type, double x, double y, double z) {
        ParticleEntity particle = ((ExWorldEventRenderer) this.renderGlobal).spawnParticleR(type, x, y, z, 0, 0, 0);

        return ScriptEntity.getEntityClass(particle);
    }
}
