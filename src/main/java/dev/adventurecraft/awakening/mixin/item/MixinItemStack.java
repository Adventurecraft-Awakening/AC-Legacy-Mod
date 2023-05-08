package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.item.ExItem;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements ExItemStack {

    private int timeLeft;
    private boolean reloading;
    private boolean justReloaded;

    @Shadow
    public int itemId;

    @Shadow
    private int meta;

    @Shadow
    public int count;

    @Shadow
    public abstract Item getItem();

    @Override
    public boolean useItemLeftClick(PlayerEntity var1, World var2, int var3, int var4, int var5, int var6) {
        return ((ExItem) this.getItem()).onItemUseLeftClick((ItemStack) (Object) this, var1, var2, var3, var4, var5, var6);
    }

    @Overwrite
    public CompoundTag writeNBT(CompoundTag var1) {
        var1.put("id", (short) this.itemId);
        var1.put("Count", this.count);
        var1.put("Damage", (short) this.meta);
        return var1;
    }

    @Overwrite
    public void readNBT(CompoundTag var1) {
        this.itemId = var1.getShort("id");
        this.count = var1.getInt("Count");
        this.meta = var1.getShort("Damage");

        if (this.itemId == AC_Items.boomerang.id) {
            this.meta = 0;
        }
    }

    @Override
    public boolean getReloading() {
        return this.reloading;
    }

    @Override
    public void setReloading(boolean value) {
        this.reloading = value;
    }

    @Override
    public boolean getJustReloaded() {
        return this.justReloaded;
    }

    @Override
    public void setJustReloaded(boolean value) {
        this.justReloaded = value;
    }

    @Override
    public int getTimeLeft() {
        return this.timeLeft;
    }

    @Override
    public void setTimeLeft(int value) {
        this.timeLeft = value;
    }
}
