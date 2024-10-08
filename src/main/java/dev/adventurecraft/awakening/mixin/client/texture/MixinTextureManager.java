package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.client.skins.TexturePack;
import net.minecraft.client.skins.TexturePackRepository;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
import java.util.stream.Stream;

// TODO: improve texture management and lookups.
//       do to not use strings and IDs at random

@Mixin(Textures.class)
public abstract class MixinTextureManager implements ExTextureManager {

    private static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
    private static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

    @Shadow
    public static boolean MIPMAP;

    @Shadow
    public HashMap<String, Integer> idMap;

    @Shadow
    private Options options;

    @Shadow
    public TexturePackRepository skins;

    @Shadow
    private IntBuffer ib;

    @Shadow
    private ByteBuffer pixels;

    @Shadow
    private List<DynamicTexture> dynamicTextures;

    @Shadow
    private boolean clamp;

    @Shadow
    private boolean blur;

    @Shadow
    private BufferedImage missingTex;

    private Int2ObjectOpenHashMap<Vec2> textureDimensionsMap = new Int2ObjectOpenHashMap<>();
    private ByteBuffer[] mipImageDatas;

    private ArrayList<String> replacedTextures = new ArrayList<>();
    private HashMap<String, AC_TextureAnimated> textureAnimations = new HashMap<>();

    @Shadow
    protected abstract BufferedImage readImage(InputStream inputStream) throws IOException;

    @Shadow
    protected abstract int smoothBlend(int var1, int var2);

    @Shadow
    protected abstract BufferedImage makeStrip(BufferedImage bufferedImage);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(TexturePackRepository var1, Options var2, CallbackInfo ci) {
        this.allocateImageData(256, 256);
    }

    @Overwrite
    public void loadTexture(BufferedImage image, int texId) {
        var options = (ExGameOptions) this.options;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        int mipLevel = options.ofMipmapLevel();
        MIPMAP = mipLevel > 0;
        if (MIPMAP /* TODO && texId != this.guiItemsTextureId*/) {
            int mipType = options.getMipmapType();
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipType);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            if (GLContext.getCapabilities().OpenGL12) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
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
                float anisoLevel = Math.min(((ExGameOptions) this.options).ofAfLevel(), anisoLimit);
                GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoLevel);
            }
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }

        if (this.blur) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        }

        if (this.clamp) {
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

            if (this.options != null && this.options.anaglyph3d) {
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
        this.pixels.clear();
        this.pixels.put(dstColors);
        this.pixels.position(0).limit(dstColors.length);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texW, texH, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.pixels);
        if (MIPMAP) {
            this.generateMipMaps(this.pixels, texW, texH);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DynamicTexture> Stream<T> getTextureBinders(Class<T> type) {
        return this.dynamicTextures.stream()
            .filter(type::isInstance)
            .map(binder -> (T) binder);
    }

    @Override
    public BufferedImage getTextureImage(String name) throws IOException {
        InputStream stream = this.skins.selected.getResource(name);
        if (stream == null) {
            throw new FileNotFoundException(name);
        }
        return this.readImage(stream);
    }

    @Overwrite
    public int loadTexture(String var1) {
        Integer id = this.idMap.get(var1);
        if (id != null) {
            return id;
        }

        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int var4 = this.ib.get(0);
        this.loadTexture(var4, var1);
        this.idMap.put(var1, var4);
        return var4;
    }

    @Override
    public void loadTexture(int id, String name) {
        String originalName = name;

        try {
            TexturePack texPack = this.skins.selected;
            if (name.startsWith("##")) {
                name = name.substring(2);
            } else if (name.startsWith("%clamp%")) {
                this.clamp = true;
                name = name.substring(7);
            } else if (name.startsWith("%blur%")) {
                this.blur = true;
                name = name.substring(6);
            }

            BufferedImage image = null;
            if (Minecraft.instance.level != null) {
                image = ((ExWorld) Minecraft.instance.level).loadMapTexture(name);
            }

            if (image == null) {
                InputStream stream = texPack.getResource(name);
                if (stream == null) {
                    File file = new File(name);
                    if (file.exists()) {
                        image = ImageIO.read(file);
                    }
                } else {
                    image = this.readImage(stream);
                }
            }

            if (image == null) {
                image = this.missingTex;
            }

            if (originalName.startsWith("##")) {
                image = this.makeStrip(image);
            }

            this.loadTexture(image, id);

            if (originalName.startsWith("%clamp%")) {
                this.clamp = false;
            } else if (originalName.startsWith("%blur%")) {
                this.blur = false;
            }

        } catch (IOException var8) {
            var8.printStackTrace();
            this.loadTexture(this.missingTex, id);
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
    public void addDynamicTexture(DynamicTexture binder) {
        for (int i = 0; i < this.dynamicTextures.size(); ++i) {
            DynamicTexture b = this.dynamicTextures.get(i);
            if (b.textureId == binder.textureId && b.tex == binder.tex) {
                this.dynamicTextures.remove(i);
                --i;
                ACMod.LOGGER.info("Texture removed: {}, image: {}, index: {}", b, b.textureId, b.tex);
            }
        }

        String tex = ((AC_TextureBinder) binder).getTexture();
        Vec2 res = this.getTextureResolution(tex);
        if (res == null) {
            this.loadTexture(tex);
            res = this.getTextureResolution(tex);
        }

        this.dynamicTextures.add(binder);
        this.expandBinderGrid(res, binder);
        ((AC_TextureBinder) binder).onTick(res);
        ACMod.LOGGER.info("Texture registered: {}, image: {}, index: {}", binder, binder.textureId, binder.tex);
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
                        color = this.smoothBlend(this.smoothBlend(topLeft, topRight), this.smoothBlend(botRight, botLeft));
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

        for (DynamicTexture binder : this.dynamicTextures) {
            binder.anaglyph3d = this.options.anaglyph3d;

            var acBinder = (AC_TextureBinder) binder;
            String texName = acBinder.getTexture();
            int texId = this.loadTexture(texName);
            Vec2 texSize = this.getTextureDimensions(texId);
            if (texSize == null) {
                throw new IllegalArgumentException("Unknown dimensions for texture id/name: " + texId + "/" + texName);
            }

            int tileW = texSize.x / 16;
            int tileH = texSize.y / 16;
            this.expandBinderGrid(texSize, binder);
            acBinder.onTick(texSize);

            this.copyScaled(binder.pixels, this.pixels, tileW);

            binder.bind((Textures) (Object) this);
            boolean fastColor = this.scalesWithFastColor(binder);

            for (int tileY = 0; tileY < binder.replicate; ++tileY) {
                for (int tileX = 0; tileX < binder.replicate; ++tileX) {
                    int subX = binder.tex % 16 * tileW + tileX * tileW;
                    int subY = binder.tex / 16 * tileH + tileY * tileH;
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, subX, subY, tileW, tileH, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.pixels);

                    if (MIPMAP && tileX == 0 && tileY == 0) {
                        this.generateMipMapsSub(subX, subY, tileW, tileH, this.pixels, binder.replicate, fastColor);
                    }
                }
            }
        }

        for (DynamicTexture binder : this.dynamicTextures) {
            if (binder.copyTo <= 0) {
                continue;
            }

            this.pixels.clear();
            this.pixels.put(binder.pixels);
            this.pixels.position(0).limit(binder.pixels.length);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, binder.copyTo);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.pixels);
            if (MIPMAP) {
                this.generateMipMapsSub(0, 0, 16, 16, this.pixels, binder.replicate, false);
            }
        }

        this.updateTextureAnimations();
    }

    private int weightedAverageColor(int var1, int var2, int var3, int var4) {
        int var5 = this.crispBlend(var1, var2);
        int var6 = this.crispBlend(var3, var4);
        int var7 = this.crispBlend(var5, var6);
        return var7;
    }

    @Overwrite
    private int crispBlend(int var1, int var2) {
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

    @Inject(method = "reloadAll", at = @At("HEAD"))
    private void clearOnReload(CallbackInfo ci) {
        Config.setFontRendererUpdated(false);
    }

    @Redirect(
        method = "reloadAll",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Textures;idMap:Ljava/util/HashMap;"))
    private HashMap<String, Integer> useEmptySetForTextureReload(Textures instance) {
        return new HashMap<>();
    }

    @Inject(
        method = "reloadAll",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Textures;idMap:Ljava/util/HashMap;",
            shift = At.Shift.BEFORE,
            ordinal = 0))
    private void useCustomTextureReload(CallbackInfo ci) {
        for (String key : this.idMap.keySet()) {
            int id = this.idMap.get(key);
            this.loadTexture(id, key);
        }
    }

    private void setTextureDimension(int var1, Vec2 var2) {
        this.textureDimensionsMap.put(var1, var2);
    }

    private Vec2 getTextureDimensions(int var1) {
        return this.textureDimensionsMap.get(var1);
    }

    private void expandBinderGrid(Vec2 size, DynamicTexture binder) {
        int tileW = size.x / 16;
        int tileH = size.y / 16;
        this.checkImageDataSize(size.x, size.y);

        int gridIndex = tileW * tileH * 4;
        if (binder.pixels == null || (binder.pixels.length != 0 && binder.pixels.length < gridIndex)) {
            binder.pixels = new byte[gridIndex];
        }
    }

    private void checkImageDataSize(int width, int height) {
        if (this.pixels != null) {
            int size = width * height * 4;
            if (this.pixels.capacity() >= size) {
                return;
            }
        }

        this.allocateImageData(width, height);
    }

    private void allocateImageData(int width, int height) {
        int size = width * height * 4;
        this.pixels = MemoryTracker.createByteBuffer(size);
        ArrayList<ByteBuffer> mipBuffers = new ArrayList<>();

        for (int level = Math.max(width, height) / 2; level > 0; level /= 2) {
            int mipSize = level * level * 4;
            ByteBuffer mipBuffer = MemoryTracker.createByteBuffer(mipSize);
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

    private void copyScaled(byte[] grid, ByteBuffer dstBuffer, int tileW) {
        int frameSize = (int) Math.sqrt(grid.length / 4);
        int ratio = tileW / frameSize;
        dstBuffer.clear();
        if (ratio > 1) {
            byte[] tmp = new byte[4];
            for (int var8 = 0; var8 < frameSize; ++var8) {
                int var9 = var8 * frameSize;
                int var10 = var8 * ratio;
                int var11 = var10 * tileW;

                for (int var12 = 0; var12 < frameSize; ++var12) {
                    int var13 = (var12 + var9) * 4;
                    tmp[0] = grid[var13];
                    tmp[1] = grid[var13 + 1];
                    tmp[2] = grid[var13 + 2];
                    tmp[3] = grid[var13 + 3];
                    int var14 = var12 * ratio;
                    int var15 = var14 + var11;

                    for (int var16 = 0; var16 < ratio; ++var16) {
                        int var17 = var15 + var16 * tileW;
                        dstBuffer.position(var17 * 4);

                        for (int var18 = 0; var18 < ratio; ++var18) {
                            dstBuffer.put(tmp);
                        }
                    }
                }
            }
        } else {
            dstBuffer.put(grid);
        }

        dstBuffer.position(0).limit(tileW * tileW * 4);
    }

    private boolean scalesWithFastColor(DynamicTexture binder) {
        return !binder.getClass().getName().equals("ModTextureStatic");
    }

    @Override
    public Vec2 getTextureResolution(String name) {
        Integer id = this.idMap.get(name);
        return id != null ? this.getTextureDimensions(id) : null;
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

    @Unique
    public void updateTextureAnimations() {
        for (AC_TextureAnimated acBinder : this.textureAnimations.values()) {
            var tex = (AC_TextureBinder) acBinder;
            IntBuffer imgBuffer = tex.getBufferAtCurrentFrame();
            if (imgBuffer == null) {
                continue;
            }
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.loadTexture(tex.getTexture()));
            GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0, tex.getX(), tex.getY(), tex.getWidth(), tex.getHeight(),
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imgBuffer);
        }
    }

    @Override
    public void replaceTexture(String keyName, String replacementName) {
        int id = this.loadTexture(keyName);
        this.loadTexture(id, replacementName);
        if (!this.replacedTextures.contains(keyName)) {
            this.replacedTextures.add(keyName);
        }
    }

    @Override
    public void revertTextures() {
        for (String key : this.replacedTextures) {
            Integer id = this.idMap.get(key);
            if (id != null) {
                this.loadTexture(id, key);
            }
        }
    }
}
