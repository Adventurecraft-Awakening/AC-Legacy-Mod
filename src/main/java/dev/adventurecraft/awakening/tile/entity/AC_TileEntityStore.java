package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;

public class AC_TileEntityStore extends TileEntity {
    public int buyItemID = AC_Items.bomb.id;
    public int buyItemAmount = 3;
    public int buyItemDamage;
    public int buySupply;
    public int buySupplyLeft = 1;
    public int sellItemID = AC_Items.shotgun.id;
    public int sellItemAmount = 1;
    public int sellItemDamage = 0;
    public AC_TriggerArea tradeTrigger;

    public void load(CompoundTag tag) {
        super.load(tag);
        this.buyItemID = tag.getInt("buyItemID");
        this.buyItemAmount = tag.getInt("buyItemAmount");
        this.buyItemDamage = tag.getInt("buyItemDamage");
        this.buySupply = tag.getInt("buySupply");
        this.buySupplyLeft = tag.getInt("buySupplyLeft");
        this.sellItemID = tag.getInt("sellItemID");
        this.sellItemAmount = tag.getInt("sellItemAmount");
        this.sellItemDamage = tag.getInt("sellItemDamage");

        this.tradeTrigger = ((ExCompoundTag) tag)
            .findCompound("tradeTrigger")
            .map(AC_TriggerArea::getFromTagCompound)
            .orElse(null);
    }

    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putInt("buyItemID", this.buyItemID);
        tag.putInt("buyItemAmount", this.buyItemAmount);
        tag.putInt("buyItemDamage", this.buyItemDamage);
        tag.putInt("buySupply", this.buySupply);
        tag.putInt("buySupplyLeft", this.buySupplyLeft);
        tag.putInt("sellItemID", this.sellItemID);
        tag.putInt("sellItemAmount", this.sellItemAmount);
        tag.putInt("sellItemDamage", this.sellItemDamage);

        if (this.tradeTrigger != null) {
            tag.putCompoundTag("tradeTrigger", this.tradeTrigger.getTagCompound());
        }
    }
}
