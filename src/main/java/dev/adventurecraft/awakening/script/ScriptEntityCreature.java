package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.entity.ExMobEntity;
import net.minecraft.entity.MobEntity;

@SuppressWarnings("unused")
public class ScriptEntityCreature extends ScriptEntityLiving {

    MobEntity mob;

    ScriptEntityCreature(MobEntity var1) {
        super(var1);
        this.mob = var1;
    }

    public void setTarget(ScriptEntity var1) {
        this.mob.method_636(var1.entity);
    }

    public ScriptEntity getTarget() {
        return ScriptEntity.getEntityClass(this.mob.method_634());
    }

    public boolean hasPath() {
        return this.mob.method_633();
    }

    public void pathToEntity(ScriptEntity var1) {
        this.mob.setTarget(this.mob.world.findPathTo(this.mob, var1.entity, 1.0F));
    }

    public void pathToBlock(int var1, int var2, int var3) {
        this.mob.setTarget(this.mob.world.method_189(this.mob, var1, var2, var3, 1.0F));
    }

    public boolean getCanForgetTargetRandomly() {
        return ((ExMobEntity) this.mob).getCanForgetTargetRandomly();
    }

    public void setCanForgetTargetRandomly(boolean var1) {
        ((ExMobEntity) this.mob).setCanForgetTargetRandomly(var1);
    }

    public boolean getCanPathRandomly() {
        return ((ExMobEntity) this.mob).getCanPathRandomly();
    }

    public void setCanPathRandomly(boolean var1) {
        ((ExMobEntity) this.mob).setCanPathRandomly(var1);
    }
}
