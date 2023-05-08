package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;

@SuppressWarnings("unused")
public class ScriptUILabel extends UIElement {

    private String text;
    private String[] textLines;
    public boolean shadow;
    public boolean centered;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public ScriptUILabel(String var1, float var2, float var3) {
        this(var1, var2, var3, ((ExInGameHud) Minecraft.instance.overlay).getScriptUI());
    }

    public ScriptUILabel(String var1, float var2, float var3, ScriptUIContainer var4) {
        this.text = "";
        this.shadow = true;
        this.centered = false;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.text = var1;
        this.textLines = var1.split("\n");
        this.prevX = this.curX = var2;
        this.prevY = this.curY = var3;
        if (var4 != null) {
            var4.add(this);
        }
    }

    @Override
    public void render(TextRenderer var1, TextureManager var2, float var3) {
        int var4 = Math.max(Math.min((int) (this.alpha * 255.0F), 255), 0);
        if (var4 == 0) {
            return;
        }

        var4 = (var4 << 8) + Math.max(Math.min((int) (this.red * 255.0F), 255), 0);
        var4 = (var4 << 8) + Math.max(Math.min((int) (this.green * 255.0F), 255), 0);
        var4 = (var4 << 8) + Math.max(Math.min((int) (this.blue * 255.0F), 255), 0);
        float var5 = this.getXAtTime(var3);
        float var6 = this.getYAtTime(var3);
        String[] var7 = this.textLines;

        for (String var10 : var7) {
            float var11 = var5;
            if (this.centered) {
                var11 = var5 - (float) (var1.getTextWidth(var10) / 2);
            }

            if (this.shadow) {
                ((ExTextRenderer) var1).drawStringWithShadow(var10, var11, var6, var4);
            } else {
                ((ExTextRenderer) var1).drawString(var10, var11, var6, var4);
            }

            var6 += 9.0F;
        }
    }

    public String getText() {
        return this.text;
    }

    public void setText(String var1) {
        this.text = var1;
        this.textLines = var1.split("\n");
    }
}
