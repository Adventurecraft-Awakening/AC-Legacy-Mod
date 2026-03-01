package dev.adventurecraft.awakening.entity;

import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.inventory.ExPlayerInventory;
import java.util.List;

import dev.adventurecraft.awakening.item.AC_Items;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ItemInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;

public class AC_EntityHookshot extends Entity {

    int timeBeforeTurnAround;
    private boolean turningAround;
    public boolean attachedToSurface;
    public boolean mainHand;
    private Mob returnsTo;
    private Entity entityGrabbed;
    ItemInstance itemStack;

    public AC_EntityHookshot(Level world) {
        super(world);
        this.setSize(0.5F, 0.5F);
        this.setTurningAround(true);
        this.timeBeforeTurnAround = 0;
        ((ExEntity) this).setCollidesWithClipBlocks(false);
    }

    public AC_EntityHookshot(Level world, Mob entity, boolean mainHand, ItemInstance stack) {
        this(world);
        this.mainHand = mainHand;
        this.setRot(entity.yRot, entity.xRot);
        double xVel = -Math.sin(entity.yRot * Math.PI / 180.0D);
        double zVel = Math.cos(entity.yRot * Math.PI / 180.0D);
        this.xd = xVel * Math.cos(entity.xRot / 180.0D * Math.PI);
        this.yd = -Math.sin(entity.xRot / 180.0D * Math.PI);
        this.zd = zVel * Math.cos(entity.xRot / 180.0D * Math.PI);
        this.setHeading();
        this.setPos(entity.x, entity.y, entity.z);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.timeBeforeTurnAround = 20;
        this.setTurningAround(false);
        this.setReturnEntity(entity);
        this.attachedToSurface = false;
        this.itemStack = stack;
    }

    public void setHeading() {
        double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
        this.yRotO = this.yRot = (float) (Math.atan2(this.xd, this.zd) * 180.0D / Math.PI);
        this.xRotO = this.xRot = (float) (Math.atan2(this.yd, speed) * 180.0D / Math.PI);
    }

    public void setHeadingReverse() {
        double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
        this.yRotO = this.yRot = (float) (Math.atan2(-this.xd, -this.zd) * 180.0D / Math.PI);
        this.xRotO = this.xRot = (float) (Math.atan2(-this.yd, speed) * 180.0D / Math.PI);
    }

    @Override
    public void tick() {
        if (this.itemStack != null && this.getReturnEntity() instanceof Player player) {
            if (this.mainHand && this.itemStack != player.inventory.getSelected()) {
                AC_Items.hookshot.releaseHookshot(this);
            }

            if (!this.mainHand && this.itemStack != ((ExPlayerInventory) player.inventory).getOffhandItemStack()) {
                AC_Items.hookshot.releaseHookshot(this);
            }
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.yRotO = this.yRot;
        this.xRotO = this.xRot;

        double xVel;
        double yVel;
        double zVel;
        if (!this.isTurningAround()) {
            xVel = this.xd;
            yVel = this.yd;
            zVel = this.zd;
            this.move(this.xd, this.yd, this.zd);
            if (this.xd == xVel && this.yd == yVel && this.zd == zVel) {
                if (this.timeBeforeTurnAround-- <= 0) {
                    this.setTurningAround(true);
                }
            } else {
                Vec3 prevPos = Vec3.create(this.xo, this.yo, this.zo);
                Vec3 nextPos = Vec3.create(this.xo + 10.0D * xVel, this.yo + 10.0D * yVel, this.zo + 10.0D * zVel);
                HitResult hit = this.level.clip(prevPos, nextPos);
                if (hit != null && hit.hitType == HitType.TILE) {
                    int id = this.level.getTile(hit.x, hit.y, hit.z);
                    if (id != Tile.LOG.id && id != Tile.WOOD.id && id != AC_Blocks.woodBlocks.id && id != AC_Blocks.halfSteps3.id) {
                        if (id != 0) {
                            this.level.playSound(this, Tile.tiles[id].soundType.getStepSound(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                        }
                    } else {
                        this.attachedToSurface = true;
                        this.setPos(hit.pos.x, hit.pos.y, hit.pos.z);
                        this.level.playSound(this, "random.drr", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                    }
                }

                this.setTurningAround(true);
            }
        } else if (this.getReturnEntity() != null) {
            Mob returnsTo = this.getReturnEntity();
            if (returnsTo.removed) {
                this.remove();
                return;
            }

            xVel = returnsTo.x - this.x;
            yVel = returnsTo.y - this.y;
            zVel = returnsTo.z - this.z;
            double speedSqr = Math.sqrt(xVel * xVel + yVel * yVel + zVel * zVel);
            this.xd = 0.75D * xVel / speedSqr;
            this.yd = 0.75D * yVel / speedSqr;
            this.zd = 0.75D * zVel / speedSqr;
            if (this.attachedToSurface) {
                if (speedSqr > 1.2D) {
                    returnsTo.push(-0.15D * this.xd, -0.15D * this.yd, -0.15D * this.zd);
                    returnsTo.fallDistance = 0.0F;
                } else {
                    returnsTo.lerpMotion(0.0D, 0.0D, 0.0D);
                }
            } else {
                if (speedSqr <= 1.2D) {
                    this.remove();
                }

                this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
                this.setHeadingReverse();
            }
        } else {
            this.remove();
        }

        if (!this.isTurningAround()) {
            var entities = (List<Entity>) this.level.getEntities(this, this.bb.inflate(0.5D, 0.5D, 0.5D));
            for (Entity entity : entities) {
                boolean isItem = entity instanceof ItemEntity;
                if (isItem || entity instanceof Mob && entity != this.getReturnEntity()) {
                    if (isItem) {
                        this.level.playSound(this, "damage.fallsmall", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                    } else {
                        this.level.playSound(this, "damage.hurtflesh", 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
                    }

                    this.setGrabbedEntity(entity);
                    this.setTurningAround(true);
                    break;
                }
            }
        }

        Entity entityGrabbed = this.getGrabbedEntity();
        if (entityGrabbed != null && !entityGrabbed.removed) {
            entityGrabbed.fallDistance = 0.0F;
            entityGrabbed.setPos(this.x, this.y, this.z);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.remove();
    }

    @Override
    public void playerTouch(Player compound) {
    }

    @Override
    public boolean hurt(Entity entity, int damage) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void remove() {
        if (this.itemStack != null) {
            this.itemStack.setDamage(0);
        }

        super.remove();
    }

    public boolean isTurningAround() {
        return turningAround;
    }

    public void setTurningAround(boolean turningAround) {
        this.turningAround = turningAround;
    }

    public Mob getReturnEntity() {
        return this.returnsTo;
    }

    public void setReturnEntity(Mob returnsTo) {
        this.returnsTo = returnsTo;
    }

    public Entity getGrabbedEntity() {
        return entityGrabbed;
    }

    public void setGrabbedEntity(Entity entityGrabbed) {
        this.entityGrabbed = entityGrabbed;
    }
}
