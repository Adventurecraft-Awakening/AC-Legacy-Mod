package dev.adventurecraft.awakening.mixin.entity.player;

import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(RemoteClientPlayerEntity.class)
public abstract class MixinRemoteClientPlayerEntity {

    @ModifyConstant(method = "<init>", constant = @Constant(stringValue = "http://s3.amazonaws.com/MinecraftSkins/"))
    private static String fix_skinUrl(String s) {
        return "http://skins.minecraft.net/MinecraftSkins/";
    }
}
