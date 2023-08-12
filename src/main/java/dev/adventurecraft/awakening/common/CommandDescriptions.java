package dev.adventurecraft.awakening.common;

import java.util.HashMap;

public class CommandDescriptions {

    private final HashMap<Object, String> map = new HashMap<>();

    public <T> T attach(T value, String description) {
        this.map.put(value, description);
        return value;
    }

    public String getDescription(Object key) {
        return this.map.get(key);
    }
}
