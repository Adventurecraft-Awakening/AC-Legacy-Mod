package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;

public class AC_GuiMusic extends Screen {
    private World world;
    private AC_TileEntityMusic music;
    private GuiSlider2 fadeOut;
    private GuiSlider2 fadeIn;
    private int page;

    public AC_GuiMusic(World var1, AC_TileEntityMusic var2) {
        this.world = var1;
        this.music = var2;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        String[] musicList = ((ExWorld) this.world).getMusicList();

        int var1 = 3 * ((this.height - 60) / 20);

        int var2;
        ButtonWidget var4;
        for (var2 = 0; var2 + var1 * this.page <= musicList.length && var2 < var1; ++var2) {
            String var3 = "Stop Music";
            if (var2 != 0 || this.page != 0) {
                var3 = musicList[var2 - 1 + var1 * this.page];
            }

            var4 = new ButtonWidget(var2, 4 + var2 % 3 * this.width / 3, 60 + var2 / 3 * 20, (this.width - 16) / 3, 18, var3);
            this.buttons.add(var4);
        }

        this.fadeOut = new GuiSlider2(200, 4, 16, 10, String.format("Fade Out: %d", this.music.fadeOut), (float) this.music.fadeOut / 5000.0F);
        this.fadeIn = new GuiSlider2(201, this.width / 2, 16, 10, String.format("Fade In: %d", this.music.fadeIn), (float) this.music.fadeIn / 5000.0F);
        this.buttons.add(this.fadeOut);
        this.buttons.add(this.fadeIn);
        var2 = (musicList.length - 1) / var1 + 1;

        for (int var5 = 0; var5 < var2; ++var5) {
            var4 = new ButtonWidget(100 + var5, 4 + var5 * 50, 40, 46, 18, String.format("Page %d", var5 + 1));
            this.buttons.add(var4);
        }

    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0 && this.page == 0) {
            this.music.musicName = "";
        } else if (var1.id < 100) {
            int var2 = 3 * ((this.height - 60) / 20);
            this.music.musicName = ((ExWorld) this.world).getMusicList()[var1.id + var2 * this.page - 1];
        } else if (var1.id < 200) {
            this.page = var1.id - 100;
            this.buttons.clear();
            this.initVanillaScreen();
        }

    }

    public void render(int var1, int var2, float var3) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.music.musicName.equals("")) {
            this.drawTextWithShadow(this.textRenderer, "Music: Stop Music", 4, 4, 14737632);
        } else {
            this.drawTextWithShadow(this.textRenderer, String.format("Music: %s", this.music.musicName), 4, 4, 14737632);
        }

        this.music.fadeOut = (int) (this.fadeOut.sliderValue * 5000.0F + 0.5F);
        this.fadeOut.text = String.format("Fade Out: %d", this.music.fadeOut);
        this.music.fadeIn = (int) (this.fadeIn.sliderValue * 5000.0F + 0.5F);
        this.fadeIn.text = String.format("Fade In: %d", this.music.fadeIn);
        super.render(var1, var2, var3);
        this.world.getChunk(this.music.x, this.music.z).method_885();
    }

    public static void showUI(World var0, AC_TileEntityMusic var1) {
        Minecraft.instance.openScreen(new AC_GuiMusic(var0, var1));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
