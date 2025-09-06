package dev.adventurecraft.awakening.extension.client.gamemode;

import net.minecraft.client.Minecraft;

public interface ExGameMode {

    Minecraft getMinecraft();

    int getDestroyExtraWidth();

    void setDestroyExtraWidth(int value);

    int getDestroyExtraDepth();

    void setDestroyExtraDepth(int value);
}
