package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.tile.entity.AC_TileEntityMessage;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;

public class AC_GuiMessage extends Screen {

    private AC_TileEntityMessage msg;
    private int page;

    public AC_GuiMessage(AC_TileEntityMessage msg) {
        this.msg = msg;
    }

    public void init() {
        ArrayList<String> soundList = ((ExWorld) msg.level).getSoundList();

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
        String sound = this.msg.sound;
        if (button.id == 0 && this.page == 0) {
            sound = "";
        } else if (button.id < 100) {
            int soundId = 3 * ((this.height - 60) / 20);
            sound = ((ExWorld) msg.level).getSoundList().get(button.id - 1 + soundId * this.page);
        } else {
            this.page = button.id - 100;
            this.buttons.clear();
            this.init();
        }

        if (!this.msg.sound.equals(sound)) {
            this.msg.sound = sound;
            this.msg.setChanged();
        }
    }

    @Override
    protected void keyPressed(char ch, int key) {
        super.keyPressed(ch, key);

        String message = this.msg.message;
        if (key == 14 && !message.isEmpty()) {
            message = message.substring(0, message.length() - 1);
        }

        if (SharedConstants.acceptableLetters.indexOf(ch) >= 0) {
            message = message + ch;
        }

        if (!this.msg.message.equals(message)) {
            this.msg.message = message;
            this.msg.setChanged();
        }
    }

    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        int color = 14737632;
        this.drawString(this.font, String.format("Message: '%s'", this.msg.message), 4, 4, color);

        if (!this.msg.sound.equals("")) {
            this.drawString(this.font, String.format("Sound: %s", this.msg.sound), 4, 24, color);
        } else {
            this.drawString(this.font, "Sound: None", 4, 24, color);
        }

        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_TileEntityMessage entity) {
        Minecraft.instance.setScreen(new AC_GuiMessage(entity));
    }
}
