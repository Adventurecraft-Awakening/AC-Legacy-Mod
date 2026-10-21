package dev.adventurecraft.awakening.mixin.client.render.entity;

import dev.adventurecraft.awakening.client.render.compat.LegacyModelBridge;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public final class MixinModelRenderRoots {
    @Mixin(LivingEntityRenderer.class)
    public abstract static class Living {
        @Redirect(method="render(Lnet/minecraft/world/entity/Mob;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/Model;render(FFFFFF)V"))
        private void ac$renderModel(Model model,float time,float strength,float bob,float yaw,float pitch,float scale) {
            LegacyModelBridge.render(model,time,strength,bob,yaw,pitch,scale);
        }
    }
    @Mixin(BoatRenderer.class)
    public abstract static class Boat {
        @Redirect(method="render(Lnet/minecraft/world/entity/item/Boat;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/Model;render(FFFFFF)V"))
        private void ac$renderModel(Model model,float time,float strength,float bob,float yaw,float pitch,float scale) {
            LegacyModelBridge.render(model,time,strength,bob,yaw,pitch,scale);
        }
    }
    @Mixin(MinecartRenderer.class)
    public abstract static class Minecart {
        @Redirect(method="render(Lnet/minecraft/world/entity/item/Minecart;DDDFF)V",at=@At(value="INVOKE",target="Lnet/minecraft/client/model/Model;render(FFFFFF)V"))
        private void ac$renderModel(Model model,float time,float strength,float bob,float yaw,float pitch,float scale) {
            LegacyModelBridge.render(model,time,strength,bob,yaw,pitch,scale);
        }
    }
}
