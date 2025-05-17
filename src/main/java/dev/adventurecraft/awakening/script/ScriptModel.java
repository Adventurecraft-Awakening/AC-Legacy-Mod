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