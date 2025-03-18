package dev.adventurecraft.awakening.tile.entity;

import dev.adventurecraft.awakening.entity.AC_EntityNPC;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public class AC_TileEntityNpcPath extends AC_TileEntityMinMax {

    private int entityID;
    private AC_EntityNPC npc = null;
    public static AC_EntityNPC lastEntity = null;

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.entityID = tag.getInt("entityID");
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putInt("entityID", this.entityID);
    }

    public AC_EntityNPC getNPC() {
        if (this.npc != null && this.npc.id == this.entityID) {
            return this.npc;
        }

        if (this.level != null) {
            Entity entity = ((ExWorld) this.level).getEntityByID(this.entityID);
            if (entity instanceof AC_EntityNPC foundNpc) {
                this.npc = foundNpc;
                return this.npc;
            }
        }

        return null;
    }

    public void setEntityToLastSelected() {
        if (lastEntity != null) {
            this.entityID = lastEntity.id;
            this.npc = lastEntity;
        }
    }

    public void pathEntity() {
        AC_EntityNPC npc = this.getNPC();
        if (npc != null) {
            this.npc.pathToPosition(this.x, this.y, this.z);
            if (this.isSet()) {
                this.npc.setTriggerOnPath(this);
            }
        }
    }

    public void pathFinished() {
        if (this.isSet()) {
            ((ExWorld) this.level).getTriggerManager().addArea(this.x, this.y, this.z, new AC_TriggerArea(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ));
            ((ExWorld) this.level).getTriggerManager().removeArea(this.x, this.y, this.z);
        }
    }
}
