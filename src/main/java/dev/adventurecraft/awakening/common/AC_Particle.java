package dev.adventurecraft.awakening.common;

import net.minecraft.client.entity.particle.ParticleEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

public class AC_Particle extends ParticleEntity {
    public AC_Particle(World arg, double x, double y, double z, double velX, double velY, double velZ) {
        super(arg, x,y,z,velX,velY,velZ);
        this.xVelocity = velX;
        this.yVelocity = velY;
        this.zVelocity = velZ;
    }

    public void setTexture(int id){
        this.texture = id;
    }

    public void setAliveTicks(int ticks){
        field_2639 = ticks;
    }

    public void setColor(float r, float g, float b){
        this.red = r;
        this.green = g;
        this.blue = b;
    }


    @Override
    public void writeNBT(CompoundTag arg) {
        super.writeNBT(arg);
    }

    @Override
    public void readNBT(CompoundTag arg) {
        super.readNBT(arg);
    }
}
