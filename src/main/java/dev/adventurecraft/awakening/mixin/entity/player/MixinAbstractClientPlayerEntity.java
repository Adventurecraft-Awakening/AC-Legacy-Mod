package dev.adventurecraft.awakening.mixin.entity.player;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LocalPlayer.class)
public abstract class MixinAbstractClientPlayerEntity {

    @ModifyConstant(method = "<init>", constant = @Constant(stringValue = "http://s3.amazonaws.com/MinecraftSkins/"))
    private static String fix_skinUrl(String s) {
        return "http://skins.minecraft.net/MinecraftSkins/";
    }
}
