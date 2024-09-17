package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiCameraBlock extends Screen {

    private AC_TileEntityCamera cam;

    public AC_GuiCameraBlock(AC_TileEntityCamera var1) {
        this.cam = var1;
    }

    @Override
    public void init() {
        {
            var button = new Button(0, 4, 4, 160, 18, "Skip to first point");
            if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
                button.message = "Linear Interpolation";
            } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.message = "Quadratic Interpolation";
            }
            this.buttons.add(button);
        }
        {
            var button = new Button(1, 4, 24, 160, 18, "Pause Game");
            if (!this.cam.pauseGame) {
                button.message = "Game Runs";
            }
            this.buttons.add(button);
        }
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 0) {
            this.cam.type = AC_CutsceneCameraBlendType.get((this.cam.type.value + 1) % AC_CutsceneCameraBlendType.MAX.value);
            if (this.cam.type == AC_CutsceneCameraBlendType.LINEAR) {
                button.message = "Linear Interpolation";
            } else if (this.cam.type == AC_CutsceneCameraBlendType.QUADRATIC) {
                button.message = "Quadratic Interpolation";
            } else {
                button.message = "Skip to first point";
            }
        } else if (button.id == 1) {
            this.cam.pauseGame = !this.cam.pauseGame;
            button.message = "Pause Game";
            if (!this.cam.pauseGame) {
                button.message = "Game Runs";
            }
        }

        this.cam.level.getChunkAt(this.cam.x, this.cam.z).markUnsaved();
    }

    @Override
    public void render(int mouseX, int mouseY, float deltaTime) {
        this.renderBackground();
        super.render(mouseX, mouseY, deltaTime);
    }

    public static void showUI(AC_TileEntityCamera entity) {
        Minecraft.instance.setScreen(new AC_GuiCameraBlock(entity));
    }
}
