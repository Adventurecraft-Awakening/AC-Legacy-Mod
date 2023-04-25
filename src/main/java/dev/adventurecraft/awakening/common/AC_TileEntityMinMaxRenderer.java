package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.block.ExBlock;
import net.minecraft.block.Block;
import net.minecraft.client.render.entity.block.BlockEntityRenderer;
import net.minecraft.entity.BlockEntity;
import org.lwjgl.opengl.GL11;

public class AC_TileEntityMinMaxRenderer extends BlockEntityRenderer {
    float r;
    float g;
    float b;

    public AC_TileEntityMinMaxRenderer(float var1, float var2, float var3) {
        this.r = var1;
        this.g = var2;
        this.b = var3;
    }

    public void render(AC_TileEntityMinMax var1, double var2, double var4, double var6, float var8) {
        if (AC_DebugMode.active) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glTranslatef((float) var2 + 0.5F, (float) var4 + 0.5F, (float) var6 + 0.5F);
            GL11.glLineWidth(6.0F);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glBegin(GL11.GL_LINES);

            for (int var9 = var1.minX; var9 <= var1.maxX; ++var9) {
                for (int var10 = var1.minY; var10 <= var1.maxY; ++var10) {
                    for (int var11 = var1.minZ; var11 <= var1.maxZ; ++var11) {
                        Block var12 = Block.BY_ID[var1.world.getBlockId(var9, var10, var11)];
                        if (var12 != null && ((ExBlock) var12).canBeTriggered()) {
                            GL11.glColor3f(0.0F, 0.0F, 0.0F);
                            GL11.glVertex3f(0.0F, 0.0F, 0.0F);
                            GL11.glColor3f(this.r, this.g, this.b);
                            GL11.glVertex3f((float) (var9 - var1.x), (float) (var10 - var1.y), (float) (var11 - var1.z));
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

    }

    public void render(BlockEntity var1, double var2, double var4, double var6, float var8) {
        this.render((AC_TileEntityMinMax) var1, var2, var4, var6, var8);
    }
}
