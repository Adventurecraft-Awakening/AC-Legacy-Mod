package dev.adventurecraft.awakening.entity;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.level.Level;

public final class AC_EntityAirFX extends Particle {

    public AC_EntityAirFX(Level level, double x, double y, double z) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.texture = 0;
        this.rCol = this.gCol = this.bCol = 1.0F;
        this.gravity = 0.0F;
        this.size /= 2.0F;

        // TODO: use different Particle constructor to avoid expensive xd/yd/zd setup
        this.xd = this.yd = this.zd = 0.0D;
    }
}
