package dev.adventurecraft.awakening;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;

import java.lang.reflect.Field;

public class ACPreLaunchFix implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        Field entrypointField;
        try {
            entrypointField = MinecraftGameProvider.class.getDeclaredField("entrypoint");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        entrypointField.setAccessible(true);
        try {
            entrypointField.set(FabricLoaderImpl.INSTANCE.getGameProvider(), "net.minecraft.client.Minecraft");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}