package dev.adventurecraft.awakening.extension.client.gui;

import dev.adventurecraft.awakening.script.ScriptUIContainer;

public interface ExInGameHud {

    ScriptUIContainer getScriptUI();

    boolean getHudEnabled();

    void setHudEnabled(boolean value);
}
