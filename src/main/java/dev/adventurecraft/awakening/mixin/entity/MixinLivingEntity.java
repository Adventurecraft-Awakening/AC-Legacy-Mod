package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.common.AC_Items;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends MixinEntity implements ExLivingEntity {

    @Shadow
    public int field_1009;

    @Shadow
    protected String texture;

    @Shadow
    protected String type;

    @Shadow
    public float handSwingProgress;

    @Shadow
    public int health;

    @Shadow
    public int prevHealth;

    @Shadow
    public int hurtTime;

    @Shadow
    public int field_1039;

    @Shadow
    public float field_1040;

    @Shadow
    public float field_1048;

    @Shadow
    public float limbDistance;

    @Shadow
    public float field_1050;

    @Shadow
    public int field_1058;

    @Shadow
    protected int despawnCounter;

    @Shadow
    protected float horizontalVelocity;

    @Shadow
    protected float forwardVelocity;

    @Shadow
    protected float field_1030;

    @Shadow
    public boolean jumping;

    @Shadow
    protected float field_1032;

    @Shadow
    protected float movementSpeed;

    @Shadow
    public Entity target;

    @Shadow
    protected int field_1034;

    public int maxHealth = 10;
    @Unique
    private ItemStack ac$heldItem;
    private long hurtTick;
    public int timesCanJumpInAir = 0;
    public int jumpsLeft = 0;
    public boolean canWallJump = false;
    private long tickBeforeNextJump;
    public double jumpVelocity = 0.42D;
    public double jumpWallMultiplier = 1.0D;
    public double jumpInAirMultiplier = 1.0D;
    public float airControl = 0.9259F;
    public double gravity = 0.08D;
    public float fov = 140.0F;
    public float extraFov = 0.0F;
    public boolean canLookRandomly = true;
    public float randomLookVelocity = 20.0F;
    public int randomLookNext = 0;
    public int randomLookRate = 100;
    public int randomLookRateVariation = 40;

    @Shadow
    public abstract void lookAt(Entity arg, float f, float g);

    @Shadow
    public void writeAdditional(CompoundTag arg) {
        throw new AssertionError();
    }

    @Shadow
    public void readAdditional(CompoundTag arg) {
        throw new AssertionError();
    }

    @Shadow
    public abstract float getStandingEyeHeight();

    @Shadow
    protected abstract void applyDamage(int i);

    @Shadow
    public abstract void method_925(Entity arg, int i, double d, double e);

    @Shadow
    protected abstract String getDeathSound();

    @Shadow
    protected abstract float getSoundVolume();

    @Shadow
    protected abstract String getHurtSound();

    @Shadow
    public abstract void onKilledBy(Entity arg);

    @Shadow
    protected abstract void tryDespawn();

    @Shadow
    protected abstract int getLookPitchSpeed();

    @Shadow
    public abstract float getHandSwingProgress(float f);

    @Shadow
    public void baseTick() {
        throw new AssertionError();
    }

    @Overwrite
    public boolean method_928(Entity var1) {
        double var2 = -180.0D * Math.atan2(var1.x - this.x, var1.z - this.z) / Math.PI;

        double var4 = var2 - this.yaw;
        while (var4 < -180.0D) {
            var4 += 360.0D;
        }

        while (var4 > 180.0D) {
            var4 -= 360.0D;
        }

        if (Math.abs(var4) > (this.fov / 2.0F + this.extraFov)) {
            return false;
        }

        Vec3d start = Vec3d.from(this.x, this.y + this.getStandingEyeHeight(), this.z);
        Vec3d end = Vec3d.from(var1.x, var1.y + var1.getStandingEyeHeight(), var1.z);
        return this.world.method_160(start, end) == null;
    }

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isInsideWall()Z"))
    private boolean damageIfNotCondition(boolean value) {
        return value && !this.field_1642;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void reduceExtraFov(CallbackInfo ci) {
        if (this.extraFov > 0.0F) {
            this.extraFov -= 5.0F;
            if (this.extraFov < 0.0F) {
                this.extraFov = 0.0F;
            }
        }
    }

    @ModifyConstant(method = "addHealth", constant = @Constant(intValue = 20))
    private int useMaxHealth(int constant) {
        return this.maxHealth;
    }

    @Inject(
        method = "damage",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/LivingEntity;hurtTime:I",
            shift = At.Shift.AFTER))
    private void setHurtTickOnDamage(Entity var1, int var2, CallbackInfoReturnable<Boolean> cir) {
        this.hurtTick = this.world.getWorldTime();
    }

    @Overwrite
    public boolean damage(Entity var1, int var2) {
        if (this.world.isClient) {
            return false;
        }

        this.despawnCounter = 0;
        if (this.health <= 0) {
            return false;
        }

        this.extraFov = 180.0F;
        this.limbDistance = 1.5F;
        boolean var3 = true;
        if ((float) this.field_1613 > (float) this.field_1009 / 2.0F && this.hurtTime > 0) {
            if (var2 <= this.field_1058) {
                return false;
            }

            this.applyDamage(var2 - this.field_1058);
            this.field_1058 = var2;
            var3 = false;
        } else {
            this.field_1058 = var2;
            this.prevHealth = this.health;
            this.field_1613 = this.field_1009;
            this.applyDamage(var2);
            this.hurtTime = this.field_1039 = 10;
            this.hurtTick = this.world.getWorldTime();
        }

        this.field_1040 = 0.0F;
        if (var3) {
            this.world.method_185((Entity) (Object) this, (byte) 2);
            this.setAttacked();
            if (var1 != null) {
                double var4 = var1.x - this.x;

                double var6;
                for (var6 = var1.z - this.z; var4 * var4 + var6 * var6 < 1.0E-4D; var6 = (Math.random() - Math.random()) * 0.01D) {
                    var4 = (Math.random() - Math.random()) * 0.01D;
                }

                this.field_1040 = (float) (Math.atan2(var6, var4) * 180.0D / (double) ((float) Math.PI)) - this.yaw;
                this.method_925(var1, var2, var4, var6);
            } else {
                this.field_1040 = (float) ((int) (Math.random() * 2.0D) * 180);
            }
        }

        if (this.health <= 0) {
            if (var3) {
                this.world.playSound((Entity) (Object) this, this.getDeathSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }

            this.onKilledBy(var1);
        } else if (var3) {
            this.world.playSound((Entity) (Object) this, this.getHurtSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }

        return true;
    }

    @Override
    public boolean attackEntityFromMulti(Entity var1, int var2) {
        if (this.world.isClient) {
            return false;
        }

        this.despawnCounter = 0;
        if (this.health <= 0) {
            return false;
        }

        this.extraFov = 180.0F;
        this.limbDistance = 1.5F;
        boolean var3 = true;
        if ((float) this.field_1613 > (float) this.field_1009 / 2.0F && this.hurtTick != this.world.getWorldTime()) {
            if (var2 <= this.field_1058) {
                return false;
            }

            this.applyDamage(var2 - this.field_1058);
            this.field_1058 = var2;
            var3 = false;
        } else {
            this.field_1058 = var2;
            this.prevHealth = this.health;
            this.field_1613 = this.field_1009;
            this.applyDamage(var2);
            this.hurtTime = this.field_1039 = 10;
            this.hurtTick = this.world.getWorldTime();
        }

        this.field_1040 = 0.0F;
        if (var3) {
            this.world.method_185((Entity) (Object) this, (byte) 2);
            this.setAttacked();
            if (var1 != null) {
                double var4 = var1.x - this.x;

                double var6;
                for (var6 = var1.z - this.z; var4 * var4 + var6 * var6 < 1.0E-4D; var6 = (Math.random() - Math.random()) * 0.01D) {
                    var4 = (Math.random() - Math.random()) * 0.01D;
                }

                this.field_1040 = (float) (Math.atan2(var6, var4) * 180.0D / (double) ((float) Math.PI)) - this.yaw;
                this.method_925(var1, var2, var4, var6);
            } else {
                this.field_1040 = (float) ((int) (Math.random() * 2.0D) * 180);
            }
        }

        if (this.health <= 0) {
            if (var3) {
                this.world.playSound((Entity) (Object) this, this.getDeathSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }

            this.onKilledBy(var1);
        } else if (var3) {
            this.world.playSound((Entity) (Object) this, this.getHurtSound(), this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }

        return true;
    }

    @Overwrite
    public void handleFallDamage(float var1) {
        if (this.handleFlying()) {
            return;
        }

        float amount = Math.max(var1 - 0.8F, 0.0F) / 0.08F;
        int ceilAmount = (int) Math.ceil(Math.pow(amount, 1.5D));
        if (ceilAmount > 0) {
            this.damage(null, ceilAmount);

            int id = this.world.getBlockId(
                MathHelper.floor(this.x),
                MathHelper.floor(this.y - 0.2 - this.standingEyeHeight),
                MathHelper.floor(this.z));

            if (id > 0) {
                BlockSounds block = Block.BY_ID[id].sounds;
                this.world.playSound((Entity) (Object) this, block.getWalkSound(), block.getVolume() * 0.5F, block.getPitch() * (12.0F / 16.0F));
            }
        }
    }

    @Overwrite
    public void travel(float var1, float var2) {
        double var3;
        double var5;
        float var7;
        if (this.handleFlying()) {
            var3 = Math.sqrt(var1 * var1 + var2 * var2);
            var5 = (double) (-0.1F * var2) * Math.sin(this.pitch * Math.PI / 180.0);
            if (var3 < 1.0D) {
                var5 *= var3;
            }

            this.yVelocity += var5;
            var7 = (float) (0.1 * (Math.abs(var2 * Math.cos(this.pitch * Math.PI / 180.0)) + Math.abs(var1)));
            this.movementInputToVelocity(var1, var2, var7);
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            this.fallDistance = 0.0F;
            this.xVelocity *= 0.8D;
            this.yVelocity *= 0.8D;
            this.zVelocity *= 0.8D;
            if (Math.abs(this.xVelocity) < 0.01D) {
                this.xVelocity = 0.0D;
            }

            if (Math.abs(this.yVelocity) < 0.01D) {
                this.yVelocity = 0.0D;
            }

            if (Math.abs(this.zVelocity) < 0.01D) {
                this.zVelocity = 0.0D;
            }
        } else if (this.method_1334()) {
            if (this.yVelocity < -0.4D) {
                this.yVelocity *= 0.8D;
            }

            var3 = this.y;
            this.movementInputToVelocity(var1, var2, 0.02F);
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            this.xVelocity *= 0.8F;
            this.yVelocity *= 0.8F;
            this.zVelocity *= 0.8F;
            this.yVelocity -= 0.25D * this.getGravity();
            if (this.field_1624 && this.method_1344(this.xVelocity, this.yVelocity + (double) 0.6F - this.y + var3, this.zVelocity)) {
                this.yVelocity = 0.3F;
            }
        } else if (this.method_1335()) {
            if (this.yVelocity < -0.4D) {
                this.yVelocity *= 0.5D;
            }

            var3 = this.y;
            this.movementInputToVelocity(var1, var2, 0.02F);
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            this.xVelocity *= 0.5D;
            this.yVelocity *= 0.5D;
            this.zVelocity *= 0.5D;
            this.yVelocity -= 0.25D * this.getGravity();
            if (this.field_1624 && this.method_1344(this.xVelocity, this.yVelocity + (double) 0.6F - this.y + var3, this.zVelocity)) {
                this.yVelocity = 0.3F;
            }
        } else {
            float var8 = 0.91F;
            if (this.onGround) {
                var8 = 0.5460001F;
                int var4 = this.world.getBlockId(MathHelper.floor(this.x), MathHelper.floor(this.boundingBox.minY) - 1, MathHelper.floor(this.z));
                if (var4 > 0) {
                    var8 = Block.BY_ID[var4].slipperiness * 0.91F;
                }
            }

            float var9 = 0.1627714F / (var8 * var8 * var8);
            this.movementInputToVelocity(var1, var2, this.onGround ? 0.1F * var9 : 0.1F * this.airControl * var9);
            var8 = 0.91F;
            if (this.onGround) {
                var8 = 0.5460001F;
                int var10 = this.world.getBlockId(MathHelper.floor(this.x), MathHelper.floor(this.boundingBox.minY) - 1, MathHelper.floor(this.z));
                if (var10 > 0) {
                    var8 = Block.BY_ID[var10].slipperiness * 0.91F;
                }
            }

            if (this.method_932()) {
                float var11 = 0.15F;
                if (this.xVelocity < (double) (-var11)) {
                    this.xVelocity = -var11;
                }

                if (this.xVelocity > (double) var11) {
                    this.xVelocity = var11;
                }

                if (this.zVelocity < (double) (-var11)) {
                    this.zVelocity = -var11;
                }

                if (this.zVelocity > (double) var11) {
                    this.zVelocity = var11;
                }

                this.fallDistance = 0.0F;
                if (this.yVelocity < -0.15D) {
                    this.yVelocity = -0.15D;
                }

                if (this.method_1373() && this.yVelocity < 0.0D) {
                    this.yVelocity = 0.0D;
                }
            }

            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            if ((this.field_1624 || this.jumping) && this.method_932()) {
                this.yVelocity = 0.2D;
            }

            this.yVelocity -= this.getGravity();
            this.yVelocity *= 0.98F;
            this.xVelocity *= var8;
            this.zVelocity *= var8;
        }

        this.field_1048 = this.limbDistance;
        var3 = this.x - this.prevX;
        var5 = this.z - this.prevZ;
        var7 = MathHelper.sqrt(var3 * var3 + var5 * var5) * 4.0F;
        if (var7 > 1.0F) {
            var7 = 1.0F;
        }

        this.limbDistance += (var7 - this.limbDistance) * 0.4F;
        this.field_1050 += this.limbDistance;
    }

    @Overwrite
    public boolean method_932() {
        int x = MathHelper.floor(this.x);
        int y = MathHelper.floor(this.boundingBox.minY);
        int z = MathHelper.floor(this.z);
        if (isClimbable(this.world, x, y, z)) {
            return true;
        }
        if (isClimbable(this.world, x, y + 1, z)) {
            return true;
        }
        return false;
    }

    private static boolean isClimbable(BlockView world, int x, int y, int z) {
        int id = world.getBlockId(x, y, z);
        if (ExLadderBlock.isLadderID(id)) {
            return true;
        }

        if (id == AC_Blocks.ropes1.id ||
            id == AC_Blocks.ropes2.id ||
            id == AC_Blocks.chains.id) {

            boolean meta = world.getBlockMeta(x, y, z) % 3 == 0;
            if (meta) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "writeAdditional", at = @At("TAIL"))
    private void writeAdditionalAC(CompoundTag var1, CallbackInfo ci) {
        var1.put("MaxHealth", (short) this.maxHealth);
        var1.put("EntityID", this.entityId);
        var1.put("timesCanJumpInAir", this.timesCanJumpInAir);
        var1.put("canWallJump", this.canWallJump);
        var1.put("fov", this.fov);
        var1.put("canLookRandomly", this.canLookRandomly);
        var1.put("randomLookVelocity", this.randomLookVelocity);
        var1.put("randomLookRate", this.randomLookRate);
        var1.put("randomLookRateVariation", this.randomLookRateVariation);
    }

    @Inject(method = "readAdditional", at = @At("TAIL"))
    private void readAdditionalAC(CompoundTag var1, CallbackInfo ci) {
        if (!var1.containsKey("MaxHealth")) {
            this.maxHealth = 10;
        } else {
            this.maxHealth = var1.getShort("MaxHealth");
        }

        //noinspection ConstantValue
        if (var1.containsKey("EntityID") && !(((Object) this instanceof PlayerEntity))) {
            this.entityId = var1.getInt("EntityID");
        }

        this.timesCanJumpInAir = var1.getInt("timesCanJumpInAir");
        this.canWallJump = var1.getBoolean("canWallJump");
        if (var1.containsKey("fov")) {
            this.fov = var1.getFloat("fov");
        }

        if (var1.containsKey("canLookRandomly")) {
            this.canLookRandomly = var1.getBoolean("canLookRandomly");
        }

        if (var1.containsKey("randomLookVelocity")) {
            this.randomLookVelocity = var1.getFloat("randomLookVelocity");
        }

        if (var1.containsKey("randomLookRate")) {
            this.randomLookRate = var1.getInt("randomLookRate");
        }

        if (var1.containsKey("randomLookRateVariation")) {
            this.randomLookRateVariation = var1.getInt("randomLookRateVariation");
        }
    }

    @Inject(
        method = "updateDespawnCounter",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;method_1335()Z",
            shift = At.Shift.AFTER))
    private void fixupYaw(CallbackInfo ci) {
        if (this.onGround) {
            this.jumpsLeft = this.timesCanJumpInAir;
        }

        if (this.moveYawOffset != 0.0F) {
            if (this.moveYawOffset > 40.0F) {
                this.moveYawOffset -= 40.0F;
                this.yaw += 40.0F;
            } else if (this.moveYawOffset < -40.0F) {
                this.moveYawOffset += 40.0F;
                this.yaw -= 40.0F;
            } else {
                this.yaw += this.moveYawOffset;
                this.moveYawOffset = 0.0F;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Inject(
        method = "updateDespawnCounter",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;method_1335()Z",
            shift = At.Shift.AFTER))
    private void doWallJump(CallbackInfo ci) {
        if (!this.jumping) {
        } else if (this.onGround) {
        } else if (this.method_1334()) {
        } else if (this.method_1335()) {
        } else if (this.world.getWorldTime() >= this.tickBeforeNextJump) {
            if (this.canWallJump && (this.collisionX != 0 || this.collisionZ != 0)) {
                this.jump();
                this.yVelocity *= this.jumpWallMultiplier;
                this.xVelocity += (double) (-this.collisionX) * 0.325D;
                this.zVelocity += (double) (-this.collisionZ) * 0.325D;

                this.moveYawOffset = (float) (180.0D * Math.atan2(-this.xVelocity, this.zVelocity) / Math.PI) - this.yaw;
                while ((double) this.moveYawOffset >= 180.0D) {
                    this.moveYawOffset = (float) ((double) this.moveYawOffset - 360.0D);
                }
                while ((double) this.moveYawOffset < -180.0D) {
                    this.moveYawOffset = (float) ((double) this.moveYawOffset + 360.0D);
                }

                for (int i = 0; i < 10; ++i) {
                    this.world.addParticle("reddust", this.x + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.y - 0.2, this.z + (this.rand.nextFloat() * this.width * 2.0F) - this.width, 2.5D, 2.5D, 2.5D);
                }
            } else if (this.jumpsLeft > 0) {
                --this.jumpsLeft;
                this.jump();
                this.yVelocity *= this.jumpInAirMultiplier;

                for (int i = 0; i < 10; ++i) {
                    this.world.addParticle("reddust", this.x + (this.rand.nextFloat() * this.width * 2.0F) - this.width, this.y - 0.2, this.z + (this.rand.nextFloat() * this.width * 2.0F) - this.width, 2.5D, 2.5D, 2.5D);
                }
            }
        }
    }

    @Overwrite
    public void jump() {
        this.tickBeforeNextJump = this.world.getWorldTime() + 5L;
        this.yVelocity = this.jumpVelocity;
    }

    @Overwrite
    public void tickHandSwing() {
        ++this.despawnCounter;

        this.tryDespawn();
        this.horizontalVelocity = 0.0F;
        this.forwardVelocity = 0.0F;
        float var2 = 8.0F;
        if (this.rand.nextFloat() < 0.02F) {
            PlayerEntity var3 = this.world.getClosestPlayerTo((Entity) (Object) this, var2);
            if (var3 != null && this.method_928(var3)) {
                this.target = var3;
                this.field_1034 = 10 + this.rand.nextInt(20);
            }
        }

        if (this.target != null) {
            this.lookAt(this.target, 10.0F, (float) this.getLookPitchSpeed());
            if (this.field_1034-- <= 0 || this.target.removed || this.target.method_1352((Entity) (Object) this) > (double) (var2 * var2)) {
                this.target = null;
            }
        } else if (this.canLookRandomly) {
            if (this.randomLookNext-- <= 0) {
                float var5 = this.rand.nextFloat();
                if ((double) var5 < 0.5D) {
                    this.field_1030 = -this.randomLookVelocity * (var5 + 0.5F);
                } else {
                    this.field_1030 = this.randomLookVelocity * var5;
                }

                this.randomLookNext = this.randomLookRate + this.rand.nextInt(this.randomLookRateVariation);
            }

            this.yaw += this.field_1030;
            this.pitch = this.field_1032;
            this.field_1030 *= 0.95F;
            if (Math.abs(this.field_1030) < 1.0F) {
                this.field_1030 = 0.0F;
            }
        }

        boolean var6 = this.method_1334();
        boolean var4 = this.method_1335();
        if (var6 || var4) {
            this.jumping = this.rand.nextFloat() < 0.8F;
        }
    }

    @Inject(
        method = "onKilledBy",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getDrops()V",
            shift = At.Shift.AFTER))
    private void dropHeartsOnDeath(Entity killer, CallbackInfo ci) {
        if (killer instanceof LivingEntity livingKiller) {
            if (livingKiller.health < ((ExLivingEntity) livingKiller).getMaxHealth() && this.rand.nextInt(3) != 0) {
                var item = new ItemEntity(this.world, this.x, this.y, this.z, new ItemStack(AC_Items.heart.id, 1, 0));
                this.world.spawnEntity(item);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public ItemStack getMonsterHeldItem() {
        return this.ac$heldItem;
    }

    @Override
    public boolean protectedByShield() {
        return false;
    }

    @Override
    public boolean protectedByShield(double x, double y, double z) {
        if (!this.protectedByShield() || !(this.getHandSwingProgress(1.0F) <= 0.0F)) {
            return false;
        }

        double var7 = this.x - x;
        double var9 = this.z - z;
        float var11 = -57.29578F * (float) Math.atan2(var7, var9) + 180.0F;

        float var12 = Math.abs(var11 - this.yaw);
        while (var12 > 180.0F) {
            var12 -= 360.0F;
        }

        while (var12 < -180.0F) {
            var12 += 360.0F;
        }

        return var12 < 50.0F;
    }

    @Override
    public double getGravity() {
        return this.gravity;
    }

    @Override
    public void setGravity(double value) {
        this.gravity = value;
    }

    @Override
    public int getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public void setMaxHealth(int value) {
        this.maxHealth = value;
    }

    @Override
    public ItemStack getHeldItem() {
        return this.ac$heldItem;
    }

    @Override
    public void setHeldItem(ItemStack value) {
        this.ac$heldItem = value;
    }

    @Override
    public boolean getCanWallJump() {
        return this.canWallJump;
    }

    @Override
    public void setCanWallJump(boolean value) {
        this.canWallJump = value;
    }

    @Override
    public int getTimesCanJumpInAir() {
        return this.timesCanJumpInAir;
    }

    @Override
    public void setTimesCanJumpInAir(int value) {
        this.timesCanJumpInAir = value;
    }

    @Override
    public void setTexture(String value) {
        this.texture = value;
    }

    @Override
    public float getFov() {
        return this.fov;
    }

    @Override
    public void setFov(float value) {
        this.fov = value;
    }

    @Override
    public float getExtraFov() {
        return this.extraFov;
    }

    @Override
    public void setExtraFov(float value) {
        this.extraFov = value;
    }

    @Override
    public float getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void setMovementSpeed(float value) {
        this.movementSpeed = value;
    }

    @Override
    public int getJumpsLeft() {
        return jumpsLeft;
    }

    @Override
    public void setJumpsLeft(int value) {
        this.jumpsLeft = value;
    }

    @Override
    public double getJumpVelocity() {
        return jumpVelocity;
    }

    @Override
    public void setJumpVelocity(double value) {
        this.jumpVelocity = value;
    }

    @Override
    public double getJumpWallMultiplier() {
        return jumpWallMultiplier;
    }

    @Override
    public void setJumpWallMultiplier(double value) {
        this.jumpWallMultiplier = value;
    }

    @Override
    public double getJumpInAirMultiplier() {
        return jumpInAirMultiplier;
    }

    public void setJumpInAirMultiplier(double value) {
        this.jumpInAirMultiplier = value;
    }

    @Override
    public float getAirControl() {
        return airControl;
    }

    @Override
    public void setAirControl(float value) {
        this.airControl = value;
    }

    @Override
    public boolean getCanLookRandomly() {
        return canLookRandomly;
    }

    @Override
    public void setCanLookRandomly(boolean value) {
        this.canLookRandomly = value;
    }

    @Override
    public float getRandomLookVelocity() {
        return randomLookVelocity;
    }

    @Override
    public void setRandomLookVelocity(float value) {
        this.randomLookVelocity = value;
    }

    @Override
    public int getRandomLookNext() {
        return randomLookNext;
    }

    @Override
    public void setRandomLookNext(int value) {
        this.randomLookNext = value;
    }

    @Override
    public int getRandomLookRate() {
        return randomLookRate;
    }

    @Override
    public void setRandomLookRate(int value) {
        this.randomLookRate = value;
    }

    @Override
    public int getRandomLookRateVariation() {
        return randomLookRateVariation;
    }

    @Override
    public void setRandomLookRateVariation(int value) {
        this.randomLookRateVariation = value;
    }
}
