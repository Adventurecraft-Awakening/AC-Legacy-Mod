package dev.adventurecraft.awakening.mixin.world;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.CakeTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CakeTile.class)
public abstract class MixinCakeTile {

    @Overwrite
    private void eat(Level level, int x, int y, int z, Player player) {
        if (player.health < ((ExLivingEntity)player).getMaxHealth()) {
            player.heal(3);
            int var6 = level.getData(x, y, z) + 1;
            if (var6 >= 6) {
                level.setTile(x, y, z, 0);
            } else {
                level.setData(x, y, z, var6);
                level.setTileDirty(x, y, z);
            }
        }

    }
}
