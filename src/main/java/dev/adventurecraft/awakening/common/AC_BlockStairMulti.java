package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AC_BlockStairMulti extends Block implements AC_IBlockColor {

    private Block modelBlock;

    protected AC_BlockStairMulti(int var1, Block var2, int var3) {
        super(var1, var3, var2.material);
        this.modelBlock = var2;
        this.setHardness(var2.getHardness());
        this.setBlastResistance(var2.resistance / 3.0F);
        this.setSounds(var2.sounds);
        this.setLightOpacity(255);
    }

    @Override
    public void updateBoundingBox(BlockView view, int x, int y, int z) {
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public AxixAlignedBoundingBox getCollisionShape(World world, int x, int y, int z) {
        return super.getCollisionShape(world, x, y, z);
    }

    @Override
    public boolean isFullOpaque() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 10;
    }

    @Override
    public boolean isSideRendered(BlockView view, int x, int y, int z, int side) {
        return super.isSideRendered(view, x, y, z, side);
    }

    @Override
    public void doesBoxCollide(World world, int x, int y, int z, AxixAlignedBoundingBox box, ArrayList hits) {
        int coreMeta = world.getBlockMeta(x, y, z) & 3;
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.doesBoxCollide(world, x, y, z, box, hits);
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
        } else {
            if (coreMeta == 1) {
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
                if (blockPX != null && blockPX.getRenderType() == this.getRenderType()) {
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

        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        this.modelBlock.randomDisplayTick(world, x, y, z, rand);
    }

    @Override
    public void activate(World world, int x, int y, int z, PlayerEntity player) {
        this.modelBlock.activate(world, x, y, z, player);
    }

    @Override
    public void activate(World world, int x, int y, int z, int var5) {
        this.modelBlock.activate(world, x, y, z, var5);
    }

    @Override
    public float getBrightness(BlockView view, int x, int y, int z) {
        return this.modelBlock.getBrightness(view, x, y, z);
    }

    @Override
    public float getBlastResistance(Entity var1) {
        return this.modelBlock.getBlastResistance(var1);
    }

    @Override
    public int getRenderPass() {
        return this.modelBlock.getRenderPass();
    }

    @Override
    public int getDropId(int meta, Random rand) {
        return this.modelBlock.getDropId(meta, rand);
    }

    @Override
    public int getDropCount(Random rand) {
        return this.modelBlock.getDropCount(rand);
    }

    @Override
    public int getTextureForSide(int var1, int var2) {
        return this.texture + (var2 >> 2);
    }

    @Override
    public int getTickrate() {
        return this.modelBlock.getTickrate();
    }

    @Override
    public AxixAlignedBoundingBox getOutlineShape(World world, int x, int y, int z) {
        return this.modelBlock.getOutlineShape(world, x, y, z);
    }

    @Override
    public void onCollideWithEntity(World world, int x, int y, int z, Entity entity, Vec3d var6) {
        this.modelBlock.onCollideWithEntity(world, x, y, z, entity, var6);
    }

    @Override
    public boolean isCollidable() {
        return this.modelBlock.isCollidable();
    }

    @Override
    public boolean isCollidable(int var1, boolean var2) {
        return this.modelBlock.isCollidable(var1, var2);
    }

    @Override
    public boolean canPlaceAt(World world, int x, int y, int z) {
        return this.modelBlock.canPlaceAt(world, x, y, z);
    }

    @Override
    public void onBlockPlaced(World world, int x, int y, int z) {
        this.onAdjacentBlockUpdate(world, x, y, z, 0);
        this.modelBlock.onBlockPlaced(world, x, y, z);
    }

    @Override
    public void onBlockRemoved(World world, int x, int y, int z) {
        this.modelBlock.onBlockRemoved(world, x, y, z);
    }

    @Override
    public void beforeDestroyedByExplosion(World world, int x, int y, int z, int meta, float var6) {
        this.modelBlock.beforeDestroyedByExplosion(world, x, y, z, meta, var6);
    }

    @Override
    public void drop(World world, int x, int y, int z, int meta) {
        this.modelBlock.drop(world, x, y, z, meta);
    }

    @Override
    public void onSteppedOn(World world, int x, int y, int z, Entity entity) {
        this.modelBlock.onSteppedOn(world, x, y, z, entity);
    }

    @Override
    public void onScheduledTick(World world, int x, int y, int z, Random rand) {
        this.modelBlock.onScheduledTick(world, x, y, z, rand);
    }

    @Override
    public boolean canUse(World world, int x, int y, int z, PlayerEntity player) {
        return this.modelBlock.canUse(world, x, y, z, player);
    }

    @Override
    public void onDestroyedByExplosion(World world, int x, int y, int z) {
        this.modelBlock.onDestroyedByExplosion(world, x, y, z);
    }

    @Override
    public void afterPlaced(World world, int x, int y, int z, LivingEntity placer) {
        int meta = world.getBlockMeta(x, y, z);
        int direction = MathHelper.floor((double) (placer.yaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (direction == 0) {
            world.setBlockMeta(x, y, z, 2 + meta);
        }

        if (direction == 1) {
            world.setBlockMeta(x, y, z, 1 + meta);
        }

        if (direction == 2) {
            world.setBlockMeta(x, y, z, 3 + meta);
        }

        if (direction == 3) {
            world.setBlockMeta(x, y, z, 0 + meta);
        }
    }

    @Override
    public int getColorMetaData(BlockView view, int x, int y, int z) {
        return view.getBlockMeta(x, y, z) >> 2;
    }

    @Override
    public void setColorMetaData(World world, int x, int y, int z, int meta) {
        world.setBlockMeta(x, y, z, world.getBlockMeta(x, y, z) & 3 | meta << 2);
    }

    @Override
    public void incrementColor(World world, int x, int y, int z) {
        int var5 = (this.getColorMetaData(world, x, y, z) + 1) % 16;
        this.setColorMetaData(world, x, y, z, var5);
    }
}
