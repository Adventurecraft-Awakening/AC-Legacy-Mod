package dev.adventurecraft.awakening.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameStartupError;
import net.minecraft.client.Minecraft;

@Environment(value= EnvType.CLIENT)
public final class ACMainThread extends Minecraft {

    public ACMainThread(int i, int j, boolean bl) {
        super(null, null, null, i, j, bl);
    }

    @Override
    public void showGameStartupError(GameStartupError arg) {
    }
}