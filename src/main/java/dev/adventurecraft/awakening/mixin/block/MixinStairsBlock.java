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
    private void setColorOnInit(int var1, Block var2, CallbackInfo ci) {
        if (var2.material == Material.WOOD) {
            this.defaultColor = 16777215;
        } else {
            this.defaultColor = AC_IBlockColor.defaultColor;
        }
    }

    @Overwrite
    public void doesBoxCollide(World var1, int var2, int var3, int var4, AxixAlignedBoundingBox var5, ArrayList var6) {
        int var7 = var1.getBlockMeta(var2, var3, var4) & 3;
        this.setBoundingBox(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
        if (var7 == 0) {
            Block var8 = Block.BY_ID[var1.getBlockId(var2 - 1, var3, var4)];
            int var9;
            if (var8 != null && var8.getRenderType() == 10) {
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
            if (var8 != null && var8.getRenderType() == 10) {
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
                if (var11 != null && var11.getRenderType() == 10) {
                    if (var10 == 3) {
                        this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 2) {
                        this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }

                var11 = Block.BY_ID[var1.getBlockId(var2 + 1, var3, var4)];
                if (var11 != null && var11.getRenderType() == 10) {
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
                if (var11 != null && var11.getRenderType() == 10) {
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
                if (var11 != null && var11.getRenderType() == 10) {
                    if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 1) {
                        this.setBoundingBox(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            } else if (var7 == 3) {
                var11 = Block.BY_ID[var1.getBlockId(var2, var3, var4 + 1)];
                if (var11 != null && var11.getRenderType() == 10) {
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
                if (var11 != null && var11.getRenderType() == 10) {
                    if (var10 == 0) {
                        this.setBoundingBox(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    } else if (var10 == 1) {
                        this.setBoundingBox(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                        super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                    }
                } else {
                    this.setBoundingBox(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                    super.doesBoxCollide(var1, var2, var3, var4, var5, var6);
                }
            }
        }
    }

    @Override
    public void drop(World var1, int var2, int var3, int var4, int var5) {
        this.template.drop(var1, var2, var3, var4, var5);
    }

    @Override
    public int getDefaultColor() {
        return this.defaultColor;
    }

    @Override
    public int getColorMetaData(BlockView var1, int var2, int var3, int var4) {
        return var1.getBlockMeta(var2, var3, var4) >> 2;
    }

    @Override
    public void setColorMetaData(World var1, int var2, int var3, int var4, int var5) {
        var1.setBlockMeta(var2, var3, var4, var1.getBlockMeta(var2, var3, var4) & 3 | var5 << 2);
    }
}
