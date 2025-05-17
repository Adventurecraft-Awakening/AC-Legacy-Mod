package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;

public class ScriptModelBlockbench extends ScriptModelBase {

    float pixelSize = 0.0625f;

    public float prevScaleX;
    public float prevScaleY;
    public float prevScaleZ;
    public float scaleX = 1.0F;
    public float scaleY = 1.0F;
    public float scaleZ = 1.0F;

    public float sizeX = 0.0F;
    public float sizeY = 0.0F;
    public float sizeZ = 0.0F;

    public float pivotX = 0.0F;
    public float pivotY = 0.0F;
    public float pivotZ = 0.0F;

    public ScriptModelBlockbench() {
        this.addToRendering();
    }

    public ScriptModelBlockbench(int width, int height) {
        this.addToRendering();
        this.textureWidth = width;
        this.textureHeight = height;
    }

    @Override
    protected void transform(float partialTick, Matrix4f matrix) {
        super.transform(partialTick, matrix);

        float invDelta = 1.0F - partialTick;
        double x = partialTick * this.x + invDelta * this.prevX;
        double y = partialTick * this.y + invDelta * this.prevY;
        double z = partialTick * this.z + invDelta * this.prevZ;

        float pxSize = this.pixelSize;
        // Move Rotation Origin to given pivot
        matrix.translate(-this.pivotX * pxSize, this.pivotY * pxSize, -this.pivotZ * pxSize);

        float deg = MathF.toRadians(partialTick);
        float invDeg = MathF.toRadians(invDelta);
        matrix.rotateZ(-(deg * this.roll + invDeg * this.prevRoll));
        matrix.rotateY(-(deg * this.pitch + invDeg * this.prevPitch));
        matrix.rotateX(deg * this.yaw + invDeg * this.prevYaw);

        // Apply scaling
        matrix.scale(this.scaleX, this.scaleY, this.scaleZ);

        // Move Object to intended Position
        matrix.translate(
            (float) (-x - this.sizeX + this.pivotX) * pxSize,
            (float) (y - this.pivotY) * pxSize,
            (float) (-z - this.sizeZ + this.pivotZ) * pxSize);
    }

    @Override
    protected void update() {
        this.prevScaleX = this.scaleX;
        this.prevScaleY = this.scaleY;
        this.prevScaleZ = this.scaleZ;
    }

    public void addBox(
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY) {
        this.setSize(width, height, length);

        var cuboid = new ModelPart(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(0, 0, 0, width, height, length, 0.0F);
        this.boxes.add(cuboid);
    }

    public void addBoxExpanded(
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY, float inflate) {
        this.setSize(width, height, length);

        var cuboid = new ModelPart(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(0, 0, 0, width, height, length, inflate);
        this.boxes.add(cuboid);
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.prevRoll = this.roll = roll;
        this.prevPitch = this.pitch = -pitch;
        this.prevYaw = this.yaw = -yaw;
    }

    public void setRotation(ScriptVec3 vec) {
        this.prevRoll = this.roll = (float) vec.z;
        this.prevPitch = this.pitch = -(float) vec.y;
        this.prevYaw = this.yaw = -(float) vec.x;
    }

    public ScriptVec3 getRotation() {
        return new ScriptVec3(-this.yaw, -this.pitch, this.roll);
    }

    public void scaleBy(float factorX, float factorY, float factorZ) {
        this.scaleX += factorX;
        this.scaleY += factorY;
        this.scaleZ += factorZ;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public ScriptVec3 getScale() {
        return new ScriptVec3(this.scaleX, this.scaleY, this.scaleZ);
    }

    public void setSize(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
    }

    public ScriptVec3 getSize() {
        return new ScriptVec3(this.sizeX, this.sizeY, this.sizeZ);
    }

    public void setPivot(float pivotX, float pivotY, float pivotZ) {
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
    }

    public void setPivot(ScriptVec3 vec) {
        this.pivotX = (float) vec.x;
        this.pivotY = (float) vec.y;
        this.pivotZ = (float) vec.z;
    }

    public ScriptVec3 getPivot() {
        return new ScriptVec3(this.pivotX, this.pivotY, this.pivotZ);
    }

    public void setBrightness(float brightness) {
        this.colorRed = this.colorGreen = this.colorBlue = Math.min(brightness, 1.0F);
    }

    public void setBrightness(int brightness) {
        setBrightness(Math.max(brightness, 255) / 256.0F);
    }

    public void moveTo(double x, double y, double z) {
        this.prevX = this.x = x;
        this.prevY = this.y = y;
        this.prevZ = this.z = z;
    }

    public void moveBy(double x, double y, double z) {
        float yaw = MathF.toRadians(this.yaw);
        float pitch = MathF.toRadians(this.pitch);
        float roll = MathF.toRadians(this.roll);

        float sinYaw = MathF.sin(yaw);
        float cosYaw = MathF.cos(yaw);
        double tempY = x * cosYaw + z * sinYaw;
        z = z * cosYaw - x * sinYaw;
        x = tempY;

        float sinPitch = MathF.sin(pitch);
        float cosPitch = MathF.cos(pitch);
        tempY = z * cosPitch + y * sinPitch;
        y = y * cosPitch - z * sinPitch;
        z = tempY;

        float sinRoll = MathF.sin(roll);
        float cosRoll = MathF.cos(roll);
        tempY = y * cosRoll + x * sinRoll;
        x = x * cosRoll - y * sinRoll;

        this.prevX = this.x += x;
        this.prevY = this.y += tempY;
        this.prevZ = this.z += z;
    }


    public void rotateTo(float yaw, float pitch, float roll) {
        this.prevYaw = this.yaw = -yaw;
        this.prevPitch = this.pitch = -pitch;
        this.prevRoll = this.roll = roll;
    }


    public void rotateBy(float yaw, float pitch, float roll) {
        this.yaw -= yaw;
        this.pitch -= pitch;
        this.roll += roll;

        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
    }


}


