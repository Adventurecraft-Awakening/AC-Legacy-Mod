package dev.adventurecraft.awakening.common.mixin;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AC_EntityNPC.class, remap = false)
public abstract class MixinAC_EntityNPC extends MixinAC_EntityLivingScript {

    @Shadow
    public boolean isAttackable;

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        return this.isAttackable && super.attackEntityFromMulti(entity, damage);
    }
}
