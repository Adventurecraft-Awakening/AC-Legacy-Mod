package dev.adventurecraft.awakening;

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
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        entrypointField.setAccessible(true);
        try {
            var instance = FabricLoaderImpl.INSTANCE.getGameProvider();
            if (entrypointField.get(instance).equals("net.minecraft.client.MinecraftApplet")) {
                entrypointField.set(instance, "net.minecraft.client.Minecraft");
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}