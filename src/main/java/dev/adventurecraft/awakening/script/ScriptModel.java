package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.util.MathF;
import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Textures;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;
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

    public ScriptVec3 getPosition() {
        return new ScriptVec3(x, y, z);
    }

    public void setPosition(double x, double y, double z) {
        this.prevX = this.x = x;
        this.prevY = this.y = y;
        this.prevZ = this.z = z;
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
    protected void update() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.prevRoll = this.roll;
    }

    @Override
    protected void transform(float deltaTime) {
        super.transform(deltaTime);

        float invDelta = 1.0F - deltaTime;
        double x = deltaTime * this.x + invDelta * this.prevX;
        double y = deltaTime * this.y + invDelta * this.prevY;
        double z = deltaTime * this.z + invDelta * this.prevZ;
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(deltaTime * this.yaw + invDelta * this.prevYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(deltaTime * this.pitch + invDelta * this.prevPitch, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(deltaTime * this.roll + invDelta * this.prevRoll, 0.0F, 0.0F, 1.0F);
    }

    @Override
    protected void render(float partialTick) {
        if (boxes.isEmpty()) {
            return;
        }
        Level world = Minecraft.instance.level;
        Textures var3 = Minecraft.instance.textures;
        if (this.texture != null && !this.texture.isEmpty()) {
            var3.bind(var3.loadTexture(this.texture));
        }

        // TODO: clean up?
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        this.transform(partialTick);

        modelView.rewind();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);

        transform.load(modelView);
        GL11.glPopMatrix();
        v.set(0.0F, 0.0F, 0.0F, 1.0F);
        Matrix4f.transform(transform, v, vr);

        switch (this.modes) {
            case 1:
                //using the position of the attached entity
                if (this.attachedTo != null) {
                    this.setBrightness(this.attachedTo.entity.getBrightness(partialTick));
                }
                break;
            case 2:
                //usage for custom RGB Values
                break;
            case 3:
                //use the lightning value of the attached model
                if (this.modelAttachment != null) {
                    this.colorRed = this.modelAttachment.colorRed;
                    this.colorGreen = this.modelAttachment.colorGreen;
                    this.colorBlue = this.modelAttachment.colorBlue;
                }
                break;
            default:
                //Default lightning values
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
        GL11.glPushMatrix();
        this.transform(partialTick);

        for (ModelPart cuboid : this.boxes) {
            cuboid.render(1.0F / 16.0F);
        }

        GL11.glPopMatrix();
        if (this.colorAlpha < 1.0) {
            GL11.glDisable(GL11.GL_BLEND);
        }
    }
}