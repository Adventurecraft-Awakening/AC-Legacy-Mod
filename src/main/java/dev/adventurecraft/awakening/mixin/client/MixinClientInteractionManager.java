package dev.adventurecraft.awakening.mixin.client;

import dev.adventurecraft.awakening.extension.client.ExInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameMode.class)
public abstract class MixinClientInteractionManager implements ExInteractionManager {

    @Shadow
    @Final
    protected Minecraft minecraft;

    private int destroyExtraWidth;

    private int destroyExtraDepth;

    @Shadow
    public boolean destroyBlock(int i, int j, int k, int l) {
        throw new AssertionError();
    }

    @Override
    public int getDestroyExtraWidth() {
        return this.destroyExtraWidth;
    }

    @Override
    public void setDestroyExtraWidth(int value) {
        this.destroyExtraWidth = value;
    }

    @Override
    public int getDestroyExtraDepth() {
        return this.destroyExtraDepth;
    }

    @Override
    public void setDestroyExtraDepth(int value) {
        this.destroyExtraDepth = value;
    }
}
