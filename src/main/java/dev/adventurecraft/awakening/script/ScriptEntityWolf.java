package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.animal.ExWolfEntity;
import net.minecraft.world.entity.animal.Wolf;

@SuppressWarnings("unused")
public class ScriptEntityWolf extends ScriptEntityCreature {

    Wolf entityWolf;

    ScriptEntityWolf(Wolf var1) {
        super(var1);
        this.entityWolf = var1;
    }

    public void setAttackStrength(int var1) {
        ((ExWolfEntity) this.entityWolf).setAttackStrength(var1);
    }

    public int getAttackStrength() {
        return ((ExWolfEntity) this.entityWolf).getAttackStrength();
    }

    public void setWolfSitting(boolean var1) {
        this.entityWolf.setInSittingPose(var1);
    }

    public boolean isWolfSitting() {
        return this.entityWolf.isInSittingPose();
    }

    public void setWolfAngry(boolean var1) {
        this.entityWolf.setAngery(var1);
    }

    public boolean isWolfAngry() {
        return this.entityWolf.isAngery();
    }

    public void setWolfTamed(boolean var1) {
        this.entityWolf.setTamed(var1);
    }

    public boolean isWolfTamed() {
        return this.entityWolf.isTamed();
    }

    public void setWolfOwner(ScriptEntityPlayer var1) {
        this.entityWolf.setOwner(var1.entityPlayer.name);
    }

    public String getWolfOwner() {
        return this.entityWolf.getOwner();
    }
}
