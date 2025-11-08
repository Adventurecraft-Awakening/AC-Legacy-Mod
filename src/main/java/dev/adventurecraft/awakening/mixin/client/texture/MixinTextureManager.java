package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.layout.Size;
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

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Stream;

// TODO: improve texture management and lookups.
//       do to not use strings and IDs at random

@Mixin(Textures.class)
public abstract class MixinTextureManager implements ExTextureManager {

    private static final ImageLoadOptions LOAD_RGBA_U8 = ImageLoadOptions.withFormat(ImageFormat.RGBA_U8);

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

    @Unique
    private Int2ObjectOpenHashMap<Vec2> textureDimensionsMap = new Int2ObjectOpenHashMap<>();

    @Unique
    private IntBuffer[] mipImageDatas;

    @Unique
    private ArrayList<String> replacedTextures = new ArrayList<>();

    @Unique
    private HashMap<String, AC_TextureAnimated> textureAnimations = new HashMap<>();

    @Unique
    private Int2ObjectOpenHashMap<ImageBuffer> loadedBuffers = new Int2ObjectOpenHashMap<>();

    @Shadow
    protected abstract int smoothBlend(int var1, int var2);

    @Shadow
    public abstract void releaseTexture(int id);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(TexturePackRepository var1, Options var2, CallbackInfo ci) {
        this.allocateImageData(256, 256);
    }

    @Overwrite
    public void loadTexture(BufferedImage image, int texId) {
        var buffer = ImageBuffer.from(image);
        loadTexture(buffer, texId);
    }

    @Unique
    @Override
    public void loadTexture(ImageBuffer image, int texId) {
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

        var colors = BufferUtils.createIntBuffer(texW * texH);
        image.copyTo(colors, ImageFormat.RGBA_U8);

        if (image.getFormat().hasAlpha()) {
            boolean hasAvgColor = false;
            int avgColor = 0;

            int colorsLength = colors.limit();
            for (int i = 0; i < colorsLength; ++i) {
                int color = colors.get(i);
                int a = color >>> 24;

                if (this.options != null && this.options.anaglyph3d) {
                    int r = color & 255;
                    int g = color >> 8 & 255;
                    int b = color >> 16 & 255;

                    int r3D = (r * 30 + g * 59 + b * 11) / 100;
                    int g3D = (r * 30 + g * 70) / 100;
                    int b3D = (r * 30 + b * 70) / 100;
                    color = Rgba.fromRgba8(r3D, g3D, b3D, a);
                }

                if (a == 0) {
                /* TODO
                if (texId == this.terrainTextureId || texId == this.guiItemsTextureId) {
                    color = -1;
                } else*/
                    {
                        if (!hasAvgColor) {
                            avgColor = this.getAverageOpaqueColor(colors);
                            hasAvgColor = true;
                        }
                        color = Rgba.withAlpha(avgColor, a);
                    }
                }

                colors.put(i, color);
            }
        }

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texW, texH, 0,
            GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, colors);

        if (MIPMAP) {
            this.generateMipMaps(colors, texW, texH);
        }
    }

    @Override
    public int loadTexture(ImageBuffer buffer) {
        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int id = this.ib.get(0);
        this.loadTexture(buffer, id);
        this.loadedBuffers.put(id, buffer);
        return id;
    }

    @Inject(method = "releaseTexture", at = @At("HEAD"))
    private void releaseLoadedBuffer(int id, CallbackInfo ci) {
        this.loadedBuffers.remove(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DynamicTexture> Stream<T> getTextureBinders(Class<T> type) {
        return this.dynamicTextures.stream()
            .filter(type::isInstance)
            .map(binder -> (T) binder);
    }

    @Override
    public ImageBuffer getTextureImage(String name) throws IOException {
        InputStream stream = this.skins.selected.getResource(name);
        if (stream == null) {
            throw new FileNotFoundException(name);
        }
        return ImageLoader.load(stream, LOAD_RGBA_U8);
    }

    @Overwrite
    public int loadTexture(String name) {
        Integer prevId = this.idMap.get(name);
        if (prevId != null) {
            return prevId;
        }

        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int id = this.ib.get(0);
        this.loadTexture(id, name);
        this.idMap.put(name, id);
        return id;
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

            ImageBuffer image = null;
            if (Minecraft.instance.level != null) {
                image = ((ExWorld) Minecraft.instance.level).loadMapTexture(name);
            }

            if (image == null) {
                InputStream stream = texPack.getResource(name);
                if (stream == null) {
                    var file = new File(name);
                    if (file.exists()) {
                        image = ImageLoader.load(file, LOAD_RGBA_U8);
                    }
                } else {
                    image = ImageLoader.load(stream, LOAD_RGBA_U8);
                }
            }

            if (image == null) {
                image = ImageBuffer.from(this.missingTex);
            }

            if (originalName.startsWith("##")) {
                // TODO:
                // image = this.makeStrip(image);
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

    @Unique
    private int getAverageOpaqueColor(IntBuffer var1) {
        long totalR = 0L;
        long totalG = 0L;
        long totalB = 0L;
        long colorCount = 0L;

        int len = var1.limit();
        for (int i = 0; i < len; i++) {
            int color = var1.get(i);
            int a = color >> 24 & 255;
            if (a != 0) {
                int r = color & 255;
                int g = color >> 8 & 255;
                int b = color >> 16 & 255;
                totalR += r;
                totalG += g;
                totalB += b;
                ++colorCount;
            }
        }

        if (colorCount <= 0L) {
            return -1;
        }
        int var11 = (int) (totalR / colorCount);
        int var12 = (int) (totalG / colorCount);
        int var13 = (int) (totalB / colorCount);
        return Rgba.fromRgb8(var13, var12, var11);
    }

    @Unique
    private void generateMipMaps(IntBuffer image, int width, int height) {
        IntBuffer srcImage = image;

        for (int level = 1; level <= 16; ++level) {
            int prevWidth = width >> (level - 1);
            int levelWidth = width >> level;
            int levelHeight = height >> level;
            if (levelWidth <= 0 || levelHeight <= 0) {
                break;
            }

            IntBuffer dstImage = this.mipImageDatas[level - 1];

            for (int y = 0; y < levelHeight; ++y) {
                for (int x = 0; x < levelWidth; ++x) {
                    int topLeft = srcImage.get((x * 2 + 0 + (y * 2 + 0) * prevWidth));
                    int topRight = srcImage.get((x * 2 + 1 + (y * 2 + 0) * prevWidth));
                    int botLeft = srcImage.get((x * 2 + 0 + (y * 2 + 1) * prevWidth));
                    int botRight = srcImage.get((x * 2 + 1 + (y * 2 + 1) * prevWidth));

                    int color = this.weightedAverageColor(topLeft, topRight, botRight, botLeft);
                    dstImage.put((x + y * levelWidth), color);
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

        var acBinder = (AC_TextureBinder) binder;
        binder.bind((Textures) (Object) this);

        Size size = GLTexture.getSize(GLTextureTarget.TEXTURE_2D, 0);
        acBinder.onTick(size);

        this.dynamicTextures.add(binder);
        ACMod.LOGGER.info("Texture registered: {}, image: {}, index: {}", binder, binder.textureId, binder.tex);
    }

    @Unique
    private void generateMipMapsSub(int subX, int subY, int tileW, int tileH, IntBuffer image, int texSize, boolean fastColor) {
        IntBuffer srcImage = image;

        for (int level = 1; level <= 16; ++level) {
            int prevWidth = tileW >> level - 1;
            int levelWidth = tileW >> level;
            int levelHeight = tileH >> level;
            int levelX = subX >> level;
            int levelY = subY >> level;
            if (levelWidth <= 0 || levelHeight <= 0) {
                break;
            }

            IntBuffer dstImage = this.mipImageDatas[level - 1];

            for (int y = 0; y < levelHeight; ++y) {
                for (int x = 0; x < levelWidth; ++x) {
                    int topLeft = srcImage.get((x * 2 + 0 + (y * 2 + 0) * prevWidth));
                    int topRight = srcImage.get((x * 2 + 1 + (y * 2 + 0) * prevWidth));
                    int botLeft = srcImage.get((x * 2 + 0 + (y * 2 + 1) * prevWidth));
                    int botRight = srcImage.get((x * 2 + 1 + (y * 2 + 1) * prevWidth));

                    int color;
                    if (fastColor) {
                        color = this.smoothBlend(this.smoothBlend(topLeft, topRight), this.smoothBlend(botRight, botLeft));
                    } else {
                        color = this.weightedAverageColor(topLeft, topRight, botRight, botLeft);
                    }
                    dstImage.put((x + y * levelWidth), color);
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
            binder.bind((Textures) (Object) this);

            int tileW = texSize.x / 16;
            int tileH = texSize.y / 16;
            this.checkImageDataSize(texSize.x, texSize.y);
            var dstPixels = this.pixels.asIntBuffer();

            Size texSize = GLTexture.getSize(GLTextureTarget.TEXTURE_2D, 0);
            acBinder.onTick(texSize);

            IntBuffer srcPixels = acBinder.getBufferAtCurrentFrame();
            if (srcPixels.limit() == 0) {
                continue;
            }
            this.copyScaled(srcPixels, dstPixels, tileW);

            binder.bind((Textures) (Object) this);
            boolean fastColor = this.scalesWithFastColor(binder);

            for (int tileY = 0; tileY < binder.replicate; ++tileY) {
                for (int tileX = 0; tileX < binder.replicate; ++tileX) {
                    int subX = binder.tex % 16 * tileW + tileX * tileW;
                    int subY = binder.tex / 16 * tileH + tileY * tileH;
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, subX, subY, tileW, tileH, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dstPixels);

                    if (MIPMAP && tileX == 0 && tileY == 0) {
                        this.generateMipMapsSub(subX, subY, tileW, tileH, dstPixels, binder.replicate, fastColor);
                    }
                }
            }

            if (binder.copyTo > 0) {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, binder.copyTo);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 16, 16, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, dstPixels);
                if (MIPMAP) {
                    this.generateMipMapsSub(0, 0, 16, 16, srcPixels, binder.replicate, false);
                }
            }
        }

        this.updateTextureAnimations();
    }

    @Unique
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

    @Unique
    private void setTextureDimension(int var1, Vec2 var2) {
        this.textureDimensionsMap.put(var1, var2);
    }

    @Unique
    private Vec2 getTextureDimensions(int var1) {
        return this.textureDimensionsMap.get(var1);
    }

    @Unique
    private void checkImageDataSize(int width, int height) {
        if (this.pixels != null) {
            int size = width * height * 4;
            if (this.pixels.capacity() >= size) {
                return;
            }
        }
        this.allocateImageData(width, height);
    }

    @Unique
    private void allocateImageData(int width, int height) {
        int size = width * height * 4;
        this.pixels = MemoryTracker.createByteBuffer(size);
        var mipBuffers = new ArrayList<IntBuffer>();

        for (int level = Math.max(width, height) / 2; level > 0; level /= 2) {
            int mipSize = level * level;
            IntBuffer mipBuffer = MemoryTracker.createIntBuffer(mipSize);
            mipBuffers.add(mipBuffer);
        }

        this.mipImageDatas = mipBuffers.toArray(new IntBuffer[0]);
    }

    @Unique
    private int getMaxMipmapLevel(int dim) {
        int size;
        for (size = 0; dim > 0; ++size) {
            dim /= 2;
        }
        return size - 1;
    }

    @Unique
    private void copyScaled(IntBuffer srcBuffer, IntBuffer dstBuffer, int tileW) {
        int srcSize = (int) Math.sqrt(srcBuffer.limit());
        if (srcSize == 0) {
            return;
        }

        ImageResizer.resizeUint8(
            srcBuffer, srcSize, srcSize, 0,
            dstBuffer, tileW, tileW, 0, 4);

        dstBuffer.limit(tileW * tileW);
    }

    @Unique
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

            String texName = tex.getTexture();
            int texId = this.loadTexture(texName);

            Size texSize = GLTexture.getSize(GLTextureTarget.TEXTURE_2D, 0);
            tex.onTick(texSize);

            IntBuffer imgBuffer = tex.getBufferAtCurrentFrame();
            if (imgBuffer.limit() == 0) {
                continue;
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
            GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0, acBinder.x, acBinder.y, tex.getWidth(), tex.getHeight(),
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
