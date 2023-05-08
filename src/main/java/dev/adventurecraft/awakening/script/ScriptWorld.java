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

    ScriptWorld(World var1) {
        this.world = var1;
    }

    public int getBlockID(int var1, int var2, int var3) {
        return this.world.getBlockId(var1, var2, var3);
    }

    public void setBlockID(int var1, int var2, int var3, int var4) {
        this.world.setBlock(var1, var2, var3, var4);
    }

    public int getMetadata(int var1, int var2, int var3) {
        return this.world.getBlockMeta(var1, var2, var3);
    }

    public void setMetadata(int var1, int var2, int var3, int var4) {
        this.world.setBlockMeta(var1, var2, var3, var4);
    }

    public void setBlockIDAndMetadata(int var1, int var2, int var3, int var4, int var5) {
        this.world.placeBlockWithMetaData(var1, var2, var3, var4, var5);
    }

    public float getLightValue(int var1, int var2, int var3) {
        return ((ExWorld) this.world).getLightValue(var1, var2, var3);
    }

    public void triggerBlock(int var1, int var2, int var3) {
        this.triggerArea(var1, var2, var3, var1, var2, var3);
    }

    public void triggerArea(int var1, int var2, int var3, int var4, int var5, int var6) {
        ((ExWorld) this.world).getTriggerManager().addArea(0, -1, 0, new AC_TriggerArea(var1, var2, var3, var4, var5, var6));
        ((ExWorld) this.world).getTriggerManager().removeArea(0, -1, 0);
    }

    public void setTriggerArea(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9) {
        ((ExWorld) this.world).getTriggerManager().addArea(var1, var2, var3, new AC_TriggerArea(var4, var5, var6, var7, var8, var9));
    }

    public void setTriggerArea(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10) {
        ((ExWorld) this.world).getTriggerManager().addArea(var1, var2, var3, var4, new AC_TriggerArea(var5, var6, var7, var8, var9, var10));
    }

    public void removeTriggerArea(int var1, int var2, int var3, int var4) {
        ((ExWorld) this.world).getTriggerManager().removeArea(var1, var2, var3, var4);
    }

    public void removeTriggerAreas(int var1, int var2, int var3) {
        ((ExWorld) this.world).getTriggerManager().removeArea(var1, var2, var3);
    }

    public ScriptEntity spawnEntity(String var1, double var2, double var4, double var6) {
        Entity var8 = EntityRegistry.create(var1, this.world);
        if (var8 != null) {
            var8.setPosition(var2, var4, var6);
            this.world.method_287(var8);
        }

        return ScriptEntity.getEntityClass(var8);
    }

    public ScriptEntity getEntityByID(int var1) {
        return ScriptEntity.getEntityClass(((ExWorld) this.world).getEntityByID(var1));
    }

    public Object[] rayTraceBlocks(ScriptVec3 var1, ScriptVec3 var2) {
        return this.rayTraceBlocks(var1.x, var1.y, var1.z, var2.x, var2.y, var2.z);
    }

    public Object[] rayTraceBlocks(double var1, double var3, double var5, double var7, double var9, double var11) {
        Object[] var13 = new Object[2];
        HitResult var14 = AC_UtilBullet.rayTraceBlocks(this.world, Vec3d.from(var1, var3, var5), Vec3d.from(var7, var9, var11));
        if (var14 != null) {
            var13[0] = new ScriptVec3(var14.field_1988.x, var14.field_1988.y, var14.field_1988.z);
            var13[1] = new ScriptVec3((float) var14.x, (float) var14.y, (float) var14.z);
        }

        return var13;
    }

    public Object[] rayTrace(ScriptVec3 var1, ScriptVec3 var2) {
        return this.rayTrace(var1.x, var1.y, var1.z, var2.x, var2.y, var2.z);
    }

    public Object[] rayTrace(double var1, double var3, double var5, double var7, double var9, double var11) {
        Object[] var13 = new Object[3];
        HitResult var14 = AC_UtilBullet.rayTrace(this.world, null, Vec3d.from(var1, var3, var5), Vec3d.from(var7, var9, var11));
        if (var14 != null) {
            var13[0] = new ScriptVec3(var14.field_1988.x, var14.field_1988.y, var14.field_1988.z);
            if (var14.type == HitType.field_789) {
                var13[1] = new ScriptVec3((float) var14.x, (float) var14.y, (float) var14.z);
            } else {
                var13[2] = ScriptEntity.getEntityClass(var14.field_1989);
            }
        }

        return var13;
    }
}
