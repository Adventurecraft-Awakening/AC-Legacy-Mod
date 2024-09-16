package dev.adventurecraft.awakening.common;

import java.util.*;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingTile;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.Minecart;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.Item;
import net.minecraft.world.ItemInstance;
import dev.adventurecraft.awakening.script.ScopeTag;
import dev.adventurecraft.awakening.script.ScriptEntity;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import org.mozilla.javascript.Scriptable;

public class AC_TileEntityMobSpawner extends AC_TileEntityScript {

    private static final Map<String,String> ALTERNATIVE_NAMES = new HashMap<>();
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
    public boolean showDebugInfo = true;
    public boolean showParticles = true;
    private boolean ignoreSpawnConditions = false;

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

        for (int id = 0; id < 8; ++id) {
            this.minVec[id] = new Coord();
            this.maxVec[id] = new Coord();
        }

        this.minSpawnVec = new Coord();
        this.maxSpawnVec = new Coord();
        this.delayLoadData = null;
        this.scope = ((ExWorld) Minecraft.instance.level).getScript().getNewScope();
    }

    public int getNumAlive() {
        int count = 0;

        for (Entity entity : this.spawnedEntities) {
            if (entity.removed) {
                this.entitiesLeft.remove(entity);
                continue;
            }
            count++;
        }

        return count;
    }

    public void resetMobs() {
        for (Entity entity : this.spawnedEntities) {
            if (!entity.removed) {
                entity.remove();
            }
        }

        this.spawnedEntities.clear();
        this.entitiesLeft.clear();
        this.deactivateTriggers();
    }

    private boolean canSpawn(Entity entity) {
        if(ignoreSpawnConditions){
            return true;
        }
        return this.level.isUnobstructed(entity.bb) &&
            this.level.getCubes(entity, entity.bb).isEmpty() &&
            !this.level.containsAnyLiquid(entity.bb);
    }

    private void spawnEntity(Entity entity) {
        this.level.addEntity(entity);
        this.spawnedEntities.add(entity);
        this.entitiesLeft.add(entity);
    }

    public void spawnMobs() {
        if (this.delay > 0 ||
            this.getNumAlive() > 0 ||
            this.delayLoadData != null) {
            return;
        }
        int spawnedCount = 0;
        while (true) {
            if (spawnedCount < this.spawnNumber * 6) {
                String id = this.entityID;
                if(ALTERNATIVE_NAMES.containsKey(this.entityID)){
                    id = ALTERNATIVE_NAMES.get(this.entityID);
                } else if(this.entityID.endsWith("(Scripted)")){
                    id = "Script";
                }

                /*if (id.equalsIgnoreCase("FallingBlock")) {
                    id = "FallingSand";
                } else
                if (id.startsWith("Slime")) {
                    id = "Slime";
                } else
                if (id.startsWith("Minecart")) {
                    id = "Minecart";
                } else
                if (id.startsWith("Spider")) {
                    id = "Spider";
                } else if (id.startsWith("Wolf")) {
                    id = "Wolf";
                } else if (id.endsWith("(Scripted)")) {
                    id = "Script";
                }*/

                Entity entity = EntityIO.newEntity(id, this.level);
                if (entity == null) {
                    System.out.println("NULL");
                    return;
                }
                switch (id){
                    case "FallingSand":
                        if (this.spawnID >= 256 || Tile.tiles[this.spawnID] == null) {
                            return;
                        }
                        if(this.spawnID == 0){
                            this.spawnID = 12;
                        }
                        ((FallingTile) entity).tileId = this.spawnID;
                        ((ExFallingBlockEntity) entity).setMetadata(this.spawnMeta);
                        break;
                    case "Item":
                        if (Item.items[this.spawnID] == null) {
                            return;
                        }
                        ((ItemEntity) entity).item = new ItemInstance(this.spawnID, 1, this.spawnMeta);
                        break;
                    case "Slime":
                        if(this.entityID.length() <= 6){
                            break;
                        }
                        int size = Integer.parseInt(this.entityID.split(":")[1].trim());
                        ((Slime) entity).setSize(size);
                        break;
                    case "Minecart":
                        if (this.entityID.equalsIgnoreCase("Minecart Chest")) {
                            ((Minecart) entity).type = 1;
                            break;
                        }
                        if (this.entityID.equalsIgnoreCase("Minecart Furnace")) {
                            ((Minecart) entity).type = 2;
                        }
                        break;
                }
                double y = this.y + this.maxSpawnVec.y;
                if (this.maxSpawnVec.y != this.minSpawnVec.y) {
                    y = this.y + this.minSpawnVec.y + this.level.random.nextInt(this.maxSpawnVec.y - this.minSpawnVec.y);
                }

                double x = (double) (this.x + this.minSpawnVec.x) + this.level.random.nextDouble() * (double) (this.maxSpawnVec.x - this.minSpawnVec.x) + 0.5D;
                double z = (double) (this.z + this.minSpawnVec.z) + this.level.random.nextDouble() * (double) (this.maxSpawnVec.z - this.minSpawnVec.z) + 0.5D;

                float yaw = 0.0F;
                if (!id.equalsIgnoreCase("FallingSand")) {
                    yaw = this.level.random.nextFloat() * 360.0F;
                }
                entity.moveTo(x, y, z, yaw, 0.0F);

                if (!this.canSpawn(entity)) {
                    ++spawnedCount;
                    continue;
                }

                this.spawnEntity(entity);

                switch (this.entityID){
                    case "Spider Skeleton":
                        var skeleton = new Skeleton(this.level);
                        skeleton.moveTo(x, y, z, yaw, 0.0F);
                        skeleton.ride(entity);
                        this.spawnEntity(skeleton);
                        break;
                    case "Spider Skeleton Sword":
                        var skeletonSword = new AC_EntitySkeletonSword(this.level);
                        skeletonSword.moveTo(x, y, z, yaw, 0.0F);
                        skeletonSword.ride(entity);
                        this.spawnEntity(skeletonSword);
                        break;
                    case "Wolf (Angry)":
                        var wolfAngry = (Wolf) entity;
                        wolfAngry.setAngery(true);
                        break;
                    case "Wolf (Tame)":
                        var wolfTamed = (Wolf) entity;
                        wolfTamed.setTamed(true);
                        wolfTamed.setPath(null);
                        wolfTamed.health = 20;
                        wolfTamed.setOwner(Minecraft.instance.player.name);
                        wolfTamed.spawnTamingParticles(true);
                        this.level.broadcastEntityEvent(wolfTamed, (byte) 7);
                        break;
                }

                if (this.entityID.endsWith("(Scripted)")) {
                    var scripted = (AC_EntityLivingScript) entity;
                    scripted.setEntityDescription(this.entityID.replace(" (Scripted)", ""));
                }

                if(showParticles) {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.spawnAnim();
                    }
                }

                if (this.spawnedEntities.size() < this.spawnNumber) {
                    ++spawnedCount;
                    continue;
                }
            }

            if (this.spawnNumber > 0 && this.spawnedEntities.isEmpty()) {
                this.delay = 20;
                this.spawnStill = true;
            } else {
                this.activateTriggers();
                this.executeScript(this.onTriggerScriptFile);
                this.spawnStill = false;
            }
            break;
        }
    }

    public void tick() {

        if (this.delayLoadData != null) {
            if (this.ticksBeforeLoad == 0) {
                short entityCount = this.delayLoadData.getShort("numEntities");
                for (int id = 0; id < entityCount; ++id) {
                    int entityId = this.delayLoadData.getInt(String.format("entID_%d", id));

                    for (Entity entity : (List<Entity>) this.level.entities) {
                        if (entity.id == entityId) {
                            this.spawnedEntities.add(entity);
                            if (entity.isAlive()) {
                                this.entitiesLeft.add(entity);
                            }
                            break;
                        }
                    }
                }

                this.delayLoadData = null;
                if (entityCount > 0) {
                    this.executeScript(this.onTriggerScriptFile);
                }
            }

            --this.ticksBeforeLoad;
            return;
        }
        if (this.delay > 0) {
            --this.delay;
            return;
        }
        if (!this.spawnedEntities.isEmpty()) {
            if (this.getNumAlive() == 0) {
                this.spawnedEntities.clear();
                this.entitiesLeft.clear();
                this.delay = this.respawnDelay;
                if (this.dropItem > 0 && !this.hasDroppedItem) {
                    var item = new ItemEntity(this.level, (double) this.x + 0.5D, (double) this.y + 0.5D, (double) this.z + 0.5D, new ItemInstance(this.dropItem, 1, 0));
                    item.throwTime = 10;
                    this.level.addEntity(item);

                    if(this.showParticles){
                        for (int i = 0; i < 20; ++i) {
                            double x = this.rand.nextGaussian() * 0.02D;
                            double y = this.rand.nextGaussian() * 0.02D;
                            double z = this.rand.nextGaussian() * 0.02D;
                            double var9 = 10.0D;
                            this.level.addParticle(
                                "explode",
                                item.x + (double) (this.rand.nextFloat() * 2.0F) - 1.0D - x * var9,
                                item.y + (double) this.rand.nextFloat() - y * var9,
                                item.z + (double) (this.rand.nextFloat() * 2.0F) - 1.0D - z * var9, x, y, z);
                        }
                    }
                    this.hasDroppedItem = true;
                }

                this.executeScript(this.onDetriggerScriptFile);

                for (int id = 4; id < 8; ++id) {
                    if (this.isTriggerSet(id)) {
                        this.activateTrigger(id, this.minVec[id], this.maxVec[id]);
                    }
                }

                this.deactivateTriggers();
            } else {
                this.executeScript(this.onUpdateScriptFile);
            }
            return;
        }

        if (this.spawnStill || !this.spawnOnTrigger && !this.spawnOnDetrigger) {
            this.spawnMobs();
        }

        super.tick();
    }

    public void setSpawnVec() {
        this.minSpawnVec.set(AC_ItemCursor.minX - this.x, AC_ItemCursor.minY - this.y, AC_ItemCursor.minZ - this.z);
        this.maxSpawnVec.set(AC_ItemCursor.maxX - this.x, AC_ItemCursor.maxY - this.y, AC_ItemCursor.maxZ - this.z);
    }

    public boolean isTriggerSet(int id) {
        boolean set = this.minVec[id].x != 0;
        set = set || this.minVec[id].y != 0;
        set = set || this.minVec[id].z != 0;
        set = set || this.maxVec[id].x != 0;
        set = set || this.maxVec[id].y != 0;
        set = set || this.maxVec[id].z != 0;
        return set;
    }

    public void setTrigger(int id) {
        this.minVec[id].set(AC_ItemCursor.minX, AC_ItemCursor.minY, AC_ItemCursor.minZ);
        this.maxVec[id].set(AC_ItemCursor.maxX, AC_ItemCursor.maxY, AC_ItemCursor.maxZ);
    }

    public void setCursor(int id) {
        AC_ItemCursor.oneX = AC_ItemCursor.minX = this.minVec[id].x;
        AC_ItemCursor.oneY = AC_ItemCursor.minY = this.minVec[id].y;
        AC_ItemCursor.oneZ = AC_ItemCursor.minZ = this.minVec[id].z;
        AC_ItemCursor.twoX = AC_ItemCursor.maxX = this.maxVec[id].x;
        AC_ItemCursor.twoY = AC_ItemCursor.maxY = this.maxVec[id].y;
        AC_ItemCursor.twoZ = AC_ItemCursor.maxZ = this.maxVec[id].z;
    }

    public void clearTrigger(int id) {
        this.minVec[id].x = this.minVec[id].y = this.minVec[id].z = 0;
        this.maxVec[id].x = this.maxVec[id].y = this.maxVec[id].z = 0;
    }

    private void activateTriggers() {
        for (int id = 0; id < 4; ++id) {
            if (this.isTriggerSet(id)) {
                this.activateTrigger(id, this.minVec[id], this.maxVec[id]);
            }
        }
    }

    private void activateTrigger(int id, Coord min, Coord max) {
        if (min.x != 0 || min.y != 0 || min.z != 0 ||
            max.x != 0 || max.y != 0 || max.z != 0) {
            ((ExWorld) this.level).getTriggerManager().addArea(
                this.x, this.y, this.z, id, new AC_TriggerArea(min.x, min.y, min.z, max.x, max.y, max.z));
        }
    }

    private void deactivateTriggers() {
        ((ExWorld) this.level).getTriggerManager().removeArea(this.x, this.y, this.z);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.entityID = tag.getString("EntityId");
        this.delay = tag.getShort("Delay");
        this.respawnDelay = tag.getInt("RespawnDelay");
        this.spawnNumber = tag.getInt("SpawnNumber");
        this.spawnOnTrigger = tag.getBoolean("SpawnOnTrigger");
        this.spawnOnDetrigger = tag.getBoolean("SpawnOnDetrigger");
        this.dropItem = tag.getInt("DropItem");
        this.hasDroppedItem = tag.getBoolean("HasDroppedItem");
        this.spawnID = tag.getInt("SpawnID");
        this.spawnMeta = tag.getInt("SpawnMeta");
        if(tag.hasKey("ShowDebugInfo")) {
            this.showDebugInfo = tag.getBoolean("ShowDebugInfo");
        }
        if(tag.hasKey("ShowParticles")) {
            this.showParticles = tag.getBoolean("ShowParticles");
        }
        for (int id = 0; id < 8; ++id) {
            this.minVec[id].x = tag.getInt("minX".concat(Integer.toString(id)));
            this.minVec[id].y = tag.getInt("minY".concat(Integer.toString(id)));
            this.minVec[id].z = tag.getInt("minZ".concat(Integer.toString(id)));
            this.maxVec[id].x = tag.getInt("maxX".concat(Integer.toString(id)));
            this.maxVec[id].y = tag.getInt("maxY".concat(Integer.toString(id)));
            this.maxVec[id].z = tag.getInt("maxZ".concat(Integer.toString(id)));
        }
        this.minSpawnVec.x = tag.getInt("minSpawnX");
        this.minSpawnVec.y = tag.getInt("minSpawnY");
        this.minSpawnVec.z = tag.getInt("minSpawnZ");
        this.maxSpawnVec.x = tag.getInt("maxSpawnX");
        this.maxSpawnVec.y = tag.getInt("maxSpawnY");
        this.maxSpawnVec.z = tag.getInt("maxSpawnZ");
        if (tag.hasKey("numEntities") && tag.getShort("numEntities") > 0) {
            this.ticksBeforeLoad = 20;
            this.delayLoadData = tag;
        }

        if (tag.hasKey("scope")) {
            ScopeTag.loadScopeFromTag(this.scope, tag.getCompoundTag("scope"));
        }
    }

    @Override
    public void save(CompoundTag tag) {
        super.save(tag);
        tag.putString("EntityId", this.entityID);
        tag.putShort("Delay", (short) this.delay);
        tag.putInt("RespawnDelay", this.respawnDelay);
        tag.putInt("SpawnNumber", this.spawnNumber);
        tag.putBoolean("SpawnOnTrigger", this.spawnOnTrigger);
        tag.putBoolean("SpawnOnDetrigger", this.spawnOnDetrigger);
        tag.putInt("SpawnID", this.spawnID);
        tag.putInt("SpawnMeta", this.spawnMeta);
        tag.putInt("DropItem", this.dropItem);
        tag.putBoolean("HasDroppedItem", this.hasDroppedItem);
        tag.putBoolean("ShowDebugInfo",this.showDebugInfo);
        tag.putBoolean("ShowParticles",this.showParticles);

        for (int id = 0; id < 8; ++id) {
            tag.putInt("minX".concat(Integer.toString(id)), this.minVec[id].x);
            tag.putInt("minY".concat(Integer.toString(id)), this.minVec[id].y);
            tag.putInt("minZ".concat(Integer.toString(id)), this.minVec[id].z);
            tag.putInt("maxX".concat(Integer.toString(id)), this.maxVec[id].x);
            tag.putInt("maxY".concat(Integer.toString(id)), this.maxVec[id].y);
            tag.putInt("maxZ".concat(Integer.toString(id)), this.maxVec[id].z);
        }

        tag.putInt("minSpawnX", this.minSpawnVec.x);
        tag.putInt("minSpawnY", this.minSpawnVec.y);
        tag.putInt("minSpawnZ", this.minSpawnVec.z);
        tag.putInt("maxSpawnX", this.maxSpawnVec.x);
        tag.putInt("maxSpawnY", this.maxSpawnVec.y);
        tag.putInt("maxSpawnZ", this.maxSpawnVec.z);
        tag.putShort("numEntities", (short) this.spawnedEntities.size());

        int id = 0;
        for (Entity entity : this.spawnedEntities) {
            tag.putInt(String.format("entID_%d", id), entity.id);
            ++id;
        }

        tag.putTag("scope", (Tag) ScopeTag.getTagFromScope(this.scope));
    }

    private void executeScript(String name) {
        if (name.isEmpty()) {
            return;
        }

        int id = 0;
        var spawned = new ScriptEntity[this.entitiesLeft.size()];

        for (Entity entity : this.entitiesLeft) {
            spawned[id++] = ScriptEntity.getEntityClass(entity);
        }

        ((ExWorld) this.level).getScript().addObject("spawnedEntities", spawned);
        ((ExWorld) this.level).getScriptHandler().runScript(name, this.scope);
    }

    static {
        ALTERNATIVE_NAMES.put("Falling Block", "FallingSand");
        ALTERNATIVE_NAMES.put("Slime Size: 1", "Slime");
        ALTERNATIVE_NAMES.put("Slime Size: 2", "Slime");
        ALTERNATIVE_NAMES.put("Slime Size: 4", "Slime");
        ALTERNATIVE_NAMES.put("Slime Size: 8", "Slime");
        ALTERNATIVE_NAMES.put("Slime Size: 16", "Slime");
        ALTERNATIVE_NAMES.put("Minecart Chest", "Minecart");
        ALTERNATIVE_NAMES.put("Minecart Furnace", "Minecart");
        ALTERNATIVE_NAMES.put("Spider Skeleton", "Spider");
        ALTERNATIVE_NAMES.put("Spider Skeleton Sword", "Spider");
        ALTERNATIVE_NAMES.put("Wolf (Angry)", "Wolf");
        ALTERNATIVE_NAMES.put("Wolf (Tame)", "Wolf");
        ALTERNATIVE_NAMES.put("Pig Zombie","PigZombie");
        ALTERNATIVE_NAMES.put("Skeleton Boss","SkeletonBoss");
        ALTERNATIVE_NAMES.put("Skeleton Rifle","SkeletonRifle");
        ALTERNATIVE_NAMES.put("Skeleton Shotgun","SkeletonShotgun");
        ALTERNATIVE_NAMES.put("Skeleton Sword","SkeletonSword");
        ALTERNATIVE_NAMES.put("Primed Tnt","PrimedTnt");
    }
}
