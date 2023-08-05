package dev.adventurecraft.awakening.script;

import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityRegistry;
import dev.adventurecraft.awakening.common.AC_TriggerArea;
import dev.adventurecraft.awakening.common.AC_UtilBullet;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class ScriptWorld {

    World world;

    ScriptWorld(World world) {
        this.world = world;
    }

    public int getBlockID(int x, int y, int z) {
        return this.world.getBlockId(x, y, z);
    }

    public void setBlockID(int x, int y, int z, int id) {
        this.world.setBlock(x, y, z, id);
    }

    public int getMetadata(int x, int y, int z) {
        return this.world.getBlockMeta(x, y, z);
    }

    public void setMetadata(int x, int y, int z, int meta) {
        this.world.setBlockMeta(x, y, z, meta);
    }

    public void setBlockIDAndMetadata(int x, int y, int z, int id, int meta) {
        this.world.placeBlockWithMetaData(x, y, z, id, meta);
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
        Entity entity = EntityRegistry.create(entityType, this.world);
        if (entity != null) {
            entity.setPosition(x, y, z);
            this.world.method_287(entity);
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
        HitResult hit = AC_UtilBullet.rayTraceBlocks(this.world, Vec3d.from(aX, aY, aZ), Vec3d.from(bX, bY, bZ));
        if (hit != null) {
            result[0] = new ScriptVec3(hit.field_1988.x, hit.field_1988.y, hit.field_1988.z);
            result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
        }
        return result;
    }

    public Object[] rayTrace(ScriptVec3 pointA, ScriptVec3 pointB) {
        return this.rayTrace(pointA.x, pointA.y, pointA.z, pointB.x, pointB.y, pointB.z);
    }

    public Object[] rayTrace(double aX, double aY, double aZ, double bX, double bY, double bZ) {
        var result = new Object[3];
        HitResult hit = AC_UtilBullet.rayTrace(this.world, null, Vec3d.from(aX, aY, aZ), Vec3d.from(bX, bY, bZ));
        if (hit != null) {
            result[0] = new ScriptVec3(hit.field_1988.x, hit.field_1988.y, hit.field_1988.z);
            if (hit.type == HitType.field_789) {
                result[1] = new ScriptVec3(hit.x, hit.y, hit.z);
            } else {
                result[2] = ScriptEntity.getEntityClass(hit.field_1989);
            }
        }
        return result;
    }
}
