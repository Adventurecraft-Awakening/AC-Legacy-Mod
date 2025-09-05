package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

public class AC_DebugMode {
    private static boolean active = false;
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

    @Environment(EnvType.CLIENT)
    public static boolean isActive() {
        return ((ExPlayerEntity) Minecraft.instance.player).isDebugMode();
    }
}

