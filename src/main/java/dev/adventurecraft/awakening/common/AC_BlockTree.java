package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockTree extends TileEntityTile implements AC_IBlockColor {

    protected AC_BlockTree(int var1, int var2) {
        super(var1, var2, Material.PLANT);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTree();
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2;
    }

    @Override
    public AABB getAABB(Level world, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean mayPick() {
        return AC_DebugMode.active;
    }

    @Override
    public boolean isSolidRender() {
        return false;
    }

    @Override
    public boolean isCubeShaped() {
        return false;
    }

    @Override
    public int getRenderShape() {
        return 36;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (AC_DebugMode.active) {
            var entity = (AC_TileEntityTree) world.getTileEntity(x, y, z);
            AC_GuiTree.showUI(world, x, y, z, entity);
        }
        return true;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
