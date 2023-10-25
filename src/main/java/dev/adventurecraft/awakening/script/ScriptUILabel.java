package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.common.TextRendererState;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.render.ExTextRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
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

    public ScriptUILabel(String text, float x, float y) {
        this(text, x, y, ((ExInGameHud) Minecraft.instance.overlay).getScriptUI());
    }

    public ScriptUILabel(String text, float x, float y, ScriptUIContainer container) {
        this.shadow = true;
        this.centered = false;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.text = text;
        this.textLines = text.split("\n");
        this.prevX = this.curX = x;
        this.prevY = this.curY = y;
        if (container != null) {
            container.add(this);
        }
    }

    @Override
    public void render(TextRenderer textRenderer, TextureManager textureManager, float deltaTime) {
        int color = Math.max(Math.min((int) (this.alpha * 255.0F), 255), 0);
        if (color == 0) {
            return;
        }

        color = (color << 8) + Math.max(Math.min((int) (this.red * 255.0F), 255), 0);
        color = (color << 8) + Math.max(Math.min((int) (this.green * 255.0F), 255), 0);
        color = (color << 8) + Math.max(Math.min((int) (this.blue * 255.0F), 255), 0);
        float x = this.getXAtTime(deltaTime);
        float y = this.getYAtTime(deltaTime);
        String[] lines = this.textLines;
        int shadowColor = ExTextRenderer.getShadowColor(color);

        TextRendererState state = ((ExTextRenderer) textRenderer).createState();
        state.setShadow(shadow);
        state.setShadowOffset(1, 1);

        state.bindTexture();
        state.begin(Tessellator.INSTANCE);
        for (String line : lines) {
            float lineX = x;
            if (this.centered) {
                lineX = x - (float) (textRenderer.getTextWidth(line) / 2);
            }

            state.setColor(color);
            state.setShadowColor(shadowColor);
            state.drawText(line, 0, line.length(), lineX, y);

            y += 9.0F;
        }
        state.end();
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
        this.textLines = text.split("\n");
    }
}
