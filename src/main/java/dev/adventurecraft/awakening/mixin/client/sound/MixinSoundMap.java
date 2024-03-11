package dev.adventurecraft.awakening.mixin.client.sound;

import dev.adventurecraft.awakening.extension.client.sound.ExSoundMap;
import net.minecraft.client.sound.SoundEntry;
import net.minecraft.client.sound.SoundMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(SoundMap.class)
public abstract class MixinSoundMap implements ExSoundMap {

    @Shadow
    private Map<String, List<SoundEntry>> idToSounds;
    @Shadow
    private List<SoundEntry> soundList;
    @Shadow
    public int count;
    @Shadow
    public boolean isRandomSound;

    @Overwrite
    public SoundEntry addSound(String name, File file) {
        try {
            URL url = file.toURI().toURL();
            return this.addSound(name, url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SoundEntry addSound(String name, URL url) {
        String fileName = name.substring(0, name.indexOf(".")); // strip extension

        String id = fileName;
        if (this.isRandomSound) {
            while (Character.isDigit(id.charAt(id.length() - 1))) {
                id = id.substring(0, id.length() - 1);
            }
        }

        id = id.replaceAll("/", ".");
        if (!this.idToSounds.containsKey(id)) {
            this.idToSounds.put(id, new ArrayList<>());
        }

        List<SoundEntry> entries = this.idToSounds.get(id);
        for (SoundEntry entry : entries) {
            String entryName = entry.soundName.substring(0, entry.soundName.indexOf(".")); // strip extension
            if (fileName.equals(entryName)) {
                entries.remove(entry);
                this.soundList.remove(entry);
                break;
            }
        }

        var newEntry = new SoundEntry(name, url);
        entries.add(newEntry);
        this.soundList.add(newEntry);
        ++this.count;
        return newEntry;
    }
}
