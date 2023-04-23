package dev.adventurecraft.awakening.extension.block;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import net.minecraft.block.Block;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface ExBlock {

    int[] subTypes = new int[256];

    Block setSubTypes(int var1);

    int getBlockLightValue(BlockView var1, int var2, int var3, int var4);

    boolean shouldRender(BlockView var1, int var2, int var3, int var4);

    boolean canBeTriggered();

    void addTriggerActivation(World var1, int var2, int var3, int var4);

    void removeTriggerActivation(World var1, int var2, int var3, int var4);

    void onTriggerActivated(World var1, int var2, int var3, int var4);

    void onTriggerDeactivated(World var1, int var2, int var3, int var4);

    void reset(World var1, int var2, int var3, int var4, boolean var5);

    int alwaysUseClick(World var1, int var2, int var3, int var4);

    int getTextureNum();

    Block setTextureNum(int var1);

    static void resetArea(World var0, int var1, int var2, int var3, int var4, int var5, int var6) {
        boolean var7 = AC_DebugMode.triggerResetActive;
        AC_DebugMode.triggerResetActive = true;

        for (int var8 = var1; var8 <= var4; ++var8) {
            for (int var9 = var2; var9 <= var5; ++var9) {
                for (int var10 = var3; var10 <= var6; ++var10) {
                    int var11 = var0.getBlockId(var8, var9, var10);
                    if (var11 != 0) {
                        ((ExBlock) Block.BY_ID[var11]).reset(var0, var8, var9, var10, false);
                    }
                }
            }
        }

        AC_DebugMode.triggerResetActive = var7;
    }
}
