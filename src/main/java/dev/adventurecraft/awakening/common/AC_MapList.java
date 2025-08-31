package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AC_MapList {

    private List<AC_MapInfo> maps = new ArrayList<>();
    private File mapDir;

    public AC_MapList() {
        this.mapDir = ACMod.getMapsDir().toFile();
        if (!this.mapDir.exists()) {
            this.mapDir.mkdirs();
        }

        this.findMaps();
    }

    public void findMaps() {
        this.maps.clear();

        File[] files = this.mapDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            String name = file.getName();
            String line1 = "";
            String line2 = "";

            var descFile = new File(file, "description.txt");
            if (descFile.exists()) {
                try (var reader = new BufferedReader(new FileReader(descFile))) {
                    line1 = reader.readLine();
                    line2 = reader.readLine();
                }
                catch (IOException ex) {
                    ACMod.LOGGER.warn("Failed to read map description \"{}\".", descFile.getPath(), ex);
                }
            }

            this.maps.add(new AC_MapInfo(name, line1, line2, new File(file, "thumbnail.png")));
        }
    }

    public List<AC_MapInfo> getMaps() {
        return this.maps;
    }
}
