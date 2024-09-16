package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.tile.entity.TileEntity;
import dev.adventurecraft.awakening.script.ScopeTag;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class AC_TileEntityScript extends TileEntity {

    public boolean inited = false;
    public boolean checkTrigger = true;
    public boolean isActivated = false;
    public String onTriggerScriptFile = "";
    public String onDetriggerScriptFile = "";
    public String onUpdateScriptFile = "";
    boolean loaded = false;
    public Scriptable scope = ((ExWorld) Minecraft.instance.level).getScript().getNewScope();

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
            this.isActivated = ((ExWorld) this.level).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (this.isActivated && !this.onUpdateScriptFile.equals("")) {
            ((ExWorld) this.level).getScriptHandler().runScript(this.onUpdateScriptFile, this.scope);
        }
    }

    @Override
    public void load(CompoundTag var1) {
        super.load(var1);
        this.onTriggerScriptFile = var1.getString("onTriggerScriptFile");
        this.onDetriggerScriptFile = var1.getString("onDetriggerScriptFile");
        this.onUpdateScriptFile = var1.getString("onUpdateScriptFile");
        this.isActivated = var1.getBoolean("isActivated");
        if (var1.hasKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, var1.getCompoundTag("scope"));
        }
    }

    @Override
    public void save(CompoundTag var1) {
        super.save(var1);
        if (!this.onTriggerScriptFile.isEmpty()) {
            var1.putString("onTriggerScriptFile", this.onTriggerScriptFile);
        }

        if (!this.onDetriggerScriptFile.isEmpty()) {
            var1.putString("onDetriggerScriptFile", this.onDetriggerScriptFile);
        }

        if (!this.onUpdateScriptFile.isEmpty()) {
            var1.putString("onUpdateScriptFile", this.onUpdateScriptFile);
        }

        var1.putBoolean("isActivated", this.isActivated);
        var1.putTag("scope", (Tag) ScopeTag.getTagFromScope(this.scope));
    }
}
