package dev.adventurecraft.awakening.common;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

public class AC_RenderBipedScaledScripted extends HumanoidMobRenderer {

	public AC_RenderBipedScaledScripted(HumanoidModel var1) {
		super(var1, 0.5F);
	}

	protected void scale(LivingEntity var1, float var2) {
		AC_EntityLivingScript var3 = (AC_EntityLivingScript)var1;
		float var4 = (1.0F - var2) * var3.prevWidth + var2 * var3.bbWidth;
		float var5 = (1.0F - var2) * var3.prevHeight + var2 * var3.bbHeight;
		var4 /= 0.6F;
		this.shadowRadius = var4 * 0.5F;
		GL11.glScalef(var4, var5 / 1.8F, var4);
	}
}
