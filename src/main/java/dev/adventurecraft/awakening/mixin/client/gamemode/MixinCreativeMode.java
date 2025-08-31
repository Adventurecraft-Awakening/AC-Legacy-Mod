package dev.adventurecraft.awakening.mixin.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.CreativeMode;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CreativeMode.class)
public abstract class MixinCreativeMode extends GameMode {

    public MixinCreativeMode(Minecraft minecraft) {
        super(minecraft);
    }

    @Overwrite
    public void adjustPlayer(Player player) {
    }
}
