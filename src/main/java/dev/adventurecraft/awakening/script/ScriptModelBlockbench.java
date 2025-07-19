package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.model.geom.ModelPart;
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
        matrix.rotateY(-(deg * -this.pitch + invDeg * -this.prevPitch));
        matrix.rotateX(deg * -this.yaw + invDeg * -this.prevYaw);

        // Apply scaling
        matrix.scale(this.scaleX, this.scaleY, this.scaleZ);

        // Move Object to intended Position
        matrix.translate(
            (float) ((-x - this.sizeX + this.pivotX) * pxSize),
            (float) ((y - this.pivotY) * pxSize),
            (float) ((-z - this.sizeZ + this.pivotZ) * pxSize));
    }

    @Override
    protected void update() {
        super.update();

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

    public void scaleTo(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;
    }

    public void scaleBy(float x, float y, float z) {
        x += this.scaleX;
        y += this.scaleY;
        z += this.scaleZ;
        this.scaleTo(x, y, z);
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
}


