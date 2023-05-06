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
import net.minecraft.client.render.RenderList;
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
    int[] field_1796;
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
    private RenderList[] renderLists;
    @Shadow
    double field_1800;
    @Shadow
    double field_1801;
    @Shadow
    double field_1802;

    private long lastMovedTime = System.currentTimeMillis();
    private IntBuffer renderListBuffer = BufferUtils.createIntBuffer(65536);

    double prevReposX;
    double prevReposY;
    double prevReposZ;

    @Shadow
    protected abstract void method_1553(int i, int j, int k);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void removeNeedlessAllocs(Minecraft var1, TextureManager var2, CallbackInfo ci) {
        this.renderLists = null;
        this.field_1796 = null;
    }

    @Overwrite
    public void method_1537() {
        Block.LEAVES.updateTexture(Config.isLeavesFancy());
        this.field_1782 = this.client.options.viewDistance;
        int renderDist;
        if (this.field_1809 != null) {
            for (renderDist = 0; renderDist < this.field_1809.length; ++renderDist) {
                this.field_1809[renderDist].method_302();
            }
        }

        renderDist = 64 << 3 - this.field_1782;
        if (Config.isLoadChunksFar()) {
            renderDist = 512;
        }

        if (Config.isFarView()) {
            if (renderDist < 512) {
                renderDist *= 3;
            } else {
                renderDist *= 2;
            }
        }

        renderDist += Config.getPreloadedChunks() * 2 * 16;
        if (!Config.isFarView() && renderDist > 400) {
            renderDist = 400;
        }

        this.prevReposX = -9999.0D;
        this.prevReposY = -9999.0D;
        this.prevReposZ = -9999.0D;
        this.field_1810 = renderDist / 16 + 1;
        this.field_1811 = 8;
        this.field_1812 = renderDist / 16 + 1;
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

        for (class_66 viz : this.field_1807) {
            if (viz != null) {
                viz.field_249 = false;
            }
        }

        this.field_1807.clear();
        this.blockEntities.clear();

        for (int cX = 0; cX < this.field_1810; ++cX) {
            for (int cY = 0; cY < this.field_1811; ++cY) {
                for (int cZ = 0; cZ < this.field_1812; ++cZ) {
                    int vizIndex = (cZ * this.field_1811 + cY) * this.field_1810 + cX;
                    class_66 viz = new class_66(this.world, this.blockEntities, cX * 16, cY * 16, cZ * 16, 16, this.field_1813 + var2);
                    this.field_1808[vizIndex] = viz;

                    if (this.field_1817) {
                        viz.field_254 = this.field_1816.get(var3);
                    }

                    viz.field_253 = false;
                    viz.field_252 = true;
                    viz.field_243 = false;
                    viz.field_251 = var3++;
                    viz.method_305();
                    this.field_1809[vizIndex] = viz;
                    this.field_1807.add(viz);
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
    public int method_1548(LivingEntity entity, int renderPass, double deltaTime) {
        if (this.field_1807.size() < 10) {
            int vizEnd = 10;
            for (int vizStart = 0; vizStart < vizEnd; ++vizStart) {
                this.field_1792 = (this.field_1792 + 1) % this.field_1809.length;
                class_66 viz = this.field_1809[this.field_1792];
                if (viz.field_249 && !this.field_1807.contains(viz)) {
                    this.field_1807.add(viz);
                }
            }
        }

        if (this.client.options.viewDistance != this.field_1782 && !Config.isLoadChunksFar()) {
            ((ExChunkCache) this.world.worldSource).updateVeryFar();
            this.method_1537();
        }

        if (renderPass == 0) {
            this.field_1787 = 0;
            this.field_1788 = 0;
            this.field_1789 = 0;
            this.field_1790 = 0;
            this.field_1791 = 0;
        }

        double peX = entity.prevRenderX + (entity.x - entity.prevRenderX) * deltaTime;
        double peY = entity.prevRenderY + (entity.y - entity.prevRenderY) * deltaTime;
        double peZ = entity.prevRenderZ + (entity.z - entity.prevRenderZ) * deltaTime;
        double eVizX = entity.x - this.field_1800;
        double eVizY = entity.y - this.field_1801;
        double eVizZ = entity.z - this.field_1802;
        double eVizSqr = eVizX * eVizX + eVizY * eVizY + eVizZ * eVizZ;
        if (eVizSqr > 64.0D) {
            this.field_1800 = entity.x;
            this.field_1801 = entity.y;
            this.field_1802 = entity.z;
            int preloadCount = Config.getPreloadedChunks() * 64;
            double eprX = entity.x - this.prevReposX;
            double eprY = entity.y - this.prevReposY;
            double eprZ = entity.z - this.prevReposZ;
            double eprSqr = eprX * eprX + eprY * eprY + eprZ * eprZ;
            if (eprSqr > (double) (preloadCount * preloadCount) + 64.0D) {
                this.prevReposX = entity.x;
                this.prevReposY = entity.y;
                this.prevReposZ = entity.z;
                this.method_1553(MathHelper.floor(entity.x), MathHelper.floor(entity.y), MathHelper.floor(entity.z));
            }

            Arrays.sort(this.field_1808, new EntityOppositeComparator(entity));
        }

        if (((ExGameOptions) this.client.options).ofSmoothFps() && renderPass == 0) {
            GL11.glFinish();
        }

        if (((ExGameOptions) this.client.options).ofSmoothInput() && renderPass == 0) {
            Config.sleep(1L);
        }

        if (this.field_1817 && this.client.options.advancedOpengl && !this.client.options.anaglyph3d && renderPass == 0) {
            int vizStart0 = 0;
            int vizEnd0 = 20;
            this.checkOcclusionQueryResult(vizStart0, vizEnd0, entity.x, entity.y, entity.z);

            for (int vizIndex = vizStart0; vizIndex < vizEnd0; ++vizIndex) {
                this.field_1808[vizIndex].field_252 = true;
            }

            int queryCount = 0;
            int chunkCount = this.method_1542(vizStart0, vizEnd0, renderPass, deltaTime);
            int vizEnd = vizEnd0;
            int vizOffset = 0;
            int vizStep = 30;

            int vizLimit = this.field_1810 / 2;
            while (vizEnd < this.field_1808.length) {
                int vizStart = vizEnd;
                if (vizOffset < vizLimit) {
                    ++vizOffset;
                } else {
                    --vizOffset;
                }

                vizEnd += vizOffset * vizStep;
                if (vizEnd <= vizStart) {
                    vizEnd = vizStart + 10;
                }

                if (vizEnd > this.field_1808.length) {
                    vizEnd = this.field_1808.length;
                }

                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glDisable(GL11.GL_FOG);
                GL11.glColorMask(false, false, false, false);
                GL11.glDepthMask(false);
                this.checkOcclusionQueryResult(vizStart, vizEnd, entity.x, entity.y, entity.z);
                GL11.glPushMatrix();
                float xOffset = 0.0F;
                float yOffset = 0.0F;
                float zOffset = 0.0F;

                for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
                    class_66 viz = this.field_1808[vizIndex];
                    if (viz.method_304()) {
                        viz.field_243 = false;
                    } else if (viz.field_243) {
                        if (Config.isOcclusionFancy() && !((ExClass_66) viz).isInFrustrumFully()) {
                            viz.field_252 = true;
                        } else if (viz.field_243 && !viz.field_253) {
                            if (((ExClass_66) viz).isVisibleFromPosition()) {
                                float dX = Math.abs((float) (((ExClass_66) viz).visibleFromX() - entity.x));
                                float dY = Math.abs((float) (((ExClass_66) viz).visibleFromY() - entity.y));
                                float dZ = Math.abs((float) (((ExClass_66) viz).visibleFromZ() - entity.z));
                                float len = dX + dY + dZ;
                                if ((double) len < 10.0D + (double) vizIndex / 1000.0D) {
                                    viz.field_252 = true;
                                    continue;
                                }
                                ((ExClass_66) viz).isVisibleFromPosition(false);
                            }

                            float dX = (float) (viz.field_237 - peX);
                            float dY = (float) (viz.field_238 - peY);
                            float dZ = (float) (viz.field_239 - peZ);
                            float mX = dX - xOffset;
                            float mY = dY - yOffset;
                            float mZ = dZ - zOffset;
                            if (mX != 0.0F || mY != 0.0F || mZ != 0.0F) {
                                GL11.glTranslatef(mX, mY, mZ);
                                xOffset += mX;
                                yOffset += mY;
                                zOffset += mZ;
                            }

                            ARBOcclusionQuery.glBeginQueryARB(GL15.GL_SAMPLES_PASSED, viz.field_254);
                            viz.method_303();
                            ARBOcclusionQuery.glEndQueryARB(GL15.GL_SAMPLES_PASSED);
                            viz.field_253 = true;
                            ++queryCount;
                        }
                    }
                }

                GL11.glPopMatrix();
                GL11.glColorMask(true, true, true, true);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glEnable(GL11.GL_FOG);

                chunkCount += this.method_1542(vizStart, vizEnd, renderPass, deltaTime);
            }
            return chunkCount;
        } else {
            int chunkCount = this.method_1542(0, this.field_1808.length, renderPass, deltaTime);
            return chunkCount;
        }
    }

    private void checkOcclusionQueryResult(int vizStart, int vizEnd, double x, double y, double z) {
        for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
            class_66 viz = this.field_1808[vizIndex];
            if (!viz.field_253) {
                continue;
            }

            this.field_1797.clear();
            ARBOcclusionQuery.glGetQueryObjectuivARB(viz.field_254, GL15.GL_QUERY_RESULT_AVAILABLE, this.field_1797);
            if (this.field_1797.get(0) == 0) {
                continue;
            }
            viz.field_253 = false;

            this.field_1797.clear();
            ARBOcclusionQuery.glGetQueryObjectuivARB(viz.field_254, GL15.GL_QUERY_RESULT, this.field_1797);
            boolean wasVisible = viz.field_252;
            viz.field_252 = this.field_1797.get(0) > 0;
            if (wasVisible && viz.field_252) {
                ((ExClass_66) viz).isVisibleFromPosition(true);
                ((ExClass_66) viz).setVisibleFromX(x);
                ((ExClass_66) viz).setVisibleFromY(y);
                ((ExClass_66) viz).setVisibleFromZ(z);
            }
        }
    }

    @Overwrite
    private int method_1542(int vizStart, int vizEnd, int renderPass, double deltaTime) {
        this.renderListBuffer.clear();

        for (int vizIndex = vizStart; vizIndex < vizEnd; ++vizIndex) {
            class_66 viz = this.field_1808[vizIndex];
            if (renderPass == 0) {
                ++this.field_1787;
                if (viz.field_244[renderPass]) {
                    ++this.field_1791;
                } else if (!viz.field_243) {
                    ++this.field_1788;
                } else if (this.field_1817 && !viz.field_252) {
                    ++this.field_1789;
                } else {
                    ++this.field_1790;
                }
            }

            if (!viz.field_244[renderPass] && viz.field_243 && (!this.field_1817 || viz.field_252)) {
                int renderListId = viz.method_297(renderPass);
                if (renderListId >= 0) {
                    this.renderListBuffer.put(renderListId);
                }
            }
        }

        this.renderListBuffer.flip();
        LivingEntity entity = this.client.viewEntity;
        double eprprX = entity.prevRenderX + (entity.x - entity.prevRenderX) * deltaTime;
        double eprprY = entity.prevRenderY + (entity.y - entity.prevRenderY) * deltaTime;
        double eprprZ = entity.prevRenderZ + (entity.z - entity.prevRenderZ) * deltaTime;
        GL11.glTranslatef((float) (-eprprX), (float) (-eprprY), (float) (-eprprZ));
        GL11.glCallLists(this.renderListBuffer);
        GL11.glTranslatef((float) eprprX, (float) eprprY, (float) eprprZ);
        return this.renderListBuffer.limit();
    }

    @Overwrite
    public void method_1540(int var1, double var2) {
        // Do not draw RenderLists
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

    private boolean isMoving(LivingEntity entity) {
        boolean moving = this.isMovingNow(entity);
        if (moving) {
            this.lastMovedTime = System.currentTimeMillis();
            return true;
        } else {
            return System.currentTimeMillis() - this.lastMovedTime < 2000L;
        }
    }

    private boolean isMovingNow(LivingEntity entity) {
        double threshold = 0.001D;
        if (entity.jumping) return true;
        if (entity.method_1373()) return true;
        if (entity.lastHandSwingProgress > threshold) return true;
        if (this.client.mouseHelper.xDelta != 0) return true;
        if (this.client.mouseHelper.yDelta != 0) return true;
        if (Math.abs(entity.x - entity.prevX) > threshold) return true;
        if (Math.abs(entity.y - entity.prevY) > threshold) return true;
        return Math.abs(entity.z - entity.prevZ) > threshold;
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

    @Override
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

    @Override
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

    @Override
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
