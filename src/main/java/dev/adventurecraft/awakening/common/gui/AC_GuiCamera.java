package dev.adventurecraft.awakening.common.gui;

import java.util.List;

import dev.adventurecraft.awakening.common.AC_CutsceneCameraBlendType;
import dev.adventurecraft.awakening.common.AC_EntityCamera;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.lwjgl.input.Keyboard;

public class AC_GuiCamera extends Screen {

    private AC_EntityCamera cam;
    private EditBox timerText;

    public AC_GuiCamera(AC_EntityCamera var1) {
        this.cam = var1;
    }

    @Override
    public void init() {
        Button button = new Button(0, 4, 4, 160, 18, "Delete Camera Point");
        this.buttons.add(button);

        button = new Button(1, 4, 24, 160, 18, "No Interpolation");
        if (this.cam.getBlendType() == AC_CutsceneCameraBlendType.LINEAR) {
            button.message = "Linear Interpolation";
        } else if (this.cam.getBlendType() == AC_CutsceneCameraBlendType.QUADRATIC) {
            button.message = "Quadratic Interpolation";
        }
        this.buttons.add(button);

        this.timerText = new EditBox(this, this.font, 80, 46, 70, 16, String.format("%.2f", this.cam.getTime()));
    }

    @Override
    public void tick() {
        this.timerText.tick();
    }

    @Override
    protected void keyPressed(char character, int key) {
        if (this.timerText.active && (key == Keyboard.KEY_BACK || character >= 48 && character <= 57 || character == 46 || character == 9)) {
            this.timerText.charTyped(character, key);
        }

        super.keyPressed(character, key);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.timerText.clicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 0) {
            this.cam.deleteCameraPoint();
            Minecraft.instance.setScreen(null);
        } else if (button.id == 1) {
            int nextBlendType = (this.cam.getBlendType().value + 1) % AC_CutsceneCameraBlendType.MAX.value;
            this.cam.setBlendType(AC_CutsceneCameraBlendType.get(nextBlendType));
            ((ExMinecraft) this.minecraft).getActiveCutsceneCamera().setPointType(this.cam.getCameraId(), this.cam.getBlendType());

            for (Entity entity : (List<Entity>) this.minecraft.level.entities) {
                if (entity instanceof AC_EntityCamera camera) {
                    if (camera.isAlive() && camera.getCameraId() == this.cam.getCameraId()) {
                        this.cam = camera;
                        break;
                    }
                }
            }

            if (this.cam.getBlendType() == AC_CutsceneCameraBlendType.LINEAR) {
                button.message = "Linear Interpolation";
            } else if (this.cam.getBlendType() == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.message = "Quadratic Interpolation";
            } else {
                button.message = "No Interpolation";
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        try {
            float timerValue = Float.parseFloat(this.timerText.getValue());
            this.cam.setTime(timerValue);
            ((ExMinecraft) this.minecraft).getActiveCutsceneCamera().setPointTime(this.cam.getCameraId(), timerValue);
        } catch (NumberFormatException var5) {
        }

        this.drawString(this.font, "Active At:", 4, 49, 0xe0e0e0);
        this.timerText.render();
        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_EntityCamera entity) {
        Minecraft.instance.setScreen(new AC_GuiCamera(entity));
    }
}
