package dev.adventurecraft.awakening.mixin.client.texture;

import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.client.render.texture.TextureUpload;
import dev.adventurecraft.awakening.client.render.texture.TextureStore;
import dev.adventurecraft.awakening.client.render.texture.TextureCatalog;
import dev.adventurecraft.awakening.client.render.texture.TextureAsset;
import dev.adventurecraft.awakening.client.render.texture.TextureLoadOptions;
import dev.adventurecraft.awakening.client.render.texture.TextureAnimationFrames;
import dev.adventurecraft.awakening.client.render.texture.TextureAnimationUpload;
import dev.adventurecraft.awakening.client.render.material.MapMaterials;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.resource.*;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.AC_TextureAnimated;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.client.skins.TexturePackRepository;
import org.apache.commons.lang3.NotImplementedException;
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
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Mixin(Textures.class)
public abstract class MixinTextureManager implements ExTextureManager {

    private static final ImageLoadOptions LOAD_RGBA_U8 = ImageLoadOptions.withFormat(ImageFormat.RGBA_U8);

    @Shadow public static boolean MIPMAP;
    @Shadow public HashMap<String, Integer> idMap;
    @Shadow private Options options;
    @Shadow public TexturePackRepository skins;
    @Shadow private ByteBuffer pixels;
    @Shadow private List<DynamicTexture> dynamicTextures;
    @Shadow private boolean clamp;
    @Shadow private boolean blur;
    @Shadow private BufferedImage missingTex;

    @Unique private TextureCatalog catalog;
    @Unique private HashMap<String, AC_TextureAnimated> textureAnimations = new HashMap<>();
    @Unique private final Set<DynamicTexture> animationWarnings = Collections.newSetFromMap(new IdentityHashMap<>());

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MemoryTracker;createByteBuffer(I)Ljava/nio/ByteBuffer;"))
    private ByteBuffer lazyPixelScratch(int ignoredSize) {
        // All upload paths below request the exact current mip-chain size on demand.
        return org.lwjgl.BufferUtils.createByteBuffer(0);
    }

    @Shadow public abstract int loadHttpTexture(String url, String fallback);

    @Override
    public TextureCatalog textureCatalog() {
        if (this.catalog == null) this.catalog = new TextureCatalog(this::loadAssetImage, () -> {
            var gameOptions = (ExGameOptions) this.options;
            MIPMAP = gameOptions.ofMipmapLevel() > 0;
            return new TextureCatalog.Settings(gameOptions.ofMipmapLevel(), gameOptions.ofMipmapMode(), this.options.anaglyph3d);
        });
        return this.catalog;
    }

    @Override
    public RenderTexture texture(String name) { return this.texture(asset(name)); }

    @Override
    public RenderTexture texture(TextureAsset asset) {
        try { return this.textureCatalog().texture(asset); }
        catch (IOException error) { throw new IllegalStateException("Cannot load texture " + asset.path(), error); }
    }

    @Override
    public RenderTexture materialTexture(String name, AC_TextureBinder.MaterialChannel channel) {
        TextureAsset source = asset(name);
        var type = channel == AC_TextureBinder.MaterialChannel.NORMAL
            ? TextureLoadOptions.DataType.NORMAL : TextureLoadOptions.DataType.SPECULAR;
        return this.texture(new TextureAsset(source.path(), new TextureLoadOptions(source.options().blur(),
            source.options().clamp(), type)));
    }

    @Override
    public boolean hasTexture(String name) {
        if (Minecraft.instance.level != null) {
            File directory = ((ExWorld) Minecraft.instance.level).getLevelDir();
            if (directory != null) {
                String relative = name.startsWith("/") ? name.substring(1) : name;
                if (new File(directory, relative).isFile()) return true;
            }
        }
        try (InputStream stream = this.skins.selected.getResource(name)) {
            return stream != null;
        } catch (IOException ignored) {
            return false;
        }
    }

    @Override
    public RenderTexture skin(String url, String fallback) {
        int id = this.loadHttpTexture(url, fallback);
        return id < 0 ? null : LegacyGlAdapter.textureForId(id);
    }

    @Override
    public RenderTexture createTexture(ImageBuffer image) {
        return this.textureCatalog().create(image, TextureLoadOptions.DEFAULT);
    }

    @Unique
    private static TextureAsset asset(String name) {
        boolean clamp = false, blur = false;
        // Decode inherited path annotations once at the asset boundary.
        if (name.startsWith("##")) name = name.substring(2); // Historical strip mode was never implemented.
        else if (name.startsWith("%clamp%")) { clamp = true; name = name.substring(7); }
        else if (name.startsWith("%blur%")) { blur = true; name = name.substring(6); }
        else if (Paths.get(name).getFileName().toString().startsWith("terrain")) clamp = true;
        TextureLoadOptions.DataType type = TextureLoadOptions.DataType.COLOR;
        if (Minecraft.instance != null && Minecraft.instance.level != null) {
            for (int atlas = 0; atlas < 4; atlas++) {
                if (name.equals(MapMaterials.normalPath(atlas))) type = TextureLoadOptions.DataType.NORMAL;
                else if (name.equals(MapMaterials.specularPath(atlas))) type = TextureLoadOptions.DataType.SPECULAR;
            }
        }
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith("_n.png")) type = TextureLoadOptions.DataType.NORMAL;
        else if (lower.endsWith("_s.png")) type = TextureLoadOptions.DataType.SPECULAR;
        return new TextureAsset(name, new TextureLoadOptions(blur, clamp, type));
    }

    @Unique
    private ImageBuffer loadAssetImage(String name) {
        try {
            ImageBuffer image = null;
            if (Minecraft.instance.level != null) image = ((ExWorld) Minecraft.instance.level).loadMapTexture(name);
            if (image != null) return image;
            try (InputStream stream = this.skins.selected.getResource(name)) {
                if (stream != null) return ImageLoader.load(stream, LOAD_RGBA_U8);
            }
            File file = new File(name);
            if (file.exists()) return ImageLoader.load(file, LOAD_RGBA_U8);
        } catch (IOException error) {
            ACMod.LOGGER.warn("Cannot load texture {}", name, error);
        }
        return ImageBuffer.from(this.missingTex);
    }

    @Overwrite
    public void loadTexture(BufferedImage image, int texId) { this.loadTexture(ImageBuffer.from(image), texId); }

    @Unique
    public void loadTexture(ImageBuffer image, int texId) {
        // The inherited upload ABI transfers generated/HTTP images to its integer-name owner.
        RenderTexture texture = this.textureCatalog().create(image, new TextureLoadOptions(this.blur, this.clamp));
        LegacyGlAdapter.ownTexture(texId, texture);
        MIPMAP = texture.descriptor().mipLevels() > 1;
    }

    @Overwrite
    public void loadTexture(int[] argb, int width, int height, int id) {
        if (argb.length < Math.multiplyExact(width, height)) throw new IllegalArgumentException("Incomplete texture image");
        RenderTexture texture = LegacyGlAdapter.textureForId(id);
        int mip = ImageMipmapper.clampLevel(width, height, texture.descriptor().mipLevels() - 1);
        IntBuffer pixels = this.getPixelBuffer(width, height, mip).asIntBuffer();
        for (int i = 0; i < width * height; i++) {
            int color = Rgba.fromBgra(argb[i]);
            if (this.options.anaglyph3d) {
                int r = Rgba.red(color), g = Rgba.green(color), b = Rgba.blue(color);
                color = Rgba.fromRgba8((r * 30 + g * 59 + b * 11) / 100,
                    (r * 30 + g * 70) / 100, (r * 30 + b * 70) / 100, Rgba.getAlpha(color));
            }
            pixels.put(i, color);
        }
        if (mip > 0) ImageMipmapper.generate(pixels, width, height, 1, mip);
        TextureUpload.uploadLevels(texture, new IntRect(0, 0, width, height), 0, mip, 1, pixels);
    }

    @Unique
    public int loadTexture(ImageBuffer image) {
        int id = LegacyGlAdapter.genTextures();
        this.loadTexture(image, id);
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
        try (InputStream stream = this.skins.selected.getResource(name)) {
            if (stream == null) throw new FileNotFoundException(name);
            return ImageLoader.load(stream, LOAD_RGBA_U8);
        }
    }

    @Overwrite
    public int loadTexture(String name) {
        Integer previous = this.idMap.get(name);
        if (previous != null) return previous;
        int id = LegacyGlAdapter.idForTexture(this.texture(name));
        this.idMap.put(name, id);
        return id;
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
        this.dynamicTextures.forEach(this::updateBinder);
        this.textureAnimations.values().forEach(this::updateBinder);
    }

    @Unique
    private void updateBinder(DynamicTexture binder) {
        binder.anaglyph3d = this.options.anaglyph3d;

        var acBinder = (AC_TextureBinder) binder;
        if (binder instanceof AC_TextureAnimated && !acBinder.hasCustomImage()) return;
        RenderTexture texture = binder instanceof AC_TextureAnimated animated
            ? this.texture(animated.texName) : acBinder.destination(this);
        Size texSize = texture.size(0);
        acBinder.onTick(texSize);

        AC_TextureBinder.Frame frame = acBinder.getCurrentFrame();
        if (frame == null) {
            return;
        }
        ImageBuffer image = frame.image();
        IntRect rect = frame.targetRect();
        if (binder instanceof AC_TextureAnimated animated) rect = new IntRect(animated.x, animated.y, rect.w, rect.h);
        if (rect.w < 1 || rect.h < 1) return;

        int replicate = Math.max(1, binder.replicate);
        try {
            TextureAnimationUpload.upload(texture, rect, image, replicate, asset(acBinder.getTexture()).options().dataType());
        } catch (IllegalArgumentException error) {
            if (this.animationWarnings.add(binder)) ACMod.LOGGER.warn("Invalid animated texture {}: {}", acBinder.getTexture(), error.getMessage());
            return;
        }
        for (var channel : AC_TextureBinder.MaterialChannel.values()) {
            AC_TextureBinder.Frame companion = acBinder.getMaterialFrame(channel);
            if (companion == null) continue;
            for (String path : materialPaths(acBinder.getTexture(), channel)) {
                if (!this.hasTexture(path)) {
                    if (this.animationWarnings.add(binder)) ACMod.LOGGER.warn("Animated material destination {} is missing; create a companion atlas first", path);
                    continue;
                }
                RenderTexture target = this.materialTexture(path, channel);
                var targetSize = target.size(0);
                try {
                    IntRect targetRect = scaleAnimationRect(rect, texSize, targetSize);
                    TextureAnimationUpload.upload(target, targetRect, companion.image(), replicate,
                        channel == AC_TextureBinder.MaterialChannel.NORMAL ? TextureLoadOptions.DataType.NORMAL : TextureLoadOptions.DataType.SPECULAR);
                } catch (IllegalArgumentException error) {
                    if (this.animationWarnings.add(binder)) ACMod.LOGGER.warn("Invalid animated companion {}: {}", path, error.getMessage());
                }
            }
        }
        // Animation is a texture revision, not a new lighting epoch. The irradiance
        // cache's rolling refresh samples current frames without restarting each tick.

        if (binder.copyTo > 0) {
            throw new NotImplementedException("Binder copying is not implemented.");
        }
    }

    @Unique
    private static IntRect scaleAnimationRect(IntRect rect, Size source, Size target) {
        long x = (long) rect.x * target.w, y = (long) rect.y * target.h;
        long width = (long) rect.w * target.w, height = (long) rect.h * target.h;
        if (source.w <= 0 || source.h <= 0 || x % source.w != 0 || width % source.w != 0
            || y % source.h != 0 || height % source.h != 0) {
            throw new IllegalArgumentException("Companion atlas cannot represent this animation rectangle exactly");
        }
        return new IntRect(Math.toIntExact(x / source.w), Math.toIntExact(y / source.h),
            Math.toIntExact(width / source.w), Math.toIntExact(height / source.h));
    }

    @Unique
    private static Set<String> materialPaths(String texture, AC_TextureBinder.MaterialChannel channel) {
        String path = asset(texture).path();
        Set<String> result = new LinkedHashSet<>();
        for (int atlas = 0; atlas < 4; atlas++) {
            if (asset(MapMaterials.albedoPath(atlas)).path().equals(path)) {
                result.add(channel == AC_TextureBinder.MaterialChannel.NORMAL
                    ? MapMaterials.normalPath(atlas) : MapMaterials.specularPath(atlas));
            }
        }
        if (result.isEmpty()) result.add(TextureAnimationFrames.siblingPath(path,
            channel == AC_TextureBinder.MaterialChannel.NORMAL ? "_n" : "_s"));
        return result;
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
        // Catalog reload owns asset images; skip the inherited integer-map upload loop.
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
        this.animationWarnings.clear();
        TextureStore.onResourceReload();
        try { this.textureCatalog().reload(); }
        catch (IOException error) { throw new IllegalStateException("Cannot reload texture catalog", error); }
        // Catalog references keep vanilla names and native consumers attached to replacement images.
    }

    @Unique
    private ByteBuffer getPixelBuffer(int width, int height, int maxLevel) {
        int size = TextureUpload.byteSize(width, height, maxLevel);
        if (this.pixels == null || this.pixels.capacity() < size) this.pixels = org.lwjgl.BufferUtils.createByteBuffer(size);
        return this.pixels.clear().limit(size);
    }

    @Override
    public void clearTextureAnimations() {
        this.textureAnimations.clear();
        this.animationWarnings.clear();
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
        String key = asset(keyName).path(), replacement = asset(replacementName).path();
        Map<String, String> bundle = new LinkedHashMap<>();
        bundle.put(key, replacement);
        if (asset(key).options().dataType() == TextureLoadOptions.DataType.COLOR) {
            for (var channel : AC_TextureBinder.MaterialChannel.values()) {
                String sibling = TextureAnimationFrames.siblingPath(replacement,
                    channel == AC_TextureBinder.MaterialChannel.NORMAL ? "_n" : "_s");
                for (String path : materialPaths(key, channel)) {
                    if (this.hasTexture(path)) bundle.put(path, this.hasTexture(sibling) ? sibling : path);
                }
            }
        }
        try { this.textureCatalog().replaceAll(bundle); }
        catch (IOException error) { throw new IllegalStateException("Cannot replace texture " + keyName, error); }
    }

    @Override
    public void revertTextures() {
        try { this.textureCatalog().revertReplacements(); }
        catch (IOException error) { throw new IllegalStateException("Cannot restore textures", error); }
    }
}
