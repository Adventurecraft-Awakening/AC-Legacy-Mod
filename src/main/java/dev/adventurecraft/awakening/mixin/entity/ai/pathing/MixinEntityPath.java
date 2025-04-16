package dev.adventurecraft.awakening.mixin.entity.ai.pathing;

import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExPathNodeNavigator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Path.class)
public abstract class MixinEntityPath implements ExEntityPath {

    private PathFinder navigator;

    private Node clearSize;

    @Shadow
    public int length;

    @Shadow
    public Node[] nodes;

    @Inject(method = "next", at = @At("TAIL"))
    private void simplify(CallbackInfo ci) {
        if (this.navigator != null) {
            ((ExPathNodeNavigator) this.navigator).simplifyPath((Path) (Object) this, this.clearSize);
        }
    }

    @Override
    public boolean needNewPath(Entity var1) {
        if (this.length > 0) {
            double var2 = var1.x - (double) this.nodes[this.length - 1].x - 0.5D;
            double var4 = var1.y - (double) var1.heightOffset - (double) this.nodes[this.length - 1].y;
            double var6 = var1.z - (double) this.nodes[this.length - 1].z - 0.5D;
            return var2 * var2 + var4 * var4 + var6 * var6 > 6.0D;
        } else {
            return false;
        }
    }

    @Override
    public PathFinder getNavigator() {
        return this.navigator;
    }

    @Override
    public void setNavigator(PathFinder value) {
        this.navigator = value;
    }

    @Override
    public Node getClearSize() {
        return this.clearSize;
    }

    @Override
    public void setClearSize(Node value) {
        this.clearSize = value;
    }
}
