package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class AC_GuiCameraBlock extends Screen {
    private AC_TileEntityCamera cam;

    public AC_GuiCameraBlock(AC_TileEntityCamera var1) {
        this.cam = var1;
    }

    public void initVanillaScreen() {
        ButtonWidget var1 = new ButtonWidget(0, 4, 4, 160, 18, "Skip to first point");
        if (this.cam.type == 1) {
            var1.text = "Linear Interpolation";
        } else if (this.cam.type == 2) {
            var1.text = "Quadratic Interpolation";
        }

        this.buttons.add(var1);
        var1 = new ButtonWidget(1, 4, 24, 160, 18, "Pause Game");
        if (!this.cam.pauseGame) {
            var1.text = "Game Runs";
        }

        this.buttons.add(var1);
    }

    protected void buttonClicked(ButtonWidget var1) {
        if (var1.id == 0) {
            this.cam.type = (this.cam.type + 1) % 3;
            if (this.cam.type == 1) {
                var1.text = "Linear Interpolation";
            } else if (this.cam.type == 2) {
                var1.text = "Quadratic Interpolation";
            } else {
                var1.text = "Skip to first point";
            }
        } else if (var1.id == 1) {
            this.cam.pauseGame = !this.cam.pauseGame;
            var1.text = "Pause Game";
            if (!this.cam.pauseGame) {
                var1.text = "Game Runs";
            }
        }

        this.cam.world.getChunk(this.cam.x, this.cam.z).method_885();
    }

    public void render(int var1, int var2, float var3) {
        this.renderBackground();
        super.render(var1, var2, var3);
    }

    public static void showUI(AC_TileEntityCamera var0) {
        Minecraft.instance.openScreen(new AC_GuiCameraBlock(var0));
    }
}
