package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;

@SuppressWarnings("unused")
public class ScriptRenderer {

    LevelRenderer worldEventRenderer;

    ScriptRenderer(LevelRenderer worldEventRenderer) {
        this.worldEventRenderer = worldEventRenderer;
    }

    public void reload(){
        ((ExWorldEventRenderer) Minecraft.instance.levelRenderer).updateAllTheRenderers();
    }

}
