package dev.adventurecraft.awakening.common;

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

    public void load(CompoundTag var1) {
        super.load(var1);
        this.buyItemID = var1.getInt("buyItemID");
        this.buyItemAmount = var1.getInt("buyItemAmount");
        this.buyItemDamage = var1.getInt("buyItemDamage");
        this.buySupply = var1.getInt("buySupply");
        this.buySupplyLeft = var1.getInt("buySupplyLeft");
        this.sellItemID = var1.getInt("sellItemID");
        this.sellItemAmount = var1.getInt("sellItemAmount");
        this.sellItemDamage = var1.getInt("sellItemDamage");
        if (var1.hasKey("tradeTrigger")) {
            this.tradeTrigger = AC_TriggerArea.getFromTagCompound(var1.getCompoundTag("tradeTrigger"));
        } else {
            this.tradeTrigger = null;
        }
    }

    public void save(CompoundTag var1) {
        super.save(var1);
        var1.putInt("buyItemID", this.buyItemID);
        var1.putInt("buyItemAmount", this.buyItemAmount);
        var1.putInt("buyItemDamage", this.buyItemDamage);
        var1.putInt("buySupply", this.buySupply);
        var1.putInt("buySupplyLeft", this.buySupplyLeft);
        var1.putInt("sellItemID", this.sellItemID);
        var1.putInt("sellItemAmount", this.sellItemAmount);
        var1.putInt("sellItemDamage", this.sellItemDamage);
        if (this.tradeTrigger != null) {
            var1.putCompoundTag("tradeTrigger", this.tradeTrigger.getTagCompound());
        }
    }
}
