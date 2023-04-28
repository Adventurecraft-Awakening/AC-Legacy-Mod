package dev.adventurecraft.awakening.mixin.entity.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.block.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DispenserBlockEntity.class)
public abstract class MixinDispenserBlockEntity extends BlockEntity {

    @Shadow
    private ItemStack[] contents;

    @Overwrite
    public ItemStack takeInventoryItem(int var1, int var2) {
        if (this.contents[var1] == null) {
            return null;
        }

        ItemStack var3;
        if (this.contents[var1].count <= var2 && this.contents[var1].count >= 0) {
            var3 = this.contents[var1];
            this.contents[var1] = null;
            this.markDirty();
        } else if (this.contents[var1].count < 0) {
            var3 = this.contents[var1].copy();
            var3.count = 1;
        } else {
            var3 = this.contents[var1].split(var2);
            if (this.contents[var1].count == 0) {
                this.contents[var1] = null;
            }
            this.markDirty();
        }
        return var3;
    }
}
