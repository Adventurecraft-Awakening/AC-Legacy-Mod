package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.client.render.ExWorldEventRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldEventRenderer;

@SuppressWarnings("unused")
public class ScriptRenderer {

    WorldEventRenderer worldEventRenderer;

    ScriptRenderer(WorldEventRenderer worldEventRenderer) {
        this.worldEventRenderer = worldEventRenderer;
    }

    public void reload(){
        ((ExWorldEventRenderer) Minecraft.instance.worldRenderer).updateAllTheRenderers();
    }

}
