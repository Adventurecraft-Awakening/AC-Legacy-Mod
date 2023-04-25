package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.CharacterUtils;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class AC_GuiUrl extends Screen {
    private AC_TileEntityUrl msg;
    private World world;

    public AC_GuiUrl(World var1, AC_TileEntityUrl var2) {
        this.world = var1;
        this.msg = var2;
    }

    public void initVanillaScreen() {
    }

    protected void buttonClicked(ButtonWidget var1) {
    }

    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (var2 != 47 || !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) && !Keyboard.isKeyDown(Keyboard.KEY_LMETA) && !Keyboard.isKeyDown(Keyboard.KEY_RMETA)) {
            if (var2 == 14 && this.msg.url.length() > 0) {
                this.msg.url = this.msg.url.substring(0, this.msg.url.length() - 1);
            }

            if (CharacterUtils.validCharacters.indexOf(var1) >= 0 && this.msg.url.length() < 30) {
                this.msg.url = this.msg.url + var1;
            }

            this.world.getChunk(this.msg.x, this.msg.z).method_885();
        } else {
            this.msg.url = ClipboardHandler.getClipboard();
            this.world.getChunk(this.msg.x, this.msg.z).method_885();
        }
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        this.drawTextWithShadow(this.textRenderer, String.format("Url: \'%s\'", this.msg.url), 4, 4, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(World var0, AC_TileEntityUrl var1) {
        Minecraft.instance.openScreen(new AC_GuiUrl(var0, var1));
    }
}
