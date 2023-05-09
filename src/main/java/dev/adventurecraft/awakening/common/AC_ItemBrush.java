package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AC_ItemBrush extends Item {

    protected AC_ItemBrush(int var1) {
        super(var1);
    }

    @Override
    public boolean useOnBlock(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, int side) {
        Block block = Block.BY_ID[world.getBlockId(x, y, z)];
        if (block instanceof AC_IBlockColor) {
            ((AC_IBlockColor) block).incrementColor(world, x, y, z);
            world.notifyListeners(x, y, z);
        } else {
            Minecraft.instance.overlay.addChatMessage("Doesn't implement Color :(");
        }
        return false;
    }
}
