package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.entity.ExLivingEntity;
import dev.adventurecraft.awakening.extension.entity.ai.pathing.ExEntityPath;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.EntityPath;
import net.minecraft.entity.player.PlayerEntity;
import dev.adventurecraft.awakening.script.EntityDescriptions;
import dev.adventurecraft.awakening.script.ScopeTag;
import dev.adventurecraft.awakening.script.ScriptEntity;
import dev.adventurecraft.awakening.script.ScriptEntityDescription;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class AC_EntityLivingScript extends LivingEntity implements IEntityPather {

    String initDescTo;
    String descriptionName;
    float prevWidth = 0.6F;
    float prevHeight = 1.8F;
    protected Scriptable scope;
    public String onCreated = "";
    public String onUpdate = "";
    public String onPathReached = "";
    public String onAttacked = "";
    public String onDeath = "";
    public String onInteraction = "";
    private EntityPath path;
    private Entity pathToEntity;
    private AC_CoordBlock pathToVec;
    public float maxPathDistance = 64.0F;
    private int nextPathIn;
    private double prevDistToPoint = 999999.0D;
    AC_TileEntityNpcPath triggerOnPath = null;

    public AC_EntityLivingScript(World world) {
        super(world);
        this.scope = ((ExWorld) world).getScript().getNewScope();
        Object jsEntity = Context.javaToJS(ScriptEntity.getEntityClass(this), this.scope);
        ScriptableObject.putProperty(this.scope, "entity", jsEntity);
    }

    public void setEntityDescription(String name) {
        this.setEntityDescription(name, true);
    }

    private void setEntityDescription(String name, boolean reset) {
        this.descriptionName = name;
        ScriptEntityDescription desc = EntityDescriptions.getDescription(name);
        if (desc == null) {
            return;
        }

        if (reset) {
            this.health = desc.health;
            ((ExLivingEntity) this).setMaxHealth(desc.health);
            this.onCreated = desc.onCreated;
            this.onUpdate = desc.onUpdate;
            this.onPathReached = desc.onPathReached;
            this.onAttacked = desc.onAttacked;
            this.onDeath = desc.onDeath;
            this.onInteraction = desc.onInteraction;
        }

        this.width = desc.width;
        this.height = desc.height;
        this.texture = desc.texture;
        this.movementSpeed = desc.moveSpeed;
        this.runCreatedScript();
    }

    @Override
    protected void tickHandSwing() {
    }

    @Override
    public void tick() {
        if (this.initDescTo != null) {
            if (!this.initDescTo.equals("")) {
                this.setEntityDescription(this.initDescTo, false);
            }

            this.initDescTo = null;
        }

        this.prevWidth = this.width;
        this.prevHeight = this.height;
        this.continuePathing();
        this.runUpdateScript();
        super.tick();
    }

    @Override
    public boolean damage(Entity entity, int damage) {
        Object jsEntity = Context.javaToJS(ScriptEntity.getEntityClass(entity), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingEntity", jsEntity);
        Object jsDamage = Context.javaToJS(damage, this.scope);
        ScriptableObject.putProperty(this.scope, "attackingDamage", jsDamage);
        return this.runOnAttackedScript() && super.damage(entity, damage);
    }

    @Override
    public void remove() {
        super.remove();
        this.runDeathScript();
    }

    @Override
    public boolean interact(PlayerEntity entity) {
        return this.runOnInteractionScript();
    }

    @Override
    public void writeAdditional(CompoundTag tag) {
        super.writeAdditional(tag);
        if (this.descriptionName != null && !this.descriptionName.equals("")) {
            tag.put("descriptionName", this.descriptionName);
        }

        if (!this.onCreated.equals("")) {
            tag.put("onCreated", this.onCreated);
        }

        if (!this.onUpdate.equals("")) {
            tag.put("onUpdate", this.onUpdate);
        }

        if (!this.onPathReached.equals("")) {
            tag.put("onPathReached", this.onPathReached);
        }

        if (!this.onAttacked.equals("")) {
            tag.put("onAttacked", this.onAttacked);
        }

        if (!this.onDeath.equals("")) {
            tag.put("onDeath", this.onDeath);
        }

        if (!this.onInteraction.equals("")) {
            tag.put("onInteraction", this.onInteraction);
        }

        if (tag.containsKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, tag.getCompoundTag("scope"));
        }
    }

    @Override
    public void readAdditional(CompoundTag tag) {
        super.readAdditional(tag);
        this.initDescTo = tag.getString("descriptionName");
        this.onCreated = tag.getString("onCreated");
        this.onUpdate = tag.getString("onUpdate");
        this.onPathReached = tag.getString("onPathReached");
        this.onAttacked = tag.getString("onAttacked");
        this.onDeath = tag.getString("onDeath");
        this.onInteraction = tag.getString("onInteraction");
        tag.put("scope", (AbstractTag) ScopeTag.getTagFromScope(this.scope));
    }

    public void runCreatedScript() {
        if (!this.onCreated.equals("")) {
            ((ExWorld) this.world).getScriptHandler().runScript(this.onCreated, this.scope);
        }
    }

    private void runUpdateScript() {
        if (!this.onUpdate.equals("")) {
            ((ExWorld) this.world).getScriptHandler().runScript(this.onUpdate, this.scope);
        }
    }

    private void runPathCompletedScript() {
        if (!this.onPathReached.equals("")) {
            ((ExWorld) this.world).getScriptHandler().runScript(this.onPathReached, this.scope);
        }
    }

    private boolean runOnAttackedScript() {
        if (!this.onAttacked.equals("")) {
            Object result = ((ExWorld) this.world).getScriptHandler().runScript(this.onAttacked, this.scope);
            return result instanceof Boolean b ? b : true;
        } else {
            return true;
        }
    }

    private void runDeathScript() {
        if (!this.onDeath.equals("")) {
            ((ExWorld) this.world).getScriptHandler().runScript(this.onDeath, this.scope);
        }
    }

    private boolean runOnInteractionScript() {
        if (!this.onInteraction.equals("")) {
            Object result = ((ExWorld) this.world).getScriptHandler().runScript(this.onInteraction, this.scope);
            return result instanceof Boolean b ? b : true;
        } else {
            return true;
        }
    }

    public boolean isPathing() {
        return this.pathToEntity != null || this.pathToVec != null || this.path != null;
    }

    public void pathToEntity(Entity entity) {
        this.pathToEntity = entity;
        this.pathToVec = null;
        this.path = this.world.findPathTo(this, this.pathToEntity, this.maxPathDistance);
        this.nextPathIn = this.world.rand.nextInt(40) + 60;
        this.prevDistToPoint = 999999.0D;
        this.triggerOnPath = null;
    }

    public void pathToPosition(int x, int y, int z) {
        this.pathToEntity = null;
        this.pathToVec = new AC_CoordBlock(x, y, z);
        this.path = this.world.method_189(this, x, y, z, this.maxPathDistance);
        this.nextPathIn = this.world.rand.nextInt(40) + 60;
        this.prevDistToPoint = 999999.0D;
        this.triggerOnPath = null;
    }

    public void clearPathing() {
        this.pathToEntity = null;
        this.pathToVec = null;
        this.path = null;
        this.triggerOnPath = null;
        this.forwardVelocity = 0.0F;
    }

    private void continuePathing() {
        if (!this.isPathing()) {
            return;
        }

        if (this.path == null || --this.nextPathIn <= 0 && this.pathToEntity != null && ((ExEntityPath) this.path).needNewPath(this.pathToEntity)) {
            if (this.pathToEntity != null) {
                this.path = this.world.findPathTo(this, this.pathToEntity, this.maxPathDistance);
            } else if (this.pathToVec != null) {
                this.path = this.world.method_189(this, this.pathToVec.x, this.pathToVec.y, this.pathToVec.z, this.maxPathDistance);
            }

            this.nextPathIn = this.world.rand.nextInt(40) + 10;
            this.prevDistToPoint = 999999.0D;
        }

        if (this.path == null) {
            return;
        }

        Vec3d point = this.path.method_2041(this);
        this.forwardVelocity = 0.0F;
        this.jumping = false;
        double dist = point.squareDistanceTo(this.x, point.y, this.z);
        if (dist >= this.prevDistToPoint && this.nextPathIn > 5) {
            this.nextPathIn = this.world.rand.nextInt(5) + 1;
        }

        this.prevDistToPoint = dist;

        double maxDist = (double) this.width * 1.1D;
        maxDist *= maxDist;

        while (point != null && point.squareDistanceTo(this.x, point.y, this.z) < maxDist) {
            this.path.method_2040();
            if (this.path.method_2042()) {
                point = null;
                this.path = null;
                this.runPathCompletedScript();
                if (this.triggerOnPath != null) {
                    this.triggerOnPath.pathFinished();
                }
                return;
            }
            point = this.path.method_2041(this);
            this.prevDistToPoint = 999999.0D;
        }

        if (point != null) {
            double dX = point.x - this.x;
            double dZ = point.z - this.z;
            double dY = point.y - (double) MathHelper.floor(this.boundingBox.minY + 0.5D);
            float newYaw = (float) (Math.atan2(dZ, dX) * 180.0D / (double) (float) Math.PI) - 90.0F;
            float extraYaw = newYaw - this.yaw;

            this.forwardVelocity = this.movementSpeed;
            while (extraYaw < -180.0F) {
                extraYaw += 360.0F;
            }

            while (extraYaw >= 180.0F) {
                extraYaw -= 360.0F;
            }

            if (extraYaw > 30.0F) {
                extraYaw = 30.0F;
            }

            if (extraYaw < -30.0F) {
                extraYaw = -30.0F;
            }

            this.yaw += extraYaw;
            if (dY > 0.0D) {
                this.jumping = true;
            }
        }
    }

    @Override
    public EntityPath getCurrentPath() {
        return this.path;
    }
}
