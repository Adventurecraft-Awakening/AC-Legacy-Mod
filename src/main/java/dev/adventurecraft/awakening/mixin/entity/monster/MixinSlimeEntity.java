package dev.adventurecraft.awakening.mixin.entity.monster;

import dev.adventurecraft.awakening.extension.entity.monster.ExSlimeEntity;
import dev.adventurecraft.awakening.mixin.entity.MixinLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeEntity.class)
public abstract class MixinSlimeEntity extends MixinLivingEntity implements ExSlimeEntity {

    @Shadow
    public abstract int getSize();

    private int attackStrength;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(World var1, CallbackInfo ci) {
        attackStrength = -1;
    }

    @Inject(
        method = "tickHandSwing",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/monster/SlimeEntity;forwardVelocity:F",
            shift = At.Shift.AFTER))
    private void reduceVelocity(CallbackInfo ci) {
        float var2 = (float) Math.sqrt(this.horizontalVelocity * this.horizontalVelocity + this.forwardVelocity * this.forwardVelocity);
        this.horizontalVelocity /= var2;
        this.forwardVelocity /= var2;
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void disableSplitOnDeath(CallbackInfo ci) {
        super.remove();
        ci.cancel();
    }

    @Overwrite
    public void onPlayerCollision(PlayerEntity var1) {
        int size = this.getSize();
        int strength = size;
        if (this.attackStrength != -1) {
            strength = this.attackStrength;
        }

        if ((size > 1 || this.attackStrength != -1) &&
            this.method_928(var1) &&
            this.distanceTo(var1) < 0.6D * size &&
            var1.damage((Entity) (Object) this, strength)) {

            this.world.playSound(
                (Entity) (Object) this,
                "mob.slimeattack",
                1.0F,
                (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    @Override
    public int getAttackStrength() {
        return this.attackStrength;
    }

    @Override
    public void setAttackStrength(int value) {
        this.attackStrength = value;
    }
}
