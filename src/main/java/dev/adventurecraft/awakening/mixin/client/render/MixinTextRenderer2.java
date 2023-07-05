package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.GLAllocationUtils;
import net.minecraft.util.CharacterUtils;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;
import java.nio.FloatBuffer;
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

    private FloatBuffer colorBuffer;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
            remap = false))
    private InputStream redirectLoadToTexturePack(Class<?> instance, String name, @Local TextureManager texMan) {
        return texMan.texturePackManager.texturePack.getResourceAsStream(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initialize(GameOptions arg, String string, TextureManager arg2, CallbackInfo ci) {
        this.colorBuffer = GLAllocationUtils.allocateFloatBuffer(32 * 3);
        for (int i = 0; i < 32; ++i) {
            int n4 = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + n4;
            int g = (i >> 1 & 1) * 170 + n4;
            int b = (i >> 0 & 1) * 170 + n4;
            if (i == 6) {
                r += 85;
            }
            boolean bl = i >= 16;
            if (arg.anaglyph3d) {
                int newR = (r * 30 + g * 59 + b * 11) / 100;
                int newG = (r * 30 + g * 70) / 100;
                int newB = (r * 30 + b * 70) / 100;
                r = newR;
                g = newG;
                b = newB;
            }
            if (bl) {
                r /= 4;
                g /= 4;
                b /= 4;
            }

            this.colorBuffer.put(i * 3 + 0, (float) r / 255.0f);
            this.colorBuffer.put(i * 3 + 1, (float) g / 255.0f);
            this.colorBuffer.put(i * 3 + 2, (float) b / 255.0f);
        }
    }

    public int getShadowColor(int color) {
        int tmp = color & -16777216;
        int shadowColor = (color & 16579836) >> 2;
        return shadowColor + tmp;
    }

    @Overwrite
    public void drawTextWithShadow(String text, int x, int y, int color) {
        this.drawText(text, x, y, color, true, x + 1, y + 1, this.getShadowColor(color));
    }

    @Override
    public void drawString(String text, float x, float y, int color, boolean shadow) {
        this.drawText(text, x, y, color, shadow, x + 1.0F, y + 1.0F, this.getShadowColor(color));
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color) {
        this.drawText(text, (float) x, (float) y, color, false, 0, 0, 0);
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            color = this.getShadowColor(color);
        }
        this.drawText(text, (float) x, (float) y, color, false, 0, 0, 0);
    }

    private void drawChar(Tessellator ts, int character, float x, float y) {
        int n4 = character % 16 * 8;
        int n3 = character / 16 * 8;
        float f = 7.99f;
        float f2 = 0.0f;
        float f3 = 0.0f;
        ts.vertex(x, y + f, 0.0, (float) n4 / 128.0f + f2, ((float) n3 + f) / 128.0f + f3);
        ts.vertex(x + f, y + f, 0.0, ((float) n4 + f) / 128.0f + f2, ((float) n3 + f) / 128.0f + f3);
        ts.vertex(x + f, y, 0.0, ((float) n4 + f) / 128.0f + f2, (float) n3 / 128.0f + f3);
        ts.vertex(x, y, 0.0, (float) n4 / 128.0f + f2, (float) n3 / 128.0f + f3);
    }

    private void drawText(
        String text, float x, float y, int color, boolean shadow, float sX, float sY, int sColor) {
        if (text == null) {
            return;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.field_2461);

        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (a == 0.0F) {
            a = 1.0F;
        }

        float sR = (float) (sColor >> 16 & 255) / 255.0F;
        float sG = (float) (sColor >> 8 & 255) / 255.0F;
        float sB = (float) (sColor & 255) / 255.0F;
        float sA = (float) (sColor >> 24 & 255) / 255.0F;
        if (sA == 0.0F) {
            sA = 1.0F;
        }

        String lowerText = text.toLowerCase();
        Tessellator ts = Tessellator.INSTANCE;
        ts.start();
        ts.color(r, g, b, a);

        float xOff = 0;

        for (int i = 0; i < text.length(); ++i) {
            while (text.length() > i + 1 && text.charAt(i) == '§') {
                int charIndex = "0123456789abcdef".indexOf(lowerText.charAt(i + 1));
                if (charIndex < 0 || charIndex > 15) {
                    charIndex = 15;
                }

                int colorIndex = charIndex * 3;
                r = this.colorBuffer.get(colorIndex + 0);
                g = this.colorBuffer.get(colorIndex + 1);
                b = this.colorBuffer.get(colorIndex + 2);

                if (shadow) {
                    int sColorIndex = (charIndex + 16) * 3;
                    sR = this.colorBuffer.get(sColorIndex + 0);
                    sG = this.colorBuffer.get(sColorIndex + 1);
                    sB = this.colorBuffer.get(sColorIndex + 2);
                } else {
                    ts.color(r, g, b, a);
                }

                i += 2;
            }

            if (i >= text.length()) {
                continue;
            }

            char c = text.charAt(i);
            int charIndex = CharacterUtils.validCharacters.indexOf(c);
            int ch;
            if (charIndex >= 0 && c < 176) {
                ch = charIndex + 32;
            } else if (c < 256) {
                ch = c;
            } else {
                continue;
            }

            if (shadow) {
                ts.color(sR, sG, sB, sA);
                this.drawChar(ts, ch, xOff + sX, sY);
                ts.color(r, g, b, a);
            }
            this.drawChar(ts, ch, xOff + x, y);
            xOff += this.field_2462[ch];
        }

        ts.tessellate();
    }

    @Overwrite
    public int getTextWidth(String text) {
        return getTextWidth(text, 0);
    }

    public int getTextWidth(CharSequence text, int start) {
        return getTextWidth(text, start, text.length());
    }

    public int getTextWidth(CharSequence text, int start, int length) {
        if (text == null) {
            return 0;
        }

        int width = 0;

        for (int i = start; i < length; ++i) {
            char c = text.charAt(i);
            if (c == 167) {
                ++i;
                continue;
            }

            int index = CharacterUtils.validCharacters.indexOf(c);
            if (index >= 0 && c < 176) {
                width += this.field_2462[index + 32];
            } else if (c < 256) {
                width += this.field_2462[c];
            }
        }

        return width;
    }

    @Overwrite
    public void method_1904(String text, int x, int y, int maxWidth, int color) {
        String[] lines = text.split("\n");
        if (lines.length > 1) {
            for (String line : lines) {
                this.method_1904(line, x, y, maxWidth, color);
                y += this.method_1902(line, maxWidth);
            }
            return;
        }

        String[] words = text.split(" ");
        int wordIndex = 0;

        StringBuilder builder = new StringBuilder();
        while (wordIndex < words.length) {
            builder.setLength(0);
            builder.append(words[wordIndex++]).append(" ");
            while (wordIndex < words.length && (this.getTextWidth(builder, 0) + this.getTextWidth(words[wordIndex])) < maxWidth) {
                builder.append(words[wordIndex++]).append(" ");
            }

            while (this.getTextWidth(builder, 0) > maxWidth) {
                int consumed = 0;
                while (this.getTextWidth(builder, 0, consumed + 1) <= maxWidth) {
                    ++consumed;
                }

                if (builder.substring(0, consumed).trim().length() > 0) {
                    this.drawText(builder.substring(0, consumed), x, y, color);
                    y += 8;
                }
                builder.delete(0, consumed);
            }

            if (builder.toString().trim().length() > 0) {
                this.drawText(builder.toString(), x, y, color);
                y += 8;
            }
        }
    }

    @Overwrite
    public int method_1902(String text, int maxWidth) {
        String[] lines = text.split("\n");
        if (lines.length > 1) {
            int y = 0;
            for (String line : lines) {
                y += this.method_1902(line, maxWidth);
            }
            return y;
        }

        String[] words = text.split(" ");
        int wordIndex = 0;
        int y = 0;

        StringBuilder builder = new StringBuilder();
        while (wordIndex < words.length) {
            builder.setLength(0);
            builder.append(words[wordIndex++]).append(" ");
            while (wordIndex < words.length && (this.getTextWidth(builder, 0) + this.getTextWidth(words[wordIndex])) < maxWidth) {
                builder.append(words[wordIndex++]).append(" ");
            }

            while (this.getTextWidth(builder, 0) > maxWidth) {
                int consumed = 0;
                while (this.getTextWidth(builder, 0, consumed + 1) <= maxWidth) {
                    ++consumed;
                }

                if (builder.substring(0, consumed).trim().length() > 0) {
                    y += 8;
                }
                builder.delete(0, consumed);
            }

            if (builder.toString().trim().length() > 0) {
                y += 8;
            }
        }

        if (y < 8) {
            y += 8;
        }

        return y;
    }
}
