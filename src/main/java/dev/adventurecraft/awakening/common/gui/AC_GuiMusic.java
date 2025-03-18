package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_TileEntityMusic;
import dev.adventurecraft.awakening.common.GuiSlider2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import java.util.ArrayList;

public class AC_GuiMusic extends Screen {

    private Level world;
    private AC_TileEntityMusic music;
    private GuiSlider2 fadeOut;
    private GuiSlider2 fadeIn;
    private int page;

    public AC_GuiMusic(Level world, AC_TileEntityMusic entity) {
        this.world = world;
        this.music = entity;
    }

    public void tick() {
    }

    public void init() {
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

            var button = new Button(i, x, y, w, 18, name);
            this.buttons.add(button);
        }

        this.fadeOut = new GuiSlider2(200, 4, 16, 10, String.format("Fade Out: %d", this.music.fadeOut), (float) this.music.fadeOut / 5000.0F);
        this.fadeIn = new GuiSlider2(201, this.width / 2, 16, 10, String.format("Fade In: %d", this.music.fadeIn), (float) this.music.fadeIn / 5000.0F);
        this.buttons.add(this.fadeOut);
        this.buttons.add(this.fadeIn);

        int pageCount = (musicList.size() - 1) / musicPerPage + 1;
        for (int i = 0; i < pageCount; ++i) {
            var button = new Button(100 + i, 4 + i * 50, 40, 46, 18, String.format("Page %d", i + 1));
            this.buttons.add(button);
        }
    }

    protected void buttonClicked(Button button) {
        if (button.id == 0 && this.page == 0) {
            this.music.musicName = "";
        } else if (button.id < 100) {
            int var2 = 3 * ((this.height - 60) / 20);
            this.music.musicName = ((ExWorld) this.world).getMusicList().get(button.id + var2 * this.page - 1);
        } else if (button.id < 200) {
            this.page = button.id - 100;
            this.buttons.clear();
            this.init();
        }
    }

    public void render(int mouseX, int mouseY, float deltaTime) {
        this.fill(0, 0, this.width, this.height, Integer.MIN_VALUE);
        if (this.music.musicName.equals("")) {
            this.drawString(this.font, "Music: Stop Music", 4, 4, 14737632);
        } else {
            this.drawString(this.font, String.format("Music: %s", this.music.musicName), 4, 4, 14737632);
        }

        this.music.fadeOut = (int) (this.fadeOut.sliderValue * 5000.0F + 0.5F);
        this.fadeOut.message = String.format("Fade Out: %d", this.music.fadeOut);
        this.music.fadeIn = (int) (this.fadeIn.sliderValue * 5000.0F + 0.5F);
        this.fadeIn.message = String.format("Fade In: %d", this.music.fadeIn);
        super.render(mouseX, mouseY, deltaTime);
        this.world.getChunkAt(this.music.x, this.music.z).markUnsaved();
    }

    public static void showUI(Level world, AC_TileEntityMusic entity) {
        Minecraft.instance.setScreen(new AC_GuiMusic(world, entity));
    }

    public boolean isPauseScreen() {
        return false;
    }
}
