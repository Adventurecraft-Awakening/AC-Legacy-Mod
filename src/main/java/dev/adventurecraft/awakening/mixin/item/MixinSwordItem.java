package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_ILeftClickItem;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.WeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WeaponItem.class)
public abstract class MixinSwordItem extends MixinItem implements AC_ILeftClickItem {

    @Redirect(
        method = {"hurtEnemy", "mineBlock"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/ItemInstance;hurtAndBreak(ILnet/minecraft/world/entity/Entity;)V"))
    private void disableItemDamage(ItemInstance instance, int arg, Entity entity) {
    }

    @Override
    public boolean mainActionLeftClick() {
        return true;
    }
}
