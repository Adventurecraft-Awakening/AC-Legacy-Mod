package dev.adventurecraft.awakening.mixin.client.gui.screen;

import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Screen.class)
public abstract class MixinScreen implements ExScreen {

    private boolean disableInputGrabbing = false;

    public boolean isDisabledInputGrabbing() {
        return this.disableInputGrabbing;
    }

    public void setDisabledInputGrabbing(boolean value) {
        this.disableInputGrabbing = value;
    }
}
