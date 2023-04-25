package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;
import net.minecraft.item.ItemStack;

public class AC_GuiStoreDebug extends Screen {
    private AC_TileEntityStore store;
    private GuiSlider2 supply;

    public AC_GuiStoreDebug(AC_TileEntityStore var1) {
        this.store = var1;
    }

    public void initVanillaScreen() {
        OptionButtonWidget var1 = new OptionButtonWidget(0, 4, 0, "Set Items");
        this.buttons.add(var1);
        this.supply = new GuiSlider2(6, 4, 26, 10, String.format("Supply: %d", this.store.buySupply), (float) this.store.buySupply / 9.0F);
        if (this.store.buySupply == -1) {
            this.supply.text = "Supply: Infinite";
            this.supply.sliderValue = 0.0F;
        }

        this.buttons.add(this.supply);
        var1 = new OptionButtonWidget(1, 4, 48, "Set Trade Trigger");
        if (this.store.tradeTrigger != null) {
            var1.text = "Clear Trade Trigger";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            ItemStack var2 = this.client.player.getHeldItem();
            if (var2 != null) {
                this.store.buyItemID = var2.itemId;
                this.store.buyItemDamage = var2.getMeta();
                this.store.buyItemAmount = var2.count;
            } else {
                this.store.buyItemID = 0;
            }

            var2 = ((ExPlayerInventory)this.client.player.inventory).getOffhandItemStack();
            if (var2 != null) {
                this.store.sellItemID = var2.itemId;
                this.store.sellItemDamage = var2.getMeta();
                this.store.sellItemAmount = var2.count;
            } else {
                this.store.sellItemID = 0;
            }
        } else if (var1.id == 1) {
            if (this.store.tradeTrigger != null) {
                this.store.tradeTrigger = null;
                var1.text = "Set Trade Trigger";
            } else {
                this.store.tradeTrigger = new AC_TriggerArea(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ, AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
                var1.text = "Clear Trade Trigger";
            }
        }

    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.store.buySupply = (int) (this.supply.sliderValue * 9.0F);
        if (this.store.buySupply != 0) {
            this.supply.text = String.format("Supply: %d", this.store.buySupply);
        } else {
            this.supply.text = "Supply: Infinite";
            this.store.buySupply = -1;
        }

        this.store.buySupplyLeft = this.store.buySupply;
        super.render(var1, var2, var3);
        ((ExMinecraft)this.client).getStoreGUI().setBuyItem(this.store.buyItemID, this.store.buyItemAmount, this.store.buyItemDamage);
        ((ExMinecraft)this.client).getStoreGUI().setSellItem(this.store.sellItemID, this.store.sellItemAmount, this.store.sellItemDamage);
        ((ExMinecraft)this.client).getStoreGUI().setSupplyLeft(this.store.buySupply);
        ((ExMinecraft)this.client).updateStoreGUI();
        ((ExMinecraft)this.client).getStoreGUI().render(var1, var2, var3);
        this.store.world.getChunk(this.store.x, this.store.z).method_885();
    }

    public static void showUI(AC_TileEntityStore var0) {
        Minecraft.instance.openScreen(new AC_GuiStoreDebug(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
