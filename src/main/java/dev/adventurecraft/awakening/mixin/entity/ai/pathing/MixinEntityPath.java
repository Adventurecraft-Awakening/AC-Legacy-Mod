package dev.adventurecraft.awakening.mixin.entity.ai.pathing;

import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExPathNodeNavigator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityPath;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPath.class)
public abstract class MixinEntityPath implements ExEntityPath {

    private PathNodeNavigator navigator;

    private PathNode clearSize;

    @Shadow
    public int field_2690;

    @Shadow
    public PathNode[] field_2691;

    @Inject(method = "method_2040", at = @At("TAIL"))
    private void simplify(CallbackInfo ci) {
        if (this.navigator != null) {
            ((ExPathNodeNavigator) this.navigator).simplifyPath((EntityPath) (Object) this, this.clearSize);
        }
    }

    @Override
    public boolean needNewPath(Entity var1) {
        if (this.field_2690 > 0) {
            double var2 = var1.x - (double) this.field_2691[this.field_2690 - 1].x - 0.5D;
            double var4 = var1.y - (double) var1.standingEyeHeight - (double) this.field_2691[this.field_2690 - 1].y;
            double var6 = var1.z - (double) this.field_2691[this.field_2690 - 1].z - 0.5D;
            return var2 * var2 + var4 * var4 + var6 * var6 > 6.0D;
        } else {
            return false;
        }
    }

    @Override
    public PathNodeNavigator getNavigator() {
        return this.navigator;
    }

    @Override
    public void setNavigator(PathNodeNavigator value) {
        this.navigator = value;
    }

    @Override
    public PathNode getClearSize() {
        return this.clearSize;
    }

    @Override
    public void setClearSize(PathNode value) {
        this.clearSize = value;
    }
}
