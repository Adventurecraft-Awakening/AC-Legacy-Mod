package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.world.ExWorld;
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

public abstract class ScriptModelBase implements ScriptColor {

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

    // TODO: Vec3 accessors?
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float roll;

    public int colorMode = 0;
    private ScriptVec4 color = new ScriptVec4(1.0);
    private int cachedBrightnessKey = -1;

    protected int textureWidth = 64;
    protected int textureHeight = 32;

    protected void transform(float partialTick, Matrix4f matrix) {
        if (this.attachedTo != null) {
            ScriptVec3 position = this.attachedTo.getPosition(partialTick);
            ScriptVecRot rotation = this.attachedTo.getRotation(partialTick);

            matrix.translate((float) position.x, (float) position.y, (float) position.z);
            matrix.rotateY(MathF.toRadians(-rotation.yaw));
            matrix.rotateX(MathF.toRadians(rotation.pitch));
        }
        else if (this.modelAttachment != null) {
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
                    this.color.setXyz(this.modelAttachment.color);
                }
                break;
            default:
                // Default lightning values
                var vr = Matrix4f.transform(localMat, 0f, 0f, 0f, new Vector3f());
                int vX = (int) Math.floor(vr.x);
                int vY = (int) Math.floor(vr.y);
                int vZ = (int) Math.floor(vr.z);

                int key = ((ExWorld) world).getLightUpdateHash(vX, vY, vZ);
                if (this.cachedBrightnessKey != key) {
                    this.cachedBrightnessKey = key;
                    this.setBrightness(world.getBrightness(vX, vY, vZ));
                }
                break;
        }

        ScriptVec4 color = this.getColor();
        float r = (float) color.getR();
        float g = (float) color.getG();
        float b = (float) color.getB();
        float a = (float) color.getA();
        if (a < 1.0) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glColor4f(r, g, b, Math.min(a, 1.0F));
        }
        else {
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

        if (a < 1.0) {
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
        this.setRGB(brightness, brightness, brightness);
    }

    public void setBrightness(int brightness) {
        this.setBrightness(Math.max(brightness, 255) / 256.0F);
    }

    @Deprecated
    public void setRGB(double r, double g, double b) {
        this.color.setR(r);
        this.color.setG(g);
        this.color.setB(b);
    }

    @Deprecated
    public void setRGBA(double r, double g, double b, double a) {
        this.color.setR(r);
        this.color.setG(g);
        this.color.setB(b);
        this.color.setA(a);
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
        double tmp = x * cosYaw + z * sinYaw;
        z = z * cosYaw - x * sinYaw;
        x = tmp;

        float sinPitch = MathF.sin(pitch);
        float cosPitch = MathF.cos(pitch);
        tmp = z * cosPitch + y * sinPitch;
        y = y * cosPitch - z * sinPitch;
        z = tmp;

        float sinRoll = MathF.sin(roll);
        float cosRoll = MathF.cos(roll);
        tmp = y * cosRoll + x * sinRoll;
        x = x * cosRoll - y * sinRoll;

        x += this.x;
        tmp += this.y;
        z += this.z;
        this.moveTo(x, tmp, z);
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

    @Override
    public ScriptVec4 getColor() {
        return this.color;
    }

    @Override
    public void setColor(ScriptVec4 value) {
        if (value == null) {
            value = new ScriptVec4(1.0F);
        }
        this.color = value;
    }

    @Deprecated
    public double getColorRed() {
        return this.color.getR();
    }

    @Deprecated
    public void setColorRed(double value) {
        this.color.setR(value);
    }

    @Deprecated
    public double getColorGreen() {
        return this.color.getG();
    }

    @Deprecated
    public void setColorGreen(double value) {
        this.color.setG(value);
    }

    @Deprecated
    public double getColorBlue() {
        return this.color.getB();
    }

    @Deprecated
    public void setColorBlue(double value) {
        this.color.setB(value);
    }

    @Deprecated
    public double getColorAlpha() {
        return this.color.getA();
    }

    @Deprecated
    public void setColorAlpha(double value) {
        this.color.setA(value);
    }

    @Deprecated
    public int getModes() {
        return this.colorMode;
    }

    @Deprecated
    public void setModes(int value) {
        this.colorMode = value;
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
