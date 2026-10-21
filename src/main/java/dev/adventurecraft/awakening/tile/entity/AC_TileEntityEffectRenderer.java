package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.editor.EditorSession;
import dev.adventurecraft.awakening.client.render.DebugDraw;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityEffectRenderer extends TileEntityRenderer {
    public void render(AC_TileEntityEffect entity, double x, double y, double z, float partialTick) {
        if (!EditorSession.forLevel(entity.level).active) return;
        DebugDraw.with(LegacyPassBridge::capture, debug -> {
            double cx = x + 0.5 + entity.offsetX, cy = y + 0.5 + entity.offsetY, cz = z + 0.5 + entity.offsetZ;
            debug.box(cx - entity.randX, cy - entity.randY, cz - entity.randZ,
                cx + entity.randX, cy + entity.randY, cz + entity.randZ, 0xff0000ff, 6, true);
        });
    }

    @Override public void render(TileEntity entity, double x, double y, double z, float partialTick) {
        this.render((AC_TileEntityEffect) entity, x, y, z, partialTick);
    }
}
