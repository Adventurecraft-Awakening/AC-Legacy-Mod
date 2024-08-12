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
        if (this.attachedTo != null) {

            ScriptVec3 position = this.attachedTo.getPosition(deltaTime);
            ScriptVecRot rotation = this.attachedTo.getRotation(deltaTime);

            GL11.glTranslated(position.x, position.y, position.z);

            GL11.glRotatef((float) (-rotation.yaw), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float) rotation.pitch, 1.0F, 0.0F, 0.0F);

        } else if (this.modelAttachment != null) {
            this.modelAttachment.transform(deltaTime);
        }

        float deltaTimeTick = 1.0F - deltaTime;

        double x = deltaTime * this.x + deltaTimeTick * this.prevX;
        double y = deltaTime * this.y + deltaTimeTick * this.prevY;
        double z = deltaTime * this.z + deltaTimeTick * this.prevZ;

        // Move Rotation Origin to given pivot
        GL11.glTranslatef(-this.pivotX * pixelSize, this.pivotY * pixelSize, -this.pivotZ * pixelSize);

        GL11.glRotatef(-(deltaTime * this.roll + deltaTimeTick * this.prevRoll), 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(-(deltaTime * this.pitch + deltaTimeTick * this.prevPitch), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((deltaTime * this.yaw + deltaTimeTick * this.prevYaw ), 1.0F, 0.0F, 0.0F);

        // Move Object to intended Position
        GL11.glTranslatef(
            (float)(-x - this.sizeX + this.pivotX) * pixelSize,
            (float)(y - this.pivotY) * pixelSize,
            (float)(-z - this.sizeZ + this.pivotZ) * pixelSize);

        // Apply scaling
        GL11.glScalef(this.scaleX, this.scaleY, this.scaleZ);
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
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevRoll = this.roll;
        this.prevPitch = this.pitch;
        this.prevYaw = this.yaw;

        this.prevScaleX = this.scaleX;
        this.prevScaleY = this.scaleY;
        this.prevScaleZ = this.scaleZ;
    }

    }
}
