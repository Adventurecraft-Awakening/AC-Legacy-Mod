package dev.adventurecraft.awakening.common;

import net.minecraft.block.Block;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockHalfStep extends AC_BlockSolid {


    private final int[][] TEXTURE_SIDE_COORDINATION = {
        {0, 1, 0},
        {0, -1, 0},
        {0, 0, 1},
        {0, 0, -1},
        {1, 0, 0},
        {-1, 0, 0}
    };


    protected AC_BlockHalfStep(int var1, int var2) {
		super(var1, var2);
	}

    @Override
	public int getTextureForSide(int var1, int var2) {
        setBoundingBox(var2);
        var2 = 2 * (var2 / 2);
        int texture = this.texture + var2;
        if (var1 <= 1) {
            return texture + 1;
        }
        return texture;
	}

    @Override
    public boolean isSideRendered(BlockView blockView, int x, int y, int z, int textureSide) {
        this.updateBlockBounds(blockView, x, y, z);
        if (textureSide >= TEXTURE_SIDE_COORDINATION.length) {
            return super.isSideRendered(blockView, x, y, z, textureSide);
        }
        int[] coordination = TEXTURE_SIDE_COORDINATION[textureSide];
        int meta = blockView.getBlockMeta(x + coordination[0], y + coordination[1], z + coordination[2]);

        int idNeighbor = blockView.getBlockId(x, y, z);
        int metaNeighbor = blockView.getBlockMeta(x, y, z);

        if (idNeighbor >= 215 && idNeighbor <= 217) {
            boolean sameMeta = meta % 2 == metaNeighbor % 2;
            if (textureSide > 1 && sameMeta) {
                //East, West, South, North Sides
                return false;
            } else if (textureSide < 2 && !sameMeta) {
                //Top, Bottom Sides
                return false;
            }
        }
        if (meta % 2 == 0 && textureSide == 0) {
            Block block = Block.BY_ID[idNeighbor];
            if (block != null) {
                return !block.isFullOpaque();
            }
        } else if (meta % 2 == 1 && textureSide == 1) {
            Block block = Block.BY_ID[idNeighbor];
            if (block != null) {
                return !block.isFullOpaque();
            }
        }
        return true;
	}

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World var1, int x, int y, int z) {
        this.updateBlockBounds(var1, x, y, z);
        return super.getCollisionShape(var1, x, y, z);
    }

    private void updateBlockBounds(BlockView var1, int x, int y, int z) {
        int meta = var1.getBlockMeta(x, y, z);
        setBoundingBox(meta);
	}

    private void setBoundingBox(int meta) {
        if (meta % 2 == 0) {
            this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        } else {
            this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    @Override
	public boolean isFullOpaque() {
		return false;
	}
}
