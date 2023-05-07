package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import net.minecraft.entity.FlyingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlyingEntity.class)
public abstract class MixinFlyingEntity extends MixinLivingEntity implements ExFlyingEntity {

    private int attackStrength = 1;

    @Override
    public int getAttackStrength() {
        return this.attackStrength;
    }
}
