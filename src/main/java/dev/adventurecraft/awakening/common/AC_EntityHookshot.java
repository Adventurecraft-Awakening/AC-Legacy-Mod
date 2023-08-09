package dev.adventurecraft.awakening.common;

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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class AC_EntityHookshot extends Entity {

    int timeBeforeTurnAround;
    boolean turningAround;
    public boolean attachedToSurface;
    public boolean mainHand;
    LivingEntity returnsTo;
    Entity entityGrabbed;
    ItemStack itemStack;

    public AC_EntityHookshot(World world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.turningAround = true;
        this.timeBeforeTurnAround = 0;
        ((ExEntity) this).setCollidesWithClipBlocks(false);
    }

    public AC_EntityHookshot(World world, LivingEntity entity, boolean mainHand, ItemStack stack) {
        this(world);
        this.mainHand = mainHand;
        this.setRotation(entity.yaw, entity.pitch);
        double xVel = -Math.sin(entity.yaw * Math.PI / 180.0D);
        double zVel = Math.cos(entity.yaw * Math.PI / 180.0D);
        this.xVelocity = xVel * Math.cos(entity.pitch / 180.0D * Math.PI);
        this.yVelocity = -Math.sin(entity.pitch / 180.0D * Math.PI);
        this.zVelocity = zVel * Math.cos(entity.pitch / 180.0D * Math.PI);
        this.setHeading();
        this.setPosition(entity.x, entity.y, entity.z);
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.timeBeforeTurnAround = 20;
        this.turningAround = false;
        this.returnsTo = entity;
        this.attachedToSurface = false;
        this.itemStack = stack;
    }

    public void setHeading() {
        double speed = Math.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
        this.prevYaw = this.yaw = (float) (Math.atan2(this.xVelocity, this.zVelocity) * 180.0D / Math.PI);
        this.prevPitch = this.pitch = (float) (Math.atan2(this.yVelocity, speed) * 180.0D / Math.PI);
    }

    public void setHeadingReverse() {
        double speed = Math.sqrt(this.xVelocity * this.xVelocity + this.zVelocity * this.zVelocity);
        this.prevYaw = this.yaw = (float) (Math.atan2(-this.xVelocity, -this.zVelocity) * 180.0D / Math.PI);
        this.prevPitch = this.pitch = (float) (Math.atan2(-this.yVelocity, speed) * 180.0D / Math.PI);
    }

    @Override
    public void tick() {
        if (this.itemStack != null && this.returnsTo instanceof PlayerEntity player) {
            if (this.mainHand && this.itemStack != player.inventory.getHeldItem()) {
                AC_Items.hookshot.releaseHookshot(this);
            }

            if (!this.mainHand && this.itemStack != ((ExPlayerInventory) player.inventory).getOffhandItemStack()) {
                AC_Items.hookshot.releaseHookshot(this);
            }
        }

        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;

        double xVel;
        double yVel;
        double zVel;
        if (!this.turningAround) {
            xVel = this.xVelocity;
            yVel = this.yVelocity;
            zVel = this.zVelocity;
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            if (this.xVelocity == xVel && this.yVelocity == yVel && this.zVelocity == zVel) {
                if (this.timeBeforeTurnAround-- <= 0) {
                    this.turningAround = true;
                }
            } else {
                Vec3d prevPos = Vec3d.create(this.prevX, this.prevY, this.prevZ);
                Vec3d nextPos = Vec3d.create(this.prevX + 10.0D * xVel, this.prevY + 10.0D * yVel, this.prevZ + 10.0D * zVel);
                HitResult hit = this.world.method_160(prevPos, nextPos);
                if (hit != null && hit.type == HitType.field_789) {
                    int id = this.world.getBlockId(hit.x, hit.y, hit.z);
                    if (id != Block.LOG.id && id != Block.WOOD.id && id != AC_Blocks.woodBlocks.id && id != AC_Blocks.halfSteps3.id) {
                        if (id != 0) {
                            this.world.playSound(this, Block.BY_ID[id].sounds.getWalkSound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                        }
                    } else {
                        this.attachedToSurface = true;
                        this.setPosition(hit.field_1988.x, hit.field_1988.y, hit.field_1988.z);
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

            xVel = this.returnsTo.x - this.x;
            yVel = this.returnsTo.y - this.y;
            zVel = this.returnsTo.z - this.z;
            double speedSqr = Math.sqrt(xVel * xVel + yVel * yVel + zVel * zVel);
            this.xVelocity = 0.75D * xVel / speedSqr;
            this.yVelocity = 0.75D * yVel / speedSqr;
            this.zVelocity = 0.75D * zVel / speedSqr;
            if (this.attachedToSurface) {
                if (speedSqr > 1.2D) {
                    this.returnsTo.accelerate(-0.15D * this.xVelocity, -0.15D * this.yVelocity, -0.15D * this.zVelocity);
                    this.returnsTo.fallDistance = 0.0F;
                } else {
                    this.returnsTo.setVelocity(0.0D, 0.0D, 0.0D);
                }
            } else {
                if (speedSqr <= 1.2D) {
                    this.remove();
                }

                this.setPosition(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
                this.setHeadingReverse();
            }
        } else {
            this.remove();
        }

        if (!this.turningAround) {
            var entities = (List<Entity>) this.world.getEntities(this, this.boundingBox.expand(0.5D, 0.5D, 0.5D));
            for (Entity entity : entities) {
                boolean isItem = entity instanceof ItemEntity;
                if (isItem || entity instanceof LivingEntity && entity != this.returnsTo) {
                    if (isItem) {
                        this.world.playSound(this, "damage.fallsmall", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    } else {
                        this.world.playSound(this, "damage.hurtflesh", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
                    }

                    this.entityGrabbed = entity;
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
    protected void writeAdditional(CompoundTag tag) {
    }

    @Override
    public void readAdditional(CompoundTag tag) {
        this.remove();
    }

    @Override
    public void onPlayerCollision(PlayerEntity compound) {
    }

    @Override
    public boolean damage(Entity entity, int damage) {
        return false;
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void remove() {
        if (this.itemStack != null) {
            this.itemStack.setMeta(0);
        }

        super.remove();
    }
}
