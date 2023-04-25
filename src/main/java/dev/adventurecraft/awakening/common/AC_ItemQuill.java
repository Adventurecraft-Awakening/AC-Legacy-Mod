package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemQuill extends Item {
    protected AC_ItemQuill(int var1) {
        super(var1);
    }

    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        double var8 = 128.0D;

        for (int var10 = var5; var10 <= 128; ++var10) {
            if (var3.getBlockId(var4, var10, var6) == 0) {
                var8 = (float) var10 + var2.standingEyeHeight;
                break;
            }
        }

        Minecraft.instance.overlay.addChatMessage(String.format("Teleporting to (%.1f, %.1f %.1f)", (double) var4 + 0.5D, var8, (double) var6 + 0.5D));
        var2.setPosition((double) var4 + 0.5D, var8, (double) var6 + 0.5D);
        return false;
    }
}
