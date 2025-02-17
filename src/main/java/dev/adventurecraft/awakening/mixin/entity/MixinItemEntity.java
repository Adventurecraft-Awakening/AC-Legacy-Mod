package dev.adventurecraft.awakening.mixin.entity;

import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity {

    @Shadow
    public ItemInstance item;

    public MixinItemEntity(Level arg) {
        super(arg);
    }

    @Redirect(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/ItemInstance;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        ))
    private double useFastRandomInInit() {
        return this.random.nextFloat();
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/ItemInstance;)V",
        at = @At("TAIL"))
    private void removeIfEmpty(Level d, double x, double y, double z, ItemInstance instance, CallbackInfo ci) {
        if (Item.items[this.item.id] == null) {
            this.remove();
        }
    }
}
