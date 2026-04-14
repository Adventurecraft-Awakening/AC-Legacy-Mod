package dev.adventurecraft.awakening.client.gui;

import dev.adventurecraft.awakening.client.gui.components.AC_EditBox;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.layout.IntBorder;
import dev.adventurecraft.awakening.layout.IntRect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.input.Keyboard;

@Environment(EnvType.CLIENT)
public class AC_ChatScreen extends Screen {

    protected final LocalPlayer player;

    protected AC_EditBox messageBox;

    public AC_ChatScreen(LocalPlayer player) {
        this.player = player;
    }

    public @Override void init() {
        Keyboard.enableRepeatEvents(true);

        int chatH = 16;
        int barOffset = 32; // TODO: drive value from InGameHud
        var border = new IntBorder(2);

        var rect = new IntRect(0, this.height - chatH - barOffset, this.width, chatH).shrink(border);
        if (this.messageBox == null) {
            this.messageBox = new AC_EditBox(rect, "");
            this.messageBox.setActive(true);

            this.messageBox.setBoxBackColor(Rgba.withAlpha(this.messageBox.getBoxBackColor(), 100));
            this.messageBox.setBoxBorderColor(Rgba.withAlpha(this.messageBox.getBoxBorderColor(), 100));
        }
        else {
            this.messageBox.setRect(rect);
        }
    }

    public @Override void removed() {
        Keyboard.enableRepeatEvents(false);
    }

    public @Override void tick() {
        this.messageBox.setActive(true);
        this.messageBox.tick();
    }

    protected void submitMessage() {
        String text = this.messageBox.getValue().trim();
        if (text.isEmpty()) {
            return;
        }

        if (!this.minecraft.isCommand(text)) {
            this.minecraft.player.chat(text);
        }
    }

    protected @Override void keyPressed(char eventCharacter, int eventKey) {
        if (this.messageBox.getTickCount() < 1) {
            // Skip first frame since it includes the key that opened chat.
            return;
        }

        if (eventKey == Keyboard.KEY_ESCAPE) {
            this.minecraft.setScreen(null);
        }
        else if (eventKey == Keyboard.KEY_RETURN) {
            this.minecraft.setScreen(null);
            this.submitMessage();
        }
        else if (eventKey == Keyboard.KEY_TAB) {

        }
        else {
            this.messageBox.charTyped(eventCharacter, eventKey);
        }
    }

    public void render(int mouseX, int mouseY, float a) {
        /*
        this.fill(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);
        this.drawString(
            this.font,
            "> " + this.message + (this.frame / 6 % 2 == 0 ? "_" : ""),
            4,
            this.height - 12,
            14737632
        );
        */
        this.messageBox.render(this.font);

        super.render(mouseX, mouseY, a);
    }

    protected @Override void mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0) {
            String name = this.minecraft.gui.selectedName;
            if (name != null) {
                this.messageBox.append(name);
                return;
            }
        }

        this.messageBox.clicked(mouseX, mouseY, button);

        super.mouseClicked(mouseX, mouseY, button);
    }
}
