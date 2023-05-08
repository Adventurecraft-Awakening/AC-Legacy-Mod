package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import net.minecraft.entity.FlyingEntity;

@SuppressWarnings("unused")
public class ScriptEntityFlying extends ScriptEntityLiving {
    FlyingEntity entityFlying;

    ScriptEntityFlying(FlyingEntity var1) {
        super(var1);
        this.entityFlying = var1;
    }

    public void setAttackStrength(int var1) {
        ((ExFlyingEntity) this.entityFlying).setAttackStrength(var1);
    }

    public int getAttackStrength() {
        return ((ExFlyingEntity) this.entityFlying).getAttackStrength();
    }
}
