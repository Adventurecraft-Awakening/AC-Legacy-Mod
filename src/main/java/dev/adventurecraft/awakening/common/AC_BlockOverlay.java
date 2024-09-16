package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockOverlay extends Tile implements AC_IBlockColor, AC_ITriggerBlock {

    protected AC_BlockOverlay(int id, int texture) {
        super(id, texture, Material.PLANT);
        this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return null;
    }

    @Override
    public AABB getTileAABB(Level world, int x, int y, int z) {
        this.updateBounds(world, x, y, z);
        return super.getTileAABB(world, x, y, z);
    }

    public void updateBounds(LevelSource view, int x, int y, int z) {
        double offset = 1.0 / 64.0;
        double minX = 0.0;
        double minY = 0.0;
        double minZ = 0.0;
        double maxX = 1.0;
        double maxY = 1.0;
        double maxZ = 1.0;
        if (view.isSolidTile(x, y - 1, z)) {
            maxY = offset;
        } else if (view.isSolidTile(x, y + 1, z)) {
            minY = 1.0 - offset;
        } else if (view.isSolidTile(x - 1, y, z)) {
            maxX = offset;
        } else if (view.isSolidTile(x + 1, y, z)) {
            minX = 1.0 - offset;
        } else if (view.isSolidTile(x, y, z - 1)) {
            maxZ = offset;
        } else if (view.isSolidTile(x, y, z + 1)) {
            minZ = 1.0 - offset;
        } else {
            maxY = offset;
        }
        ((ExBlock) this).setBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isCubeShaped() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return 37;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
