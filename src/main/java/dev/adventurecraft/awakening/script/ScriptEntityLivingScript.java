package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;

@SuppressWarnings("unused")
public class ScriptEntityLivingScript extends ScriptEntityLiving {

    AC_EntityLivingScript entityLivingScript;

    ScriptEntityLivingScript(AC_EntityLivingScript var1) {
        super(var1);
        this.entityLivingScript = var1;
    }

    public boolean isPathing() {
        return this.entityLivingScript.isPathing();
    }

    public void clearPath() {
        this.entityLivingScript.clearPathing();
    }

    public void pathToEntity(ScriptEntity var1) {
        this.entityLivingScript.pathToEntity(var1.entity);
    }

    public void pathToBlock(int var1, int var2, int var3) {
        this.entityLivingScript.pathToPosition(var1, var2, var3);
    }

    public String getOnCreated() {
        return this.entityLivingScript.onCreated;
    }

    public void setOnCreated(String var1) {
        this.entityLivingScript.onCreated = var1;
        this.entityLivingScript.runCreatedScript();
    }

    public String getOnUpdated() {
        return this.entityLivingScript.onUpdate;
    }

    public void setOnUpdated(String var1) {
        this.entityLivingScript.onUpdate = var1;
    }

    public String getOnPathReached() {
        return this.entityLivingScript.onPathReached;
    }

    public void setOnPathReached(String var1) {
        this.entityLivingScript.onPathReached = var1;
    }

    public String getOnAttacked() {
        return this.entityLivingScript.onAttacked;
    }

    public void setOnAttacked(String var1) {
        this.entityLivingScript.onAttacked = var1;
    }

    public String getOnDeath() {
        return this.entityLivingScript.onDeath;
    }

    public void setOnDeath(String var1) {
        this.entityLivingScript.onDeath = var1;
    }

    public String getOnInteraction() {
        return this.entityLivingScript.onInteraction;
    }

    public void setOnInteraction(String var1) {
        this.entityLivingScript.onInteraction = var1;
    }
}
