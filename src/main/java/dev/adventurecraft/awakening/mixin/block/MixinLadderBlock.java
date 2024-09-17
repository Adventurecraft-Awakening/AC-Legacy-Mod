package dev.adventurecraft.awakening.mixin.block;

import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.LadderTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderTile.class)
public abstract class MixinLadderBlock {

    @Redirect(method = {"getAABB", "getTileAABB"}, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getData(III)I"))
    private int redirectBlockMeta(Level world, int x, int y, int z) {
        return world.getData(x, y, z) % 4 + 2;
    }

    @Inject(method = "mayPlace", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;isSolidBlockingTile(III)Z",
            shift = At.Shift.BEFORE,
            ordinal = 3),
            cancellable = true)
    private void canPlaceAtId(Level world, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        int id = world.getTile(x, y - 1, z);
        if (ExLadderBlock.isLadderID(id)) {
            cir.setReturnValue(true);
        }
    }

    @Overwrite
    public void setPlacedOnFace(Level var1, int var2, int var3, int var4, int var5) {
        int var6 = var1.getData(var2, var3, var4);
        if (var6 == 0 && ExLadderBlock.isLadderID(var1.getTile(var2, var3 - 1, var4))) {
            var6 = var1.getData(var2, var3 - 1, var4) % 4 + 2;
        }

        if (var6 == 0 && ExLadderBlock.isLadderID(var1.getTile(var2, var3 + 1, var4))) {
            var6 = var1.getData(var2, var3 + 1, var4) % 4 + 2;
        }

        if ((var6 == 0 || var5 == 2) && var1.isSolidTile(var2, var3, var4 + 1)) {
            var6 = 2;
        }

        if ((var6 == 0 || var5 == 3) && var1.isSolidTile(var2, var3, var4 - 1)) {
            var6 = 3;
        }

        if ((var6 == 0 || var5 == 4) && var1.isSolidTile(var2 + 1, var3, var4)) {
            var6 = 4;
        }

        if ((var6 == 0 || var5 == 5) && var1.isSolidTile(var2 - 1, var3, var4)) {
            var6 = 5;
        }

        var1.setData(var2, var3, var4, var6 - 2);
    }

    @Overwrite
    public void neighborChanged(Level arg, int i, int j, int k, int l) {
    }
}
