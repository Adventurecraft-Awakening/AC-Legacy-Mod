package dev.adventurecraft.awakening.mixin.entity.ai.pathing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExPathNodeNavigator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

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
        if (Tile.tiles[id].mayPick()) {
            AABB box = Tile.tiles[id].getAABB(Minecraft.instance.level, x, y, z);
            return box != null;
        }
        return false;
    }

    @Override
    public Path simplifyPath(Path var1, Node var2) {
        if (var1 == null) {
            return null;
        }

        var var3 = new ArrayList<Node>();
        Node var4 = null;
        Node var5 = null;
        Node var6 = null;
        int var7 = 0;
        boolean var8 = false;
        Node[] var9 = var1.nodes;

        for (Node pathNode : var9) {
            if (var7++ >= var1.pos) {
                if (var8) {
                    var3.add(pathNode);
                } else if (var4 == null) {
                    var4 = pathNode;
                    var3.add(pathNode);
                } else if (var5 == null) {
                    if (var4.y != pathNode.y) {
                        var4 = pathNode;
                        var3.add(pathNode);
                        var8 = true;
                    } else {
                        var5 = pathNode;
                        var6 = pathNode;
                    }
                } else if (var6.y != pathNode.y) {
                    var3.add(var6);
                    var3.add(pathNode);
                    var4 = pathNode;
                    var5 = null;
                    var6 = null;
                    var8 = true;
                } else {
                    int var13 = pathNode.x - var4.x;
                    int var14 = pathNode.z - var4.z;
                    float var15;
                    float var16;
                    byte var17;
                    int var18;
                    if (Math.abs(var13) < Math.abs(var14)) {
                        var15 = 0.0F;
                        var16 = (float) var13 / (float) Math.abs(var14);
                        var17 = 1;
                        if (var14 < 0) {
                            var17 = -1;
                        }

                        for (var18 = 1; var18 < Math.abs(var14); ++var18) {
                            if (this.isFree(null, var4.x + (int) var15, var4.y, var4.z + var18 * var17, var2) == 1 &&
                                this.isFree(null, var4.x + (int) var15, var4.y - 1, var4.z + var18 * var17, var2) != 1 &&
                                this.isFree(null, var4.x + (int) var15 + 1, var4.y, var4.z + var18 * var17, var2) == 1 &&
                                this.isFree(null, var4.x + (int) var15 + 1, var4.y - 1, var4.z + var18 * var17, var2) != 1 &&
                                this.isFree(null, var4.x + (int) var15 - 1, var4.y, var4.z + var18 * var17, var2) == 1 &&
                                this.isFree(null, var4.x + (int) var15 - 1, var4.y - 1, var4.z + var18 * var17, var2) != 1) {
                                var15 += var16;
                            } else {
                                var3.add(var5);
                                var3.add(pathNode);
                                var8 = true;
                            }
                        }
                    } else {
                        var15 = 0.0F;
                        var16 = (float) var14 / (float) Math.abs(var13);
                        var17 = 1;
                        if (var13 < 0) {
                            var17 = -1;
                        }

                        for (var18 = 1; var18 < Math.abs(var13); ++var18) {
                            if (this.isFree(null, var4.x + var18 * var17, var4.y, var4.z + (int) var15, var2) == 1 &&
                                this.isFree(null, var4.x + var18 * var17, var4.y - 1, var4.z + (int) var15, var2) != 1 &&
                                this.isFree(null, var4.x + var18 * var17, var4.y, var4.z + (int) var15 + 1, var2) == 1 &&
                                this.isFree(null, var4.x + var18 * var17, var4.y - 1, var4.z + (int) var15 + 1, var2) != 1 &&
                                this.isFree(null, var4.x + var18 * var17, var4.y, var4.z + (int) var15 - 1, var2) == 1 &&
                                this.isFree(null, var4.x + var18 * var17, var4.y - 1, var4.z + (int) var15 - 1, var2) != 1) {
                                var15 += var16;
                            } else {
                                var4 = var5;
                                var3.add(var5);
                                var3.add(pathNode);
                                var8 = true;
                            }
                        }
                    }

                    var6 = pathNode;
                }
            }
        }

        if (!var8) {
            if (var6 != null) {
                var3.add(var6);
            } else if (var5 != null) {
                var3.add(var5);
            }
        }

        var1.nodes = var3.toArray(new Node[0]);
        var1.length = var3.size();
        var1.pos = 0;
        ((ExEntityPath) var1).setNavigator((PathFinder) (Object) this);
        ((ExEntityPath) var1).setClearSize(var2);
        return var1;
    }
}
