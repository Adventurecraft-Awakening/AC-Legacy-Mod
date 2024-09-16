package dev.adventurecraft.awakening.mixin.client.sound;

import dev.adventurecraft.awakening.extension.client.sound.ExSoundMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.sounds.Sound;
import net.minecraft.client.sounds.SoundRepository;

@Mixin(SoundRepository.class)
public abstract class MixinSoundMap implements ExSoundMap {

    @Shadow
    private Map<String, List<Sound>> urls;
    @Shadow
    private List<Sound> all;
    @Shadow
    public int count;
    @Shadow
    public boolean trimDigits;

    @Overwrite
    public Sound add(String name, File file) {
        try {
            URL url = file.toURI().toURL();
            return this.addSound(name, url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Sound addSound(String name, URL url) {
        String fileName = name.substring(0, name.indexOf(".")); // strip extension

        String id = fileName;
        if (this.trimDigits) {
            while (Character.isDigit(id.charAt(id.length() - 1))) {
                id = id.substring(0, id.length() - 1);
            }
        }

        id = id.replaceAll("/", ".");
        if (!this.urls.containsKey(id)) {
            this.urls.put(id, new ArrayList<>());
        }

        List<Sound> entries = this.urls.get(id);
        for (Sound entry : entries) {
            String entryName = entry.name.substring(0, entry.name.indexOf(".")); // strip extension
            if (fileName.equals(entryName)) {
                entries.remove(entry);
                this.all.remove(entry);
                break;
            }
        }

        var newEntry = new Sound(name, url);
        entries.add(newEntry);
        this.all.add(newEntry);
        ++this.count;
        return newEntry;
    }
}
