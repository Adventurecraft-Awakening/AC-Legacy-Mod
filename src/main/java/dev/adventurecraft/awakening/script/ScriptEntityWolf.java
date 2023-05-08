package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.animal.ExWolfEntity;
import net.minecraft.entity.animal.WolfEntity;

@SuppressWarnings("unused")
public class ScriptEntityWolf extends ScriptEntityCreature {

    WolfEntity entityWolf;

    ScriptEntityWolf(WolfEntity var1) {
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
        this.entityWolf.setSitting(var1);
    }

    public boolean isWolfSitting() {
        return this.entityWolf.isSitting();
    }

    public void setWolfAngry(boolean var1) {
        this.entityWolf.setAngry(var1);
    }

    public boolean isWolfAngry() {
        return this.entityWolf.isAngry();
    }

    public void setWolfTamed(boolean var1) {
        this.entityWolf.setHasOwner(var1);
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
