package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.glu.GLU;

public class AC_MapEditing {
    public Minecraft mc;
    public Level world;
    public HitResult cursor;
    private TileRenderer renderBlocks;
    public int selectedBlockID;
    public int selectedMetadata;

    public AC_MapEditing(Minecraft mc, Level world) {
        this.mc = mc;
        this.world = world;
        this.renderBlocks = new TileRenderer(world);
    }

    public void updateWorld(Level world) {
        this.world = world;
        this.renderBlocks.level = world;
    }

    public void updateCursor(LivingEntity entity, float var2, float deltaTime) {
        if (this.mc.mouseGrabbed) {
            this.cursor = null;
            return;
        }

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        try (var stack = MemoryStack.stackPush()) {
            var viewport = stack.mallocInt(16);
            var modelMatrix = stack.mallocFloat(16);
            var projMatrix = stack.mallocFloat(16);
            var objPos = stack.mallocFloat(3);

            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projMatrix);
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
            GLU.gluUnProject(mouseX, mouseY, 1.0F, modelMatrix, projMatrix, viewport, objPos);

            Vec3 start = entity.getPos(deltaTime);
            Vec3 end = start.add(objPos.get(0) * 1024.0F, objPos.get(1) * 1024.0F, objPos.get(2) * 1024.0F);
            this.cursor = this.world.clip(start, end);
        }
    }

    public void paint() {
        if (this.cursor == null || this.cursor.hitType != HitType.TILE) {
            return;
        }
        int x = this.cursor.x + this.getCursorXOffset();
        int y = this.cursor.y + this.getCursorYOffset();
        int z = this.cursor.z + this.getCursorZOffset();
        this.setBlock(x, y, z, this.selectedBlockID, this.selectedMetadata);
    }

    public int getCursorXOffset() {
        return this.cursor.face == 4 ? -1 : (this.cursor.face == 5 ? 1 : 0);
    }

    public int getCursorYOffset() {
        return this.cursor.face == 0 ? -1 : (this.cursor.face == 1 ? 1 : 0);
    }

    public int getCursorZOffset() {
        return this.cursor.face == 2 ? -1 : (this.cursor.face == 3 ? 1 : 0);
    }

    private void setBlock(int x, int y, int z, int id, int meta) {
        if (y >= 0 && y < 128) {
            this.world.setTileAndData(x, y, z, id, meta);
        }
    }

    public void render(float deltaTime) {
        LivingEntity entity = Minecraft.instance.cameraEntity;
        if (this.mc.mouseGrabbed) {
            return;
        }
        this.drawCursor(entity, deltaTime);
        if (this.cursor == null) {
            return;
        }
        float prX = (float) (entity.xOld + (entity.x - entity.xOld) * (double) deltaTime);
        float prY = (float) (entity.yOld + (entity.y - entity.yOld) * (double) deltaTime);
        float prZ = (float) (entity.zOld + (entity.z - entity.zOld) * (double) deltaTime);
        GL11.glPushMatrix();
        GL11.glTranslatef(-prX, -prY, -prZ);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        this.mc.textures.bind(this.mc.textures.loadTexture("/terrain.png"));
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
        LivingEntity entity = Minecraft.instance.cameraEntity;
        float prX = (float) (entity.xOld + (entity.x - entity.xOld) * (double) deltaTime);
        float prY = (float) (entity.yOld + (entity.y - entity.yOld) * (double) deltaTime);
        float prZ = (float) (entity.zOld + (entity.z - entity.zOld) * (double) deltaTime);
        GL11.glPushMatrix();
        GL11.glTranslatef(-prX, -prY, -prZ);
        GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.4F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
        Vec3 rot = entity.getLookAngle();
        int rX = (int) (entity.x + (double) AC_DebugMode.reachDistance * rot.x) - AC_ItemCursor.minX;
        int rY = (int) (entity.y + (double) AC_DebugMode.reachDistance * rot.y) - AC_ItemCursor.minY;
        int rZ = (int) (entity.z + (double) AC_DebugMode.reachDistance * rot.z) - AC_ItemCursor.minZ;

        for (int texIndex = 0; texIndex <= 3; ++texIndex) {
            if (texIndex == 0) {
                this.mc.textures.bind(this.mc.textures.loadTexture("/terrain.png"));
            } else {
                this.mc.textures.bind(this.mc.textures.loadTexture(String.format("/terrain%d.png", texIndex)));
            }

            ((ExBlockRenderer) this.renderBlocks).startRenderingBlocks(this.world);

            for (int x = AC_ItemCursor.minX; x <= AC_ItemCursor.maxX; ++x) {
                for (int y = AC_ItemCursor.minY; y <= AC_ItemCursor.maxY; ++y) {
                    for (int z = AC_ItemCursor.minZ; z <= AC_ItemCursor.maxZ; ++z) {
                        int id = this.mc.level.getTile(x, y, z);
                        Tile block = Tile.tiles[id];
                        if (block != null && ((ExBlock) block).getTextureNum() == texIndex) {
                            int meta = this.mc.level.getData(x, y, z);
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
        Tile block = Tile.tiles[id];
        if (block == null) {
            return;
        }
        int prevId = this.world.getTile(x, y, z);
        int prevMeta = this.world.getData(x, y, z);
        ((ExWorld) this.world).setBlockAndMetadataTemp(x, y, z, id, meta);
        this.renderBlocks.tesselateInWorld(block, x, y, z);
        ((ExWorld) this.world).setBlockAndMetadataTemp(x, y, z, prevId, prevMeta);
    }

    private void drawBox(AABB aabb) {
        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINE_STRIP);
        ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        ts.vertex(aabb.x1, aabb.y0, aabb.z0);
        ts.vertex(aabb.x1, aabb.y0, aabb.z1);
        ts.vertex(aabb.x0, aabb.y0, aabb.z1);
        ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        ts.end();
        ts.begin(GL11.GL_LINE_STRIP);
        ts.vertex(aabb.x0, aabb.y1, aabb.z0);
        ts.vertex(aabb.x1, aabb.y1, aabb.z0);
        ts.vertex(aabb.x1, aabb.y1, aabb.z1);
        ts.vertex(aabb.x0, aabb.y1, aabb.z1);
        ts.vertex(aabb.x0, aabb.y1, aabb.z0);
        ts.end();
        ts.begin(GL11.GL_LINES);
        ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        ts.vertex(aabb.x0, aabb.y1, aabb.z0);
        ts.vertex(aabb.x1, aabb.y0, aabb.z0);
        ts.vertex(aabb.x1, aabb.y1, aabb.z0);
        ts.vertex(aabb.x1, aabb.y0, aabb.z1);
        ts.vertex(aabb.x1, aabb.y1, aabb.z1);
        ts.vertex(aabb.x0, aabb.y0, aabb.z1);
        ts.vertex(aabb.x0, aabb.y1, aabb.z1);
        ts.end();
    }

    private void drawCursor(LivingEntity entity, float deltaTime) {
        if (this.cursor == null || this.cursor.hitType != HitType.TILE) {
            return;
        }

        int id = this.world.getTile(this.cursor.x, this.cursor.y, this.cursor.z);
        if (id <= 0) {
            return;
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        Tile block = Tile.tiles[id];
        block.updateShape(this.world, this.cursor.x, this.cursor.y, this.cursor.z);
        double prX = entity.xOld + (entity.x - entity.xOld) * (double) deltaTime;
        double prY = entity.yOld + (entity.y - entity.yOld) * (double) deltaTime;
        double prZ = entity.zOld + (entity.z - entity.zOld) * (double) deltaTime;
        float boxSize = 0.002F;
        AABB aabb = block
            .getTileAABB(this.world, this.cursor.x, this.cursor.y, this.cursor.z)
            .inflate(boxSize, boxSize, boxSize)
            .offset(-prX, -prY, -prZ);
        this.drawBox(aabb);
        GL11.glColor4f(1.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(4.0F);
        Tesselator ts = Tesselator.instance;
        ts.begin(GL11.GL_LINE_STRIP);
        if (this.cursor.face == 0) {
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
            ts.vertex(aabb.x1, aabb.y0, aabb.z0);
            ts.vertex(aabb.x1, aabb.y0, aabb.z1);
            ts.vertex(aabb.x0, aabb.y0, aabb.z1);
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        } else if (this.cursor.face == 1) {
            ts.vertex(aabb.x0, aabb.y1, aabb.z0);
            ts.vertex(aabb.x1, aabb.y1, aabb.z0);
            ts.vertex(aabb.x1, aabb.y1, aabb.z1);
            ts.vertex(aabb.x0, aabb.y1, aabb.z1);
            ts.vertex(aabb.x0, aabb.y1, aabb.z0);
        } else if (this.cursor.face == 2) {
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
            ts.vertex(aabb.x1, aabb.y0, aabb.z0);
            ts.vertex(aabb.x1, aabb.y1, aabb.z0);
            ts.vertex(aabb.x0, aabb.y1, aabb.z0);
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        } else if (this.cursor.face == 3) {
            ts.vertex(aabb.x0, aabb.y0, aabb.z1);
            ts.vertex(aabb.x1, aabb.y0, aabb.z1);
            ts.vertex(aabb.x1, aabb.y1, aabb.z1);
            ts.vertex(aabb.x0, aabb.y1, aabb.z1);
            ts.vertex(aabb.x0, aabb.y0, aabb.z1);
        } else if (this.cursor.face == 4) {
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
            ts.vertex(aabb.x0, aabb.y1, aabb.z0);
            ts.vertex(aabb.x0, aabb.y1, aabb.z1);
            ts.vertex(aabb.x0, aabb.y0, aabb.z1);
            ts.vertex(aabb.x0, aabb.y0, aabb.z0);
        } else if (this.cursor.face == 5) {
            ts.vertex(aabb.x1, aabb.y0, aabb.z0);
            ts.vertex(aabb.x1, aabb.y1, aabb.z0);
            ts.vertex(aabb.x1, aabb.y1, aabb.z1);
            ts.vertex(aabb.x1, aabb.y0, aabb.z1);
            ts.vertex(aabb.x1, aabb.y0, aabb.z0);
        }

        ts.end();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void setBlock(int id, int meta) {
        this.selectedBlockID = id;
        this.selectedMetadata = meta;
    }
}
