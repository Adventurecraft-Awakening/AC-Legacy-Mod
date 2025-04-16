package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;
import dev.adventurecraft.awakening.mixin.entity.MixinLivingEntity;
import dev.adventurecraft.awakening.script.ScriptEntity;
import net.minecraft.world.entity.Entity;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AC_EntityLivingScript.class)
public abstract class MixinAC_EntityLivingScript extends MixinLivingEntity {

    @Shadow(remap = false)
    protected Scriptable scope;

    @Shadow(remap = false)
    protected abstract boolean runOnAttackedScript();

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        Object jsEntity = Context.javaToJS(ScriptEntity.getEntityClass(entity), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingEntity", jsEntity);
        Object jsDamage = Context.javaToJS(damage, this.scope);
        ScriptableObject.putProperty(this.scope, "attackingDamage", jsDamage);
        return this.runOnAttackedScript() && super.attackEntityFromMulti(entity, damage);
    }
}
