package dev.adventurecraft.awakening.client.gamemode;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiplayerGameMode;

public class MultiplayerAdventureGameMode extends MultiplayerGameMode {

    public MultiplayerAdventureGameMode(Minecraft minecraft, ClientPacketListener connection) {
        super(minecraft, connection);
    }

    public @Override boolean destroyBlock(int x, int y, int z, int face) {
        return GameModeUtil.destroyBlocks(this, x, y, z, face, super::destroyBlock);
    }

    public @Override void continueDestroyBlock(int x, int y, int z, int face) {
        if (AC_DebugMode.isActive()) {
            super.continueDestroyBlock(x, y, z, face);
        }
    }

    public @Override float getPickRange() {
        return GameModeUtil.getPickRange(this).orElseGet(super::getPickRange);
    }
}
