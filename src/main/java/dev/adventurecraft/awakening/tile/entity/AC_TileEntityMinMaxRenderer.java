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

public class AC_TileEntityMinMaxRenderer extends TileEntityRenderer {

    float r;
    float g;
    float b;

    public AC_TileEntityMinMaxRenderer(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public void renderArea(AC_TileEntityMinMax entity, double x, double y, double z, float tickTime) {
        if (EditorSession.forLevel(entity.level).active) {
            renderArea(entity.min(), entity.max(), entity, x, y, z, this.r, this.g, this.b);
        }
    }

    public static void renderArea(
        BlockPos min,
        BlockPos max,
        TileEntity entity,
        double x,
        double y,
        double z,
        float r,
        float g,
        float b
    ) {
        int color = Rgba.fromRgba8(Rgba.saturateToByte(r), Rgba.saturateToByte(g), Rgba.saturateToByte(b), 255);
        DebugDraw.with(LegacyPassBridge::capture, debug -> {
        for (int bX = min.x(); bX <= max.x(); bX++) {
            for (int bY = min.y(); bY <= max.y(); bY++) {
                for (int bZ = min.z(); bZ <= max.z(); bZ++) {
                    Tile tile = Tile.tiles[entity.level.getTile(bX, bY, bZ)];
                    if (tile != null && ((ExBlock) tile).canBeTriggered()) {
                        debug.line(x + 0.5, y + 0.5, z + 0.5,
                            x + 0.5 + bX - entity.x, y + 0.5 + bY - entity.y, z + 0.5 + bZ - entity.z,
                            Rgba.BLACK, color, 6, true);
                    }
                }
            }
        }

        });
    }

    public @Override void render(TileEntity entity, double x, double y, double z, float tickTime) {
        this.renderArea((AC_TileEntityMinMax) entity, x, y, z, tickTime);
    }
}
