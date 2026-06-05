package dev.adventurecraft.awakening.extension.client.gui;

import dev.adventurecraft.awakening.dom.Node;
import dev.adventurecraft.awakening.script.ScriptUIContainer;

public interface ExInGameHud {

    void addMessage(Node node);

    ScriptUIContainer getScriptUI();

    boolean getHudEnabled();

    void setHudEnabled(boolean value);
}
