package dev.adventurecraft.awakening.tile.entity;

import java.util.*;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.entity.AC_EntityLivingScript;
import dev.adventurecraft.awakening.entity.AC_EntitySkeletonSword;
import dev.adventurecraft.awakening.extension.entity.ExFallingBlockEntity;
import dev.adventurecraft.awakening.extension.util.io.ExCompoundTag;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.item.AC_ItemCursor;
import dev.adventurecraft.awakening.util.Xoshiro128PP;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityIO;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.nbt.CompoundTag;
import org.mozilla.javascript.Scriptable;

public class AC_TileEntityMobSpawner extends AC_TileEntityScript {

    private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<>();

    public int delay = -1;
    public String entityID = "Pig";
    public int spawnNumber;
    public int respawnDelay;
    public int dropItem;
    public boolean hasDroppedItem;
    public boolean spawnOnTrigger;
    public boolean spawnOnDetrigger;
    public final List<Entity> spawnedEntities;
    public final List<Entity> entitiesLeft;
    public int spawnID;
    public int spawnMeta;
    private final Xoshiro128PP rand;
    public final Coord[] minVec;
    public final Coord[] maxVec;
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
        this.rand = new Xoshiro128PP();
        this.minVec = new Coord[8];
        this.maxVec = new Coord[8];

        Arrays.fill(this.minVec, Coord.zero);
        Arrays.fill(this.maxVec, Coord.zero);

        this.minSpawnVec = Coord.zero;
        this.maxSpawnVec = Coord.zero;
        this.delayLoadData = null;
        this.scope = ((ExWorld) Minecraft.instance.level).getScript().getNewScope();
    }

    public int getNumAlive() {
        int count = 0;

        var iterator = this.entitiesLeft.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (entity.removed) {
                iterator.remove();
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
        if (ignoreSpawnConditions) {
            return true;
        }
        return this.level.isUnobstructed(entity.bb) && this.level.getCubes(entity, entity.bb).isEmpty() &&
            !this.level.containsAnyLiquid(entity.bb);
    }

    private void spawnEntity(Entity entity) {
        this.level.addEntity(entity);
        this.spawnedEntities.add(entity);
        this.entitiesLeft.add(entity);
    }

    public void spawnMobs() {
        if (this.delay > 0 || this.getNumAlive() > 0 || this.delayLoadData != null) {
            return;
        }

        int spawnedCount = 0;
        while (true) {
            if (spawnedCount < this.spawnNumber * 6) {
                String id = this.entityID;
                String altId = ALTERNATIVE_NAMES.get(this.entityID);
                if (altId != null) {
                    id = altId;
                }
                else if (this.entityID.endsWith("(Scripted)")) {
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
                    ACMod.LOGGER.error("Missing entity for id {}", id);
                    return;
                }

                switch (id) {
                    case "FallingSand":
                        if (this.spawnID >= 256 || Tile.tiles[this.spawnID] == null) {
                            return;
                        }
                        if (this.spawnID == 0) {
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
                        if (this.entityID.length() <= 6) {
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
                    // TODO: use nextDouble()?
                    int bound = this.maxSpawnVec.y - this.minSpawnVec.y;
                    y = this.y + this.minSpawnVec.y + this.level.random.nextInt(bound);
                }

                double x = (double) (this.x + this.minSpawnVec.x) +
                    this.level.random.nextDouble() * (double) (this.maxSpawnVec.x - this.minSpawnVec.x) + 0.5D;

                double z = (double) (this.z + this.minSpawnVec.z) +
                    this.level.random.nextDouble() * (double) (this.maxSpawnVec.z - this.minSpawnVec.z) + 0.5D;

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

                switch (this.entityID) {
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

                if (showParticles) {
                    if (entity instanceof Mob livingEntity) {
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
            }
            else {
                this.activateTriggers();
                this.executeScript(this.onTriggerScriptFile);
                this.spawnStill = false;
            }
            break;
        }
    }

    private void loadFromTag(CompoundTag tag) {
        short entityCount = tag.getShort("numEntities");
        for (int id = 0; id < entityCount; ++id) {
            int entityId = tag.getInt(String.format("entID_%d", id));

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

        if (entityCount > 0) {
            this.executeScript(this.onTriggerScriptFile);
        }
    }

    private void spawnItem() {
        var instance = new ItemInstance(this.dropItem, 1, 0);
        var item = new ItemEntity(this.level, this.x + 0.5D, this.y + 0.5D, this.z + 0.5D, instance);
        item.throwTime = 10;
        this.level.addEntity(item);

        if (this.showParticles) {
            this.spawnItemParticles(item);
        }
    }

    private void spawnItemParticles(ItemEntity item) {
        double size = 10.0D;
        for (int i = 0; i < 20; ++i) {
            double dx = this.rand.nextGaussian() * 0.02D;
            double dy = this.rand.nextGaussian() * 0.02D;
            double dz = this.rand.nextGaussian() * 0.02D;

            double x = item.x + this.rand.nextSignedFloat() - dx * size;
            double y = item.y + this.rand.nextFloat() - dy * size;
            double z = item.z + this.rand.nextSignedFloat() - dz * size;
            
            this.level.addParticle("explode", x, y, z, dx, dy, dz);
        }
    }

    private void detrigger() {
        this.spawnedEntities.clear();
        this.entitiesLeft.clear();
        this.delay = this.respawnDelay;

        if (this.dropItem > 0 && !this.hasDroppedItem) {
            this.spawnItem();
            this.hasDroppedItem = true;
        }

        this.executeScript(this.onDetriggerScriptFile);

        for (int id = 4; id < 8; ++id) {
            if (this.isTriggerSet(id)) {
                this.activateTrigger(id, this.minVec[id], this.maxVec[id]);
            }
        }

        this.deactivateTriggers();
    }

    public void tick() {
        if (this.delayLoadData != null) {
            if (this.ticksBeforeLoad == 0) {
                this.loadFromTag(this.delayLoadData);
                this.delayLoadData = null;
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
                this.detrigger();
            }
            else {
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
        var origin = new Coord(this.x, this.y, this.z);
        this.minSpawnVec = AC_ItemCursor.min().sub(origin);
        this.maxSpawnVec = AC_ItemCursor.max().sub(origin);
    }

    public boolean isTriggerSet(int id) {
        Coord min = this.minVec[id];
        Coord max = this.maxVec[id];
        return !min.equals(0) || !max.equals(0);
    }

    public void setTrigger(int id) {
        this.minVec[id] = AC_ItemCursor.min();
        this.maxVec[id] = AC_ItemCursor.max();
    }

    public void setCursor(int id) {
        AC_ItemCursor.setOne(AC_ItemCursor.setMin(this.minVec[id]));
        AC_ItemCursor.setTwo(AC_ItemCursor.setMax(this.maxVec[id]));
    }

    public void clearTrigger(int id) {
        this.minVec[id] = Coord.zero;
        this.maxVec[id] = Coord.zero;
    }

    private void activateTriggers() {
        for (int id = 0; id < 4; ++id) {
            if (this.isTriggerSet(id)) {
                this.activateTrigger(id, this.minVec[id], this.maxVec[id]);
            }
        }
    }

    private void activateTrigger(int id, Coord min, Coord max) {
        if (min.equals(0) && max.equals(0)) {
            return;
        }
        var area = new AC_TriggerArea(min, max);
        ((ExWorld) this.level).getTriggerManager().addArea(this.x, this.y, this.z, id, area);
    }

    private void deactivateTriggers() {
        ((ExWorld) this.level).getTriggerManager().removeArea(this.x, this.y, this.z);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var exTag = (ExCompoundTag) tag;

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
        exTag.findBool("ShowDebugInfo").ifPresent(b -> this.showDebugInfo = b);
        exTag.findBool("ShowParticles").ifPresent(b -> this.showParticles = b);

        // TODO: replace with NBT int arrays
        for (int id = 0; id < 8; ++id) {
            this.minVec[id] = new Coord(tag.getInt("minX" + id), tag.getInt("minY" + id), tag.getInt("minZ" + id));
            this.maxVec[id] = new Coord(tag.getInt("maxX" + id), tag.getInt("maxY" + id), tag.getInt("maxZ" + id));
        }
        this.minSpawnVec = new Coord(tag.getInt("minSpawnX"), tag.getInt("minSpawnY"), tag.getInt("minSpawnZ"));
        this.maxSpawnVec = new Coord(tag.getInt("maxSpawnX"), tag.getInt("maxSpawnY"), tag.getInt("maxSpawnZ"));

        if (exTag.findShort("numEntities").filter(n -> n > 0).isPresent()) {
            this.ticksBeforeLoad = 20;
            this.delayLoadData = tag;
        }

        exTag.findCompound("scope").ifPresent(c -> ScopeTag.loadScopeFromTag(this.scope, c));
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
        tag.putBoolean("ShowDebugInfo", this.showDebugInfo);
        tag.putBoolean("ShowParticles", this.showParticles);

        for (int id = 0; id < 8; ++id) {
            Coord min = this.minVec[id];
            Coord max = this.maxVec[id];
            tag.putInt("minX" + id, min.x);
            tag.putInt("minY" + id, min.y);
            tag.putInt("minZ" + id, min.z);
            tag.putInt("maxX" + id, max.x);
            tag.putInt("maxY" + id, max.y);
            tag.putInt("maxZ" + id, max.z);
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

        tag.putTag("scope", ScopeTag.getTagFromScope(this.scope));
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
        ALTERNATIVE_NAMES.put("Pig Zombie", "PigZombie");
        ALTERNATIVE_NAMES.put("Skeleton Boss", "SkeletonBoss");
        ALTERNATIVE_NAMES.put("Skeleton Rifle", "SkeletonRifle");
        ALTERNATIVE_NAMES.put("Skeleton Shotgun", "SkeletonShotgun");
        ALTERNATIVE_NAMES.put("Skeleton Sword", "SkeletonSword");
        ALTERNATIVE_NAMES.put("Primed Tnt", "PrimedTnt");
    }
}
