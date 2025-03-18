package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.entity.AC_Particle;
import net.minecraft.client.Minecraft;

public class ScriptParticleEntity extends ScriptEntity {

    ScriptParticleEntity(AC_Particle entity) {
        super(entity);
    }

    public int texture = 0;

    public float r = 1.0F;
    public float g = 1.0F;
    public float b = 1.0F;

    public int alive = 20;

    public void spawn(){
        AC_Particle acParticle = (AC_Particle)entity;
        acParticle.setTexture(this.texture);
        acParticle.setColor(r,g,b);
        acParticle.setAliveTicks(alive);

        Minecraft.instance.particleEngine.add(acParticle);
    }
}
