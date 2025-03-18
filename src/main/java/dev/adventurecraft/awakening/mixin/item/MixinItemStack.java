package dev.adventurecraft.awakening.mixin.item;

import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.extension.item.ExItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemInstance.class)
public abstract class MixinItemStack implements ExItemStack {

    private int timeLeft;
    private boolean reloading;
    private boolean justReloaded;

    @Shadow
    public int id;

    @Shadow
    private int auxValue;

    @Shadow
    public int count;

    @Shadow
    public abstract Item getItem();

    @Overwrite
    public CompoundTag save(CompoundTag tag) {
        tag.putShort("id", (short) this.id);
        tag.putInt("Count", this.count);
        tag.putShort("Damage", (short) this.auxValue);
        return tag;
    }

    @Overwrite
    public void load(CompoundTag tag) {
        this.id = tag.getShort("id");
        this.count = tag.getInt("Count");
        this.auxValue = tag.getShort("Damage");

        if (this.id == AC_Items.boomerang.id) {
            this.auxValue = 0;
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
