package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.DebugDraw;
import dev.adventurecraft.awakening.client.render.NativeRenderer;
import dev.adventurecraft.awakening.client.render.QuadBuilder;
import dev.adventurecraft.awakening.client.render.RenderScene;
import dev.adventurecraft.awakening.client.render.state.*;
import dev.adventurecraft.awakening.client.render.block.BlockMesher;
import dev.adventurecraft.awakening.client.render.block.BlockMeshing;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.world.AC_BlockCopyUtils;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.input.Mouse;

public class AC_MapEditing {
    public Minecraft mc;
    public Level world;
    public HitResult cursor;
    private final QuadBuilder previewVertices = QuadBuilder.frameOwned(256);
    public int selectedBlockID;
    public int selectedMetadata;

    public AC_MapEditing(Minecraft mc, Level world) {
        this.mc = mc;
        this.world = world;
    }

    public void updateWorld(Level world) {
        this.world = world;
    }

    public void updateCursor(Mob entity, float var2, float deltaTime) {
        if (this.mc.mouseGrabbed) {
            this.cursor = null;
            return;
        }

        int mouseX = Mouse.getX();
        int mouseY = Mouse.getY();
        var camera = dev.adventurecraft.awakening.client.render.RenderScene.hasWorld()
            ? dev.adventurecraft.awakening.client.render.RenderScene.camera() : LegacyPassBridge.cameraView();
        var ray = camera.screenRay(mouseX * (double) dev.adventurecraft.awakening.client.render.RenderSystem.width() / this.mc.width,
            (this.mc.height - mouseY) * (double) dev.adventurecraft.awakening.client.render.RenderSystem.height() / this.mc.height);
        Vec3 origin = entity.getPos(deltaTime);
        Vec3 start = origin.add(ray.originX(), ray.originY(), ray.originZ());
        Vec3 end = start.add(ray.directionX() * 1024, ray.directionY() * 1024, ray.directionZ() * 1024);
        this.cursor = this.world.clip(start, end);
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
        if (this.mc.mouseGrabbed) return;
        this.drawCursor(this.mc.cameraEntity, deltaTime);
        if (this.cursor == null || this.cursor.hitType != HitType.TILE) return;
        Tile tile = Tile.tiles[this.selectedBlockID];
        if (tile == null) return;
        BlockMesher mesher = this.beginPreview(BlockMeshing.options(this.world));
        mesher.preview(tile, this.cursor.x + this.getCursorXOffset(), this.cursor.y + this.getCursorYOffset(),
            this.cursor.z + this.getCursorZOffset(), this.selectedMetadata);
        this.finishPreview(((ExBlock) tile).getTextureNum());
    }

    public void renderSelection(float deltaTime) {
        if (!EditorSession.forLevel(this.world).selection.bothSet) return;
        BlockPos pos = AC_BlockCopyUtils.calculatePastePosition().sub(EditorSession.forLevel(this.world).selection.min);
        BlockPos min = EditorSession.forLevel(this.world).selection.min, max = EditorSession.forLevel(this.world).selection.max;
        var options = BlockMeshing.options(this.world);
        for (int texture = 0; texture <= 3; texture++) {
            BlockMesher mesher = this.beginPreview(options);
            for (int x = min.x(); x <= max.x(); x++) {
                for (int y = min.y(); y <= max.y(); y++) {
                    for (int z = min.z(); z <= max.z(); z++) {
                        Tile tile = Tile.tiles[this.mc.level.getTile(x, y, z)];
                        if (tile != null && ((ExBlock) tile).getTextureNum() == texture) {
                            mesher.preview(tile, x + pos.x(), y + pos.y(), z + pos.z(), this.mc.level.getData(x, y, z));
                        }
                    }
                }
            }
            this.finishPreview(texture);
        }
    }

    private BlockMesher beginPreview(BlockMesher.Options options) {
        this.previewVertices.clear();
        return new BlockMesher(this.world, this.previewVertices, RenderScene.cameraX(), RenderScene.cameraY(),
            RenderScene.cameraZ(), options);
    }

    private void finishPreview(int textureIndex) {
        var parameters = RenderScene.worldParameters();
        var blend = new BlendState(true, BlendFactor.CONSTANT_ALPHA, BlendFactor.ONE_MINUS_CONSTANT_ALPHA,
            new Color4(1, 1, 1, .4F));
        parameters = parameters.withRaster(parameters.raster().withBlend(blend).withCull(CullMode.NONE));
        String path = textureIndex == 0 ? "/terrain.png" : "/terrain" + textureIndex + ".png";
        var texture = ((ExTextureManager) this.mc.textures).texture(path);
        NativeRenderer.pass(parameters).drawQuads(this.previewVertices, texture);
    }

    private void drawCursor(Mob entity, float deltaTime) {
        if (this.cursor == null || this.cursor.hitType != HitType.TILE) {
            return;
        }

        int id = this.world.getTile(this.cursor.x, this.cursor.y, this.cursor.z);
        if (id <= 0) {
            return;
        }

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
        DebugDraw.with(RenderScene::worldParameters, debug -> {
            debug.box(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1, 0x66000000, 2, false);
            var points = new java.util.ArrayList<double[]>();
        if (this.cursor.face == 0) {
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
        }
        else if (this.cursor.face == 1) {
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z0});
        }
        else if (this.cursor.face == 2) {
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
        }
        else if (this.cursor.face == 3) {
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z1});
        }
        else if (this.cursor.face == 4) {
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x0, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x0, aabb.y0, aabb.z0});
        }
        else if (this.cursor.face == 5) {
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z0});
            points.add(new double[] {aabb.x1, aabb.y1, aabb.z1});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z1});
            points.add(new double[] {aabb.x1, aabb.y0, aabb.z0});
        }

            for (int i = 1; i < points.size(); i++) {
                double[] a = points.get(i - 1), b = points.get(i);
                debug.line(a[0], a[1], a[2], b[0], b[1], b[2], 0x660000ff, 0x660000ff, 4, false);
            }
        });
    }

    public void setBlock(int id, int meta) {
        this.selectedBlockID = id;
        this.selectedMetadata = meta;
    }
}
