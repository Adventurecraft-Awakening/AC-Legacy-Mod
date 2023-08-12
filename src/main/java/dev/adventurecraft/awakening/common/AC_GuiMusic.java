package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.World;

import java.util.ArrayList;

public class AC_GuiMusic extends Screen {

    private World world;
    private AC_TileEntityMusic music;
    private GuiSlider2 fadeOut;
    private GuiSlider2 fadeIn;
    private int page;

    public AC_GuiMusic(World world, AC_TileEntityMusic entity) {
        this.world = world;
        this.music = entity;
    }

    public void tick() {
    }

    public void initVanillaScreen() {
        ArrayList<String> musicList = ((ExWorld) this.world).getMusicList();

        int musicPerPage = 3 * ((this.height - 60) / 20);
        for (int i = 0; i + musicPerPage * this.page <= musicList.size() && i < musicPerPage; ++i) {
            String name = "Stop Music";
            if (i != 0 || this.page != 0) {
                name = musicList.get(i - 1 + musicPerPage * this.page);
            }

            int x = 4 + i % 3 * this.width / 3;
            int y = 60 + i / 3 * 20;
            int w = (this.width - 16) / 3;

            var button = new ButtonWidget(i, x, y, w, 18, name);
            this.buttons.add(button);
        }

        this.fadeOut = new GuiSlider2(200, 4, 16, 10, String.format("Fade Out: %d", this.music.fadeOut), (float) this.music.fadeOut / 5000.0F);
        this.fadeIn = new GuiSlider2(201, this.width / 2, 16, 10, String.format("Fade In: %d", this.music.fadeIn), (float) this.music.fadeIn / 5000.0F);
        this.buttons.add(this.fadeOut);
        this.buttons.add(this.fadeIn);

        int pageCount = (musicList.size() - 1) / musicPerPage + 1;
        for (int i = 0; i < pageCount; ++i) {
            var button = new ButtonWidget(100 + i, 4 + i * 50, 40, 46, 18, String.format("Page %d", i + 1));
            this.buttons.add(button);
        }
    }

    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0 && this.page == 0) {
            this.music.musicName = "";
        } else if (button.id < 100) {
            int var2 = 3 * ((this.height - 60) / 20);
            this.music.musicName = ((ExWorld) this.world).getMusicList().get(button.id + var2 * this.page - 1);
        } else if (button.id < 200) {
            this.page = button.id - 100;
            this.buttons.clear();
            this.initVanillaScreen();
        }
    }

    public void render(int mouseX, int mouseY, float deltaTime) {
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
        super.render(mouseX, mouseY, deltaTime);
        this.world.getChunk(this.music.x, this.music.z).method_885();
    }

    public static void showUI(World world, AC_TileEntityMusic entity) {
        Minecraft.instance.openScreen(new AC_GuiMusic(world, entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
