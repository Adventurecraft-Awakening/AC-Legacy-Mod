package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.world.World;

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
    Entity entityToTrack;
    private boolean ranOnCreate = false;

    public AC_EntityNPC(World world) {
        super(world);
        this.texture = "/mob/char.png";
        this.npcName = "New NPC";
        this.chatMsg = "Hello!";
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    public void tick() {
        if (!this.ranOnCreate) {
            this.ranOnCreate = true;
            this.runCreatedScript();
        }

        if (this.pathToHome && !this.isPathing() && this.squaredDistanceTo(this.spawnX, this.spawnY, this.spawnZ) > 4.0D) {
            this.pathToPosition((int) this.spawnX, (int) this.spawnY, (int) this.spawnZ);
        }

        super.tick();
        if (this.trackPlayer && this.entityToTrack == null) {
            this.entityToTrack = this.findPlayerToTrack();
        }

        if (this.entityToTrack != null) {
            if (!this.entityToTrack.isAlive()) {
                this.entityToTrack = null;
            } else if (this.entityToTrack.distanceTo(this) > 16.0F || !this.method_928(this.entityToTrack)) {
                this.entityToTrack = null;
            }
        }

        if (this.entityToTrack != null) {
            double dX = this.entityToTrack.x - this.x;
            double dZ = this.entityToTrack.z - this.z;
            float newYaw = (float) (Math.atan2(dZ, dX) * 180.0D / (double) ((float) Math.PI)) - 90.0F;

            float extraYaw = newYaw - this.yaw;
            while (extraYaw < -180.0F) {
                extraYaw += 360.0F;
            }

            while (extraYaw > 180.0F) {
                extraYaw -= 360.0F;
            }

            extraYaw = Math.max(Math.min(extraYaw, 10.0F), -10.0F);
            this.yaw += extraYaw;
        }

    }

    @Override
    protected void tickHandSwing() {
        if (this.initialSpot) {
            return;
        }
        this.initialSpot = true;
        this.spawnX = this.x;
        this.spawnY = this.y;
        this.spawnZ = this.z;
    }

    protected Entity findPlayerToTrack() {
        PlayerEntity var1 = this.world.getClosestPlayerTo(this, 16.0D);
        return var1 != null && this.method_928(var1) ? var1 : null;
    }

    @Override
    public boolean method_1380() {
        return false;
    }

    @Override
    public void method_1353(Entity entity) {
        //System.out.println("collision"); TODO ???
    }

    public boolean seesThePlayer() {
        return this.entityToTrack != null;
    }

    @Override
    public void writeAdditional(CompoundTag tag) {
        super.writeAdditional(tag);
        tag.put("npcName", this.npcName);
        tag.put("chatMsg", this.chatMsg);
        tag.put("texture", this.texture);
        tag.put("spawnX", this.spawnX);
        tag.put("spawnY", this.spawnY);
        tag.put("spawnZ", this.spawnZ);
        tag.put("pathToHome", this.pathToHome);
        tag.put("trackPlayer", this.trackPlayer);
        tag.put("isAttackable", this.isAttackable);
    }

    @Override
    public void readAdditional(CompoundTag tag) {
        super.readAdditional(tag);
        this.npcName = tag.getString("npcName");
        this.chatMsg = tag.getString("chatMsg");
        this.texture = tag.getString("texture");
        if (tag.containsKey("spawnX")) {
            this.spawnX = tag.getDouble("spawnX");
            this.spawnY = tag.getDouble("spawnY");
            this.spawnZ = tag.getDouble("spawnZ");
        }

        if (tag.containsKey("pathToHome")) {
            this.pathToHome = tag.getBoolean("pathToHome");
        }

        if (tag.containsKey("trackPlayer")) {
            this.trackPlayer = tag.getBoolean("trackPlayer");
        }

        if (tag.containsKey("isAttackable")) {
            this.isAttackable = tag.getBoolean("isAttackable");
        }
    }

    @Override
    public boolean interact(PlayerEntity entity) {
        if (super.interact(entity)) {
            if (this.chatMsg != null && !this.chatMsg.equals("")) {
                Minecraft.instance.overlay.addChatMessage(String.format("<%s> %s", this.npcName, this.chatMsg));
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean damage(Entity entity, int damage) {
        return this.isAttackable && super.damage(entity, damage);
    }
}
