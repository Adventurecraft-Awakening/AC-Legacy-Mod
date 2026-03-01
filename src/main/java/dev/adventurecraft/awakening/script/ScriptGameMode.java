package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.entity.player.GameMode;

public class ScriptGameMode {
    private final GameMode gameMode;

    public ScriptGameMode(GameMode mode) {
        this.gameMode = mode;
    }

    public String getName() {
        return this.gameMode.getName();
    }

    public @Override String toString() {
        return "GameMode{name=%s}".formatted(this.getName());
    }
}
