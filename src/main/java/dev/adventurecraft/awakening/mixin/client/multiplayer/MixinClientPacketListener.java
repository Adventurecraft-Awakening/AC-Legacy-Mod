package dev.adventurecraft.awakening.mixin.client.multiplayer;

import dev.adventurecraft.awakening.client.gamemode.MultiplayerAdventureGameMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiplayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Redirect(
        method = "handleLogin",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/ClientPacketListener;)Lnet/minecraft/client/multiplayer/MultiplayerGameMode;"
        )
    )
    private MultiplayerGameMode useCustomGameMode(Minecraft connection, ClientPacketListener clientPacketListener) {
        return new MultiplayerAdventureGameMode(connection, clientPacketListener);
    }
}
