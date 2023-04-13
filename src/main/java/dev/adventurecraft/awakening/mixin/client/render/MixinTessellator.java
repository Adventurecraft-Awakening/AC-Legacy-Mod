package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.extension.client.render.ExTessellator;
import net.minecraft.client.render.Tessellator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

@Mixin(Tessellator.class)
public abstract class MixinTessellator implements ExTessellator {

    @Shadow
    private static boolean useTriangles;
    @Shadow
    private static boolean useVbo;
    @Shadow
    private ByteBuffer byteBuffer;
    @Shadow
    private IntBuffer intBuffer;
    @Shadow
    private FloatBuffer floatBuffer;
    @Shadow
    private int[] bufferArray;
    @Shadow
    private int vertexCount;
    @Shadow
    private double textureX;
    @Shadow
    private double textureY;
    @Shadow
    private int color;
    @Shadow
    private boolean hasColor;
    @Shadow
    private boolean hasTexture;
    @Shadow
    private boolean hasNormals;
    @Shadow
    private int bufferIndex;
    @Shadow
    private int vertexAmount;
    @Shadow
    private boolean disableColor;
    @Shadow
    private int drawingMode;
    @Shadow
    private double xOffset;
    @Shadow
    private double yOffset;
    @Shadow
    private double zOffset;
    @Shadow
    private int normal;
    @Shadow
    private boolean tessellating;
    @Shadow
    private boolean canUseVbo;
    @Shadow
    private IntBuffer vertexBuffer;
    @Shadow
    private int vboIndex;
    @Shadow
    private int vboCount;
    @Shadow
    private int bufferSize;

    private boolean renderingChunk = false;
    private int[] vertexIconIndex = new int[262144];
    private double[] quadXs = new double[4];
    private double[] quadYs = new double[4];
    private double[] quadZs = new double[4];
    private double[] quadUs = new double[4];
    private double[] quadVs = new double[4];
    private int[] quadColors = new int[4];
    private boolean[] drawnIcons = new boolean[256];

    @Shadow
    protected abstract void clear();

    @Shadow
    public abstract void setTextureXY(double d, double e);

    /*
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        useTriangles = false;
        bufferArray = null;
    }

    private void draw(int var1, int var2) {
        int var3 = var2 - var1;
        if (var3 > 0) {
            if (var3 % 4 == 0) {
                if (this.canUseVbo) {
                    throw new IllegalStateException("VBO not implemented");
                } else {
                    this.floatBuffer.position(3);
                    GL11.glTexCoordPointer(2, 32, (FloatBuffer) this.floatBuffer);
                    this.byteBuffer.position(20);
                    GL11.glColorPointer(4, true, 32, this.byteBuffer);
                    this.floatBuffer.position(0);
                    GL11.glVertexPointer(3, 32, (FloatBuffer) this.floatBuffer);
                    if (this.drawingMode == 7 && useTriangles) {
                        GL11.glDrawArrays(GL11.GL_TRIANGLES, var1, var3);
                    } else {
                        GL11.glDrawArrays(this.drawingMode, var1, var3);
                    }

                }
            }
        }
    }

    private int drawForIcon(int var1, int var2) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, terrainTextures[var1]);
        int var3 = -1;
        int var4 = -1;

        for (int var5 = var2; var5 < this.vertexAmount; ++var5) {
            int var6 = this.vertexIconIndex[var5];
            if (var6 == var1) {
                if (var4 < 0) {
                    var4 = var5;
                }
            } else if (var4 >= 0) {
                this.draw(var4, var5);
                var4 = -1;
                if (var3 < 0) {
                    var3 = var5;
                }
            }
        }

        if (var4 >= 0) {
            this.draw(var4, this.vertexAmount);
        }

        if (var3 < 0) {
            var3 = this.vertexAmount;
        }

        return var3;
    }

    @Overwrite
    public void tessellate() {
        if (!this.tessellating) {
            throw new IllegalStateException("Not tesselating!");
        } else {
            this.tessellating = false;
            if (!this.renderingChunk) {
                GL11.glEnd();
                this.checkOpenGlError();
            } else if (this.vertexCount > 0) {
                this.byteBuffer.position(0);
                this.byteBuffer.limit(this.bufferIndex * 4);
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                Arrays.fill(this.drawnIcons, false);
                int var1 = 0;

                for (int var2 = 0; var2 < this.vertexAmount; ++var2) {
                    int var3 = this.vertexIconIndex[var2];
                    if (!this.drawnIcons[var3]) {
                        var2 = this.drawForIcon(var3, var2) - 1;
                        ++var1;
                        this.drawnIcons[var3] = true;
                    }
                }

                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            }

            this.clear();
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void clearIntBuffer(CallbackInfo ci) {
        this.intBuffer.clear();
    }

    @Inject(method = "start(I)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/Tessellator;clear()V"))
    private void nonChunkBegin(int var1, CallbackInfo ci) {
        if (!this.renderingChunk) {
            GL11.glBegin(var1);
        }
    }

    @Inject(method = "color(IIII)V", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/render/Tessellator;hasColor:Z",
            shift = At.Shift.AFTER))
    private void nonChunkColor(int var1, int var2, int var3, int var4, CallbackInfo ci) {
        if (!this.renderingChunk) {
            GL11.glColor4ub((byte) var1, (byte) var2, (byte) var3, (byte) var4);
        }
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    private void chunkVertex(double var1, double var3, double var5, double var7, double var9, CallbackInfo ci) {
        if (this.renderingChunk) {
            int var11 = this.vertexAmount % 4;
            this.quadXs[var11] = var1;
            this.quadYs[var11] = var3;
            this.quadZs[var11] = var5;
            this.quadUs[var11] = var7;
            this.quadVs[var11] = var9;
            this.quadColors[var11] = this.color;
            if (var11 != 3) {
                ++this.vertexAmount;
            } else {
                this.vertexAmount -= 3;
                double var12 = (this.quadUs[0] + this.quadUs[1] + this.quadUs[2] + this.quadUs[3]) / 4.0D;
                double var14 = (this.quadVs[0] + this.quadVs[1] + this.quadVs[2] + this.quadVs[3]) / 4.0D;
                if (var12 > 0.875D && var12 < 1.0D && var14 > 0.75D && var14 < 0.875D) {
                    boolean var16 = true;
                }

                int var26 = (int) (var12 * 16.0D);
                int var17 = (int) (var14 * 16.0D);
                int var18 = var17 * 16 + var26;
                double var19 = (double) var26 / 16.0D;
                double var21 = (double) var17 / 16.0D;
                int var23 = this.vertexAmount;

                for (int var24 = 0; var24 < 4; ++var24) {
                    var1 = this.quadXs[var24];
                    var3 = this.quadYs[var24];
                    var5 = this.quadZs[var24];
                    var7 = this.quadUs[var24];
                    var9 = this.quadVs[var24];
                    this.vertexIconIndex[var23 + var24] = var18;
                    var7 -= var19;
                    var9 -= var21;
                    var7 *= 16.0D;
                    var9 *= 16.0D;
                    int var25 = this.color;
                    this.color = this.quadColors[var24];
                    this.setTextureXY(var7, var9);
                    this.addVertex(var1, var3, var5);
                    this.color = var25;
                }

            }
            ci.cancel();
        }
    }

    @Overwrite
    public void addVertex(double var1, double var3, double var5) {
        if (!this.renderingChunk) {
            GL11.glVertex3f((float) (var1 + this.xOffset), (float) (var3 + this.yOffset), (float) (var5 + this.zOffset));
        } else {
            ++this.vertexAmount;
            if (this.drawingMode == 7 && useTriangles && this.vertexAmount % 4 == 0) {
                for (int var7 = 0; var7 < 2; ++var7) {
                    int var8 = 8 * (3 - var7);
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 0));
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 1));
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 2));
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 3));
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 4));
                    this.intBuffer.put(this.intBuffer.get(this.bufferIndex - var8 + 5));
                    this.intBuffer.put(0);
                    this.intBuffer.put(0);
                    ++this.vertexCount;
                    this.bufferIndex += 8;
                }
            }

            this.intBuffer.put(Float.floatToRawIntBits((float) (var1 + this.xOffset)));
            this.intBuffer.put(Float.floatToRawIntBits((float) (var3 + this.yOffset)));
            this.intBuffer.put(Float.floatToRawIntBits((float) (var5 + this.zOffset)));
            this.intBuffer.put(Float.floatToRawIntBits((float) this.textureX));
            this.intBuffer.put(Float.floatToRawIntBits((float) this.textureY));
            this.intBuffer.put(this.color);
            this.intBuffer.put(0);
            this.intBuffer.put(0);
            this.bufferIndex += 8;
            ++this.vertexCount;
            if (this.renderingChunk && this.vertexAmount % 4 == 0 && this.bufferIndex >= this.bufferSize - 32) {
                this.tessellate();
                this.tessellating = true;
            }

        }
    }

    @Inject(method = "setNormal", at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void nonChunkNormal(float var1, float var2, float var3, CallbackInfo ci, int var4, int var5, int var6) {
        if (!this.renderingChunk) {
            GL11.glNormal3b((byte) var4, (byte) var5, (byte) var6);
        }
    }

    @Inject(method = "setTextureXY", at = @At("TAIL"))
    private void nonChunkTexCoord(double var1, double var2, CallbackInfo ci) {
        if (!this.renderingChunk) {
            GL11.glTexCoord2f((float) var1, (float) var2);
        }
    }

     */

    public void setRenderingChunk(boolean var1) {
        this.renderingChunk = var1;
    }

    private void checkOpenGlError() {
        int var1 = GL11.glGetError();
        if (var1 != 0) {
            String var2 = "OpenGL Error: " + var1 + " " + Util.translateGLErrorString(var1);
            Exception var3 = new Exception(var2);
            ACMod.LOGGER.error(var2, var3);
        }
    }
}
