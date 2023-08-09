package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_ILeftClickItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SwordItem.class)
public abstract class MixinSwordItem extends MixinItem implements AC_ILeftClickItem {

    @Redirect(
        method = {"postHit", "postMine"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;applyDamage(ILnet/minecraft/entity/Entity;)V"))
    private void disableItemDamage(ItemStack instance, int arg, Entity entity) {
    }

    @Override
    public boolean mainActionLeftClick() {
        return true;
    }
}
