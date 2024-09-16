package dev.adventurecraft.awakening.common;

import net.minecraft.client.particle.Particle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class AC_Particle extends Particle {
    public AC_Particle(Level arg, double x, double y, double z, double velX, double velY, double velZ) {
        super(arg, x,y,z,velX,velY,velZ);
        this.xd = velX;
        this.yd = velY;
        this.zd = velZ;
    }

    public void setTexture(int id){
        this.texture = id;
    }

    public void setAliveTicks(int ticks){
        lifetime = ticks;
    }

    public void setColor(float r, float g, float b){
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }


    @Override
    public void saveWithoutId(CompoundTag arg) {
        super.saveWithoutId(arg);
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);
    }
}
