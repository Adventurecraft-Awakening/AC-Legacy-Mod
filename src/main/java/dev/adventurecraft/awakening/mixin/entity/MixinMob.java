package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.entity.ExMob;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.SoundType;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMob extends MixinEntity implements ExMob {

    @Shadow
    public int invulnerableDuration;

    @Shadow
    protected String textureName;

    @Shadow
    public float attackAnim;

    @Shadow
    public int health;

    @Shadow
    public int lastHealth;

    @Shadow
    public int hurtTime;

    @Shadow
    public int hurtDuration;

    @Shadow
    public float hurtDir;

    @Shadow
    public float walkAnimSpeedO;

    @Shadow
    public float walkAnimSpeed;

    @Shadow
    public float walkAnimPos;

    @Shadow
    public int lastHurt;

    @Shadow
    protected int noActionTime;

    @Shadow
    protected float xxa;

    @Shadow
    protected float zza;

    @Shadow
    protected float yRotA;

    @Shadow
    public boolean jumping;

    @Shadow
    protected float defaultLookAngle;

    @Shadow
    protected float runSpeed;

    @Shadow
    public Entity lookAt;

    @Shadow
    protected int lookTime;

    @Unique
    protected int maxHealth = 10;
    @Unique
    private ItemInstance ac$heldItem;
    @Unique
    private long hurtTick;
    @Unique
    public int timesCanJumpInAir = 0;
    @Unique
    public int jumpsLeft = 0;
    @Unique
    public boolean canWallJump = false;
    @Unique
    private long tickBeforeNextJump;
    @Unique
    public double jumpVelocity = 0.42D;
    @Unique
    public double jumpWallMultiplier = 1.0D;
    @Unique
    public double jumpInAirMultiplier = 1.0D;
    @Unique
    public float airControl = 0.9259F;
    @Unique
    public double gravity = 0.08D;
    @Unique
    public float fov = 140.0F;
    @Unique
    public float extraFov = 0.0F;
    @Unique
    public boolean canLookRandomly = true;
    @Unique
    public float randomLookVelocity = 20.0F;
    @Unique
    public int randomLookNext = 0;
    @Unique
    public int randomLookRate = 100;
    @Unique
    public int randomLookRateVariation = 40;
    @Unique
    public boolean canGetFallDamage = true;

    @Shadow
    public abstract void setLookAt(Entity arg, float f, float g);

    @Shadow
    public abstract float getHeadHeight();

    @Shadow
    protected abstract void actuallyHurt(int i);

    @Shadow
    public abstract void knockback(Entity arg, int i, double d, double e);

    @Shadow
    protected abstract String getDeathSound();

    @Shadow
    protected abstract float getSoundVolume();

    @Shadow
    protected abstract String getHurtSound();

    @Shadow
    public abstract void die(Entity killer);

    @Shadow
    protected abstract void checkDespawn();

    @Shadow
    protected abstract int getMaxHeadXRot();

    @Shadow
    public abstract float getAttackAnim(float f);

    @Shadow
    public void baseTick() {
        throw new AssertionError();
    }

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;random()D",
            remap = false
        ))
    private double useFastRandomInInit() {
        return this.random.nextFloat();
    }

    @Overwrite
    public boolean canSee(Entity entity) {
        double entityYaw = -180.0D * Math.atan2(entity.x - this.x, entity.z - this.z) / Math.PI;

        double viewYaw = entityYaw - this.yRot;
        while (viewYaw < -180.0D) {
            viewYaw += 360.0D;
        }

        while (viewYaw > 180.0D) {
            viewYaw -= 360.0D;
        }

        if (Math.abs(viewYaw) > (this.fov / 2.0F + this.extraFov)) {
            return false;
        }

        Vec3 start = Vec3.newTemp(this.x, this.y + this.getHeadHeight(), this.z);
        Vec3 end = Vec3.newTemp(entity.x, entity.y + entity.getHeadHeight(), entity.z);
        return this.level.clip(start, end) == null;
    }

    @ModifyExpressionValue(
        method = "baseTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;isInWall()Z"))
    private boolean damageIfNotCondition(boolean value) {
        return value && !this.noPhysics;
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

    @ModifyConstant(method = "heal", constant = @Constant(intValue = 20))
    private int useMaxHealth(int constant) {
        return this.maxHealth;
    }

    @Inject(
        method = "hurt",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Mob;hurtTime:I",
            shift = At.Shift.AFTER))
    private void setHurtTickOnDamage(Entity entity, int damage, CallbackInfoReturnable<Boolean> cir) {
        this.hurtTick = this.level.getTime();
    }

    @Overwrite
    public boolean hurt(Entity entity, int damage) {
        if (this.level.isClientSide) {
            return false;
        }

        this.noActionTime = 0;
        if (this.health <= 0) {
            return false;
        }

        this.extraFov = 180.0F;
        this.walkAnimSpeed = 1.5F;
        boolean attacked = true;
        if ((float) this.invulnerableTime > (float) this.invulnerableDuration / 2.0F && this.hurtTime > 0) {
            if (damage <= this.lastHurt) {
                return false;
            }

            this.actuallyHurt(damage - this.lastHurt);
            this.lastHurt = damage;
            attacked = false;
        } else {
            this.lastHurt = damage;
            this.lastHealth = this.health;
            this.invulnerableTime = this.invulnerableDuration;
            this.actuallyHurt(damage);
            this.hurtTime = this.hurtDuration = 10;
            this.hurtTick = this.level.getTime();
        }

        this.hurtDir = 0.0F;
        if (attacked) {
            this.level.broadcastEntityEvent((Entity) (Object) this, (byte) 2);
            this.markHurt();
            if (entity != null) {
                double dX = entity.x - this.x;
                double dZ = entity.z - this.z;
                while (dX * dX + dZ * dZ < 1.0E-4D) {
                    dX = (this.random.nextFloat() - this.random.nextFloat()) * 0.01f;
                    dZ = (this.random.nextFloat() - this.random.nextFloat()) * 0.01f;
                }

                this.hurtDir = (float) (Math.atan2(dZ, dX) * 180.0D / Math.PI) - this.yRot;
                this.knockback(entity, damage, dX, dZ);
            } else {
                this.hurtDir = (float) ((int) (this.random.nextFloat() * 2.0F) * 180);
            }
        }

        if (this.health <= 0) {
            if (attacked) {
                this.level.playSound((Entity) (Object) this, this.getDeathSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

            this.die(entity);
        } else if (attacked) {
            this.level.playSound((Entity) (Object) this, this.getHurtSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        }

        return true;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        if (this.level.isClientSide) {
            return false;
        }

        this.noActionTime = 0;
        if (this.health <= 0) {
            return false;
        }

        this.extraFov = 180.0F;
        this.walkAnimSpeed = 1.5F;
        boolean attacked = true;
        if ((float) this.invulnerableTime > (float) this.invulnerableDuration / 2.0F && this.hurtTick != this.level.getTime()) {
            if (damage <= this.lastHurt) {
                return false;
            }

            this.actuallyHurt(damage - this.lastHurt);
            this.lastHurt = damage;
            attacked = false;
        } else {
            this.lastHurt = damage;
            this.lastHealth = this.health;
            this.invulnerableTime = this.invulnerableDuration;
            this.actuallyHurt(damage);
            this.hurtTime = this.hurtDuration = 10;
            this.hurtTick = this.level.getTime();
        }

        this.hurtDir = 0.0F;
        if (attacked) {
            this.level.broadcastEntityEvent((Entity) (Object) this, (byte) 2);
            this.markHurt();
            if (entity != null) {
                double dX = entity.x - this.x;
                double dZ = entity.z - this.z;
                while (dX * dX + dZ * dZ < 1.0E-4D) {
                    dX = (Math.random() - Math.random()) * 0.01D;
                    dZ = (Math.random() - Math.random()) * 0.01D;
                }

                this.hurtDir = (float) (Math.atan2(dZ, dX) * 180.0D / Math.PI) - this.yRot;
                this.knockback(entity, damage, dX, dZ);
            } else {
                this.hurtDir = (float) ((int) (Math.random() * 2.0D) * 180);
            }
        }

        if (this.health <= 0) {
            if (attacked) {
                this.level.playSound((Entity) (Object) this, this.getDeathSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }

            this.die(entity);
        } else if (attacked) {
            this.level.playSound((Entity) (Object) this, this.getHurtSound(), this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        }

        return true;
    }

    @Overwrite
    public void causeFallDamage(float var1) {
        if (!this.canGetFallDamage) {
            return;
        }

        if (this.getIsFlying()) {
            return;
        }

        float amount = Math.max(var1 - 0.8F, 0.0F) / 0.08F;
        int ceilAmount = (int) Math.ceil(Math.pow(amount, 1.5D));
        if (ceilAmount > 0) {
            this.hurt(null, ceilAmount);

            int id = this.level.getTile(
                Mth.floor(this.x),
                Mth.floor(this.y - 0.2 - this.heightOffset),
                Mth.floor(this.z));

            if (id > 0) {
                SoundType block = Tile.tiles[id].soundType;
                this.level.playSound((Entity) (Object) this, block.getStepSound(), block.getVolume() * 0.5F, block.getPitch() * (12.0F / 16.0F));
            }
        }
    }

    @Overwrite
    public void travel(float xInput, float zInput) {
        if (this.getIsFlying()) {
            double speed = Math.sqrt(xInput * xInput + zInput * zInput);
            double yVel = (double) (-0.1F * zInput) * Math.sin(this.xRot * Math.PI / 180.0);
            if (speed < 1.0D) {
                yVel *= speed;
            }

            this.yd += yVel;
            float inputSpeed = (float) (0.1 * (Math.abs(zInput * Math.cos(this.xRot * Math.PI / 180.0)) + Math.abs(xInput)));
            this.moveRelative(xInput, zInput, inputSpeed);
            this.move(this.xd, this.yd, this.zd);
            this.fallDistance = 0.0F;
            this.xd *= 0.8D;
            this.yd *= 0.8D;
            this.zd *= 0.8D;
            if (Math.abs(this.xd) < 0.01D) {
                this.xd = 0.0D;
            }

            if (Math.abs(this.yd) < 0.01D) {
                this.yd = 0.0D;
            }

            if (Math.abs(this.zd) < 0.01D) {
                this.zd = 0.0D;
            }
        } else if (this.isInWater()) {
            if (this.yd < -0.4D) {
                this.yd *= 0.8D;
            }

            double lastY = this.y;
            this.moveRelative(xInput, zInput, 0.02F);
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.8F;
            this.yd *= 0.8F;
            this.zd *= 0.8F;
            this.yd -= 0.25D * this.getGravity();
            if (this.horizontalCollision && this.isFree(this.xd, this.yd + (double) 0.6F - this.y + lastY, this.zd)) {
                this.yd = 0.3F;
            }
        } else if (this.isInLava()) {
            if (this.yd < -0.4D) {
                this.yd *= 0.5D;
            }

            double lastY = this.y;
            this.moveRelative(xInput, zInput, 0.02F);
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.5D;
            this.yd *= 0.5D;
            this.zd *= 0.5D;
            this.yd -= 0.25D * this.getGravity();
            if (this.horizontalCollision && this.isFree(this.xd, this.yd + (double) 0.6F - this.y + lastY, this.zd)) {
                this.yd = 0.3F;
            }
        } else {
            float slipperiness = 0.91F;
            if (this.onGround) {
                slipperiness = 0.5460001F;
                int id = this.level.getTile(Mth.floor(this.x), Mth.floor(this.bb.y0) - 1, Mth.floor(this.z));
                if (id > 0) {
                    slipperiness = Tile.tiles[id].friction * 0.91F;
                }
            }

            float inputFactor = 0.1627714F / (slipperiness * slipperiness * slipperiness);
            this.moveRelative(xInput, zInput, this.onGround ? 0.1F * inputFactor : 0.1F * this.airControl * inputFactor);
            slipperiness = 0.91F;
            if (this.onGround) {
                slipperiness = 0.5460001F;
                int id = this.level.getTile(Mth.floor(this.x), Mth.floor(this.bb.y0) - 1, Mth.floor(this.z));
                if (id > 0) {
                    slipperiness = Tile.tiles[id].friction * 0.91F;
                }
            }

            if (this.onLadder()) {
                double deceleration = 0.15F;
                if (this.xd < -deceleration) {
                    this.xd = -deceleration;
                }

                if (this.xd > deceleration) {
                    this.xd = deceleration;
                }

                if (this.zd < -deceleration) {
                    this.zd = -deceleration;
                }

                if (this.zd > deceleration) {
                    this.zd = deceleration;
                }

                this.fallDistance = 0.0F;
                if (this.yd < -0.15D) {
                    this.yd = -0.15D;
                }

                if (this.isSneaking() && this.yd < 0.0D) {
                    this.yd = 0.0D;
                }
            }

            this.move(this.xd, this.yd, this.zd);
            if ((this.horizontalCollision || this.jumping) && this.onLadder()) {
                this.yd = 0.2D;
            }

            this.yd -= this.getGravity();
            this.yd *= 0.98F;
            this.xd *= slipperiness;
            this.zd *= slipperiness;
        }

        this.walkAnimSpeedO = this.walkAnimSpeed;
        double dX = this.x - this.xo;
        double dZ = this.z - this.zo;
        float limbChange = (float) Math.min(Math.sqrt(dX * dX + dZ * dZ) * 4.0D, 1.0D);
        this.walkAnimSpeed += (limbChange - this.walkAnimSpeed) * 0.4F;
        this.walkAnimPos += this.walkAnimSpeed;
    }

    @Overwrite
    public boolean onLadder() {
        int x = Mth.floor(this.x);
        int y = Mth.floor(this.bb.y0);
        int z = Mth.floor(this.z);
        if (isClimbable(this.level, x, y, z)) {
            return true;
        }
        if (isClimbable(this.level, x, y + 1, z)) {
            return true;
        }
        return false;
    }

    private static boolean isClimbable(LevelSource world, int x, int y, int z) {
        int id = world.getTile(x, y, z);
        if (ExLadderBlock.isLadderID(id)) {
            return true;
        }

        if (id == AC_Blocks.ropes1.id ||
            id == AC_Blocks.ropes2.id ||
            id == AC_Blocks.chains.id) {

            boolean meta = world.getData(x, y, z) % 3 == 0;
            if (meta) {
                return true;
            }
        }
        return false;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    protected void ac$addAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        tag.putShort("MaxHealth", (short) this.maxHealth);
        tag.putInt("EntityID", this.id);
        tag.putInt("timesCanJumpInAir", this.timesCanJumpInAir);
        tag.putBoolean("canWallJump", this.canWallJump);
        tag.putFloat("fov", this.fov);
        tag.putBoolean("canLookRandomly", this.canLookRandomly);
        tag.putFloat("randomLookVelocity", this.randomLookVelocity);
        tag.putInt("randomLookRate", this.randomLookRate);
        tag.putInt("randomLookRateVariation", this.randomLookRateVariation);
        tag.putBoolean("canGetFallDamage", this.canGetFallDamage);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void ac$readAdditionalSaveData(CompoundTag tag, CallbackInfo ci) {
        var exTag = (ExCompoundTag) tag;
        this.maxHealth = exTag.findShort("MaxHealth").orElse((short) 10);

        //noinspection ConstantValue
        exTag.findInt("EntityID")
            .filter(id -> !((Object) this instanceof Player))
            .ifPresent(id -> this.id = id);

        this.timesCanJumpInAir = tag.getInt("timesCanJumpInAir");
        this.canWallJump = tag.getBoolean("canWallJump");
        exTag.findFloat("fov").ifPresent(this::setFov);

        exTag.findBool("canLookRandomly").ifPresent(this::setCanLookRandomly);
        exTag.findFloat("randomLookVelocity").ifPresent(this::setRandomLookVelocity);
        exTag.findInt("randomLookRate").ifPresent(this::setRandomLookRate);
        exTag.findInt("randomLookRateVariation").ifPresent(this::setRandomLookRateVariation);
        exTag.findBool("canGetFallDamage").ifPresent(this::setCanGetFallDamage);
    }

    @Inject(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;isInLava()Z",
            shift = At.Shift.AFTER))
    private void fixupYaw(CallbackInfo ci) {
        if (this.onGround) {
            this.jumpsLeft = this.timesCanJumpInAir;
        }

        if (this.moveYawOffset != 0.0F) {
            if (this.moveYawOffset > 40.0F) {
                this.moveYawOffset -= 40.0F;
                this.yRot += 40.0F;
            } else if (this.moveYawOffset < -40.0F) {
                this.moveYawOffset += 40.0F;
                this.yRot -= 40.0F;
            } else {
                this.yRot += this.moveYawOffset;
                this.moveYawOffset = 0.0F;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Inject(
        method = "aiStep",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;isInLava()Z",
            shift = At.Shift.AFTER))
    private void doWallJump(CallbackInfo ci) {
        if (!this.jumping) {
        } else if (this.onGround) {
        } else if (this.isInWater()) {
        } else if (this.isInLava()) {
        } else if (this.level.getTime() >= this.tickBeforeNextJump) {
            if (this.canWallJump && (this.collisionX != 0 || this.collisionZ != 0)) {
                this.jumpFromGround();
                this.yd *= this.jumpWallMultiplier;
                this.xd += (double) (-this.collisionX) * 0.325D;
                this.zd += (double) (-this.collisionZ) * 0.325D;

                this.moveYawOffset = (float) (180.0D * Math.atan2(-this.xd, this.zd) / Math.PI) - this.yRot;
                while ((double) this.moveYawOffset >= 180.0D) {
                    this.moveYawOffset = (float) ((double) this.moveYawOffset - 360.0D);
                }
                while ((double) this.moveYawOffset < -180.0D) {
                    this.moveYawOffset = (float) ((double) this.moveYawOffset + 360.0D);
                }

                for (int i = 0; i < 10; ++i) {
                    this.level.addParticle("reddust", this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth, this.y - 0.2, this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth, 2.5D, 2.5D, 2.5D);
                }
            } else if (this.jumpsLeft > 0) {
                --this.jumpsLeft;
                this.jumpFromGround();
                this.yd *= this.jumpInAirMultiplier;

                for (int i = 0; i < 10; ++i) {
                    this.level.addParticle("reddust", this.x + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth, this.y - 0.2, this.z + (this.random.nextFloat() * this.bbWidth * 2.0F) - this.bbWidth, 2.5D, 2.5D, 2.5D);
                }
            }
        }
    }

    @Overwrite
    public void jumpFromGround() {
        this.tickBeforeNextJump = this.level.getTime() + 5L;
        this.yd = this.jumpVelocity;
    }

    @Overwrite
    public void serverAiStep() {
        ++this.noActionTime;

        this.checkDespawn();
        this.xxa = 0.0F;
        this.zza = 0.0F;
        double searchDist = 8.0F;
        if (this.random.nextFloat() < 0.02F) {
            Player player = this.level.getNearestPlayer((Entity) (Object) this, searchDist);
            if (player != null && this.canSee(player)) {
                this.lookAt = player;
                this.lookTime = 10 + this.random.nextInt(20);
            }
        }

        if (this.lookAt != null) {
            this.setLookAt(this.lookAt, 10.0F, (float) this.getMaxHeadXRot());
            if (this.lookTime-- <= 0 || this.lookAt.removed || this.lookAt.distanceToSqr((Entity) (Object) this) > (searchDist * searchDist)) {
                this.lookAt = null;
            }
        } else if (this.canLookRandomly) {
            if (this.randomLookNext-- <= 0) {
                float rngLook = this.random.nextFloat();
                if (rngLook < 0.5F) {
                    this.yRotA = -this.randomLookVelocity * (rngLook + 0.5F);
                } else {
                    this.yRotA = this.randomLookVelocity * rngLook;
                }

                int extra = this.randomLookRateVariation > 0 ? this.random.nextInt(this.randomLookRateVariation) : 0;
                this.randomLookNext = this.randomLookRate + extra;
            }

            this.yRot += this.yRotA;
            this.xRot = this.defaultLookAngle;
            this.yRotA *= 0.95F;
            if (Math.abs(this.yRotA) < 1.0F) {
                this.yRotA = 0.0F;
            }
        }

        boolean var6 = this.isInWater();
        boolean var4 = this.isInLava();
        if (var6 || var4) {
            this.jumping = this.random.nextFloat() < 0.8F;
        }
    }

    @Inject(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Mob;dropDeathLoot()V",
            shift = At.Shift.AFTER))
    private void dropHeartsOnDeath(Entity killer, CallbackInfo ci) {
        if (killer instanceof Mob livingKiller) {
            if (livingKiller.health < ((ExMob) livingKiller).getMaxHealth() && this.random.nextInt(3) != 0) {
                var instance = new ItemInstance(AC_Items.heart.id, 1, 0);
                var itemEntity = new ItemEntity(this.level, this.x, this.y, this.z, instance);
                this.level.addEntity(itemEntity);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public ItemInstance getCarriedItem() {
        return this.ac$heldItem;
    }

    @Overwrite
    public Vec3 getViewVector(float deltaTime) {
        return super.getRotation(deltaTime);
    }

    @Override
    public boolean protectedByShield() {
        return false;
    }

    @Override
    public boolean protectedByShield(double x, double y, double z) {
        if (!this.protectedByShield() || !(this.getAttackAnim(1.0F) <= 0.0F)) {
            return false;
        }

        double dX = this.x - x;
        double dZ = this.z - z;
        float shieldYaw = (float) (-57.29578D * Math.atan2(dX, dZ) + 180.0D);

        float protectYaw = Math.abs(shieldYaw - this.yRot);
        while (protectYaw > 180.0F) {
            protectYaw -= 360.0F;
        }

        while (protectYaw < -180.0F) {
            protectYaw += 360.0F;
        }

        return protectYaw < 50.0F;
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
    public ItemInstance getSelectedItem() {
        return this.ac$heldItem;
    }

    @Override
    public void setHeldItem(ItemInstance value) {
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
        this.textureName = value;
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
        return runSpeed;
    }

    @Override
    public void setMovementSpeed(float value) {
        this.runSpeed = value;
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
        // TODO: throw on "value <= 0"
        this.randomLookRateVariation = value;
    }

    @Override
    public void setCanGetFallDamage(boolean arg) {
        this.canGetFallDamage = arg;
    }

    @Override
    public boolean getCanGetFallDamage() {
        return this.canGetFallDamage;
    }
}
