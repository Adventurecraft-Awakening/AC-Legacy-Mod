package dev.adventurecraft.awakening.tile;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.StairsTile;
import net.minecraft.world.level.tile.Tile;

public class AC_BlockStairMulti extends StairsTile implements AC_IBlockColor {

    protected AC_BlockStairMulti(int id, Tile template, int texture) {
        super(id, template);
        this.tex = texture;
    }

    public @Override int getTexture(int side, int meta) {
        return this.tex + (meta >> 2);
    }

    public @Override int getTexture(int i) {
        return this.tex;
    }

    public @Override int getTexture(LevelSource level, int x, int y, int z, int side) {
        return this.getTexture(side, level.getData(x, y, z));
    }

    public @Override void setPlacedBy(Level world, int x, int y, int z, Mob placer) {
        int meta = world.getData(x, y, z);
        int direction = Mth.floor((double) (placer.yRot * 4.0F / 360.0F) + 0.5D) & 3;
        int offset = switch (direction) {
            case 0 -> 2;
            case 1 -> 1;
            case 2 -> 3;
            default -> 0;
        };
        world.setData(x, y, z, meta + offset);
    }

    public @Override int getMaxColorMeta() {
        return 16;
    }

    public @Override int getFoliageColor(LevelSource view, int x, int y, int z) {
        return 0xFFFFFF;
    }

    public @Override int getColorMeta(LevelSource view, int x, int y, int z) {
        return view.getData(x, y, z) >> 2;
    }

    public @Override void setColorMeta(Level world, int x, int y, int z, int meta) {
        world.setData(x, y, z, world.getData(x, y, z) & 3 | meta << 2);
    }
}
