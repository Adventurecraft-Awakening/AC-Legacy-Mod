package dev.adventurecraft.awakening.extension.client.render;

import net.minecraft.client.options.GameOptions;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Overwrite;

public interface ExTextRenderer {

    void init(GameOptions var1, String var2, TextureManager var3);

    void drawStringWithShadow(String var1, float var2, float var3, int var4);

    void drawString(String var1, float var2, float var3, int var4);
}
