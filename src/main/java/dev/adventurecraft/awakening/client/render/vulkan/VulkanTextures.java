package dev.adventurecraft.awakening.client.render.vulkan;

import dev.adventurecraft.awakening.client.render.state.SamplerState;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkSamplerCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import static dev.adventurecraft.awakening.client.render.vulkan.VulkanDevice.check;
import static org.lwjgl.vulkan.VK10.*;

/** Native texture snapshots, sampled views, and samplers owned by one renderer. */
final class VulkanTextures implements AutoCloseable {
    private static final int FORMAT = VK_FORMAT_R8G8B8A8_UNORM;
    private static final int USAGE = VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT |
        VK_IMAGE_USAGE_TRANSFER_SRC_BIT;

    private final VulkanDevice context;
    private final VulkanBindings bindings;
    private final VulkanArena staging;
    private final VulkanUploadBatch uploads;
    private final Consumer<Runnable> retire;
    private final LongConsumer uploadedBytes;
    private final Set<Texture> textures = new HashSet<>();
    private final Map<SamplerState, Long> samplers = new HashMap<>();
    private boolean closed;

    VulkanTextures(VulkanDevice context, VulkanBindings bindings, VulkanArena staging, VulkanUploadBatch uploads,
                   Consumer<Runnable> retire, LongConsumer uploadedBytes) {
        this.context = context;
        this.bindings = bindings;
        this.staging = staging;
        this.uploads = uploads;
        this.retire = retire;
        this.uploadedBytes = uploadedBytes;
    }

    Texture upload(int width, int height, int mipLevels, ByteBuffer rgba,
                   List<ByteBuffer> suppliedLevels, SamplerState samplerState) {
        ensureOpen();
        int baseBytes = pixelBytes(width, height);
        if (rgba.remaining() != baseBytes) {
            throw new IllegalArgumentException("RGBA texture length does not match its dimensions");
        }
        boolean explicitLevels = suppliedLevels != null && !suppliedLevels.isEmpty();
        int maximumLevels = 32 - Integer.numberOfLeadingZeros(Math.max(width, height));
        Objects.requireNonNull(samplerState);
        if (mipLevels < 1 || mipLevels > maximumLevels) throw new IllegalArgumentException("Invalid texture mip count");
        if (explicitLevels && suppliedLevels.size() != mipLevels) throw new IllegalArgumentException("Supplied mip chain must match allocation");
        int uploadLevels = explicitLevels ? mipLevels : 1;
        long stagingBytes = 0;
        for (int level = 0; level < uploadLevels; level++) {
            ByteBuffer pixels = explicitLevels ? suppliedLevels.get(level) : rgba;
            int expected = pixelBytes(Math.max(1, width >> level), Math.max(1, height >> level));
            if (pixels == null || pixels.remaining() != expected) {
                throw new IllegalArgumentException("RGBA texture mip length does not match level " + level);
            }
            stagingBytes = Math.addExact(stagingBytes, expected);
        }

        VulkanImage image = null;
        VulkanArena.Slice staged = null;
        Texture result = null;
        try {
            staged = staging.allocate(stagingBytes, 4);
            uploads.retain(staged);
            uploadedBytes.accept(stagingBytes);
            long offset = 0;
            for (int level = 0; level < uploadLevels; level++) {
                ByteBuffer pixels = explicitLevels ? suppliedLevels.get(level) : rgba;
                staged.pageUpload(offset, pixels);
                offset += pixels.remaining();
            }
            image = createImage(width, height, mipLevels);
            result = texture(image, new VulkanTextureChanges(width, height, mipLevels), samplerState, 0, mipLevels - 1);
            uploads.texture(image, staged, uploadLevels, explicitLevels);
            return result;
        } catch (RuntimeException | Error error) {
            if (staged != null) uploads.discard(staged);
            if (result != null) result.release();
            else if (image != null) image.close();
            throw error;
        }
    }

    Texture withSampler(Texture source, SamplerState samplerState) {
        requireTexture(source);
        return texture(source.image, source.changes, samplerState, source.baseLevel, source.maxLevel);
    }

    Texture withSampler(Texture source, SamplerState samplerState, int baseLevel, int maxLevel) {
        requireTexture(source);
        return texture(source.image, source.changes, samplerState, baseLevel, maxLevel);
    }

    /** The caller requests a copy when a queued draw or CPU snapshot still needs the old contents. */
    Texture update(Texture source, List<VulkanRenderer.TextureRegion> regions, boolean regenerateMipmaps,
                   boolean preserveSource, long submission) {
        requireTexture(source);
        if (submission < 0) throw new IllegalArgumentException("Texture update submission must be nonnegative");
        List<VulkanRenderer.TextureRegion> updates = List.copyOf(regions);
        if (updates.isEmpty()) return texture(source.image, source.changes, source.samplerKey, source.baseLevel, source.maxLevel);
        for (var region : updates) {
            int level = region.level();
            if (level < 0 || level >= source.image.mipLevels || region.x() < 0 || region.y() < 0 ||
                region.width() <= 0 || region.height() <= 0 ||
                (long) region.x() + region.width() > Math.max(1, source.width() >> level) ||
                (long) region.y() + region.height() > Math.max(1, source.height() >> level) ||
                region.rowStrideBytes() < Math.multiplyExact(region.width(), 4) || region.rowStrideBytes() % 4 != 0 ||
                region.rgba().remaining() < Math.addExact(Math.multiplyExact((long) region.height() - 1,
                    region.rowStrideBytes()), Math.multiplyExact(region.width(), 4))) {
                throw new IllegalArgumentException("Texture update exceeds its mip allocation");
            }
        }

        VulkanImage image = preserveSource ? createImage(source.width(), source.height(), source.image.mipLevels) : source.image;
        List<VulkanArena.Slice> staged = new ArrayList<>();
        Texture result = null;
        try {
            VulkanTextureChanges changes = preserveSource ? source.changes.copy() : source.changes;
            for (var region : updates) staged.add(stageRegion(region));
            result = texture(image, changes, source.samplerKey, source.baseLevel, source.maxLevel);
            List<VulkanUploadBatch.Patch> patches = new ArrayList<>(updates.size());
            for (int i = 0; i < updates.size(); i++) {
                var region = updates.get(i);
                patches.add(new VulkanUploadBatch.Patch(region.level(), region.x(), region.y(), region.width(),
                    region.height(), staged.get(i)));
            }
            uploads.patch(source.image, image, patches, preserveSource, regenerateMipmaps);
            // Stamp only after validation, native allocations, staging and queue preparation succeeded.
            for (var region : updates)
                changes.mark(region.level(), region.x(), region.y(), region.width(), region.height(), submission);
            return result;
        } catch (RuntimeException | Error error) {
            for (var pixels : staged) uploads.discard(pixels);
            if (result != null) result.release();
            else if (preserveSource) image.close();
            throw error;
        }
    }

    private VulkanArena.Slice stageRegion(VulkanRenderer.TextureRegion region) {
        int rowBytes = Math.multiplyExact(region.width(), 4);
        int bytes = Math.multiplyExact(rowBytes, region.height());
        var slice = staging.allocate(bytes, 4);
        try {
            ByteBuffer source = region.rgba().duplicate();
            int start = source.position();
            for (int row = 0; row < region.height(); row++) {
                source.position(Math.addExact(start, Math.multiplyExact(row, region.rowStrideBytes())));
                source.limit(Math.addExact(source.position(), rowBytes));
                slice.pageUpload((long) row * rowBytes, source);
                source.limit(region.rgba().limit());
            }
            uploads.retain(slice);
            uploadedBytes.accept(bytes);
            return slice;
        } catch (RuntimeException | Error error) {
            slice.close();
            throw error;
        }
    }

    void requireTexture(Texture texture) {
        ensureOpen();
        if (texture == null || texture.closed || !textures.contains(texture)) {
            throw new IllegalArgumentException("Texture is closed or belongs to another renderer");
        }
    }

    private Texture texture(VulkanImage image, VulkanTextureChanges changes, SamplerState samplerKey, int baseLevel, int maxLevel) {
        int base = Math.clamp(baseLevel, 0, image.mipLevels - 1);
        int max = Math.clamp(maxLevel, base, image.mipLevels - 1);
        long view = image.view;
        long descriptor = VK_NULL_HANDLE;
        try (var stack = MemoryStack.stackPush()) {
            var handle = stack.mallocLong(1);
            if (base != 0 || max != image.mipLevels - 1) {
                var viewInfo = VkImageViewCreateInfo.calloc(stack).sType$Default().image(image.handle)
                    .viewType(VK_IMAGE_VIEW_TYPE_2D).format(FORMAT);
                viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT).baseMipLevel(base)
                    .levelCount(max - base + 1).layerCount(1);
                check(vkCreateImageView(context.device(), viewInfo, null, handle), "create texture mip view");
                view = handle.get(0);
            }
            descriptor = bindings.allocateTexture();
            var info = VkDescriptorImageInfo.calloc(1, stack).sampler(sampler(samplerKey)).imageView(view)
                .imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            var write = VkWriteDescriptorSet.calloc(1, stack).sType$Default().dstSet(descriptor).dstBinding(0)
                .descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1).pImageInfo(info);
            vkUpdateDescriptorSets(context.device(), write, null);
            Texture texture = new Texture(image, changes, descriptor, view, samplerKey, base, max);
            image.retain();
            textures.add(texture);
            return texture;
        } catch (RuntimeException | Error error) {
            if (descriptor != VK_NULL_HANDLE) bindings.recycleTexture(descriptor);
            if (view != image.view) vkDestroyImageView(context.device(), view, null);
            throw error;
        }
    }

    private VulkanImage createImage(int width, int height, int mipLevels) {
        return VulkanImage.create(context, width, height, FORMAT, USAGE, VK_IMAGE_ASPECT_COLOR_BIT, mipLevels);
    }

    private long sampler(SamplerState state) {
        return samplers.computeIfAbsent(state, value -> {
            try (var stack = MemoryStack.stackPush()) {
                var info = VkSamplerCreateInfo.calloc(stack).sType$Default()
                    .magFilter(value.mag() == SamplerState.Filter.LINEAR ? VK_FILTER_LINEAR : VK_FILTER_NEAREST)
                    .minFilter(value.min() == SamplerState.Filter.LINEAR ? VK_FILTER_LINEAR : VK_FILTER_NEAREST)
                    .mipmapMode(value.mipmap() == SamplerState.MipmapMode.LINEAR ? VK_SAMPLER_MIPMAP_MODE_LINEAR : VK_SAMPLER_MIPMAP_MODE_NEAREST)
                    .addressModeU(addressMode(value.wrapU())).addressModeV(addressMode(value.wrapV()))
                    .addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT).minLod(0)
                    .maxLod(value.mipmap() == SamplerState.MipmapMode.NONE ? 0 : 32).maxAnisotropy(1);
                var result = stack.mallocLong(1);
                check(vkCreateSampler(context.device(), info, null, result), "create texture sampler");
                return result.get(0);
            }
        });
    }

    private static int addressMode(SamplerState.AddressMode value) {
        return switch (value) {
            case REPEAT -> VK_SAMPLER_ADDRESS_MODE_REPEAT;
            case MIRRORED_REPEAT -> VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
            case CLAMP_TO_EDGE -> VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
            case CLAMP_TO_BORDER -> VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
        };
    }

    private static int pixelBytes(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Texture dimensions must be positive");
        return Math.multiplyExact(Math.multiplyExact(width, height), 4);
    }

    private void ensureOpen() {
        if (closed) throw new IllegalStateException("Vulkan textures are closed");
    }

    final class Texture implements AutoCloseable {
        private final VulkanImage image;
        private final VulkanTextureChanges changes;
        private final long descriptor;
        private final long view;
        private final SamplerState samplerKey;
        private final int baseLevel;
        private final int maxLevel;
        private boolean closed;
        private boolean released;

        private Texture(VulkanImage image, VulkanTextureChanges changes, long descriptor, long view, SamplerState samplerKey, int baseLevel, int maxLevel) {
            this.image = image;
            this.changes = changes;
            this.descriptor = descriptor;
            this.view = view;
            this.samplerKey = samplerKey;
            this.baseLevel = baseLevel;
            this.maxLevel = maxLevel;
        }

        VulkanImage image() { return image; }
        long descriptor() { return descriptor; }
        int width() { return image.width; }
        int height() { return image.height; }
        int sampledWidth() { return Math.max(1, image.width >> baseLevel); }
        int sampledHeight() { return Math.max(1, image.height >> baseLevel); }
        SamplerState samplerState() { return samplerKey; }
        void writeReactiveTiles(ByteBuffer target, int offset, long submission) { changes.writeReactiveTiles(target, offset, submission); }
        void appendReactiveTiles(ByteBuffer target, int offset, long submission) { changes.appendReactiveTiles(target, offset, submission); }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            retire.accept(this::release);
        }

        private void release() {
            if (released) return;
            released = true;
            closed = true;
            textures.remove(this);
            bindings.recycleTexture(descriptor);
            if (view != image.view) vkDestroyImageView(context.device(), view, null);
            image.release();
        }
    }

    /** All GPU work and upload retirement callbacks must complete before closing native textures. */
    @Override
    public void close() {
        if (closed) return;
        closed = true;
        for (Texture texture : List.copyOf(textures)) texture.release();
        for (long sampler : samplers.values()) vkDestroySampler(context.device(), sampler, null);
        samplers.clear();
    }
}
