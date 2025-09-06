package dev.adventurecraft.awakening.client.gamemode;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.SurvivalGameMode;

public class AdventureGameMode extends SurvivalGameMode {

    public AdventureGameMode(Minecraft minecraft) {
        super(minecraft);
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
