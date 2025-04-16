package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.tile.AC_IBlockColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

public class AC_ItemBrush extends Item {

    protected AC_ItemBrush(int var1) {
        super(var1);
    }

    @Override
    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        Tile block = Tile.tiles[world.getTile(x, y, z)];
        if (block instanceof AC_IBlockColor) {
            int amount = player.isSneaking() ? -1 : 1;
            ((AC_IBlockColor) block).incrementColor(world, x, y, z, amount);
            world.sendTileUpdated(x, y, z);
        } else {
            Minecraft.instance.gui.addMessage("Doesn't implement Color :(");
        }
        return false;
    }
}
