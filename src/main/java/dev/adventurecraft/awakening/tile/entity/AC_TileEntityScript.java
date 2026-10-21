package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.extension.nbt.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.tile.entity.TileEntity;
import dev.adventurecraft.awakening.script.ScriptAttachment;
import org.mozilla.javascript.Scriptable;

public class AC_TileEntityScript extends TileEntity {

    public boolean checkTrigger = true;
    public boolean isActivated = false;
    public String onTriggerScriptFile = "";
    public String onDetriggerScriptFile = "";
    public String onUpdateScriptFile = "";
    public Scriptable scope;
    protected final ScriptAttachment scriptAttachment = new ScriptAttachment();

    /** Chunk decoding may run on an I/O worker; attach only when gameplay invokes a script. */
    public Scriptable scriptScope() {
        if (this.scope == null) {
            this.scriptAttachment.bind("xCoord", this.x).bind("yCoord", this.y).bind("zCoord", this.z);
        }
        return this.scope = this.scriptAttachment.attach(((ExWorld) this.level).getScript());
    }

    @Override
    public void tick() {
        if (this.checkTrigger) {
            this.isActivated = ((ExWorld) this.level).getTriggerManager().isActivated(this.x, this.y, this.z);
            this.checkTrigger = false;
        }

        if (this.isActivated && !this.onUpdateScriptFile.isEmpty()) {
            ((ExWorld) this.level).getScriptHandler().runScript(this.onUpdateScriptFile, this.scriptScope());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.onTriggerScriptFile = tag.getString("onTriggerScriptFile");
        this.onDetriggerScriptFile = tag.getString("onDetriggerScriptFile");
        this.onUpdateScriptFile = tag.getString("onUpdateScriptFile");

        this.isActivated = tag.getBoolean("isActivated");
        this.scriptAttachment.load(tag.getCompoundTag("scope"));
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        var exTag = (ExCompoundTag) tag;
        exTag.putNonEmptyString("onTriggerScriptFile", this.onTriggerScriptFile);
        exTag.putNonEmptyString("onDetriggerScriptFile", this.onDetriggerScriptFile);
        exTag.putNonEmptyString("onUpdateScriptFile", this.onUpdateScriptFile);

        tag.putBoolean("isActivated", this.isActivated);
        tag.putTag("scope", this.scriptAttachment.save());
    }
}
