package dev.adventurecraft.awakening.tile;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

public class AC_BlockPlant extends Tile implements AC_IBlockColor {

    private static final float SIZE = 0.2F;

    protected AC_BlockPlant(int var1, int var2) {
        super(var1, var2, Material.PLANT);
        this.setShape(0.5F - SIZE, 0.0F, 0.5F - SIZE, 0.5F + SIZE, SIZE * 3.0F, 0.5F + SIZE);
    }

    @Override
    public int getTexture(int var1, int var2) {
        return this.tex + var2;
    }

    @Override
    public AABB getAABB(Level var1, int var2, int var3, int var4) {
        return null;
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
        return BlockShapes.REEDS;
    }

    @Override
    public int getMaxColorMeta() {
        return ExBlock.subTypes[this.id];
    }
}
