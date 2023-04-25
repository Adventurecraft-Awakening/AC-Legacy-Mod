package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import net.minecraft.client.ClientInteractionManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientInteractionManager.class)
public abstract class MixinClientInteractionManager implements ExInteractionManager {

    private int destroyExtraWidth;

    private int destroyExtraDepth;

    public int getDestroyExtraWidth() {
        return this.destroyExtraWidth;
    }

    public void setDestroyExtraWidth(int value) {
        this.destroyExtraWidth = value;
    }

    public int getDestroyExtraDepth() {
        return this.destroyExtraDepth;
    }

    public void setDestroyExtraDepth(int value) {
        this.destroyExtraDepth = value;
    }
}
