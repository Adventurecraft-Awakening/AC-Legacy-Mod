package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.mozilla.javascript.Scriptable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public abstract class MixinItem implements AC_ISlotCallbackItem {

    @Shadow
    @Final
    public int id;

    @Shadow
    public abstract boolean usesMeta();

    @Redirect(method = "getTexturePosition(Lnet/minecraft/item/ItemStack;)I", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/item/ItemStack;getMeta()I"))
    private int defaultMetaForTexPos(ItemStack instance) {
        if (instance != null) {
            return instance.getMeta();
        }
        return 0;
    }

    @Override
    public void onAddToSlot(PlayerEntity player, int slotId, ItemStack stack) {
        var world = (ExWorld) player.world;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.usesMeta()) {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d_%d.js", this.id, stack.getMeta()), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d.js", this.id), scope, false);
        }
    }

    @Override
    public void onRemovedFromSlot(PlayerEntity player, int slotId, ItemStack stack) {
        var world = (ExWorld) player.world;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.usesMeta()) {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d_%d.js", this.id, stack.getMeta()), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d.js", this.id), scope, false);
        }
    }
}
