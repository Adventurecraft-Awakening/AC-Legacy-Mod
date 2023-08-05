package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.common.AC_IBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(StairsBlock.class)
public abstract class MixinStairsBlock extends Block implements AC_IBlockColor {

    @Shadow
    private Block template;

    private int defaultColor;

    protected MixinStairsBlock(int i, Material arg) {
        super(i, arg);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setColorOnInit(int id, Block block, CallbackInfo ci) {
        if (block.material == Material.WOOD) {
            this.defaultColor = 16777215;
        } else {
            this.defaultColor = AC_IBlockColor.defaultColor;
        }
    }

    @Overwrite
    public void doesBoxCollide(World world, int x, int y, int z, AxixAlignedBoundingBox box, ArrayList hits) {
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.doesBoxCollide(world, x, y, z, box, hits);

        int coreMeta = world.getBlockMeta(x, y, z) & 3;
        if (coreMeta == 0) {
            Block blockNX = Block.BY_ID[world.getBlockId(x - 1, y, z)];
            if (blockNX != null && blockNX.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x - 1, y, z) & 3;
                if (meta == 2) {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            }

            Block blockPX = Block.BY_ID[world.getBlockId(x + 1, y, z)];
            if (blockPX != null && blockPX.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x + 1, y, z) & 3;
                if (meta == 2) {
                    this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            } else {
                this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                super.doesBoxCollide(world, x, y, z, box, hits);
            }
        } else if (coreMeta == 1) {
            Block blockNX = Block.BY_ID[world.getBlockId(x - 1, y, z)];
            if (blockNX != null && blockNX.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x - 1, y, z) & 3;
                if (meta == 3) {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 2) {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            } else {
                this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                super.doesBoxCollide(world, x, y, z, box, hits);
            }

            Block blockPX = Block.BY_ID[world.getBlockId(x + 1, y, z)];
            if (blockPX != null && blockPX.getRenderType() == this.getRenderType()){
                int meta = world.getBlockMeta(x + 1, y, z) & 3;
                if (meta == 2) {
                    this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 3) {
                    this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            }
        } else if (coreMeta == 2) {
            Block blockNZ = Block.BY_ID[world.getBlockId(x, y, z - 1)];
            if (blockNZ != null && blockNZ.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x, y, z - 1) & 3;
                if (meta == 1) {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 0) {
                    this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            }

            Block blockPZ = Block.BY_ID[world.getBlockId(x, y, z + 1)];
            if (blockPZ != null && blockPZ.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x, y, z + 1) & 3;
                if (meta == 0) {
                    this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 1) {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            } else {
                this.setBoundingBox(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                super.doesBoxCollide(world, x, y, z, box, hits);
            }
        } else if (coreMeta == 3) {
            Block blockPZ = Block.BY_ID[world.getBlockId(x, y, z + 1)];
            if (blockPZ != null && blockPZ.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x, y, z + 1) & 3;
                if (meta == 1) {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 0) {
                    this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            }

            Block blockNZ = Block.BY_ID[world.getBlockId(x, y, z - 1)];
            if (blockNZ != null && blockNZ.getRenderType() == this.getRenderType()) {
                int meta = world.getBlockMeta(x, y, z - 1) & 3;
                if (meta == 0) {
                    this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                } else if (meta == 1) {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.doesBoxCollide(world, x, y, z, box, hits);
                }
            } else {
                this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                super.doesBoxCollide(world, x, y, z, box, hits);
            }
        }
    }

    @Override
    public void drop(World world, int x, int y, int z, int meta) {
        this.template.drop(world, x, y, z, meta);
    }

    @Override
    public int getColorMultiplier(BlockView view, int x, int y, int z) {
        int meta = this.getColorMetaData(view, x, y, z);
        if (meta == 1) {
            meta = 16775065;
        } else if (meta == 2) {
            meta = 16767663;
        } else if (meta == 3) {
            meta = 10736540;
        } else if (meta == 4) {
            meta = 9755639;
        } else if (meta == 5) {
            meta = 8880573;
        } else if (meta == 6) {
            meta = 15539236;
        } else {
            meta = this.defaultColor;
        }
        return meta;
    }

    @Override
    public int getColorMetaData(BlockView view, int x, int y, int z) {
        return view.getBlockMeta(x, y, z) >> 2;
    }

    @Override
    public void setColorMetaData(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, world.getBlockMeta(x, y, z) & 3 | meta << 2);
    }
}
