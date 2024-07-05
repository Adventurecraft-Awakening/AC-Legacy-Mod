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
public class ScriptModel {

    private final List<Cuboid> boxes = new LinkedList<>();
    public ScriptEntity attachedTo;
    public ScriptModel modelAttachment;
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
    private int textureWidth = 64;
    private int textureHeight = 32;
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f transform = new Matrix4f();
    private static final Vector4f v = new Vector4f();
    private static final Vector4f vr = new Vector4f();
    private static final LinkedList<ScriptModel> activeModels = new LinkedList<>();

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

    public void addBox(String boxName, float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9) {
        this.addBoxExpanded(var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
    }
    public void addBox(float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9) {
        this.addBoxExpanded(var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
    }

    public void addBoxExpanded(String boxName, float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9, float var10) {
        this.addBoxExpanded(var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
    }
    public void addBoxExpanded(float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9, float var10) {
        Cuboid cuboid = new Cuboid(var8, var9);
        ((ExCuboid) cuboid).setTWidth(this.textureWidth);
        ((ExCuboid) cuboid).setTHeight(this.textureHeight);
        ((ExCuboid) cuboid).addBoxInverted(var2, var3, var4, var5, var6, var7, var10);
        this.boxes.add(cuboid);
    }

    public void setPosition(double x, double y, double z) {
        this.prevX = this.x = x;
        this.prevY = this.y = y;
        this.prevZ = this.z = z;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.prevYaw = this.yaw = yaw;
        this.prevPitch = this.pitch = pitch;
        this.prevRoll = this.roll = roll;
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

    private void update() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
    }

    private void transform(float deltaTime) {
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

    private void render(float var1) {
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
        transform.load(modelView);
        GL11.glPopMatrix();
        v.set(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix4f.transform(transform, v, vr);

        if (modes >= 1) {
            if(modes == 1 && attachedTo != null) {
                var position = attachedTo.getPosition();
                setBrightness(world.method_1782((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z)));
            }
        } else {
            setBrightness(world.method_1782(Math.round(vr.x), Math.round(vr.x), Math.round(vr.x)));
        }
        GL11.glColor3f(colorRed, colorGreen, colorBlue);
        GL11.glPushMatrix();
        this.transform(var1);

        for (Cuboid cuboid : this.boxes) {
            cuboid.render(1.0F / 16.0F);
        }

        GL11.glPopMatrix();
    }

    public void removeFromRendering() {
        activeModels.remove(this);
    }

    public void addToRendering() {
        activeModels.add(this);
    }

    public static void renderAll(float var0) {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        for (ScriptModel scriptModel : activeModels) {
            scriptModel.render(var0);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void updateAll() {
        for (ScriptModel scriptModel : activeModels) {
            scriptModel.update();
        }
    }

    public static void clearAll() {
        activeModels.clear();
    }
}
