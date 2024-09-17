package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.Vec2;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.ptexture.DynamicTexture;
import net.minecraft.world.level.Level;

@Mixin(DynamicTexture.class)
public abstract class MixinTextureBinder implements AC_TextureBinder {

    @Shadow
    public byte[] pixels;
    @Shadow
    public int tex;
    @Shadow
    public boolean anaglyph3d;
    @Shadow
    public int textureId;

    @Unique
    public IntBuffer imageData;
    @Unique
    public int x;
    @Unique
    public int y;
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

    @Shadow
    public void tick() {
        throw new AssertionError();
    }

    @Overwrite
    public void bind(Textures var1) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, var1.loadTexture(this.getTexture()));
    }

    @Override
    public void setAtlasRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onTick(Vec2 size) {
        this.tick();
    }

    @Override
    public String getTexture() {
        if (this.textureId == 0) return "/terrain.png";
        //if (this.renderMode == 1) return "/gui/items.png";
        return "/gui/items.png";
    }

    @Override
    public IntBuffer getBufferAtCurrentFrame() {
        if (!this.hasImages) {
            return null;
        }

        int frameStart = this.curFrame * this.width * this.height;
        int frameEnd = frameStart + this.width * this.height;

        this.curFrame = (this.curFrame + 1) % this.numFrames;

        return this.imageData
            .position(frameStart)
            .limit(frameEnd);
    }

    @Override
    public void loadImage(String name, Level world) {
        BufferedImage image = null;
        if (world instanceof ExWorld exWorld) {
            image = exWorld.loadMapTexture(name);
        }

        loadImage(name, image);
    }

    @Override
    public void loadImage(String name, BufferedImage image) {
        this.hasImages = false;
        this.curFrame = 0;

        if (image != null) {
            this.width = image.getWidth();
            this.numFrames = image.getHeight() / image.getWidth();
            this.imageData = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
            getRgb(image, 0, 0, image.getWidth(), image.getHeight(), imageData, image.getWidth());
            this.imageData.clear();
            this.hasImages = true;
            this.pixels = new byte[width * width * 4];
        }
    }

    @Unique
    protected void getRgb(
        BufferedImage image, int startX, int startY, int w, int h, IntBuffer rgbArray, int scansize) {

        WritableRaster raster = image.getRaster();
        ColorModel colorModel = image.getColorModel();

        int nbands = raster.getNumBands();
        int dataType = raster.getDataBuffer().getDataType();
        Object data = switch (dataType) {
            case DataBuffer.TYPE_BYTE -> new byte[nbands];
            case DataBuffer.TYPE_USHORT -> new short[nbands];
            case DataBuffer.TYPE_INT -> new int[nbands];
            case DataBuffer.TYPE_FLOAT -> new float[nbands];
            case DataBuffer.TYPE_DOUBLE -> new double[nbands];
            default -> throw new IllegalArgumentException("Unknown data buffer type: " +
                dataType);
        };

        int padding = scansize - w;
        for (int y = startY; y < startY + h; y++) {
            for (int x = startX; x < startX + w; x++) {
                int color = colorModel.getRGB(raster.getDataElements(x, y, data));
                rgbArray.put(color);
            }

            if (padding > 0) {
                rgbArray.position(rgbArray.position() + padding);
            }
        }
    }

    @Unique
    protected void swapBgra(IntBuffer buffer) {
        buffer.mark();
        for (int i = buffer.position(); i < buffer.limit(); i++) {
            int color = buffer.get(i);
            color = (color >> 16 & 255) |
                ((color >> 8 & 255) << 8) |
                ((color & 255) << 16) |
                ((color >> 24 & 255) << 24);
            buffer.put(i, color);
        }
        buffer.reset();
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
