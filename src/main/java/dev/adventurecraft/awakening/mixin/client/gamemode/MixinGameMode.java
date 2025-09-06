package dev.adventurecraft.awakening.mixin.client.gamemode;

import dev.adventurecraft.awakening.extension.client.gamemode.ExGameMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gamemode.GameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.LevelEvent;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.*;

@Mixin(GameMode.class)
public abstract class MixinGameMode implements ExGameMode {

    @Shadow @Final protected Minecraft minecraft;

    @Unique private int destroyExtraWidth;
    @Unique private int destroyExtraDepth;

    @Overwrite
    public boolean destroyBlock(int x, int y, int z, int face) {
        Level level = this.minecraft.level;
        Tile tile = Tile.tiles[level.getTile(x, y, z)];
        if (tile == null) {
            return false;
        }
        int data = level.getData(x, y, z);
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, x, y, z, tile.id + data * 256);

        boolean broken = level.setTile(x, y, z, 0);
        if (broken) {
            tile.destroy(level, x, y, z, data);
        }
        return broken;
    }

    @Override
    public Minecraft getMinecraft() {
        return this.minecraft;
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
