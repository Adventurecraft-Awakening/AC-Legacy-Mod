package dev.adventurecraft.awakening.script;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

public abstract class ScriptModelBase {

    protected final List<ModelPart> boxes = new LinkedList<>();
    public ScriptEntity attachedTo;
    public ScriptModelBase modelAttachment;
    public String texture;
    public double prevX;
    public double prevY;
    public double prevZ;
    public float prevYaw;
    public float prevPitch;
    public float prevRoll;
    public float prevScaleX;
    public float prevScaleY;
    public float prevScaleZ;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public float roll;
    public float scaleX = 1.0F;
    public float scaleY = 1.0F;
    public float scaleZ = 1.0F;
    public float sizeX = 0.0F;
    public float sizeY = 0.0F;
    public float sizeZ = 0.0F;
    public float pivotX = 0.0F;
    public float pivotY = 0.0F;
    public float pivotZ = 0.0F;
    public int modes = 0;
    public float colorRed = 1.0F;
    public float colorGreen = 1.0F;
    public float colorBlue = 1.0F;
    public float colorAlpha = 1.0F;
    protected int textureWidth = 64;
    protected int textureHeight = 32;

    protected abstract void transform(float deltaTime);
    protected abstract void render(float var1);

    protected abstract void update();

    public void removeFromRendering() {
        activeModels.remove(this);
    }

    public void addToRendering() {
        activeModels.add(this);
    }

    protected static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    protected static final Matrix4f transform = new Matrix4f();
    protected static final Vector4f v = new Vector4f();
    protected static final Vector4f vr = new Vector4f();
    protected static final LinkedList<ScriptModelBase> activeModels = new LinkedList<>();

    public static void renderAll(float var0) {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        for (ScriptModelBase scriptModel : activeModels) {
            scriptModel.render(var0);
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
