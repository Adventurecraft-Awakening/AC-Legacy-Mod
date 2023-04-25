package dev.adventurecraft.awakening.common;

import net.minecraft.entity.BlockEntity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityStore extends BlockEntity {
    public int buyItemID = AC_Items.bomb.id;
    public int buyItemAmount = 3;
    public int buyItemDamage;
    public int buySupply;
    public int buySupplyLeft = 1;
    public int sellItemID = AC_Items.hammer.id; // AC_Items.shotgun.id; TODO
    public int sellItemAmount = 1;
    public int sellItemDamage = 0;
    public AC_TriggerArea tradeTrigger;

    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.buyItemID = var1.getInt("buyItemID");
        this.buyItemAmount = var1.getInt("buyItemAmount");
        this.buyItemDamage = var1.getInt("buyItemDamage");
        this.buySupply = var1.getInt("buySupply");
        this.buySupplyLeft = var1.getInt("buySupplyLeft");
        this.sellItemID = var1.getInt("sellItemID");
        this.sellItemAmount = var1.getInt("sellItemAmount");
        this.sellItemDamage = var1.getInt("sellItemDamage");
        if (var1.containsKey("tradeTrigger")) {
            this.tradeTrigger = AC_TriggerArea.getFromTagCompound(var1.getCompoundTag("tradeTrigger"));
        } else {
            this.tradeTrigger = null;
        }
    }

    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("buyItemID", this.buyItemID);
        var1.put("buyItemAmount", this.buyItemAmount);
        var1.put("buyItemDamage", this.buyItemDamage);
        var1.put("buySupply", this.buySupply);
        var1.put("buySupplyLeft", this.buySupplyLeft);
        var1.put("sellItemID", this.sellItemID);
        var1.put("sellItemAmount", this.sellItemAmount);
        var1.put("sellItemDamage", this.sellItemDamage);
        if (this.tradeTrigger != null) {
            var1.put("tradeTrigger", this.tradeTrigger.getTagCompound());
        }
    }
}
