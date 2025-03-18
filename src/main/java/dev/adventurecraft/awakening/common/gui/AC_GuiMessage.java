package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMessage;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.Level;
import java.util.ArrayList;

public class AC_GuiMessage extends Screen {

    private AC_TileEntityMessage msg;
    private Level world;
    private int page;

    public AC_GuiMessage(Level world, AC_TileEntityMessage msg) {
        this.world = world;
        this.msg = msg;
    }

    public void init() {
        ArrayList<String> soundList = ((ExWorld) this.world).getSoundList();

        int soundsPerPage = 3 * ((this.height - 60) / 20);
        for (int i = 0; i + soundsPerPage * this.page - 1 < soundList.size() && i < soundsPerPage; ++i) {
            String name = "None";
            if (i != 0 || this.page != 0) {
                name = soundList.get(i + soundsPerPage * this.page - 1);
            }

            int x = 4 + i % 3 * this.width / 3;
            int y = 60 + i / 3 * 20;
            int w = (this.width - 16) / 3;
            
            var button = new Button(i, x, y, w, 18, name);
            this.buttons.add(button);
        }

        int pageCount = soundList.size() / soundsPerPage + 1;
        for (int i = 0; i < pageCount; ++i) {
            var button = new Button(100 + i, 4 + i * 50, 40, 46, 18, String.format("Page %d", i + 1));
            this.buttons.add(button);
        }
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 0 && this.page == 0) {
            this.msg.sound = "";
        } else if (button.id < 100) {
            int soundId = 3 * ((this.height - 60) / 20);
            this.msg.sound = ((ExWorld) this.world).getSoundList().get(button.id - 1 + soundId * this.page);
        } else {
            this.page = button.id - 100;
            this.buttons.clear();
            this.init();
        }

        this.world.getChunkAt(this.msg.x, this.msg.z).markUnsaved();
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (var2 == 14 && this.msg.message.length() > 0) {
            this.msg.message = this.msg.message.substring(0, this.msg.message.length() - 1);
        }

        if (SharedConstants.acceptableLetters.indexOf(var1) >= 0 && this.msg.message.length() < 30) {
            this.msg.message = this.msg.message + var1;
        }
    }

    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        this.drawString(this.font, String.format("Message: '%s'", this.msg.message), 4, 4, 14737632);
        if (!this.msg.sound.equals("")) {
            this.drawString(this.font, String.format("Sound: %s", this.msg.sound), 4, 24, 14737632);
        } else {
            this.drawString(this.font, "Sound: None", 4, 24, 14737632);
        }

        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(Level var0, AC_TileEntityMessage var1) {
        Minecraft.instance.setScreen(new AC_GuiMessage(var0, var1));
    }
}
