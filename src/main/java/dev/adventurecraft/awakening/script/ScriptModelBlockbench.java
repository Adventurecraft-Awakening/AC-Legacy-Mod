package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.util.vector.Matrix4f;

public class ScriptModelBlockbench extends ScriptModelBase {

    private static final float PIXEL_SIZE = 0.0625F; // 1 / 16

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

    public double worldX = 0.0D;
    public double worldY = 0.0D;
    public double worldZ = 0.0D;

    public float worldYaw = 0.0F;
    public float worldPitch = 0.0F;
    public float worldRoll = 0.0F;

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

        matrix.translate(
            (float) this.worldX,
            (float) this.worldY,
            (float) this.worldZ
        );

        matrix.rotateY(MathF.toRadians(this.worldYaw));
        matrix.rotateX(MathF.toRadians(this.worldPitch));
        matrix.rotateZ(MathF.toRadians(this.worldRoll));

        matrix.translate(
            -this.pivotX * PIXEL_SIZE,
            this.pivotY * PIXEL_SIZE,
            -this.pivotZ * PIXEL_SIZE
        );

        float interpolatedRoll = partialTick * this.roll + invDelta * this.prevRoll;
        float interpolatedPitch = partialTick * this.pitch + invDelta * this.prevPitch;
        float interpolatedYaw = partialTick * this.yaw + invDelta * this.prevYaw;

        matrix.rotateZ(-MathF.toRadians(interpolatedRoll));
        matrix.rotateY(MathF.toRadians(interpolatedPitch));
        matrix.rotateX(-MathF.toRadians(interpolatedYaw));

        /*
         * Apply model scale.
         */
        matrix.scale(this.scaleX, this.scaleY, this.scaleZ);

        matrix.translate(
            (float) ((-x - this.sizeX + this.pivotX) * PIXEL_SIZE),
            (float) (( y - this.pivotY) * PIXEL_SIZE),
            (float) ((-z - this.sizeZ + this.pivotZ) * PIXEL_SIZE)
        );
    }

    @Override
    protected void update() {
        super.update();

        this.prevScaleX = this.scaleX;
        this.prevScaleY = this.scaleY;
        this.prevScaleZ = this.scaleZ;
    }

    public void addBox(
        int width,
        int height,
        int length,
        int textureOffsetX,
        int textureOffsetY
    ) {
        this.addBoxInflated(width, height, length, textureOffsetX, textureOffsetY, 0.0F);
    }

    public void addBoxInflated(
        int width,
        int height,
        int length,
        int textureOffsetX,
        int textureOffsetY,
        float inflate
    ) {
        this.setSize(width, height, length);

        var cuboid = new ModelPart(textureOffsetX, textureOffsetY);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(0, 0, 0, width, height, length, inflate);

        this.boxes.add(cuboid);
    }

    public void scaleTo(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }

    public void scaleBy(float x, float y, float z) {
        this.scaleTo(
            this.scaleX + x,
            this.scaleY + y,
            this.scaleZ + z
        );
    }

    public void setScale(float x, float y, float z) {
        this.scaleTo(x, y, z);

        this.prevScaleX = this.scaleX;
        this.prevScaleY = this.scaleY;
        this.prevScaleZ = this.scaleZ;
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
        this.setPivot((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public ScriptVec3 getPivot() {
        return new ScriptVec3(this.pivotX, this.pivotY, this.pivotZ);
    }

    public void setWorldPosition(double x, double y, double z) {
        this.worldX = x;
        this.worldY = y;
        this.worldZ = z;
    }

    public ScriptVec3 getWorldPosition() {
        return new ScriptVec3(this.worldX, this.worldY, this.worldZ);
    }

    public void moveWorldPosition(double x, double y, double z) {
        this.setWorldPosition(
            this.worldX + x,
            this.worldY + y,
            this.worldZ + z
        );
    }

    public void setWorldRotation(float yaw, float pitch, float roll) {
        this.worldYaw = yaw;
        this.worldPitch = pitch;
        this.worldRoll = roll;
    }

    public ScriptVec3 getWorldRotation() {
        return new ScriptVec3(this.worldYaw, this.worldPitch, this.worldRoll);
    }

    public void rotateWorld(float yaw, float pitch, float roll) {
        this.setWorldRotation(
            this.worldYaw + yaw,
            this.worldPitch + pitch,
            this.worldRoll + roll
        );
    }

    public void setWorldTransform(
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        float roll
    ) {
        this.worldX = x;
        this.worldY = y;
        this.worldZ = z;

        this.worldYaw = yaw;
        this.worldPitch = pitch;
        this.worldRoll = roll;
    }
}