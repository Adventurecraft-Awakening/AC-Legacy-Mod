package dev.adventurecraft.awakening.common;

import java.util.List;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextboxWidget;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;

public class AC_GuiCamera extends Screen {

    private AC_EntityCamera cam;
    private TextboxWidget timerText;

    public AC_GuiCamera(AC_EntityCamera var1) {
        this.cam = var1;
    }

    @Override
    public void initVanillaScreen() {
        ButtonWidget button = new ButtonWidget(0, 4, 4, 160, 18, "Delete Camera Point");
        this.buttons.add(button);

        button = new ButtonWidget(1, 4, 24, 160, 18, "No Interpolation");
        if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
            button.text = "Linear Interpolation";
        } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
            button.text = "Quadratic Interpolation";
        }
        this.buttons.add(button);

        this.timerText = new TextboxWidget(this, this.textRenderer, 80, 46, 70, 16, String.format("%.2f", this.cam.time));
    }

    @Override
    public void tick() {
        this.timerText.tick();
    }

    @Override
    protected void keyPressed(char character, int key) {
        if (this.timerText.selected && (key == Keyboard.KEY_BACK || character >= 48 && character <= 57 || character == 46 || character == 9)) {
            this.timerText.keyPressed(character, key);
        }

        super.keyPressed(character, key);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.timerText.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            this.cam.deleteCameraPoint();
            Minecraft.instance.openScreen(null);
        } else if (button.id == 1) {
            this.cam.type = AC_CutsceneCameraBlendType.get((this.cam.type.value + 1) % AC_CutsceneCameraBlendType.MAX.value);
            ((ExMinecraft) this.client).getActiveCutsceneCamera().setPointType(this.cam.cameraID, this.cam.type);

            for (Entity entity : (List<Entity>) this.client.world.entities) {
                if (entity instanceof AC_EntityCamera camera) {
                    if (camera.isAlive() && camera.cameraID == this.cam.cameraID) {
                        this.cam = camera;
                        break;
                    }
                }
            }

            if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
                button.text = "Linear Interpolation";
            } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.text = "Quadratic Interpolation";
            } else {
                button.text = "No Interpolation";
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        try {
            float timerValue = Float.parseFloat(this.timerText.getText());
            this.cam.time = timerValue;
            ((ExMinecraft) this.client).getActiveCutsceneCamera().setPointTime(this.cam.cameraID, timerValue);
        } catch (NumberFormatException var5) {
        }

        this.drawTextWithShadow(this.textRenderer, "Active At:", 4, 49, 0xe0e0e0);
        this.timerText.draw();
        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_EntityCamera entity) {
        Minecraft.instance.openScreen(new AC_GuiCamera(entity));
    }
}
