package dev.adventurecraft.awakening.world;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.image.*;
import dev.adventurecraft.awakening.item.AC_ItemCustom;
import dev.adventurecraft.awakening.script.EntityDescriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ptexture.*;
import net.minecraft.locale.I18n;
import net.minecraft.world.level.Level;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

/** Owns map resource paths and catalogs; mixins only forward the legacy API. */
public final class MapResources implements AutoCloseable {
    private final Minecraft client;
    private final Level level;
    private final File directory;
    private final ArrayList<String> musicList = new ArrayList<>();
    private final ArrayList<String> soundList = new ArrayList<>();

    public MapResources(Minecraft client, Level level, File directory) {
        this.client = client;
        this.level = level;
        this.directory = directory;
        ((ExTranslationStorage) I18n.getInstance()).loadMapTranslation(directory);
    }

    public void loadTerrain(String saveName) {
        if (!AC_TerrainImage.loadMap(this.directory)) {
            AC_TerrainImage.loadMap(new File(new File(Minecraft.getWorkingDirectory(), "saves"), saveName));
        }
    }

    public void loadDefinitions() {
        this.loadSoundOverrides();
        EntityDescriptions.loadDescriptions(new File(this.directory, "entitys"));
        AC_ItemCustom.loadItems(new File(this.directory, "items"));
    }

    public ArrayList<String> music() { return this.musicList; }
    public ArrayList<String> sounds() { return this.soundList; }

    @Override public void close() {
        this.musicList.clear();
        this.soundList.clear();
    }

    public void loadMapTextures() {
        var texManager = ((ExTextureManager) this.client.textures);
        this.client.textures.reloadAll();

        for (Object oEntry : this.client.textures.idMap.entrySet()) {
            var entry = (java.util.Map.Entry<String, Integer>) oEntry;
            String name = entry.getKey();
            int id = entry.getValue();
            try {
                texManager.loadTexture(id, name);
            }
            catch (IllegalArgumentException ex) {
                ACMod.LOGGER.error("Failed to load texture \"{}\".", name, ex);
            }
        }

        this.loadTextureAnimations();
        Level self = this.level;
        AC_TextureBinder.loadImages(texManager, AC_TextureFanFX.class, self);
        AC_TextureBinder.loadImages(texManager, FireTexture.class, self);
        AC_TextureBinder.loadImages(texManager, LavaTexture.class, self);
        AC_TextureBinder.loadImages(texManager, LavaSideTexture.class, self);
        AC_TextureBinder.loadImages(texManager, PortalTexture.class, self);
        AC_TextureBinder.loadImages(texManager, WaterTexture.class, self);
        AC_TextureBinder.loadImages(texManager, WaterSideTexture.class, self);
        ExGrassColor.loadGrass("/misc/grasscolor.png", self);
        ExFoliageColor.loadFoliage("/misc/foliagecolor.png", self);
        ((ExWorldProperties) this.level.levelData).loadTextureReplacements(self);
    }

    private void loadTextureAnimations() {
        var texManager = ((ExTextureManager) this.client.textures);
        texManager.clearTextureAnimations();

        var file = new File(this.directory, "animations.txt");
        if (!file.exists()) {
            return;
        }

        Level self = this.level;
        try (var reader = new BufferedReader(new FileReader(file, StandardCharsets.ISO_8859_1))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] elements = line.split(",", 7);
                if (elements.length == 7) {
                    try {
                        String animName = elements[0].trim();
                        String texName = elements[1].trim();
                        String imageName = elements[2].trim();
                        int x = Integer.parseInt(elements[3].trim());
                        int y = Integer.parseInt(elements[4].trim());
                        int w = Integer.parseInt(elements[5].trim());
                        int h = Integer.parseInt(elements[6].trim());
                        var instance = new AC_TextureAnimated(texName, x, y, w, h);
                        //noinspection DataFlowIssue
                        ((AC_TextureBinder) instance).loadImage(imageName, self);
                        texManager.registerTextureAnimation(animName, instance);
                    }
                    catch (Exception error) {
                        ACMod.LOGGER.warn("Invalid map texture animation: {}", line, error);
                    }
                }
            }
        }
        catch (IOException error) {
            ACMod.LOGGER.warn("Failed to load map texture animations from {}", file, error);
        }
    }

    public ImageBuffer loadMapTexture(String name) {
        var file = new File(this.directory, name);
        if (file.exists()) {
            try {
                return ImageLoader.load(file, ImageLoadOptions.withFormat(ImageFormat.RGBA_U8));
            }
            catch (Exception ex) {
                ACMod.LOGGER.error("Failed to load map texture \"{}\":", file.getPath(), ex);
            }
        }
        return null;
    }

    public void loadMapMusic() {
        this.loadAudio("music", this.musicList, true);

        // TODO: play after level is initialized
        String playingMusic = ((ExWorldProperties) this.level.levelData).getPlayingMusic();
        if (!playingMusic.isEmpty()) {
            var level = this.level;
            ((ExSoundHelper) this.client.soundEngine).playMusicFromStreaming(level, playingMusic, 0, 0);
        }
    }

    public void loadMapSounds() { this.loadAudio("sound", this.soundList, false); }

    private void loadAudio(String folder, ArrayList<String> catalog, boolean streaming) {
        catalog.clear();
        File[] files = new File(this.directory, folder).listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".ogg")) continue;
            String filename = file.getName().toLowerCase(Locale.ROOT);
            String name = folder + "/" + filename;
            if (streaming) this.client.soundEngine.addStreaming(name, file);
            else this.client.soundEngine.add(name, file);
            catalog.add(folder + "." + filename.replace(".ogg", ""));
        }
    }

    public void loadSoundOverrides() {
        // TODO: unload sounds?

        this.client.bgLoader.forceReload();
        File var1 = new File(this.directory, "soundOverrides");
        if (var1.exists()) {
            this.client.bgLoader.loadAll(var1, "");
        }
    }

}
