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
    public void drawTextWithShadow(String text, int x, int y, int color) {
        this.drawText(text, x + 1, y + 1, color, true);
        this.drawText(text, x, y, color);
    }

    @Override
    public void drawStringWithShadow(String text, float x, float y, int color) {
        this.renderString(text, x + 1.0F, y + 1.0F, color, true);
        this.drawString(text, x, y, color);
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color) {
        this.drawText(text, x, y, color, false);
    }

    @Override
    public void drawString(String text, float x, float y, int color) {
        this.renderString(text, x, y, color, false);
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color, boolean shadow) {
        this.renderString(text, (float) x, (float) y, color, shadow);
    }

    private void renderString(String text, float x, float y, int color, boolean shadow) {
        if (text == null) {
            return;
        }

        if (shadow) {
            int tmp = color & -16777216;
            color = (color & 16579836) >> 2;
            color += tmp;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.field_2461);
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        if (alpha == 0.0F) {
            alpha = 1.0F;
        }

        GL11.glColor4f(red, green, blue, alpha);
        this.field_2464.clear();
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0.0F);

        String lowerText = text.toLowerCase();

        for (int i = 0; i < text.length(); ++i) {
            while (text.length() > i + 1 && text.charAt(i) == 167) {
                int charIndex = "0123456789abcdef".indexOf(lowerText.charAt(i + 1));
                if (charIndex < 0 || charIndex > 15) {
                    charIndex = 15;
                }

                this.field_2464.put(this.field_2463 + 256 + charIndex + (shadow ? 16 : 0));
                if (this.field_2464.remaining() == 0) {
                    this.field_2464.flip();
                    GL11.glCallLists(this.field_2464);
                    this.field_2464.clear();
                }
                i += 2;
            }

            if (i < text.length()) {
                char c = text.charAt(i);
                int charIndex = CharacterUtils.validCharacters.indexOf(c);
                if (charIndex >= 0 && c < 176) {
                    this.field_2464.put(this.field_2463 + charIndex + 32);
                } else if (c < 256) {
                    this.field_2464.put(this.field_2463 + c);
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

    @Overwrite
    public int getTextWidth(String text) {
        if (text == null) {
            return 0;
        }

        int width = 0;

        for (int i = 0; i < text.length(); ++i) {
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
            while (wordIndex < words.length && this.getTextWidth(builder + words[wordIndex]) < maxWidth) {
                builder.append(words[wordIndex++]).append(" ");
            }

            while (this.getTextWidth(builder.toString()) > maxWidth) {
                int consumed = 0;
                while (this.getTextWidth(builder.substring(0, consumed + 1)) <= maxWidth) {
                    ++consumed;
                }

                if (builder.substring(0, consumed).trim().length() > 0) {
                    this.drawText(builder.substring(0, consumed), x, y, color);
                    y += 8;
                }
                builder.setLength(0);
                builder.append(builder.substring(consumed));
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
            while (wordIndex < words.length && this.getTextWidth(builder + words[wordIndex]) < maxWidth) {
                builder.append(words[wordIndex++]).append(" ");
            }

            while (this.getTextWidth(builder.toString()) > maxWidth) {
                int consumed = 0;
                while (this.getTextWidth(builder.substring(0, consumed + 1)) <= maxWidth) {
                    ++consumed;
                }

                if (builder.substring(0, consumed).trim().length() > 0) {
                    y += 8;
                }
                builder.setLength(0);
                builder.append(builder.substring(consumed));
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
