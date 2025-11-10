package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.gl.*;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Size;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.MemoryTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.client.skins.TexturePack;
import net.minecraft.client.skins.TexturePackRepository;
import org.apache.commons.lang3.NotImplementedException;
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
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

// TODO: improve texture management and lookups.
//       do to not use strings and IDs at random

@Mixin(Textures.class)
public abstract class MixinTextureManager implements ExTextureManager {

    private static final ImageLoadOptions LOAD_RGBA_U8 = ImageLoadOptions.withFormat(ImageFormat.RGBA_U8);

    private static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
    private static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;

    @Shadow public static boolean MIPMAP;
    @Shadow public HashMap<String, Integer> idMap;
    @Shadow private Options options;
    @Shadow public TexturePackRepository skins;
    @Shadow private IntBuffer ib;
    @Shadow private ByteBuffer pixels;
    @Shadow private List<DynamicTexture> dynamicTextures;
    @Shadow private boolean clamp;
    @Shadow private boolean blur;
    @Shadow private BufferedImage missingTex;

    @Unique private boolean strip;
    @Unique private HashMap<String, String> replacedTextures = new HashMap<>();
    @Unique private HashMap<String, AC_TextureAnimated> textureAnimations = new HashMap<>();

    @Shadow
    public abstract void releaseTexture(int id);

    @Overwrite
    public void loadTexture(BufferedImage image, int texId) {
        var buffer = ImageBuffer.from(image);
        this.loadTexture(buffer, texId);
    }

    @Unique
    @Override
    public void loadTexture(ImageBuffer image, int texId) {
        long start = System.nanoTime();
        var options = (ExGameOptions) this.options;
        int texW = image.getWidth();
        int texH = image.getHeight();
        var texRect = new IntRect(0, 0, texW, texH);

        final var target = GLTextureTarget.TEXTURE_2D;
        GL11.glBindTexture(target.symbol, texId);

        int mipLevel = options.ofMipmapLevel();
        GLMipMode mipMode = GLMipMode.NONE;
        GLFilter filter = this.blur ? GLFilter.LINEAR : GLFilter.NEAREST;
        int wrapping = this.clamp ? GL12.GL_CLAMP_TO_EDGE : GL12.GL_REPEAT;

        MIPMAP = mipLevel > 0;
        if (MIPMAP /* TODO && texId != this.guiItemsTextureId*/) {
            mipMode = options.ofMipmapMode();
            mipLevel = ImageMipmapper.clampLevel(texW, texH, mipLevel);
        }

        if (GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic) {
            float anisoLevel = 1f;
            if (mipLevel > 0) {
                // TODO: cache this
                FloatBuffer buf = BufferUtils.createFloatBuffer(1);
                buf.rewind();
                GL11.glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, buf);
                float anisoLimit = buf.get(0);

                anisoLevel = Math.min(((ExGameOptions) this.options).ofAfLevel(), anisoLimit);
            }
            GL11.glTexParameterf(target.symbol, GL_TEXTURE_MAX_ANISOTROPY_EXT, anisoLevel);
        }

        GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(target.symbol, GL12.GL_TEXTURE_MAX_LEVEL, mipLevel);

        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MIN_FILTER, filter.getMinSymbol(mipMode));
        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_MAG_FILTER, filter.getMagSymbol(mipMode));

        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_WRAP_S, wrapping);
        GL11.glTexParameteri(target.symbol, GL11.GL_TEXTURE_WRAP_T, wrapping);

        var pixelBuffer = this.getPixelBuffer(texW, texH).asIntBuffer();

        var level0Buffer = pixelBuffer.slice(0, texW * texH);
        image.copyTo(level0Buffer, ImageFormat.RGBA_U8);

        boolean anaglyph = this.options != null && this.options.anaglyph3d;
        if (image.getFormat().hasAlpha() || anaglyph) {
            // TODO: do any textures actually need average color?
            int avgColor = 0;
            avgColor = anaglyph
                ? this.copyAlphaReplaceAnaglyph(level0Buffer, avgColor)
                : this.copyAlphaReplace(level0Buffer, avgColor);
        }

        if (mipLevel >= 1) {
            ImageMipmapper.generate(pixelBuffer, texW, texH, 1, mipLevel);
        }

        this.initTextureLevels(target, texW, texH, mipLevel);
        this.uploadTextureLevels(target, texRect, 0, mipLevel, 1, pixelBuffer);

        if (ACMod.LOGGER.isDebugEnabled()) {
            long end = System.nanoTime();
            String time = String.format("%.3g ms", (end - start) / (double) 1_000_000);
            String name = this.findTextureById(texId).orElse("");
            var info = new GLTextureInfo(target, name, texId);
            ACMod.LOGGER.debug("Texture processed - {}, {}", info, time);
        }
    }

    @Unique
    private int copyAlphaReplaceAnaglyph(IntBuffer buffer, int avgColor) {
        int len = buffer.limit();
        for (int i = 0; i < len; ++i) {
            int color = buffer.get(i);

            int r = color & 255;
            int g = (color >>> 8) & 255;
            int b = (color >>> 16) & 255;
            int a = (color >>> 24);

            int r3D = (r * 30 + g * 59 + b * 11) / 100;
            int g3D = (r * 30 + g * 70) / 100;
            int b3D = (r * 30 + b * 70) / 100;
            color = Rgba.fromRgba8(r3D, g3D, b3D, a);

            if (a == 0) {
                if (avgColor == 0) {
                    avgColor = ImageMipmapper.getAverageRgb(buffer);
                }
                color = Rgba.withAlpha(avgColor, a);
            }
            buffer.put(i, color);
        }
        return avgColor;
    }

    @Unique
    private int copyAlphaReplace(IntBuffer buffer, int avgColor) {
        int len = buffer.limit();
        for (int i = 0; i < len; ++i) {
            int color = buffer.get(i);
            int a = color >>> 24;
            if (a == 0) {
                if (avgColor == 0) {
                    avgColor = ImageMipmapper.getAverageRgb(buffer);
                }
                color = Rgba.withAlpha(avgColor, a);
            }
            buffer.put(i, color);
        }
        return avgColor;
    }

    @Unique
    private Optional<String> findTextureById(int id) {
        return this.idMap.entrySet().stream().filter(e -> e.getValue() == id).findAny().map(Map.Entry::getKey);
    }

    @Override
    public int loadTexture(ImageBuffer buffer) {
        this.ib.clear();
        MemoryTracker.genTextures(this.ib);
        int id = this.ib.get(0);
        this.loadTexture(buffer, id);
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DynamicTexture> Stream<T> getTextureBinders(Class<T> type) {
        return this.dynamicTextures.stream().filter(type::isInstance).map(binder -> (T) binder);
    }

    @Override
    public ImageBuffer getTextureImage(String name)
        throws IOException {
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
        this.idMap.put(name, id);
        this.loadTexture(id, name);
        return id;
    }

    @Override
    public void loadTexture(int id, String name) {
        String originalName = name;
        try {
            TexturePack texPack = this.skins.selected;
            if (name.startsWith("##")) {
                this.strip = true;
                name = name.substring(2);
            }
            else if (name.startsWith("%clamp%")) {
                this.clamp = true;
                name = name.substring(7);
            }
            else if (name.startsWith("%blur%")) {
                this.blur = true;
                name = name.substring(6);
            }
            else {
                if (Paths.get(name).getFileName().toString().startsWith("terrain")) {
                    this.clamp = true;
                }
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
                }
                else {
                    image = ImageLoader.load(stream, LOAD_RGBA_U8);
                }
            }

            if (image == null) {
                image = ImageBuffer.from(this.missingTex);
            }

            if (this.strip) {
                // TODO:
                // image = this.makeStrip(image);
            }

            this.loadTexture(image, id);

            this.strip = false;
            this.clamp = false;
            this.blur = false;
        }
        catch (IOException var8) {
            var8.printStackTrace();
            this.loadTexture(this.missingTex, id);
        }
    }

    // TODO: move to GLDevice/GLTexture
    @Unique
    private void initTextureLevels(GLTextureTarget target, int width, int height, int maxLevel) {
        final int type = GL11.GL_UNSIGNED_BYTE;
        final int format = GL11.GL_RGBA;
        final int internalFormat = GL11.GL_RGBA8;

        int levelCount = maxLevel + 1;
        int levelLimit = MathF.log2(Math.max(width, height)) + 1;
        if (levelCount > levelLimit) {
            throw new RuntimeException();
        }

        // TODO: immutable GLTextures
        //if (GLContext.getCapabilities().GL_ARB_texture_storage) {
        //    ARBTextureStorage.glTexStorage2D(target.id, levelCount, internalFormat, width, height);
        //    return;
        //}

        for (int level = 0; level <= maxLevel; ++level) {
            int w = Math.max(1, width >> level);
            int h = Math.max(1, height >> level);
            GL11.glTexImage2D(target.symbol, level, internalFormat, w, h, 0, format, type, (ByteBuffer) null);
        }
    }

    // TODO: move to GLDevice/GLTexture
    @Unique
    private void uploadTextureLevels(
        GLTextureTarget target,
        IntRect rect,
        int minLevel,
        int maxLevel,
        int replicate,
        IntBuffer levelBuffer
    ) {
        if (replicate < 1) {
            throw new IllegalArgumentException();
        }
        final int format = GL11.GL_RGBA;
        final int type = GL11.GL_UNSIGNED_BYTE;

        int levelOffset = ImageMipmapper.getPixelOffset(rect.w, rect.h, minLevel);

        for (int level = minLevel; level <= maxLevel; ++level) {
            int lW = Math.max(1, rect.w >> level);
            int lH = Math.max(1, rect.h >> level);
            int lX = rect.x >> level;
            int lY = rect.y >> level;

            IntBuffer levelSpan = levelBuffer.slice(levelOffset, lW * lH);
            levelOffset += levelSpan.limit();

            for (int tileY = 0; tileY < replicate; ++tileY) {
                int tY = lY + tileY * lH;

                for (int tileX = 0; tileX < replicate; ++tileX) {
                    int tX = lX + tileX * lW;

                    // glCopyTexSubImage2D would be good here... if it didn't need the texture wrapped in a framebuffer
                    GL11.glTexSubImage2D(target.symbol, level, tX, tY, lW, lH, format, type, levelSpan);
                }
            }
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

        this.updateBinder(binder);
        this.dynamicTextures.add(binder);
        ACMod.LOGGER.info("Texture registered: {}, image: {}, index: {}", binder, binder.textureId, binder.tex);
    }

    @Overwrite
    public void tick() {
        // TODO:
        //this.terrainTextureId = this.getTextureId("/terrain.png");
        //this.guiItemsTextureId = this.getTextureId("/gui/items.png");

        this.dynamicTextures.forEach(this::updateBinder);
        this.textureAnimations.values().forEach(this::updateBinder);

        //updateTextureAnimations();
    }

    @Unique
    private void updateBinder(DynamicTexture binder) {
        binder.anaglyph3d = this.options.anaglyph3d;

        var acBinder = (AC_TextureBinder) binder;
        binder.bind((Textures) (Object) this);

        Size texSize = GLTexture.getSize(GLTextureTarget.TEXTURE_2D, 0);
        acBinder.onTick(texSize);

        AC_TextureBinder.Frame frame = acBinder.getCurrentFrame();
        if (frame == null) {
            return;
        }
        // TODO: clamp coords to image size

        ImageBuffer image = frame.image();
        IntRect rect = frame.targetRect();

        ByteBuffer byteBuffer = this.getPixelBuffer(rect.w, rect.h);
        IntBuffer pixelBuffer = byteBuffer.asIntBuffer();

        ImageBuffer level0 = ImageBuffer.wrap(byteBuffer, rect.w, rect.h, ImageFormat.RGBA_U8);
        ImageResizer.resize(image, level0);

        final var target = GLTextureTarget.TEXTURE_2D;
        int maxLevel = GL11.glGetTexParameteri(target.symbol, GL12.GL_TEXTURE_MAX_LEVEL);
        int mipLevel = ImageMipmapper.clampLevel(rect.w, rect.h, maxLevel);
        if (mipLevel >= 1) {
            ImageMipmapper.generate(pixelBuffer, rect.w, rect.h, 1, mipLevel);
        }

        int replicate = Math.max(1, binder.replicate);
        this.uploadTextureLevels(target, rect, 0, mipLevel, replicate, pixelBuffer);

        if (binder.copyTo > 0) {
            throw new NotImplementedException("Binder copying is not implemented.");
        }
    }

    @Overwrite
    private int crispBlend(int L, int R) {
        return Rgba.crispBlend(L, R);
    }

    @Redirect(
        method = "reloadAll",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Textures;idMap:Ljava/util/HashMap;"
        )
    )
    private HashMap<String, Integer> useEmptySetForTextureReload(Textures instance) {
        // TODO: why? add docs!
        return new HashMap<>();
    }

    @Inject(
        method = "reloadAll",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/renderer/Textures;idMap:Ljava/util/HashMap;",
            shift = At.Shift.BEFORE,
            ordinal = 0
        )
    )
    private void useCustomTextureReload(CallbackInfo ci) {
        // TODO: why? add docs!
        for (String key : this.idMap.keySet()) {
            int id = this.idMap.get(key);
            this.loadTexture(id, key);
        }
    }

    @Unique
    private ByteBuffer getPixelBuffer(int width, int height) {
        int level = MathF.roundUpToPow2Mask(Math.max(width, height));
        int size = level * level * 4;
        if (this.pixels == null || this.pixels.capacity() < size) {
            this.pixels = MemoryTracker.createByteBuffer(size);
        }
        return this.pixels;
    }

    @Override
    public void clearTextureAnimations() {
        // TODO: dispose resources?
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

    @Override
    public void replaceTexture(String keyName, String replacementName) {
        int id = this.loadTexture(keyName);
        this.loadTexture(id, replacementName);
        this.replacedTextures.put(keyName, replacementName);
    }

    @Override
    public void revertTextures() {
        this.replacedTextures.forEach((key, name) -> {
            Integer id = this.idMap.get(key);
            if (id != null) {
                this.loadTexture(id, key);
            }
        });
    }
}
