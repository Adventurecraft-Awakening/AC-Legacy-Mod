package dev.adventurecraft.awakening.common.gui;

import java.text.NumberFormat;
import java.util.List;
import java.util.function.Consumer;

import dev.adventurecraft.awakening.client.gui.components.AC_ValueBox;
import dev.adventurecraft.awakening.common.AC_CutsceneCameraBlendType;
import dev.adventurecraft.awakening.entity.AC_EntityCamera;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.primitives.Property;
import dev.adventurecraft.awakening.primitives.TickTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;

public class AC_GuiCamera extends Screen {

    private AC_EntityCamera camera;
    private AC_ValueBox<TickTime> timerText;

    public AC_GuiCamera(AC_EntityCamera entity) {
        this.camera = entity;
    }

    private static String getBlendMsg(AC_CutsceneCameraBlendType type) {
        return switch (type) {
            case LINEAR -> "Linear Interpolation";
            case QUADRATIC -> "Quadratic Interpolation";
            default -> "No Interpolation";
        };
    }

    @Override
    public void init() {
        this.buttons.add(new Button(0, 4, 4, 160, 18, "Delete Camera Point"));
        this.buttons.add(new Button(1, 4, 24, 160, 18, getBlendMsg(this.camera.getBlendType())));

        Consumer<Float> timeSetter = value -> {
            this.camera.setTime(value);
            ((ExMinecraft) this.minecraft).getActiveCutsceneCamera().setPointTime(this.camera.getCameraId(), value);
        };
        this.timerText = new AC_ValueBox<>(
            new IntRect(80, 46, 70, 16),
            Property.of(this.camera::getTime, timeSetter).map(TickTime::fromSeconds, TickTime::seconds),
            new TickTime.TimeFormat(NumberFormat.getInstance())
        );
    }

    @Override
    public void tick() {
        this.timerText.tick();
    }

    @Override
    protected void keyPressed(char character, int key) {
        this.timerText.charTyped(character, key);

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
            this.camera.deleteCameraPoint();
            Minecraft.instance.setScreen(null);
        }
        else if (button.id == 1) {
            this.camera.setBlendType(AC_CutsceneCameraBlendType.cycle(this.camera.getBlendType(), 1));
            button.message = getBlendMsg(this.camera.getBlendType());

            ((ExMinecraft) this.minecraft)
                .getActiveCutsceneCamera()
                .setPointType(this.camera.getCameraId(), this.camera.getBlendType());

            for (Entity entity : (List<Entity>) this.minecraft.level.entities) {
                if (entity instanceof AC_EntityCamera cam) {
                    if (cam.isAlive() && cam.getCameraId() == this.camera.getCameraId()) {
                        this.camera = cam;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();

        this.drawString(this.font, "Active at:", 4, 49, 0xe0e0e0);
        this.timerText.render(this.font);

        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_EntityCamera entity) {
        Minecraft.instance.setScreen(new AC_GuiCamera(entity));
    }
}
