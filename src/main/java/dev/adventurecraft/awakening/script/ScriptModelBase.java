package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;

import java.util.ArrayList;

import net.minecraft.client.model.geom.ModelPart;
import org.lwjgl.util.vector.Vector3f;

public abstract class ScriptModelBase {

    protected final ArrayList<ModelPart> boxes = new ArrayList<>();

    public ScriptEntity attachedTo;
    public ScriptModelBase modelAttachment;
    public String texture;

    public double prevX;
    public double prevY;
    public double prevZ;
    public float prevYaw;
    public float prevPitch;
    public float prevRoll;

    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float roll;

    public int modes = 0;
    public float colorRed = 1.0F;
    public float colorGreen = 1.0F;
    public float colorBlue = 1.0F;
    public float colorAlpha = 1.0F;

    protected int textureWidth = 64;
    protected int textureHeight = 32;

    protected void transform(float partialTick, Matrix4f matrix) {
        if (this.attachedTo != null) {
            ScriptVec3 position = this.attachedTo.getPosition(partialTick);
            ScriptVecRot rotation = this.attachedTo.getRotation(partialTick);

            matrix.translate((float) position.x, (float) position.y, (float) position.z);
            matrix.rotateY(MathF.toRadians(-rotation.yaw));
            matrix.rotateX(MathF.toRadians(rotation.pitch));
        } else if (this.modelAttachment != null) {
            this.modelAttachment.transform(partialTick, matrix);
        }
    }

    protected void render(float partialTick, Matrix4f transform) {
        if (boxes.isEmpty()) {
            return;
        }
        Level world = Minecraft.instance.level;
        Textures var3 = Minecraft.instance.textures;
        if (this.texture != null && !this.texture.isEmpty()) {
            var3.bind(var3.loadTexture(this.texture));
        }

        var localMat = new Matrix4f();
        this.transform(partialTick, localMat);

        switch (this.modes) {
            case 1:
                // using the position of the attached entity
                if (this.attachedTo != null) {
                    this.setBrightness(this.attachedTo.entity.getBrightness(partialTick));
                }
                break;
            case 2:
                // usage for custom RGB Values
                break;
            case 3:
                // use the lightning value of the attached model
                if (this.modelAttachment != null) {
                    this.colorRed = this.modelAttachment.colorRed;
                    this.colorGreen = this.modelAttachment.colorGreen;
                    this.colorBlue = this.modelAttachment.colorBlue;
                }
                break;
            default:
                // Default lightning values
                var vr = Matrix4f.transform(localMat, new Vector3f(), new Vector3f());
                setBrightness(world.getBrightness(Math.round(vr.x), Math.round(vr.y), Math.round(vr.z)));
                break;
        }

        float r = Math.min(this.colorRed, 1.0F);
        float g = Math.min(this.colorGreen, 1.0F);
        float b = Math.min(this.colorBlue, 1.0F);
        if (this.colorAlpha < 1.0) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(r, g, b, Math.min(this.colorAlpha, 1.0F));
        } else {
            GL11.glColor3f(r, g, b);
        }

        var cuboidMat = Matrix4f.mul(transform, localMat, localMat);
        try (var stack = MemoryStack.stackPush()) {
            var matBuf = stack.mallocFloat(16);

            for (ModelPart cuboid : this.boxes) {
                var mat = new Matrix4f(cuboidMat);
                ((ExCuboid) cuboid).translateTo(mat);

                mat.store(matBuf);
                GL11.glLoadMatrixf(matBuf.flip());
                ((ExCuboid) cuboid).render();
            }
        }

        if (this.colorAlpha < 1.0) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }

    protected void update() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;

        this.prevRoll = this.roll;
        this.prevPitch = this.pitch;
        this.prevYaw = this.yaw;
    }

    public void removeFromRendering() {
        activeModels.remove(this);
    }

    public void addToRendering() {
        activeModels.add(this);
    }

    public void setBrightness(float brightness) {
        this.colorRed = this.colorGreen = this.colorBlue = brightness;
    }

    public void setBrightness(int brightness) {
        this.setBrightness(Math.max(brightness, 255) / 256.0F);
    }

    public void setPosition(double x, double y, double z) {
        this.moveTo(x, y, z);
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
    }

    public void setPosition(ScriptVec3 vec) {
        this.setPosition(vec.x, vec.y, vec.z);
    }

    public ScriptVec3 getPosition() {
        return new ScriptVec3(x, y, z);
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

        x += this.x;
        tempY += this.y;
        z += this.z;
        this.moveTo(x, tempY, z);
    }

    public void rotateTo(float yaw, float pitch, float roll) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public void rotateBy(float yaw, float pitch, float roll) {
        yaw += this.yaw;
        pitch += this.pitch;
        roll += this.roll;
        this.rotateTo(yaw, pitch, roll);
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.rotateTo(yaw, pitch, roll);
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
    }

    public void setRotation(ScriptVec3 vec) {
        this.setRotation((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public ScriptVec3 getRotation() {
        return new ScriptVec3(this.yaw, this.pitch, this.roll);
    }

    public void setAlpha(float alpha) {
        this.colorAlpha = alpha;
    }

    public float getAlpha() {
        return this.colorAlpha;
    }

    protected static final ArrayList<ScriptModelBase> activeModels = new ArrayList<>();

    public static void renderAll(float partialTick, Matrix4f transform) {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        for (ScriptModelBase scriptModel : activeModels) {
            scriptModel.render(partialTick, transform);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void updateAll() {
        for (ScriptModelBase scriptModel : activeModels) {
            scriptModel.update();
        }
    }

    public static void clearAll() {
        activeModels.clear();
    }
}
