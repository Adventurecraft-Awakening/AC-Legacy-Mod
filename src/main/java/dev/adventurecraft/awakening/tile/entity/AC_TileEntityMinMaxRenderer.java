package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.common.AC_DebugMode;
import dev.adventurecraft.awakening.common.Coord;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import org.lwjgl.opengl.GL11;

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
        if (AC_DebugMode.isActive()) {
            renderArea(entity.min(), entity.max(), entity, x, y, z, this.r, this.g, this.b);
        }
    }

    public static void renderArea(
        Coord min,
        Coord max,
        TileEntity entity,
        double x,
        double y,
        double z,
        float r,
        float g,
        float b
    ) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glTranslatef((float) (x + 0.5), (float) (y + 0.5), (float) (z + 0.5));
        GL11.glLineWidth(6.0F);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_LINES);

        for (int bX = min.x; bX <= max.x; bX++) {
            for (int bY = min.y; bY <= max.y; bY++) {
                for (int bZ = min.z; bZ <= max.z; bZ++) {
                    Tile tile = Tile.tiles[entity.level.getTile(bX, bY, bZ)];
                    if (tile != null && ((ExBlock) tile).canBeTriggered()) {
                        GL11.glColor3f(0.0F, 0.0F, 0.0F);
                        GL11.glVertex3f(0.0F, 0.0F, 0.0F);
                        GL11.glColor3f(r, g, b);
                        GL11.glVertex3f(bX - entity.x, bY - entity.y, bZ - entity.z);
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
        this.renderArea((AC_TileEntityMinMax) entity, x, y, z, tickTime);
    }
}
