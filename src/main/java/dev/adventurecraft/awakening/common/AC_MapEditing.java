package dev.adventurecraft.awakening.common;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.GLU;

public class AC_MapEditing {
    public Minecraft mc;
    public World world;
    public HitResult cursor;
    private BlockRenderer renderBlocks;
    public int selectedBlockID;
    public int selectedMetadata;

    public AC_MapEditing(Minecraft mc, World world) {
        this.mc = mc;
        this.world = world;
        this.renderBlocks = new BlockRenderer(world);
    }

    public void updateWorld(World world) {
        this.world = world;
        this.renderBlocks.blockView = world;
    }

    public void updateCursor(LivingEntity entity, float var2, float deltaTime) {
        if (this.mc.hasFocus) {
            this.cursor = null;
            return;
        }

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);
        FloatBuffer projMatrix = BufferUtils.createFloatBuffer(16);
        FloatBuffer objPos = BufferUtils.createFloatBuffer(3);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        float mouseXf = (float) mouseX;
        float mouseYf = (float) mouseY;
        GLU.gluUnProject(mouseXf, mouseYf, 1.0F, modelMatrix, projMatrix, viewport, objPos);
        Vec3d start = entity.getPosition(deltaTime);
        Vec3d end = start.translate(objPos.get(0) * 1024.0F, objPos.get(1) * 1024.0F, objPos.get(2) * 1024.0F);
        this.cursor = this.world.method_160(start, end);
    }

    public void paint() {
        if (this.cursor == null || this.cursor.type != HitType.field_789) {
            return;
        }
        int x = this.cursor.x + this.getCursorXOffset();
        int y = this.cursor.y + this.getCursorYOffset();
        int z = this.cursor.z + this.getCursorZOffset();
        this.setBlock(x, y, z, this.selectedBlockID, this.selectedMetadata);
    }

    public int getCursorXOffset() {
        return this.cursor.field_1987 == 4 ? -1 : (this.cursor.field_1987 == 5 ? 1 : 0);
    }

    public int getCursorYOffset() {
        return this.cursor.field_1987 == 0 ? -1 : (this.cursor.field_1987 == 1 ? 1 : 0);
    }

    public int getCursorZOffset() {
        return this.cursor.field_1987 == 2 ? -1 : (this.cursor.field_1987 == 3 ? 1 : 0);
    }

    private void setBlock(int x, int y, int z, int id, int meta) {
        if (y >= 0 && y < 128) {
            this.world.placeBlockWithMetaData(x, y, z, id, meta);
        }
    }

    public void render(float deltaTime) {
        LivingEntity entity = Minecraft.instance.viewEntity;
        if (this.mc.hasFocus) {
            return;
        }
        this.drawCursor(entity, deltaTime);
        if (this.cursor == null) {
            return;
        }
        float prX = (float) (entity.prevRenderX + (entity.x - entity.prevRenderX) * (double) deltaTime);
        float prY = (float) (entity.prevRenderY + (entity.y - entity.prevRenderY) * (double) deltaTime);
        float prZ = (float) (entity.prevRenderZ + (entity.z - entity.prevRenderZ) * (double) deltaTime);
        GL11.glPushMatrix();
        GL11.glTranslatef(-prX, -prY, -prZ);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId("/terrain.png"));
        ((ExBlockRenderer) this.renderBlocks).startRenderingBlocks(this.world);
        this.drawBlock(this.cursor.x + this.getCursorXOffset(), this.cursor.y + this.getCursorYOffset(), this.cursor.z + this.getCursorZOffset(), this.selectedBlockID, this.selectedMetadata);
        ((ExBlockRenderer) this.renderBlocks).stopRenderingBlocks();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public void renderSelection(float deltaTime) {
        if (!AC_ItemCursor.bothSet) {
            return;
        }
        LivingEntity entity = Minecraft.instance.viewEntity;
        float prX = (float) (entity.prevRenderX + (entity.x - entity.prevRenderX) * (double) deltaTime);
        float prY = (float) (entity.prevRenderY + (entity.y - entity.prevRenderY) * (double) deltaTime);
        float prZ = (float) (entity.prevRenderZ + (entity.z - entity.prevRenderZ) * (double) deltaTime);
        GL11.glPushMatrix();
        GL11.glTranslatef(-prX, -prY, -prZ);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        Vec3d rot = entity.getRotation();
        int rX = (int) (entity.x + (double) AC_DebugMode.reachDistance * rot.x) - AC_ItemCursor.minX;
        int rY = (int) (entity.y + (double) AC_DebugMode.reachDistance * rot.y) - AC_ItemCursor.minY;
        int rZ = (int) (entity.z + (double) AC_DebugMode.reachDistance * rot.z) - AC_ItemCursor.minZ;

        for (int texIndex = 0; texIndex <= 3; ++texIndex) {
            if (texIndex == 0) {
                this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId("/terrain.png"));
            } else {
                this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId(String.format("/terrain%d.png", texIndex)));
            }

            ((ExBlockRenderer) this.renderBlocks).startRenderingBlocks(this.world);

            for (int x = AC_ItemCursor.minX; x <= AC_ItemCursor.maxX; ++x) {
                for (int y = AC_ItemCursor.minY; y <= AC_ItemCursor.maxY; ++y) {
                    for (int z = AC_ItemCursor.minZ; z <= AC_ItemCursor.maxZ; ++z) {
                        int id = this.mc.world.getBlockId(x, y, z);
                        Block block = Block.BY_ID[id];
                        if (block != null && ((ExBlock) block).getTextureNum() == texIndex) {
                            int meta = this.mc.world.getBlockMeta(x, y, z);
                            this.drawBlock(x + rX, y + rY, z + rZ, id, meta);
                        }
                    }
                }
            }

            ((ExBlockRenderer) this.renderBlocks).stopRenderingBlocks();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawBlock(int x, int y, int z, int id, int meta) {
        Block block = Block.BY_ID[id];
        if (block == null) {
            return;
        }
        int prevId = this.world.getBlockId(x, y, z);
        int prevMeta = this.world.getBlockMeta(x, y, z);
        ((ExWorld) this.world).setBlockAndMetadataTemp(x, y, z, id, meta);
        this.renderBlocks.render(block, x, y, z);
        ((ExWorld) this.world).setBlockAndMetadataTemp(x, y, z, prevId, prevMeta);
    }

    private void drawBox(AxixAlignedBoundingBox aabb) {
        Tessellator ts = Tessellator.INSTANCE;
        ts.start(GL11.GL_LINE_STRIP);
        ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        ts.tessellate();
        ts.start(GL11.GL_LINE_STRIP);
        ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        ts.tessellate();
        ts.start(GL11.GL_LINES);
        ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
        ts.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
        ts.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        ts.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
        ts.tessellate();
    }

    private void drawCursor(LivingEntity entity, float deltaTime) {
        if (this.cursor == null || this.cursor.type != HitType.field_789) {
            return;
        }

        int id = this.world.getBlockId(this.cursor.x, this.cursor.y, this.cursor.z);
        if (id <= 0) {
            return;
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        Block block = Block.BY_ID[id];
        block.updateBoundingBox(this.world, this.cursor.x, this.cursor.y, this.cursor.z);
        double prX = entity.prevRenderX + (entity.x - entity.prevRenderX) * (double) deltaTime;
        double prY = entity.prevRenderY + (entity.y - entity.prevRenderY) * (double) deltaTime;
        double prZ = entity.prevRenderZ + (entity.z - entity.prevRenderZ) * (double) deltaTime;
        float boxSize = 0.002F;
        AxixAlignedBoundingBox aabb = block
            .getOutlineShape(this.world, this.cursor.x, this.cursor.y, this.cursor.z)
            .expand(boxSize, boxSize, boxSize)
            .duplicateAndTranslate(-prX, -prY, -prZ);
        this.drawBox(aabb);
        GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(4.0F);
        Tessellator ts = Tessellator.INSTANCE;
        ts.start(GL11.GL_LINE_STRIP);
        if (this.cursor.field_1987 == 0) {
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        } else if (this.cursor.field_1987 == 1) {
            ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
        } else if (this.cursor.field_1987 == 2) {
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        } else if (this.cursor.field_1987 == 3) {
            ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
        } else if (this.cursor.field_1987 == 4) {
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.minX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.minX, aabb.minY, aabb.minZ);
        } else if (this.cursor.field_1987 == 5) {
            ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.minZ);
            ts.addVertex(aabb.maxX, aabb.maxY, aabb.maxZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.maxZ);
            ts.addVertex(aabb.maxX, aabb.minY, aabb.minZ);
        }

        ts.tessellate();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void setBlock(int id, int meta) {
        this.selectedBlockID = id;
        this.selectedMetadata = meta;
    }
}
