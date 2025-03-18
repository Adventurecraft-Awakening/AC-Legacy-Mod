package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public class AC_RenderNPC extends AC_RenderBipedScaledScripted {

    public AC_RenderNPC(HumanoidModel var1) {
        super(var1);
    }

    protected void renderNameTags(LivingEntity var1, double var2, double var4, double var6) {
        AC_EntityNPC var8 = (AC_EntityNPC) var1;
        if (Minecraft.renderDebug()) {
            this.renderNameTag(var1, String.format("%s - %d", var8.npcName, var8.id), var2, var4, var6, 64);
        } else if (var8.seesThePlayer()) {
            this.renderNameTag(var1, var8.npcName, var2, var4, var6, 64);
        }
    }
}
