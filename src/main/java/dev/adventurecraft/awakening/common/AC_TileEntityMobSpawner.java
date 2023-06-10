package dev.adventurecraft.awakening.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ChestMinecartEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityRegistry;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.animal.WolfEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import dev.adventurecraft.awakening.script.ScopeTag;
import dev.adventurecraft.awakening.script.ScriptEntity;
import net.minecraft.util.io.AbstractTag;
import net.minecraft.util.io.CompoundTag;
import org.mozilla.javascript.Scriptable;

public class AC_TileEntityMobSpawner extends AC_TileEntityScript {

    public int delay = -1;
    public String entityID = "Pig";
    public int spawnNumber;
    public int respawnDelay;
    public int dropItem;
    public boolean hasDroppedItem;
    public boolean spawnOnTrigger;
    public boolean spawnOnDetrigger;
    public List<Entity> spawnedEntities;
    public List<Entity> entitiesLeft;
    public int spawnID;
    public int spawnMeta;
    Random rand;
    public Coord[] minVec;
    public Coord[] maxVec;
    public Coord minSpawnVec;
    public Coord maxSpawnVec;
    public int ticksBeforeLoad = 20;
    public CompoundTag delayLoadData;
    private boolean spawnStill = false;
    Scriptable scope;

    public AC_TileEntityMobSpawner() {
        this.delay = 20;
        this.spawnNumber = 3;
        this.respawnDelay = 1200;
        this.spawnedEntities = new ArrayList<>();
        this.entitiesLeft = new ArrayList<>();
        this.dropItem = 0;
        this.hasDroppedItem = false;
        this.spawnOnTrigger = true;
        this.spawnOnDetrigger = false;
        this.rand = new Random();
        this.minVec = new Coord[8];
        this.maxVec = new Coord[8];

        for (int var1 = 0; var1 < 8; ++var1) {
            this.minVec[var1] = new Coord();
            this.maxVec[var1] = new Coord();
        }

        this.minSpawnVec = new Coord();
        this.maxSpawnVec = new Coord();
        this.delayLoadData = null;
        this.scope = ((ExWorld) Minecraft.instance.world).getScript().getNewScope();
    }

    public int getNumAlive() {
        int var1 = 0;

        for (Entity var3 : this.spawnedEntities) {
            if (!var3.removed) {
                ++var1;
            } else {
                this.entitiesLeft.remove(var3);
            }
        }

        return var1;
    }

    public void resetMobs() {

        for (Entity var2 : this.spawnedEntities) {
            if (!var2.removed) {
                var2.remove();
            }
        }

        this.spawnedEntities.clear();
        this.entitiesLeft.clear();
        this.deactivateTriggers();
    }

    private boolean canSpawn(Entity var1) {
        return this.world.canSpawnEntity(var1.boundingBox) &&
            this.world.method_190(var1, var1.boundingBox).size() == 0 &&
            !this.world.method_218(var1.boundingBox);
    }

    public void spawnMobs() {
        if (this.delay > 0 || this.getNumAlive() > 0 || this.delayLoadData != null) {
            return;
        }

        int var1 = 0;

        while (true) {
            label130:
            {
                if (var1 < this.spawnNumber * 6) {
                    String var2 = this.entityID.replace(" ", "");
                    if (var2.equalsIgnoreCase("FallingBlock")) {
                        var2 = "FallingSand";
                    } else if (var2.startsWith("Slime")) {
                        var2 = "Slime";
                    } else if (var2.startsWith("Minecart")) {
                        var2 = "Minecart";
                    } else if (var2.startsWith("Spider")) {
                        var2 = "Spider";
                    } else if (var2.startsWith("Wolf")) {
                        var2 = "Wolf";
                    } else if (var2.endsWith("(Scripted)")) {
                        var2 = "Script";
                    }

                    Entity var3 = EntityRegistry.create(var2, this.world);
                    if (var3 == null) {
                        return;
                    }

                    if (var2.equalsIgnoreCase("FallingSand")) {
                        if (this.spawnID >= 256 || Block.BY_ID[this.spawnID] == null) {
                            return;
                        }

                        ((FallingBlockEntity) var3).blockId = this.spawnID;
                        ((ExFallingBlockEntity) var3).setMetadata(this.spawnMeta);
                    } else if (var2.equalsIgnoreCase("Item")) {
                        if (Item.byId[this.spawnID] == null) {
                            return;
                        }

                        ((ItemEntity) var3).stack = new ItemStack(this.spawnID, 1, this.spawnMeta);
                    } else if (this.entityID.startsWith("Slime") && this.entityID.length() > 6) {
                        int var4 = Integer.parseInt(this.entityID.split(":")[1].trim());
                        ((SlimeEntity) var3).setSize(var4);
                    } else if (this.entityID.equalsIgnoreCase("Minecart Chest")) {
                        ((ChestMinecartEntity) var3).type = 1;
                    } else if (this.entityID.equalsIgnoreCase("Minecart Furnace")) {
                        ((ChestMinecartEntity) var3).type = 2;
                    }

                    double var6;
                    if (this.maxSpawnVec.y == this.minSpawnVec.y) {
                        var6 = this.y + this.maxSpawnVec.y;
                    } else {
                        var6 = this.y + this.minSpawnVec.y + this.world.rand.nextInt(this.maxSpawnVec.y - this.minSpawnVec.y);
                    }

                    double var12 = (double) (this.x + this.minSpawnVec.x) + this.world.rand.nextDouble() * (double) (this.maxSpawnVec.x - this.minSpawnVec.x) + 0.5D;
                    double var8 = (double) (this.z + this.minSpawnVec.z) + this.world.rand.nextDouble() * (double) (this.maxSpawnVec.z - this.minSpawnVec.z) + 0.5D;
                    float var10;
                    if (!var2.equalsIgnoreCase("FallingSand")) {
                        var10 = this.world.rand.nextFloat() * 360.0F;
                    } else {
                        var10 = 0.0F;
                    }

                    var3.setPositionAndAngles(var12, var6, var8, var10, 0.0F);
                    if (!this.canSpawn(var3)) {
                        break label130;
                    }

                    this.world.spawnEntity(var3);
                    if (this.entityID.equalsIgnoreCase("Spider Skeleton")) {
                        SkeletonEntity var11 = new SkeletonEntity(this.world);
                        var11.setPositionAndAngles(var12, var6, var8, var10, 0.0F);
                        this.world.spawnEntity(var11);
                        var11.startRiding(var3);
                        this.spawnedEntities.add(var11);
                        this.entitiesLeft.add(var11);
                    } else if (this.entityID.equalsIgnoreCase("Spider Skeleton Sword")) {
                        AC_EntitySkeletonSword var13 = new AC_EntitySkeletonSword(this.world);
                        var13.setPositionAndAngles(var12, var6, var8, var10, 0.0F);
                        this.world.spawnEntity(var13);
                        var13.startRiding(var3);
                        this.spawnedEntities.add(var13);
                        this.entitiesLeft.add(var13);
                    } else {
                        WolfEntity var14;
                        if (this.entityID.equalsIgnoreCase("Wolf (Angry)")) {
                            var14 = (WolfEntity) var3;
                            var14.setAngry(true);
                        } else if (this.entityID.equalsIgnoreCase("Wolf (Tame)")) {
                            var14 = (WolfEntity) var3;
                            var14.setHasOwner(true);
                            var14.setTarget(null);
                            var14.health = 20;
                            var14.setOwner(Minecraft.instance.player.name);
                            var14.spawnBoneParticles(true);
                            this.world.method_185(var14, (byte) 7);
                        }
                    }

                    if (this.entityID.endsWith("(Scripted)")) {
                        var var15 = (AC_EntityLivingScript) var3;
                        var15.setEntityDescription(this.entityID.replace(" (Scripted)", ""));
                    }

                    if (var3 instanceof LivingEntity) {
                        ((LivingEntity) var3).onSpawnedFromSpawner();
                    }

                    this.spawnedEntities.add(var3);
                    this.entitiesLeft.add(var3);
                    if (this.spawnedEntities.size() < this.spawnNumber) {
                        break label130;
                    }
                }

                if (this.spawnNumber > 0 && this.spawnedEntities.size() == 0) {
                    this.delay = 20;
                    this.spawnStill = true;
                } else {
                    this.activateTriggers();
                    this.executeScript(this.onTriggerScriptFile);
                    this.spawnStill = false;
                }

                return;
            }

            ++var1;
        }
    }

    public void tick() {
        int var2;
        if (this.delayLoadData != null) {
            if (this.ticksBeforeLoad == 0) {
                short var12 = this.delayLoadData.getShort("numEntities");

                for (var2 = 0; var2 < var12; ++var2) {
                    int var13 = this.delayLoadData.getInt(String.format("entID_%d", var2));

                    for (Entity var6 : (List<Entity>) this.world.entities) {
                        if (var6.entityId == var13) {
                            this.spawnedEntities.add(var6);
                            if (var6.isAlive()) {
                                this.entitiesLeft.add(var6);
                            }
                            break;
                        }
                    }
                }

                this.delayLoadData = null;
                if (var12 > 0) {
                    this.executeScript(this.onTriggerScriptFile);
                }
            }

            --this.ticksBeforeLoad;
        } else if (this.delay > 0) {
            --this.delay;
        } else if (!this.spawnedEntities.isEmpty()) {
            if (this.getNumAlive() == 0) {
                this.spawnedEntities.clear();
                this.entitiesLeft.clear();
                this.delay = this.respawnDelay;
                if (this.dropItem > 0 && !this.hasDroppedItem) {
                    ItemEntity var1 = new ItemEntity(this.world, (double) this.x + 0.5D, (double) this.y + 0.5D, (double) this.z + 0.5D, new ItemStack(this.dropItem, 1, 0));
                    var1.pickupDelay = 10;
                    this.world.spawnEntity(var1);

                    for (var2 = 0; var2 < 20; ++var2) {
                        double var3 = this.rand.nextGaussian() * 0.02D;
                        double var5 = this.rand.nextGaussian() * 0.02D;
                        double var7 = this.rand.nextGaussian() * 0.02D;
                        double var9 = 10.0D;
                        this.world.addParticle("explode", var1.x + (double) (this.rand.nextFloat() * 2.0F) - 1.0D - var3 * var9, var1.y + (double) this.rand.nextFloat() - var5 * var9, var1.z + (double) (this.rand.nextFloat() * 2.0F) - 1.0D - var7 * var9, var3, var5, var7);
                    }

                    this.hasDroppedItem = true;
                }

                this.executeScript(this.onDetriggerScriptFile);

                for (int var11 = 4; var11 < 8; ++var11) {
                    if (this.isTriggerSet(var11)) {
                        this.activateTrigger(var11, this.minVec[var11], this.maxVec[var11]);
                    }
                }

                this.deactivateTriggers();
            } else {
                this.executeScript(this.onUpdateScriptFile);
            }

        } else {
            if (this.spawnStill || !this.spawnOnTrigger && !this.spawnOnDetrigger) {
                this.spawnMobs();
            }

            super.tick();
        }
    }

    public void setSpawnVec() {
        this.minSpawnVec.set(AC_ItemCursor.minX - this.x, AC_ItemCursor.minY - this.y, AC_ItemCursor.minZ - this.z);
        this.maxSpawnVec.set(AC_ItemCursor.maxX - this.x, AC_ItemCursor.maxY - this.y, AC_ItemCursor.maxZ - this.z);
    }

    public boolean isTriggerSet(int var1) {
        boolean var2 = this.minVec[var1].x != 0;
        var2 = var2 || this.minVec[var1].y != 0;
        var2 = var2 || this.minVec[var1].z != 0;
        var2 = var2 || this.maxVec[var1].x != 0;
        var2 = var2 || this.maxVec[var1].y != 0;
        var2 = var2 || this.maxVec[var1].z != 0;
        return var2;
    }

    public void setTrigger(int var1) {
        this.minVec[var1].set(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ);
        this.maxVec[var1].set(AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
    }

    public void setCursor(int var1) {
        AC_ItemCursor.oneX = AC_ItemCursor.minX = this.minVec[var1].x;
        AC_ItemCursor.oneY = AC_ItemCursor.minY = this.minVec[var1].y;
        AC_ItemCursor.oneZ = AC_ItemCursor.minZ = this.minVec[var1].z;
        AC_ItemCursor.twoX = AC_ItemCursor.maxX = this.maxVec[var1].x;
        AC_ItemCursor.twoY = AC_ItemCursor.maxY = this.maxVec[var1].y;
        AC_ItemCursor.twoZ = AC_ItemCursor.maxZ = this.maxVec[var1].z;
    }

    public void clearTrigger(int var1) {
        this.minVec[var1].x = this.minVec[var1].y = this.minVec[var1].z = 0;
        this.maxVec[var1].x = this.maxVec[var1].y = this.maxVec[var1].z = 0;
    }

    private void activateTriggers() {
        for (int var1 = 0; var1 < 4; ++var1) {
            if (this.isTriggerSet(var1)) {
                this.activateTrigger(var1, this.minVec[var1], this.maxVec[var1]);
            }
        }

    }

    private void activateTrigger(int var1, Coord var2, Coord var3) {
        if (var2.x != 0 || var2.y != 0 || var2.z != 0 || var3.x != 0 || var3.y != 0 || var3.z != 0) {
            ((ExWorld) this.world).getTriggerManager().addArea(
                this.x, this.y, this.z, var1, new AC_TriggerArea(var2.x, var2.y, var2.z, var3.x, var3.y, var3.z));
        }
    }

    private void deactivateTriggers() {
        ((ExWorld) this.world).getTriggerManager().removeArea(this.x, this.y, this.z);
    }

    @Override
    public void readNBT(CompoundTag var1) {
        super.readNBT(var1);
        this.entityID = var1.getString("EntityId");
        this.delay = var1.getShort("Delay");
        this.respawnDelay = var1.getInt("RespawnDelay");
        this.spawnNumber = var1.getInt("SpawnNumber");
        this.spawnOnTrigger = var1.getBoolean("SpawnOnTrigger");
        this.spawnOnDetrigger = var1.getBoolean("SpawnOnDetrigger");
        this.dropItem = var1.getInt("DropItem");
        this.hasDroppedItem = var1.getBoolean("HasDroppedItem");
        this.spawnID = var1.getInt("SpawnID");

        for (int var2 = 0; var2 < 8; ++var2) {
            this.minVec[var2].x = var1.getInt("minX".concat(Integer.toString(var2)));
            this.minVec[var2].y = var1.getInt("minY".concat(Integer.toString(var2)));
            this.minVec[var2].z = var1.getInt("minZ".concat(Integer.toString(var2)));
            this.maxVec[var2].x = var1.getInt("maxX".concat(Integer.toString(var2)));
            this.maxVec[var2].y = var1.getInt("maxY".concat(Integer.toString(var2)));
            this.maxVec[var2].z = var1.getInt("maxZ".concat(Integer.toString(var2)));
        }

        this.minSpawnVec.x = var1.getInt("minSpawnX");
        this.minSpawnVec.y = var1.getInt("minSpawnY");
        this.minSpawnVec.z = var1.getInt("minSpawnZ");
        this.maxSpawnVec.x = var1.getInt("maxSpawnX");
        this.maxSpawnVec.y = var1.getInt("maxSpawnY");
        this.maxSpawnVec.z = var1.getInt("maxSpawnZ");
        if (var1.containsKey("numEntities") && var1.getShort("numEntities") > 0) {
            this.ticksBeforeLoad = 20;
            this.delayLoadData = var1;
        }

        if (var1.containsKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, var1.getCompoundTag("scope"));
        }
    }

    @Override
    public void writeNBT(CompoundTag var1) {
        super.writeNBT(var1);
        var1.put("EntityId", this.entityID);
        var1.put("Delay", (short) this.delay);
        var1.put("RespawnDelay", this.respawnDelay);
        var1.put("SpawnNumber", this.spawnNumber);
        var1.put("SpawnOnTrigger", this.spawnOnTrigger);
        var1.put("SpawnOnDetrigger", this.spawnOnDetrigger);
        var1.put("SpawnID", this.spawnID);
        var1.put("DropItem", this.dropItem);
        var1.put("HasDroppedItem", this.hasDroppedItem);

        int var2;
        for (var2 = 0; var2 < 8; ++var2) {
            var1.put("minX".concat(Integer.toString(var2)), this.minVec[var2].x);
            var1.put("minY".concat(Integer.toString(var2)), this.minVec[var2].y);
            var1.put("minZ".concat(Integer.toString(var2)), this.minVec[var2].z);
            var1.put("maxX".concat(Integer.toString(var2)), this.maxVec[var2].x);
            var1.put("maxY".concat(Integer.toString(var2)), this.maxVec[var2].y);
            var1.put("maxZ".concat(Integer.toString(var2)), this.maxVec[var2].z);
        }

        var1.put("minSpawnX", this.minSpawnVec.x);
        var1.put("minSpawnY", this.minSpawnVec.y);
        var1.put("minSpawnZ", this.minSpawnVec.z);
        var1.put("maxSpawnX", this.maxSpawnVec.x);
        var1.put("maxSpawnY", this.maxSpawnVec.y);
        var1.put("maxSpawnZ", this.maxSpawnVec.z);
        var1.put("numEntities", (short) this.spawnedEntities.size());
        var2 = 0;

        for (Entity var4 : this.spawnedEntities) {
            var1.put(String.format("entID_%d", var2), var4.entityId);
            ++var2;
        }

        var1.put("scope", (AbstractTag) ScopeTag.getTagFromScope(this.scope));
    }

    private void executeScript(String var1) {
        if (var1.equals("")) {
            return;
        }

        int var2 = 0;
        ScriptEntity[] var3 = new ScriptEntity[this.entitiesLeft.size()];

        for (Entity entity : this.entitiesLeft) {
            var3[var2++] = ScriptEntity.getEntityClass(entity);
        }

        ((ExWorld) this.world).getScript().addObject("spawnedEntities", var3);
        ((ExWorld) this.world).getScriptHandler().runScript(var1, this.scope);
    }
}
