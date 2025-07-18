package dev.adventurecraft.awakening.mixin.entity.player;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.item.AC_IItemLight;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.mixin.entity.MixinMob;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends MixinMob implements ExPlayerEntity {

    @Shadow
    public Inventory inventory;

    @Shadow
    public AbstractContainerMenu inventoryMenu;

    @Shadow
    public int swingTime;

    @Shadow
    public boolean swinging;

    @Shadow
    public String name;

    public boolean isSwingingOffhand;
    public int swingProgressIntOffhand;
    public float prevSwingProgressOffhand;
    public float swingProgressOffhand;
    private boolean swappedItems;
    private int numHeartPieces;
    public String cloakTexture;
    private boolean allowsCrafting;

    @Shadow
    public abstract ItemInstance getSelectedItem();

    @Shadow
    public abstract void removeSelectedItem();

    @Shadow
    protected abstract void method_510(Mob arg, boolean bl);

    @Shadow
    public abstract void awardStat(Stat arg, int i);

    @Shadow
    public abstract boolean isSleeping();

    @Shadow
    public abstract void stopSleepInBed(boolean bl, boolean bl2, boolean bl3);

    @Shadow public AbstractContainerMenu containerMenu;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Level var1, CallbackInfo ci) {
        this.health = 12;
        this.maxHealth = 12;
        this.updateContainer();
    }

    @Environment(EnvType.CLIENT)
    @ModifyConstant(method = "resetPos", constant = @Constant(intValue = 20))
    private int useMaxHealth0(int constant) {
        return this.maxHealth;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "resetPos", at = @At("TAIL"))
    private void resetAfterSpawn(CallbackInfo ci) {
        AC_Items.hookshot.resetPlayerHookshotState();
        this.removed = false;
        this.onFire = -this.flameTime;
    }

    @Overwrite
    public void serverAiStep() {
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime == 8) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }

        this.attackAnim = (float) this.swingTime / 8.0F;
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
        if (inventoryMenu instanceof ExPlayerContainer exContainer) {
            boolean allowsCrafting = ((ExWorldProperties) this.level.levelData).getAllowsInventoryCrafting();
            exContainer.setAllowsCrafting(allowsCrafting);
        }
    }

    @ModifyConstant(
        method = "aiStep",
        constant = @Constant(intValue = 20, ordinal = 0))
    private int useMaxHealth1(int constant) {
        return this.maxHealth;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
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
            AC_PlayerTorch.setTorchState(this.level, false);
        } else {
            AC_PlayerTorch.setTorchState(this.level, true);
            AC_PlayerTorch.setTorchPos(this.level, (float) this.x, (float) this.y, (float) this.z);
        }

        if (this.yd < -0.2D && this.isUsingUmbrella()) {
            this.yd = -0.2D;
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

    @Inject(method = "startSleepInBed", at = @At(value = "HEAD"), cancellable = true)
    private void onlySleepWhenAllowed(CallbackInfoReturnable ci) {
        boolean canSleepBool = ((ExWorldProperties) this.level.levelData).getCanSleep();
        if (!canSleepBool) {
            ci.cancel();
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
            AC_PlayerTorch.setTorchState(this.level, true);
            AC_PlayerTorch.setTorchPos(this.level, (float) this.x, (float) this.y, (float) this.z);
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
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void keepInventoryOnDeath(Inventory instance) {
    }

    @Override
    protected void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$readAdditionalSaveData(tag, ci);

        this.numHeartPieces = tag.getInt("NumHeartPieces");
        if (this.maxHealth < 12) {
            this.health = this.health * 12 / this.maxHealth;
            this.maxHealth = 12;
        }
    }

    @Override
    protected void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$addAdditionalSaveData(tag, ci);

        tag.putInt("NumHeartPieces", this.numHeartPieces);
    }

    @Redirect(
        method = "hurt",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    private boolean alwaysGetOutOfBedOnDamage(Level instance) {
        return false;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        this.noActionTime = 0;
        if (this.health <= 0) {
            return false;
        }

        if (this.isSleeping()) {
            this.stopSleepInBed(true, true, false);
        }

        if (entity instanceof Monster || entity instanceof Arrow) {
            if (this.level.difficulty == 0) {
                damage = 0;
            }

            if (this.level.difficulty == 1) {
                damage = damage / 3 + 1;
            }

            if (this.level.difficulty == 3) {
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

        if (owner instanceof Mob livingOwner) {
            this.method_510(livingOwner, false);
        }

        this.awardStat(Stats.DAMAGE_TAKEN, damage);
        return super.attackEntityFromMulti(entity, damage);
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void noDamageInDebugMode(int var1, CallbackInfo ci) {
        if (AC_DebugMode.active) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "actuallyHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;hurtArmor(I)V"))
    private void noDamageToArmor(Inventory instance, int i) {
    }

    @Inject(method = "drop()V", at = @At("HEAD"), cancellable = true)
    private void noThrownBoomerangDrop(CallbackInfo ci) {
        if (this.inventory.getSelected() != null && this.inventory.getSelected().id == AC_Items.boomerang.id && this.inventory.getSelected().getAuxValue() == 1) {
            ci.cancel();
        }
    }

    @Overwrite
    public void interact(Entity entity) {
        if (entity.interact((Player) (Object) this)) {
            return;
        }

        ItemInstance heldItem = this.getSelectedItem();
        if (heldItem != null && entity instanceof Mob) {
            heldItem.interactEnemy((Mob) entity);

            if (heldItem.count == 0) {
                heldItem.snap((Player) (Object) this);
                this.removeSelectedItem();
            }
        }
    }

    @Overwrite
    public void swing() {
        if (this.swappedItems) {
            this.swingOffhandItem();
        } else {
            this.swingTime = -1;
            this.swinging = true;
        }
    }

    @Overwrite
    public void attack(Entity entity) {
        int attackDamage = this.inventory.getAttackDamage(entity);
        if (attackDamage <= 0) {
            return;
        }

        if (this.yd < 0.0D) {
            ++attackDamage;
        }

        entity.hurt((Entity) (Object) this, attackDamage);
        ItemInstance heldItem = this.getSelectedItem();
        if (heldItem != null && entity instanceof Mob) {
            heldItem.hurtEnemy((Mob) entity, (Player) (Object) this);

            if (heldItem.count == 0) {
                heldItem.snap((Player) (Object) this);
                this.removeSelectedItem();
            }
        }

        if (entity instanceof Mob) {
            if (entity.isAlive()) {
                this.method_510((Mob) entity, true);
            }

            this.awardStat(Stats.DAMAGE_DEALT, attackDamage);
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

    @Override
    public void openPalette() {
    }

    @Overwrite
    public float getDestroySpeed(Tile tile) {
        float destroySpeed = this.inventory.getDestroySpeed(tile);
        return destroySpeed;
    }
}
