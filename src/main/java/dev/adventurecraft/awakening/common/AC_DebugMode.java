package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;

public class AC_DebugMode {
    public static boolean isHandEmptyOrCursor(Player player) {
        ItemInstance heldItem = player.getSelectedItem();
        return heldItem == null || heldItem.id == AC_Items.cursor.id;
    }

    public static boolean showDebugGuiOnUse(Player player) {
        return dev.adventurecraft.awakening.editor.EditorSession.forLevel(player.level).active && isHandEmptyOrCursor(player);
    }
}

