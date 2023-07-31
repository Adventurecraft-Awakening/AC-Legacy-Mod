package dev.adventurecraft.awakening.extension.util.io;

import java.util.Set;

public interface ExCompoundTag {

    Set<String> getKeys();

    Object getValue(String key);
}
