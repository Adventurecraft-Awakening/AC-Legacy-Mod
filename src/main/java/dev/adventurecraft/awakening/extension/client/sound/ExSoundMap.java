package dev.adventurecraft.awakening.extension.client.sound;

import java.net.URL;
import net.minecraft.client.sounds.Sound;

public interface ExSoundMap {

    Sound addSound(String name, URL url);
}
