package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemTriggerStick extends Item {
    protected AC_ItemTriggerStick(int var1) {
        super(var1);
        this.setTexturePosition(5, 3);
    }

    public boolean onItemUseLeftClick(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        Minecraft.instance.overlay.addChatMessage(String.format("Triggering (%d, %d, %d)", var4, var5, var6));
        ((ExWorld) var3).getTriggerManager().addArea(0, -1, 0, new AC_TriggerArea(var4, var5, var6, var4, var5, var6));
        ((ExWorld) var3).getTriggerManager().removeArea(0, -1, 0);
        return false;
    }

    public boolean useOnBlock(ItemStack var1, PlayerEntity var2, World var3, int var4, int var5, int var6, int var7) {
        Minecraft.instance.overlay.addChatMessage(String.format("Checking (%d, %d, %d)", var4, var5, var6));
        ((ExWorld) var3).getTriggerManager().outputTriggerSources(var4, var5, var6);
        return false;
    }
}
