package dev.adventurecraft.awakening.common;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.EntityPath;
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
    int ticksTillNewPath = 0;
    EntityPath pathToPoint = null;
    Entity entityToTrack;
    private boolean ranOnCreate = false;

    public AC_EntityNPC(World var1) {
        super(var1);
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
            double var1 = this.entityToTrack.x - this.x;
            double var3 = this.entityToTrack.z - this.z;
            float var5 = (float) (Math.atan2(var3, var1) * 180.0D / (double) ((float) Math.PI)) - 90.0F;

            float var6;
            var6 = var5 - this.yaw;
            while (var6 < -180.0F) {
                var6 += 360.0F;
            }

            while (var6 > 180.0F) {
                var6 -= 360.0F;
            }

            var6 = Math.max(Math.min(var6, 10.0F), -10.0F);
            this.yaw += var6;
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
    public void method_1353(Entity var1) {
        //System.out.println("collision"); TODO ???
    }

    public boolean seesThePlayer() {
        return this.entityToTrack != null;
    }

    @Override
    public void writeAdditional(CompoundTag var1) {
        super.writeAdditional(var1);
        var1.put("npcName", this.npcName);
        var1.put("chatMsg", this.chatMsg);
        var1.put("texture", this.texture);
        var1.put("spawnX", this.spawnX);
        var1.put("spawnY", this.spawnY);
        var1.put("spawnZ", this.spawnZ);
        var1.put("pathToHome", this.pathToHome);
        var1.put("trackPlayer", this.trackPlayer);
        var1.put("isAttackable", this.isAttackable);
    }

    @Override
    public void readAdditional(CompoundTag var1) {
        super.readAdditional(var1);
        this.npcName = var1.getString("npcName");
        this.chatMsg = var1.getString("chatMsg");
        this.texture = var1.getString("texture");
        if (var1.containsKey("spawnX")) {
            this.spawnX = var1.getDouble("spawnX");
            this.spawnY = var1.getDouble("spawnY");
            this.spawnZ = var1.getDouble("spawnZ");
        }

        if (var1.containsKey("pathToHome")) {
            this.pathToHome = var1.getBoolean("pathToHome");
        }

        if (var1.containsKey("trackPlayer")) {
            this.trackPlayer = var1.getBoolean("trackPlayer");
        }

        if (var1.containsKey("isAttackable")) {
            this.isAttackable = var1.getBoolean("isAttackable");
        }
    }

    @Override
    public boolean interact(PlayerEntity var1) {
        if (super.interact(var1)) {
            if (this.chatMsg != null && !this.chatMsg.equals("")) {
                Minecraft.instance.overlay.addChatMessage(String.format("<%s> %s", this.npcName, this.chatMsg));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean damage(Entity var1, int var2) {
        return this.isAttackable && super.damage(var1, var2);
    }

    @Override
    public boolean attackEntityFromMulti(Entity var1, int var2) {
        return this.isAttackable && super.attackEntityFromMulti(var1, var2);
    }
}
