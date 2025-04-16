package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityStore;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.GuiSlider2;
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

    public AC_GuiStoreDebug(AC_TileEntityStore entity) {
        this.store = entity;
    }

    public void init() {
        this.buttons.add(new OptionButton(0, 4, 0, "Set Items"));

        this.supply = new GuiSlider2(6, 4, 26, 10, String.format("Supply: %d", this.store.buySupply), (float) this.store.buySupply / 9.0F);
        if (this.store.buySupply == -1) {
            this.supply.message = "Supply: Infinite";
            this.supply.sliderValue = 0.0F;
        }
        this.buttons.add(this.supply);

        var var1 = new OptionButton(1, 4, 48, "Set Trade Trigger");
        if (this.store.tradeTrigger != null) {
            var1.message = "Clear Trade Trigger";
        }
        this.buttons.add(var1);
    }

    protected void buttonClicked(Button button) {
        if (button.id == 0) {
            ItemInstance item = this.minecraft.player.getSelectedItem();
            if (item != null) {
                this.store.buyItemID = item.id;
                this.store.buyItemDamage = item.getAuxValue();
                this.store.buyItemAmount = item.count;
            } else {
                this.store.buyItemID = 0;
            }

            item = ((ExPlayerInventory) this.minecraft.player.inventory).getOffhandItemStack();
            if (item != null) {
                this.store.sellItemID = item.id;
                this.store.sellItemDamage = item.getAuxValue();
                this.store.sellItemAmount = item.count;
            } else {
                this.store.sellItemID = 0;
            }
        } else if (button.id == 1) {
            if (this.store.tradeTrigger != null) {
                this.store.tradeTrigger = null;
                button.message = "Set Trade Trigger";
            } else {
                this.store.tradeTrigger = new AC_TriggerArea(
                    AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ,
                    AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
                button.message = "Clear Trade Trigger";
            }
        }
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.store.buySupply = (int) (this.supply.sliderValue * 9.0F);
        if (this.store.buySupply != 0) {
            this.supply.message = String.format("Supply: %d", this.store.buySupply);
        } else {
            this.supply.message = "Supply: Infinite";
            this.store.buySupply = -1;
        }

        this.store.buySupplyLeft = this.store.buySupply;
        super.render(mouseX, mouseY, tick);

        AC_GuiStore gui = ((ExMinecraft) this.minecraft).getStoreGUI();
        gui.setBuyItem(this.store.buyItemID, this.store.buyItemAmount, this.store.buyItemDamage);
        gui.setSellItem(this.store.sellItemID, this.store.sellItemAmount, this.store.sellItemDamage);
        gui.setSupplyLeft(this.store.buySupply);
        ((ExMinecraft) this.minecraft).updateStoreGUI();
        gui.render(mouseX, mouseY, tick);
    }

    @Override
    public void removed() {
        super.removed();

        this.store.setChanged();
    }

    public static void showUI(AC_TileEntityStore var0) {
        Minecraft.instance.setScreen(new AC_GuiStoreDebug(var0));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
