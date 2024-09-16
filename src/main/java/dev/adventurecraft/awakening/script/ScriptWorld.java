package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitType;
import net.minecraft.world.phys.Vec3;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.AC_UtilBullet;

@SuppressWarnings("unused")
public class ScriptWorld {

    Level world;

    ScriptWorld(Level world) {
        this.world = world;
    }

    public int getBlockID(int x, int y, int z) {
        return this.world.getTile(x, y, z);
    }

    public void setBlockID(int x, int y, int z, int id) {
        this.world.setTile(x, y, z, id);
    }

    public int getMetadata(int x, int y, int z) {
        return this.world.getData(x, y, z);
    }

    public void setMetadata(int x, int y, int z, int meta) {
        this.world.setData(x, y, z, meta);
    }

    public void setBlockIDAndMetadata(int x, int y, int z, int id, int meta) {
        this.world.setTileAndData(x, y, z, id, meta);
    }

    public float getLightValue(int x, int y, int z) {
        return ((ExWorld) this.world).getLightValue(x, y, z);
    }

    public void triggerBlock(int x, int y, int z) {
        this.triggerArea(x, y, z, x, y, z);
    }

    public void triggerArea(int aX, int aY, int aZ, int bX, int bY, int bZ) {
        ((ExWorld) this.world).getTriggerManager().addArea(0, -1, 0, new AC_TriggerArea(aX, aY, aZ, bX, bY, bZ));
        ((ExWorld) this.world).getTriggerManager().removeArea(0, -1, 0);
    }

    public void setTriggerArea(int x, int y, int z, int aX, int aY, int aZ, int bX, int bY, int bZ) {
        ((ExWorld) this.world).getTriggerManager().addArea(x, y, z, new AC_TriggerArea(aX, aY, aZ, bX, bY, bZ));
    }

    public void setTriggerArea(int x, int y, int z, int id, int aX, int aY, int aZ, int bX, int bY, int bZ) {
        ((ExWorld) this.world).getTriggerManager().addArea(x, y, z, id, new AC_TriggerArea(aX, aY, aZ, bX, bY, bZ));
    }

    public void removeTriggerArea(int x, int y, int z, int id) {
        ((ExWorld) this.world).getTriggerManager().removeArea(x, y, z, id);
    }

    public void removeTriggerAreas(int x, int y, int z) {
        ((ExWorld) this.world).getTriggerManager().removeArea(x, y, z);
    }

    public ScriptEntity spawnEntity(String entityType, double x, double y, double z) {
        Entity entity = EntityIO.newEntity(entityType, this.world);
        if (entity != null) {
            entity.setPos(x, y, z);
            this.world.ensureAdded(entity);
        }

        return ScriptEntity.getEntityClass(entity);
    }

    public ScriptEntity getEntityByID(int id) {
        return ScriptEntity.getEntityClass(((ExWorld) this.world).getEntityByID(id));
    }

    public Object[] rayTraceBlocks(ScriptVec3 pointA, ScriptVec3 pointB) {
        return this.rayTraceBlocks(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public Object[] rayTraceBlocks(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        var result = new Object[2];
        HitResult hit = AC_UtilBullet.rayTraceBlocks(this.world, Vec3.newTemp(aX, aY, aZ), Vec3.newTemp(bX, bY, bZ));
        if (hit != null) {
            result[0] = new ScriptVec3(hit.pos.x, hit.pos.y, hit.pos.z);
            result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
        }
        return result;
    }

    public Object[] rayTrace(ScriptVec3 pointA, ScriptVec3 pointB) {
        return this.rayTrace(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public Object[] rayTrace(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        var result = new Object[3];
        HitResult hit = AC_UtilBullet.rayTrace(this.world, null, Vec3.newTemp(aX, aY, aZ), Vec3.newTemp(bX, bY, bZ));
        if (hit != null) {
            result[0] = new ScriptVec3(hit.pos.x, hit.pos.y, hit.pos.z);
            if (hit.hitType == HitType.TILE) {
                result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
            } else {
                result[2] = ScriptEntity.getEntityClass(hit.entity);
            }
        }
        return result;
    }
}
