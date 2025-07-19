package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.tile.AC_IBlockColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.StairsTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;

@Mixin(StairsTile.class)
public abstract class MixinStairsBlock extends Tile implements AC_IBlockColor {

    @Unique private int defaultColor;

    protected MixinStairsBlock(int i, Material arg) {
        super(i, arg);
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void setColorOnInit(int id, Tile block, CallbackInfo ci) {
        if (block.material == Material.WOOD) {
            this.defaultColor = 16777215;
        }
        else {
            this.defaultColor = AC_IBlockColor.defaultColor;
        }
    }

    @Overwrite
    public void addAABBs(Level world, int x, int y, int z, AABB box, ArrayList hits) {
        this.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.addAABBs(world, x, y, z, box, hits);

        int coreMeta = world.getData(x, y, z) & 3;
        if (coreMeta == 0) {
            Tile blockNX = Tile.tiles[world.getTile(x - 1, y, z)];
            if (blockNX != null && blockNX.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x - 1, y, z) & 3;
                if (meta == 2) {
                    this.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            }

            Tile blockPX = Tile.tiles[world.getTile(x + 1, y, z)];
            if (blockPX != null && blockPX.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x + 1, y, z) & 3;
                if (meta == 2) {
                    this.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            } else {
                this.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                super.addAABBs(world, x, y, z, box, hits);
            }
        } else if (coreMeta == 1) {
            Tile blockNX = Tile.tiles[world.getTile(x - 1, y, z)];
            if (blockNX != null && blockNX.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x - 1, y, z) & 3;
                if (meta == 3) {
                    this.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 2) {
                    this.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            } else {
                this.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                super.addAABBs(world, x, y, z, box, hits);
            }

            Tile blockPX = Tile.tiles[world.getTile(x + 1, y, z)];
            if (blockPX != null && blockPX.getRenderShape() == this.getRenderShape()){
                int meta = world.getData(x + 1, y, z) & 3;
                if (meta == 2) {
                    this.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            }
        } else if (coreMeta == 2) {
            Tile blockNZ = Tile.tiles[world.getTile(x, y, z - 1)];
            if (blockNZ != null && blockNZ.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x, y, z - 1) & 3;
                if (meta == 1) {
                    this.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 0) {
                    this.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            }

            Tile blockPZ = Tile.tiles[world.getTile(x, y, z + 1)];
            if (blockPZ != null && blockPZ.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x, y, z + 1) & 3;
                if (meta == 0) {
                    this.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 1) {
                    this.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            } else {
                this.setShape(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                super.addAABBs(world, x, y, z, box, hits);
            }
        } else if (coreMeta == 3) {
            Tile blockPZ = Tile.tiles[world.getTile(x, y, z + 1)];
            if (blockPZ != null && blockPZ.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x, y, z + 1) & 3;
                if (meta == 1) {
                    this.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 0) {
                    this.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            }

            Tile blockNZ = Tile.tiles[world.getTile(x, y, z - 1)];
            if (blockNZ != null && blockNZ.getRenderShape() == this.getRenderShape()) {
                int meta = world.getData(x, y, z - 1) & 3;
                if (meta == 0) {
                    this.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                } else if (meta == 1) {
                    this.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.addAABBs(world, x, y, z, box, hits);
                }
            } else {
                this.setShape(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                super.addAABBs(world, x, y, z, box, hits);
            }
        }
    }

    public @Override int getFoliageColor(LevelSource view, int x, int y, int z) {
        int meta = this.getColorMeta(view, x, y, z);
        return switch (meta) {
            case 1 -> 16775065;
            case 2 -> 16767663;
            case 3 -> 10736540;
            case 4 -> 9755639;
            case 5 -> 8880573;
            case 6 -> 15539236;
            default -> this.defaultColor;
        };
    }

    public @Override int getColorMeta(LevelSource view, int x, int y, int z) {
        return view.getData(x, y, z) >> 2;
    }

    public @Override void setColorMeta(Level world, int x, int y, int z, int meta) {
        world.setData(x, y, z, world.getData(x, y, z) & 3 | meta << 2);
    }
}
