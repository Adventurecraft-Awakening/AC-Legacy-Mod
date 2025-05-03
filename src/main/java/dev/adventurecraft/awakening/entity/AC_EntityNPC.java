package dev.adventurecraft.awakening.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AC_EntityNPC extends AC_EntityLivingScript {

    public String npcName;
    public String chatMsg;
    boolean initialSpot = false;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public boolean pathToHome = true;
    public boolean trackPlayer = true;
    public boolean isAttackable = false;
    public Entity entityToTrack = null;
    private boolean ranOnCreate = false;


    public AC_EntityNPC(Level world) {
        super(world);
        this.textureName = "/mob/char.png";
        this.npcName = "New NPC";
        this.chatMsg = "Hello!";
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        if (!this.ranOnCreate) {
            this.ranOnCreate = true;
            this.runCreatedScript();
        }

        if (this.pathToHome && !this.isPathing() && this.distanceToSqr(this.spawnX, this.spawnY, this.spawnZ) > 4.0D) {
            this.pathToPosition((int) this.spawnX, (int) this.spawnY, (int) this.spawnZ);
        }

        super.tick();
        if (this.trackPlayer && this.entityToTrack == null) {
            this.entityToTrack = this.findPlayerToTrack();
        }

        if (this.entityToTrack != null) {
            if (!this.entityToTrack.isAlive()) {
                this.entityToTrack = null;
            } else if (this.entityToTrack.distanceTo(this) > 16.0F || !this.canSee(this.entityToTrack)) {
                this.entityToTrack = null;
            }
        }

        if (this.entityToTrack != null) {
            double dX = this.entityToTrack.x - this.x;
            double dZ = this.entityToTrack.z - this.z;
            float newYaw = (float) (Math.atan2(dZ, dX) * 180.0D / (double) ((float) Math.PI)) - 90.0F;

            float extraYaw = newYaw - this.yRot;
            while (extraYaw < -180.0F) {
                extraYaw += 360.0F;
            }

            while (extraYaw > 180.0F) {
                extraYaw -= 360.0F;
            }

            extraYaw = Math.max(Math.min(extraYaw, 10.0F), -10.0F);
            this.yRot += extraYaw;
        }

    }

    @Override
    protected void serverAiStep() {
        if (this.initialSpot) {
            return;
        }
        this.initialSpot = true;
        this.spawnX = this.x;
        this.spawnY = this.y;
        this.spawnZ = this.z;
    }

    protected Entity findPlayerToTrack() {
        Player var1 = this.level.getNearestPlayer(this, 16.0D);
        return var1 != null && this.canSee(var1) ? var1 : null;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
        //System.out.println("collision"); TODO ???
    }

    public boolean seesThePlayer() {
        return this.entityToTrack != null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("npcName", this.npcName);
        tag.putString("chatMsg", this.chatMsg);
        tag.putString("texture", this.textureName);
        tag.putDouble("spawnX", this.spawnX);
        tag.putDouble("spawnY", this.spawnY);
        tag.putDouble("spawnZ", this.spawnZ);
        tag.putBoolean("pathToHome", this.pathToHome);
        tag.putBoolean("trackPlayer", this.trackPlayer);
        tag.putBoolean("isAttackable", this.isAttackable);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.npcName = tag.getString("npcName");
        this.chatMsg = tag.getString("chatMsg");
        this.textureName = tag.getString("texture");
        if (tag.hasKey("spawnX")) {
            this.spawnX = tag.getDouble("spawnX");
            this.spawnY = tag.getDouble("spawnY");
            this.spawnZ = tag.getDouble("spawnZ");
        }

        if (tag.hasKey("pathToHome")) {
            this.pathToHome = tag.getBoolean("pathToHome");
        }

        if (tag.hasKey("trackPlayer")) {
            this.trackPlayer = tag.getBoolean("trackPlayer");
        }

        if (tag.hasKey("isAttackable")) {
            this.isAttackable = tag.getBoolean("isAttackable");
        }
    }

    @Override
    public boolean interact(Player entity) {
        if (super.interact(entity)) {
            if (this.chatMsg != null && !this.chatMsg.equals("")) {
                Minecraft.instance.gui.addMessage(String.format("<%s> %s", this.npcName, this.chatMsg));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hurt(Entity entity, int damage) {
        return this.isAttackable && super.hurt(entity, damage);
    }
}
