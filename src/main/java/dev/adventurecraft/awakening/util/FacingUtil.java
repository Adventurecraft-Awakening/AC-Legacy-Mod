package dev.adventurecraft.awakening.util;

import net.minecraft.util.Facing;

public final class FacingUtil {

    public static int getFacing(double yaw) {
        int raw = (int) Math.floor((yaw * 4.0F / 360.0F) + 0.5D) & 3;
        return switch (raw) {
            case 0 -> Facing.SOUTH;
            case 1 -> Facing.WEST;
            case 2 -> Facing.NORTH;
            case 3 -> Facing.EAST;
            default -> Facing.DOWN;
        };
    }

    public static String getFacingName(int facing) {
        // TODO: localize with I18n
        return switch (facing) {
            case Facing.DOWN -> "Down";
            case Facing.UP -> "Up";
            case Facing.NORTH -> "North";
            case Facing.SOUTH -> "South";
            case Facing.WEST -> "West";
            case Facing.EAST -> "East";
            default -> "";
        };
    }

    public static String getFacingDir(int facing) {
        // TODO: localize with I18n
        return switch (facing) {
            case Facing.DOWN -> "-Y";
            case Facing.UP -> "+Y";
            case Facing.NORTH -> "-Z";
            case Facing.SOUTH -> "+Z";
            case Facing.WEST -> "-X";
            case Facing.EAST -> "+X";
            default -> "";
        };
    }
}
