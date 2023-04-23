package dev.adventurecraft.awakening.common;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
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

    public AC_MapEditing(Minecraft var1, World var2) {
        this.mc = var1;
        this.world = var2;
        this.renderBlocks = new BlockRenderer(var2);
    }

    public void updateWorld(World var1) {
        this.world = var1;
        this.renderBlocks.blockView = var1;
    }

    public void updateCursor(LivingEntity var1, float var2, float var3) {
        if (this.mc.hasFocus) {
            this.cursor = null;
        } else {
            int var4 = Mouse.getX();
            int var5 = Mouse.getY();
            IntBuffer var6 = BufferUtils.createIntBuffer(16);
            FloatBuffer var7 = BufferUtils.createFloatBuffer(16);
            FloatBuffer var8 = BufferUtils.createFloatBuffer(16);
            FloatBuffer var11 = BufferUtils.createFloatBuffer(3);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, var7);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, var8);
            GL11.glGetInteger(GL11.GL_VIEWPORT, var6);
            float var9 = (float) var4;
            float var10 = (float) var5;
            GLU.gluUnProject(var9, var10, 1.0F, var7, var8, var6, var11);
            Vec3d var12 = var1.getPosition(var3);
            Vec3d var13 = var12.translate((double) (var11.get(0) * 1024.0F), (double) (var11.get(1) * 1024.0F), (double) (var11.get(2) * 1024.0F));
            this.cursor = this.world.method_160(var12, var13);
        }
    }

    public void paint() {
        if (this.cursor != null && this.cursor.type == HitType.field_789) {
            int var1 = this.cursor.x;
            int var2 = this.cursor.y;
            int var3 = this.cursor.z;
            this.setBlock(var1 + this.getCursorXOffset(), var2 + this.getCursorYOffset(), var3 + this.getCursorZOffset(), this.selectedBlockID, this.selectedMetadata);
        }
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

    private void setBlock(int var1, int var2, int var3, int var4, int var5) {
        if (var2 >= 0 && var2 < 128) {
            this.world.placeBlockWithMetaData(var1, var2, var3, var4, var5);
        }
    }

    public void render(float var1) {
        LivingEntity var2 = Minecraft.instance.viewEntity;
        if (this.mc.hasFocus) {
            return;
        }
        this.drawCursor(var2, var1);
        if (this.cursor == null) {
            return;
        }
        float var3 = (float) (var2.prevRenderX + (var2.x - var2.prevRenderX) * (double) var1);
        float var4 = (float) (var2.prevRenderY + (var2.y - var2.prevRenderY) * (double) var1);
        float var5 = (float) (var2.prevRenderZ + (var2.z - var2.prevRenderZ) * (double) var1);
        GL11.glPushMatrix();
        GL11.glTranslatef(-var3, -var4, -var5);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId("/terrain.png"));
        ((ExBlockRenderer)this.renderBlocks).startRenderingBlocks(this.world);
        this.drawBlock(this.cursor.x + this.getCursorXOffset(), this.cursor.y + this.getCursorYOffset(), this.cursor.z + this.getCursorZOffset(), this.selectedBlockID, this.selectedMetadata);
        ((ExBlockRenderer)this.renderBlocks).stopRenderingBlocks();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public void renderSelection(float var1) {
        if (!AC_ItemCursor.bothSet) {
            return;
        }
        LivingEntity var2 = Minecraft.instance.viewEntity;
        float var3 = (float) (var2.prevRenderX + (var2.x - var2.prevRenderX) * (double) var1);
        float var4 = (float) (var2.prevRenderY + (var2.y - var2.prevRenderY) * (double) var1);
        float var5 = (float) (var2.prevRenderZ + (var2.z - var2.prevRenderZ) * (double) var1);
        GL11.glPushMatrix();
        GL11.glTranslatef(-var3, -var4, -var5);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        Vec3d var6 = var2.getRotation();
        int var7 = (int) (var2.x + (double) AC_DebugMode.reachDistance * var6.x) - AC_ItemCursor.minX;
        int var8 = (int) (var2.y + (double) AC_DebugMode.reachDistance * var6.y) - AC_ItemCursor.minY;
        int var9 = (int) (var2.z + (double) AC_DebugMode.reachDistance * var6.z) - AC_ItemCursor.minZ;

        for (int var10 = 0; var10 <= 3; ++var10) {
            if (var10 == 0) {
                this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId("/terrain.png"));
            } else {
                this.mc.textureManager.bindTexture(this.mc.textureManager.getTextureId(String.format("/terrain%d.png", var10)));
            }

            ((ExBlockRenderer)this.renderBlocks).startRenderingBlocks(this.world);

            for (int var11 = AC_ItemCursor.minX; var11 <= AC_ItemCursor.maxX; ++var11) {
                for (int var12 = AC_ItemCursor.minY; var12 <= AC_ItemCursor.maxY; ++var12) {
                    for (int var13 = AC_ItemCursor.minZ; var13 <= AC_ItemCursor.maxZ; ++var13) {
                        int var14 = this.mc.world.getBlockId(var11, var12, var13);
                        if (Block.BY_ID[var14] != null && ((ExBlock)Block.BY_ID[var14]).getTextureNum() == var10) {
                            int var15 = this.mc.world.getBlockMeta(var11, var12, var13);
                            this.drawBlock(var11 + var7, var12 + var8, var13 + var9, var14, var15);
                        }
                    }
                }
            }

            ((ExBlockRenderer)this.renderBlocks).stopRenderingBlocks();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawBlock(int var1, int var2, int var3, int var4, int var5) {
        Block var6 = Block.BY_ID[var4];
        if (var6 == null) {
            return;
        }
        int var7 = this.world.getBlockId(var1, var2, var3);
        int var8 = this.world.getBlockMeta(var1, var2, var3);
        //this.world.setBlockAndMetadataTemp(var1, var2, var3, var4, var5); TODO
        this.renderBlocks.render(var6, var1, var2, var3);
        //this.world.setBlockAndMetadataTemp(var1, var2, var3, var7, var8); TODO
    }

    private void drawBox(AxixAlignedBoundingBox var1) {
        Tessellator var2 = Tessellator.INSTANCE;
        var2.start(3);
        var2.addVertex(var1.minX, var1.minY, var1.minZ);
        var2.addVertex(var1.maxX, var1.minY, var1.minZ);
        var2.addVertex(var1.maxX, var1.minY, var1.maxZ);
        var2.addVertex(var1.minX, var1.minY, var1.maxZ);
        var2.addVertex(var1.minX, var1.minY, var1.minZ);
        var2.tessellate();
        var2.start(3);
        var2.addVertex(var1.minX, var1.maxY, var1.minZ);
        var2.addVertex(var1.maxX, var1.maxY, var1.minZ);
        var2.addVertex(var1.maxX, var1.maxY, var1.maxZ);
        var2.addVertex(var1.minX, var1.maxY, var1.maxZ);
        var2.addVertex(var1.minX, var1.maxY, var1.minZ);
        var2.tessellate();
        var2.start(1);
        var2.addVertex(var1.minX, var1.minY, var1.minZ);
        var2.addVertex(var1.minX, var1.maxY, var1.minZ);
        var2.addVertex(var1.maxX, var1.minY, var1.minZ);
        var2.addVertex(var1.maxX, var1.maxY, var1.minZ);
        var2.addVertex(var1.maxX, var1.minY, var1.maxZ);
        var2.addVertex(var1.maxX, var1.maxY, var1.maxZ);
        var2.addVertex(var1.minX, var1.minY, var1.maxZ);
        var2.addVertex(var1.minX, var1.maxY, var1.maxZ);
        var2.tessellate();
    }

    private void drawCursor(LivingEntity var1, float var2) {
        if (this.cursor == null || this.cursor.type != HitType.field_789) {
            return;
        }
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        float var3 = 0.002F;
        int var4 = this.world.getBlockId(this.cursor.x, this.cursor.y, this.cursor.z);
        if (var4 > 0) {
            Block.BY_ID[var4].updateBoundingBox(this.world, this.cursor.x, this.cursor.y, this.cursor.z);
            double var5 = var1.prevRenderX + (var1.x - var1.prevRenderX) * (double) var2;
            double var7 = var1.prevRenderY + (var1.y - var1.prevRenderY) * (double) var2;
            double var9 = var1.prevRenderZ + (var1.z - var1.prevRenderZ) * (double) var2;
            AxixAlignedBoundingBox var11 = Block.BY_ID[var4].getOutlineShape(this.world, this.cursor.x, this.cursor.y, this.cursor.z).expand((double) var3, (double) var3, (double) var3).duplicateAndTranslate(-var5, -var7, -var9);
            this.drawBox(var11);
            GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(4.0F);
            Tessellator var12 = Tessellator.INSTANCE;
            var12.start(3);
            if (this.cursor.field_1987 == 0) {
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
                var12.addVertex(var11.maxX, var11.minY, var11.minZ);
                var12.addVertex(var11.maxX, var11.minY, var11.maxZ);
                var12.addVertex(var11.minX, var11.minY, var11.maxZ);
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
            } else if (this.cursor.field_1987 == 1) {
                var12.addVertex(var11.minX, var11.maxY, var11.minZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.minZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.minX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.minX, var11.maxY, var11.minZ);
            } else if (this.cursor.field_1987 == 2) {
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
                var12.addVertex(var11.maxX, var11.minY, var11.minZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.minZ);
                var12.addVertex(var11.minX, var11.maxY, var11.minZ);
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
            } else if (this.cursor.field_1987 == 3) {
                var12.addVertex(var11.minX, var11.minY, var11.maxZ);
                var12.addVertex(var11.maxX, var11.minY, var11.maxZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.minX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.minX, var11.minY, var11.maxZ);
            } else if (this.cursor.field_1987 == 4) {
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
                var12.addVertex(var11.minX, var11.maxY, var11.minZ);
                var12.addVertex(var11.minX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.minX, var11.minY, var11.maxZ);
                var12.addVertex(var11.minX, var11.minY, var11.minZ);
            } else if (this.cursor.field_1987 == 5) {
                var12.addVertex(var11.maxX, var11.minY, var11.minZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.minZ);
                var12.addVertex(var11.maxX, var11.maxY, var11.maxZ);
                var12.addVertex(var11.maxX, var11.minY, var11.maxZ);
                var12.addVertex(var11.maxX, var11.minY, var11.minZ);
            }

            var12.tessellate();
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void setBlock(int var1, int var2) {
        this.selectedBlockID = var1;
        this.selectedMetadata = var2;
    }
}
