package dev.adventurecraft.awakening.mixin.entity;

import dev.adventurecraft.awakening.extension.entity.ExFlyingEntity;
import net.minecraft.world.entity.FlyingMob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlyingMob.class)
public abstract class MixinFlyingEntity extends MixinMob implements ExFlyingEntity {

    private int attackStrength = 1;

    @Override
    public int getAttackStrength() {
        return this.attackStrength;
    }

    @Override
    public void setAttackStrength(int value) {
        this.attackStrength = value;
    }
}
