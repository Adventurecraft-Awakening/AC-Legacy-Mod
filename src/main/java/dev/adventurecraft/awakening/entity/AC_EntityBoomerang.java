package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExEntity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;

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

    public AC_EntityBoomerang(Level world) {
        super(world);
        this.setSize(0.5F, 1.0F / 16.0F);
        this.heightOffset = STANDING_EYE_HEIGHT;

        ExEntity entity = (ExEntity)this;
        entity.setCollidesWithClipBlocks(true);
        entity.setIgnoreCobwebCollision(true);
    }

    public AC_EntityBoomerang(Level world, Entity returnsTo) {
        this(world);
        this.returnsTo = returnsTo;
        //this.itemStack = stack;

        this.ticksBeforeTurnAround = 30;
        this.turningAround = false;

        //Entity positions/rotations
        this.setRot(returnsTo.yRot, returnsTo.xRot);
        double xVel = -Math.sin(returnsTo.yRot * Math.PI / 180.0D);
        double zVel = Math.cos(returnsTo.yRot * Math.PI / 180.0D);
        this.xd = VELOCITY_SPEED * xVel * Math.cos(returnsTo.xRot / 180.0D * Math.PI);
        this.yd = -VELOCITY_SPEED * Math.sin(returnsTo.xRot / 180.0D * Math.PI);
        this.zd = VELOCITY_SPEED * zVel * Math.cos(returnsTo.xRot / 180.0D * Math.PI);
        this.setPos(returnsTo.x, returnsTo.y, returnsTo.z);
        this.determineRotation();

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;
        if (!this.turningAround) {
            double velX = this.xd;
            double velY = this.yd;
            double velZ = this.zd;

            this.move(this.xd, this.yd, this.zd);

            boolean bounced = false;
            if (this.xd != velX) {
                this.xd = -velX;
                bounced = true;
            }

            if (this.yd != velY) {
                this.yd = -velY;
                bounced = true;
            }

            if (this.zd != velZ) {
                this.zd = -velZ;
                bounced = true;
            }

            if (bounced) {
                this.xd *= BOUNCE_FACTOR;
                this.yd *= BOUNCE_FACTOR;
                this.zd *= BOUNCE_FACTOR;
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

            this.xd = VELOCITY_SPEED * rX / dist;
            this.yd = VELOCITY_SPEED * rY / dist;
            this.zd = VELOCITY_SPEED * rZ / dist;
            this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
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

        List<Entity> entities = this.level.getEntities(this, this.bb.inflate(0.5D, 0.5D, 0.5D));
        for (Entity entity : entities) {
            if (entity instanceof ItemEntity itemEntity) {
                if(!this.itemsPickedUp.contains(itemEntity)) {
                    this.itemsPickedUp.add(itemEntity);
                }
                continue;
            }
            if (entity instanceof Mob && entity != this.returnsTo) {
                ((ExEntity) entity).setStunned(20);
                entity.xo = entity.x;
                entity.yo = entity.y;
                entity.zo = entity.z;
                entity.yRotO = entity.yRot;
                entity.xRotO = entity.xRot;
            }
        }

        for (Entity entity : this.itemsPickedUp) {
            if (!entity.removed) {
                entity.setPos(this.x, this.y, this.z);
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
            int id = this.level.getTile(this.blockX, this.blockY, this.blockZ);
            if (id == Tile.LEVER.id && this.returnsTo instanceof Player player) {
                Tile.LEVER.use(this.level, this.blockX, this.blockY, this.blockZ, player);
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        if(this.returnsTo == null){
            return;
        }
        if(!(this.returnsTo instanceof Player)){
            return;
        }
        for (Player player : (List<Player>) level.players) {
            Inventory playerInventory = player.inventory;
            //checks if the player still holds the item on the cursor
            if(playerInventory.getCarried() != null){
                ItemInstance cursorItemStack = playerInventory.getCarried();
                if(setBoomerangMeta(cursorItemStack)){
                    break;
                }
            }

            //searching if the boomerang is still somewhere in the inventory
            for(ItemInstance itemStacks : playerInventory.items){
                if(itemStacks == null){
                    continue;
                }
                if(setBoomerangMeta(itemStacks)){
                    break;
                }
            }
        }
        List<Entity> entities = this.level.getAllEntities();
        for (Entity entity : entities) {
            if(entity == null){
                continue;
            }
            if(!(entity instanceof ItemEntity itemEntity)){
                continue;
            }
            if(itemEntity.item == null){
                continue;
            }
            setBoomerangMeta(itemEntity.item);
        }
    }

    private boolean setBoomerangMeta(ItemInstance itemStack){
        if(itemStack.id == 456 && itemStack.getAuxValue() > 0){
            itemStack.setDamage(0);
            return true;
        }
        return false;
    }

    private void determineRotation() {
        this.yRot = (float) (-57.29578D * Math.atan2(this.xd, this.zd));
        double speed = Math.sqrt(this.zd * this.zd + this.xd * this.xd);
        this.xRot = (float) (-57.29578D * Math.atan2(this.yd, speed));
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.remove();
    }

    @Override
    public void playerTouch(Player player) {
    }

    @Override
    public boolean hurt(Entity entity, int damage) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }
}
