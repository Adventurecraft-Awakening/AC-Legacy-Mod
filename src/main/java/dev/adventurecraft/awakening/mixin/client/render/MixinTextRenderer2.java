package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.TextRect;
import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.CharacterUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer2 implements ExTextRenderer {

    @Shadow
    private int[] field_2462;

    @Shadow
    public int field_2461;

    @Unique
    private float[] colorBuffer;

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
        this.colorBuffer = new float[32 * 3];
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

            this.colorBuffer[i * 3 + 0] = (float) r / 255.0f;
            this.colorBuffer[i * 3 + 1] = (float) g / 255.0f;
            this.colorBuffer[i * 3 + 2] = (float) b / 255.0f;
        }
    }

    @Overwrite
    public void drawTextWithShadow(String text, int x, int y, int color) {
        if (text == null) {
            return;
        }
        this.drawText(
            text, 0, text.length(),
            x, y, color, true, 1, 1, ExTextRenderer.getShadowColor(color));
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color) {
        if (text == null) {
            return;
        }
        this.drawText(
            text, 0, text.length(),
            (float) x, (float) y, color, false, 0, 0, 0);
    }

    @Overwrite
    public void drawText(String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return;
        }
        if (shadow) {
            color = ExTextRenderer.getShadowColor(color);
        }
        this.drawText(
            text, 0, text.length(),
            (float) x, (float) y, color, false, 0, 0, 0);
    }

    @Override
    public void drawText(
        CharSequence text, int start, int end,
        float x, float y, int color, boolean shadow, float sX, float sY, int sColor) {
        if (text == null || end - start == 0) {
            return;
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.field_2461);
        TextRendererState state = this.createState();
        state.begin(Tessellator.INSTANCE);
        state.setColor(color);
        if (shadow) {
            state.setShadow(true);
            state.setShadowOffset(sX, sY);
            state.setShadowColor(sColor);
        }
        state.drawText(text, start, end, x, y);
        state.end();
    }

    @Overwrite
    public int getTextWidth(String text) {
        var rect = this.getTextWidth(text, 0);
        return rect.width();
    }

    @Override
    public TextRendererState createState() {
        return new TextRendererState(this.colorBuffer, this.field_2462);
    }

    @Override
    @NotNull
    public TextRect getTextWidth(CharSequence text, int start, int end, long maxWidth) {
        if (text == null) {
            return TextRect.empty;
        }
        TextRendererState.validateCharSequence(text, start, end);
        if (end - start == 0) {
            return TextRect.empty;
        }

        int width = 0;
        int i;
        for (i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                i++;
                continue;
            }

            int index = CharacterUtils.validCharacters.indexOf(c);
            if (index >= 0 && c < 176) {
                width += this.field_2462[index + 32];
            } else if (c < 256) {
                width += this.field_2462[c];
            }

            if (width > maxWidth || c == '\n') {
                i++;
                break;
            }
        }

        return new TextRect(i - start, width);
    }
}
