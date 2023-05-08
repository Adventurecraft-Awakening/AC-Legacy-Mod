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

public class AC_EntityLivingScript extends LivingEntity implements IEntityPather, AC_IMultiAttackEntity {

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

    public AC_EntityLivingScript(World var1) {
        super(var1);
        this.scope = ((ExWorld) var1).getScript().getNewScope();
        Object var2 = Context.javaToJS(ScriptEntity.getEntityClass(this), this.scope);
        ScriptableObject.putProperty(this.scope, "entity", var2);
    }

    public void setEntityDescription(String var1) {
        this.setEntityDescription(var1, true);
    }

    private void setEntityDescription(String var1, boolean var2) {
        this.descriptionName = var1;
        ScriptEntityDescription var3 = EntityDescriptions.getDescription(var1);
        if (var3 != null) {
            if (var2) {
                this.health = var3.health;
                ((ExLivingEntity) this).setMaxHealth(var3.health);
                this.onCreated = var3.onCreated;
                this.onUpdate = var3.onUpdate;
                this.onPathReached = var3.onPathReached;
                this.onAttacked = var3.onAttacked;
                this.onDeath = var3.onDeath;
                this.onInteraction = var3.onInteraction;
            }

            this.width = var3.width;
            this.height = var3.height;
            this.texture = var3.texture;
            this.movementSpeed = var3.moveSpeed;
            this.runCreatedScript();
        }

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
    public boolean damage(Entity var1, int var2) {
        Object var3 = Context.javaToJS(ScriptEntity.getEntityClass(var1), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingEntity", var3);
        var3 = Context.javaToJS((var2), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingDamage", var3);
        return this.runOnAttackedScript() && super.damage(var1, var2);
    }

    public boolean attackEntityFromMulti(Entity var1, int var2) {
        Object var3 = Context.javaToJS(ScriptEntity.getEntityClass(var1), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingEntity", var3);
        var3 = Context.javaToJS((var2), this.scope);
        ScriptableObject.putProperty(this.scope, "attackingDamage", var3);
        return this.runOnAttackedScript() && AC_IMultiAttackEntity.super.attackEntityFromMulti(var1, var2);
    }

    @Override
    public void remove() {
        super.remove();
        this.runDeathScript();
    }

    @Override
    public boolean interact(PlayerEntity var1) {
        return this.runOnInteractionScript();
    }

    @Override
    public void writeAdditional(CompoundTag var1) {
        super.writeAdditional(var1);
        if (this.descriptionName != null && !this.descriptionName.equals("")) {
            var1.put("descriptionName", this.descriptionName);
        }

        if (!this.onCreated.equals("")) {
            var1.put("onCreated", this.onCreated);
        }

        if (!this.onUpdate.equals("")) {
            var1.put("onUpdate", this.onUpdate);
        }

        if (!this.onPathReached.equals("")) {
            var1.put("onPathReached", this.onPathReached);
        }

        if (!this.onAttacked.equals("")) {
            var1.put("onAttacked", this.onAttacked);
        }

        if (!this.onDeath.equals("")) {
            var1.put("onDeath", this.onDeath);
        }

        if (!this.onInteraction.equals("")) {
            var1.put("onInteraction", this.onInteraction);
        }

        if (var1.containsKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, var1.getCompoundTag("scope"));
        }

    }

    @Override
    public void readAdditional(CompoundTag var1) {
        super.readAdditional(var1);
        this.initDescTo = var1.getString("descriptionName");
        this.onCreated = var1.getString("onCreated");
        this.onUpdate = var1.getString("onUpdate");
        this.onPathReached = var1.getString("onPathReached");
        this.onAttacked = var1.getString("onAttacked");
        this.onDeath = var1.getString("onDeath");
        this.onInteraction = var1.getString("onInteraction");
        var1.put("scope", (AbstractTag) ScopeTag.getTagFromScope(this.scope));
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
            Object var1 = ((ExWorld) this.world).getScriptHandler().runScript(this.onAttacked, this.scope);
            return var1 instanceof Boolean b ? b : true;
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
            Object var1 = ((ExWorld) this.world).getScriptHandler().runScript(this.onInteraction, this.scope);
            return var1 instanceof Boolean b ? b : true;
        } else {
            return true;
        }
    }

    public boolean isPathing() {
        return this.pathToEntity != null || this.pathToVec != null || this.path != null;
    }

    public void pathToEntity(Entity var1) {
        this.pathToEntity = var1;
        this.pathToVec = null;
        this.path = this.world.findPathTo(this, this.pathToEntity, this.maxPathDistance);
        this.nextPathIn = this.world.rand.nextInt(40) + 60;
        this.prevDistToPoint = 999999.0D;
        this.triggerOnPath = null;
    }

    public void pathToPosition(int var1, int var2, int var3) {
        this.pathToEntity = null;
        this.pathToVec = new AC_CoordBlock(var1, var2, var3);
        this.path = this.world.method_189(this, var1, var2, var3, this.maxPathDistance);
        this.nextPathIn = this.world.rand.nextInt(40) + 60;
        this.prevDistToPoint = 999999.0D;
        this.triggerOnPath = null;
    }

    public void clearPathing() {
        this.pathToEntity = null;
        this.pathToVec = null;
        this.path = null;
        this.triggerOnPath = null;
    }

    private void continuePathing() {
        if (this.isPathing()) {
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

            Vec3d var1 = this.path.method_2041(this);
            this.forwardVelocity = 0.0F;
            this.jumping = false;
            double var2 = var1.squareDistanceTo(this.x, var1.y, this.z);
            if (var2 >= this.prevDistToPoint && this.nextPathIn > 5) {
                this.nextPathIn = this.world.rand.nextInt(5) + 1;
            }

            this.prevDistToPoint = var2;

            double var4;
            for (var4 = (double) this.width * 1.1D; var1 != null && var1.squareDistanceTo(this.x, var1.y, this.z) < var4 * var4; this.prevDistToPoint = 999999.0D) {
                this.path.method_2040();
                if (this.path.method_2042()) {
                    var1 = null;
                    this.path = null;
                    this.runPathCompletedScript();
                    if (this.triggerOnPath != null) {
                        this.triggerOnPath.pathFinished();
                    }
                    return;
                }

                var1 = this.path.method_2041(this);
            }

            if (var1 != null) {
                var4 = var1.x - this.x;
                double var6 = var1.z - this.z;
                double var8 = var1.y - (double) MathHelper.floor(this.boundingBox.minY + 0.5D);
                float var10 = (float) (Math.atan2(var6, var4) * 180.0D / (double) ((float) Math.PI)) - 90.0F;
                float var11 = var10 - this.yaw;

                this.forwardVelocity = this.movementSpeed;
                while (var11 < -180.0F) {
                    var11 += 360.0F;
                }

                while (var11 >= 180.0F) {
                    var11 -= 360.0F;
                }

                if (var11 > 30.0F) {
                    var11 = 30.0F;
                }

                if (var11 < -30.0F) {
                    var11 = -30.0F;
                }

                this.yaw += var11;
                if (var8 > 0.0D) {
                    this.jumping = true;
                }
            }
        }

    }

    @Override
    public EntityPath getCurrentPath() {
        return this.path;
    }
}
