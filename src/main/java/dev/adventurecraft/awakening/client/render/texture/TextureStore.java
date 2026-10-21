package dev.adventurecraft.awakening.client.render.texture;

import dev.adventurecraft.awakening.client.render.RenderSystem;
import dev.adventurecraft.awakening.client.render.resource.RenderTexture;
import dev.adventurecraft.awakening.client.render.resource.TextureDesc;
import dev.adventurecraft.awakening.client.render.state.SamplerState;
import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import dev.adventurecraft.awakening.layout.Size;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Render-thread texture storage. Collection captures revisions; preparation stages their uploads. */
public final class TextureStore {
    private static final Map<RenderTexture, Data> TEXTURES = new IdentityHashMap<>();
    private static RenderTexture white;
    private static long reloadGeneration;
    private static long materialGeneration;
    private TextureStore() { }

    public static void register(RenderTexture texture, TextureDesc descriptor) {
        Data data = new Data(Objects.requireNonNull(descriptor, "descriptor"));
        if (TEXTURES.putIfAbsent(texture, data) != null) throw new IllegalStateException("Texture is already registered");
        allocate(data, descriptor);
    }
    private static Data data(RenderTexture texture) {
        Data data = TEXTURES.get(Objects.requireNonNull(texture, "texture").storageOwner());
        if (data == null) throw new IllegalStateException("Texture storage has been released");
        return data;
    }
    public static TextureDesc descriptor(RenderTexture texture) {
        TextureDesc descriptor = data(texture).descriptor;
        if (descriptor == null) throw new IllegalStateException("Legacy image storage has not been defined");
        return descriptor;
    }
    /** Mutable allocation exists only for inherited texture definitions, never native creation. */
    public static RenderTexture createLegacyTexture() {
        RenderTexture texture = RenderTexture.create(new TextureDesc(1, 1, 1));
        Data data = data(texture);
        data.legacy = true;
        data.descriptor = null;
        data.levels.clear(); data.patches.clear();
        data.maxLevel = 1000;
        return texture;
    }
    /** A generated image transferred to the inherited integer-name owner may be redefined there. */
    public static void adoptLegacyAllocation(RenderTexture texture) { data(texture).legacy = true; }
    public static void allocateLegacy(RenderTexture texture, TextureDesc descriptor) {
        Data data = data(texture);
        if (!data.legacy) throw new IllegalStateException("Native texture allocation is immutable");
        allocate(data, descriptor);
    }
    private static void allocate(Data data, TextureDesc descriptor) {
        data.descriptor = descriptor;
        data.levels.clear(); data.patches.clear();
        for (int level = 0; level < descriptor.mipLevels(); level++) {
            int width = descriptor.width(level), height = descriptor.height(level);
            data.levels.put(level, new Level(new Pixels(width, height, BufferUtils.createByteBuffer(pixelBytes(width, height))), null));
            data.patch(level, new PixelPatch(0, 0, width, height, data.levels.get(level).pixels.rgba, width * 4));
        }
        data.baseLevel = 0; data.maxLevel = descriptor.mipLevels() - 1;
        data.changed(true);
    }
    public static void defineLegacyLevel(RenderTexture texture, int level, int width, int height, ByteBuffer rgba) {
        Data data = data(texture);
        if (!data.legacy) throw new IllegalStateException("Native texture allocation is immutable");
        if (level < 0 || width < 0 || height < 0 || rgba.remaining() != pixelBytes(width, height)) {
            throw new IllegalArgumentException("Invalid legacy texture level");
        }
        data.levels.put(level, new Level(new Pixels(width, height, BufferUtils.createByteBuffer(rgba.remaining()).put(rgba.duplicate()).flip()), null));
        data.patches.removeIf(patch -> patch.level == level);
        if (width > 0 && height > 0) data.patch(level, new PixelPatch(0, 0, width, height, data.levels.get(level).pixels.rgba, width * 4));
        data.descriptor = legacyDescriptor(data);
        data.changed(true);
    }
    private static TextureDesc legacyDescriptor(Data data) {
        Level base = data.levels.get(0);
        if (base == null || base.pixels.width == 0 || base.pixels.height == 0) return null;
        int maximum = 32 - Integer.numberOfLeadingZeros(Math.max(base.pixels.width, base.pixels.height));
        int count = 0;
        while (count < maximum) {
            Level level = data.levels.get(count);
            if (level == null || level.pixels.width != Math.max(1, base.pixels.width >> count) || level.pixels.height != Math.max(1, base.pixels.height >> count)) break;
            count++;
        }
        return new TextureDesc(base.pixels.width, base.pixels.height, count);
    }
    public static void delete(RenderTexture texture) {
        Data data = TEXTURES.remove(texture.storageOwner());
        if (data == null) return;
        data.deleted = true;
        for (Snapshot snapshot : List.copyOf(data.snapshots)) snapshot.disposeIfUnused();
    }
    public static long reloadGeneration() { return reloadGeneration; }
    public static void onResourceReload() { reloadGeneration++; materialGeneration++; }
    public static long materialGeneration() { return materialGeneration; }
    public static void onMaterialChange() { materialGeneration++; }
    public static RenderTexture whiteTexture() {
        if (white == null || white.isClosed()) {
            white = RenderTexture.create(new TextureDesc(1, 1, 1));
            white.upload(0, 0, 0, 1, 1, BufferUtils.createByteBuffer(4).putInt(-1).flip());
            white.setClamp(true);
        }
        return white.borrow();
    }
    public static Size size(RenderTexture texture, int level) {
        Level image = data(texture).levels.get(level);
        return image == null ? new Size(0, 0) : new Size(image.pixels.width, image.pixels.height);
    }
    public static long sizeInBytes(RenderTexture texture) {
        long bytes = 0;
        for (Level image : data(texture).levels.values()) bytes = Math.addExact(bytes, image.pixels.rgba.remaining());
        return bytes;
    }
    public static ByteBuffer readLevel(RenderTexture texture, int level) {
        Level image = data(texture).levels.get(level);
        return image == null ? ByteBuffer.allocate(0).asReadOnlyBuffer() : image.materialize().asReadOnlyBuffer().order(image.pixels.rgba.order());
    }
    public static SamplerState sampler(RenderTexture texture) { return data(texture).sampler; }
    public static int baseLevel(RenderTexture texture) { return data(texture).baseLevel; }
    public static int maxLevel(RenderTexture texture) { return data(texture).maxLevel; }
    public static void setSampler(RenderTexture texture, SamplerState sampler) {
        Data data = data(texture);
        sampler = Objects.requireNonNull(sampler, "sampler");
        if (!data.sampler.equals(sampler)) { data.sampler = sampler; data.changed(false); }
    }
    public static void setMipRange(RenderTexture texture, int baseLevel, int maxLevel) {
        if (baseLevel < 0 || maxLevel < 0) throw new IllegalArgumentException("Negative mip level");
        Data data = data(texture);
        if (data.baseLevel != baseLevel || data.maxLevel != maxLevel) {
            data.baseLevel = baseLevel; data.maxLevel = maxLevel; data.changed(false);
        }
    }
    private static Level uploadLevel(Data data, int level, int x, int y, int width, int height, int pixelCount) {
        Level image = data.levels.get(level);
        if (image == null) throw new IllegalStateException("Texture level has not been allocated");
        if (x < 0 || y < 0 || width < 0 || height < 0 || (long) x + width > image.pixels.width || (long) y + height > image.pixels.height
            || pixelCount != Math.multiplyExact(width, height)) throw new IllegalArgumentException("Invalid texture upload region");
        return image;
    }
    public static void upload(RenderTexture texture, int level, int x, int y, int width, int height, ByteBuffer rgba) {
        if (rgba.remaining() % 4 != 0) throw new IllegalArgumentException("RGBA8 upload has incomplete pixels");
        Data data = data(texture);
        Level image = uploadLevel(data, level, x, y, width, height, rgba.remaining() / 4);
        if (width == 0 || height == 0) return;
        if (image.pixels.readers == 0) {
            image.compact();
            for (int row = 0; row < height; row++) image.pixels.rgba.put(((row + y) * image.pixels.width + x) * 4,
                rgba, rgba.position() + row * width * 4, width * 4);
            data.levels.put(level, new Level(image.pixels, null));
            data.patch(level, image.region(x, y, width, height));
        } else {
            // Captured revisions keep their baseline. Copy only this patch, never the whole mip.
            ByteBuffer payload = BufferUtils.createByteBuffer(rgba.remaining()).put(rgba.duplicate()).flip();
            appendPatch(data, level, image, new PixelPatch(x, y, width, height, payload, width * 4));
        }
        data.changed(true);
    }
    public static void upload(RenderTexture texture, int level, int x, int y, int width, int height, IntBuffer rgba) {
        Data data = data(texture);
        Level image = uploadLevel(data, level, x, y, width, height, rgba.remaining());
        if (width == 0 || height == 0) return;
        if (image.pixels.readers == 0) {
            image.compact();
            IntBuffer target = image.pixels.rgba.asIntBuffer();
            for (int row = 0; row < height; row++) target.put((row + y) * image.pixels.width + x, rgba, rgba.position() + row * width, width);
            data.levels.put(level, new Level(image.pixels, null));
            data.patch(level, image.region(x, y, width, height));
        } else {
            ByteBuffer payload = BufferUtils.createByteBuffer(Math.multiplyExact(rgba.remaining(), 4));
            payload.asIntBuffer().put(rgba.duplicate());
            appendPatch(data, level, image, new PixelPatch(x, y, width, height, payload, width * 4));
        }
        data.changed(true);
    }
    private static void appendPatch(Data data, int level, Level image, PixelPatch patch) {
        data.levels.put(level, new Level(image.pixels, new PatchNode(image.tail, patch)));
        data.patch(level, patch);
    }

    /** Each capture must be closed once after its draw is enqueued or abandoned. */
    public static Snapshot snapshot(RenderTexture texture) {
        if (texture == null) return new Snapshot();
        Data data = data(texture);
        SamplerState sampler = texture.sampler();
        int base = texture.baseLevel(), max = texture.maxLevel();
        if (data.latest == null || data.latest.revision != data.revision || data.latest.discarded
            || !data.latest.sampler.equals(sampler) || data.latest.baseLevel != base || data.latest.maxLevel != max) {
            data.latest = new Snapshot(data, sampler, base, max);
        }
        data.latest.uses++;
        return data.latest;
    }
    public static void prepareUploads(Collection<Snapshot> snapshots) {
        var ordered = new ArrayList<>(new LinkedHashSet<>(snapshots));
        ordered.sort(Comparator.comparingLong(snapshot -> snapshot.revision));
        for (Snapshot snapshot : ordered) snapshot.prepare();
    }
    public static Snapshot prepare(RenderTexture texture) {
        Snapshot snapshot = snapshot(texture);
        try { prepareUploads(List.of(snapshot)); return snapshot; }
        catch (RuntimeException | Error error) { snapshot.close(); throw error; }
    }
    /** Lookup only: all upload work belongs to prepareUploads, never draw resolution. */
    public static VulkanRenderer.Texture nativeTexture(RenderTexture texture) {
        if (texture == null) return RenderSystem.whiteTexture();
        Data data = data(texture);
        Snapshot snapshot = data.prepared;
        if (snapshot == null || snapshot.revision != data.revision || !snapshot.sampler.equals(texture.sampler())
            || snapshot.baseLevel != texture.baseLevel() || snapshot.maxLevel != texture.maxLevel())
            throw new IllegalStateException("Texture uploads must be prepared before drawing");
        return snapshot.nativeTexture();
    }
    public static final class Snapshot implements AutoCloseable {
        private final Data owner;
        private final long revision, imageRevision;
        private final TextureDesc descriptor;
        private final SamplerState sampler;
        private final int baseLevel, maxLevel;
        private List<UploadPatch> patches;
        private Map<Integer, Level> levels;
        private VulkanRenderer.Texture gpu;
        private int allocatedLevels;
        private int uses;
        private boolean ready, generatedMips, releasedCpu, discarded;
        private Snapshot() {
            owner = null; revision = imageRevision = 0; descriptor = null; sampler = SamplerState.NEAREST_REPEAT;
            baseLevel = maxLevel = 0; patches = List.of(); levels = Map.of(); uses = 1; ready = true;
        }
        private Snapshot(Data data, SamplerState sampling, int base, int max) {
            owner = data; revision = data.revision; imageRevision = data.imageRevision; descriptor = data.descriptor;
            sampler = sampling; baseLevel = base; maxLevel = max;
            patches = List.copyOf(data.patches); levels = Map.copyOf(data.levels);
            for (Level level : levels.values()) level.pixels.readers++;
            data.snapshots.add(this);
        }
        private void prepare() {
            if (ready) return;
            if (discarded) throw new IllegalStateException("Texture snapshot is no longer retained");
            if (descriptor == null) { ready = true; releaseCpu(); return; }
            var renderer = RenderSystem.renderer();
            Snapshot previous = owner.prepared;
            generatedMips = owner.legacy && descriptor.mipLevels() == 1
                && (sampler.mipmap() != SamplerState.MipmapMode.NONE || previous != null && previous.generatedMips);
            allocatedLevels = generatedMips ? 32 - Integer.numberOfLeadingZeros(Math.max(descriptor.width(), descriptor.height())) : descriptor.mipLevels();
            boolean replaceImage = previous == null || previous.gpu == null || previous.imageRevision > imageRevision
                || !previous.descriptor.equals(descriptor)
                || previous.allocatedLevels != allocatedLevels || previous.generatedMips != generatedMips;
            VulkanRenderer.Texture result = previous == null ? null : previous.gpu;
            try {
                if (replaceImage) {
                    List<ByteBuffer> supplied = new ArrayList<>();
                    for (int level = 0; level < descriptor.mipLevels(); level++) supplied.add(levels.get(level).materialize());
                    result = renderer.uploadTexture(descriptor.width(), descriptor.height(), allocatedLevels, supplied.getFirst(), generatedMips ? null : supplied, sampler);
                } else if (previous.imageRevision != imageRevision) {
                    List<VulkanRenderer.TextureRegion> regions = new ArrayList<>();
                    for (UploadPatch upload : patches) {
                        int level = upload.level;
                        if (level >= allocatedLevels || generatedMips && level != 0) continue;
                        PixelPatch patch = upload.patch;
                        regions.add(new VulkanRenderer.TextureRegion(level, patch.x, patch.y, patch.width, patch.height,
                            patch.rgba.duplicate(), patch.rowStride));
                    }
                    boolean preserve = false;
                    for (Snapshot retained : owner.snapshots) {
                        if (retained.ready && retained.gpu != null && retained.uses > 0 && retained.imageRevision == previous.imageRevision) { preserve = true; break; }
                    }
                    if (!regions.isEmpty()) result = renderer.updateTexture(result, regions, generatedMips, preserve);
                }
                if (replaceImage || previous == null || !sampler.equals(previous.sampler) || baseLevel != previous.baseLevel || maxLevel != previous.maxLevel) {
                    var sampled = renderer.withSampler(result, sampler, baseLevel, maxLevel);
                    if (result != sampled && (previous == null || result != previous.gpu)) result.close();
                    result = sampled;
                }
                // Each logical snapshot owns its descriptor even when pixels and sampling are shared.
                if (previous != null && result == previous.gpu) result = renderer.withSampler(result, sampler, baseLevel, maxLevel);
            } catch (RuntimeException | Error error) {
                if (result != null && (previous == null || result != previous.gpu)) result.close();
                throw error;
            }
            gpu = result;
            ready = true;
            // An out-of-order retained revision must not replace the anchor used for later deltas.
            boolean newest = previous == null || previous.revision <= revision;
            if (newest) owner.prepared = this;
            if (owner.revision == revision) owner.patches.clear();
            releaseCpu();
            if (newest && previous != null) previous.disposeIfUnused();
        }
        public VulkanRenderer.Texture nativeTexture() {
            if (discarded) throw new IllegalStateException("Texture snapshot is no longer retained");
            if (!ready) throw new IllegalStateException("Texture snapshot has not been prepared");
            return gpu == null ? RenderSystem.whiteTexture() : gpu;
        }
        private void releaseCpu() {
            if (releasedCpu) return;
            for (Level level : levels.values()) level.pixels.readers--;
            levels = Map.of(); patches = List.of(); releasedCpu = true;
            if (owner != null) owner.compactLevels();
        }
        private void disposeIfUnused() {
            if (owner == null || uses != 0 || !owner.deleted && owner.prepared == this) return;
            releaseCpu();
            if (gpu != null) { gpu.close(); gpu = null; }
            owner.snapshots.remove(this); discarded = true;
        }
        @Override public void close() {
            if (uses <= 0) throw new IllegalStateException("Texture snapshot released more than captured");
            uses--;
            disposeIfUnused();
        }
    }
    public static void releaseGpuResources() {
        for (Data data : TEXTURES.values()) {
            for (Snapshot snapshot : data.snapshots) {
                if (snapshot.gpu != null) { snapshot.gpu.close(); snapshot.gpu = null; }
                snapshot.discarded = true; snapshot.releaseCpu();
            }
            data.snapshots.clear(); data.latest = data.prepared = null;
        }
    }
    private static int pixelBytes(int width, int height) { return Math.multiplyExact(Math.multiplyExact(width, height), 4); }
    /** Mutable baseline is changed only while no unprepared snapshot observes it. */
    private static final class Pixels {
        final int width, height;
        final ByteBuffer rgba;
        int readers;
        Pixels(int width, int height, ByteBuffer rgba) { this.width = width; this.height = height; this.rgba = rgba; }
    }
    private record PixelPatch(int x, int y, int width, int height, ByteBuffer rgba, int rowStride) {
        boolean covers(PixelPatch other) {
            return x <= other.x && y <= other.y && (long) x + width >= (long) other.x + other.width
                && (long) y + height >= (long) other.y + other.height;
        }
        void apply(ByteBuffer target, int targetWidth) {
            for (int row = 0; row < height; row++) target.put(((y + row) * targetWidth + x) * 4,
                rgba, rgba.position() + row * rowStride, width * 4);
        }
    }
    private record PatchNode(PatchNode previous, PixelPatch patch) { }
    private record UploadPatch(int level, PixelPatch patch) { }
    private static final class Level {
        final Pixels pixels;
        final PatchNode tail;
        Level(Pixels pixels, PatchNode tail) { this.pixels = pixels; this.tail = tail; }
        private void applyPatches(ByteBuffer target) {
            var ordered = new ArrayList<PixelPatch>();
            for (PatchNode node = tail; node != null; node = node.previous) ordered.add(node.patch);
            for (int i = ordered.size() - 1; i >= 0; i--) ordered.get(i).apply(target, pixels.width);
        }
        ByteBuffer materialize() {
            if (tail == null) return pixels.rgba.duplicate();
            ByteBuffer result = BufferUtils.createByteBuffer(pixels.rgba.remaining()).put(pixels.rgba.duplicate()).flip();
            applyPatches(result);
            return result;
        }
        void compact() {
            if (pixels.readers != 0) throw new IllegalStateException("A captured pixel baseline cannot be mutated");
            applyPatches(pixels.rgba);
        }
        PixelPatch region(int x, int y, int width, int height) {
            int offset = (y * pixels.width + x) * 4;
            int length = (height - 1) * pixels.width * 4 + width * 4;
            return new PixelPatch(x, y, width, height, pixels.rgba.slice(offset, length), pixels.width * 4);
        }
    }
    private static final class Data {
        final Map<Integer, Level> levels = new HashMap<>();
        final List<UploadPatch> patches = new ArrayList<>();
        final Set<Snapshot> snapshots = new LinkedHashSet<>();
        TextureDesc descriptor;
        SamplerState sampler = SamplerState.NEAREST_REPEAT;
        int baseLevel, maxLevel = Integer.MAX_VALUE;
        long revision, imageRevision;
        boolean legacy, deleted;
        Snapshot latest, prepared;
        Data(TextureDesc descriptor) { this.descriptor = descriptor; }
        void changed(boolean pixels) { revision++; if (pixels) imageRevision++; }
        void compactLevels() {
            for (var entry : levels.entrySet()) {
                Level level = entry.getValue();
                if (level.tail != null && level.pixels.readers == 0) {
                    level.compact();
                    entry.setValue(new Level(level.pixels, null));
                }
            }
        }
        void patch(int level, PixelPatch patch) {
            if (patch.width == 0 || patch.height == 0) return;
            // Keep distant atlas updates separate; a later complete overwrite supersedes old payloads.
            patches.removeIf(old -> old.level == level && patch.covers(old.patch));
            patches.add(new UploadPatch(level, patch));
            int count = 0;
            for (UploadPatch upload : patches) if (upload.level == level) count++;
            if (count > 128) {
                Level image = levels.get(level);
                ByteBuffer pixels = image.materialize();
                patches.removeIf(old -> old.level == level);
                patches.add(new UploadPatch(level, new PixelPatch(0, 0, image.pixels.width, image.pixels.height,
                    pixels, image.pixels.width * 4)));
            }
        }
    }
}
