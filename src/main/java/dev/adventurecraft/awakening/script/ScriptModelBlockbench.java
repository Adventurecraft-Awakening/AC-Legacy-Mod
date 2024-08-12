package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

public class ScriptModelBlockbench extends ScriptModelBase {
    float pixelSize = 0.0625f;
    public ScriptModelBlockbench() {
        this.addToRendering();
    }

    public ScriptModelBlockbench(int width, int height) {
        this.addToRendering();
        this.textureWidth = width;
        this.textureHeight = height;
    }
    @Override
    protected void transform(float deltaTime) {

    }

    protected void render(float var1) {
        if (boxes.isEmpty()) {
            return;
        }
        World world = Minecraft.instance.world;
        TextureManager var3 = Minecraft.instance.textureManager;
        if (this.texture != null && !this.texture.isEmpty()) {
            var3.bindTexture(var3.getTextureId(this.texture));
        }

        GL11.glPushMatrix();

        // Initial transformation
        this.transform(var1);

        // Adjust lighting modes
        switch (this.modes) {
            case 1:
                // Using the position of the attached entity
                if (this.attachedTo != null) {
                    var position = this.attachedTo.getPosition();
                    setBrightness(world.method_1782((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z)));
                }
                break;
            case 2:
                // Usage for custom RGB Values
                break;
            case 3:
                // Use the lighting value of the attached model
                if (this.modelAttachment != null) {
                    this.colorRed = this.modelAttachment.colorRed;
                    this.colorGreen = this.modelAttachment.colorGreen;
                    this.colorBlue = this.modelAttachment.colorBlue;
                }
                break;
            default:
                setBrightness(1);
                break;
        }

        // Handle alpha blending
        if (colorAlpha < 1.0) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
        } else {
            GL11.glDisable(GL11.GL_BLEND);
        }

        // Set color with alpha
        GL11.glColor4f(colorRed, colorGreen, colorBlue, colorAlpha);

        // Render each cuboid
        for (Cuboid cuboid : this.boxes) {
            cuboid.render(1.0F / 16.0F);
        }

        GL11.glPopMatrix();
    }

    @Override
    protected void update() {

    }
}
