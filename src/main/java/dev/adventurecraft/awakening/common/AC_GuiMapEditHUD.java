package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import org.lwjgl.input.Keyboard;

public class AC_GuiMapEditHUD extends Screen {

    private Level world;
    private long clickedTime;
    private AC_GuiEditPalette palette;

    public AC_GuiMapEditHUD(Level var1) {
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
    public void init() {
    }

    @Override
    protected void buttonClicked(Button var1) {
    }

    @Override
    public void keyboardEvent() {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
                this.minecraft.toggleFullScreen();
                return;
            }

            this.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }

        this.minecraft.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        if (var2 == 1) {
            this.minecraft.setScreen(null);
            this.minecraft.grabMouse();
            AC_DebugMode.editMode = false;
        }
    }

    @Override
    protected void mouseClicked(int var1, int var2, int var3) {
        if (!this.palette.mouseClicked(var1, var2, var3, this.minecraft, this.width, this.height)) {
            if (var3 == 0) {
                long var4 = this.world.getTime();
                if (this.clickedTime != var4) {
                    AC_DebugMode.mapEditing.paint();
                }
            } else if (var3 == 1) {
                this.minecraft.mouseHandler.grab();
                this.minecraft.mouseGrabbed = true;
            }
        }
    }

    @Override
    protected void mouseReleased(int var1, int var2, int var3) {
        if (this.clickedButton != null && var3 == 0) {
            this.clickedButton.mouseReleased(var1, var2);
            this.clickedButton = null;
        } else if (var3 == 1) {
            this.minecraft.mouseGrabbed = false;
            this.minecraft.mouseHandler.release();
        }
    }

    @Override
    public void render(int var1, int var2, float var3) {
        super.render(var1, var2, var3);
        this.palette.drawPalette(this.minecraft, this.font, this.width, this.height);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
