package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class AC_EntityBoomerang extends Entity {

    double bounceFactor;
    float prevBoomerangRotation;
    float boomerangRotation;
    int timeBeforeTurnAround;
    boolean turningAround;
    Entity returnsTo;
    ArrayList<ItemEntity> itemsPickedUp;
    ItemStack item;
    int chunkX;
    int chunkY;
    int chunkZ;

    public AC_EntityBoomerang(World var1) {
        super(var1);
        this.setSize(0.5F, 1.0F / 16.0F);
        this.standingEyeHeight = 0.03125F;
        this.bounceFactor = 0.85D;
        this.boomerangRotation = 0.0F;
        this.turningAround = true;
        this.timeBeforeTurnAround = 0;
        this.itemsPickedUp = new ArrayList<>();
        ((ExEntity) this).setCollidesWithClipBlocks(false);
    }

    public AC_EntityBoomerang(World var1, Entity var2, ItemStack var3) {
        this(var1);
        this.item = var3;
        this.setRotation(var2.yaw, var2.pitch);
        double var4 = -MathHelper.sin(var2.yaw * 3.141593F / 180.0F);
        double var6 = MathHelper.cos(var2.yaw * 3.141593F / 180.0F);
        this.xVelocity = 0.5D * var4 * (double) MathHelper.cos(var2.pitch / 180.0F * 3.141593F);
        this.yVelocity = -0.5D * (double) MathHelper.sin(var2.pitch / 180.0F * 3.141593F);
        this.zVelocity = 0.5D * var6 * (double) MathHelper.cos(var2.pitch / 180.0F * 3.141593F);
        this.setPosition(var2.x, var2.y, var2.z);
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.timeBeforeTurnAround = 30;
        this.turningAround = false;
        this.returnsTo = var2;
        this.chunkX = (int) Math.floor(this.x);
        this.chunkY = (int) Math.floor(this.y);
        this.chunkZ = (int) Math.floor(this.z);
    }

    @Override
    public void tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        if (!this.turningAround) {
            double velX = this.xVelocity;
            double velY = this.yVelocity;
            double velZ = this.zVelocity;
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);

            boolean bounced = false;
            if (this.xVelocity != velX) {
                this.xVelocity = -velX;
                bounced = true;
            }

            if (this.yVelocity != velY) {
                this.yVelocity = -velY;
                bounced = true;
            }

            if (this.zVelocity != velZ) {
                this.zVelocity = -velZ;
                bounced = true;
            }

            if (bounced) {
                this.xVelocity *= this.bounceFactor;
                this.yVelocity *= this.bounceFactor;
                this.zVelocity *= this.bounceFactor;
            }

            if (this.timeBeforeTurnAround-- <= 0) {
                this.turningAround = true;
            }
        } else if (this.returnsTo != null) {
            double rX = this.returnsTo.x - this.x;
            double rY = this.returnsTo.y - this.y;
            double rZ = this.returnsTo.z - this.z;
            double dist = Math.sqrt(rX * rX + rY * rY + rZ * rZ);
            if (dist < 1.5D) {
                this.remove();
            }

            this.xVelocity = 0.5D * rX / dist;
            this.yVelocity = 0.5D * rY / dist;
            this.zVelocity = 0.5D * rZ / dist;
            this.setPosition(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
        } else {
            this.remove();
        }

        this.determineRotation();
        this.prevBoomerangRotation = this.boomerangRotation;

        this.boomerangRotation += 36.0F;
        while (this.boomerangRotation > 360.0F) {
            this.boomerangRotation -= 360.0F;
        }

        List<Entity> entities = this.world.getEntities(this, this.boundingBox.expand(0.5D, 0.5D, 0.5D));
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity ie) {
                this.itemsPickedUp.add(ie);
            } else if (entity instanceof LivingEntity && entity != this.returnsTo) {
                ((ExEntity) entity).setStunned(20);
                entity.prevX = entity.x;
                entity.prevY = entity.y;
                entity.prevZ = entity.z;
                entity.prevYaw = entity.yaw;
                entity.prevPitch = entity.pitch;
            }
        }

        for (Entity entity : this.itemsPickedUp) {
            if (!entity.removed) {
                entity.setPosition(this.x, this.y, this.z);
            }
        }

        int cX = (int) Math.floor(this.x);
        int cY = (int) Math.floor(this.y);
        int cZ = (int) Math.floor(this.z);
        if (cX != this.chunkX || cY != this.chunkY || cZ != this.chunkZ) {
            this.chunkX = cX;
            this.chunkY = cY;
            this.chunkZ = cZ;
            int id = this.world.getBlockId(this.chunkX, this.chunkY, this.chunkZ);
            if (id == Block.LEVER.id && this.returnsTo instanceof PlayerEntity player) {
                Block.LEVER.canUse(this.world, this.chunkX, this.chunkY, this.chunkZ, player);
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (this.item != null) {
            this.item.setMeta(0);
        }
    }

    public void determineRotation() {
        this.yaw = -57.29578F * (float) Math.atan2(this.xVelocity, this.zVelocity);
        double var1 = Math.sqrt(this.zVelocity * this.zVelocity + this.xVelocity * this.xVelocity);
        this.pitch = -57.29578F * (float) Math.atan2(this.yVelocity, var1);
    }

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
}
