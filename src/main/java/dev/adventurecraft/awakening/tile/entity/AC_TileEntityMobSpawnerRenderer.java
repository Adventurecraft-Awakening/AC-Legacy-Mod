package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.lwjgl.opengl.GL11;

public class AC_TileEntityMobSpawnerRenderer extends TileEntityRenderer {

    public void render(AC_TileEntityMobSpawner entity, double x, double y, double z, float var8) {
        if (!AC_DebugMode.active) {
            return;
        }
        if(!entity.showDebugInfo){
            return;
        }

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

            for (int bX = min.x; bX <= max.x; ++bX) {
                for (int bY = min.y; bY <= max.y; ++bY) {
                    for (int bZ = min.z; bZ <= max.z; ++bZ) {
                        Tile block = Tile.tiles[entity.level.getTile(bX, bY, bZ)];
                        if (block != null && ((ExBlock) block).canBeTriggered()) {
                            GL11.glColor3f(0.0F, 0.0F, 0.0F);
                            GL11.glVertex3f(0.0F, 0.0F, 0.0F);
                            GL11.glColor3f(0.105F * i, (float) 1 /i, 0.486F);
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

    @Override
    public void render(TileEntity var1, double var2, double var4, double var6, float var8) {
        this.render((AC_TileEntityMobSpawner) var1, var2, var4, var6, var8);
    }
}
