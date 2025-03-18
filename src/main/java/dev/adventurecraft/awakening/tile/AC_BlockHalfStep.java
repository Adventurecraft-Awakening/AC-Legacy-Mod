package dev.adventurecraft.awakening.tile;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

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
	public int getTexture(int var1, int var2) {
        setBoundingBox(var2);
        var2 = 2 * (var2 / 2);
        int texture = this.tex + var2;
        if (var1 <= 1) {
            return texture + 1;
        }
        return texture;
	}

    @Override
    public boolean shouldRenderFace(LevelSource blockView, int x, int y, int z, int textureSide) {
        updateBlockBounds(blockView,x,y,z);
        if (textureSide >= TEXTURE_SIDE_COORDINATION.length) {
            return super.shouldRenderFace(blockView, x, y, z, textureSide);
        }
        int[] coordination = TEXTURE_SIDE_COORDINATION[textureSide];
        int meta = blockView.getData(x + coordination[0], y + coordination[1], z + coordination[2]);

        int idNeighbor = blockView.getTile(x, y, z);
        int metaNeighbor = blockView.getData(x, y, z);

        if (idNeighbor >= 215 && idNeighbor <= 217) {
            boolean sameMeta = meta % 2 == metaNeighbor % 2;
            if (textureSide > 1 && sameMeta) {
                //East, West, South, North Sides
                return false;
            } else if (textureSide < 2 && !sameMeta) {
                //Top, Bottom Sides
                return true;
            }
        }
        if (meta % 2 == 0 && textureSide == 0) {
            Tile block = Tile.tiles[idNeighbor];
            if (block != null) {
                return !block.isSolidRender();
            }
        } else if (meta % 2 == 1 && textureSide == 1) {
            Tile block = Tile.tiles[idNeighbor];
            if (block != null) {
                return !block.isSolidRender();
            }
        }
        return true;
	}

    @Override
    public AABB getAABB(Level var1, int x, int y, int z) {
        updateBlockBounds(var1,x,y,z);
        return super.getAABB(var1, x, y, z);
    }

    @Override
    public void updateShape(LevelSource arg, int i, int j, int k) {
        updateBlockBounds(arg,i,j,k);
    }

    private void updateBlockBounds(LevelSource var1, int x, int y, int z) {
        int meta = var1.getData(x, y, z);
        setBoundingBox(meta);
	}

    private void setBoundingBox(int meta) {
        if (meta % 2 == 0) {
            this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        } else {
            this.setShape(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    @Override
	public boolean isSolidRender() {
		return false;
	}
}
