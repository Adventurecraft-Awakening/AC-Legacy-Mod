package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityNpcPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiNpcPath extends Screen {

    private AC_TileEntityNpcPath path;

    public AC_GuiNpcPath(AC_TileEntityNpcPath entity) {
        this.path = entity;
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        String name = "<Unselected>";
        AC_EntityNPC last = AC_TileEntityNpcPath.lastEntity;
        if (last != null) {
            name = last.npcName;
        }

        this.buttons.add(new OptionButton(0, 4, 20, String.format("Set Path NPC: %s", name)));
        this.buttons.add(new OptionButton(1, 4, 80, "Use Current Selection"));
        this.buttons.add(new OptionButton(2, 4, 100, "Reset Target"));
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 0) {
            this.path.setEntityToLastSelected();
        }
        else if (button.id == 1) {
            this.path.setMin(AC_ItemCursor.min());
            this.path.setMax(AC_ItemCursor.max());
        }
        else if (button.id == 2) {
            this.path.setMin(Coord.zero);
            this.path.setMax(Coord.zero);
        }

        this.path.setChanged();
    }

    @Override
    public void render(int mouseX, int mouseY, float tickTime) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        String name = "<Not Set>";
        AC_EntityNPC entity = this.path.getNPC();
        if (entity != null) {
            name = entity.npcName;
        }

        int color = 0xe0e0e0;
        this.drawString(this.font, String.format("NPC: %s", name), 4, 4, color);
        AC_GuiStrings.drawMinMax(this, this.path, 4, 44, color);
        super.render(mouseX, mouseY, tickTime);
    }

    public static void showUI(AC_TileEntityNpcPath var0) {
        Minecraft.instance.setScreen(new AC_GuiNpcPath(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
