package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.client.render.*;
import dev.adventurecraft.awakening.client.render.compat.LegacyModelBridge;
import dev.adventurecraft.awakening.extension.client.model.ExCuboid;
import dev.adventurecraft.awakening.extension.client.render.ExHeldItemRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.*;

@Mixin(HumanoidMobRenderer.class)
public abstract class MixinHumanoidMobRenderer extends LivingEntityRenderer {
    @Shadow protected HumanoidModel humanoidModel;
    protected MixinHumanoidMobRenderer(Model model,float shadow) { super(model,shadow); }
    @Overwrite public void additionalRendering(Mob mob,float tick) {
        var item=mob.getCarriedItem();if(item==null) return;
        var entry=LegacyModelBridge.entry();
        var arm=((ExCuboid)this.humanoidModel.rightArm).pose(entry.pose(),1F/16,false);
        var pose=CarriedItemPose.pose(arm,item,false);
        ((ExHeldItemRenderer)this.entityRenderDispatcher.itemInHandRenderer).renderItem(mob,item,
            entry.context().parameters(pose,entry.tint()));
    }
}
