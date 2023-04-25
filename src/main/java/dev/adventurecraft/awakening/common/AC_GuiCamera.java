package dev.adventurecraft.awakening.common;

import java.util.List;

import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextboxWidget;
import net.minecraft.entity.Entity;

public class AC_GuiCamera extends Screen {
    private AC_EntityCamera cam;
    private TextboxWidget timerText;

    public AC_GuiCamera(AC_EntityCamera var1) {
        this.cam = var1;
    }

    public void initVanillaScreen() {
        ButtonWidget var1 = new ButtonWidget(0, 4, 4, 160, 18, "Delete Camera Point");
        this.buttons.add(var1);
        var1 = new ButtonWidget(1, 4, 24, 160, 18, "No Interpolation");
        if (this.cam.type == 1) {
            var1.text = "Linear Interpolation";
        } else if (this.cam.type == 2) {
            var1.text = "Quadratic Interpolation";
        }

        this.buttons.add(var1);
        this.timerText = new TextboxWidget(this, this.textRenderer, 80, 46, 70, 16, String.format("%.2f", this.cam.time));
    }

    public void tick() {
        this.timerText.tick();
    }

    protected void keyPressed(char var1, int var2) {
        if (this.timerText.selected && (var2 == 14 || var1 >= 48 && var1 <= 57 || var1 == 46 || var1 == 9)) {
            this.timerText.keyPressed(var1, var2);
        }

        super.keyPressed(var1, var2);
    }

    protected void mouseClicked(int var1, int var2, int var3) {
        this.timerText.mouseClicked(var1, var2, var3);
        super.mouseClicked(var1, var2, var3);
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            this.cam.deleteCameraPoint();
            Minecraft.instance.openScreen(null);
        } else if (var1.id == 1) {
            this.cam.type = (this.cam.type + 1) % 3;
            ((ExMinecraft) this.client).getActiveCutsceneCamera().setPointType(this.cam.cameraID, this.cam.type);

            for (Entity var3 : (List<Entity>) this.client.world.entities) {
                if (var3 instanceof AC_EntityCamera var4) {
                    if (var4.isAlive() && var4.cameraID == this.cam.cameraID) {
                        this.cam = var4;
                        break;
                    }
                }
            }

            if (this.cam.type == 1) {
                var1.text = "Linear Interpolation";
            } else if (this.cam.type == 2) {
                var1.text = "Quadratic Interpolation";
            } else {
                var1.text = "No Interpolation";
            }
        }
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();

        try {
            float var4 = Float.parseFloat(this.timerText.getText());
            this.cam.time = var4;
            ((ExMinecraft) this.client).getActiveCutsceneCamera().setTime(this.cam.cameraID, (float) var4);
        } catch (NumberFormatException var5) {
        }

        this.drawTextWithShadow(this.textRenderer, "Active At:", 4, 49, 14737632);
        this.timerText.draw();
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_EntityCamera var0) {
        Minecraft.instance.openScreen(new AC_GuiCamera(var0));
    }
}
