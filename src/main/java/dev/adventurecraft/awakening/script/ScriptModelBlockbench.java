package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
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
        // Apply scaling
        GL11.glScalef(this.scaleX, this.scaleY, this.scaleZ);
        // Move Object to intended Position
        GL11.glTranslatef(
            (float)(-x - this.sizeX + this.pivotX) * pixelSize,
            (float)(y - this.pivotY) * pixelSize,
            (float)(-z - this.sizeZ + this.pivotZ) * pixelSize);


    }

    protected void render(float var1) {
        if (boxes.isEmpty()) {
            return;
        }
        Level world = Minecraft.instance.level;
        Textures var3 = Minecraft.instance.textures;
        if (this.texture != null && !this.texture.isEmpty()) {
            var3.bind(var3.loadTexture(this.texture));
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
                    setBrightness(world.getBrightness((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z)));
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
        for (ModelPart cuboid : this.boxes) {
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

    public void addBox(int width, int height, int length,
                       int textureOffsetX, int textureOffsetY) {
        this.setSize(width,height,length);

        ModelPart cuboid = new ModelPart(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(0,0,0, width, height, length, 0.0F);
        this.boxes.add(cuboid);
    }
    public void setBrightness(float brightness){ this.colorRed = this.colorGreen = this.colorBlue = brightness;}

    public void setPosition(double x, double y, double z) {
        this.prevX = this.x = x;
        this.prevY = this.y = y;
        this.prevZ = this.z = z;
    }

    public void setPosition(ScriptVec3 vec) {
        this.prevX = this.x = vec.x;
        this.prevY = this.y = vec.y;
        this.prevZ = this.z = vec.z;
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(x, y, z);
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.prevRoll = this.roll = roll;
        this.prevPitch = this.pitch = -pitch;
        this.prevYaw = this.yaw = -yaw;
    }

    public void setRotation(ScriptVec3 vec) {
        this.prevRoll = this.roll = (float)vec.z;
        this.prevPitch = this.pitch = - (float)vec.y;
        this.prevYaw = this.yaw = - (float)vec.x;
    }

    public ScriptVec3 getRotation() {
        return new ScriptVec3(-this.yaw,-this.pitch,this.roll);
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public void scaleBy(float factorX, float factorY, float factorZ){
        this.scaleX += factorX;
        this.scaleY += factorY;
        this.scaleZ += factorZ;
    }

    public ScriptVec3 getScale() {
        return new ScriptVec3(this.scaleX, this.scaleY, this.scaleZ);
    }

    public void setSize(int sizeX, int sizeY, int sizeZ){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public ScriptVec3 getSize(){ return new ScriptVec3(this.sizeX,this.sizeY,this.sizeZ);}

    public void setPivot(float pivotX, float pivotY, float pivotZ) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
    }

    public void setPivot(ScriptVec3 vec) {
        this.pivotX = (float)vec.x;
        this.pivotY = (float)vec.y;
        this.pivotZ = (float)vec.z;
    }

    public ScriptVec3 getPivot() {return new ScriptVec3(this.pivotX, this.pivotY, this.pivotZ);
    }

    public void setAlpha(float alpha) {
        this.colorAlpha = alpha;
    }

    public float getAlpha(){ return this.colorAlpha;}
}


