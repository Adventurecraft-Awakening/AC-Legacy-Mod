package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityNpcPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiNpcPath extends Screen {

    private AC_TileEntityNpcPath path;

    public AC_GuiNpcPath(AC_TileEntityNpcPath var1) {
        this.path = var1;
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        String var1 = "<Unselected>";
        AC_EntityNPC var2 = AC_TileEntityNpcPath.lastEntity;
        if (var2 != null) {
            var1 = var2.npcName;
        }

        this.buttons.add(new OptionButton(0, 4, 20, String.format("Set Path NPC: %s", var1)));
        this.buttons.add(new OptionButton(1, 4, 80, "Use Current Selection"));
        this.buttons.add(new OptionButton(2, 4, 100, "Reset Target"));
    }

    @Override
    protected void buttonClicked(Button var1) {
        if (var1.id == 0) {
            this.path.setEntityToLastSelected();
        } else if (var1.id == 1) {
            this.path.minX = AC_ItemCursor.minX;
            this.path.minY = AC_ItemCursor.minY;
            this.path.minZ = AC_ItemCursor.minZ;
            this.path.maxX = AC_ItemCursor.maxX;
            this.path.maxY = AC_ItemCursor.maxY;
            this.path.maxZ = AC_ItemCursor.maxZ;
        } else if (var1.id == 2) {
            this.path.minX = 0;
            this.path.minY = 0;
            this.path.minZ = 0;
            this.path.maxX = 0;
            this.path.maxY = 0;
            this.path.maxZ = 0;
        }

        this.path.setChanged();
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        String var4 = "<Not Set>";
        AC_EntityNPC var5 = this.path.getNPC();
        if (var5 != null) {
            var4 = var5.npcName;
        }

        this.drawString(this.font, String.format("NPC: %s", var4), 4, 4, 14737632);
        this.drawString(this.font, String.format("Min: (%d, %d, %d)", this.path.minX, this.path.minY, this.path.minZ), 4, 44, 14737632);
        this.drawString(this.font, String.format("Max: (%d, %d, %d)", this.path.maxX, this.path.maxY, this.path.maxZ), 4, 64, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityNpcPath var0) {
        Minecraft.instance.setScreen(new AC_GuiNpcPath(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
