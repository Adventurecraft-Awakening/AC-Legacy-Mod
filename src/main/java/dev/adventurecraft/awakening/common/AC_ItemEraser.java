package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemEraser extends Item {
    protected AC_ItemEraser(int var1) {
        super(var1);
    }

    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        if (AC_ItemCursor.bothSet) {
            Minecraft.instance.overlay.addChatMessage("Erasing Area");
            int var8 = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int var9 = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int var10 = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int var11 = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int var12 = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
            int var13 = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

            for (int var14 = var8; var14 <= var9; ++var14) {
                for (int var15 = var10; var15 <= var11; ++var15) {
                    for (int var16 = var12; var16 <= var13; ++var16) {
                        var3.setBlock(var14, var15, var16, 0);
                    }
                }
            }
        }

        return false;
    }
}
