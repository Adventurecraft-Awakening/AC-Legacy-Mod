package dev.adventurecraft.awakening.mixin.entity.player;

import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity implements ExPlayerEntity {

    @Shadow
    public PlayerInventory inventory;
    public boolean isSwingingOffhand;
    public int swingProgressIntOffhand;
    public float prevSwingProgressOffhand;
    public float swingProgressOffhand;
    private boolean swappedItems;
    private int numHeartPieces;
    public String cloakTexture;

    @Override
    public boolean isUsingUmbrella() {
        if (this.inventory.getHeldItem() != null && this.inventory.getHeldItem().itemId == AC_Items.umbrella.id) {
            return true;
        } else {
            ItemStack offhand = ((ExPlayerInventory) this.inventory).getOffhandItemStack();
            if (offhand != null) {
                return offhand.itemId == AC_Items.umbrella.id;
            }
        }
        return false;
    }

    @Override
    public void swingOffhandItem() {
        this.swingProgressIntOffhand = -1;
        this.isSwingingOffhand = true;
    }

    @Override
    public float getSwingOffhandProgress(float var1) {
        float var2 = this.swingProgressOffhand - this.prevSwingProgressOffhand;
        if (var2 < 0.0F) {
            ++var2;
        }
        return this.prevSwingProgressOffhand + var2 * var1;
    }

    @Override
    public int getHeartPiecesCount() {
        return this.numHeartPieces;
    }

    @Override
    public void setHeartPiecesCount(int value) {
        this.numHeartPieces = value;
    }

    @Override
    public boolean areSwappedItems() {
        return this.swappedItems;
    }

    @Override
    public void setSwappedItems(boolean value) {
        this.swappedItems = value;
    }

    @Override
    public String getCloakTexture() {
        return this.cloakTexture;
    }
}
