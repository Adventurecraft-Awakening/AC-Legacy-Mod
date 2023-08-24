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
    public SoundEntry addSound(String id, File file) {
        try {
            URL url = file.toURI().toURL();
            return this.addSound(id, url);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SoundEntry addSound(String id, URL url) {
        String var3 = id;
        id = id.substring(0, id.indexOf("."));

        String var4 = id;
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
            if (var4.equals(entry.soundName.substring(0, entry.soundName.indexOf(".")))) {
                entries.remove(entry);
                break;
            }
        }

        var newEntry = new SoundEntry(var3, url);
        entries.add(newEntry);
        this.soundList.add(newEntry);
        ++this.count;
        return newEntry;
    }
}
