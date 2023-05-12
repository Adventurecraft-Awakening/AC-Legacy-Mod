package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemTriggerStick extends Item implements AC_ILeftClickItem {

    protected AC_ItemTriggerStick(int id) {
        super(id);
        this.setTexturePosition(5, 3);
    }

    @Override
    public boolean onItemUseLeftClick(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        Minecraft.instance.overlay.addChatMessage(String.format("Triggering (%d, %d, %d)", x, y, z));
        ((ExWorld) world).getTriggerManager().addArea(0, -1, 0, new AC_TriggerArea(x, y, z, x, y, z));
        ((ExWorld) world).getTriggerManager().removeArea(0, -1, 0);
        return false;
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        Minecraft.instance.overlay.addChatMessage(String.format("Checking (%d, %d, %d)", x, y, z));
        ((ExWorld) world).getTriggerManager().outputTriggerSources(x, y, z);
        return false;
    }
}
