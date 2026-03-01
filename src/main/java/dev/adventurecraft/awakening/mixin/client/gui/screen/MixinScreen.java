package dev.adventurecraft.awakening.mixin.client.gui.screen;

import dev.adventurecraft.awakening.extension.client.gui.screen.ExScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Screen.class)
public abstract class MixinScreen implements ExScreen {

    @Shadow protected Minecraft minecraft;
    @Shadow public Font font;

    @Unique private boolean disableInputGrabbing = false;

    public @Override Minecraft getMinecraft() {
        if (this.minecraft == null) {
            return Minecraft.instance;
        }
        return this.minecraft;
    }

    public @Override Font getFont() {
        if (this.font == null) {
            return this.getMinecraft().font;
        }
        return this.font;
    }

    public @Override boolean isDisabledInputGrabbing() {
        return this.disableInputGrabbing;
    }

    public @Override void setDisabledInputGrabbing(boolean value) {
        this.disableInputGrabbing = value;
    }
}
