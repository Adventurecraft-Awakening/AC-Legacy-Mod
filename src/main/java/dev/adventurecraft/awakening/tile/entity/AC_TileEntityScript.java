package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;
import dev.adventurecraft.awakening.script.ScopeTag;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class AC_TileEntityScript extends TileEntity {

    public boolean checkTrigger = true;
    public boolean isActivated = false;
    public String onTriggerScriptFile = "";
    public String onDetriggerScriptFile = "";
    public String onUpdateScriptFile = "";
    public Scriptable scope;

    private void initScope() {
        this.scope = ((ExWorld) level).getScript().getNewScope();
        ScriptableObject.putProperty(this.scope, "xCoord", this.x);
        ScriptableObject.putProperty(this.scope, "yCoord", this.y);
        ScriptableObject.putProperty(this.scope, "zCoord", this.z);
    }

    @Override
    public void tick() {
        if (this.scope == null) {
            this.initScope();
        }

        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.level).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (this.isActivated && !this.onUpdateScriptFile.isEmpty()) {
            ((ExWorld) this.level).getScriptHandler().runScript(this.onUpdateScriptFile, this.scope);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.onTriggerScriptFile = tag.getString("onTriggerScriptFile");
        this.onDetriggerScriptFile = tag.getString("onDetriggerScriptFile");
        this.onUpdateScriptFile = tag.getString("onUpdateScriptFile");

        this.isActivated = tag.getBoolean("isActivated");
        ((ExCompoundTag) tag).findCompound("scope").ifPresent(c -> ScopeTag.loadScopeFromTag(this.scope, c));
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        var exTag = (ExCompoundTag) tag;
        exTag.putNonEmptyString("onTriggerScriptFile", this.onTriggerScriptFile);
        exTag.putNonEmptyString("onDetriggerScriptFile", this.onDetriggerScriptFile);
        exTag.putNonEmptyString("onUpdateScriptFile", this.onUpdateScriptFile);

        tag.putBoolean("isActivated", this.isActivated);
        tag.putTag("scope", ScopeTag.getTagFromScope(this.scope));
    }
}
