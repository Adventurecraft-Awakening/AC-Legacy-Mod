package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import net.minecraft.block.Block;
import net.minecraft.class_66;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.particle.*;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldEventRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.CameraView;
import net.minecraft.client.util.EntityOppositeComparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.pathing.EntityPath;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

@Mixin(WorldEventRenderer.class)
public abstract class MixinWorldEventRenderer implements ExWorldEventRenderer {

    @Shadow
    public List blockEntities;
    @Shadow
    private World world;
    @Shadow
    private TextureManager textureManager;
    @Shadow
    private List<class_66> field_1807;
    @Shadow
    private class_66[] field_1808;
    @Shadow
    private class_66[] field_1809;
    @Shadow
    private int field_1810;
    @Shadow
    private int field_1811;
    @Shadow
    private int field_1812;
    @Shadow
    private int field_1813;
    @Shadow
    private Minecraft client;
    @Shadow
    private IntBuffer field_1816;
    @Shadow
    private boolean field_1817;
    @Shadow
    private int field_1818;
    @Shadow
    private int field_1776;
    @Shadow
    private int field_1777;
    @Shadow
    private int field_1778;
    @Shadow
    private int field_1779;
    @Shadow
    private int field_1780;
    @Shadow
    private int field_1781;
    @Shadow
    private int field_1782;
    @Shadow
    private int field_1783;
    @Shadow
    IntBuffer field_1797;
    @Shadow
    private int field_1787;
    @Shadow
    private int field_1788;
    @Shadow
    private int field_1789;
    @Shadow
    private int field_1790;
    @Shadow
    private int field_1791;
    @Shadow
    private int field_1792;
    @Shadow
    double field_1800;
    @Shadow
    double field_1801;
    @Shadow
    double field_1802;

    private long lastMovedTime = System.currentTimeMillis();
    private IntBuffer field_22019_aY = BufferUtils.createIntBuffer(65536);

    double prevReposX;
    double prevReposY;
    double prevReposZ;

    @Shadow
    protected abstract void method_1553(int i, int j, int k);

    @Overwrite
    public void method_1537() {
        Block.LEAVES.updateTexture(Config.isTreesFancy());
        this.field_1782 = this.client.options.viewDistance;
        int var1;
        if (this.field_1809 != null) {
            for (var1 = 0; var1 < this.field_1809.length; ++var1) {
                this.field_1809[var1].method_302();
            }
        }

        var1 = 64 << 3 - this.field_1782;
        if (Config.isLoadChunksFar()) {
            var1 = 512;
        }

        if (Config.isFarView()) {
            if (var1 < 512) {
                var1 *= 3;
            } else {
                var1 *= 2;
            }
        }

        var1 += Config.getPreloadedChunks() * 2 * 16;
        if (!Config.isFarView() && var1 > 400) {
            var1 = 400;
        }

        this.prevReposX = -9999.0D;
        this.prevReposY = -9999.0D;
        this.prevReposZ = -9999.0D;
        this.field_1810 = var1 / 16 + 1;
        this.field_1811 = 8;
        this.field_1812 = var1 / 16 + 1;
        this.field_1809 = new class_66[this.field_1810 * this.field_1811 * this.field_1812];
        this.field_1808 = new class_66[this.field_1810 * this.field_1811 * this.field_1812];
        int var2 = 0;
        int var3 = 0;
        this.field_1776 = 0;
        this.field_1777 = 0;
        this.field_1778 = 0;
        this.field_1779 = this.field_1810;
        this.field_1780 = this.field_1811;
        this.field_1781 = this.field_1812;

        int var4;
        for (var4 = 0; var4 < this.field_1807.size(); ++var4) {
            class_66 var5 = this.field_1807.get(var4);
            if (var5 != null) {
                var5.field_249 = false;
            }
        }

        this.field_1807.clear();
        this.blockEntities.clear();

        for (var4 = 0; var4 < this.field_1810; ++var4) {
            for (int var8 = 0; var8 < this.field_1811; ++var8) {
                for (int var6 = 0; var6 < this.field_1812; ++var6) {
                    int var7 = (var6 * this.field_1811 + var8) * this.field_1810 + var4;
                    this.field_1809[var7] = new class_66(this.world, this.blockEntities, var4 * 16, var8 * 16, var6 * 16, 16, this.field_1813 + var2);
                    if (this.field_1817) {
                        this.field_1809[var7].field_254 = this.field_1816.get(var3);
                    }

                    this.field_1809[var7].field_253 = false;
                    this.field_1809[var7].field_252 = true;
                    this.field_1809[var7].field_243 = false;
                    this.field_1809[var7].field_251 = var3++;
                    this.field_1809[var7].method_305();
                    this.field_1808[var7] = this.field_1809[var7];
                    this.field_1807.add(this.field_1809[var7]);
                    var2 += 3;
                }
            }
        }

        if (this.world != null) {
            Entity var9 = this.client.viewEntity;
            if (var9 == null) {
                var9 = this.client.player;
            }

            if (var9 != null) {
                this.method_1553(MathHelper.floor(var9.x), MathHelper.floor(var9.y), MathHelper.floor(var9.z));
                Arrays.sort(this.field_1808, new EntityOppositeComparator(var9));
            }
        }

        this.field_1783 = 2;
    }

    @Overwrite
    public int method_1548(LivingEntity var1, int var2, double var3) {
        if (this.field_1807.size() < 10) {
            byte var5 = 10;

            for (int var6 = 0; var6 < var5; ++var6) {
                this.field_1792 = (this.field_1792 + 1) % this.field_1809.length;
                class_66 var7 = this.field_1809[this.field_1792];
                if (var7.field_249 && !this.field_1807.contains(var7)) {
                    this.field_1807.add(var7);
                }
            }
        }

        if (this.client.options.viewDistance != this.field_1782 && !Config.isLoadChunksFar()) {
            ((ExChunkCache) this.world.worldSource).updateVeryFar();
            this.method_1537();
        }

        if (var2 == 0) {
            this.field_1787 = 0;
            this.field_1788 = 0;
            this.field_1789 = 0;
            this.field_1790 = 0;
            this.field_1791 = 0;
        }

        double var39 = var1.prevRenderX + (var1.x - var1.prevRenderX) * var3;
        double var40 = var1.prevRenderY + (var1.y - var1.prevRenderY) * var3;
        double var9 = var1.prevRenderZ + (var1.z - var1.prevRenderZ) * var3;
        double var11 = var1.x - this.field_1800;
        double var13 = var1.y - this.field_1801;
        double var15 = var1.z - this.field_1802;
        double var17 = var11 * var11 + var13 * var13 + var15 * var15;
        int var19;
        if (var17 > 64.0D) {
            this.field_1800 = var1.x;
            this.field_1801 = var1.y;
            this.field_1802 = var1.z;
            var19 = Config.getPreloadedChunks() * 64;
            double var20 = var1.x - this.prevReposX;
            double var22 = var1.y - this.prevReposY;
            double var24 = var1.z - this.prevReposZ;
            double var26 = var20 * var20 + var22 * var22 + var24 * var24;
            if (var26 > (double) (var19 * var19) + 64.0D) {
                this.prevReposX = var1.x;
                this.prevReposY = var1.y;
                this.prevReposZ = var1.z;
                this.method_1553(MathHelper.floor(var1.x), MathHelper.floor(var1.y), MathHelper.floor(var1.z));
            }

            Arrays.sort(this.field_1808, new EntityOppositeComparator(var1));
        }

        if (((ExGameOptions) this.client.options).ofSmoothFps() && var2 == 0) {
            GL11.glFinish();
        }

        if (((ExGameOptions) this.client.options).ofSmoothInput() && var2 == 0) {
            Config.sleep(1L);
        }

        byte var41 = 0;
        int var42 = 0;
        if (this.field_1817 && this.client.options.advancedOpengl && !this.client.options.anaglyph3d && var2 == 0) {
            byte var21 = 0;
            byte var43 = 20;
            this.checkOcclusionQueryResult(var21, var43, var1.x, var1.y, var1.z);

            int var23;
            for (var23 = var21; var23 < var43; ++var23) {
                this.field_1808[var23].field_252 = true;
            }

            var19 = var41 + this.method_1542(var21, var43, var2, var3);
            var23 = var43;
            int var44 = 0;
            byte var25 = 30;

            int var27;
            for (int var45 = this.field_1810 / 2; var23 < this.field_1808.length; var19 += this.method_1542(var27, var23, var2, var3)) {
                var27 = var23;
                if (var44 < var45) {
                    ++var44;
                } else {
                    --var44;
                }

                var23 += var44 * var25;
                if (var23 <= var27) {
                    var23 = var27 + 10;
                }

                if (var23 > this.field_1808.length) {
                    var23 = this.field_1808.length;
                }

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_FOG);
                GL11.glColorMask(false, false, false, false);
                GL11.glDepthMask(false);
                this.checkOcclusionQueryResult(var27, var23, var1.x, var1.y, var1.z);
                GL11.glPushMatrix();
                float var28 = 0.0F;
                float var29 = 0.0F;
                float var30 = 0.0F;

                for (int var31 = var27; var31 < var23; ++var31) {
                    class_66 var32 = this.field_1808[var31];
                    if (var32.method_304()) {
                        var32.field_243 = false;
                    } else if (var32.field_243) {
                        if (Config.isOcclusionFancy() && !((ExClass_66) var32).isInFrustrumFully()) {
                            var32.field_252 = true;
                        } else if (var32.field_243 && !var32.field_253) {
                            float var33;
                            float var34;
                            float var35;
                            float var36;
                            if (((ExClass_66) var32).isVisibleFromPosition()) {
                                var33 = Math.abs((float) (((ExClass_66) var32).visibleFromX() - var1.x));
                                var34 = Math.abs((float) (((ExClass_66) var32).visibleFromY() - var1.y));
                                var35 = Math.abs((float) (((ExClass_66) var32).visibleFromZ() - var1.z));
                                var36 = var33 + var34 + var35;
                                if ((double) var36 < 10.0D + (double) var31 / 1000.0D) {
                                    var32.field_252 = true;
                                    continue;
                                }

                                ((ExClass_66) var32).isVisibleFromPosition(false);
                            }

                            var33 = (float) ((double) var32.field_237 - var39);
                            var34 = (float) ((double) var32.field_238 - var40);
                            var35 = (float) ((double) var32.field_239 - var9);
                            var36 = var33 - var28;
                            float var37 = var34 - var29;
                            float var38 = var35 - var30;
                            if (var36 != 0.0F || var37 != 0.0F || var38 != 0.0F) {
                                GL11.glTranslatef(var36, var37, var38);
                                var28 += var36;
                                var29 += var37;
                                var30 += var38;
                            }

                            ARBOcclusionQuery.glBeginQueryARB(GL15.GL_SAMPLES_PASSED, var32.field_254);
                            var32.method_303();
                            ARBOcclusionQuery.glEndQueryARB(GL15.GL_SAMPLES_PASSED);
                            var32.field_253 = true;
                            ++var42;
                        }
                    }
                }

                GL11.glPopMatrix();
                GL11.glColorMask(true, true, true, true);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_FOG);
            }
        } else {
            var19 = var41 + this.method_1542(0, this.field_1808.length, var2, var3);
        }

        return var19;
    }

    private void checkOcclusionQueryResult(int var1, int var2, double var3, double var5, double var7) {
        for (int var9 = var1; var9 < var2; ++var9) {
            class_66 var10 = this.field_1808[var9];
            if (var10.field_253) {
                this.field_1797.clear();
                ARBOcclusionQuery.glGetQueryObjectuivARB(var10.field_254, GL15.GL_QUERY_RESULT_AVAILABLE, this.field_1797);
                if (this.field_1797.get(0) != 0) {
                    var10.field_253 = false;
                    this.field_1797.clear();
                    ARBOcclusionQuery.glGetQueryObjectuivARB(var10.field_254, GL15.GL_QUERY_RESULT, this.field_1797);
                    boolean var11 = var10.field_252;
                    var10.field_252 = this.field_1797.get(0) > 0;
                    if (var11 && var10.field_252) {
                        ((ExClass_66) var10).isVisibleFromPosition(true);
                        ((ExClass_66) var10).visibleFromX(var3);
                        ((ExClass_66) var10).visibleFromY(var5);
                        ((ExClass_66) var10).visibleFromZ(var7);
                    }
                }
            }
        }
    }

    @Overwrite
    private int method_1542(int var1, int var2, int var3, double var4) {
        this.field_22019_aY.clear();
        int var6 = 0;

        for (int var7 = var1; var7 < var2; ++var7) {
            if (var3 == 0) {
                ++this.field_1787;
                if (this.field_1808[var7].field_244[var3]) {
                    ++this.field_1791;
                } else if (!this.field_1808[var7].field_243) {
                    ++this.field_1788;
                } else if (this.field_1817 && !this.field_1808[var7].field_252) {
                    ++this.field_1789;
                } else {
                    ++this.field_1790;
                }
            }

            if (!this.field_1808[var7].field_244[var3] && this.field_1808[var7].field_243 && (!this.field_1817 || this.field_1808[var7].field_252)) {
                int var8 = this.field_1808[var7].method_297(var3);
                if (var8 >= 0) {
                    this.field_22019_aY.put(var8);
                    ++var6;
                }
            }
        }

        this.field_22019_aY.flip();
        LivingEntity var14 = this.client.viewEntity;
        double var15 = var14.prevRenderX + (var14.x - var14.prevRenderX) * var4;
        double var10 = var14.prevRenderY + (var14.y - var14.prevRenderY) * var4;
        double var12 = var14.prevRenderZ + (var14.z - var14.prevRenderZ) * var4;
        GL11.glTranslatef((float) (-var15), (float) (-var10), (float) (-var12));
        GL11.glCallLists(this.field_22019_aY);
        GL11.glTranslatef((float) var15, (float) var10, (float) var12);
        return var6;
    }

    @Overwrite
    public void method_1540(int var1, double var2) {
    }

    @Redirect(method = "renderSky", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 0))
    private void configurableSky1(int list) {
        if (Config.isSkyEnabled()) {
            GL11.glCallList(list);
        }
    }

    @Redirect(method = "renderSky", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 2))
    private void configurableSky2(int list) {
        if (Config.isSkyEnabled()) {
            GL11.glCallList(list);
        }
    }

    @Redirect(method = "renderSky", at = @At(
            value = "INVOKE",
            target = "Lorg/lwjgl/opengl/GL11;glCallList(I)V",
            remap = false,
            ordinal = 1))
    private void configurableStars(int list) {
        if (Config.isStarsEnabled()) {
            GL11.glCallList(list);
        }
    }

    @Inject(method = "method_1552", at = @At("HEAD"), cancellable = true)
    private void configurableClouds(float var1, CallbackInfo ci) {
        if (((ExGameOptions) this.client.options).ofClouds() == 3) {
            ci.cancel();
        }
    }

    @Redirect(method = "method_1552", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/options/GameOptions;fancyGraphics:Z"))
    private boolean fancyClouds(GameOptions instance) {
        return Config.isCloudsFancy();
    }

    @Redirect(method = "method_1552", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/dimension/Dimension;getCloudHeight()F"))
    private float redirectCloudHeight(Dimension instance) {
        float height = instance.getCloudHeight();
        height += ((ExGameOptions) this.client.options).ofCloudsHeight() * 25.0F;
        return height;
    }

    @Overwrite
    public void renderClouds(float var1) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        float var2 = (float) (this.client.viewEntity.prevRenderY + (this.client.viewEntity.y - this.client.viewEntity.prevRenderY) * (double) var1);
        Tessellator var3 = Tessellator.INSTANCE;
        float var4 = 12.0F;
        float var5 = 4.0F;
        double var6 = (this.client.viewEntity.prevX + (this.client.viewEntity.x - this.client.viewEntity.prevX) * (double) var1 + (double) (((float) this.field_1818 + var1) * 0.03F)) / (double) var4;
        double var8 = (this.client.viewEntity.prevZ + (this.client.viewEntity.z - this.client.viewEntity.prevZ) * (double) var1) / (double) var4 + (double) 0.33F;
        float var10 = this.world.dimension.getCloudHeight() - var2 + 0.33F;
        var10 += ((ExGameOptions) this.client.options).ofCloudsHeight() * 25.0F;
        int var11 = MathHelper.floor(var6 / 2048.0D);
        int var12 = MathHelper.floor(var8 / 2048.0D);
        var6 -= (var11 * 2048);
        var8 -= (var12 * 2048);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureManager.getTextureId("/environment/clouds.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Vec3d var13 = this.world.method_282(var1);
        float var14 = (float) var13.x;
        float var15 = (float) var13.y;
        float var16 = (float) var13.z;
        float var17;
        float var18;
        float var19;
        if (this.client.options.anaglyph3d) {
            var17 = (var14 * 30.0F + var15 * 59.0F + var16 * 11.0F) / 100.0F;
            var18 = (var14 * 30.0F + var15 * 70.0F) / 100.0F;
            var19 = (var14 * 30.0F + var16 * 70.0F) / 100.0F;
            var14 = var17;
            var15 = var18;
            var16 = var19;
        }

        var17 = (float) (var6 * 0.0D);
        var18 = (float) (var8 * 0.0D);
        var19 = 0.00390625F;
        var17 = (float) MathHelper.floor(var6) * var19;
        var18 = (float) MathHelper.floor(var8) * var19;
        float var20 = (float) (var6 - (double) MathHelper.floor(var6));
        float var21 = (float) (var8 - (double) MathHelper.floor(var8));
        byte var22 = 8;
        byte var23 = 3;
        float var24 = 1.0F / 1024.0F;
        GL11.glScalef(var4, 1.0F, var4);

        for (int var25 = 0; var25 < 2; ++var25) {
            if (var25 == 0) {
                GL11.glColorMask(false, false, false, false);
            } else if (this.client.options.anaglyph3d) {
                if (GameRenderer.field_2341 == 0) {
                    GL11.glColorMask(false, true, true, true);
                } else {
                    GL11.glColorMask(true, false, false, true);
                }
            } else {
                GL11.glColorMask(true, true, true, true);
            }

            double var26 = 0.02D;

            for (int var28 = -var23 + 1; var28 <= var23; ++var28) {
                for (int var29 = -var23 + 1; var29 <= var23; ++var29) {
                    var3.start();
                    float var30 = (float) (var28 * var22);
                    float var31 = (float) (var29 * var22);
                    float var32 = var30 - var20;
                    float var33 = var31 - var21;
                    var3.color(var14 * 0.9F, var15 * 0.9F, var16 * 0.9F, 0.8F);
                    int var34;
                    if (var28 > -1) {
                        var3.setNormal(-1.0F, 0.0F, 0.0F);

                        for (var34 = 0; var34 < var22; ++var34) {
                            var3.vertex(var32 + (float) var34 + 0.0F, (double) (var10 + 0.0F) + var26, var33 + (float) var22, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 0.0F, (double) (var10 + var5) - var26, var33 + (float) var22, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 0.0F, (double) (var10 + var5) - var26, var33 + 0.0F, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 0.0F, (double) (var10 + 0.0F) + var26, var33 + 0.0F, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                        }
                    }

                    if (var28 <= 1) {
                        var3.setNormal(1.0F, 0.0F, 0.0F);

                        for (var34 = 0; var34 < var22; ++var34) {
                            var3.vertex(var32 + (float) var34 + 1.0F - var24, (double) (var10 + 0.0F) + var26, var33 + (float) var22, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 1.0F - var24, (double) (var10 + var5) - var26, var33 + (float) var22, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 1.0F - var24, (double) (var10 + var5) - var26, var33 + 0.0F, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                            var3.vertex(var32 + (float) var34 + 1.0F - var24, (double) (var10 + 0.0F) + var26, var33 + 0.0F, (var30 + (float) var34 + 0.5F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                        }
                    }

                    var3.color(var14 * 0.8F, var15 * 0.8F, var16 * 0.8F, 0.8F);
                    if (var29 > -1) {
                        var3.setNormal(0.0F, 0.0F, -1.0F);

                        for (var34 = 0; var34 < var22; ++var34) {
                            var3.vertex(var32 + 0.0F, (double) (var10 + var5) - var26, var33 + (float) var34 + 0.0F, (var30 + 0.0F) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + (float) var22, (double) (var10 + var5) - var26, var33 + (float) var34 + 0.0F, (var30 + (float) var22) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + (float) var22, (double) (var10 + 0.0F) + var26, var33 + (float) var34 + 0.0F, (var30 + (float) var22) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + 0.0F, (double) (var10 + 0.0F) + var26, var33 + (float) var34 + 0.0F, (var30 + 0.0F) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                        }
                    }

                    if (var29 <= 1) {
                        var3.setNormal(0.0F, 0.0F, 1.0F);

                        for (var34 = 0; var34 < var22; ++var34) {
                            var3.vertex(var32 + 0.0F, (double) (var10 + var5) - var26, var33 + (float) var34 + 1.0F - var24, (var30 + 0.0F) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + (float) var22, (double) (var10 + var5) - var26, var33 + (float) var34 + 1.0F - var24, (var30 + (float) var22) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + (float) var22, (double) (var10 + 0.0F) + var26, var33 + (float) var34 + 1.0F - var24, (var30 + (float) var22) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                            var3.vertex(var32 + 0.0F, (double) (var10 + 0.0F) + var26, var33 + (float) var34 + 1.0F - var24, (var30 + 0.0F) * var19 + var17, (var31 + (float) var34 + 0.5F) * var19 + var18);
                        }
                    }

                    if (var10 > -var5 - 1.0F) {
                        var3.color(var14 * 0.7F, var15 * 0.7F, var16 * 0.7F, 0.8F);
                        var3.setNormal(0.0F, -1.0F, 0.0F);
                        var3.vertex(var32 + 0.0F, var10 + 0.0F, var33 + (float) var22, (var30 + 0.0F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                        var3.vertex(var32 + (float) var22, var10 + 0.0F, var33 + (float) var22, (var30 + (float) var22) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                        var3.vertex(var32 + (float) var22, var10 + 0.0F, var33 + 0.0F, (var30 + (float) var22) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                        var3.vertex(var32 + 0.0F, var10 + 0.0F, var33 + 0.0F, (var30 + 0.0F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                    }

                    if (var10 <= var5 + 1.0F) {
                        var3.color(var14, var15, var16, 0.8F);
                        var3.setNormal(0.0F, 1.0F, 0.0F);
                        var3.vertex(var32 + 0.0F, var10 + var5 - var24, var33 + (float) var22, (var30 + 0.0F) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                        var3.vertex(var32 + (float) var22, var10 + var5 - var24, var33 + (float) var22, (var30 + (float) var22) * var19 + var17, (var31 + (float) var22) * var19 + var18);
                        var3.vertex(var32 + (float) var22, var10 + var5 - var24, var33 + 0.0F, (var30 + (float) var22) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                        var3.vertex(var32 + 0.0F, var10 + var5 - var24, var33 + 0.0F, (var30 + 0.0F) * var19 + var17, (var31 + 0.0F) * var19 + var18);
                    }

                    var3.tessellate();
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Overwrite
    public boolean method_1549(LivingEntity var1, boolean var2) {
        if (this.field_1807.size() <= 0) {
            return false;
        } else {
            int var3 = 0;
            int var4 = Config.getUpdatesPerFrame();
            if (Config.isDynamicUpdates() && !this.isMoving(var1)) {
                var4 *= 3;
            }

            byte var5 = 4;
            int var6 = 0;
            class_66 var7 = null;
            float var8 = Float.MAX_VALUE;
            int var9 = -1;

            long avgFrameTime = 0L;
            if (AC_PlayerTorch.isTorchActive()) {
                avgFrameTime = ((ExMinecraft) Minecraft.instance).getAvgFrameTime();
            }

            int var10;
            for (var10 = 0; var10 < this.field_1807.size(); ++var10) {
                class_66 var11 = this.field_1807.get(var10);
                if (var11 == null) {
                    continue;
                }

                ++var6;
                if (!var11.field_249) {
                    this.field_1807.set(var10, null);
                } else {
                    float var12 = var11.method_299(var1);
                    if (var12 <= 256.0F && this.isActingNow()) {
                        var11.method_296();
                        var11.field_249 = false;
                        this.field_1807.set(var10, null);
                        ++var3;
                    } else {
                        if (var12 > 256.0F && var3 >= var4) {
                            break;
                        }

                        if (!var11.field_243) {
                            var12 *= var5;
                        }

                        if (var7 == null) {
                            var7 = var11;
                            var8 = var12;
                            var9 = var10;
                        } else if (var12 < var8) {
                            var7 = var11;
                            var8 = var12;
                            var9 = var10;
                        }
                    }
                }

                // TODO: investigate if this should be here. Optifine messed with this method a lot.
                if (AC_PlayerTorch.isTorchActive() && (var9 >= 3 || var10 > 40000000L || var9 >= 2 && var10 > 16666666L)) {
                    break;
                }
            }

            int var16;
            if (var7 != null) {
                var7.method_296();
                var7.field_249 = false;
                this.field_1807.set(var9, null);
                ++var3;
                float var15 = var8 / 5.0F;

                for (var16 = 0; var16 < this.field_1807.size() && var3 < var4; ++var16) {
                    class_66 var17 = this.field_1807.get(var16);
                    if (var17 != null) {
                        float var13 = var17.method_299(var1);
                        if (!var17.field_243) {
                            var13 *= var5;
                        }

                        float var14 = Math.abs(var13 - var8);
                        if (var14 < var15) {
                            var17.method_296();
                            var17.field_249 = false;
                            this.field_1807.set(var16, null);
                            ++var3;
                        }
                    }
                }
            }

            if (var6 == 0) {
                this.field_1807.clear();
            }

            if (this.field_1807.size() > 100 && var6 < this.field_1807.size() * 4 / 5) {
                var10 = 0;

                for (var16 = 0; var16 < this.field_1807.size(); ++var16) {
                    class_66 var18 = this.field_1807.get(var16);
                    if (var18 != null && var16 != var10) {
                        this.field_1807.set(var10, var18);
                        ++var10;
                    }
                }

                for (var16 = this.field_1807.size() - 1; var16 >= var10; --var16) {
                    this.field_1807.remove(var16);
                }
            }

            return true;
        }
    }

    private boolean isMoving(LivingEntity var1) {
        boolean var2 = this.isMovingNow(var1);
        if (var2) {
            this.lastMovedTime = System.currentTimeMillis();
            return true;
        } else {
            return System.currentTimeMillis() - this.lastMovedTime < 2000L;
        }
    }

    private boolean isMovingNow(LivingEntity var1) {
        double var2 = 0.001D;
        if (var1.jumping) return true;
        if (var1.method_1373()) return true;
        if ((double) var1.lastHandSwingProgress > var2) return true;
        if (this.client.mouseHelper.xDelta != 0) return true;
        if (this.client.mouseHelper.yDelta != 0) return true;
        if (Math.abs(var1.x - var1.prevX) > var2) return true;
        if (Math.abs(var1.y - var1.prevY) > var2) return true;
        return Math.abs(var1.z - var1.prevZ) > var2;
    }

    private boolean isActingNow() {
        if (Mouse.isButtonDown(0))
            return true;
        return Mouse.isButtonDown(1);
    }

    @Inject(method = "method_1148", at = @At("HEAD"), cancellable = true)
    public void cancelOnNull(CallbackInfo ci) {
        if (this.field_1809 == null) {
            ci.cancel();
        }
    }

    @Override
    public void setAllRenderersVisible() {
        if (this.field_1809 != null) {
            for (class_66 class66 : this.field_1809) {
                class66.field_252 = true;
            }
        }
    }

    @Override
    public int renderAllSortedRenderers(int var1, double var2) {
        return this.method_1542(0, this.field_1808.length, var1, var2);
    }

    @Override
    public void updateAllTheRenderers() {
        for (class_66 item : this.field_1809) {
            if (!item.field_249) {
                this.field_1807.add(item);
            }
            item.method_305();
        }
    }

    @Redirect(method = "renderEntities", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;F)V",
            ordinal = 1))
    private void renderEntityBasedOnCamera(EntityRenderDispatcher instance, Entity var7, float var3) {
        ExMinecraft exClient = (ExMinecraft) this.client;
        if ((!exClient.isCameraActive() || !exClient.isCameraPause()) && (!AC_DebugMode.active || var7 instanceof PlayerEntity) && ((ExEntity) var7).getStunned() <= 0) {
            instance.render(var7, var3);
        } else {
            instance.render(var7, 1.0F);
        }
    }

    @Inject(method = "renderEntities", at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;size()I",
            shift = At.Shift.AFTER,
            ordinal = 0,
            remap = false))
    private void renderScriptModels(Vec3d var1, CameraView var2, float var3, CallbackInfo ci) {
        GL11.glPushMatrix();
        GL11.glTranslated(-EntityRenderDispatcher.field_2490, -EntityRenderDispatcher.field_2491, -EntityRenderDispatcher.field_2492);
        //ScriptModel.renderAll(var3); TODO
        GL11.glPopMatrix();
    }

    /* TODO: Optifine messes with this. Figure out where to put it?
    @Inject(method = "method_1548", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/render/WorldEventRenderer;field_1808:[Lnet/minecraft/class_66;",
            shift = At.Shift.BEFORE,
            ordinal = 8))
    private void checkFarPlane(
            LivingEntity var1,
            int var2,
            double var3,
            CallbackInfoReturnable<Integer> cir,
            @Local(name = "var25") int var25) {

        if (this.field_1808[var25].field_252) {
            float var37 = ((ExGameRenderer) this.client.gameRenderer).getFarPlane() * 1.25F;
            this.field_1808[var25].field_252 = this.field_1808[var25].method_299(var1) > var37;
        }
    }
    */

    @ModifyExpressionValue(method = "method_1552", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;prevRenderY:D",
            ordinal = 0))
    private double changeCameraY(double value, @Local(ordinal = 0, argsOnly = true) float var1) {
        ExMinecraft exClient = (ExMinecraft) this.client;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint var7 = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return var7.posY;
        }
        return value;
    }

    @ModifyExpressionValue(method = "method_1552", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;prevX:D",
            ordinal = 0))
    private double changeCameraX(double value, @Local(ordinal = 0, argsOnly = true) float var1) {
        ExMinecraft exClient = (ExMinecraft) this.client;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint var7 = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return (double) var7.posX + (double) (((float) this.field_1818 + var1) * 0.03F);
        }
        return value;
    }

    @ModifyExpressionValue(method = "method_1552", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;prevX:D",
            ordinal = 0))
    private double changeCameraZ(double value, @Local(ordinal = 0, argsOnly = true) float var1) {
        ExMinecraft exClient = (ExMinecraft) this.client;
        if (exClient.isCameraActive()) {
            AC_CutsceneCameraPoint var7 = exClient.getCutsceneCamera().getCurrentPoint(var1);
            return var7.posZ;
        }
        return value;
    }

    public void drawCursorSelection(LivingEntity var1, ItemStack var2, float var3) {
        if (AC_ItemCursor.bothSet && var2 != null && var2.itemId >= AC_Items.cursor.id && var2.itemId <= AC_Items.cursor.id + 20) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1.0F, 0.6F, 0.0F, 0.4F);
            GL11.glLineWidth(3.0F);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            int var4 = Math.min(AC_ItemCursor.oneX, AC_ItemCursor.twoX);
            int var5 = Math.max(AC_ItemCursor.oneX, AC_ItemCursor.twoX) + 1;
            int var6 = Math.min(AC_ItemCursor.oneY, AC_ItemCursor.twoY);
            int var7 = Math.max(AC_ItemCursor.oneY, AC_ItemCursor.twoY) + 1;
            int var8 = Math.min(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ);
            int var9 = Math.max(AC_ItemCursor.oneZ, AC_ItemCursor.twoZ) + 1;
            double var10 = var1.prevRenderX + (var1.x - var1.prevRenderX) * (double) var3;
            double var12 = var1.prevRenderY + (var1.y - var1.prevRenderY) * (double) var3;
            double var14 = var1.prevRenderZ + (var1.z - var1.prevRenderZ) * (double) var3;
            Tessellator var16 = Tessellator.INSTANCE;

            int var17;
            for (var17 = var4; var17 <= var5; ++var17) {
                var16.start(3);
                var16.addVertex((double) var17 - var10, (double) var6 - var12, (double) var8 - var14);
                var16.addVertex((double) var17 - var10, (double) var7 - var12, (double) var8 - var14);
                var16.addVertex((double) var17 - var10, (double) var7 - var12, (double) var9 - var14);
                var16.addVertex((double) var17 - var10, (double) var6 - var12, (double) var9 - var14);
                var16.addVertex((double) var17 - var10, (double) var6 - var12, (double) var8 - var14);
                var16.tessellate();
            }

            for (var17 = var6; var17 <= var7; ++var17) {
                var16.start(3);
                var16.addVertex((double) var4 - var10, (double) var17 - var12, (double) var8 - var14);
                var16.addVertex((double) var5 - var10, (double) var17 - var12, (double) var8 - var14);
                var16.addVertex((double) var5 - var10, (double) var17 - var12, (double) var9 - var14);
                var16.addVertex((double) var4 - var10, (double) var17 - var12, (double) var9 - var14);
                var16.addVertex((double) var4 - var10, (double) var17 - var12, (double) var8 - var14);
                var16.tessellate();
            }

            for (var17 = var8; var17 <= var9; ++var17) {
                var16.start(3);
                var16.addVertex((double) var4 - var10, (double) var6 - var12, (double) var17 - var14);
                var16.addVertex((double) var5 - var10, (double) var6 - var12, (double) var17 - var14);
                var16.addVertex((double) var5 - var10, (double) var7 - var12, (double) var17 - var14);
                var16.addVertex((double) var4 - var10, (double) var7 - var12, (double) var17 - var14);
                var16.addVertex((double) var4 - var10, (double) var6 - var12, (double) var17 - var14);
                var16.tessellate();
            }

            GL11.glLineWidth(1.0F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
        }

    }

    public void drawEntityPath(Entity var1, LivingEntity var2, float var3) {
        if (!(var1 instanceof IEntityPather var4)) {
            return;
        }

        EntityPath var5 = var4.getCurrentPath();
        if (var5 == null) {
            return;
        }

        double var6 = var2.prevRenderX + (var2.x - var2.prevRenderX) * (double) var3;
        double var8 = var2.prevRenderY + (var2.y - var2.prevRenderY) * (double) var3;
        double var10 = var2.prevRenderZ + (var2.z - var2.prevRenderZ) * (double) var3;
        Tessellator var12 = Tessellator.INSTANCE;
        var12.start(3);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (var1 instanceof MobEntity mob && mob.method_634() != null) {
            GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.4F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 0.0F, 0.4F);
        }

        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        var12.addVertex(var1.x - var6, var1.y - var8, var1.z - var10);

        for (int var13 = var5.field_2692; var13 < var5.field_2690; ++var13) {
            PathNode var14 = var5.field_2691[var13];
            var12.addVertex((double) var14.x - var6 + 0.5D, (double) var14.y - var8 + 0.5D, (double) var14.z - var10 + 0.5D);
        }

        var12.tessellate();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawEntityFOV(LivingEntity var1, LivingEntity var2, float var3) {
        if (var1 == var2) {
            return;
        }

        double var4 = var2.prevRenderX + (var2.x - var2.prevRenderX) * (double) var3;
        double var6 = var2.prevRenderY + (var2.y - var2.prevRenderY) * (double) var3;
        double var8 = var2.prevRenderZ + (var2.z - var2.prevRenderZ) * (double) var3;
        Tessellator var10 = Tessellator.INSTANCE;
        var10.start(3);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        if (((ExLivingEntity) var1).getExtraFov() > 0.0F) {
            GL11.glColor4f(1.0F, 0.5F, 0.0F, 0.4F);
        } else {
            GL11.glColor4f(0.0F, 1.0F, 0.0F, 0.4F);
        }

        GL11.glLineWidth(5.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float var11 = Math.min(((ExLivingEntity) var1).getFov() / 2.0F + ((ExLivingEntity) var1).getExtraFov(), 180.0F);
        double var12 = 5.0D * Math.sin(-Math.PI * (double) (var1.yaw - var11) / 180.0D) + var1.x;
        double var14 = 5.0D * Math.cos(-Math.PI * (double) (var1.yaw - var11) / 180.0D) + var1.z;
        var10.addVertex(var12 - var4, var1.y - var6 + (double) var1.getStandingEyeHeight(), var14 - var8);
        var10.addVertex(var1.x - var4, var1.y - var6 + (double) var1.getStandingEyeHeight(), var1.z - var8);
        var12 = 5.0D * Math.sin(-Math.PI * (double) (var1.yaw + var11) / 180.0D) + var1.x;
        var14 = 5.0D * Math.cos(-Math.PI * (double) (var1.yaw + var11) / 180.0D) + var1.z;
        var10.addVertex(var12 - var4, var1.y - var6 + (double) var1.getStandingEyeHeight(), var14 - var8);
        var10.tessellate();
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Overwrite
    public void addParticle(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
        this.spawnParticleR(var1, var2, var4, var6, var8, var10, var12);
    }

    @Override
    public ParticleEntity spawnParticleR(String var1, double var2, double var4, double var6, double var8, double var10, double var12) {
        if (this.client != null && this.client.viewEntity != null && this.client.particleManager != null) {
            double var14 = this.client.viewEntity.x - var2;
            double var16 = this.client.viewEntity.y - var4;
            double var18 = this.client.viewEntity.z - var6;
            double var20 = 16384.0D;
            if (var14 * var14 + var16 * var16 + var18 * var18 > var20 * var20) {
                return null;
            } else {
                ParticleEntity var22 = switch (var1) {
                    case "bubble" -> new BubbleParticle(this.world, var2, var4, var6, var8, var10, var12);
                    case "smoke" -> new SmokeParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    case "note" -> new NoteParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    case "portal" -> new PortalParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    case "explode" -> new ExplosionParticle(this.world, var2, var4, var6, var8, var10, var12);
                    case "flame" -> new FireParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    case "lava" -> new LavaParticle(this.world, var2, var4, var6);
                    case "footstep" -> new FootstepParticle(this.textureManager, this.world, var2, var4, var6);
                    case "splash" -> new WaterParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    case "largesmoke" ->
                            new SmokeParticleEntity(this.world, var2, var4, var6, var8, var10, var12, 2.5F);
                    case "reddust" ->
                            new RedstoneParticleEntity(this.world, var2, var4, var6, (float) var8, (float) var10, (float) var12);
                    case "snowballpoof" -> new PoofParticleEntity(this.world, var2, var4, var6, Item.SNOWBALL);
                    case "snowshovel" -> new SnowPuffParticle(this.world, var2, var4, var6, var8, var10, var12);
                    case "slime" -> new PoofParticleEntity(this.world, var2, var4, var6, Item.SLIMEBALL);
                    case "heart" -> new HeartParticleEntity(this.world, var2, var4, var6, var8, var10, var12);
                    default -> null;
                };

                if (var22 != null) {
                    this.client.particleManager.addParticle(var22);
                }

                return var22;
            }
        } else {
            return null;
        }
    }

    public void resetAll() {
        this.doReset(false);
    }

    public void resetForDeath() {
        this.doReset(true);
    }

    private void doReset(boolean var1) {
        AC_DebugMode.triggerResetActive = true;

        for (class_66 item : this.field_1809) {
            int var3 = item.field_231;
            int var4 = item.field_232;
            int var5 = item.field_233;
            if (this.world.method_155(var3, var4, var5, var3 + 15, var4 + 15, var5 + 15)) {
                for (int var6 = 0; var6 < 16; ++var6) {
                    for (int var7 = 0; var7 < 16; ++var7) {
                        for (int var8 = 0; var8 < 16; ++var8) {
                            int var9 = this.world.getBlockId(var3 + var6, var4 + var7, var5 + var8);
                            if (var9 > 0) {
                                ((ExBlock) Block.BY_ID[var9]).reset(this.world, var3 + var6, var4 + var7, var5 + var8, var1);
                            }
                        }
                    }
                }
            }
        }

        AC_DebugMode.triggerResetActive = false;
    }
}
