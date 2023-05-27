package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.extension.item.ExItem;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.mozilla.javascript.Scriptable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Item.class)
public abstract class MixinItem implements ExItem {

    public boolean decrementDamage;
    public int itemUseDelay = 5;

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
    public boolean getDecrementDamage() {
        return this.decrementDamage;
    }

    @Override
    public void setDecrementDamage(boolean value) {
        this.decrementDamage = value;
    }

    @Override
    public int getItemUseDelay() {
        return this.itemUseDelay;
    }

    @Override
    public void setItemUseDelay(int value) {
        this.itemUseDelay = value;
    }

    @Override
    public void onAddToSlot(PlayerEntity player, int slotId, int itemMeta) {
        var world = (ExWorld) Minecraft.instance.world;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.usesMeta()) {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d_%d.js", this.id, itemMeta), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onAddToSlot_%d.js", this.id), scope, false);
        }
    }

    @Override
    public void onRemovedFromSlot(PlayerEntity player, int slotId, int itemMeta) {
        var world = (ExWorld) Minecraft.instance.world;
        Scriptable scope = world.getScope();
        scope.put("slotID", scope, slotId);
        if (this.usesMeta()) {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d_%d.js", this.id, itemMeta), scope, false);
        } else {
            world.getScriptHandler().runScript(String.format("item_onRemovedFromSlot_%d.js", this.id), scope, false);
        }
    }
}
