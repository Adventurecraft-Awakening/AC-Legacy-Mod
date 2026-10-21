package dev.adventurecraft.awakening.client.render.texture;

import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.resource.TextureDesc;
import dev.adventurecraft.awakening.client.render.resource.MipmapMode;
import dev.adventurecraft.awakening.client.render.resource.TextureFilter;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageMipmapper;
import dev.adventurecraft.awakening.layout.IntRect;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.lwjgl.BufferUtils;

/** Render-thread asset ownership. Stable asset references survive image replacement during reload. */
public final class TextureCatalog implements AutoCloseable {
    @FunctionalInterface public interface Loader { ImageBuffer load(String path) throws IOException; }
    public record Settings(int mipLevel, MipmapMode mipmap, boolean anaglyph) {
        public Settings { if (mipLevel < 0) throw new IllegalArgumentException("Negative mip level"); Objects.requireNonNull(mipmap); }
    }
    private final Loader loader;
    private final Supplier<Settings> settings;
    private final Map<TextureAsset, Entry> assets = new LinkedHashMap<>();
    private final Map<String, String> replacements = new LinkedHashMap<>();
    private ByteBuffer scratch;

    public TextureCatalog(Loader loader, Supplier<Settings> settings) {
        this.loader = Objects.requireNonNull(loader); this.settings = Objects.requireNonNull(settings);
    }
    public RenderTexture texture(TextureAsset asset) throws IOException {
        Entry entry = assets.get(asset);
        if (entry == null) {
            entry = new Entry(asset);
            update(entry);
            assets.put(asset, entry);
        }
        return entry.reference.borrow();
    }
    public record AssetTexture(TextureAsset asset, RenderTexture texture) { }
    /** Borrowed current asset references for diagnostic views. */
    public java.util.List<AssetTexture> assets() {
        var result = new java.util.ArrayList<AssetTexture>(assets.size());
        for (Entry entry : assets.values()) result.add(new AssetTexture(entry.asset, entry.reference.borrow()));
        return java.util.List.copyOf(result);
    }
    /** Caller owns generated images; catalog assets are always borrowed. */
    public RenderTexture create(ImageBuffer image, TextureLoadOptions options) {
        return upload(null, image, options, settings.get());
    }
    public void reload() throws IOException {
        for (Entry entry : assets.values()) update(entry);
    }
    public void replace(String path, String replacement) throws IOException {
        replaceAll(Map.of(path, replacement));
    }
    /** Load and prepare a texture bundle before publishing any of its replacement images. */
    public void replaceAll(Map<String, String> paths) throws IOException {
        if (paths.isEmpty()) return;
        var prepared = new LinkedHashMap<Entry, RenderTexture>();
        try {
            for (Entry entry : assets.values()) {
                String replacement = paths.get(entry.asset.path());
                if (replacement == null) continue;
                ImageBuffer image = loader.load(replacement);
                prepared.put(entry, upload(null, image, entry.asset.options(), settings.get()));
            }
        } catch (IOException | RuntimeException | Error error) {
            for (RenderTexture image : prepared.values()) image.close();
            throw error;
        }
        replacements.putAll(paths);
        for (var replacement : prepared.entrySet()) {
            RenderTexture old = replacement.getKey().image;
            replacement.getKey().image = replacement.getValue();
            old.close();
        }
        TextureStore.onMaterialChange();
    }
    public void revertReplacements() throws IOException {
        var paths = new LinkedHashMap<String, String>();
        for (String path : replacements.keySet()) paths.put(path, path);
        replaceAll(paths);
        replacements.clear();
    }
    private void update(Entry entry) throws IOException {
        ImageBuffer image = loader.load(replacements.getOrDefault(entry.asset.path(), entry.asset.path()));
        entry.image = upload(entry.image, image, entry.asset.options(), settings.get());
    }
    private RenderTexture upload(RenderTexture old, ImageBuffer image, TextureLoadOptions options, Settings settings) {
        int width = image.getWidth(), height = image.getHeight();
        int maxMip = ImageMipmapper.clampLevel(width, height, settings.mipLevel());
        TextureDesc desc = new TextureDesc(width, height, maxMip + 1);
        boolean replace = old == null || !old.descriptor().equals(desc);
        RenderTexture result = replace ? RenderTexture.create(desc) : old;
        try {
            TextureFilter filter = options.blur() ? TextureFilter.LINEAR : TextureFilter.NEAREST;
            result.setFilter(filter, filter, maxMip == 0 ? MipmapMode.NONE : settings.mipmap());
            result.setClamp(options.clamp());
            int bytes = TextureUpload.byteSize(width, height, maxMip);
            if (scratch == null || scratch.capacity() < bytes) scratch = BufferUtils.createByteBuffer(bytes);
            var pixels = scratch.clear().limit(bytes).asIntBuffer();
            TextureUpload.prepare(image, pixels, maxMip, settings.anaglyph(), options.dataType());
            TextureUpload.uploadLevels(result, new IntRect(0, 0, width, height), 0, maxMip, 1, pixels);
        } catch (RuntimeException | Error error) {
            if (replace) result.close();
            throw error;
        }
        if (replace && old != null) old.close();
        return result;
    }
    @Override public void close() {
        for (Entry entry : assets.values()) { entry.reference.close(); entry.image.close(); }
        assets.clear(); replacements.clear(); scratch = null;
    }
    private static final class Entry {
        final TextureAsset asset;
        final RenderTexture reference;
        RenderTexture image;
        Entry(TextureAsset asset) { this.asset = asset; this.reference = RenderTexture.assetReference(() -> this.image); }
    }
}
