package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityUrl;
import dev.adventurecraft.awakening.common.ClipboardHandler;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.input.Keyboard;

public class AC_GuiUrl extends Screen {
    private AC_TileEntityUrl msg;

    public AC_GuiUrl(AC_TileEntityUrl var2) {
        this.msg = var2;
    }

    public void init() {
    }

    protected void buttonClicked(Button var1) {
    }

    protected void keyPressed(char ch, int key) {
        super.keyPressed(ch, key);

        boolean noModifier = !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) &&
            !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) &&
            !Keyboard.isKeyDown(Keyboard.KEY_LMETA) &&
            !Keyboard.isKeyDown(Keyboard.KEY_RMETA);

        String url = this.msg.url;
        if ((key != 47) || noModifier) {
            if (key == 14 && !url.isEmpty()) {
                url = this.msg.url.substring(0, url.length() - 1);
            }

            if (SharedConstants.acceptableLetters.indexOf(ch) >= 0) {
                url = url + ch;
            }
        } else {
            url = ClipboardHandler.getClipboard();
        }

        if (!this.msg.url.equals(url)) {
            this.msg.url = url;
            this.msg.setChanged();
        }
    }

    public void render(int mouseX, int mouseY, float tick) {
        this.renderBackground();
        this.drawString(this.font, String.format("Url: '§3%s§f'", this.msg.url), 4, 4, 14737632);
        super.render(mouseX, mouseY, tick);
    }

    public static void showUI(AC_TileEntityUrl entity) {
        Minecraft.instance.setScreen(new AC_GuiUrl(entity));
    }
}
