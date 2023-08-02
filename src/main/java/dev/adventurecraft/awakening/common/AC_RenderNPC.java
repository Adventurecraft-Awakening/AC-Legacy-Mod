package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class AC_RenderNPC extends AC_RenderBipedScaledScripted {

    public AC_RenderNPC(BipedEntityModel var1) {
        super(var1);
    }

    protected void method_821(LivingEntity var1, double var2, double var4, double var6) {
        AC_EntityNPC var8 = (AC_EntityNPC) var1;
        if (Minecraft.isDebugHudEnabled()) {
            this.method_818(var1, String.format("%s - %d", var8.npcName, var8.entityId), var2, var4, var6, 64);
        } else if (var8.seesThePlayer()) {
            this.method_818(var1, var8.npcName, var2, var4, var6, 64);
        }
    }
}
