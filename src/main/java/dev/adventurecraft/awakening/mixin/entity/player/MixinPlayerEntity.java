package dev.adventurecraft.awakening.mixin.entity.player;

import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.entity.player.AdventureGameMode;
import dev.adventurecraft.awakening.entity.player.DebugGameMode;
import dev.adventurecraft.awakening.entity.player.GameMode;
import dev.adventurecraft.awakening.extension.container.ExPlayerContainer;
import dev.adventurecraft.awakening.extension.entity.player.ExPlayerEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.item.AC_IItemLight;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.mixin.entity.MixinMob;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.world.RayFlags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.BedSleepingProblem;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayerEntity extends MixinMob implements ExPlayerEntity {

    @Shadow public Inventory inventory;
    @Shadow public AbstractContainerMenu inventoryMenu;
    @Shadow public int swingTime;
    @Shadow public boolean swinging;
    @Shadow public String name;

    public boolean isSwingingOffhand;
    public int swingProgressIntOffhand;
    public float prevSwingProgressOffhand;
    public float swingProgressOffhand;
    private boolean swappedItems;
    private int numHeartPieces;
    public String cloakTexture;
    private GameMode gameMode = new AdventureGameMode();

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

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(Level var1, CallbackInfo ci) {
        this.health = 12;
        this.maxHealth = 12;
        this.updateContainer();
    }

    @Environment(EnvType.CLIENT)
    @ModifyConstant(
        method = "resetPos",
        constant = @Constant(intValue = 20)
    )
    private int useMaxHealth0(int constant) {
        return this.maxHealth;
    }

    @Environment(EnvType.CLIENT)
    @Inject(
        method = "resetPos",
        at = @At("TAIL")
    )
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
        }
        else {
            this.swingTime = 0;
        }

        this.attackAnim = (float) this.swingTime / 8.0F;
        if (this.isSwingingOffhand) {
            ++this.swingProgressIntOffhand;
            if (this.swingProgressIntOffhand == 8) {
                this.swingProgressIntOffhand = 0;
                this.isSwingingOffhand = false;
            }
        }
        else {
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
        constant = @Constant(
            intValue = 20,
            ordinal = 0
        )
    )
    private int useMaxHealth1(int constant) {
        return this.maxHealth;
    }

    @Unique
    private static boolean isSimpleLight(ItemInstance item) {
        if (item == null) {
            return false;
        }
        return item.id == Tile.TORCH.id || item.id == AC_Blocks.lights1.id;
    }

    @Unique
    private static boolean isComplexLight(Entity entity, ItemInstance item) {
        if (item != null && Item.items[item.id] instanceof AC_IItemLight itemLight) {
            return itemLight.isLighting(entity, item);
        }
        return false;
    }

    @Inject(
        method = "aiStep",
        at = @At("TAIL")
    )
    private void updateLantern(CallbackInfo ci) {
        ItemInstance handItem = this.inventory.getSelected();
        ItemInstance offhandItem = ((ExPlayerInventory) this.inventory).getOffhandItemStack();

        var self = (Player) (Object) this;
        boolean emitLight = (isSimpleLight(handItem) || isSimpleLight(offhandItem)) ||
            (isComplexLight(self, handItem) || isComplexLight(self, offhandItem));

        if (!emitLight) {
            AC_PlayerTorch.setTorchState(this.level, false);
        }
        else {
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

    @Inject(
        method = "startSleepInBed",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    private void onlySleepWhenAllowed(CallbackInfoReturnable<BedSleepingProblem> ci) {
        boolean canSleepBool = ((ExWorldProperties) this.level.levelData).getCanSleep();
        if (!canSleepBool) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"
        )
    )
    private void keepInventoryOnDeath(Inventory instance) {
    }

    @Override
    protected void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$readAdditionalSaveData(tag, ci);
        var exTag = (ExCompoundTag) tag;

        this.numHeartPieces = tag.getInt("NumHeartPieces");
        if (this.maxHealth < 12) {
            this.health = this.health * 12 / this.maxHealth;
            this.maxHealth = 12;
        }

        this.inventory.selected = exTag.findInt("MainHandSlot").orElse(0);
        ((ExPlayerInventory) this.inventory).setOffhandSlot(exTag.findInt("OffHandSlot").orElse(1));
    }

    @Override
    protected void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        super.ac$addAdditionalSaveData(tag, ci);

        tag.putInt("NumHeartPieces", this.numHeartPieces);

        tag.putInt("MainHandSlot", this.inventory.selected);
        tag.putInt("OffHandSlot", ((ExPlayerInventory) this.inventory).getOffhandSlot());
    }

    @Redirect(
        method = "hurt",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/Level;isClientSide:Z"
        )
    )
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
            switch (this.level.difficulty) {
                case 0 -> damage = 0;
                case 1 -> damage = damage / 3 + 1;
                case 3 -> damage = damage * 3 / 2;
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

    @Inject(
        method = "actuallyHurt",
        at = @At("HEAD"),
        cancellable = true
    )
    private void noDamageInDebugMode(int var1, CallbackInfo ci) {
        // TODO: check GameMode whether damage can be taken
        // TODO: cancel "hurt" instead? would cause things to properly "miss"
        if (this.isDebugMode()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "actuallyHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;hurtArmor(I)V"
        )
    )
    private void noDamageToArmor(Inventory instance, int i) {
    }

    @Inject(
        method = "drop()V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void noThrownBoomerangDrop(CallbackInfo ci) {
        var selectedItem = this.inventory.getSelected();
        if (selectedItem != null && selectedItem.id == AC_Items.boomerang.id && selectedItem.getAuxValue() == 1) {
            ci.cancel();
        }
    }

    @Overwrite
    public void interact(Entity entity) {
        var self = (Player) (Object) this;
        if (entity.interact(self)) {
            return;
        }

        ItemInstance heldItem = this.getSelectedItem();
        if (heldItem != null && entity instanceof Mob mob) {
            heldItem.interactEnemy(mob);

            if (heldItem.count == 0) {
                heldItem.snap(self);
                this.removeSelectedItem();
            }
        }
    }

    @Overwrite
    public void swing() {
        if (this.swappedItems) {
            this.swingOffhandItem();
        }
        else {
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

        var self = (Player) (Object) this;
        entity.hurt(self, attackDamage);

        if (entity instanceof Mob mob) {
            ItemInstance heldItem = this.getSelectedItem();
            heldItem.hurtEnemy(mob, self);

            if (heldItem.count == 0) {
                heldItem.snap(self);
                this.removeSelectedItem();
            }

            if (entity.isAlive()) {
                this.method_510(mob, true);
            }

            this.awardStat(Stats.DAMAGE_DEALT, attackDamage);
        }
    }

    @Environment(value = EnvType.CLIENT)
    @Override
    public HitResult pick(double pickRange, float partialTick) {
        Vec3 pointA = this.getPos(partialTick);
        Vec3 dir = this.getViewVector(partialTick);
        Vec3 pointB = pointA.add(dir.x * pickRange, dir.y * pickRange, dir.z * pickRange);

        int flags = this.isDebugMode() ? (RayFlags.DEBUG | (AC_DebugMode.isFluidHittable ? RayFlags.LIQUID : 0)) : 0;
        return ((ExWorld) this.level).rayTraceBlocks2(pointA, pointB, flags);
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
        var hookshot = AC_Items.hookshot;
        if ((hookshot.mainHookshot == null || !hookshot.mainHookshot.attachedToSurface) &&
            (hookshot.offHookshot == null || !hookshot.offHookshot.attachedToSurface)) {
            return super.getGravity();
        }
        return 0.0D;
    }

    @Override
    public boolean isUsingUmbrella() {
        var selected = this.inventory.getSelected();
        if (selected != null && selected.id == AC_Items.umbrella.id) {
            return true;
        }

        var offhand = ((ExPlayerInventory) this.inventory).getOffhandItemStack();
        if (offhand != null) {
            return offhand.id == AC_Items.umbrella.id;
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

    public @Override GameMode getGameMode() {
        return this.gameMode;
    }

    public @Override void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }
}
