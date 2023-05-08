package dev.adventurecraft.awakening.common;

import java.util.List;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AC_EntityHookshot extends Entity {

    int timeBeforeTurnAround;
    boolean turningAround;
    public boolean attachedToSurface;
    public boolean mainHand;
    LivingEntity returnsTo;
    Entity entityGrabbed;
    ItemStack item;

    public AC_EntityHookshot(World var1) {
        super(var1);
        this.setSize(0.5F, 0.5F);
        this.turningAround = true;
        this.timeBeforeTurnAround = 0;
        ((ExEntity) this).setCollidesWithClipBlocks(false);
    }

    public AC_EntityHookshot(World var1, LivingEntity var2, boolean var3, ItemStack var4) {
        this(var1);
        this.mainHand = var3;
        this.setRotation(var2.yaw, var2.pitch);
        double var5 = -MathHelper.sin(var2.yaw * 3.141593F / 180.0F);
        double var7 = MathHelper.cos(var2.yaw * 3.141593F / 180.0F);
        this.xVelocity = var5 * (double) MathHelper.cos(var2.pitch / 180.0F * 3.141593F);
        this.yVelocity = -MathHelper.sin(var2.pitch / 180.0F * 3.141593F);
        this.zVelocity = var7 * (double) MathHelper.cos(var2.pitch / 180.0F * 3.141593F);
        this.setHeading();
        this.setPosition(var2.x, var2.y, var2.z);
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.timeBeforeTurnAround = 20;
        this.turningAround = false;
        this.returnsTo = var2;
        this.attachedToSurface = false;
        this.item = var4;
    }

    public void setHeading() {
        float var1 = MathHelper.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
        this.prevYaw = this.yaw = (float) (Math.atan2(this.xVelocity, this.zVelocity) * 180.0D / (double) ((float) Math.PI));
        this.prevPitch = this.pitch = (float) (Math.atan2(this.yVelocity, var1) * 180.0D / (double) ((float) Math.PI));
    }

    public void setHeadingReverse() {
        float var1 = MathHelper.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
        this.prevYaw = this.yaw = (float) (Math.atan2(-this.xVelocity, -this.zVelocity) * 180.0D / (double) ((float) Math.PI));
        this.prevPitch = this.pitch = (float) (Math.atan2(-this.yVelocity, var1) * 180.0D / (double) ((float) Math.PI));
    }

    @Override
    public void tick() {
        if (this.item != null && this.returnsTo instanceof PlayerEntity) {
            PlayerEntity var1 = (PlayerEntity) this.returnsTo;
            if (this.mainHand && this.item != var1.inventory.getHeldItem()) {
                AC_Items.hookshot.releaseHookshot(this);
            }

            if (!this.mainHand && this.item != ((ExPlayerInventory) var1.inventory).getOffhandItemStack()) {
                AC_Items.hookshot.releaseHookshot(this);
            }
        }

        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        double var3;
        double var5;
        double var11;
        if (!this.turningAround) {
            var11 = this.xVelocity;
            var3 = this.yVelocity;
            var5 = this.zVelocity;
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            if (this.xVelocity == var11 && this.yVelocity == var3 && this.zVelocity == var5) {
                if (this.timeBeforeTurnAround-- <= 0) {
                    this.turningAround = true;
                }
            } else {
                Vec3d var7 = Vec3d.create(this.prevX, this.prevY, this.prevZ);
                Vec3d var8 = Vec3d.create(this.prevX + 10.0D * var11, this.prevY + 10.0D * var3, this.prevZ + 10.0D * var5);
                HitResult var9 = this.world.method_160(var7, var8);
                if (var9 != null && var9.type == HitType.field_789) {
                    int var10 = this.world.getBlockId(var9.x, var9.y, var9.z);
                    if (var10 != Block.LOG.id && var10 != Block.WOOD.id && var10 != AC_Blocks.woodBlocks.id && var10 != AC_Blocks.halfSteps3.id) {
                        if (var10 != 0) {
                            this.world.playSound(this, Block.BY_ID[var10].sounds.getWalkSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                        }
                    } else {
                        this.attachedToSurface = true;
                        this.setPosition(var9.field_1988.x, var9.field_1988.y, var9.field_1988.z);
                        this.world.playSound(this, "random.drr", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    }
                }

                this.turningAround = true;
            }
        } else if (this.returnsTo != null) {
            if (this.returnsTo.removed) {
                this.remove();
                return;
            }

            var11 = this.returnsTo.x - this.x;
            var3 = this.returnsTo.y - this.y;
            var5 = this.returnsTo.z - this.z;
            double var14 = Math.sqrt(var11 * var11 + var3 * var3 + var5 * var5);
            this.xVelocity = 0.75D * var11 / var14;
            this.yVelocity = 0.75D * var3 / var14;
            this.zVelocity = 0.75D * var5 / var14;
            if (this.attachedToSurface) {
                if (var14 > 1.2D) {
                    this.returnsTo.accelerate(-0.15D * this.xVelocity, -0.15D * this.yVelocity, -0.15D * this.zVelocity);
                    this.returnsTo.fallDistance = 0.0F;
                } else {
                    this.returnsTo.setVelocity(0.0D, 0.0D, 0.0D);
                }
            } else {
                if (var14 <= 1.2D) {
                    this.remove();
                }

                this.setPosition(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
                this.setHeadingReverse();
            }
        } else {
            this.remove();
        }

        if (!this.turningAround) {
            List<Entity> var12 = (List<Entity>) this.world.getEntities(this, this.boundingBox.expand(0.5D, 0.5D, 0.5D));

            for (Entity var13 : var12) {
                boolean var4 = var13 instanceof ItemEntity;
                if (var4 || var13 instanceof LivingEntity && var13 != this.returnsTo) {
                    if (var4) {
                        this.world.playSound(this, "damage.fallsmall", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    } else {
                        this.world.playSound(this, "damage.hurtflesh", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    }

                    this.entityGrabbed = var13;
                    this.turningAround = true;
                    break;
                }
            }
        }

        if (this.entityGrabbed != null && !this.entityGrabbed.removed) {
            this.entityGrabbed.fallDistance = 0.0F;
            this.entityGrabbed.setPosition(this.x, this.y, this.z);
        }
    }

    @Override
    protected void writeAdditional(CompoundTag var1) {
    }

    @Override
    public void readAdditional(CompoundTag var1) {
        this.remove();
    }

    @Override
    public void onPlayerCollision(PlayerEntity var1) {
    }

    @Override
    public boolean damage(Entity var1, int var2) {
        return false;
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void remove() {
        if (this.item != null) {
            this.item.setMeta(0);
        }

        super.remove();
    }
}
