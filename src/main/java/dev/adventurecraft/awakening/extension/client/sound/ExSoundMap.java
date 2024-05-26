package dev.adventurecraft.awakening.extension.client.sound;

import net.minecraft.client.sound.SoundEntry;

import java.net.URL;

public interface ExSoundMap {

    SoundEntry addSound(String name, URL url);
}
