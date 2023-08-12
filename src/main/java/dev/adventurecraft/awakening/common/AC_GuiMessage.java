package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.CharacterUtils;
import net.minecraft.world.World;

import java.util.ArrayList;

public class AC_GuiMessage extends Screen {

    private AC_TileEntityMessage msg;
    private World world;
    private int page;

    public AC_GuiMessage(World world, AC_TileEntityMessage msg) {
        this.world = world;
        this.msg = msg;
    }

    public void initVanillaScreen() {
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
            
            var button = new ButtonWidget(i, x, y, w, 18, name);
            this.buttons.add(button);
        }

        int pageCount = soundList.size() / soundsPerPage + 1;
        for (int i = 0; i < pageCount; ++i) {
            var button = new ButtonWidget(100 + i, 4 + i * 50, 40, 46, 18, String.format("Page %d", i + 1));
            this.buttons.add(button);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0 && this.page == 0) {
            this.msg.sound = "";
        } else if (button.id < 100) {
            int soundId = 3 * ((this.height - 60) / 20);
            this.msg.sound = ((ExWorld) this.world).getSoundList().get(button.id - 1 + soundId * this.page);
        } else {
            this.page = button.id - 100;
            this.buttons.clear();
            this.initVanillaScreen();
        }

        this.world.getChunk(this.msg.x, this.msg.z).method_885();
    }

    @Override
    protected void keyPressed(char var1, int var2) {
        super.keyPressed(var1, var2);
        if (var2 == 14 && this.msg.message.length() > 0) {
            this.msg.message = this.msg.message.substring(0, this.msg.message.length() - 1);
        }

        if (CharacterUtils.validCharacters.indexOf(var1) >= 0 && this.msg.message.length() < 30) {
            this.msg.message = this.msg.message + var1;
        }
    }

    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        this.drawTextWithShadow(this.textRenderer, String.format("Message: '%s'", this.msg.message), 4, 4, 14737632);
        if (!this.msg.sound.equals("")) {
            this.drawTextWithShadow(this.textRenderer, String.format("Sound: %s", this.msg.sound), 4, 24, 14737632);
        } else {
            this.drawTextWithShadow(this.textRenderer, "Sound: None", 4, 24, 14737632);
        }

        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(World var0, AC_TileEntityMessage var1) {
        Minecraft.instance.openScreen(new AC_GuiMessage(var0, var1));
    }
}
