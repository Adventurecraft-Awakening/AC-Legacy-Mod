package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.item.AC_ISlotCallbackItem;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    public abstract boolean isStackedByData();

    @Environment(EnvType.CLIENT)
    @Redirect(
        method = "getIcon(Lnet/minecraft/world/ItemInstance;)I",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/ItemInstance;getAuxValue()I"))
    private int defaultMetaForTexPos(ItemInstance instance) {
        if (instance != null) {
            return instance.getAuxValue();
        }
        return 0;
    }

    @Override
    public void onAddToSlot(Player player, int slotId, ItemInstance stack) {
        var world = (ExWorld) player.level;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.isStackedByData()) {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d_%d.js", this.id, stack.getAuxValue()), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d.js", this.id), scope, false);
        }
    }

    @Override
    public void onRemovedFromSlot(Player player, int slotId, ItemInstance stack) {
        var world = (ExWorld) player.level;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.isStackedByData()) {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d_%d.js", this.id, stack.getAuxValue()), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d.js", this.id), scope, false);
        }
    }
}
