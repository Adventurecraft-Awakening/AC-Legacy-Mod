package dev.adventurecraft.awakening.extension.client.util;

import dev.adventurecraft.awakening.ACMod;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public interface ExResourceDownloadThread {

    static void loadSoundsFromResources(Minecraft client, Class<?> rootClass, String resourcePath, String namePath) {
        var descriptor = rootClass.getResourceAsStream(resourcePath);
        if (descriptor == null) {
            return;
        }
        var reader = new BufferedReader(new InputStreamReader(descriptor));
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                String subResourceName = resourcePath + line;
                var subResource = rootClass.getResource(subResourceName);
                if (subResource == null) {
                    continue;
                }

                String subName = namePath + line;
                try {
                    var subFile = new File(subResource.toURI());
                    if (subFile.isFile() && subResourceName.endsWith(".ogg")) {
                        client.loadSoundFromDir(subName, subFile);
                    } else if (subFile.isDirectory()) {
                        loadSoundsFromResources(client, rootClass, subResourceName + "/", subName + "/");
                    }
                } catch (URISyntaxException e) {
                    ACMod.LOGGER.warn("Failed to load resource \"{}\" from jar \"{}\".", subName, rootClass.getName(), e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
