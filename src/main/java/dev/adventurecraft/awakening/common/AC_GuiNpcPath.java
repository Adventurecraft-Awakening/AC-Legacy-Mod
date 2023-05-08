package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;

public class AC_GuiNpcPath extends Screen {

    private AC_TileEntityNpcPath path;

    public AC_GuiNpcPath(AC_TileEntityNpcPath var1) {
        this.path = var1;
    }

    @Override
    public void tick() {
    }

    @Override
    public void initVanillaScreen() {
        String var1 = "<Unselected>";
        AC_EntityNPC var2 = AC_TileEntityNpcPath.lastEntity;
        if (var2 != null) {
            var1 = var2.npcName;
        }

        this.buttons.add(new OptionButtonWidget(0, 4, 20, String.format("Set Path NPC: %s", var1)));
        this.buttons.add(new OptionButtonWidget(1, 4, 80, "Use Current Selection"));
        this.buttons.add(new OptionButtonWidget(2, 4, 100, "Reset Target"));
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
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

        this.client.world.getChunk(this.path.x, this.path.y).method_885();
    }

    @Override
    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        String var4 = "<Not Set>";
        AC_EntityNPC var5 = this.path.getNPC();
        if (var5 != null) {
            var4 = var5.npcName;
        }

        this.drawTextWithShadow(this.textRenderer, String.format("NPC: %s", var4), 4, 4, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Min: (%d, %d, %d)", this.path.minX, this.path.minY, this.path.minZ), 4, 44, 14737632);
        this.drawTextWithShadow(this.textRenderer, String.format("Max: (%d, %d, %d)", this.path.maxX, this.path.maxY, this.path.maxZ), 4, 64, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityNpcPath var0) {
        Minecraft.instance.openScreen(new AC_GuiNpcPath(var0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
