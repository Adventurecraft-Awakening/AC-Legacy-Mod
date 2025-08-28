package dev.adventurecraft.awakening.common.gui;

import dev.adventurecraft.awakening.common.AC_CutsceneCameraBlendType;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;

public class AC_GuiCameraBlock extends Screen {

    private AC_TileEntityCamera camera;

    public AC_GuiCameraBlock(AC_TileEntityCamera entity) {
        this.camera = entity;
    }

    private static String getBlendMsg(AC_CutsceneCameraBlendType type) {
        return switch (type) {
            case LINEAR -> "Linear Interpolation";
            case QUADRATIC -> "Quadratic Interpolation";
            default -> "Skip to first point";
        };
    }

    private static String getPauseMsg(boolean pauseGame) {
        return pauseGame ? "Pause Game" : "Game Runs";
    }

    @Override
    public void init() {
        this.buttons.add(new Button(0, 4, 4, 160, 18, getBlendMsg(this.camera.getBlendType())));
        this.buttons.add(new Button(1, 4, 24, 160, 18, getPauseMsg(this.camera.pauseGame)));
    }

    @Override
    protected void buttonClicked(Button button) {
        if (button.id == 0) {
            this.camera.setBlendType(AC_CutsceneCameraBlendType.cycle(this.camera.getBlendType(), 1));
            button.message = getBlendMsg(this.camera.getBlendType());
        }
        else if (button.id == 1) {
            this.camera.pauseGame = !this.camera.pauseGame;
            button.message = getPauseMsg(this.camera.pauseGame);
        }

        this.camera.setChanged();
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
