package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.Boat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class MixinBoatEntity extends Entity {

    public MixinBoatEntity(Level arg) {
        super(arg);
    }

    @Redirect(method = "hurt", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/Boat;spawnAtLocation(IIF)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity disableDropItemOnDamage(Boat instance, int var1, int var2, float var3) {
        return null;
    }

    @Inject(method = "hurt", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/Boat;remove()V",
            shift = At.Shift.AFTER))
    private void dropBoatItemOnDamage(Entity var1, int var2, CallbackInfoReturnable<Boolean> cir) {
        this.dropBoatItem();
    }

    @Redirect(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/Boat;spawnAtLocation(IIF)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity disableDropItemOnTick(Boat instance, int var1, int var2, float var3) {
        return null;
    }

    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/item/Boat;remove()V",
            shift = At.Shift.AFTER))
    private void dropBoatItemOnTick(CallbackInfo ci) {
        this.dropBoatItem();
    }

    private void dropBoatItem() {
        this.spawnAtLocation(Item.BOAT.id, 1, 0.0F);
    }
}
