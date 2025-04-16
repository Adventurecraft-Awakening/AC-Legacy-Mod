package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.image.ImageBuffer;
import dev.adventurecraft.awakening.image.ImageFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.nio.IntBuffer;

import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public abstract class MixinTextureBinder implements AC_TextureBinder {

    @Shadow
    public byte[] pixels = new byte[0];
    @Shadow
    public int tex;
    @Shadow
    public boolean anaglyph3d;
    @Shadow
    public int textureId;

    @Unique
    public IntBuffer imageData;
    @Unique
    public int width;
    @Unique
    public int height;
    @Unique
    public int curFrame;
    @Unique
    public int numFrames;
    @Unique
    public boolean hasImages;

    public MixinTextureBinder() {
    }

    @Inject(method = "<init>", at = @At("TAIL"))
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.loadTexture(this.getTexture()));
    }

    @Override
    public void animate() {
        if (this.numFrames == 0) {
            this.curFrame = 0;
        } else {
            this.curFrame = (this.curFrame + 1) % this.numFrames;
        }
    }

    @Override
    public void onTick(Vec2 size) {
        this.tick();
        this.animate();
    }

    @Override
    public String getTexture() {
        if (this.textureId == 0) return "/terrain.png";
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }

    @Override
    public IntBuffer getBufferAtCurrentFrame() {
        int frameSize = this.width * this.height;
        int frameStart = this.curFrame * frameSize;
        return this.imageData.slice(frameStart, frameSize);
    }

    @Override
    public void loadImage(String name, Level world) {
        ImageBuffer image = null;
        if (world instanceof ExWorld exWorld) {
            image = exWorld.loadMapTexture(name);
        }

        loadImage(name, image);
    }

    @Override
    public void loadImage(String name, ImageBuffer image) {
        this.hasImages = false;
        this.curFrame = 0;
        this.width = 16;
        this.height = 16;

        if (image != null) {
            int imgW = image.getWidth();
            int imgH = image.getHeight();
            int minSize = Math.min(imgH, imgW);
            int maxSize = Math.max(imgH, imgW);
            this.width = minSize;
            this.height = minSize;
            this.numFrames = maxSize / minSize;

            int capacity = minSize * maxSize * 4;
            var imageBuffer = BufferUtils.createByteBuffer(capacity).clear();
            this.imageData = imageBuffer.asIntBuffer();

            if (imgW > imgH) {
                var dstImage = ImageBuffer.wrap(imageBuffer, imgH, imgW, ImageFormat.RGBA_U8);

                // The only texture in the game needing this sprite transpose is the fan texture.
                for (int y = 0; y < minSize; y++) {
                    var srcRow = image.getRowSlice(y).asIntBuffer();
                    for (int i = 0; i < this.numFrames; i++) {
                        var srcSpan = srcRow.slice(i * minSize, minSize);
                        var dstRow = dstImage.getRowSlice(y + i * minSize);
                        dstRow.asIntBuffer().put(srcSpan);
                    }
                }
            } else {
                image.copyTo(this.imageData, ImageFormat.RGBA_U8);
            }
            this.hasImages = true;
        }
    }

    @Unique
    protected IntBuffer allocImageData(int width, int height, int frameCount) {
        int capacity = width * height * frameCount;
        return BufferUtils.createIntBuffer(capacity).clear();
    }

    @Unique
    protected IntBuffer allocImageData(int width, int height) {
        return this.allocImageData(width, height, 1);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }
}
