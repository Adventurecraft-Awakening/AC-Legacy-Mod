package dev.adventurecraft.awakening.mixin.entity.player;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.mixin.entity.MixinLivingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends MixinLivingEntity implements ExPlayerEntity {

    @Shadow
    public Inventory inventory;

    @Shadow
    public AbstractContainerMenu playerContainer;

    @Shadow
    public int handSwingTicks;

    @Shadow
    public boolean handSwinging;

    public boolean isSwingingOffhand;
    public int swingProgressIntOffhand;
    public float prevSwingProgressOffhand;
    public float swingProgressOffhand;
    private boolean swappedItems;
    private int numHeartPieces;
    public String cloakTexture;
    private boolean allowsCrafting;

    @Shadow
    public abstract ItemInstance getHeldItem();

    @Shadow
    public abstract void breakHeldItem();

    @Shadow
    protected abstract void method_510(LivingEntity arg, boolean bl);

    @Shadow
    public abstract void increaseStat(Stat arg, int i);

    @Shadow
    public abstract boolean isLyingOnBed();

    @Shadow
    public abstract void getOutOfBed(boolean bl, boolean bl2, boolean bl3);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.health = 12;
        this.maxHealth = 12;
        this.updateContainer();
    }

    @ModifyConstant(method = "afterSpawn", constant = @Constant(intValue = 20))
    private int useMaxHealth0(int constant) {
        return this.maxHealth;
    }

    @Inject(method = "afterSpawn", at = @At("TAIL"))
    private void resetAfterSpawn(CallbackInfo ci) {
        AC_Items.hookshot.resetPlayerHookshotState();
        this.removed = false;
        this.fireTicks = -this.field_1646;
    }

    @Overwrite
    public void tickHandSwing() {
        if (this.handSwinging) {
            ++this.handSwingTicks;
            if (this.handSwingTicks == 8) {
                this.handSwingTicks = 0;
                this.handSwinging = false;
            }
        } else {
            this.handSwingTicks = 0;
        }

        this.handSwingProgress = (float) this.handSwingTicks / 8.0F;
        if (this.isSwingingOffhand) {
            ++this.swingProgressIntOffhand;
            if (this.swingProgressIntOffhand == 8) {
                this.swingProgressIntOffhand = 0;
                this.isSwingingOffhand = false;
            }
        } else {
            this.swingProgressIntOffhand = 0;
        }

        this.swingProgressOffhand = (float) this.swingProgressIntOffhand / 8.0F;
    }

    @Override
    public void baseTick() {
        this.updateContainer();
        this.prevSwingProgressOffhand = this.swingProgressOffhand;
        super.baseTick();
    }

    private void updateContainer() {
        if (playerContainer instanceof ExPlayerContainer exContainer) {
            boolean allowsCrafting = ((ExWorldProperties) Minecraft.instance.level.levelData).getAllowsInventoryCrafting();
            exContainer.setAllowsCrafting(allowsCrafting);
        }
    }

    @ModifyConstant(
        method = "updateDespawnCounter",
        constant = @Constant(intValue = 20, ordinal = 0))
    private int useMaxHealth1(int constant) {
        return this.maxHealth;
    }

    @Inject(method = "updateDespawnCounter", at = @At("TAIL"))
    private void updateLantern(CallbackInfo ci) {
        ItemInstance handItem = this.inventory.getSelected();
        ItemInstance offhandItem = ((ExPlayerInventory) this.inventory).getOffhandItemStack();

        boolean emitLight = false;
        if (handItem != null && Item.items[handItem.id] instanceof AC_IItemLight handLight) {
            emitLight |= handLight.isLighting(handItem);
        }
        if (offhandItem != null && Item.items[offhandItem.id] instanceof AC_IItemLight offhandLight) {
            emitLight |= offhandLight.isLighting(offhandItem);
        }

        if (!emitLight &&
            (handItem == null || handItem.id != Tile.TORCH.id && handItem.id != AC_Blocks.lights1.id) &&
            (offhandItem == null || offhandItem.id != Tile.TORCH.id && offhandItem.id != AC_Blocks.lights1.id)) {
            AC_PlayerTorch.setTorchState(this.world, false);
        } else {
            AC_PlayerTorch.setTorchState(this.world, true);
            AC_PlayerTorch.setTorchPos(this.world, (float) this.x, (float) this.y, (float) this.z);
        }

        if (this.yVelocity < -0.2D && this.isUsingUmbrella()) {
            this.yVelocity = -0.2D;
        }

        if (!this.onGround) {
            if (handItem != null && handItem.id == AC_Items.umbrella.id) {
                handItem.setDamage(1);
            }

            if (offhandItem != null && offhandItem.id == AC_Items.umbrella.id) {
                offhandItem.setDamage(1);
            }
        }
    }

    private boolean handleLantern(ItemInstance item) {
        if (item == null) {
            return false;
        }
        if (item.id != AC_Items.lantern.id) {
            return false;
        }

        if (item.getAuxValue() < item.getMaxDamage()) {
            item.setDamage(item.getAuxValue() + 1);
            AC_PlayerTorch.setTorchState(this.world, true);
            AC_PlayerTorch.setTorchPos(this.world, (float) this.x, (float) this.y, (float) this.z);
        }

        if (item.getAuxValue() == item.getMaxDamage()) {
            if (!this.inventory.removeResource(AC_Items.oil.id)) {
                return false;
            }

            item.setDamage(0);
        }

        return true;
    }

    @Redirect(
        method = "onKilledBy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/PlayerInventory;dropInventory()V"))
    private void keepInventoryOnDeath(Inventory instance) {
    }

    @Inject(method = "readAdditional", at = @At("TAIL"))
    private void readAdditionalAC(CompoundTag var1, CallbackInfo ci) {
        this.numHeartPieces = var1.getInt("NumHeartPieces");
        if (this.maxHealth < 12) {
            this.health = this.health * 12 / this.maxHealth;
            this.maxHealth = 12;
        }
    }

    @Inject(method = "writeAdditional", at = @At("TAIL"))
    private void writeAdditionalAC(CompoundTag var1, CallbackInfo ci) {
        var1.putInt("NumHeartPieces", this.numHeartPieces);
    }

    @Redirect(
        method = "damage",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;isClient:Z"))
    private boolean alwaysGetOutOfBedOnDamage(Level instance) {
        return false;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        this.despawnCounter = 0;
        if (this.health <= 0) {
            return false;
        }

        if (this.isLyingOnBed()) {
            this.getOutOfBed(true, true, false);
        }

        if (entity instanceof Monster || entity instanceof Arrow) {
            if (this.world.difficulty == 0) {
                damage = 0;
            }

            if (this.world.difficulty == 1) {
                damage = damage / 3 + 1;
            }

            if (this.world.difficulty == 3) {
                damage = damage * 3 / 2;
            }
        }

        if (damage == 0) {
            return false;
        }

        Entity owner = entity;
        if (entity instanceof Arrow arrow && arrow.owner != null) {
            owner = arrow.owner;
        }

        if (owner instanceof LivingEntity livingOwner) {
            this.method_510(livingOwner, false);
        }

        this.increaseStat(Stats.DAMAGE_TAKEN, damage);
        return super.attackEntityFromMulti(entity, damage);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void noDamageInDebugMode(int var1, CallbackInfo ci) {
        if (AC_DebugMode.active) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "applyDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/PlayerInventory;damageArmor(I)V"))
    private void noDamageToArmor(Inventory instance, int i) {
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void noThrownBoomerangDrop(CallbackInfo ci) {
        if (this.inventory.getSelected() != null && this.inventory.getSelected().id == AC_Items.boomerang.id && this.inventory.getSelected().getAuxValue() == 1) {
            ci.cancel();
        }
    }

    @Overwrite
    public void interactWith(Entity entity) {
        if (entity.interact((Player) (Object) this)) {
            return;
        }

        ItemInstance heldItem = this.getHeldItem();
        if (heldItem != null && entity instanceof LivingEntity) {
            heldItem.interactEnemy((LivingEntity) entity);

            if (heldItem.count == 0) {
                heldItem.snap((Player) (Object) this);
                this.breakHeldItem();
            }
        }
    }

    @Overwrite
    public void swingHand() {
        if (this.swappedItems) {
            this.swingOffhandItem();
        } else {
            this.handSwingTicks = -1;
            this.handSwinging = true;
        }
    }

    @Overwrite
    public void attack(Entity entity) {
        int attackDamage = this.inventory.getAttackDamage(entity);
        if (attackDamage <= 0) {
            return;
        }

        if (this.yVelocity < 0.0D) {
            ++attackDamage;
        }

        entity.hurt((Entity) (Object) this, attackDamage);
        ItemInstance heldItem = this.getHeldItem();
        if (heldItem != null && entity instanceof LivingEntity) {
            heldItem.hurtEnemy((LivingEntity) entity, (Player) (Object) this);

            if (heldItem.count == 0) {
                heldItem.snap((Player) (Object) this);
                this.breakHeldItem();
            }
        }

        if (entity instanceof LivingEntity) {
            if (entity.isAlive()) {
                this.method_510((LivingEntity) entity, true);
            }

            this.increaseStat(Stats.DAMAGE_DEALT, attackDamage);
        }
    }

    @Override
    public boolean protectedByShield() {
        ItemInstance var1 = this.inventory.getSelected();
        ItemInstance var2 = ((ExPlayerInventory) this.inventory).getOffhandItemStack();
        if (var1 != null && var1.id == AC_Items.woodenShield.id ||
            var2 != null && var2.id == AC_Items.woodenShield.id) {
            return this.getSwingOffhandProgress(1.0F) == 0.0F;
        }
        return false;
    }

    @Override
    public double getGravity() {
        if ((AC_Items.hookshot.mainHookshot == null || !AC_Items.hookshot.mainHookshot.attachedToSurface) &&
            (AC_Items.hookshot.offHookshot == null || !AC_Items.hookshot.offHookshot.attachedToSurface)) {
            return super.getGravity();
        }
        return 0.0D;
    }

    @Override
    public boolean isUsingUmbrella() {
        if (this.inventory.getSelected() != null && this.inventory.getSelected().id == AC_Items.umbrella.id) {
            return true;
        } else {
            ItemInstance offhand = ((ExPlayerInventory) this.inventory).getOffhandItemStack();
            if (offhand != null) {
                return offhand.id == AC_Items.umbrella.id;
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

    @Override
    public void setCloakTexture(String value) {
        this.cloakTexture = value;
    }
}
