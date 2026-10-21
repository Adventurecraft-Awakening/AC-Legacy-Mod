package dev.adventurecraft.awakening.mixin.entity.ai.pathing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.entity.ai.pathing.PathSimplifier;
import dev.adventurecraft.awakening.entity.ai.pathing.PathingCollisionContext;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExPathNodeNavigator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathFinder.class)
public abstract class MixinPathNodeNavigator implements ExPathNodeNavigator {

    @Shadow
    private LevelSource level;

    @Shadow
    protected abstract int isFree(Entity arg, int i, int j, int k, Node arg2);

    @ModifyConstant(
        method = "findPath(Lnet/minecraft/world/entity/Entity;DDDF)Lnet/minecraft/world/level/pathfinder/Path;",
        constant = {
            @Constant(floatValue = 1.0F, ordinal = 0),
            @Constant(floatValue = 1.0F, ordinal = 2)})
    private float widenPath(float value) {
        return 1.25F;
    }

    @ModifyReturnValue(
        method = "findPath(Lnet/minecraft/world/entity/Entity;DDDF)Lnet/minecraft/world/level/pathfinder/Path;",
        at = @At("RETURN"))
    private Path simplifyOnFind(Path path, @Local(ordinal = 2) Node node) {
        return this.simplifyPath(path, node);
    }

    @Inject(
        method = "isFree",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/LevelSource;getTile(III)I",
            shift = At.Shift.BEFORE),
        cancellable = true)
    private void returnOnFence(
        Entity var1, int var2, int var3, int var4, Node var5, CallbackInfoReturnable<Integer> cir,
        @Local(ordinal = 3) int x,
        @Local(ordinal = 4) int y,
        @Local(ordinal = 5) int z) {
        if (y > 1) {
            int id = this.level.getTile(x, y - 1, z);
            if (id == Tile.OAK_FENCE.id) {
                cir.setReturnValue(0);
            }
        }
    }

    @Redirect(method = "isFree", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/Material;blocksMotion()Z"))
    private boolean refinedMoveBlockCheck(
        Material instance,
        @Local(ordinal = 3) int x,
        @Local(ordinal = 4) int y,
        @Local(ordinal = 5) int z,
        @Local(ordinal = 6) int id) {
        return PathingCollisionContext.blocksMotion(this.level, instance, Tile.tiles[id], x, y, z);
    }

    @Override
    public Path simplifyPath(Path path, Node clearSize) {
        if (path == null) {
            return null;
        }

        path.nodes = PathSimplifier.simplify(path.nodes, path.pos, clearSize,
            (x, y, z, size) -> this.isFree(null, x, y, z, size));
        path.length = path.nodes.length;
        path.pos = 0;
        var extension = (ExEntityPath) path;
        extension.setNavigator((PathFinder) (Object) this);
        extension.setClearSize(clearSize);
        return path;
    }
}
