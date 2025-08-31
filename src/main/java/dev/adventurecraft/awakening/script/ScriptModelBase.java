package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.util.MathF;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

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

    public int colorMode = 0;
    public float r = 1.0F;
    public float g = 1.0F;
    public float b = 1.0F;
    public float a = 1.0F;

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
        Textures texMan = Minecraft.instance.textures;
        if (this.texture != null && !this.texture.isEmpty()) {
            texMan.bind(texMan.loadTexture(this.texture));
        }

        var localMat = new Matrix4f();
        this.transform(partialTick, localMat);

        switch (this.colorMode) {
            case 1:
                // using the position of the attached entity
                if (this.attachedTo != null && this.attachedTo.entity != null) {
                    this.setBrightness(this.attachedTo.entity.getBrightness(partialTick));
                }
                //if the entity DON'T exist it's like setting it to mode 2
                break;
            case 2:
                // usage for custom RGB Values
                break;
            case 3:
                // use the lightning value of the attached model
                if (this.modelAttachment != null) {
                    this.r = this.modelAttachment.r;
                    this.g = this.modelAttachment.g;
                    this.b = this.modelAttachment.b;
                }
                break;
            default:
                // Default lightning values
                var vr = Matrix4f.transform(localMat, new Vector3f(), new Vector3f());
                setBrightness(world.getBrightness(
                    Math.round(vr.x + 0.5F),
                    Math.round(vr.y + 0.5F),
                    Math.round(vr.z + 0.5F)
                ));
                break;
        }

        if (this.a < 1.0) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(r, g, b, Math.min(this.a, 1.0F));
        } else {
            GL11.glColor3f(r, g, b);
        }

        Matrix4f.mul(transform, localMat, localMat);
        try (var stack = MemoryStack.stackPush()) {
            var matBuf = stack.mallocFloat(16);

            var mat = new Matrix4f();
            for (ModelPart cuboid : this.boxes) {
                var exCuboid = (ExCuboid) cuboid;
                if (!exCuboid.canRender()) {
                    continue;
                }

                mat.load(localMat);
                exCuboid.translateTo(mat);

                mat.store(matBuf);
                GL11.glLoadMatrixf(matBuf.flip());
                exCuboid.render();
            }
        }

        if (this.a < 1.0) {
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
        setRGB(brightness, brightness, brightness);
    }

    public void setBrightness(int brightness) {
        this.setBrightness(Math.max(brightness, 255) / 256.0F);
    }

    public void setRGB(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void setRGBA(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
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

    public float getAlpha() {
        return this.a;
    }

    public void setAlpha(float alpha) {
        this.a = alpha;
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
