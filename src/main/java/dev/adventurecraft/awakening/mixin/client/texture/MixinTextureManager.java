package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TexturePackManager;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.TextureBinder;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager implements ExTextureManager {

    private static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
    private static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

    @Shadow
    public static boolean field_1245;

    @Shadow
    public HashMap<String, Integer> textures;

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

    @Shadow
    private BufferedImage missingTexImage;

    private Int2ObjectOpenHashMap<Vec2> textureDimensionsMap = new Int2ObjectOpenHashMap<>();
    private Map<String, byte[]> textureDataMap = new HashMap<>();
    private ByteBuffer[] mipImageDatas;

    private ArrayList<String> replacedTextures = new ArrayList<>();
    private HashMap<String, AC_TextureAnimated> textureAnimations = new HashMap<>();

    @Shadow
    protected abstract BufferedImage readImage(InputStream inputStream) throws IOException;

    @Shadow
    protected abstract int method_1086(int var1, int var2);

    @Shadow
    protected abstract BufferedImage method_1101(BufferedImage bufferedImage);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(TexturePackManager var1, GameOptions var2, CallbackInfo ci) {
        this.allocateImageData(256, 256);
    }

    @Overwrite
    public void bindImageToId(BufferedImage image, int texId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        field_1245 = Config.isUseMipmaps();
        if (field_1245 /* TODO && texId != this.guiItemsTextureId*/) {
            int mipType = Config.getMipmapType();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipType);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            if (GLContext.getCapabilities().OpenGL12) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
                int mipLevel = Config.getMipmapLevel();
                if (mipLevel >= 4) {
                    int minDim = Math.min(image.getWidth(), image.getHeight());
                    mipLevel = this.getMaxMipmapLevel(minDim);
                    if (mipLevel < 0) {
                        mipLevel = 0;
                    }
                }

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, mipLevel);
            }

            if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
                FloatBuffer var18 = BufferUtils.createFloatBuffer(16);
                var18.rewind();
                GL11.glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, var18);
                float anisoLimit = var18.get(0);
                float anisoLevel = Math.min(Config.getAnisotropicFilterLevel(), anisoLimit);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoLevel);
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

        int texW = image.getWidth();
        int texH = image.getHeight();
        this.setTextureDimension(texId, new Vec2(texW, texH));

        int[] srcColors = new int[texW * texH];
        byte[] dstColors = new byte[texW * texH * 4];
        image.getRGB(0, 0, texW, texH, srcColors, 0, texW);

        boolean hasAvgColor = false;
        int avgColor = 0;

        for (int i = 0; i < srcColors.length; ++i) {
            int color;
            int a = srcColors[i] >> 24 & 255;
            int r = srcColors[i] >> 16 & 255;
            int g = srcColors[i] >> 8 & 255;
            int b = srcColors[i] & 255;

            if (this.gameOptions != null && this.gameOptions.anaglyph3d) {
                int r3D = (r * 30 + g * 59 + b * 11) / 100;
                int g3D = (r * 30 + g * 70) / 100;
                int b3D = (r * 30 + b * 70) / 100;
                r = r3D;
                g = g3D;
                b = b3D;
            }

            if (a == 0) {
                /* TODO
                if (texId == this.terrainTextureId || texId == this.guiItemsTextureId) {
                    color = -1;
                } else*/
                {
                    if (!hasAvgColor) {
                        avgColor = this.getAverageOpaqueColor(srcColors);
                        hasAvgColor = true;
                    }
                    color = avgColor;
                }

                r = color >> 16 & 255;
                g = color >> 8 & 255;
                b = color & 255;
            }

            dstColors[i * 4 + 0] = (byte) r;
            dstColors[i * 4 + 1] = (byte) g;
            dstColors[i * 4 + 2] = (byte) b;
            dstColors[i * 4 + 3] = (byte) a;
        }

        this.checkImageDataSize(texW, texH);
        this.currentImageBuffer.clear();
        this.currentImageBuffer.put(dstColors);
        this.currentImageBuffer.position(0).limit(dstColors.length);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texW, texH, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
        if (field_1245) {
            this.generateMipMaps(this.currentImageBuffer, texW, texH);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends TextureBinder> T getTextureBinder(Class<T> type) {
        for (TextureBinder binder : this.textureBinders) {
            if (type.isInstance(binder)) {
                return (T) binder;
            }
        }
        return null;
    }

    @Override
    public BufferedImage getTextureImage(String name) throws IOException {
        InputStream stream = this.texturePackManager.texturePack.getResourceAsStream(name);
        if (stream == null) {
            throw new FileNotFoundException(name);
        }
        return this.readImage(stream);
    }

    @Overwrite
    public int getTextureId(String var1) {
        Integer var3 = this.textures.get(var1);
        if (var3 != null) {
            return var3;
        }

        this.field_1249.clear();
        GLAllocationUtils.genTextures(this.field_1249);
        int var4 = this.field_1249.get(0);
        this.loadTexture(var4, var1);
        this.textures.put(var1, var4);
        return var4;
    }

    @Override
    public void loadTexture(int id, String name) {
        String originalName = name;

        try {
            TexturePack var4 = this.texturePackManager.texturePack;
            if (name.startsWith("##")) {
                name = name.substring(2);
            } else if (name.startsWith("%clamp%")) {
                this.isClampTexture = true;
                name = name.substring(7);
            } else if (name.startsWith("%blur%")) {
                this.isBlurTexture = true;
                name = name.substring(6);
            }

            BufferedImage var5 = null;
            if (Minecraft.instance.world != null) {
                var5 = ((ExWorld) Minecraft.instance.world).loadMapTexture(name);
            }

            if (var5 == null) {
                InputStream var6 = var4.getResourceAsStream(name);
                if (var6 == null) {
                    File var7 = new File(name);
                    if (var7.exists()) {
                        var5 = ImageIO.read(var7);
                    }

                    if (var5 == null) {
                        var5 = this.missingTexImage;
                    }
                } else {
                    var5 = this.readImage(var6);
                }
            }

            if (originalName.startsWith("##")) {
                var5 = this.method_1101(var5);
            }

            this.bindImageToId(var5, id);
            if (originalName.startsWith("%clamp%")) {
                this.isClampTexture = false;
            } else if (originalName.startsWith("%blur%")) {
                this.isBlurTexture = false;
            }

        } catch (IOException var8) {
            var8.printStackTrace();
            this.bindImageToId(this.missingTexImage, id);
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

    private void generateMipMaps(ByteBuffer image, int width, int height) {
        ByteBuffer srcImage = image;

        for (int level = 1; level <= 16; ++level) {
            int prevWidth = width >> (level - 1);
            int levelWidth = width >> level;
            int levelHeight = height >> level;
            if (levelWidth <= 0 || levelHeight <= 0) {
                break;
            }

            ByteBuffer dstImage = this.mipImageDatas[level - 1];

            for (int y = 0; y < levelHeight; ++y) {
                for (int x = 0; x < levelWidth; ++x) {
                    int topLeft = srcImage.getInt((x * 2 + 0 + (y * 2 + 0) * prevWidth) * 4);
                    int topRight = srcImage.getInt((x * 2 + 1 + (y * 2 + 0) * prevWidth) * 4);
                    int botLeft = srcImage.getInt((x * 2 + 0 + (y * 2 + 1) * prevWidth) * 4);
                    int botRight = srcImage.getInt((x * 2 + 1 + (y * 2 + 1) * prevWidth) * 4);

                    int color = this.weightedAverageColor(topLeft, topRight, botRight, botLeft);
                    dstImage.putInt((x + y * levelWidth) * 4, color);
                }
            }

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, level, GL11.GL_RGBA, levelWidth, levelHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dstImage);
            srcImage = dstImage;
        }
    }

    @Overwrite
    public void addTextureBinder(TextureBinder var1) {
        for (int var2 = 0; var2 < this.textureBinders.size(); ++var2) {
            TextureBinder var3 = this.textureBinders.get(var2);
            if (var3.renderMode == var1.renderMode && var3.index == var1.index) {
                this.textureBinders.remove(var2);
                --var2;
                ACMod.LOGGER.info("Texture removed: " + var3 + ", image: " + var3.renderMode + ", index: " + var3.index);
            }
        }

        String tex = ((AC_TextureBinder) var1).getTexture();
        Vec2 var2 = this.getTextureResolution(tex);
        if (var2 == null) {
            this.getTextureId(tex);
            var2 = this.getTextureResolution(tex);
        }

        this.textureBinders.add(var1);
        ((AC_TextureBinder) var1).onTick(var2);
        ACMod.LOGGER.info("Texture registered: " + var1 + ", image: " + var1.renderMode + ", index: " + var1.index);
    }

    private void generateMipMapsSub(int subX, int subY, int tileW, int tileH, ByteBuffer image, int texSize, boolean fastColor) {
        ByteBuffer srcImage = image;

        for (int level = 1; level <= 16; ++level) {
            int prevWidth = tileW >> level - 1;
            int levelWidth = tileW >> level;
            int levelHeight = tileH >> level;
            int levelX = subX >> level;
            int levelY = subY >> level;
            if (levelWidth <= 0 || levelHeight <= 0) {
                break;
            }

            ByteBuffer dstImage = this.mipImageDatas[level - 1];

            for (int y = 0; y < levelHeight; ++y) {
                for (int x = 0; x < levelWidth; ++x) {
                    int topLeft = srcImage.getInt((x * 2 + 0 + (y * 2 + 0) * prevWidth) * 4);
                    int topRight = srcImage.getInt((x * 2 + 1 + (y * 2 + 0) * prevWidth) * 4);
                    int botLeft = srcImage.getInt((x * 2 + 0 + (y * 2 + 1) * prevWidth) * 4);
                    int botRight = srcImage.getInt((x * 2 + 1 + (y * 2 + 1) * prevWidth) * 4);

                    int color;
                    if (fastColor) {
                        color = this.method_1086(this.method_1086(topLeft, topRight), this.method_1086(botRight, botLeft));
                    } else {
                        color = this.weightedAverageColor(topLeft, topRight, botRight, botLeft);
                    }
                    dstImage.putInt((x + y * levelWidth) * 4, color);
                }
            }

            for (int tileY = 0; tileY < texSize; ++tileY) {
                int yOffset = levelY + tileY * levelHeight;

                for (int tileX = 0; tileX < texSize; ++tileX) {
                    int xOffset = levelX + tileX * levelWidth;

                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, level, xOffset, yOffset, levelWidth, levelHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dstImage);
                }
            }

            srcImage = dstImage;
        }
    }

    @Overwrite
    public void tick() {
        // TODO:
        //this.terrainTextureId = this.getTextureId("/terrain.png");
        //this.guiItemsTextureId = this.getTextureId("/gui/items.png");

        for (TextureBinder binder : this.textureBinders) {
            binder.render3d = this.gameOptions.anaglyph3d;

            int texId = this.getTextureId(((AC_TextureBinder) binder).getTexture());
            Vec2 texSize = this.getTextureDimensions(texId);
            if (texSize == null) {
                throw new IllegalArgumentException("Unknown dimensions for texture id: " + texId);
            }

            int tileW = texSize.x / 16;
            int tileH = texSize.y / 16;
            this.checkImageDataSize(texSize.x, texSize.y);
            this.currentImageBuffer.limit(0);

            if (this.currentImageBuffer.limit() <= 0) {
                ((AC_TextureBinder) binder).onTick(texSize);
                if (binder.grid == null) {
                    continue;
                }

                int gridIndex = tileW * tileH * 4;
                if (binder.grid.length == gridIndex) {
                    this.currentImageBuffer.clear();
                    this.currentImageBuffer.put(binder.grid);
                    this.currentImageBuffer.position(0).limit(binder.grid.length);
                } else {
                    this.copyScaled(binder.grid, this.currentImageBuffer, tileW);
                }
            }

            binder.bindTexture((TextureManager) (Object) this);
            boolean fastColor = this.scalesWithFastColor(binder);

            for (int tileY = 0; tileY < binder.textureSize; ++tileY) {
                for (int tileX = 0; tileX < binder.textureSize; ++tileX) {
                    int subX = binder.index % 16 * tileW + tileX * tileW;
                    int subY = binder.index / 16 * tileH + tileY * tileH;
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, subX, subY, tileW, tileH, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);

                    if (field_1245 && tileX == 0 && tileY == 0) {
                        this.generateMipMapsSub(subX, subY, tileW, tileH, this.currentImageBuffer, binder.textureSize, fastColor);
                    }
                }
            }
        }

        for (TextureBinder binder : this.textureBinders) {
            if (binder.id <= 0) {
                continue;
            }

            this.currentImageBuffer.clear();
            this.currentImageBuffer.put(binder.grid);
            this.currentImageBuffer.position(0).limit(binder.grid.length);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, binder.id);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
            if (field_1245) {
                this.generateMipMapsSub(0, 0, 16, 16, this.currentImageBuffer, binder.textureSize, false);
            }
        }

        this.updateTextureAnimations();
    }

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
    private void clearOnReload(CallbackInfo ci) {
        this.textureDataMap.clear();
        Config.setFontRendererUpdated(false);
    }

    @Redirect(
        method = "reloadTexturesFromTexturePack",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/texture/TextureManager;textures:Ljava/util/HashMap;"))
    private HashMap<String, Integer> useEmptySetForTextureReload(TextureManager instance) {
        return new HashMap<>();
    }

    @Inject(
        method = "reloadTexturesFromTexturePack",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/texture/TextureManager;textures:Ljava/util/HashMap;",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void useCustomTextureReload(CallbackInfo ci) {
        for (String key : this.textures.keySet()) {
            int id = this.textures.get(key);
            this.loadTexture(id, key);
        }
    }

    private void setTextureDimension(int var1, Vec2 var2) {
        this.textureDimensionsMap.put(var1, var2);
    }

    private Vec2 getTextureDimensions(int var1) {
        return this.textureDimensionsMap.get(var1);
    }

    private void checkImageDataSize(int width, int height) {
        if (this.currentImageBuffer != null) {
            int size = width * height * 4;
            if (this.currentImageBuffer.capacity() >= size) {
                return;
            }
        }

        this.allocateImageData(width, height);
    }

    private void allocateImageData(int width, int height) {
        int size = width * height * 4;
        this.currentImageBuffer = GLAllocationUtils.allocateByteBuffer(size);
        ArrayList<ByteBuffer> mipBuffers = new ArrayList<>();

        for (int level = Math.max(width, height) / 2; level > 0; level /= 2) {
            int mipSize = level * level * 4;
            ByteBuffer mipBuffer = GLAllocationUtils.allocateByteBuffer(mipSize);
            mipBuffers.add(mipBuffer);
        }

        this.mipImageDatas = mipBuffers.toArray(new ByteBuffer[0]);
    }

    private int getMaxMipmapLevel(int dim) {
        int size;
        for (size = 0; dim > 0; ++size) {
            dim /= 2;
        }

        return size - 1;
    }

    private void copyScaled(byte[] var1, ByteBuffer var2, int var3) {
        int var4 = (int) Math.sqrt(var1.length / 4);
        int var5 = var3 / var4;
        byte[] var6 = new byte[4];
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

    private boolean scalesWithFastColor(TextureBinder binder) {
        return !binder.getClass().getName().equals("ModTextureStatic");
    }

    @Override
    public Vec2 getTextureResolution(String name) {
        Integer var2 = this.textures.get(name);
        return var2 != null ? this.getTextureDimensions(var2) : null;
    }

    @Override
    public void clearTextureAnimations() {
        this.textureAnimations.clear();
    }

    @Override
    public void registerTextureAnimation(String animationName, AC_TextureAnimated animation) {
        this.textureAnimations.put(animationName, animation);
    }

    @Override
    public void unregisterTextureAnimation(String animationName) {
        this.textureAnimations.remove(animationName);
    }

    public void updateTextureAnimations() {
        for (AC_TextureAnimated tex : this.textureAnimations.values()) {
            tex.onTick();
            this.currentImageBuffer.clear();
            this.currentImageBuffer.put(tex.imageData);
            this.currentImageBuffer.position(0).limit(tex.imageData.length);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getTextureId(tex.getTexture()));
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, tex.x, tex.y, tex.width, tex.height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.currentImageBuffer);
        }
    }

    @Override
    public void replaceTexture(String keyName, String replacementName) {
        int id = this.getTextureId(keyName);
        this.loadTexture(id, replacementName);
        if (!this.replacedTextures.contains(keyName)) {
            this.replacedTextures.add(keyName);
        }
    }

    @Override
    public void revertTextures() {
        for (String key : this.replacedTextures) {
            Integer id = this.textures.get(key);
            if (id != null) {
                this.loadTexture(id, key);
            }
        }
    }
}
