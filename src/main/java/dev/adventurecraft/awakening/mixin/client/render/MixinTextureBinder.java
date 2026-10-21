package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlAdapter;
import dev.adventurecraft.awakening.client.render.compat.LegacyGlConstants;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.client.render.texture.TextureAnimationFrames;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import dev.adventurecraft.awakening.layout.IntRect;
import dev.adventurecraft.awakening.layout.Size;
import org.lwjgl.BufferUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public abstract class MixinTextureBinder implements AC_TextureBinder {

    @Shadow public byte[] pixels = new byte[0];
    @Shadow public int tex;
    @Shadow public boolean anaglyph3d;
    @Shadow public int textureId;

    @Unique public ByteBuffer imageData;
    @Unique public int width;
    @Unique public int height;
    @Unique public int curFrame;
    @Unique public int numFrames;
    @Unique public boolean hasImages;
    @Unique private TextureAnimationFrames ac$normalFrames;
    @Unique private TextureAnimationFrames ac$specularFrames;

    public MixinTextureBinder() {
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void initData(CallbackInfo ci) {
        this.width = 16;
        this.height = 16;
        this.imageData = this.allocImageData(this.width, this.height);
    }

    @Shadow
    public void tick() {
        throw new AssertionError();
    }

    @Overwrite
    public void bind(Textures var1) {
        LegacyGlAdapter.bindTexture(LegacyGlConstants.TEXTURE_2D, var1.loadTexture(this.getTexture()));
    }

    @Override
    public void animate() {
        if (this.numFrames == 0) {
            this.curFrame = 0;
        }
        else {
            this.curFrame = (this.curFrame + 1) % this.numFrames;
        }
    }

    @Override
    public void onTick(Size size) {
        this.tick();
        this.animate();
    }

    @Override
    public String getTexture() {
        if (this.textureId == 0) {
            return "/terrain.png";
        }
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }

    @Override
    public IntRect getCurrentFrameRect() {
        int x = this.tex % 16 * this.width;
        int y = this.tex / 16 * this.height;
        return new IntRect(x, y, this.width, this.height);
    }

    @Override
    public Frame getCurrentFrame() {
        if (this.width < 1 || this.height < 1 || this.imageData == null) return EMPTY_FRAME;
        int frameSize = this.width * this.height * 4;
        int frameStart = this.curFrame * frameSize;
        if (frameSize > this.imageData.limit() - frameStart) {
            // TODO: warn about mismatched sizes
            return EMPTY_FRAME;
        }
        var data = this.imageData.slice(frameStart, frameSize);
        var image = ImageBuffer.wrap(data, this.width, this.height, ImageFormat.RGBA_U8);
        return new Frame(this.getCurrentFrameRect(), image);
    }

    @Override
    public boolean hasCustomImage() {
        return this.hasImages;
    }

    @Override
    public Frame getMaterialFrame(MaterialChannel channel) {
        if (!this.hasImages) return null;
        TextureAnimationFrames frames = switch (channel) {
            case NORMAL -> this.ac$normalFrames;
            case SPECULAR -> this.ac$specularFrames;
        };
        return frames == null ? null : new Frame(this.getCurrentFrameRect(), frames.frame(this.curFrame));
    }

    @Override
    public void loadImage(String name, Level world) {
        this.clearCompanions();
        ImageBuffer image = null;
        if (name != null && world instanceof ExWorld exWorld) {
            image = exWorld.loadMapTexture(name);
        }

        this.loadImage(name, image);
        this.loadCompanions(name, world, image);
    }

    @Override
    public void loadImage(String name, ImageBuffer image) {
        this.width = 16;
        this.height = 16;
        this.resetImageSource();
        if (image == null) return;
        try {
            this.loadFrames(TextureAnimationFrames.from(image));
        } catch (IllegalArgumentException error) {
            ACMod.LOGGER.warn("Ignoring invalid animation strip {}: {}", name, error.getMessage());
        }
    }

    /** Clear every optional source when a map or image changes, including failed loads. */
    @Unique
    protected void clearCompanions() {
        this.ac$normalFrames = null;
        this.ac$specularFrames = null;
    }

    /** Retain enough writable storage for procedural binders whose working arrays are already sized. */
    @Unique
    protected void resetImageSource() {
        this.clearCompanions();
        this.hasImages = false;
        this.curFrame = 0;
        this.numFrames = 0;
        int capacity = this.imageData == null ? 0 : this.imageData.capacity();
        this.imageData = BufferUtils.createByteBuffer(Math.max(16 * 16 * 4, capacity));
    }

    /** Legacy procedural subclasses require a writable, frame-major base buffer. */
    @Unique
    protected void loadFrames(TextureAnimationFrames frames) {
        this.width = frames.frameWidth();
        this.height = frames.frameHeight();
        this.numFrames = frames.frameCount();
        int frameBytes = Math.multiplyExact(Math.multiplyExact(this.width, this.height), 4);
        this.imageData = this.allocImageData(this.width, this.height, this.numFrames);
        for (int index = 0; index < this.numFrames; index++) {
            var output = this.imageData.slice(index * frameBytes, frameBytes).order(ByteOrder.nativeOrder());
            frames.frame(index).copyTo(output.asIntBuffer(), ImageFormat.RGBA_U8);
        }
        this.hasImages = true;
    }

    @Unique
    protected void loadCompanions(String name, Level world, ImageBuffer baseImage) {
        this.clearCompanions();
        if (!this.hasImages || baseImage == null || name == null || !(world instanceof ExWorld exWorld)) return;
        TextureAnimationFrames base = this.width == this.height
            ? TextureAnimationFrames.from(baseImage)
            : TextureAnimationFrames.from(baseImage, this.width, this.height);
        this.ac$normalFrames = this.loadCompanion(exWorld, name, "_n", base);
        this.ac$specularFrames = this.loadCompanion(exWorld, name, "_s", base);
    }

    @Unique
    private TextureAnimationFrames loadCompanion(ExWorld world, String name, String suffix, TextureAnimationFrames base) {
        String source = TextureAnimationFrames.siblingPath(name, suffix);
        ImageBuffer image = world.loadMapTexture(source);
        if (image == null && Minecraft.instance != null && Minecraft.instance.textures != null) {
            var textures = (ExTextureManager) Minecraft.instance.textures;
            try {
                if (textures.hasTexture(source)) image = textures.getTextureImage(source);
            } catch (IOException error) {
                ACMod.LOGGER.warn("Cannot load animated material companion {}: {}", source, error.getMessage());
            }
        }
        if (image == null) return null;
        try {
            if (image.getWidth() != base.sourceWidth() || image.getHeight() != base.sourceHeight()) {
                throw new IllegalArgumentException("source dimensions do not match the base animation");
            }
            TextureAnimationFrames frames = base.frameWidth() == base.frameHeight()
                ? TextureAnimationFrames.from(image)
                : TextureAnimationFrames.from(image, base.frameWidth(), base.frameHeight());
            if (!base.matchesLayout(frames)) {
                throw new IllegalArgumentException("frame layout does not match the base animation");
            }
            return frames;
        } catch (IllegalArgumentException error) {
            ACMod.LOGGER.warn("Ignoring animated material companion {}: {}", source, error.getMessage());
            return null;
        }
    }

    @Unique
    protected ByteBuffer allocImageData(int width, int height, int frameCount) {
        int capacity = width * height * frameCount * 4;
        return BufferUtils.createByteBuffer(capacity).clear();
    }

    @Unique
    protected ByteBuffer allocImageData(int width, int height) {
        return this.allocImageData(width, height, 1);
    }

    @Override
    public void setTileSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
