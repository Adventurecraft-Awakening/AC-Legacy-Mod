package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import net.minecraft.client.ClientInteractionManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientInteractionManager.class)
public abstract class MixinClientInteractionManager implements ExInteractionManager {

    @Shadow
    @Final
    protected Minecraft client;

    private int destroyExtraWidth;

    private int destroyExtraDepth;

    @Shadow
    public boolean breakBlock(int i, int j, int k, int l) {
        throw new AssertionError();
    }

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
