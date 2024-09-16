package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.ItemInstance;

public class AC_GuiStoreDebug extends Screen {
    private AC_TileEntityStore store;
    private GuiSlider2 supply;

    public AC_GuiStoreDebug(AC_TileEntityStore var1) {
        this.store = var1;
    }

    public void init() {
        OptionButton var1 = new OptionButton(0, 4, 0, "Set Items");
        this.buttons.add(var1);
        this.supply = new GuiSlider2(6, 4, 26, 10, String.format("Supply: %d", this.store.buySupply), (float) this.store.buySupply / 9.0F);
        if (this.store.buySupply == -1) {
            this.supply.message = "Supply: Infinite";
            this.supply.sliderValue = 0.0F;
        }

        this.buttons.add(this.supply);
        var1 = new OptionButton(1, 4, 48, "Set Trade Trigger");
        if (this.store.tradeTrigger != null) {
            var1.message = "Clear Trade Trigger";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(Button var1) {
        if (var1.id == 0) {
            ItemInstance var2 = this.minecraft.player.getSelectedItem();
            if (var2 != null) {
                this.store.buyItemID = var2.id;
                this.store.buyItemDamage = var2.getAuxValue();
                this.store.buyItemAmount = var2.count;
            } else {
                this.store.buyItemID = 0;
            }

            var2 = ((ExPlayerInventory)this.minecraft.player.inventory).getOffhandItemStack();
            if (var2 != null) {
                this.store.sellItemID = var2.id;
                this.store.sellItemDamage = var2.getAuxValue();
                this.store.sellItemAmount = var2.count;
            } else {
                this.store.sellItemID = 0;
            }
        } else if (var1.id == 1) {
            if (this.store.tradeTrigger != null) {
                this.store.tradeTrigger = null;
                var1.message = "Set Trade Trigger";
            } else {
                this.store.tradeTrigger = new AC_TriggerArea(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ, AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
                var1.message = "Clear Trade Trigger";
            }
        }

    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.store.buySupply = (int) (this.supply.sliderValue * 9.0F);
        if (this.store.buySupply != 0) {
            this.supply.message = String.format("Supply: %d", this.store.buySupply);
        } else {
            this.supply.message = "Supply: Infinite";
            this.store.buySupply = -1;
        }

        this.store.buySupplyLeft = this.store.buySupply;
        super.render(var1, var2, var3);
        ((ExMinecraft)this.minecraft).getStoreGUI().setBuyItem(this.store.buyItemID, this.store.buyItemAmount, this.store.buyItemDamage);
        ((ExMinecraft)this.minecraft).getStoreGUI().setSellItem(this.store.sellItemID, this.store.sellItemAmount, this.store.sellItemDamage);
        ((ExMinecraft)this.minecraft).getStoreGUI().setSupplyLeft(this.store.buySupply);
        ((ExMinecraft)this.minecraft).updateStoreGUI();
        ((ExMinecraft)this.minecraft).getStoreGUI().render(var1, var2, var3);
        this.store.level.getChunkAt(this.store.x, this.store.z).markUnsaved();
    }

    public static void showUI(AC_TileEntityStore var0) {
        Minecraft.instance.setScreen(new AC_GuiStoreDebug(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
