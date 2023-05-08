package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.BlockEntity;
import dev.adventurecraft.awakening.script.ScopeTag;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class AC_TileEntityScript extends BlockEntity {

    public boolean inited = false;
    public boolean checkTrigger = true;
    public boolean isActivated = false;
    public String onTriggerScriptFile = "";
    public String onDetriggerScriptFile = "";
    public String onUpdateScriptFile = "";
    boolean loaded = false;
    public Scriptable scope = ((ExWorld) Minecraft.instance.world).getScript().getNewScope();

    @Override
    public void tick() {
        if (!this.inited) {
            this.inited = true;
            Object var1 = Context.javaToJS((this.x), this.scope);
            ScriptableObject.putProperty(this.scope, "xCoord", var1);
            var1 = Context.javaToJS((this.y), this.scope);
            ScriptableObject.putProperty(this.scope, "yCoord", var1);
            var1 = Context.javaToJS((this.z), this.scope);
            ScriptableObject.putProperty(this.scope, "zCoord", var1);
        }

        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.world).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (this.isActivated && !this.onUpdateScriptFile.equals("")) {
            ((ExWorld) this.world).getScriptHandler().runScript(this.onUpdateScriptFile, this.scope);
        }
    }

    @Override
    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.onTriggerScriptFile = var1.getString("onTriggerScriptFile");
        this.onDetriggerScriptFile = var1.getString("onDetriggerScriptFile");
        this.onUpdateScriptFile = var1.getString("onUpdateScriptFile");
        this.isActivated = var1.getBoolean("isActivated");
        if (var1.containsKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, var1.getCompoundTag("scope"));
        }
    }

    @Override
    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        if (!this.onTriggerScriptFile.isEmpty()) {
            var1.put("onTriggerScriptFile", this.onTriggerScriptFile);
        }

        if (!this.onDetriggerScriptFile.isEmpty()) {
            var1.put("onDetriggerScriptFile", this.onDetriggerScriptFile);
        }

        if (!this.onUpdateScriptFile.isEmpty()) {
            var1.put("onUpdateScriptFile", this.onUpdateScriptFile);
        }

        var1.put("isActivated", this.isActivated);
        var1.put("scope", (AbstractTag) ScopeTag.getTagFromScope(this.scope));
    }
}
