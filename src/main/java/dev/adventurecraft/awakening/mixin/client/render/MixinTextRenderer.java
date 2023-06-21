package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.client.options.Config;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.QuadPoint;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.entity.ChickenRenderer;
import net.minecraft.client.resource.TexturePack;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.CharacterUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Formatter;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer implements ExTextRenderer {

    private int[] field_22009_h = new int[32];
    private byte[] charTexWidths = new byte[256];
    private byte[] charPixelWidths = new byte[256];
    private byte[] unicodeWidth = new byte[65536];
    private int[] charTexIds = new int[256];
    private int basicTexID;
    private int lastBoundTexID;
    private TextureManager tex;
    private float xPos;
    private float yPos;
    private int imgWidth = 128;
    private int imgHeight = 128;
    private int b = 8;
    private int charHeight = 8;
    private String textureName;
    private GameOptions gameSettings;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(GameOptions var1, String var2, TextureManager var3, CallbackInfo ci) {
        this.tex = var3;
        this.textureName = var2;
        this.gameSettings = var1;

        this.init();
    }

    private void init() {
        BufferedImage var1;
        try {
            InputStream var2;
            if (Minecraft.instance != null) {
                TexturePack texturePack = Minecraft.instance.texturePackManager.texturePack;
                var1 = ImageIO.read(texturePack.getResourceAsStream(this.textureName));
                var2 = texturePack.getResourceAsStream("/font/glyph_sizes.bin");
            } else {
                var1 = ImageIO.read(ChickenRenderer.class.getResourceAsStream(this.textureName));
                var2 = ChickenRenderer.class.getResourceAsStream("/font/glyph_sizes.bin");
            }
            if (var2 != null) {
                var2.read(this.unicodeWidth);
            }
        } catch (IOException var11) {
            throw new RuntimeException(var11);
        }

        this.imgWidth = var1.getWidth();
        this.imgHeight = var1.getHeight();
        this.b = this.imgWidth / 16;
        this.charHeight = this.imgHeight / 16;
        int[] var12 = new int[this.imgWidth * this.imgHeight];
        var1.getRGB(0, 0, this.imgWidth, this.imgHeight, var12, 0, this.imgWidth);

        int var3;
        int var4;
        int var5;
        int var6;
        int var7;
        int var9;
        int var10;
        for (var3 = 0; var3 < 256; ++var3) {
            var4 = var3 % 16;
            var5 = var3 / 16;

            for (var6 = this.b - 1; var6 >= 0; --var6) {
                var7 = var4 * this.b + var6;
                boolean var8 = true;

                for (var9 = 0; var9 < this.charHeight && var8; ++var9) {
                    var10 = var12[var7 + (var5 * this.charHeight + var9) * this.imgWidth] & 255;
                    if (var10 > 0) {
                        var8 = false;
                    }
                }

                if (!var8) {
                    break;
                }
            }

            if (var3 == 32) {
                var6 = 2;
            }

            this.charTexWidths[var3] = (byte) (var6 + 2);
            this.charPixelWidths[var3] = (byte) ((var6 + 2) * 128 / this.imgWidth);
        }

        this.basicTexID = this.tex.getTextureId(var1);

        for (var3 = 0; var3 < 32; ++var3) {
            var4 = (var3 >> 3 & 1) * 85;
            var5 = (var3 >> 2 & 1) * 170 + var4;
            var6 = (var3 >> 1 & 1) * 170 + var4;
            var7 = (var3 >> 0 & 1) * 170 + var4;
            if (var3 == 6) {
                var5 += 85;
            }

            if (this.gameSettings.anaglyph3d) {
                int var13 = (var5 * 30 + var6 * 59 + var7 * 11) / 100;
                var9 = (var5 * 30 + var6 * 70) / 100;
                var10 = (var5 * 30 + var7 * 70) / 100;
                var5 = var13;
                var6 = var9;
                var7 = var10;
            }

            if (var3 >= 16) {
                var5 /= 4;
                var6 /= 4;
                var7 /= 4;
            }

            this.field_22009_h[var3] = (var5 & 255) << 16 | (var6 & 255) << 8 | var7 & 255;
        }
    }

    private void func_22002_b(int var1) {
        float var2 = (float) (var1 % 16 * this.b);
        float var3 = (float) (var1 / 16 * this.charHeight);
        if (this.lastBoundTexID != this.basicTexID) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.basicTexID);
            this.lastBoundTexID = this.basicTexID;
        }

        float var4 = (float) this.charTexWidths[var1] - 0.01F;
        float var5 = this.charPixelWidths[var1];
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(var2 / (float) this.imgWidth, var3 / (float) this.imgHeight);
        GL11.glVertex3f(this.xPos, this.yPos, 0.0F);
        GL11.glTexCoord2f(var2 / (float) this.imgWidth, (var3 + (float) this.charHeight) / (float) this.imgHeight);
        GL11.glVertex3f(this.xPos, this.yPos + 8.0F, 0.0F);
        GL11.glTexCoord2f((var2 + var4) / (float) this.imgWidth, var3 / (float) this.imgHeight);
        GL11.glVertex3f(this.xPos + var5, this.yPos, 0.0F);
        GL11.glTexCoord2f((var2 + var4) / (float) this.imgWidth, (var3 + (float) this.charHeight) / (float) this.imgHeight);
        GL11.glVertex3f(this.xPos + var5, this.yPos + 8.0F, 0.0F);
        GL11.glEnd();
        this.xPos += var5;
    }

    private void func_22003_b(int var1) {
        StringBuilder var2 = new StringBuilder();
        (new Formatter(var2)).format("/font/glyph_%02X.png", var1);

        BufferedImage var3;
        try {
            if (Minecraft.instance != null) {
                var3 = ImageIO.read(Minecraft.instance.texturePackManager.texturePack.getResourceAsStream(var2.toString()));
            } else {
                var3 = ImageIO.read(QuadPoint.class.getResourceAsStream(var2.toString()));
            }
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        this.charTexIds[var1] = this.tex.getTextureId(var3);
        this.lastBoundTexID = this.charTexIds[var1];
    }

    private void func_22004_c(char var1) {
        if (this.unicodeWidth[var1] == 0) {
            return;
        }

        int var2 = var1 / 256;
        if (this.charTexIds[var2] == 0) {
            this.func_22003_b(var2);
        }

        if (this.lastBoundTexID != this.charTexIds[var2]) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.charTexIds[var2]);
            this.lastBoundTexID = this.charTexIds[var2];
        }

        int var3 = this.unicodeWidth[var1] >> 4;
        int var4 = this.unicodeWidth[var1] & 15;
        float var5;
        float var6;
        if (var4 > 7) {
            var6 = 16.0F;
            var5 = 0.0F;
        } else {
            var6 = (float) (var4 + 1);
            var5 = (float) var3;
        }

        float var7 = (float) (var1 % 16 * 16) + var5;
        float var8 = (float) ((var1 & 255) / 16 * 16);
        float var9 = var6 - var5 - 0.02F;

        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f(var7 / 256.0F, var8 / 256.0F);
        GL11.glVertex3f(this.xPos, this.yPos, 0.0F);
        GL11.glTexCoord2f(var7 / 256.0F, (var8 + 15.98F) / 256.0F);
        GL11.glVertex3f(this.xPos, this.yPos + 7.99F, 0.0F);
        GL11.glTexCoord2f((var7 + var9) / 256.0F, var8 / 256.0F);
        GL11.glVertex3f(this.xPos + var9 / 2.0F, this.yPos, 0.0F);
        GL11.glTexCoord2f((var7 + var9) / 256.0F, (var8 + 15.98F) / 256.0F);
        GL11.glVertex3f(this.xPos + var9 / 2.0F, this.yPos + 7.99F, 0.0F);
        GL11.glEnd();
        this.xPos += (var6 - var5) / 2.0F + 1.0F;
    }

    private void renderStringImpl(String var1, boolean var2) {
        for (int var3 = 0; var3 < var1.length(); ++var3) {
            char var4 = var1.charAt(var3);
            int var5;
            if (var4 == 167 && var3 + 1 < var1.length()) {
                var5 = "0123456789abcdef".indexOf(var1.toLowerCase().charAt(var3 + 1));
                if (var5 < 0) {
                    var5 = 15;
                }

                if (var2) {
                    var5 += 16;
                }

                int var6 = this.field_22009_h[var5];
                GL11.glColor3f((float) (var6 >> 16) / 255.0F, (float) (var6 >> 8 & 255) / 255.0F, (float) (var6 & 255) / 255.0F);
                ++var3;
            } else {
                var5 = CharacterUtils.validCharacters.indexOf(var4);
                if (var4 == 32) {
                    this.xPos += 4.0F;
                } else if (var5 > 0) {
                    this.func_22002_b(var5 + 32);
                } else {
                    this.func_22004_c(var4);
                }
            }
        }
    }

    @Overwrite
    public void drawText(String var1, int var2, int var3, int var4, boolean var5) {
        this.checkUpdated();
        if (var1 == null) {
            return;
        }

        this.lastBoundTexID = 0;
        if ((var4 & -16777216) == 0) {
            var4 |= -16777216;
        }

        if (var5) {
            var4 = (var4 & 16579836) >> 2 | var4 & -16777216;
        }

        GL11.glColor4f((float) (var4 >> 16 & 255) / 255.0F, (float) (var4 >> 8 & 255) / 255.0F, (float) (var4 & 255) / 255.0F, (float) (var4 >> 24 & 255) / 255.0F);
        this.xPos = (float) var2;
        this.yPos = (float) var3;
        this.renderStringImpl(var1, var5);
    }

    @Overwrite
    public int getTextWidth(String var1) {
        this.checkUpdated();
        if (var1 == null) {
            return 0;
        } else {
            int var2 = 0;

            for (int var3 = 0; var3 < var1.length(); ++var3) {
                char var4 = var1.charAt(var3);
                if (var4 == 167) {
                    ++var3;
                } else {
                    int var5 = CharacterUtils.validCharacters.indexOf(var4);
                    if (var5 >= 0) {
                        var2 += this.charPixelWidths[var5 + 32];
                    } else if (this.unicodeWidth[var4] != 0) {
                        int var6 = this.unicodeWidth[var4] >> 4;
                        int var7 = this.unicodeWidth[var4] & 15;
                        if (var7 > 7) {
                            var7 = 15;
                            var6 = 0;
                        }

                        ++var7;
                        var2 += (var7 - var6) / 2 + 1;
                    }
                }
            }

            return var2;
        }
    }

    private void checkUpdated() {
        if (!Config.isFontRendererUpdated()) {
            this.init();
            Config.setFontRendererUpdated(true);
        }
    }

    @Inject(method = "method_1902", at = @At("HEAD"))
    private void checkUpdatedAtLayout1(String var1, int var2, CallbackInfoReturnable<Integer> cir) {
        this.checkUpdated();
    }

    @Inject(method = "method_1904", at = @At("HEAD"))
    private void checkUpdatedAtLayout2(String var1, int var2, int var3, int var4, int var5, CallbackInfo ci) {
        this.checkUpdated();
    }

    @ModifyConstant(method = {"method_1902", "method_1904"}, constant = @Constant(intValue = 8))
    private int replaceConstWithCharHeight(int constant) {
        return this.charHeight;
    }
}