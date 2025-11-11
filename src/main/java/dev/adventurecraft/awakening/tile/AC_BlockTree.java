package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.gui.AC_GuiTree;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTree;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.TileEntityTile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;

public class AC_BlockTree extends TileEntityTile implements AC_IBlockColor {

    protected AC_BlockTree(int id, int tex) {
        super(id, tex, Material.PLANT);
    }

    @Override
    protected TileEntity newTileEntity() {
        return new AC_TileEntityTree();
    }

    @Override
    public int getTexture(int side, int meta) {
        return this.tex + meta;
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
        return AC_BlockShapes.BLOCK_TREE;
    }

    @Override
    public boolean use(Level world, int x, int y, int z, Player player) {
        if (!AC_DebugMode.showDebugGuiOnUse(player)) {
            return false;
        }
        var entity = ((ExWorld) world).ac$getTileEntity(x, y, z, AC_TileEntityTree.class);
        AC_GuiTree.showUI(entity);
        return true;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
