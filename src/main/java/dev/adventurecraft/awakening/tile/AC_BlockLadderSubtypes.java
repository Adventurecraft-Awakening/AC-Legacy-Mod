package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.LadderTile;

public class AC_BlockLadderSubtypes extends LadderTile implements AC_IBlockColor {

    protected AC_BlockLadderSubtypes(int var1, int var2) {
        super(var1, var2);
    }

    @Override
    public int getTexture(int var1, int meta) {
        meta /= 4;
        return this.tex + meta;
    }

    @Override
    public void setPlacedOnFace(Level world, int x, int y, int z, int side) {
        int coreMeta = world.getData(x, y, z);
        int meta = 0;
        if (meta == 0 && ExLadderBlock.isLadderID(world.getTile(x, y - 1, z))) {
            meta = world.getData(x, y - 1, z) % 4 + 2;
        }

        if (meta == 0 && ExLadderBlock.isLadderID(world.getTile(x, y + 1, z))) {
            meta = world.getData(x, y + 1, z) % 4 + 2;
        }

        if ((meta == 0 || side == 2) && world.isSolidTile(x, y, z + 1)) {
            meta = 2;
        }

        if ((meta == 0 || side == 3) && world.isSolidTile(x, y, z - 1)) {
            meta = 3;
        }

        if ((meta == 0 || side == 4) && world.isSolidTile(x + 1, y, z)) {
            meta = 4;
        }

        if ((meta == 0 || side == 5) && world.isSolidTile(x - 1, y, z)) {
            meta = 5;
        }

        coreMeta += Math.max(meta - 2, 0) % 4;
        world.setData(x, y, z, coreMeta);
    }

    @Override
    public void neighborChanged(Level world, int x, int y, int z, int id) {
    }

    @Override
    public int getMaxColorMeta() {
        return 4;
    }

    @Override
    public void incrementColor(Level world, int x, int y, int z, int amount) {
        int meta = this.getColorMeta(world, x, y, z);
        int maxMeta = this.getMaxColorMeta();
        int clampedMeta = Integer.remainderUnsigned(meta + amount * 4, maxMeta * 4);
        world.setData(x, y, z, clampedMeta);
    }
}
