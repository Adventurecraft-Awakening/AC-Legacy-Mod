package dev.adventurecraft.awakening.mixin.entity.monster;

import dev.adventurecraft.awakening.extension.entity.monster.ExSlimeEntity;
import dev.adventurecraft.awakening.mixin.entity.MixinMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class MixinSlimeEntity extends MixinMob implements ExSlimeEntity {

    @Shadow
    public abstract int getSize();

    private int attackStrength;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        attackStrength = -1;
    }

    @Inject(
        method = "serverAiStep",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/Slime;zza:F",
            shift = At.Shift.AFTER))
    private void reduceVelocity(CallbackInfo ci) {
        float var2 = (float) Math.sqrt(this.xxa * this.xxa + this.zza * this.zza);
        this.xxa /= var2;
        this.zza /= var2;
    }

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void disableSplitOnDeath(CallbackInfo ci) {
        super.remove();
        ci.cancel();
    }

    @Overwrite
    public void playerTouch(Player var1) {
        int size = this.getSize();
        int strength = size;
        if (this.attackStrength != -1) {
            strength = this.attackStrength;
        }

        if ((size > 1 || this.attackStrength != -1) &&
            this.canSee(var1) &&
            this.distanceTo(var1) < 0.6D * size &&
            var1.hurt((Entity) (Object) this, strength)) {

            this.level.playSound(
                (Entity) (Object) this,
                "mob.slimeattack",
                1.0F,
                (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
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
