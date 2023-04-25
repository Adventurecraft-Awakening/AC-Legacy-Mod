package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemHammer extends Item {
    protected AC_ItemHammer(int var1) {
        super(var1);
    }

    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        if (AC_ItemCursor.bothSet) {
            int var8 = var3.getBlockId(var4, var5, var6);
            int var9 = var3.getBlockMeta(var4, var5, var6);
            Minecraft.instance.overlay.addChatMessage(String.format("Swapping Area With BlockID %d", var8));
            int var10 = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int var11 = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int var12 = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int var13 = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int var14 = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
            int var15 = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);

            for (int var16 = var10; var16 <= var11; ++var16) {
                for (int var17 = var12; var17 <= var13; ++var17) {
                    for (int var18 = var14; var18 <= var15; ++var18) {
                        var3.placeBlockWithMetaData(var16, var17, var18, var8, var9);
                    }
                }
            }
        }

        return false;
    }

    public float getStrengthOnBlock(ItemStack var1, Block var2) {
        return 32.0F;
    }

    public boolean isEffectiveOn(Block var1) {
        return true;
    }

    public boolean shouldSpinWhenRendering() {
        return true;
    }
}
