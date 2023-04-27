package dev.adventurecraft.awakening.common;

import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.world.World;

public class AC_EntityAirFX extends ParticleEntity {
    public AC_EntityAirFX(World var1, double var2, double var4, double var6) {
        super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
        this.texture = 0;
        this.red = this.green = this.blue = 1.0F;
        this.field_2641 = 0.0F;
        this.field_2640 /= 2.0F;
        this.xVelocity = this.yVelocity = this.zVelocity = 0.0D;
    }

    public int method_2003() {
        return 0;
    }
}
