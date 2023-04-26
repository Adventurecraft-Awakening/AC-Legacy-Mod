package dev.adventurecraft.awakening.mixin.client.render;

import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.util.CharacterUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.IntBuffer;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer2 implements ExTextRenderer {

    @Shadow
    private int[] field_2462;

    @Shadow
    public int field_2461;

    @Shadow
    private int field_2463;

    @Shadow
    private IntBuffer field_2464;

    @Overwrite
    public void drawTextWithShadow(String var1, int var2, int var3, int var4) {
        this.drawText(var1, var2 + 1, var3 + 1, var4, true);
        this.drawText(var1, var2, var3, var4);
    }

    public void drawStringWithShadow(String var1, float var2, float var3, int var4) {
        this.renderString(var1, var2 + 1.0F, var3 + 1.0F, var4, true);
        this.drawString(var1, var2, var3, var4);
    }

    @Overwrite
    public void drawText(String var1, int var2, int var3, int var4) {
        this.drawText(var1, var2, var3, var4, false);
    }

    public void drawString(String var1, float var2, float var3, int var4) {
        this.renderString(var1, var2, var3, var4, false);
    }

    @Overwrite
    public void drawText(String var1, int var2, int var3, int var4, boolean var5) {
        this.renderString(var1, (float) var2, (float) var3, var4, var5);
    }

    public void renderString(String var1, float var2, float var3, int var4, boolean var5) {
        if (var1 != null) {
            if (var5) {
                int var6 = var4 & -16777216;
                var4 = (var4 & 16579836) >> 2;
                var4 += var6;
            }

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.field_2461);
            float var13 = (float) (var4 >> 16 & 255) / 255.0F;
            float var7 = (float) (var4 >> 8 & 255) / 255.0F;
            float var8 = (float) (var4 & 255) / 255.0F;
            float var9 = (float) (var4 >> 24 & 255) / 255.0F;
            if (var9 == 0.0F) {
                var9 = 1.0F;
            }

            GL11.glColor4f(var13, var7, var8, var9);
            this.field_2464.clear();
            GL11.glPushMatrix();
            GL11.glTranslatef(var2, var3, 0.0F);

            for (int var10 = 0; var10 < var1.length(); ++var10) {
                int var11;
                for (; var1.length() > var10 + 1 && var1.charAt(var10) == 167; var10 += 2) {
                    var11 = "0123456789abcdef".indexOf(var1.toLowerCase().charAt(var10 + 1));
                    if (var11 < 0 || var11 > 15) {
                        var11 = 15;
                    }

                    this.field_2464.put(this.field_2463 + 256 + var11 + (var5 ? 16 : 0));
                    if (this.field_2464.remaining() == 0) {
                        this.field_2464.flip();
                        GL11.glCallLists(this.field_2464);
                        this.field_2464.clear();
                    }
                }

                if (var10 < var1.length()) {
                    var11 = CharacterUtils.validCharacters.indexOf(var1.charAt(var10));
                    char var12 = var1.charAt(var10);
                    if (var11 >= 0 && var12 < 176) {
                        this.field_2464.put(this.field_2463 + var11 + 32);
                    } else if (var12 < 256) {
                        this.field_2464.put(this.field_2463 + var12);
                    }
                }

                if (this.field_2464.remaining() == 0) {
                    this.field_2464.flip();
                    GL11.glCallLists(this.field_2464);
                    this.field_2464.clear();
                }
            }

            this.field_2464.flip();
            GL11.glCallLists(this.field_2464);
            GL11.glPopMatrix();
        }
    }

    @Overwrite
    public int getTextWidth(String var1) {
        if (var1 == null) {
            return 0;
        } else {
            int var2 = 0;

            for (int var3 = 0; var3 < var1.length(); ++var3) {
                if (var1.charAt(var3) == 167) {
                    ++var3;
                } else {
                    int var4 = CharacterUtils.validCharacters.indexOf(var1.charAt(var3));
                    char var5 = var1.charAt(var3);
                    if (var4 >= 0 && var5 < 176) {
                        var2 += this.field_2462[var4 + 32];
                    } else if (var5 < 256) {
                        var2 += this.field_2462[var5];
                    }
                }
            }

            return var2;
        }
    }

    @Overwrite
    public void method_1904(String var1, int var2, int var3, int var4, int var5) {
        String[] var6 = var1.split("\n");
        if (var6.length > 1) {
            for (String s : var6) {
                this.method_1904(s, var2, var3, var4, var5);
                var3 += this.method_1902(s, var4);
            }
        } else {
            String[] var7 = var1.split(" ");
            int var8 = 0;

            while (var8 < var7.length) {
                String var9;
                var9 = var7[var8++] + " ";
                while (var8 < var7.length && this.getTextWidth(var9 + var7[var8]) < var4) {
                    var9 = var9 + var7[var8++] + " ";
                }

                int var10;
                for (; this.getTextWidth(var9) > var4; var9 = var9.substring(var10)) {
                    var10 = 0;
                    while (this.getTextWidth(var9.substring(0, var10 + 1)) <= var4) {
                        ++var10;
                    }

                    if (var9.substring(0, var10).trim().length() > 0) {
                        this.drawText(var9.substring(0, var10), var2, var3, var5);
                        var3 += 8;
                    }
                }

                if (var9.trim().length() > 0) {
                    this.drawText(var9, var2, var3, var5);
                    var3 += 8;
                }
            }

        }
    }

    @Overwrite
    public int method_1902(String var1, int var2) {
        String[] var3 = var1.split("\n");
        int var5;
        if (var3.length > 1) {
            int var9 = 0;

            for (var5 = 0; var5 < var3.length; ++var5) {
                var9 += this.method_1902(var3[var5], var2);
            }

            return var9;
        } else {
            String[] var4 = var1.split(" ");
            var5 = 0;
            int var6 = 0;

            while (var5 < var4.length) {
                String var7;
                var7 = var4[var5++] + " ";
                while (var5 < var4.length && this.getTextWidth(var7 + var4[var5]) < var2) {
                    var7 = var7 + var4[var5++] + " ";
                }

                int var8;
                for (; this.getTextWidth(var7) > var2; var7 = var7.substring(var8)) {
                    var8 = 0;
                    while (this.getTextWidth(var7.substring(0, var8 + 1)) <= var2) {
                        ++var8;
                    }

                    if (var7.substring(0, var8).trim().length() > 0) {
                        var6 += 8;
                    }
                }

                if (var7.trim().length() > 0) {
                    var6 += 8;
                }
            }

            if (var6 < 8) {
                var6 += 8;
            }

            return var6;
        }
    }
}
