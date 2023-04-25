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

    public void tick() {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        double var1;
        double var3;
        double var5;
        if (!this.turningAround) {
            var1 = this.xVelocity;
            var3 = this.yVelocity;
            var5 = this.zVelocity;
            this.move(this.xVelocity, this.yVelocity, this.zVelocity);
            boolean var7 = false;
            if (this.xVelocity != var1) {
                this.xVelocity = -var1;
                var7 = true;
            }

            if (this.yVelocity != var3) {
                this.yVelocity = -var3;
                var7 = true;
            }

            if (this.zVelocity != var5) {
                this.zVelocity = -var5;
                var7 = true;
            }

            if (var7) {
                this.xVelocity *= this.bounceFactor;
                this.yVelocity *= this.bounceFactor;
                this.zVelocity *= this.bounceFactor;
            }

            if (this.timeBeforeTurnAround-- <= 0) {
                this.turningAround = true;
            }
        } else if (this.returnsTo != null) {
            var1 = this.returnsTo.x - this.x;
            var3 = this.returnsTo.y - this.y;
            var5 = this.returnsTo.z - this.z;
            double var14 = Math.sqrt(var1 * var1 + var3 * var3 + var5 * var5);
            if (var14 < 1.5D) {
                this.remove();
            }

            this.xVelocity = 0.5D * var1 / var14;
            this.yVelocity = 0.5D * var3 / var14;
            this.zVelocity = 0.5D * var5 / var14;
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

        List<Entity> var9 = this.world.getEntities(this, this.boundingBox.expand(0.5D, 0.5D, 0.5D));

        int var2;
        for (var2 = 0; var2 < var9.size(); ++var2) {
            Entity var11 = var9.get(var2);
            if (var11 instanceof ItemEntity ie) {
                this.itemsPickedUp.add(ie);
            } else if (var11 instanceof LivingEntity && var11 != this.returnsTo) {
                ((ExEntity) var11).setStunned(20);
                var11.prevX = var11.x;
                var11.prevY = var11.y;
                var11.prevZ = var11.z;
                var11.prevYaw = var11.yaw;
                var11.prevPitch = var11.pitch;
            }
        }

        for (Entity var11 : this.itemsPickedUp) {
            if (!var11.removed) {
                var11.setPosition(this.x, this.y, this.z);
            }
        }

        int cX = (int) Math.floor(this.x);
        int cY = (int) Math.floor(this.y);
        int cZ = (int) Math.floor(this.z);
        if (cX != this.chunkX || cY != this.chunkY || cZ != this.chunkZ) {
            this.chunkX = cX;
            this.chunkY = cY;
            this.chunkZ = cZ;
            int var13 = this.world.getBlockId(this.chunkX, this.chunkY, this.chunkZ);
            if (var13 == Block.LEVER.id && this.returnsTo instanceof PlayerEntity) {
                Block.LEVER.canUse(this.world, this.chunkX, this.chunkY, this.chunkZ, (PlayerEntity) this.returnsTo);
            }
        }

    }

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

    public void readAdditional(CompoundTag var1) {
        this.remove();
    }

    public void onPlayerCollision(PlayerEntity var1) {
    }

    public boolean damage(Entity var1, int var2) {
        return false;
    }

    protected void initDataTracker() {
    }
}
