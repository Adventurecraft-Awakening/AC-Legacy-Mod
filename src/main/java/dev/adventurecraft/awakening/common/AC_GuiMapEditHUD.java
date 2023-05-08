package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class AC_GuiMapEditHUD extends Screen {

    private World world;
    private long clickedTime;
    private AC_GuiEditPalette palette;

    public AC_GuiMapEditHUD(World var1) {
        this.world = var1;
        AC_DebugMode.editMode = true;
        if (AC_DebugMode.mapEditing == null) {
            AC_DebugMode.mapEditing = new AC_MapEditing(Minecraft.instance, var1);
        } else {
            AC_DebugMode.mapEditing.updateWorld(var1);
        }

        this.palette = new AC_GuiEditPalette();
    }

    @Override
    public void initVanillaScreen() {
    }

    @Override
    protected void buttonClicked(ButtonWidget var1) {
    }

    @Override
    public void onKeyboardEvent() {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
                this.client.toggleFullscreen();
                return;
            }

            this.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }

        this.client.player.method_136(Keyboard.getEventKey(), Keyboard.getEventKeyState());
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        if (var2 == 1) {
            this.client.openScreen(null);
            this.client.lockCursor();
            AC_DebugMode.editMode = false;
        }
    }

    @Override
    protected void mouseClicked(int var1, int var2, int var3) {
        if (!this.palette.mouseClicked(var1, var2, var3, this.client, this.width, this.height)) {
            if (var3 == 0) {
                long var4 = this.world.getWorldTime();
                if (this.clickedTime != var4) {
                    AC_DebugMode.mapEditing.paint();
                }
            } else if (var3 == 1) {
                this.client.mouseHelper.grabCursor();
                this.client.hasFocus = true;
            }
        }
    }

    @Override
    protected void mouseReleased(int var1, int var2, int var3) {
        if (this.lastClickedButton != null && var3 == 0) {
            this.lastClickedButton.mouseReleased(var1, var2);
            this.lastClickedButton = null;
        } else if (var3 == 1) {
            this.client.hasFocus = false;
            this.client.mouseHelper.ungrabCursor();
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        super.render(var1, var2, var3);
        this.palette.drawPalette(this.client, this.textRenderer, this.width, this.height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
