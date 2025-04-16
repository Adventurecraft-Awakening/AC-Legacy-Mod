package dev.adventurecraft.awakening.item;

import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class AC_ItemTriggerStick extends Item implements AC_ILeftClickItem {

    protected AC_ItemTriggerStick(int id) {
        super(id);
        this.setIcon(5, 3);
    }

    @Override
    public boolean onItemUseLeftClick(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        Minecraft.instance.gui.addMessage(String.format("Triggering (%d, %d, %d)", x, y, z));
        ((ExWorld) world).getTriggerManager().addArea(0, -1, 0, new AC_TriggerArea(x, y, z, x, y, z));
        ((ExWorld) world).getTriggerManager().removeArea(0, -1, 0);
        return false;
    }

    @Override
    public boolean useOn(ItemInstance stack, Player player, Level world, int x, int y, int z, int side) {
        Minecraft.instance.gui.addMessage(String.format("Checking (%d, %d, %d)", x, y, z));
        ((ExWorld) world).getTriggerManager().outputTriggerSources(x, y, z);
        return false;
    }
}
