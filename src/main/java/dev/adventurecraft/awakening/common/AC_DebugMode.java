package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.item.AC_Items;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.player.Player;

public class AC_DebugMode {
    public static boolean active = false;
    public static boolean levelEditing = false;
    public static boolean editMode = false;
    public static AC_MapEditing mapEditing = null;
    public static boolean renderPaths = false;
    public static int reachDistance = 4;
    public static boolean renderFov = false;
    public static boolean renderCollisions = false;
    public static boolean renderRays = false;

    public static boolean triggerResetActive = false;
    public static boolean isFluidHittable = true;

    public static boolean isHandEmptyOrCursor(Player player) {
        ItemInstance heldItem = player.getSelectedItem();
        return heldItem == null || heldItem.id == AC_Items.cursor.id;
    }

    public static boolean showDebugGuiOnUse(Player player) {
        return active && isHandEmptyOrCursor(player);
    }
}

