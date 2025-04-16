package dev.adventurecraft.awakening.entity;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;

public class AC_EntityAirFX extends Particle {
    public AC_EntityAirFX(Level var1, double var2, double var4, double var6) {
        super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
        this.texture = 0;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.gravity = 0.0F;
        this.size /= 2.0F;
        this.xd = this.yd = this.zd = 0.0D;
    }

    public int getParticleTexture() {
        return 0;
    }
}
