package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.entity.BoatEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public abstract class MixinBoatEntity extends Entity {

    public MixinBoatEntity(World arg) {
        super(arg);
    }

    @Redirect(method = "damage", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/BoatEntity;dropItem(IIF)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity disableDropItemOnDamage(BoatEntity instance, int var1, int var2, float var3) {
        return null;
    }

    @Inject(method = "damage", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/BoatEntity;remove()V",
            shift = At.Shift.AFTER))
    private void dropBoatItemOnDamage(Entity var1, int var2, CallbackInfoReturnable<Boolean> cir) {
        this.dropBoatItem();
    }

    @Redirect(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/BoatEntity;dropItem(IIF)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity disableDropItemOnTick(BoatEntity instance, int var1, int var2, float var3) {
        return null;
    }

    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/BoatEntity;remove()V",
            shift = At.Shift.AFTER))
    private void dropBoatItemOnTick(CallbackInfo ci) {
        this.dropBoatItem();
    }

    private void dropBoatItem() {
        this.dropItem(Item.BOAT.id, 1, 0.0F);
    }
}
