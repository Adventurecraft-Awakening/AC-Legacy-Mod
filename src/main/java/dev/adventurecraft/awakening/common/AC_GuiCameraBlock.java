package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

public class AC_GuiCameraBlock extends Screen {

    private AC_TileEntityCamera cam;

    public AC_GuiCameraBlock(AC_TileEntityCamera var1) {
        this.cam = var1;
    }

    @Override
    public void initVanillaScreen() {
        {
            var button = new ButtonWidget(0, 4, 4, 160, 18, "Skip to first point");
            if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
                button.text = "Linear Interpolation";
            } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.text = "Quadratic Interpolation";
            }
            this.buttons.add(button);
        }
        {
            var button = new ButtonWidget(1, 4, 24, 160, 18, "Pause Game");
            if (!this.cam.pauseGame) {
                button.text = "Game Runs";
            }
            this.buttons.add(button);
        }
    }

    @Override
    protected void buttonClicked(ButtonWidget button) {
        if (button.id == 0) {
            this.cam.type = AC_CutsceneCameraBlendType.get((this.cam.type.value + 1) % AC_CutsceneCameraBlendType.MAX.value);
            if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
                button.text = "Linear Interpolation";
            } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.text = "Quadratic Interpolation";
            } else {
                button.text = "Skip to first point";
            }
        } else if (button.id == 1) {
            this.cam.pauseGame = !this.cam.pauseGame;
            button.text = "Pause Game";
            if (!this.cam.pauseGame) {
                button.text = "Game Runs";
            }
        }

        this.cam.world.getChunk(this.cam.x, this.cam.z).method_885();
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_TileEntityCamera entity) {
        Minecraft.instance.openScreen(new AC_GuiCameraBlock(entity));
    }
}
