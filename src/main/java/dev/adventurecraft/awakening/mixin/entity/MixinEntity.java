package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(Entity.class)
public abstract class MixinEntity implements ExEntity {

    @Shadow
    public int entityId;
    @Shadow
    public Entity passenger;
    @Shadow
    public Entity vehicle;
    @Shadow
    public World world;
    @Shadow
    public double prevX;
    @Shadow
    public double prevY;
    @Shadow
    public double prevZ;
    @Shadow
    public double x;
    @Shadow
    public double y;
    @Shadow
    public double z;
    @Shadow
    public double xVelocity;
    @Shadow
    public double yVelocity;
    @Shadow
    public double zVelocity;
    @Shadow
    public float yaw;
    @Shadow
    public float pitch;
    @Shadow
    public float prevYaw;
    @Shadow
    public float prevPitch;
    @Final
    @Shadow
    public AxixAlignedBoundingBox boundingBox;
    @Shadow
    public boolean onGround;
    @Shadow
    public boolean field_1624;
    @Shadow
    public boolean inCobweb;
    @Shadow
    public boolean removed;
    @Shadow
    public float standingEyeHeight;
    @Shadow
    public float width;
    @Shadow
    public float height;
    @Shadow
    public float field_1635;
    @Shadow
    public boolean field_1642;
    @Shadow
    public float fallDistance;
    @Shadow
    public int field_1611;
    @Shadow
    protected Random rand;
    @Shadow
    public int field_1646;
    @Shadow
    public int fireTicks;
    @Shadow
    public int field_1613;
    @Shadow
    public int air;

    public boolean ignoreCobwebCollision = false;
    public boolean isFlying;
    public int stunned;
    public boolean collidesWithClipBlocks = true;
    public int collisionX;
    public int collisionZ;
    public float moveYawOffset = 0.0F;

    @Shadow
    protected abstract void handleFallDamage(float f);

    @Shadow
    public abstract void accelerate(double d, double e, double f);

    @Shadow
    public abstract boolean damage(Entity arg, int i);

    @Shadow
    public void tick() {
        throw new AssertionError();
    }

    @Shadow
    public void remove() {
        throw new AssertionError();
    }

    @Shadow
    public abstract boolean method_1334();

    @Shadow
    public abstract void setPosition(double d, double e, double f);

    @Shadow
    protected abstract void setSize(float f, float g);

    @Shadow
    public abstract void move(double d, double e, double f);

    @Inject(method = "move", at = @At(value = "HEAD"))
    private void collideWithCobweb(CallbackInfo ci) {
        if (this.inCobweb && isIgnoreCobwebCollision()) {
            this.inCobweb = false;
        }
    }

    @Shadow
    public abstract ItemEntity dropItem(int i, int j);

    @Shadow
    public abstract boolean method_1335();

    @Shadow
    protected abstract void setAttacked();

    @Shadow
    public abstract boolean method_1344(double d, double e, double f);

    @Shadow
    public abstract float distanceTo(Entity arg);

    @Shadow
    public abstract boolean method_1373();

    @Inject(method = "move", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/entity/Entity;z:D",
        shift = At.Shift.AFTER,
        ordinal = 2))
    private void collideInMove(
        double var1,
        double var2,
        double var3,
        CallbackInfo ci,
        @Local(argsOnly = true, ordinal = 2) double var5,
        @Local(ordinal = 5) double var11,
        @Local(ordinal = 6) double var13,
        @Local(ordinal = 7) double var15) {
        this.collisionX = Double.compare(var11, var1);

        int yPos;
        boolean isCollidingWithBlock = false;
        int blockId;
        if (this.collisionX != 0) {
            for (yPos = 0; (double) yPos < (double) this.height + this.y - (double) this.standingEyeHeight - Math.floor(this.y - (double) this.standingEyeHeight); ++yPos) {
                blockId = this.world.getBlockId((int) Math.floor(this.x) + this.collisionX, (int) Math.floor(this.y + (double) yPos - (double) this.standingEyeHeight), (int) Math.floor(this.z));
                if (blockId != 0 && blockId != AC_Blocks.clipBlock.id) {
                    isCollidingWithBlock = true;
                    break;
                }
            }

            if (!isCollidingWithBlock) {
                this.collisionX = 0;
            }
        }

        this.collisionZ = Double.compare(var15, var5);

        if (this.collisionZ != 0) {
            isCollidingWithBlock = false;

            for (yPos = 0; (double) yPos < (double) this.height + this.y - this.standingEyeHeight - Math.floor(this.y - (double) this.standingEyeHeight); ++yPos) {
                blockId = this.world.getBlockId((int) Math.floor(this.x), (int) Math.floor(this.y + (double) yPos - (double) this.standingEyeHeight), (int) Math.floor(this.z) + this.collisionZ);
                if (blockId != 0 && blockId != AC_Blocks.clipBlock.id) {
                    isCollidingWithBlock = true;
                    break;
                }
            }

            if (!isCollidingWithBlock) {
                this.collisionZ = 0;
            }
        }
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;field_1611:I",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0))
    private void moveCeil(Entity instance, int value) {
        instance.field_1611 = (int) ((double) this.field_1611 + Math.ceil((this.field_1635 - (float) this.field_1611)));
    }

    @Overwrite
    public void method_1374(double var1, boolean var3) {
        if (var3) {
            if (this.yVelocity < 0.0D) {
                this.handleFallDamage(-((float) this.yVelocity));
            }
        } else if (var1 < 0.0D) {
            this.fallDistance = (float) ((double) this.fallDistance - var1);
        }
    }

    @Overwrite
    public void movementInputToVelocity(float x, float z, float speed) {
        float inputSqr = x * x + z * z;
        if (inputSqr >= 1.0E-4F) {
            x *= speed;
            z *= speed;
            double sin = Math.sin((this.yaw + this.moveYawOffset) * Math.PI / 180.0D);
            double cos = Math.cos((this.yaw + this.moveYawOffset) * Math.PI / 180.0D);
            this.xVelocity += x * cos - z * sin;
            this.zVelocity += z * cos + x * sin;
        }
    }

    @Redirect(method = "method_1353", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/entity/Entity;accelerate(DDD)V",
        ordinal = 1))
    private void reverseAccelerateDir(Entity instance, double var2, double var3, double var4) {
        if (instance.method_1380()) {
            instance.accelerate(var2, var3, var4);
        } else {
            this.accelerate(-var2, var3, -var4);
        }
    }

    @Redirect(method = "isInsideWall", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/World;canSuffocate(III)Z"))
    private boolean preventSuffocate(World instance, int x, int y, int z) {
        return instance.canSuffocate(x, y, z) && instance.method_1783(x, y, z);
    }

    @Overwrite
    public Vec3d getRotation() {
        return this.getRotation(1.0f);
    }

    @Override
    public Vec3d getRotation(float deltaTime) {
        double pitch = this.prevPitch + (this.pitch - this.prevPitch) * deltaTime;
        double yaw = this.prevYaw + (this.yaw - this.prevYaw) * deltaTime;
        double yCos = Math.cos(-yaw * (Math.PI / 180) - Math.PI);
        double ySin = Math.sin(-yaw * (Math.PI / 180) - Math.PI);
        double pCos = -Math.cos(-pitch * (Math.PI / 180));
        double pSin = Math.sin(-pitch * (Math.PI / 180));
        return Vec3d.from(ySin * pCos, pSin, yCos * pCos);
    }

    @Override
    public void setRotation(double x, double y, double z) {
        double root = Math.sqrt(x * x + z * z);
        double yDeg = (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0;
        double pDeg = -(Math.atan2(y, root) * 180.0 / Math.PI);
        this.prevPitch = this.pitch;
        this.prevYaw = this.yaw;
        this.pitch = (float) pDeg;
        this.yaw = (float) yDeg;
    }

    @Override
    public boolean attackEntityFromMulti(Entity entity, int damage) {
        return this.damage(entity, damage);
    }

    @Override
    public boolean handleFlying() {
        return this.isFlying;
    }

    @Override
    public void setIsFlying(boolean value) {
        this.isFlying = value;
    }

    @Override
    public boolean getCollidesWithClipBlocks() {
        return this.collidesWithClipBlocks;
    }

    @Override
    public void setCollidesWithClipBlocks(boolean value) {
        this.collidesWithClipBlocks = value;
    }

    @Override
    public int getStunned() {
        return this.stunned;
    }

    @Override
    public void setStunned(int value) {
        this.stunned = value;
    }

    @Override
    public int getCollisionX() {
        return this.collisionX;
    }

    @Override
    public int getCollisionZ() {
        return this.collisionZ;
    }

    @Override
    public void setIgnoreCobwebCollision(boolean value){
        this.ignoreCobwebCollision = value;
    }

    @Override
    public boolean isIgnoreCobwebCollision(){
        return ignoreCobwebCollision;
    }
}
