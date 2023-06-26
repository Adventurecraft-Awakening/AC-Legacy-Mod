package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.CharacterUtils;
import net.minecraft.world.World;

public class AC_GuiMessage extends Screen {

    private AC_TileEntityMessage msg;
    private World world;
    private int page;

    public AC_GuiMessage(World world, AC_TileEntityMessage msg) {
        this.world = world;
        this.msg = msg;
    }

    public void initVanillaScreen() {
        String[] soundList = ((ExWorld) this.world).getSoundList();

        int var1 = 3 * ((this.height - 60) / 20);

        int var2;
        ButtonWidget var4;
        for (var2 = 0; var2 + var1 * this.page - 1 < soundList.length && var2 < var1; ++var2) {
            String var3 = "None";
            if (var2 != 0 || this.page != 0) {
                var3 = soundList[var2 + var1 * this.page - 1];
            }

            var4 = new ButtonWidget(var2, 4 + var2 % 3 * this.width / 3, 60 + var2 / 3 * 20, (this.width - 16) / 3, 18, var3);
            this.buttons.add(var4);
        }

        var2 = soundList.length / var1 + 1;

        for (int var5 = 0; var5 < var2; ++var5) {
            var4 = new ButtonWidget(100 + var5, 4 + var5 * 50, 40, 46, 18, String.format("Page %d", var5 + 1));
            this.buttons.add(var4);
        }

    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0 && this.page == 0) {
            this.msg.sound = "";
        } else if (button.id < 100) {
            int var2 = 3 * ((this.height - 60) / 20);
            this.msg.sound = ((ExWorld) this.world).getSoundList()[button.id - 1 + var2 * this.page];
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
            this.drawTextWithShadow(this.textRenderer, String.format("Sound: None"), 4, 24, 14737632);
        }

        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(World var0, AC_TileEntityMessage var1) {
        Minecraft.instance.openScreen(new AC_GuiMessage(var0, var1));
    }
}
