package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.texture.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.ExTessellator;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TexturePackManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.resource.DefaultTexturePack;
import net.minecraft.client.resource.TexturePack;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GLAllocationUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager implements ExTextureManager {

    @Shadow
    public static boolean field_1245;
    @Shadow
    private GameOptions gameOptions;
    @Shadow
    private TexturePackManager texturePackManager;
    @Shadow
    private IntBuffer field_1249;
    @Shadow
    private ByteBuffer currentImageBuffer;
    @Shadow
    private List<TextureBinder> textureBinders;
    @Shadow
    private boolean isClampTexture;
    @Shadow
    private boolean isBlurTexture;

    private BufferedImage missingTexImage;
    private int terrainTextureId = -1;
    private int guiItemsTextureId = -1;
    private boolean hdTexturesInstalled = false;
    private Map<Integer, Dimension> textureDimensionsMap = new HashMap<>();
    private Map<String, byte[]> textureDataMap = new HashMap<>();
    private int tickCounter = 0;
    private ByteBuffer[] mipImageDatas;
    private boolean dynamicTexturesUpdated = false;

    @Shadow
    public abstract int getTextureId(String string);

    @Shadow
    protected abstract BufferedImage readImage(InputStream inputStream);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(TexturePackManager var1, GameOptions var2, CallbackInfo ci) {
        this.allocateImageData(256);
        this.missingTexImage = new BufferedImage(64, 64, 2);
        Graphics var8 = this.missingTexImage.getGraphics();
        var8.setColor(Color.WHITE);
        var8.fillRect(0, 0, 64, 64);
        var8.setColor(Color.BLACK);
        var8.drawString("missingtex", 1, 10);
        var8.dispose();
    }

    @Inject(method = "getTextureId(Ljava/lang/String;)I", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/texture/TextureManager;bindImageToId(Ljava/awt/image/BufferedImage;I)V",
            ordinal = 4,
            shift = At.Shift.BEFORE))
    private void storeTextureId(String var1, CallbackInfoReturnable<Integer> cir) {
        int var4 = this.field_1249.get(0);

        if (var1.equals("/terrain.png")) {
            this.terrainTextureId = var4;
        }

        if (var1.equals("/gui/items.png")) {
            this.guiItemsTextureId = var4;
        }
    }

    @Overwrite
    public void bindImageToId(BufferedImage var1, int var2) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, var2);
        field_1245 = Config.isUseMipmaps();
        int var3;
        int var4;
        if (field_1245 && var2 != this.guiItemsTextureId) {
            var3 = Config.getMipmapType();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, var3);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            if (GLContext.getCapabilities().OpenGL12) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
                var4 = Config.getMipmapLevel();
                if (var4 >= 4) {
                    int var5 = Math.min(var1.getWidth(), var1.getHeight());
                    var4 = this.getMaxMipmapLevel(var5);
                    if (var4 < 0) {
                        var4 = 0;
                    }
                }

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, var4);
            }

            if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                FloatBuffer var18 = BufferUtils.createFloatBuffer(16);
                var18.rewind();
                GL11.glGetFloat('\u84ff', var18);
                float var19 = var18.get(0);
                float var6 = (float) Config.getAnisotropicFilterLevel();
                var6 = Math.min(var6, var19);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, '\u84fe', var6);
            }
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }

        if (this.isBlurTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        if (this.isClampTexture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }

        var3 = var1.getWidth();
        var4 = var1.getHeight();
        this.setTextureDimension(var2, new Dimension(var3, var4));
        int[] var20 = new int[var3 * var4];
        byte[] var21 = new byte[var3 * var4 * 4];
        var1.getRGB(0, 0, var3, var4, var20, 0, var3);
        int var7 = 0;
        int[] var8 = new int[255];

        int var10;
        int var11;
        int var12;
        int var13;
        int var14;
        int var15;
        for (int var9 = 0; var9 < var20.length; ++var9) {
            var10 = var20[var9] >> 24 & 255;
            var11 = var20[var9] >> 16 & 255;
            var12 = var20[var9] >> 8 & 255;
            var13 = var20[var9] & 255;
            if (this.gameOptions != null && this.gameOptions.anaglyph3d) {
                var14 = (var11 * 30 + var12 * 59 + var13 * 11) / 100;
                var15 = (var11 * 30 + var12 * 70) / 100;
                int var16 = (var11 * 30 + var13 * 70) / 100;
                var11 = var14;
                var12 = var15;
                var13 = var16;
            }

            if (var10 == 0) {
                boolean var23 = false;
                if (var2 != this.terrainTextureId && var2 != this.guiItemsTextureId) {
                    if (var7 == 0) {
                        var7 = this.getAverageOpaqueColor(var20);
                    }

                    var14 = var7;
                } else {
                    var14 = -1;
                }

                var11 = var14 >> 16 & 255;
                var12 = var14 >> 8 & 255;
                var13 = var14 & 255;
            }

            var21[var9 * 4 + 0] = (byte) var11;
            var21[var9 * 4 + 1] = (byte) var12;
            var21[var9 * 4 + 2] = (byte) var13;
            var21[var9 * 4 + 3] = (byte) var10;
        }

        this.checkImageDataSize(var3);
        this.currentImageBuffer.clear();
        this.currentImageBuffer.put(var21);
        this.currentImageBuffer.position(0).limit(var21.length);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, var3, var4, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
        if (field_1245) {
            this.generateMipMaps(this.currentImageBuffer, var3, var4);
        }

        if (Config.isMultiTexture() && (var2 == this.terrainTextureId || var2 == this.guiItemsTextureId)) {
            int[] var22 = null;
            if (var2 == this.terrainTextureId) {
                var22 = ExTessellator.terrainTextures;
            }

            if (var2 == this.guiItemsTextureId) {
                var22 = ExTessellator.itemTextures;
            }

            var10 = var3 / 16;
            var11 = var4 / 16;

            for (var12 = 0; var12 < 16; ++var12) {
                for (var13 = 0; var13 < 16; ++var13) {
                    var14 = var13 * var10;
                    var15 = var12 * var11;
                    BufferedImage var24 = var1.getSubimage(var14, var15, var10, var11);
                    int var17 = var12 * 16 + var13;
                    if (var22[var17] == 0) {
                        this.field_1249.clear();
                        GLAllocationUtils.genTextures(this.field_1249);
                        var22[var17] = this.field_1249.get(0);
                    }

                    if (var2 == this.guiItemsTextureId) {
                        this.isClampTexture = true;
                    } else {
                        this.isClampTexture = Config.isTerrainIconClamped(var17);
                    }

                    this.bindImageToId(var24, var22[var17]);
                }
            }

            this.isClampTexture = false;
        }
    }

    private int getAverageOpaqueColor(int[] var1) {
        long var2 = 0L;
        long var4 = 0L;
        long var6 = 0L;
        long var8 = 0L;

        int var11;
        int var12;
        int var13;
        for (int i : var1) {
            var11 = i;
            var12 = var11 >> 24 & 255;
            if (var12 != 0) {
                var13 = var11 >> 16 & 255;
                int var14 = var11 >> 8 & 255;
                int var15 = var11 & 255;
                var2 += var13;
                var4 += var14;
                var6 += var15;
                ++var8;
            }
        }

        if (var8 <= 0L) {
            return -1;
        } else {
            short var16 = 255;
            var11 = (int) (var2 / var8);
            var12 = (int) (var4 / var8);
            var13 = (int) (var6 / var8);
            return var16 << 24 | var11 << 16 | var12 << 8 | var13;
        }
    }

    private void generateMipMaps(ByteBuffer var1, int var2, int var3) {
        ByteBuffer var4 = var1;

        for (int var5 = 1; var5 <= 16; ++var5) {
            int var6 = var2 >> var5 - 1;
            int var7 = var2 >> var5;
            int var8 = var3 >> var5;
            if (var7 <= 0 || var8 <= 0) {
                break;
            }

            ByteBuffer var9 = this.mipImageDatas[var5 - 1];

            for (int var10 = 0; var10 < var7; ++var10) {
                for (int var11 = 0; var11 < var8; ++var11) {
                    int var12 = var4.getInt((var10 * 2 + 0 + (var11 * 2 + 0) * var6) * 4);
                    int var13 = var4.getInt((var10 * 2 + 1 + (var11 * 2 + 0) * var6) * 4);
                    int var14 = var4.getInt((var10 * 2 + 1 + (var11 * 2 + 1) * var6) * 4);
                    int var15 = var4.getInt((var10 * 2 + 0 + (var11 * 2 + 1) * var6) * 4);
                    int var16 = this.weightedAverageColor(var12, var13, var14, var15);
                    var9.putInt((var10 + var11 * var7) * 4, var16);
                }
            }

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, var5, GL11.GL_RGBA, var7, var8, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, var9);
            var4 = var9;
        }
    }

    public void addTextureBinder(TextureBinder var1) {
        for (int var2 = 0; var2 < this.textureBinders.size(); ++var2) {
            TextureBinder var3 = this.textureBinders.get(var2);
            if (var3.renderMode == var1.renderMode && var3.index == var1.index) {
                this.textureBinders.remove(var2);
                --var2;
                Config.dbg("Texture removed: " + var3 + ", image: " + var3.renderMode + ", index: " + var3.index);
            }
        }

        this.textureBinders.add(var1);
        var1.updateTexture();
        Config.dbg("Texture registered: " + var1 + ", image: " + var1.renderMode + ", index: " + var1.index);
        this.dynamicTexturesUpdated = false;
    }

    private void generateMipMapsSub(int var1, int var2, int var3, int var4, ByteBuffer var5, int var6, boolean var7, int var8, int var9) {
        ByteBuffer var10 = var5;

        for (int var11 = 1; var11 <= 16; ++var11) {
            int var12 = var3 >> var11 - 1;
            int var13 = var3 >> var11;
            int var14 = var4 >> var11;
            int var15 = var1 >> var11;
            int var16 = var2 >> var11;
            if (var13 <= 0 || var14 <= 0) {
                break;
            }

            ByteBuffer var17 = this.mipImageDatas[var11 - 1];

            int var18;
            int var19;
            int var20;
            int var21;
            int var22;
            for (var18 = 0; var18 < var13; ++var18) {
                for (var19 = 0; var19 < var14; ++var19) {
                    var20 = var10.getInt((var18 * 2 + 0 + (var19 * 2 + 0) * var12) * 4);
                    var21 = var10.getInt((var18 * 2 + 1 + (var19 * 2 + 0) * var12) * 4);
                    var22 = var10.getInt((var18 * 2 + 1 + (var19 * 2 + 1) * var12) * 4);
                    int var23 = var10.getInt((var18 * 2 + 0 + (var19 * 2 + 1) * var12) * 4);
                    int var24;
                    if (var7) {
                        var24 = this.method_1086(this.method_1086(var20, var21), this.method_1086(var22, var23));
                    } else {
                        var24 = this.weightedAverageColor(var20, var21, var22, var23);
                    }

                    var17.putInt((var18 + var19 * var13) * 4, var24);
                }
            }

            for (var18 = 0; var18 < var6; ++var18) {
                for (var19 = 0; var19 < var6; ++var19) {
                    var20 = var18 * var13;
                    var21 = var19 * var14;
                    if (Config.isMultiTexture() && var8 == this.terrainTextureId) {
                        var22 = var19 * 16 + var18;
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, ExTessellator.terrainTextures[var9 + var22]);
                        var20 = 0;
                        var21 = 0;
                    }

                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, var11, var15 + var20, var16 + var21, var13, var14, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, var17);
                }
            }

            var10 = var17;
        }

    }

    public void tick() {
        this.checkHdTextures();
        ++this.tickCounter;
        this.terrainTextureId = this.getTextureId("/terrain.png");
        this.guiItemsTextureId = this.getTextureId("/gui/items.png");

        int var1;
        TextureBinder var2;
        for (var1 = 0; var1 < this.textureBinders.size(); ++var1) {
            var2 = this.textureBinders.get(var1);
            var2.render3d = this.gameOptions.anaglyph3d;
            if (!var2.getClass().getName().equals("ModTextureStatic") || !this.dynamicTexturesUpdated) {
                boolean var3 = false;
                int var14;
                if (var2.renderMode == 0) {
                    var14 = this.terrainTextureId;
                } else {
                    var14 = this.guiItemsTextureId;
                }

                Dimension var4 = this.getTextureDimensions(var14);
                if (var4 == null) {
                    throw new IllegalArgumentException("Unknown dimensions for texture id: " + var14);
                }

                int var5 = var4.width / 16;
                int var6 = var4.height / 16;
                this.checkImageDataSize(var4.width);
                this.currentImageBuffer.limit(0);
                boolean var7 = this.updateCustomTexture(var2, this.currentImageBuffer, var4.width / 16);
                if (!var7 || this.currentImageBuffer.limit() > 0) {
                    boolean var8;
                    if (this.currentImageBuffer.limit() <= 0) {
                        var8 = this.updateDefaultTexture(var2, this.currentImageBuffer, var4.width / 16);
                        if (var8 && this.currentImageBuffer.limit() <= 0) {
                            continue;
                        }
                    }

                    if (this.currentImageBuffer.limit() <= 0) {
                        var2.updateTexture();
                        if (var2.grid == null) {
                            continue;
                        }

                        int var15 = var5 * var6 * 4;
                        if (var2.grid.length == var15) {
                            this.currentImageBuffer.clear();
                            this.currentImageBuffer.put(var2.grid);
                            this.currentImageBuffer.position(0).limit(var2.grid.length);
                        } else {
                            this.copyScaled(var2.grid, this.currentImageBuffer, var5);
                        }
                    }

                    var2.bindTexture((TextureManager) (Object) this);
                    var8 = this.scalesWithFastColor(var2);

                    int var9;
                    int var10;
                    for (var9 = 0; var9 < var2.textureSize; ++var9) {
                        for (var10 = 0; var10 < var2.textureSize; ++var10) {
                            int var11 = var2.index % 16 * var5 + var9 * var5;
                            int var12 = var2.index / 16 * var6 + var10 * var6;
                            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, var11, var12, var5, var6, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
                            if (field_1245 && var9 == 0 && var10 == 0) {
                                this.generateMipMapsSub(var11, var12, var5, var6, this.currentImageBuffer, var2.textureSize, var8, 0, 0);
                            }
                        }
                    }

                    if (Config.isMultiTexture() && var14 == this.terrainTextureId) {
                        for (var9 = 0; var9 < var2.textureSize; ++var9) {
                            for (var10 = 0; var10 < var2.textureSize; ++var10) {
                                byte var16 = 0;
                                byte var17 = 0;
                                int var13 = var10 * 16 + var9;
                                GL11.glBindTexture(GL11.GL_TEXTURE_2D, ExTessellator.terrainTextures[var2.index + var13]);
                                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, var16, var17, var5, var6, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
                                if (field_1245 && var9 == 0 && var10 == 0) {
                                    this.generateMipMapsSub(var16, var17, var5, var6, this.currentImageBuffer, var2.textureSize, var8, var14, var2.index);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.dynamicTexturesUpdated = true;

        for (var1 = 0; var1 < this.textureBinders.size(); ++var1) {
            var2 = this.textureBinders.get(var1);
            if (var2.id > 0) {
                this.currentImageBuffer.clear();
                this.currentImageBuffer.put(var2.grid);
                this.currentImageBuffer.position(0).limit(var2.grid.length);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, var2.id);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
                if (field_1245) {
                    this.generateMipMapsSub(0, 0, 16, 16, this.currentImageBuffer, var2.textureSize, false, 0, 0);
                }
            }
        }

    }

    @Shadow
    protected abstract int method_1086(int var1, int var2);

    private int weightedAverageColor(int var1, int var2, int var3, int var4) {
        int var5 = this.method_1098(var1, var2);
        int var6 = this.method_1098(var3, var4);
        int var7 = this.method_1098(var5, var6);
        return var7;
    }

    @Overwrite
    private int method_1098(int var1, int var2) {
        int var3 = (var1 & -16777216) >> 24 & 255;
        int var4 = (var2 & -16777216) >> 24 & 255;
        int var5 = (var3 + var4) / 2;
        if (var3 == 0 && var4 == 0) {
            var3 = 1;
            var4 = 1;
        } else {
            if (var3 == 0) {
                var1 = var2;
                var5 /= 2;
            }

            if (var4 == 0) {
                var2 = var1;
                var5 /= 2;
            }
        }

        int var6 = (var1 >> 16 & 255) * var3;
        int var7 = (var1 >> 8 & 255) * var3;
        int var8 = (var1 & 255) * var3;
        int var9 = (var2 >> 16 & 255) * var4;
        int var10 = (var2 >> 8 & 255) * var4;
        int var11 = (var2 & 255) * var4;
        int var12 = (var6 + var9) / (var3 + var4);
        int var13 = (var7 + var10) / (var3 + var4);
        int var14 = (var8 + var11) / (var3 + var4);
        return var5 << 24 | var12 << 16 | var13 << 8 | var14;
    }

    @Inject(method = "reloadTexturesFromTexturePack", at = @At("HEAD"))
    public void reloadTexturesFromTexturePack(CallbackInfo ci) {
        this.textureDataMap.clear();
        this.dynamicTexturesUpdated = false;
        Config.setFontRendererUpdated(false);
    }

    private void setTextureDimension(int var1, Dimension var2) {
        this.textureDimensionsMap.put(var1, var2);
        if (var1 == this.terrainTextureId) {
            Config.setIconWidthTerrain(var2.width / 16);
            this.updateDynamicTextures(0, var2);
        }

        if (var1 == this.guiItemsTextureId) {
            Config.setIconWidthItems(var2.width / 16);
            this.updateDynamicTextures(1, var2);
        }
    }

    private Dimension getTextureDimensions(int var1) {
        return this.textureDimensionsMap.get(var1);
    }

    private void updateDynamicTextures(int var1, Dimension var2) {
        this.checkHdTextures();

        for (TextureBinder textureBinder : this.textureBinders) {
            if (textureBinder.renderMode == var1 && textureBinder instanceof TextureHDFX) {
                TextureHDFX hdfx = (TextureHDFX) textureBinder;
                hdfx.setTexturePackBase(this.texturePackManager.texturePack);
                hdfx.setTileWidth(var2.width / 16);
                textureBinder.updateTexture();
            }
        }
    }

    public boolean updateCustomTexture(TextureBinder var1, ByteBuffer var2, int var3) {
        if (var1.index == Block.STILL_WATER.texture) {
            if (Config.isGeneratedWater()) {
                return false;
            }
            return this.updateCustomTexture(var1, "/custom_water_still.png", var2, var3, Config.isAnimatedWater(), 1);
        }
        if (var1.index == Block.STILL_WATER.texture + 1) {
            if (Config.isGeneratedWater()) {
                return false;
            }
            return this.updateCustomTexture(var1, "/custom_water_flowing.png", var2, var3, Config.isAnimatedWater(), 1);
        }
        if (var1.index == Block.STILL_LAVA.texture) {
            if (Config.isGeneratedLava()) {
                return false;
            }
            return this.updateCustomTexture(var1, "/custom_lava_still.png", var2, var3, Config.isAnimatedLava(), 1);
        }
        if (var1.index == Block.STILL_LAVA.texture + 1) {
            if (Config.isGeneratedLava()) {
                return false;
            }
            return this.updateCustomTexture(var1, "/custom_lava_flowing.png", var2, var3, Config.isAnimatedLava(), 1);
        }
        if (var1.index == Block.PORTAL.texture)
            return this.updateCustomTexture(var1, "/custom_portal.png", var2, var3, Config.isAnimatedPortal(), 1);
        if (var1.index == Block.FIRE.texture)
            return this.updateCustomTexture(var1, "/custom_fire_n_s.png", var2, var3, Config.isAnimatedFire(), 1);
        if (var1.index == Block.FIRE.texture + 16)
            return this.updateCustomTexture(var1, "/custom_fire_e_w.png", var2, var3, Config.isAnimatedFire(), 1);
        return false;
    }

    private boolean updateDefaultTexture(TextureBinder var1, ByteBuffer var2, int var3) {
        if (this.texturePackManager.texturePack instanceof DefaultTexturePack) {
            return false;
        }
        if (var1.index == Block.STILL_WATER.texture) {
            if (Config.isGeneratedWater()) {
                return false;
            }
            return this.updateDefaultTexture(var1, var2, var3, false, 1);
        }
        if (var1.index == Block.STILL_WATER.texture + 1) {
            if (Config.isGeneratedWater()) {
                return false;
            }
            return this.updateDefaultTexture(var1, var2, var3, Config.isAnimatedWater(), 1);
        }
        if (var1.index == Block.STILL_LAVA.texture) {
            if (Config.isGeneratedLava()) {
                return false;
            }
            return this.updateDefaultTexture(var1, var2, var3, false, 1);
        }
        if (var1.index == Block.STILL_LAVA.texture + 1) {
            if (Config.isGeneratedLava()) {
                return false;
            }
            return this.updateDefaultTexture(var1, var2, var3, Config.isAnimatedLava(), 3);
        }
        return false;
    }

    private boolean updateDefaultTexture(TextureBinder var1, ByteBuffer var2, int var3, boolean var4, int var5) {
        int var6 = var1.index;
        if (!var4 && this.dynamicTexturesUpdated) {
            return true;
        } else {
            byte[] var7 = this.getTerrainIconData(var6, var3);
            if (var7 == null) {
                return false;
            } else {
                var2.clear();
                int var8 = var7.length;
                if (var4) {
                    int var9 = var3 - this.tickCounter / var5 % var3;
                    int var10 = var9 * var3 * 4;
                    var2.put(var7, var10, var8 - var10);
                    var2.put(var7, 0, var10);
                } else {
                    var2.put(var7, 0, var8);
                }

                var2.position(0).limit(var8);
                return true;
            }
        }
    }

    private boolean updateCustomTexture(TextureBinder var1, String var2, ByteBuffer var3, int var4, boolean var5, int var6) {
        byte[] var7 = this.getCustomTextureData(var2, var4);
        if (var7 == null) {
            return false;
        } else if (!var5 && this.dynamicTexturesUpdated) {
            return true;
        } else {
            int var8 = var4 * var4 * 4;
            int var9 = var7.length / var8;
            int var10 = this.tickCounter / var6 % var9;
            int var11 = 0;
            if (var5) {
                var11 = var8 * var10;
            }

            var3.clear();
            var3.put(var7, var11, var8);
            var3.position(0).limit(var8);
            return true;
        }
    }

    private byte[] getTerrainIconData(int var1, int var2) {
        String var3 = "Tile-" + var1;
        byte[] var4 = this.getCustomTextureData(var3, var2);
        if (var4 != null) {
            return var4;
        } else {
            byte[] var5 = this.getCustomTextureData("/terrain.png", var2 * 16);
            if (var5 == null) {
                return null;
            } else {
                var4 = new byte[var2 * var2 * 4];
                int var6 = var1 % 16;
                int var7 = var1 / 16;
                int var8 = var6 * var2;
                int var9 = var7 * var2;
                int var10000 = var8 + var2;
                var10000 = var9 + var2;

                for (int var12 = 0; var12 < var2; ++var12) {
                    int var13 = var9 + var12;

                    for (int var14 = 0; var14 < var2; ++var14) {
                        int var15 = var8 + var14;
                        int var16 = 4 * (var15 + var13 * var2 * 16);
                        int var17 = 4 * (var14 + var12 * var2);
                        var4[var17 + 0] = var5[var16 + 0];
                        var4[var17 + 1] = var5[var16 + 1];
                        var4[var17 + 2] = var5[var16 + 2];
                        var4[var17 + 3] = var5[var16 + 3];
                    }
                }

                this.setCustomTextureData(var3, var4);
                return var4;
            }
        }
    }

    public byte[] getCustomTextureData(String var1, int var2) {
        byte[] var3 = this.textureDataMap.get(var1);
        if (var3 == null) {
            if (this.textureDataMap.containsKey(var1)) {
                return null;
            }

            var3 = this.loadImage(var1, var2);
            this.textureDataMap.put(var1, var3);
        }

        return var3;
    }

    private void setCustomTextureData(String var1, byte[] var2) {
        this.textureDataMap.put(var1, var2);
    }

    private byte[] loadImage(String var1, int var2) {
        try {
            TexturePack var3 = this.texturePackManager.texturePack;
            if (var3 == null) {
                return null;
            } else {
                InputStream var4 = var3.getResourceAsStream(var1);
                if (var4 == null) {
                    return null;
                } else {
                    BufferedImage var5 = this.readImage(var4);
                    if (var5 == null) {
                        return null;
                    } else {
                        if (var2 > 0 && var5.getWidth() != var2) {
                            double var6 = var5.getHeight() / var5.getWidth();
                            int var8 = (int) ((double) var2 * var6);
                            var5 = ExTextureManager.scaleBufferedImage(var5, var2, var8);
                        }

                        int var19 = var5.getWidth();
                        int var7 = var5.getHeight();
                        int[] var20 = new int[var19 * var7];
                        byte[] var9 = new byte[var19 * var7 * 4];
                        var5.getRGB(0, 0, var19, var7, var20, 0, var19);

                        for (int var10 = 0; var10 < var20.length; ++var10) {
                            int var11 = var20[var10] >> 24 & 255;
                            int var12 = var20[var10] >> 16 & 255;
                            int var13 = var20[var10] >> 8 & 255;
                            int var14 = var20[var10] & 255;
                            if (this.gameOptions != null && this.gameOptions.anaglyph3d) {
                                int var15 = (var12 * 30 + var13 * 59 + var14 * 11) / 100;
                                int var16 = (var12 * 30 + var13 * 70) / 100;
                                int var17 = (var12 * 30 + var14 * 70) / 100;
                                var12 = var15;
                                var13 = var16;
                                var14 = var17;
                            }

                            var9[var10 * 4 + 0] = (byte) var12;
                            var9[var10 * 4 + 1] = (byte) var13;
                            var9[var10 * 4 + 2] = (byte) var14;
                            var9[var10 * 4 + 3] = (byte) var11;
                        }

                        return var9;
                    }
                }
            }
        } catch (Exception var18) {
            var18.printStackTrace();
            return null;
        }
    }

    private void checkImageDataSize(int var1) {
        if (this.currentImageBuffer != null) {
            int var2 = var1 * var1 * 4;
            if (this.currentImageBuffer.capacity() >= var2) {
                return;
            }
        }

        this.allocateImageData(var1);
    }

    private void allocateImageData(int var1) {
        int var2 = var1 * var1 * 4;
        this.currentImageBuffer = GLAllocationUtils.allocateByteBuffer(var2);
        ArrayList<ByteBuffer> var3 = new ArrayList<>();

        for (int var4 = var1 / 2; var4 > 0; var4 /= 2) {
            int var5 = var4 * var4 * 4;
            ByteBuffer var6 = GLAllocationUtils.allocateByteBuffer(var5);
            var3.add(var6);
        }

        this.mipImageDatas = var3.toArray(new ByteBuffer[var3.size()]);
    }

    public void checkHdTextures() {
        if (!this.hdTexturesInstalled) {
            Minecraft var1 = Config.getMinecraft();
            if (var1 != null) {
                this.addTextureBinder(new TextureHDLavaFX());
                this.addTextureBinder(new TextureHDWaterFX());
                this.addTextureBinder(new TextureHDPortalFX());
                this.addTextureBinder(new TextureHDCompassFX(var1));
                this.addTextureBinder(new TextureHDWatchFX(var1));
                this.addTextureBinder(new TextureHDWaterFlowFX());
                this.addTextureBinder(new TextureHDLavaFlowFX());
                this.addTextureBinder(new TextureHDFlamesFX(0));
                this.addTextureBinder(new TextureHDFlamesFX(1));
                this.hdTexturesInstalled = true;
            }
        }
    }

    private int getMaxMipmapLevel(int var1) {
        int var2;
        for (var2 = 0; var1 > 0; ++var2) {
            var1 /= 2;
        }

        return var2 - 1;
    }

    private void copyScaled(byte[] var1, ByteBuffer var2, int var3) {
        int var4 = (int) Math.sqrt(var1.length / 4);
        int var5 = var3 / var4;
        byte[] var6 = new byte[4];
        int var7 = var3 * var3;
        var2.clear();
        if (var5 > 1) {
            for (int var8 = 0; var8 < var4; ++var8) {
                int var9 = var8 * var4;
                int var10 = var8 * var5;
                int var11 = var10 * var3;

                for (int var12 = 0; var12 < var4; ++var12) {
                    int var13 = (var12 + var9) * 4;
                    var6[0] = var1[var13];
                    var6[1] = var1[var13 + 1];
                    var6[2] = var1[var13 + 2];
                    var6[3] = var1[var13 + 3];
                    int var14 = var12 * var5;
                    int var15 = var14 + var11;

                    for (int var16 = 0; var16 < var5; ++var16) {
                        int var17 = var15 + var16 * var3;
                        var2.position(var17 * 4);

                        for (int var18 = 0; var18 < var5; ++var18) {
                            var2.put(var6);
                        }
                    }
                }
            }
        }

        var2.position(0).limit(var3 * var3 * 4);
    }

    private boolean scalesWithFastColor(TextureBinder var1) {
        return !var1.getClass().getName().equals("ModTextureStatic");
    }
}
