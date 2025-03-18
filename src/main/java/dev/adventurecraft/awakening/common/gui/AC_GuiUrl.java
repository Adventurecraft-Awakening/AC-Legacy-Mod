package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_TileEntityUrl;
import dev.adventurecraft.awakening.common.ClipboardHandler;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import org.lwjgl.input.Keyboard;

public class AC_GuiUrl extends Screen {
    private AC_TileEntityUrl msg;
    private Level world;

    public AC_GuiUrl(Level var1, AC_TileEntityUrl var2) {
        this.world = var1;
        this.msg = var2;
    }

    public void init() {
    }

    protected void buttonClicked(Button var1) {
    }

    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (var2 != 47 || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_LMETA) && !Keyboard.isKeyDown(Keyboard.KEY_RMETA)) {
            if (var2 == 14 && this.msg.url.length() > 0) {
                this.msg.url = this.msg.url.substring(0, this.msg.url.length() - 1);
            }

            if (SharedConstants.acceptableLetters.indexOf(var1) >= 0 && this.msg.url.length() < 30) {
                this.msg.url = this.msg.url + var1;
            }

            this.world.getChunkAt(this.msg.x, this.msg.z).markUnsaved();
        } else {
            this.msg.url = ClipboardHandler.getClipboard();
            this.world.getChunkAt(this.msg.x, this.msg.z).markUnsaved();
        }
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        this.drawString(this.font, String.format("Url: \'%s\'", this.msg.url), 4, 4, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(Level var0, AC_TileEntityUrl var1) {
        Minecraft.instance.setScreen(new AC_GuiUrl(var0, var1));
    }
}
