package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

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
        if (!AC_DebugMode.active) {
            return;
        }
        if (!entity.showDebugInfo) {
            return;
        }
        this.renderBounds(entity, x, y, z);
    }

    private void renderBounds(AC_TileEntityMobSpawner entity, double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
        GL11.glLineWidth(6.0F);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);

        for (int i = 0; i < 8; i++) {
            Coord min = entity.minVec[i];
            Coord max = entity.maxVec[i];
            for (int bX = min.x; bX <= max.x; bX++) {
                for (int bY = min.y; bY <= max.y; bY++) {
                    for (int bZ = min.z; bZ <= max.z; bZ++) {
                        Tile block = Tile.tiles[entity.level.getTile(bX, bY, bZ)];
                        if (block != null && ((ExBlock) block).canBeTriggered()) {
                            GL11.glColor3f(0.0F, 0.0F, 0.0F);
                            GL11.glVertex3f(0.0F, 0.0F, 0.0F);
                            GL11.glColor3d(lineColors[i].x, lineColors[i].y, lineColors[i].z);
                            GL11.glVertex3f((float) (bX - entity.x), (float) (bY - entity.y), (float) (bZ - entity.z));
                        }
                    }
                }
            }
        }

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glLineWidth(1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public @Override void render(TileEntity entity, double x, double y, double z, float tickTime) {
        this.render((AC_TileEntityMobSpawner) entity, x, y, z, tickTime);
    }
}
