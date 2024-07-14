package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Cuboid;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class ScriptModel extends ScriptModelBase {

    public ScriptModel() {
        this.addToRendering();
    }

    public ScriptModel(int width, int height) {
        this.addToRendering();
        this.textureWidth = width;
        this.textureHeight = height;
    }
    public void setBrightness(float brightness){
        this.colorRed = this.colorGreen = this.colorBlue = brightness;
    }

    //Old method
    public void addBox(String boxName,
                       float offsetX, float offsetY, float offsetZ,
                       int width, int height, int length,
                       int textureOffsetX, int textureOffsetY) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, 0.0F);
    }

    public void addBox(float offsetX, float offsetY, float offsetZ,
                       int width, int height, int length,
                       int textureOffsetX, int textureOffsetY) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, 0.0F);
    }

    //Old legacy method
    public void addBoxExpanded(String boxName,
                               float offsetX, float offsetY, float offsetZ,
                               int width, int height, int length,
                               int textureOffsetX, int textureOffsetY,
                               float scale) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, scale);
    }

    public void addBoxExpanded(float offsetX, float offsetY, float offsetZ,
                               int width, int height, int length,
                               int textureOffsetX, int textureOffsetY,
                               float scale) {
        Cuboid cuboid = new Cuboid(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(offsetX, offsetY, offsetZ, width, height, length, scale);
        this.boxes.add(cuboid);
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(x, y, z);
    }

    public void setPosition(double x, double y, double z) {
        this.prevX = this.x = x;
        this.prevY = this.y = y;
        this.prevZ = this.z = z;
    }

    public void moveTo(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void moveBy(double x, double y, double z) {
        double yaw = Math.toRadians(this.yaw);
        double pitch = Math.toRadians(this.pitch);
        double roll = Math.toRadians(this.roll);
        double tempY = x * Math.cos(yaw) + z * Math.sin(yaw);
        z = z * Math.cos(yaw) - x * Math.sin(yaw);
        x = tempY;
        tempY = z * Math.cos(pitch) + y * Math.sin(pitch);
        y = y * Math.cos(pitch) - z * Math.sin(pitch);
        z = tempY;
        tempY = y * Math.cos(roll) + x * Math.sin(roll);
        x = x * Math.cos(roll) - y * Math.sin(roll);
        this.x += x;
        this.y += tempY;
        this.z += z;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.prevYaw = this.yaw = yaw;
        this.prevPitch = this.pitch = pitch;
        this.prevRoll = this.roll = roll;
    }

    public void rotateTo(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public void rotateBy(float yaw, float pitch, float roll) {
        this.yaw += yaw;
        this.pitch += pitch;
        this.roll += roll;
    }

    @Override
    protected void update() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
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
        double x = (double) deltaTime * this.x + (double) deltaTimeTick * this.prevX;
        double y = (double) deltaTime * this.y + (double) deltaTimeTick * this.prevY;
        double z = (double) deltaTime * this.z + (double) deltaTimeTick * this.prevZ;
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(deltaTime * this.yaw + deltaTimeTick * this.prevYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(deltaTime * this.pitch + deltaTimeTick * this.prevPitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(deltaTime * this.roll + deltaTimeTick * this.prevRoll, 0.0F, 0.0F, 1.0F);
    }

    @Override
    protected void render(float var1) {
        if (boxes.isEmpty()) {
            return;
        }
        World world = Minecraft.instance.world;
        TextureManager var3 = Minecraft.instance.textureManager;
        if (this.texture != null && !this.texture.isEmpty()) {
            var3.bindTexture(var3.getTextureId(this.texture));
        }

        // TODO: clean up?
        GL11.glPushMatrix();
        GL11.glLoadIdentity();


        this.transform(var1);

        modelView.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);

        //GL11.glTranslatef(scale,scale,scale);

        transform.load(modelView);
        GL11.glPopMatrix();
        v.set(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix4f.transform(transform, v, vr);


        switch (this.modes){
            case 1:
                //using the position of the attached entity
                if(this.attachedTo != null){
                    var position = this.attachedTo.getPosition();
                    setBrightness(world.method_1782((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z)));
                }
                break;
            case 2:
                //usage for custom RGB Values
                break;
            case 3:
                //use the lightning value of the attached model
                if(this.modelAttachment != null){
                    this.colorRed = this.modelAttachment.colorRed;
                    this.colorGreen = this.modelAttachment.colorGreen;
                    this.colorBlue = this.modelAttachment.colorBlue;
                }
                break;
            default:
                //Default lightning values
                setBrightness(world.method_1782(Math.round(vr.x), Math.round(vr.x), Math.round(vr.x)));
                break;
        }
        if(colorAlpha < 1.0){
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(colorRed, colorGreen, colorBlue, colorAlpha);
        } else{
            GL11.glColor3f(colorRed, colorGreen, colorBlue);
        }
        //GL11.glColor3f(colorRed, colorGreen, colorBlue);
        GL11.glPushMatrix();
        this.transform(var1);

        for (Cuboid cuboid : this.boxes) {
            cuboid.render(1.0F / 16.0F);
        }

        GL11.glPopMatrix();
        if(colorAlpha < 1.0){
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
