package dev.adventurecraft.awakening.common;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widgets.OptionButtonWidget;

public class AC_GuiUrlRequest extends Screen {
    private String url;
    private String msg;

    public AC_GuiUrlRequest(String var1) {
        this(var1, "The map wants you to goto");
    }

    public AC_GuiUrlRequest(String var1, String var2) {
        this.url = var1;
        this.msg = var2;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        this.buttons.add(new OptionButtonWidget(0, this.width / 2 - 75, this.height / 2 + 10, "Open URL"));
        this.buttons.add(new OptionButtonWidget(1, this.width / 2 - 75, this.height / 2 + 32, "Don\'t Open"));
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            Desktop var2 = Desktop.getDesktop();
            if (var2.isSupported(Action.BROWSE)) {
                try {
                    var2.browse(new URI(this.url));
                } catch (IOException var4) {
                    var4.printStackTrace();
                } catch (URISyntaxException var5) {
                    var5.printStackTrace();
                }
            }
        }

        this.client.openScreen((Screen) null);
    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        this.drawTextWithShadow(this.textRenderer, this.msg, this.width / 2 - this.textRenderer.getTextWidth(this.msg) / 2, this.height / 2 - 15, 14737632);
        this.drawTextWithShadow(this.textRenderer, this.url, this.width / 2 - this.textRenderer.getTextWidth(this.url) / 2, this.height / 2, 14737632);
        super.render(var1, var2, var3);
    }

    public static void showUI(String var0) {
        Minecraft.instance.openScreen(new AC_GuiUrlRequest(var0));
    }

    public static void showUI(String var0, String var1) {
        Minecraft.instance.openScreen(new AC_GuiUrlRequest(var0, var1));
    }

    public boolean isPauseScreen() {
        return true;
    }
}
