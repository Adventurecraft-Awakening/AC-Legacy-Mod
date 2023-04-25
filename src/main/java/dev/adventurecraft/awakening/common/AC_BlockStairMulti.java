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

    public void updateBoundingBox(BlockView var1, int var2, int var3, int var4) {
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public AxixAlignedBoundingBox getCollisionShape(World var1, int var2, int var3, int var4) {
        return super.getCollisionShape(var1, var2, var3, var4);
    }

    public boolean isFullOpaque() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public int getRenderType() {
        return 10;
    }

    public boolean isSideRendered(BlockView var1, int var2, int var3, int var4, int var5) {
        return super.isSideRendered(var1, var2, var3, var4, var5);
    }

    public void doesBoxCollide(World var1, int var2, int var3, int var4, AxixAlignedBoundingBox var5, ArrayList var6) {
        int var7 = var1.getBlockMeta(var2, var3, var4) & 3;
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
        if (var7 == 0) {
            Block var8 = Block.BY_ID[var1.getBlockId(var2 - 1, var3, var4)];
            int var9;
            if (var8 != null && var8.getRenderType() == this.getRenderType()) {
                var9 = var1.getBlockMeta(var2 - 1, var3, var4) & 3;
                if (var9 == 2) {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                } else if (var9 == 3) {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            }

            var9 = var1.getBlockMeta(var2 + 1, var3, var4) & 3;
            var8 = Block.BY_ID[var1.getBlockId(var2 + 1, var3, var4)];
            if (var8 != null && var8.getRenderType() == this.getRenderType() && (var9 == 2 || var9 == 3)) {
                if (var9 == 2) {
                    this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                } else if (var9 == 3) {
                    this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            } else {
                this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
                super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
            }
        } else {
            int var10;
            Block var11;
            if (var7 == 1) {
                var10 = var1.getBlockMeta(var2 - 1, var3, var4) & 3;
                var11 = Block.BY_ID[var1.getBlockId(var2 - 1, var3, var4)];
                if (var11 != null && var11.getRenderType() == this.getRenderType() && (var10 == 2 || var10 == 3)) {
                    if (var10 == 3) {
                        this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else {
                        this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }

                var11 = Block.BY_ID[var1.getBlockId(var2 + 1, var3, var4)];
                if (var11 != null && var11.getRenderType() == this.getRenderType()) {
                    var10 = var1.getBlockMeta(var2 + 1, var3, var4) & 3;
                    if (var10 == 2) {
                        this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 3) {
                        this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                }
            } else if (var7 == 2) {
                var11 = Block.BY_ID[var1.getBlockId(var2, var3, var4 - 1)];
                if (var11 != null && var11.getRenderType() == this.getRenderType()) {
                    var10 = var1.getBlockMeta(var2, var3, var4 - 1) & 3;
                    if (var10 == 1) {
                        this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                }

                var10 = var1.getBlockMeta(var2, var3, var4 + 1) & 3;
                var11 = Block.BY_ID[var1.getBlockId(var2, var3, var4 + 1)];
                if (var11 != null && var11.getRenderType() == this.getRenderType() && (var10 == 0 || var10 == 1)) {
                    if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else {
                        this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            } else if (var7 == 3) {
                var11 = Block.BY_ID[var1.getBlockId(var2, var3, var4 + 1)];
                if (var11 != null && var11.getRenderType() == this.getRenderType()) {
                    var10 = var1.getBlockMeta(var2, var3, var4 + 1) & 3;
                    if (var10 == 1) {
                        this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                }

                var10 = var1.getBlockMeta(var2, var3, var4 - 1) & 3;
                var11 = Block.BY_ID[var1.getBlockId(var2, var3, var4 - 1)];
                if (var11 != null && var11.getRenderType() == this.getRenderType() && (var10 == 0 || var10 == 1)) {
                    if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else {
                        this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            }
        }

        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void randomDisplayTick(World var1, int var2, int var3, int var4, Random var5) {
        this.modelBlock.randomDisplayTick(var1, var2, var3, var4, var5);
    }

    public void activate(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        this.modelBlock.activate(var1, var2, var3, var4, var5);
    }

    public void activate(World var1, int var2, int var3, int var4, int var5) {
        this.modelBlock.activate(var1, var2, var3, var4, var5);
    }

    public float getBrightness(BlockView var1, int var2, int var3, int var4) {
        return this.modelBlock.getBrightness(var1, var2, var3, var4);
    }

    public float getBlastResistance(Entity var1) {
        return this.modelBlock.getBlastResistance(var1);
    }

    public int getRenderPass() {
        return this.modelBlock.getRenderPass();
    }

    public int getDropId(int var1, Random var2) {
        return this.modelBlock.getDropId(var1, var2);
    }

    public int getDropCount(Random var1) {
        return this.modelBlock.getDropCount(var1);
    }

    public int getTextureForSide(int var1, int var2) {
        return this.texture + (var2 >> 2);
    }

    public int getTickrate() {
        return this.modelBlock.getTickrate();
    }

    public AxixAlignedBoundingBox getOutlineShape(World var1, int var2, int var3, int var4) {
        return this.modelBlock.getOutlineShape(var1, var2, var3, var4);
    }

    public void onCollideWithEntity(World var1, int var2, int var3, int var4, Entity var5, Vec3d var6) {
        this.modelBlock.onCollideWithEntity(var1, var2, var3, var4, var5, var6);
    }

    public boolean isCollidable() {
        return this.modelBlock.isCollidable();
    }

    public boolean isCollidable(int var1, boolean var2) {
        return this.modelBlock.isCollidable(var1, var2);
    }

    public boolean canPlaceAt(World var1, int var2, int var3, int var4) {
        return this.modelBlock.canPlaceAt(var1, var2, var3, var4);
    }

    public void onBlockPlaced(World var1, int var2, int var3, int var4) {
        this.onAdjacentBlockUpdate(var1, var2, var3, var4, 0);
        this.modelBlock.onBlockPlaced(var1, var2, var3, var4);
    }

    public void onBlockRemoved(World var1, int var2, int var3, int var4) {
        this.modelBlock.onBlockRemoved(var1, var2, var3, var4);
    }

    public void beforeDestroyedByExplosion(World var1, int var2, int var3, int var4, int var5, float var6) {
        this.modelBlock.beforeDestroyedByExplosion(var1, var2, var3, var4, var5, var6);
    }

    public void drop(World var1, int var2, int var3, int var4, int var5) {
        this.modelBlock.drop(var1, var2, var3, var4, var5);
    }

    public void onSteppedOn(World var1, int var2, int var3, int var4, Entity var5) {
        this.modelBlock.onSteppedOn(var1, var2, var3, var4, var5);
    }

    public void onScheduledTick(World var1, int var2, int var3, int var4, Random var5) {
        this.modelBlock.onScheduledTick(var1, var2, var3, var4, var5);
    }

    public boolean canUse(World var1, int var2, int var3, int var4, PlayerEntity var5) {
        return this.modelBlock.canUse(var1, var2, var3, var4, var5);
    }

    public void onDestroyedByExplosion(World var1, int var2, int var3, int var4) {
        this.modelBlock.onDestroyedByExplosion(var1, var2, var3, var4);
    }

    public void afterPlaced(World var1, int var2, int var3, int var4, LivingEntity var5) {
        int var6 = var1.getBlockMeta(var2, var3, var4);
        int var7 = MathHelper.floor((double) (var5.yaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (var7 == 0) {
            var1.setBlockMeta(var2, var3, var4, 2 + var6);
        }

        if (var7 == 1) {
            var1.setBlockMeta(var2, var3, var4, 1 + var6);
        }

        if (var7 == 2) {
            var1.setBlockMeta(var2, var3, var4, 3 + var6);
        }

        if (var7 == 3) {
            var1.setBlockMeta(var2, var3, var4, 0 + var6);
        }
    }

    protected int getColorMetaData(BlockView var1, int var2, int var3, int var4) {
        return var1.getBlockMeta(var2, var3, var4) >> 2;
    }

    protected void setColorMetaData(World var1, int var2, int var3, int var4, int var5) {
        var1.setBlockMeta(var2, var3, var4, var1.getBlockMeta(var2, var3, var4) & 3 | var5 << 2);
    }

    public void incrementColor(World var1, int var2, int var3, int var4) {
        int var5 = (this.getColorMetaData(var1, var2, var3, var4) + 1) % 16;
        this.setColorMetaData(var1, var2, var3, var4, var5);
    }
}
