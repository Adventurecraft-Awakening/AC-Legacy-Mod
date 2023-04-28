package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.entity.FlyingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlyingEntity.class)
public abstract class MixinFlyingEntity extends MixinLivingEntity {

    public int attackStrength = 1;
}
