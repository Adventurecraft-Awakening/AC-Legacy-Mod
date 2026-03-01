package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.Textures;
import org.lwjgl.opengl.GL11;

@SuppressWarnings("unused")
public class ScriptUIRect extends UIElement implements ScriptColor {

    public double width;
    public double height;

    private ScriptVec4 color;

    public ScriptUIRect(double x, double y, double width, double height, ScriptVec4 color) {
        this(x, y, width, height, color, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    @Deprecated
    public ScriptUIRect(double x, double y, double width, double height, double red, double green, double blue, double alpha) {
        this(x, y, width, height, red, green, blue, alpha, ((ExInGameHud) Minecraft.instance.gui).getScriptUI());
    }

    public ScriptUIRect(double x, double y, double width, double height, ScriptVec4 color, ScriptUIContainer container) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.color = color;
        if (container != null) {
            container.add(this);
        }
    }

    @Deprecated
    public ScriptUIRect(
        double x,
        double y,
        double width,
        double height,
        double red,
        double green,
        double blue,
        double alpha,
        ScriptUIContainer container
    ) {
        this(x, y, width, height, new ScriptVec4(red, green, blue, alpha), container);
    }

    @Override
    public void render(Font font, Textures textures, float deltaTime) {
        double x = this.getXAtTime(deltaTime);
        double y = this.getYAtTime(deltaTime);
        Tesselator ts = Tesselator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        ts.begin();
        ScriptVec4 c = this.getColor();
        float r = (float) c.getR();
        float g = (float) c.getG();
        float b = (float) c.getB();
        float a = (float) c.getA();
        ts.color(r, g, b, a);
        ts.vertex(x, y + this.height, 0.0D);
        ts.vertex(x + this.width, y + this.height, 0.0D);
        ts.vertex(x + this.width, y, 0.0D);
        ts.vertex(x, y, 0.0D);
        ts.end();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public ScriptVec4 getColor() {
        return this.color;
    }

    @Override
    public void setColor(ScriptVec4 value) {
        if (value == null) {
            value = new ScriptVec4(1.0);
        }
        this.color = value;
    }
}
