package dev.adventurecraft.awakening.mixin.client.gui.components;

import dev.adventurecraft.awakening.extension.client.gui.components.ExButton;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Button.class)
public abstract class MixinButton implements ExButton {

    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int width;
    @Shadow public int height;

    public @Override void setRect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
}
