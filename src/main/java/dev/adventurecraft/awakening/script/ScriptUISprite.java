package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.texture.TextureManager;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUISprite extends UIElement {

    public String texture;
    public float width;
    public float height;
    public float imageWidth;
    public float imageHeight;
    public double u;
    public double v;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public ScriptUISprite(String var1, float var2, float var3, float var4, float var5, double var6, double var8) {
        this(var1, var2, var3, var4, var5, var6, var8, ((ExInGameHud) Minecraft.instance.overlay).getScriptUI());
    }

    public ScriptUISprite(String var1, float var2, float var3, float var4, float var5, double var6, double var8, ScriptUIContainer var10) {
        this.imageWidth = 256.0F;
        this.imageHeight = 256.0F;
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;
        this.alpha = 1.0F;
        this.texture = var1;
        this.prevX = this.curX = var2;
        this.prevY = this.curY = var3;
        this.width = var4;
        this.height = var5;
        this.u = var6;
        this.v = var8;
        if (var10 != null) {
            var10.add(this);
        }

    }

    @Override
    public void render(TextRenderer var1, TextureManager var2, float var3) {
        if (this.texture.startsWith("http")) {
            var2.bindTexture(var2.getSkinTextureId(this.texture, "./pack.png"));
        } else {
            var2.bindTexture(var2.getTextureId(this.texture));
        }

        GL11.glColor4f(this.red, this.green, this.blue, this.alpha);
        float var4 = this.getXAtTime(var3);
        float var5 = this.getYAtTime(var3);
        float var6 = 1.0F / this.imageWidth;
        float var7 = 1.0F / this.imageHeight;
        Tessellator var8 = Tessellator.INSTANCE;
        var8.start();
        var8.vertex(var4, var5 + this.height, 0.0D, this.u * (double) var6, (this.v + (double) this.height) * (double) var7);
        var8.vertex(var4 + this.width, var5 + this.height, 0.0D, (float) (this.u + (double) this.width) * var6, (float) (this.v + (double) this.height) * var7);
        var8.vertex(var4 + this.width, var5, 0.0D, (float) (this.u + (double) this.width) * var6, this.v * (double) var7);
        var8.vertex(var4, var5, 0.0D, this.u * (double) var6, this.v * (double) var7);
        var8.tessellate();
    }
}
