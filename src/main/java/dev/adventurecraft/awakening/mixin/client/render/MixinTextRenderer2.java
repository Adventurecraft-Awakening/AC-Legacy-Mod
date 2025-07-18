package dev.adventurecraft.awakening.mixin.client.render;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.TextRect;
import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import dev.adventurecraft.awakening.image.Rgba;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;

@Mixin(Font.class)
public abstract class MixinTextRenderer2 implements ExTextRenderer {

    @Shadow
    private int[] charWidths;

    @Unique
    private int[] colorBuffer;

    @Override
    public int[] getCharWidths() {
        return this.charWidths;
    }

    @Override
    public int[] getColorPalette() {
        return this.colorBuffer;
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;",
            remap = false
        )
    )
    private InputStream redirectLoadToTexturePack(
        Class<?> instance,
        String name,
        @Local(argsOnly = true) Textures texMan
    ) {
        return texMan.skins.selected.getResource(name);
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void initialize(Options arg, String string, Textures arg2, CallbackInfo ci) {
        var colorBuffer = new int[32];
        for (int i = 0; i < 32; ++i) {
            int n4 = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + n4;
            int g = (i >> 1 & 1) * 170 + n4;
            int b = (i >> 0 & 1) * 170 + n4;
            if (i == 6) {
                r += 85;
            }

            if (arg.anaglyph3d) {
                int newR = (r * 30 + g * 59 + b * 11) / 100;
                int newG = (r * 30 + g * 70) / 100;
                int newB = (r * 30 + b * 70) / 100;
                r = newR;
                g = newG;
                b = newB;
            }

            boolean shadow = i >= 16;
            if (shadow) {
                r >>= 2;
                g >>= 2;
                b >>= 2;
            }
            colorBuffer[i] = Rgba.fromRgb8(r, g, b);
        }
        this.colorBuffer = colorBuffer;
    }

    @Overwrite
    public void drawShadow(String text, int x, int y, int color) {
        if (text == null) {
            return;
        }
        color = Rgba.fromBgra(color);
        this.drawText(text, 0, text.length(), x, y, color, true, 1, 1, ExTextRenderer.getShadowColor(color));
    }

    @Overwrite
    public void draw(String text, int x, int y, int color) {
        if (text == null) {
            return;
        }
        color = Rgba.fromBgra(color);
        this.drawText(text, 0, text.length(), (float) x, (float) y, color, false, 0, 0, 0);
    }

    @Overwrite
    public void draw(String text, int x, int y, int color, boolean shadow) {
        if (text == null) {
            return;
        }
        color = Rgba.fromBgra(color);
        if (shadow) {
            color = ExTextRenderer.getShadowColor(color);
        }
        this.drawText(text, 0, text.length(), (float) x, (float) y, color, false, 0, 0, 0);
    }

    @Override
    public void drawText(
        CharSequence text,
        int start,
        int end,
        float x,
        float y,
        int color,
        boolean hasShadow,
        float sX,
        float sY,
        int shadow
    ) {
        if (text == null || end - start == 0) {
            return;
        }

        TextRendererState state = this.createState();
        state.setColor(Rgba.alphaOrOpaque(color));
        if (hasShadow) {
            state.setShadowOffset(sX, sY);
            state.setShadow(Rgba.alphaOrOpaque(shadow));
        }

        var ts = Tesselator.instance;
        state.begin(ts);
        state.drawText(ts, text, start, end, x, y);
        state.end(ts);
    }

    @Overwrite
    public int width(String text) {
        var rect = this.getTextWidth(text, 0);
        return rect.width();
    }

    @Override
    @NotNull
    public TextRendererState createState() {
        return new TextRendererState((Font) (Object) this);
    }

    @Override
    @NotNull
    public TextRect getTextWidth(CharSequence text, int start, int end, long maxWidth, boolean newLines) {
        if (text == null) {
            return TextRect.empty;
        }
        TextRendererState.validateCharSequence(text, start, end);
        if (end - start == 0) {
            return TextRect.empty;
        }

        int[] widthLookup = this.getCharWidths();

        int width = 0;
        int i;
        for (i = start; i < end; ++i) {
            char c = text.charAt(i);
            if (end > i + 1 && c == '§') {
                i++; // skip the format code digit
                continue;
            }

            int charIndex = this.getCharIndex(c);
            if (charIndex == -1) {
                continue;
            }
            width += widthLookup[charIndex];

            if (width > maxWidth) {
                break;
            }
            else if (newLines && c == '\n') {
                i++;
                break;
            }
        }

        return new TextRect(i - start, width);
    }
}
