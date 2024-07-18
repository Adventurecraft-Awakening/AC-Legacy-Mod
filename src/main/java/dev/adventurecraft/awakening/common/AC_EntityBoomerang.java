package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.mixin.entity.MixinEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AC_EntityBoomerang extends Entity {

    private static final double VELOCITY_SPEED = 0.5D;
    private static final double BOUNCE_FACTOR = 0.85D;
    private static final float STANDING_EYE_HEIGHT = 0.03125F;
    private static final double DISTANCE_MAX = 35.0D;
    private static final double DISTANCE_MIN = 1.5D;
    private final ArrayList<ItemEntity> itemsPickedUp = new ArrayList<>();
    private boolean turningAround = true;
    private Entity returnsTo = null;
    private int ticksBeforeTurnAround = 0;
    private int blockX = 0;
    private int blockY = 0;
    private int blockZ = 0;
    public float prevBoomerangRotation;
    public float boomerangRotation = 0.0F;

    public AC_EntityBoomerang(World world) {
        super(world);
        this.setSize(0.5F, 1.0F / 16.0F);
        this.standingEyeHeight = STANDING_EYE_HEIGHT;

        ExEntity entity = (ExEntity)this;
        entity.setCollidesWithClipBlocks(true);
        entity.setIgnoreCobwebCollision(true);
    }

    public AC_EntityBoomerang(World world, Entity returnsTo) {
        this(world);
        this.returnsTo = returnsTo;
        //this.itemStack = stack;

        this.ticksBeforeTurnAround = 30;
        this.turningAround = false;

        //Entity positions/rotations
        this.setRotation(returnsTo.yaw, returnsTo.pitch);
        double xVel = -Math.sin(returnsTo.yaw * Math.PI / 180.0D);
        double zVel = Math.cos(returnsTo.yaw * Math.PI / 180.0D);
        this.xVelocity = VELOCITY_SPEED * xVel * Math.cos(returnsTo.pitch / 180.0D * Math.PI);
        this.yVelocity = -VELOCITY_SPEED * Math.sin(returnsTo.pitch / 180.0D * Math.PI);
        this.zVelocity = VELOCITY_SPEED * zVel * Math.cos(returnsTo.pitch / 180.0D * Math.PI);
        this.setPosition(returnsTo.x, returnsTo.y, returnsTo.z);
        this.determineRotation();

        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
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
                this.xVelocity *= BOUNCE_FACTOR;
                this.yVelocity *= BOUNCE_FACTOR;
                this.zVelocity *= BOUNCE_FACTOR;
            }

            if (this.ticksBeforeTurnAround-- <= 0) {
                this.turningAround = true;
            }
        } else if (this.returnsTo != null) {
            double rX = this.returnsTo.x - this.x;
            double rY = this.returnsTo.y - this.y;
            double rZ = this.returnsTo.z - this.z;
            double dist = Math.sqrt(rX * rX + rY * rY + rZ * rZ);

            if (dist < DISTANCE_MIN || dist > DISTANCE_MAX) {
                this.remove();
                return;
            }

            this.xVelocity = VELOCITY_SPEED * rX / dist;
            this.yVelocity = VELOCITY_SPEED * rY / dist;
            this.zVelocity = VELOCITY_SPEED * rZ / dist;
            this.setPosition(this.x + this.xVelocity, this.y + this.yVelocity, this.z + this.zVelocity);
        } else {
            this.remove();
            return;
        }

        this.determineRotation();

        this.boomerangRotation += 36.0F;
        if(this.boomerangRotation >= 360.0F){
            this.boomerangRotation -= 360.0F;
        }
        this.prevBoomerangRotation = this.boomerangRotation;

        List<Entity> entities = this.world.getEntities(this, this.boundingBox.expand(0.5D, 0.5D, 0.5D));
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity itemEntity) {
                if(!this.itemsPickedUp.contains(itemEntity)) {
                    this.itemsPickedUp.add(itemEntity);
                }
                continue;
            }
            if (entity instanceof LivingEntity && entity != this.returnsTo) {
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

        int bX = (int) Math.floor(this.x);
        int bY = (int) Math.floor(this.y);
        int bZ = (int) Math.floor(this.z);
        if (bX != this.blockX ||
            bY != this.blockY ||
            bZ != this.blockZ) {
            this.blockX = bX;
            this.blockY = bY;
            this.blockZ = bZ;
            int id = this.world.getBlockId(this.blockX, this.blockY, this.blockZ);
            if (id == Block.LEVER.id && this.returnsTo instanceof PlayerEntity player) {
                Block.LEVER.canUse(this.world, this.blockX, this.blockY, this.blockZ, player);
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        if(this.returnsTo == null){
            return;
        }
        if(!(this.returnsTo instanceof PlayerEntity)){
            return;
        }
        for (PlayerEntity player : (List<PlayerEntity>) world.players) {
            PlayerInventory playerInventory = player.inventory;
            //checks if the player still holds the item on the cursor
            if(playerInventory.getCursorItem() != null){
                ItemStack cursorItemStack = playerInventory.getCursorItem();
                if(setBoomerangMeta(cursorItemStack)){
                    break;
                }
            }

            //searching if the boomerang is still somewhere in the inventory
            for(ItemStack itemStacks : playerInventory.main){
                if(itemStacks == null){
                    continue;
                }
                if(setBoomerangMeta(itemStacks)){
                    break;
                }
            }
        }
        List<Entity> entities = this.world.getEntities();
        for (Entity entity : entities) {
            if(entity == null){
                continue;
            }
            if(!(entity instanceof ItemEntity itemEntity)){
                continue;
            }
            if(itemEntity.stack == null){
                continue;
            }
            setBoomerangMeta(itemEntity.stack);
        }
    }

    private boolean setBoomerangMeta(ItemStack itemStack){
        if(itemStack.itemId == 456 && itemStack.getMeta() > 0){
            itemStack.setMeta(0);
            return true;
        }
        return false;
    }

    private void determineRotation() {
        this.yaw = (float) (-57.29578D * Math.atan2(this.xVelocity, this.zVelocity));
        double speed = Math.sqrt(this.zVelocity * this.zVelocity + this.xVelocity * this.xVelocity);
        this.pitch = (float) (-57.29578D * Math.atan2(this.yVelocity, speed));
    }

    protected void writeAdditional(CompoundTag tag) {
    }

    @Override
    public void readAdditional(CompoundTag tag) {
        this.remove();
    }

    @Override
    public void onPlayerCollision(PlayerEntity player) {
    }

    @Override
    public boolean damage(Entity entity, int damage) {
        return false;
    }

    @Override
    protected void initDataTracker() {
    }
}
