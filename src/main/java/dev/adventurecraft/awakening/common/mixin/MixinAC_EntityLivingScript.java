package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;
import dev.adventurecraft.awakening.mixin.entity.MixinMob;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AC_EntityLivingScript.class, remap = false)
public abstract class MixinAC_EntityLivingScript extends MixinMob {

    @Shadow protected abstract void prepareAttackScript(Entity entity, int damage);

    @Shadow
    protected abstract boolean runOnAttackedScript();

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        this.prepareAttackScript(entity, damage);
        return this.runOnAttackedScript() && super.attackEntityFromMulti(entity, damage);
    }
}
