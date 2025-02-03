package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.util.vector.Matrix4f;

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

    public void setBrightness(float brightness) {
        this.colorRed = this.colorGreen = this.colorBlue = Math.min(brightness, 1.0F);
    }

    public void setBrightness(int brightness) {
        setBrightness(Math.max(brightness, 255) / 256.0F);
    }

    //Old method
    public void addBox(
        String boxName,
        float offsetX, float offsetY, float offsetZ,
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, 0.0F);
    }

    public void addBox(
        float offsetX, float offsetY, float offsetZ,
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, 0.0F);
    }

    //Old legacy method
    public void addBoxExpanded(
        String boxName,
        float offsetX, float offsetY, float offsetZ,
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY,
        float scale) {
        this.addBoxExpanded(offsetX, offsetY, offsetZ, width, height, length, textureOffsetX, textureOffsetY, scale);
    }

    public void addBoxExpanded(
        float offsetX, float offsetY, float offsetZ,
        int width, int height, int length,
        int textureOffsetX, int textureOffsetY,
        float scale) {
        var cuboid = new ModelPart(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(offsetX, offsetY, offsetZ, width, height, length, scale);
        this.boxes.add(cuboid);
    }

    public void moveTo(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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
    protected void transform(float deltaTime, Matrix4f matrix) {
        super.transform(deltaTime, matrix);

        float invDelta = 1.0F - deltaTime;
        double x = deltaTime * this.x + invDelta * this.prevX;
        double y = deltaTime * this.y + invDelta * this.prevY;
        double z = deltaTime * this.z + invDelta * this.prevZ;
        matrix.translate((float) x, (float) y, (float) z);

        float rad = MathF.toRadians(deltaTime);
        float invRad = MathF.toRadians(invDelta);
        matrix.rotateY(rad * this.yaw + invRad * this.prevYaw);
        matrix.rotateX(rad * this.pitch + invRad * this.prevPitch);
        matrix.rotateZ(rad * this.roll + invRad * this.prevRoll);
    }
}