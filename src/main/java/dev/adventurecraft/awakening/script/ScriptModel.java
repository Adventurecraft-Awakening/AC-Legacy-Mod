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
import java.util.HashMap;
import java.util.LinkedList;

@SuppressWarnings("unused")
public class ScriptModel {

    HashMap<String, Cuboid> boxes;
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
    private int textureWidth;
    private int textureHeight;
    private static FloatBuffer modelview = BufferUtils.createFloatBuffer(16);
    private static Matrix4f transform = new Matrix4f();
    private static Vector4f v = new Vector4f();
    private static Vector4f vr = new Vector4f();
    static LinkedList<ScriptModel> activeModels = new LinkedList<>();

    public ScriptModel() {
        this(64, 32);
    }

    public ScriptModel(int var1, int var2) {
        this.boxes = new HashMap<>();
        this.addToRendering();
        this.textureWidth = var1;
        this.textureHeight = var2;
    }

    public void addBox(String var1, float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9) {
        this.addBoxExpanded(var1, var2, var3, var4, var5, var6, var7, var8, var9, 0.0F);
    }

    public void addBoxExpanded(String var1, float var2, float var3, float var4, int var5, int var6, int var7, int var8, int var9, float var10) {
        Cuboid var11 = new Cuboid(var8, var9);
        ((ExCuboid) var11).setTWidth(this.textureWidth);
        ((ExCuboid) var11).setTHeight(this.textureHeight);
        ((ExCuboid) var11).addBoxInverted(var2, var3, var4, var5, var6, var7, var10);
        this.boxes.put(var1, var11);
    }

    public void setPosition(double var1, double var3, double var5) {
        this.prevX = this.x = var1;
        this.prevY = this.y = var3;
        this.prevZ = this.z = var5;
    }

    public void setRotation(float var1, float var2, float var3) {
        this.prevYaw = this.yaw = var1;
        this.prevPitch = this.pitch = var2;
        this.prevRoll = this.roll = var3;
    }

    public void moveTo(double var1, double var3, double var5) {
        this.x = var1;
        this.y = var3;
        this.z = var5;
    }

    public void moveBy(double var1, double var3, double var5) {
        double var7 = Math.toRadians(this.yaw);
        double var9 = Math.toRadians(this.pitch);
        double var11 = Math.toRadians(this.roll);
        double var13 = var1 * Math.cos(var7) + var5 * Math.sin(var7);
        var5 = var5 * Math.cos(var7) - var1 * Math.sin(var7);
        var1 = var13;
        var13 = var5 * Math.cos(var9) + var3 * Math.sin(var9);
        var3 = var3 * Math.cos(var9) - var5 * Math.sin(var9);
        var5 = var13;
        var13 = var3 * Math.cos(var11) + var1 * Math.sin(var11);
        var1 = var1 * Math.cos(var11) - var3 * Math.sin(var11);
        this.x += var1;
        this.y += var13;
        this.z += var5;
    }

    public void rotateTo(float var1, float var2, float var3) {
        this.yaw = var1;
        this.pitch = var2;
        this.roll = var3;
    }

    public void rotateBy(float var1, float var2, float var3) {
        this.yaw += var1;
        this.pitch += var2;
        this.roll += var3;
    }

    private void update() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
    }

    private void transform(float var1) {
        if (this.attachedTo != null) {
            ScriptVec3 var2 = this.attachedTo.getPosition(var1);
            ScriptVecRot var3 = this.attachedTo.getRotation(var1);
            GL11.glTranslated(var2.x, var2.y, var2.z);
            GL11.glRotatef((float) (-var3.yaw), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float) var3.pitch, 1.0F, 0.0F, 0.0F);
        } else if (this.modelAttachment != null) {
            this.modelAttachment.transform(var1);
        }

        float var9 = 1.0F - var1;
        double var10 = (double) var1 * this.x + (double) var9 * this.prevX;
        double var5 = (double) var1 * this.y + (double) var9 * this.prevY;
        double var7 = (double) var1 * this.z + (double) var9 * this.prevZ;
        GL11.glTranslated(var10, var5, var7);
        GL11.glRotatef(var1 * this.yaw + var9 * this.prevYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(var1 * this.pitch + var9 * this.prevPitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(var1 * this.roll + var9 * this.prevRoll, 0.0F, 0.0F, 1.0F);
    }

    private void render(float var1) {
        World var2 = Minecraft.instance.world;
        TextureManager var3 = Minecraft.instance.textureManager;
        if (this.texture != null && !this.texture.equals("")) {
            var3.bindTexture(var3.getTextureId(this.texture));
        }

        // TODO: clean up?
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        this.transform(var1);
        modelview.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelview);
        transform.load(modelview);
        GL11.glPopMatrix();
        v.set(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix4f.transform(transform, v, vr);
        float var4 = var2.method_1782(Math.round(vr.x), Math.round(vr.y), Math.round(vr.z));
        GL11.glColor3f(var4, var4, var4);
        GL11.glPushMatrix();
        this.transform(var1);

        for (Cuboid var6 : this.boxes.values()) {
            var6.render(1.0F / 16.0F);
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

        for (ScriptModel var2 : activeModels) {
            var2.render(var0);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void updateAll() {
        for (ScriptModel var1 : activeModels) {
            var1.update();
        }
    }

    public static void clearAll() {
        activeModels.clear();
    }
}
