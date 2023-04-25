package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.entity.block.BlockEntityRenderer;
import net.minecraft.entity.BlockEntity;
import org.lwjgl.opengl.GL11;

public class AC_TileEntityEffectRenderer extends BlockEntityRenderer {
	public void render(AC_TileEntityEffect var1, double var2, double var4, double var6, float var8) {
		if(AC_DebugMode.active) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glTranslatef((float)var2 + 0.5F, (float)var4 + 0.5F, (float)var6 + 0.5F);
			GL11.glLineWidth(6.0F);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glTranslatef(var1.offsetX, var1.offsetY, var1.offsetZ);
			GL11.glColor3f(1.0F, 0.0F, 0.0F);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3f(var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, -var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(var1.randX, var1.randY, -var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(var1.randX, var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, -var1.randY, -var1.randZ);
			GL11.glVertex3f(-var1.randX, var1.randY, -var1.randZ);
			GL11.glEnd();
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glLineWidth(1.0F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}

	}

	public void render(BlockEntity var1, double var2, double var4, double var6, float var8) {
		this.render((AC_TileEntityEffect)var1, var2, var4, var6, var8);
	}
}
