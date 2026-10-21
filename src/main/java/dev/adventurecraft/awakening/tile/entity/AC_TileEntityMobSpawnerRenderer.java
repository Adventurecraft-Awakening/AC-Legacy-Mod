package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.DebugDraw;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.image.Rgba;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.world.BlockPos;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.Vec3;

public class AC_TileEntityMobSpawnerRenderer extends TileEntityRenderer {
    Vec3[] lineColors = {
        Vec3.create(0.00,0.25,0.70),
        Vec3.create(0.10,0.30,0.77),
        Vec3.create(0.20,0.35,0.84),
        Vec3.create(0.30,0.40,0.90),
        Vec3.create(0.85,0.20,0.60),
        Vec3.create(0.86,0.30,0.63),
        Vec3.create(0.87,0.40,0.66),
        Vec3.create(0.88,0.50,0.70)
    };

    public void render(AC_TileEntityMobSpawner entity, double x, double y, double z, float tickTime) {
        if (!EditorSession.forLevel(entity.level).active) {
            return;
        }
        if (!entity.showDebugInfo) {
            return;
        }
        this.renderBounds(entity, x, y, z);
    }

    private void renderBounds(AC_TileEntityMobSpawner entity, double x, double y, double z) {
        DebugDraw.with(LegacyPassBridge::capture, debug -> {
        for (int i = 0; i < 8; i++) {
            BlockPos min = entity.minVec[i];
            BlockPos max = entity.maxVec[i];
            for (int bX = min.x(); bX <= max.x(); bX++) {
                for (int bY = min.y(); bY <= max.y(); bY++) {
                    for (int bZ = min.z(); bZ <= max.z(); bZ++) {
                        Tile block = Tile.tiles[entity.level.getTile(bX, bY, bZ)];
                        if (block != null && ((ExBlock) block).canBeTriggered()) {
                            Vec3 c = this.lineColors[i];
                            int color = Rgba.fromRgba8(Rgba.saturateToByte(c.x), Rgba.saturateToByte(c.y), Rgba.saturateToByte(c.z), 255);
                            debug.line(x + 0.5, y + 0.5, z + 0.5,
                                x + 0.5 + bX - entity.x, y + 0.5 + bY - entity.y, z + 0.5 + bZ - entity.z,
                                Rgba.BLACK, color, 6, true);
                        }
                    }
                }
            }
        }

        });
    }

    public @Override void render(TileEntity entity, double x, double y, double z, float tickTime) {
        this.render((AC_TileEntityMobSpawner) entity, x, y, z, tickTime);
    }
}
