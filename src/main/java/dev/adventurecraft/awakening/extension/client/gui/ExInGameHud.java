package dev.adventurecraft.awakening.extension.client.gui;

import dev.adventurecraft.awakening.chat.Component;
import dev.adventurecraft.awakening.script.ScriptUIContainer;

public interface ExInGameHud {

    void addMessage(Component component);

    ScriptUIContainer getScriptUI();

    boolean getHudEnabled();

    void setHudEnabled(boolean value);
}
