package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockColor extends Tile implements AC_IBlockColor {

    public AC_BlockColor(int id, int texture, Material material) {
        super(id, texture, material);
    }

    @Override
    public int getFoliageColor(LevelSource view, int x, int y, int z) {
        int meta = this.getColorMeta(view, x, y, z);
        return switch (meta) {
            case 1 -> 16775065;
            case 2 -> 16767663;
            case 3 -> 10736540;
            case 4 -> 9755639;
            case 5 -> 8880573;
            case 6 -> 15539236;
            default -> defaultColor;
        };
    }
}
