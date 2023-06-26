package dev.adventurecraft.awakening.common;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.io.CompoundTag;

public class AC_TileEntityNpcPath extends AC_TileEntityMinMax {

    private int entityID;
    private AC_EntityNPC npc = null;
    public static AC_EntityNPC lastEntity = null;

    @Override
    public void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        this.entityID = tag.getInt("entityID");
    }

    @Override
    public void writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        tag.put("entityID", this.entityID);
    }

    public AC_EntityNPC getNPC() {
        if (this.npc != null && this.npc.entityId == this.entityID) {
            return this.npc;
        }

        if (this.world != null) {
            Entity entity = ((ExWorld) this.world).getEntityByID(this.entityID);
            if (entity instanceof AC_EntityNPC foundNpc) {
                this.npc = foundNpc;
                return this.npc;
            }
        }

        return null;
    }

    public void setEntityToLastSelected() {
        if (lastEntity != null) {
            this.entityID = lastEntity.entityId;
            this.npc = lastEntity;
        }
    }

    void pathEntity() {
        AC_EntityNPC npc = this.getNPC();
        if (npc != null) {
            this.npc.pathToPosition(this.x, this.y, this.z);
            if (this.isSet()) {
                this.npc.triggerOnPath = this;
            }
        }
    }

    void pathFinished() {
        if (this.isSet()) {
            ((ExWorld) this.world).getTriggerManager().addArea(this.x, this.y, this.z, new AC_TriggerArea(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ));
            ((ExWorld) this.world).getTriggerManager().removeArea(this.x, this.y, this.z);
        }
    }
}
