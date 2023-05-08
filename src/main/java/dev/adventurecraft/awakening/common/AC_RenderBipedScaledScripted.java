package dev.adventurecraft.awakening.common;

import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

public class AC_RenderBipedScaledScripted extends BipedEntityRenderer {
	public AC_RenderBipedScaledScripted(BipedEntityModel var1) {
		super(var1, 0.5F);
	}

	protected void method_823(LivingEntity var1, float var2) {
		AC_EntityLivingScript var3 = (AC_EntityLivingScript)var1;
		float var4 = (1.0F - var2) * var3.prevWidth + var2 * var3.width;
		float var5 = (1.0F - var2) * var3.prevHeight + var2 * var3.height;
		var4 /= 0.6F;
		this.field_2678 = var4 * 0.5F;
		GL11.glScalef(var4, var5 / 1.8F, var4);
	}
}
