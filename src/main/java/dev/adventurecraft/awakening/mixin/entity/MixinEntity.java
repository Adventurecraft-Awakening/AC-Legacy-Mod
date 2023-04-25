package dev.adventurecraft.awakening.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.AC_Blocks;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity implements ExEntity {

    @Shadow
    public double x;

    @Shadow
    public double y;

    @Shadow
    public float height;

    @Shadow
    public World world;

    @Shadow
    public float standingEyeHeight;

    @Shadow
    public double z;

    @Shadow
    public float field_1635;

    @Shadow
    public int field_1611;

    @Shadow
    public double yVelocity;

    @Shadow
    public float fallDistance;

    @Shadow
    public float yaw;

    @Shadow
    public double xVelocity;

    @Shadow
    public double zVelocity;

    @Shadow
    protected abstract void handleFallDamage(float f);

    @Shadow
    public abstract void accelerate(double d, double e, double f);

    @Shadow
    public abstract boolean damage(Entity arg, int i);

    public boolean isFlying;
    public int stunned;
    public boolean collidesWithClipBlocks = true;
    public int collisionX;
    public int collisionZ;
    public float moveYawOffset = 0.0F;

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
            @Local(name = "var5", print = true) double var5,
            @Local(name = "var11") double var11,
            @Local(name = "var13") double var13,
            @Local(name = "var15") double var15) {
        this.collisionX = Double.compare(var11, var1);

        int var22;
        boolean var38;
        int var39;
        if (this.collisionX != 0) {
            var38 = false;

            for (var22 = 0; (double) var22 < (double) this.height + this.y - (double) this.standingEyeHeight - Math.floor(this.y - (double) this.standingEyeHeight); ++var22) {
                var39 = this.world.getBlockId((int) Math.floor(this.x) + this.collisionX, (int) Math.floor(this.y + (double) var22 - (double) this.standingEyeHeight), (int) Math.floor(this.z));
                if (var39 != 0 && var39 != AC_Blocks.clipBlock.id) {
                    var38 = true;
                }
            }

            if (!var38) {
                this.collisionX = 0;
            }
        }

        this.collisionZ = Double.compare(var15, var5);

        if (this.collisionZ != 0) {
            var38 = false;

            for (var22 = 0; (double) var22 < (double) this.height + this.y - this.standingEyeHeight - Math.floor(this.y - (double) this.standingEyeHeight); ++var22) {
                var39 = this.world.getBlockId((int) Math.floor(this.x), (int) Math.floor(this.y + (double) var22 - (double) this.standingEyeHeight), (int) Math.floor(this.z) + this.collisionZ);
                if (var39 != 0 && var39 != AC_Blocks.clipBlock.id) {
                    var38 = true;
                }
            }

            if (!var38) {
                this.collisionZ = 0;
            }
        }
    }

    @Redirect(method = "move", at = @At(
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
    public void movementInputToVelocity(float var1, float var2, float var3) {
        float var4 = var1 * var1 + var2 * var2;
        if (var4 >= 1.0E-4F) {
            var1 *= var3;
            var2 *= var3;
            float var5 = MathHelper.sin((this.yaw + this.moveYawOffset) * (float) Math.PI / 180.0F);
            float var6 = MathHelper.cos((this.yaw + this.moveYawOffset) * (float) Math.PI / 180.0F);
            this.xVelocity += var1 * var6 - var2 * var5;
            this.zVelocity += var2 * var6 + var1 * var5;
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

    @Override
    public boolean attackEntityFromMulti(Entity var1, int var2) {
        return this.damage(var1, var2);
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
}
