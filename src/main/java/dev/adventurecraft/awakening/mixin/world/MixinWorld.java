package dev.adventurecraft.awakening.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.ACMainThread;
import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.gui.ExInGameHud;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import dev.adventurecraft.awakening.extension.client.sound.ExSoundHelper;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
import dev.adventurecraft.awakening.script.EntityDescriptions;
import dev.adventurecraft.awakening.script.ScopeTag;
import dev.adventurecraft.awakening.script.Script;
import dev.adventurecraft.awakening.script.ScriptModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.render.*;
import net.minecraft.client.renderer.ptexture.FireTexture;
import net.minecraft.client.renderer.ptexture.LavaSideTexture;
import net.minecraft.client.renderer.ptexture.LavaTexture;
import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.client.renderer.ptexture.WaterSideTexture;
import net.minecraft.client.renderer.ptexture.WaterTexture;
import net.minecraft.locale.I18n;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelListener;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.LightUpdate;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.chunk.ChunkCache;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelIO;
import net.minecraft.world.level.storage.McRegionLevelStorageSource;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.mozilla.javascript.Scriptable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

@Mixin(Level.class)
public abstract class MixinWorld implements ExWorld, LevelSource {

    @Shadow
    static int maxLoop;

    @Shadow
    public LevelData levelData;

    @Shadow
    public DimensionDataStorage dataStorage;

    @Shadow
    public boolean isFindingSpawn;

    @Shadow
    @Final
    @Mutable
    public Dimension dimension;

    @Shadow
    public Random random;

    @Shadow
    public ChunkSource chunkSource;

    @Shadow
    @Final
    @Mutable
    protected LevelIO levelIo;

    @Shadow
    public int skyDarken;

    @Shadow
    private ArrayList<AABB> boxes;

    @Shadow
    private Set<TickNextTickData> tickNextTickSet;

    @Shadow
    protected List<LevelListener> listeners;

    @Shadow
    public int saveInterval;

    @Shadow
    protected float oRainLevel;

    @Shadow
    protected float rainLevel;

    @Shadow
    protected float oThunderLevel;

    @Shadow
    protected float thunderLevel;

    @Shadow
    public List<Player> players;

    @Shadow
    public boolean isClientSide;

    @Shadow
    private int delayUntilNextMoodSound;

    @Shadow
    protected int randValue;

    @Shadow
    protected int lightingCooldown;

    @Shadow
    public boolean isNew;

    @Shadow
    private TreeSet<TickNextTickData> tickNextTickList;

    @Shadow
    public List<Entity> entities;

    @Shadow
    private List<LightUpdate> lightUpdates;

    @Shadow
    @Final
    @Mutable
    protected int addend;

    @Shadow
    private List entitiesToRemove;

    @Shadow
    public List tileEntityList;

    @Shadow
    private List pendingTileEntities;

    @Shadow
    public List globalEntities;

    @Shadow
    private long cloudColor;

    @Shadow
    private List es;

    @Shadow
    private boolean spawnFriendlies;

    @Shadow
    private boolean spawnEnemies;

    @Shadow
    private long sessionId;

    public File levelDir;
    private int[] coordOrder;
    public ArrayList<String> musicList = new ArrayList<>();
    public ArrayList<String> soundList = new ArrayList<>();
    private LevelIO mapHandler;
    public AC_TriggerManager triggerManager = new AC_TriggerManager((Level) (Object) this);
    public boolean fogColorOverridden;
    public boolean fogDensityOverridden;
    boolean firstTick = true;
    boolean newSave;
    public AC_UndoStack undoStack = new AC_UndoStack();
    private ArrayList<CollisionList> collisionDebugLists = new ArrayList<>();
    private ArrayList<AABB> rayCheckedBlocks = new ArrayList<>();
    private ArrayList<RayDebugList> rayDebugLists = new ArrayList<>();
    public Script script = new Script((Level) (Object) this);
    public AC_JScriptHandler scriptHandler;
    public AC_MusicScripts musicScripts;
    public Scriptable scope;

    @Shadow
    public abstract int getTile(int i, int j, int k);

    @Shadow
    public abstract boolean isEmptyTile(int i, int j, int k);

    @Shadow
    public abstract LevelChunk getChunk(int i, int j);

    @Shadow
    public abstract int getLightLevel(int i, int j, int k);

    @Shadow
    public abstract boolean hasChunkAt(int i, int j, int k);

    @Shadow
    public abstract boolean isSkyLit(int i, int j, int k);

    @Shadow
    public abstract int getBrightness(LightLayer arg, int i, int j, int k);

    @Shadow
    public abstract void updateLight(LightLayer arg, int i, int j, int k, int l, int m, int n);

    @Shadow
    public abstract LevelChunk getChunkAt(int i, int j);

    @Shadow
    public abstract boolean hasChunksAt(int i, int j, int k, int l, int m, int n);

    @Shadow
    protected abstract boolean hasChunk(int i, int j);

    @Shadow
    public abstract void tick(Entity arg);

    @Shadow
    public abstract void removeTileEntity(int i, int j, int k);

    @Shadow
    public abstract int getSkyDarken(float f);

    @Shadow
    public abstract void save(boolean bl, ProgressListener arg);

    @Shadow
    public abstract long getTime();

    @Shadow
    public abstract boolean isRaining();

    @Shadow
    public abstract boolean isThundering();

    @Shadow
    public abstract boolean isRainingAt(int i, int j, int k);

    @Shadow
    public abstract boolean addGlobalEntity(Entity arg);

    @Shadow
    public abstract boolean setTile(int i, int j, int k, int l);

    @Shadow
    public abstract void updateSkyBrightness();

    @Shadow
    protected abstract void setInitialSpawn();

    @Override
    public void initWorld(
        String mapName, LevelIO dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener) {
        this.addend = 1013904223;
        this.fogColorOverridden = false;
        this.fogDensityOverridden = false;
        this.firstTick = true;
        this.newSave = false;
        this.musicList = new ArrayList<>();
        this.soundList = new ArrayList<>();
        this.triggerManager = new AC_TriggerManager((Level) (Object) this);
        this.undoStack = new AC_UndoStack();
        this.collisionDebugLists = new ArrayList<>();
        this.rayCheckedBlocks = new ArrayList<>();
        this.rayDebugLists = new ArrayList<>();
        File gameDir = Minecraft.getWorkingDirectory();
        File mapsDir = ACMainThread.getMapsDirectory();
        File levelDir = new File(mapsDir, mapName);
        ((ExTranslationStorage) I18n.getInstance()).loadMapTranslation(levelDir);
        this.mapHandler = new McRegionLevelStorageSource(mapsDir, mapName, false);
        this.levelDir = levelDir;
        this.lightUpdates = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.entitiesToRemove = new ArrayList<>();
        this.tickNextTickList = new TreeSet<>();
        this.tickNextTickSet = new HashSet<>();
        this.tileEntityList = new ArrayList<>();
        this.pendingTileEntities = new ArrayList<>();
        this.players = new ArrayList<>();
        this.globalEntities = new ArrayList<>();
        this.cloudColor = 16777215L;
        this.randValue = (new Random()).nextInt();
        this.sessionId = System.currentTimeMillis();
        this.saveInterval = 40;
        this.random = new Random();
        this.listeners = new ArrayList<>();
        this.boxes = new ArrayList<>();
        this.spawnEnemies = true;
        this.spawnFriendlies = true;
        this.delayUntilNextMoodSound = this.random.nextInt(12000);
        this.es = new ArrayList<>();
        this.levelIo = dimData;
        if (dimData != null) {
            this.dataStorage = new DimensionDataStorage(dimData);
            this.levelData = dimData.getLevelData();
        } else {
            this.dataStorage = new DimensionDataStorage(this.mapHandler);
        }

        if (this.levelData == null) {
            this.newSave = true;
            this.levelData = this.mapHandler.getLevelData();
        }

        if (!AC_TerrainImage.loadMap(levelDir)) {
            AC_TerrainImage.loadMap(new File(new File(gameDir, "saves"), saveName));
        }

        this.isNew = this.levelData == null;
        if (dimension != null) {
            this.dimension = dimension;
        } else if (this.levelData != null && this.levelData.getDimension() == -1) {
            this.dimension = Dimension.getNew(-1);
        } else {
            this.dimension = Dimension.getNew(0);
        }

        boolean newProps = false;
        if (this.levelData == null) {
            this.levelData = new LevelData(seed, saveName);
            newProps = true;
        } else {
            this.levelData.setLevelName(saveName);
        }

        var props = (ExWorldProperties) this.levelData;
        // Load current hud status
        ((ExInGameHud)Minecraft.instance.gui).setHudEnabled(props.getHudEnabled());

        props.getWorldGenProps().useImages = AC_TerrainImage.isLoaded;
        if (props.getTriggerData() != null) {
            this.triggerManager.loadFromTagCompound(props.getTriggerData());
        }

        this.dimension.setLevel((Level) (Object) this);
        this.loadBrightness();
        this.chunkSource = this.createLevelSource();
        if (newProps) {
            this.setInitialSpawn();
            this.isFindingSpawn = true;
            int var11 = 0;

            int var12;
            for (var12 = 0; !this.dimension.isValidSpawn(var11, var12); var12 += this.random.nextInt(64) - this.random.nextInt(64)) {
                var11 += this.random.nextInt(64) - this.random.nextInt(64);
            }

            this.levelData.setSpawnXYZ(var11, this.getTopTile(var11, var12), var12);
            this.isFindingSpawn = false;
        }

        this.updateSkyBrightness();
        this.prepareWeather();

        this.loadMapMusic();
        this.loadMapSounds();

        this.script = new Script((Level) (Object) this);

        if (props.getGlobalScope() != null) {
            ScopeTag.loadScopeFromTag(this.script.globalScope, props.getGlobalScope());
        }

        this.scriptHandler = new AC_JScriptHandler((Level) (Object) this, levelDir);
        this.scriptHandler.loadScripts(progressListener);

        this.musicScripts = new AC_MusicScripts(this.script, levelDir, this.scriptHandler);
        if (props.getMusicScope() != null) {
            ScopeTag.loadScopeFromTag(this.musicScripts.scope, props.getMusicScope());
        }

        this.scope = this.script.getNewScope();
        if (props.getWorldScope() != null) {
            ScopeTag.loadScopeFromTag(this.scope, props.getWorldScope());
        }

        this.loadSoundOverrides();
        EntityDescriptions.loadDescriptions(new File(levelDir, "entitys"));
        AC_ItemCustom.loadItems(new File(levelDir, "items"));
        AC_TileEntityNpcPath.lastEntity = null;
    }

    @Override
    public void loadMapTextures() {
        var texManager = ((ExTextureManager) Minecraft.instance.textures);
        Minecraft.instance.textures.reloadAll();

        for (Object oEntry : Minecraft.instance.textures.idMap.entrySet()) {
            var entry = (Map.Entry<String, Integer>) oEntry;
            String name = entry.getKey();
            int id = entry.getValue();
            try {
                texManager.loadTexture(id, name);
            } catch (IllegalArgumentException ex) {
                ACMod.LOGGER.error("Failed to load texture \"{}\".", name, ex);
            }
        }

        this.loadTextureAnimations();
        Level world = (Level) (Object) this;
        AC_TextureBinder.loadImages(texManager, AC_TextureFanFX.class, world);
        AC_TextureBinder.loadImages(texManager, FireTexture.class, world);
        AC_TextureBinder.loadImages(texManager, LavaTexture.class, world);
        AC_TextureBinder.loadImages(texManager, LavaSideTexture.class, world);
        AC_TextureBinder.loadImages(texManager, PortalTexture.class, world);
        AC_TextureBinder.loadImages(texManager, WaterTexture.class, world);
        AC_TextureBinder.loadImages(texManager, WaterSideTexture.class, world);
        ExGrassColor.loadGrass("/misc/grasscolor.png", world);
        ExFoliageColor.loadFoliage("/misc/foliagecolor.png", world);
        ((ExWorldProperties) this.levelData).loadTextureReplacements(world);
    }

    private void loadTextureAnimations() {
        var texManager = ((ExTextureManager) Minecraft.instance.textures);
        texManager.clearTextureAnimations();

        var file = new File(this.levelDir, "animations.txt");
        if (!file.exists()) {
            return;
        }

        try {
            var reader = new BufferedReader(new FileReader(file));
            try {
                while (reader.ready()) {
                    String line = reader.readLine();
                    String[] elements = line.split(",", 7);
                    if (elements.length == 7) {
                        try {
                            String animName = elements[0].trim();
                            String texName = elements[1].trim();
                            String imageName = elements[2].trim();
                            int x = Integer.parseInt(elements[3].trim());
                            int y = Integer.parseInt(elements[4].trim());
                            int w = Integer.parseInt(elements[5].trim());
                            int h = Integer.parseInt(elements[6].trim());
                            var instance = new AC_TextureAnimated(texName, x, y, w, h);
                            //noinspection DataFlowIssue
                            ((AC_TextureBinder) instance).loadImage(imageName, (Level) (Object) this);
                            texManager.registerTextureAnimation(animName, instance);
                        } catch (Exception var12) {
                            var12.printStackTrace();
                        }
                    }
                }
            } catch (IOException var13) {
                var13.printStackTrace();
            }
        } catch (FileNotFoundException var14) {
            var14.printStackTrace();
        }
    }

    @Override
    public BufferedImage loadMapTexture(String name) {
        var file = new File(this.levelDir, name);
        if (file.exists()) {
            try {
                BufferedImage image = ImageIO.read(file);
                return image;
            } catch (Exception var4) {
            }
        }
        return null;
    }

    @Override
    public void updateChunkProvider() {
        this.chunkSource = this.createLevelSource();
    }

    @Overwrite
    public ChunkSource createLevelSource() {
        ChunkStorage io;
        if (this.levelIo == null) {
            io = this.mapHandler.readDimension(this.dimension);
        } else {
            io = this.levelIo.readDimension(this.dimension);
            if (this.mapHandler != null) {
                io = new MapChunkLoader(this.mapHandler.readDimension(this.dimension), io);
            }
        }

        try {
            var cache = (ChunkCache) ACMod.UNSAFE.allocateInstance(ChunkCache.class);
            ((ExChunkCache) cache).init((Level) (Object) this, io, this.dimension.createRandomLevelSource());
            return cache;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Redirect(method = "setInitialSpawn", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/storage/LevelData;setSpawnXYZ(III)V"))
    private void spawnAtUncoveredBlock(LevelData instance, int x, int y, int z) {
        this.levelData.setSpawnXYZ(x, this.getFirstUncoveredBlockY(x, z), z);
    }

    @Inject(method = "validateSpawn", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/storage/LevelData;setSpawnZ(I)V",
        shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void spawnAtUncoveredBlock(CallbackInfo ci, int x, int z) {
        this.levelData.setSpawnY(this.getFirstUncoveredBlockY(x, z));
    }

    public int getFirstUncoveredBlockY(int x, int z) {
        int y = 127;
        while (this.isEmptyTile(x, y, z) && y > 0) {
            --y;
        }
        return y;
    }

    @Overwrite
    public int getTopTile(int x, int z) {
        int y = this.getFirstUncoveredBlockY(x, z);
        return this.getTile(x, y, z);
    }

    @Redirect(method = "saveLevel", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/storage/LevelIO;saveWithPlayers(Lnet/minecraft/world/level/storage/LevelData;Ljava/util/List;)V"))
    private void modifySave(LevelIO instance, LevelData worldProperties, List<Player> list) {
        var exProps = (ExWorldProperties) worldProperties;
        exProps.setGlobalScope(ScopeTag.getTagFromScope(this.script.globalScope));
        exProps.setWorldScope(ScopeTag.getTagFromScope(this.scope));
        exProps.setMusicScope(ScopeTag.getTagFromScope(this.musicScripts.scope));

        if (this.levelIo != null) {
            this.levelIo.saveWithPlayers(worldProperties, list);
        }

        if (AC_DebugMode.levelEditing || this.levelIo == null) {
            this.mapHandler.saveWithPlayers(worldProperties, list);
        }
    }

    @Override
    public boolean setBlockAndMetadataTemp(int x, int y, int z, int id, int meta) {
        if (x < -32000000 || z < -32000000 || x >= 32000000 || z > 32000000) {
            return false;
        }
        if (y < 0) {
            return false;
        } else if (y >= 128) {
            return false;
        } else {
            LevelChunk chunk = this.getChunk(x >> 4, z >> 4);
            return ((ExChunk) chunk).setBlockIDWithMetadataTemp(x & 15, y, z & 15, id, meta);
        }
    }

    @Overwrite
    public int getRawBrightness(int x, int y, int z, boolean var4) {
        if (x < -32000000 || z < -32000000 || x >= 32000000 || z > 32000000) {
            return 15;
        }

        if (var4) {
            int id = this.getTile(x, y, z);
            if (id != 0 && (id == Tile.SLAB.id || id == Tile.FARMLAND.id || id == Tile.COBBLESTONE_STAIRS.id || id == Tile.WOOD_STAIRS.id || Tile.tiles[id] instanceof AC_BlockStairMulti)) {
                int topId = this.getRawBrightness(x, y + 1, z, false);
                int rightId = this.getRawBrightness(x + 1, y, z, false);
                int leftId = this.getRawBrightness(x - 1, y, z, false);
                int frontId = this.getRawBrightness(x, y, z + 1, false);
                int backId = this.getRawBrightness(x, y, z - 1, false);
                if (rightId > topId) {
                    topId = rightId;
                }

                if (leftId > topId) {
                    topId = leftId;
                }

                if (frontId > topId) {
                    topId = frontId;
                }

                if (backId > topId) {
                    topId = backId;
                }

                return topId;
            }
        }

        if (y < 0) {
            return 0;
        }

        if (y >= 128) {
            y = 127;
        }

        LevelChunk chunk = this.getChunk(x >> 4, z >> 4);
        x &= 15;
        z &= 15;
        return chunk.getRawBrightness(x, y, z, this.skyDarken);
    }

    @Overwrite
    public void updateLightIfOtherThan(LightLayer lightType, int x, int y, int z, int value) {
        if (this.dimension.hasCeiling && lightType == LightLayer.SKY) {
            return;
        }
        if (this.hasChunkAt(x, y, z)) {
            if (lightType == LightLayer.SKY) {
                if (this.isSkyLit(x, y, z)) {
                    value = 15;
                }
            } else if (lightType == LightLayer.BLOCK) {
                int var6 = this.getTile(x, y, z);
                if (Tile.tiles[var6] != null && ((ExBlock) Tile.tiles[var6]).getBlockLightValue(this, x, y, z) < value) {
                    value = ((ExBlock) Tile.tiles[var6]).getBlockLightValue(this, x, y, z);
                }
            }

            if (this.getBrightness(lightType, x, y, z) != value) {
                this.updateLight(lightType, x, y, z, x, y, z);
            }
        }
    }

    @Override
    public float getLightValue(int x, int y, int z) {
        int var4 = this.getLightLevel(x, y, z);
        float var5 = AC_PlayerTorch.getTorchLight((Level) (Object) this, x, y, z);
        return (float) var4 < var5 ? Math.min(var5, 15.0F) : (float) var4;
    }

    private float getBrightnessLevel(float var1) {
        int var2 = (int) Math.floor(var1);
        if ((float) var2 != var1) {
            int var3 = (int) Math.ceil(var1);
            float var4 = var1 - (float) var2;
            return (1.0F - var4) * this.dimension.brightnessRamp[var2] + var4 * this.dimension.brightnessRamp[var3];
        } else {
            return this.dimension.brightnessRamp[var2];
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getBrightness(int x, int y, int z, int var4) {
        float var5 = this.getLightValue(x, y, z);
        if (var5 < (float) var4) {
            var5 = (float) var4;
        }

        return this.getBrightnessLevel(var5);
    }

    @Overwrite
    public float getBrightness(int x, int y, int z) {
        float var4 = this.getLightValue(x, y, z);
        return this.getBrightnessLevel(var4);
    }

    public float getDayLight() {
        int var1 = 15 - this.skyDarken;
        return this.dimension.brightnessRamp[var1];
    }

    @Overwrite
    public HitResult clip(Vec3 pointA, Vec3 pointB, boolean var3, boolean var4) {
        return this.rayTraceBlocks2(pointA, pointB, var3, var4, true);
    }

    @Override
    public HitResult rayTraceBlocks2(Vec3 pointA, Vec3 pointB, boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip) {
        if (Double.isNaN(pointA.x) || Double.isNaN(pointA.y) || Double.isNaN(pointA.z)) {
            return null;
        }
        if (Double.isNaN(pointB.x) || Double.isNaN(pointB.y) || Double.isNaN(pointB.z)) {
            return null;
        }

        // Copy coords because pointA is mutated by method.
        double paX = pointA.x;
        double paY = pointA.y;
        double paZ = pointA.z;

        HitResult hit = this.rayTraceBlocksCore(
            pointA, pointB, blockCollidableFlag, useCollisionShapes, collideWithClip);

        if (AC_DebugMode.renderRays) {
            this.recordRayDebugList(paX, paY, paZ, pointB.x, pointB.y, pointB.z, hit);
        }
        return hit;
    }

    @Override
    public void recordRayDebugList(
        double aX, double aY, double aZ, double bX, double bY, double bZ, HitResult hit) {

        var blocksCollisionsArray = saveAsDoubleArray(this.rayCheckedBlocks);
        this.rayCheckedBlocks.clear();

        var list = new RayDebugList(aX, aY, aZ, bX, bY, bZ, blocksCollisionsArray, hit);
        this.rayDebugLists.add(list);
    }

    @Override
    public HitResult rayTraceBlocksCore(Vec3 pointA, Vec3 pointB, boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip) {
        int bX = Mth.floor(pointB.x);
        int bY = Mth.floor(pointB.y);
        int bZ = Mth.floor(pointB.z);
        int aX = Mth.floor(pointA.x);
        int aY = Mth.floor(pointA.y);
        int aZ = Mth.floor(pointA.z);
        int aId = this.getTile(aX, aY, aZ);
        Tile aBlock = Tile.tiles[aId];
        AABB aAabb = null;
        if (aBlock != null &&
            (!useCollisionShapes || (aAabb = aBlock.getAABB((Level) (Object) this, aX, aY, aZ)) != null) &&
            (aId > 0 && (collideWithClip || aId != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(aId)) && aBlock.mayPick(this.getData(aX, aY, aZ), blockCollidableFlag))) {

            if (aAabb != null && AC_DebugMode.renderRays) {
                this.rayCheckedBlocks.add(aAabb);
            }

            HitResult hit = aBlock.clip((Level) (Object) this, aX, aY, aZ, pointA, pointB);
            if (hit != null) {
                return hit;
            }
        }

        int stepsLeft = 200;

        while (stepsLeft-- >= 0) {
            if (Double.isNaN(pointA.x) || Double.isNaN(pointA.y) || Double.isNaN(pointA.z)) {
                return null;
            }
            if (aX == bX && aY == bY && aZ == bZ) {
                return null;
            }

            boolean moveX = true;
            boolean moveY = true;
            boolean moveZ = true;
            double startX = 999.0D;
            double startY = 999.0D;
            double startZ = 999.0D;
            if (bX > aX) {
                startX = (double) aX + 1.0D;
            } else if (bX < aX) {
                startX = (double) aX + 0.0D;
            } else {
                moveX = false;
            }

            if (bY > aY) {
                startY = (double) aY + 1.0D;
            } else if (bY < aY) {
                startY = (double) aY + 0.0D;
            } else {
                moveY = false;
            }

            if (bZ > aZ) {
                startZ = (double) aZ + 1.0D;
            } else if (bZ < aZ) {
                startZ = (double) aZ + 0.0D;
            } else {
                moveZ = false;
            }

            double distX = 999.0D;
            double distY = 999.0D;
            double distZ = 999.0D;
            double deltaX = pointB.x - pointA.x;
            double deltaY = pointB.y - pointA.y;
            double deltaZ = pointB.z - pointA.z;
            if (moveX) {
                distX = (startX - pointA.x) / deltaX;
            }

            if (moveY) {
                distY = (startY - pointA.y) / deltaY;
            }

            if (moveZ) {
                distZ = (startZ - pointA.z) / deltaZ;
            }

            int side;
            if (distX < distY && distX < distZ) {
                if (bX > aX) {
                    side = 4;
                } else {
                    side = 5;
                }

                pointA.x = startX;
                pointA.y += deltaY * distX;
                pointA.z += deltaZ * distX;
            } else if (distY < distZ) {
                if (bY > aY) {
                    side = 0;
                } else {
                    side = 1;
                }

                pointA.x += deltaX * distY;
                pointA.y = startY;
                pointA.z += deltaZ * distY;
            } else {
                if (bZ > aZ) {
                    side = 2;
                } else {
                    side = 3;
                }

                pointA.x += deltaX * distZ;
                pointA.y += deltaY * distZ;
                pointA.z = startZ;
            }

            aX = Mth.floor(pointA.x);
            if (side == 5) {
                --aX;
            }

            aY = Mth.floor(pointA.y);
            if (side == 1) {
                --aY;
            }

            aZ = Mth.floor(pointA.z);
            if (side == 3) {
                --aZ;
            }

            int id = this.getTile(aX, aY, aZ);
            Tile block = Tile.tiles[id];
            AABB aabb = null;
            if (block != null &&
                (!useCollisionShapes || (aabb = block.getAABB((Level) (Object) this, aX, aY, aZ)) != null) &&
                id != 0 && block.mayPick(this.getData(aX, aY, aZ), blockCollidableFlag) && ((ExBlock) block).shouldRender(this, aX, aY, aZ)) {

                if (aabb != null && AC_DebugMode.renderRays) {
                    this.rayCheckedBlocks.add(aabb);
                }

                HitResult hit = block.clip((Level) (Object) this, aX, aY, aZ, pointA, pointB);
                if (hit != null && (collideWithClip || (block.id != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(block.id)))) {
                    return hit;
                }
            }
        }
        return null;
    }

    @Redirect(method = "addEntity", at = @At(
        value = "INVOKE",
        target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
        ordinal = 1))
    private <E> boolean spawnIfNotExisting(List<E> instance, E entity) {
        if (!instance.contains(entity)) {
            return instance.add(entity);
        }
        return false;
    }

    @Redirect(
        method = "getCubes",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/tile/Tile;addAABBs(Lnet/minecraft/world/level/Level;IIILnet/minecraft/world/phys/AABB;Ljava/util/ArrayList;)V"))
    private void guardBoxCollide(Tile block, Level world, int x, int y, int z, AABB aabb, ArrayList<AABB> output, @Local(argsOnly = true) Entity entity) {
        if ((((ExEntity) entity).getCollidesWithClipBlocks() || block.id != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(block.id))) {
            block.addAABBs(world, x, y, z, aabb, output);
        }
    }

    @Inject(
        method = "getCubes",
        at = @At("RETURN"))
    private void recordCollision(Entity entity, AABB aabb, CallbackInfoReturnable<List> cir) {
        if (!AC_DebugMode.renderCollisions) {
            return;
        }

        var collisionsArray = saveAsDoubleArray((List<AABB>) cir.getReturnValue());
        this.collisionDebugLists.add(new CollisionList(entity, aabb, collisionsArray));
    }

    private static double[] saveAsDoubleArray(List<AABB> boxList) {
        int size = boxList.size();
        if (size == 0) {
            return null;
        }

        var boxArray = new double[size * 6];
        for (int i = 0; i < size; i++) {
            AABB box = boxList.get(i);
            boxArray[i * 6 + 0] = box.x0;
            boxArray[i * 6 + 1] = box.y0;
            boxArray[i * 6 + 2] = box.z0;
            boxArray[i * 6 + 3] = box.x1;
            boxArray[i * 6 + 4] = box.y1;
            boxArray[i * 6 + 5] = box.z1;
        }
        return boxArray;
    }

    @Overwrite
    public float getTimeOfDay(float var1) {
        ExWorldProperties props = (ExWorldProperties) this.levelData;
        return this.dimension.getTimeOfDay(props.getTimeOfDay(), var1 * props.getTimeRate());
    }

    @Inject(method = "getFogColor", at = @At("RETURN"))
    private void changeFogColor(float var1, CallbackInfoReturnable<Vec3> cir) {
        Vec3 var3 = cir.getReturnValue();
        ExWorldProperties props = (ExWorldProperties) this.levelData;
        if (props.isOverrideFogColor()) {
            if (this.fogColorOverridden) {
                var3.x = props.getFogR();
                var3.y = props.getFogG();
                var3.z = props.getFogB();
            } else {
                var3.x = (double) (1.0F - var1) * var3.x + (double) (var1 * props.getFogR());
                var3.y = (double) (1.0F - var1) * var3.y + (double) (var1 * props.getFogG());
                var3.z = (double) (1.0F - var1) * var3.z + (double) (var1 * props.getFogB());
            }
        }
    }

    @Overwrite
    public int getTopSolidBlock(int x, int z) {
        LevelChunk chunk = this.getChunkAt(x, z);

        int y = 127;
        while (this.getMaterial(x, y, z).blocksMotion() && y > 0) {
            --y;
        }

        x &= 15;
        z &= 15;

        while (y > 0) {
            int id = chunk.getTile(x, y, z);
            Material mat = id != 0 ? Tile.tiles[id].material : Material.AIR;
            if (mat.blocksMotion() || mat.isLiquid()) {
                return y + 1;
            }
            --y;
        }

        return -1;
    }

    @Inject(
        method = "tickEntities",
        at = @At("HEAD"))
    private void clearCollisionList(CallbackInfo ci) {
        this.collisionDebugLists.clear();
    }

    // This injection will be inverted at the target since the expression only captured the field access
    @ModifyExpressionValue(
        method = "tickEntities",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;removed:Z",
            ordinal = 2))
    private boolean fixupRemoveCondition(boolean value, @Local Entity entity) {
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        return !(!entity.removed && (!mc.isCameraActive() || !mc.isCameraPause()) && (!AC_DebugMode.active || entity instanceof Player));
    }

    @Inject(
        method = "tickEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;tick(Lnet/minecraft/world/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void fixupBoundingBox(CallbackInfo ci) {
        AABB.resetPool();
    }

    @Redirect(
        method = "tickEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getChunk(II)Lnet/minecraft/world/level/chunk/LevelChunk;"))
    private LevelChunk ignoreIfKilledOnSave(Level instance, int j, int i, @Local TileEntity var5) {
        if (((ExBlockEntity) var5).isKilledFromSaving()) {
            return null;
        }
        LevelChunk chunk = this.getChunk(var5.x >> 4, var5.z >> 4);
        return chunk;
    }

    @Overwrite
    public void tick(Entity entity, boolean var2) {
        int eX = Mth.floor(entity.x);
        int eZ = Mth.floor(entity.z);
        int var5 = 32;
        if (!var2 || this.hasChunksAt(eX - var5, 0, eZ - var5, eX + var5, 128, eZ + var5)) {
            entity.xOld = entity.x;
            entity.yOld = entity.y;
            entity.zOld = entity.z;
            entity.yRotO = entity.yRot;
            entity.xRotO = entity.xRot;
            if (var2 && entity.inChunk) {
                int stunned = ((ExEntity) entity).getStunned();
                if (stunned > 0) {
                    ((ExEntity) entity).setStunned(stunned - 1);
                } else if (entity.riding != null) {
                    entity.rideTick();
                } else {
                    entity.tick();
                }
            }

            if (Double.isNaN(entity.x) || Double.isInfinite(entity.x)) {
                entity.x = entity.xOld;
            }

            if (Double.isNaN(entity.y) || Double.isInfinite(entity.y)) {
                entity.y = entity.yOld;
            }

            if (Double.isNaN(entity.z) || Double.isInfinite(entity.z)) {
                entity.z = entity.zOld;
            }

            if (Double.isNaN(entity.xRot) || Double.isInfinite(entity.xRot)) {
                entity.xRot = entity.xRotO;
            }

            if (Double.isNaN(entity.yRot) || Double.isInfinite(entity.yRot)) {
                entity.yRot = entity.yRotO;
            }

            int ecX = Mth.floor(entity.x / 16.0D);
            int ecY = Mth.floor(entity.y / 16.0D);
            int ecZ = Mth.floor(entity.z / 16.0D);
            if (!entity.inChunk || entity.xChunk != ecX || entity.yChunk != ecY || entity.zChunk != ecZ) {
                if (entity.inChunk && this.hasChunk(entity.xChunk, entity.zChunk)) {
                    this.getChunk(entity.xChunk, entity.zChunk).removeEntity(entity, entity.yChunk);
                }

                if (this.hasChunk(ecX, ecZ)) {
                    entity.inChunk = true;
                    this.getChunk(ecX, ecZ).addEntity(entity);
                } else {
                    entity.inChunk = false;
                }
            }

            if (var2 && entity.inChunk && entity.rider != null) {
                if (!entity.rider.removed && entity.rider.riding == entity) {
                    this.tick(entity.rider);
                } else {
                    entity.rider.riding = null;
                    entity.rider = null;
                }
            }
        }
    }

    @Inject(method = "setTileEntity", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/level/Level;updatingTileEntities:Z",
        shift = At.Shift.BEFORE))
    private void removeBlockEntityOnSet(int x, int y, int z, TileEntity var4, CallbackInfo ci) {
        this.removeTileEntity(x, y, z);
    }

    @Redirect(method = "removeTileEntity", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/Level;getTileEntity(III)Lnet/minecraft/world/level/tile/entity/TileEntity;"))
    private TileEntity removeBlockEntityDontCreate(Level instance, int x, int y, int z) {
        return this.getBlockTileEntityDontCreate(x, y, z);
    }

    @Overwrite
    public void updateLight(LightLayer lightType, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        if (this.dimension.hasCeiling && lightType == LightLayer.SKY) {
            return;
        }
        ++maxLoop;

        try {
            if (maxLoop == 50) {
                return;
            }

            int x = (var5 + var2) / 2;
            int z = (var7 + var4) / 2;
            if (!this.hasChunkAt(x, 64, z)) {
                return;
            }

            if (this.getChunkAt(x, z).isEmpty()) {
                return;
            }

            if (var8) {
                int count = Math.min(this.lightUpdates.size(), 5);
                for (int i = 0; i < count; ++i) {
                    LightUpdate var14 = this.lightUpdates.get(this.lightUpdates.size() - i - 1);
                    if (var14.type == lightType && var14.expandToContain(var2, var3, var4, var5, var6, var7)) {
                        return;
                    }
                }
            }

            this.lightUpdates.add(new LightUpdate(lightType, var2, var3, var4, var5, var6, var7));

            final int maxUpdates = 1000000;
            if (this.lightUpdates.size() > maxUpdates) {
                System.out.println("More than " + maxUpdates + " updates, aborting lighting updates");
                this.lightUpdates.clear();
            }
        } finally {
            --maxLoop;
        }
    }

    @Override
    public void ac$preTick() {
        if (this.firstTick) {
            var props = (ExWorldProperties) this.levelData;

            if (this.newSave && !props.getOnNewSaveScript().equals("")) {
                this.scriptHandler.runScript(props.getOnNewSaveScript(), this.scope);
            }

            if (!props.getOnLoadScript().equals("")) {
                this.scriptHandler.runScript(props.getOnLoadScript(), this.scope);
            }

            this.firstTick = false;
        }
    }

    @Overwrite
    public void tick() {
        var props = (ExWorldProperties) this.levelData;

        ScriptModel.updateAll();
        if (!props.getOnUpdateScript().equals("")) {
            this.scriptHandler.runScript(props.getOnUpdateScript(), this.scope);
        }

        this.fogColorOverridden = props.isOverrideFogColor();
        this.fogDensityOverridden = props.isOverrideFogDensity();
        this.tickWeather();
        this.chunkSource.tick();
        int var1 = this.getSkyDarken(1.0F);
        if (var1 != this.skyDarken) {
            this.skyDarken = var1;

            for (LevelListener worldListener : this.listeners) {
                worldListener.skyColorChanged();
            }
        }

        long var4 = this.levelData.getTime() + 1L;
        if (var4 % (long) this.saveInterval == 0L) {
            this.save(false, null);
        }

        this.levelData.setTime(var4);
        props.addToTimeOfDay(props.getTimeRate());
        this.tickPendingTicks(false);
        this.tickTiles();
        if (this.levelData.isRaining()) {
            this.DoSnowModUpdate();
        }

        this.script.wakeupScripts(var4);
    }

    @Overwrite
    private void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0F;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0F;
            }
        }
    }

    @Overwrite
    public void tickWeather() {
        if (this.dimension.hasCeiling) {
            return;
        }

        if (this.lightingCooldown > 0) {
            --this.lightingCooldown;
        }

        this.oRainLevel = this.rainLevel;
        if (this.levelData.isRaining()) {
            this.rainLevel = (float) ((double) this.rainLevel + 0.01D);
        } else {
            this.rainLevel = (float) ((double) this.rainLevel - 0.01D);
        }

        if (this.rainLevel < 0.0F) {
            this.rainLevel = 0.0F;
        }

        if (this.rainLevel > 1.0F) {
            this.rainLevel = 1.0F;
        }

        this.oThunderLevel = this.thunderLevel;
        if (this.levelData.isThundering()) {
            this.thunderLevel = (float) ((double) this.thunderLevel + 0.01D);
        } else {
            this.thunderLevel = (float) ((double) this.thunderLevel - 0.01D);
        }

        if (this.thunderLevel < 0.0F) {
            this.thunderLevel = 0.0F;
        }

        if (this.thunderLevel > 1.0F) {
            this.thunderLevel = 1.0F;
        }
    }

    @Overwrite
    public void tickTiles() {
        for (Player var2 : this.players) {
            int var3 = Mth.floor(var2.x / 16.0D);
            int var4 = Mth.floor(var2.z / 16.0D);
            byte var5 = 9;

            for (int var6 = -var5; var6 <= var5; ++var6) {
                for (int var7 = -var5; var7 <= var5; ++var7) {
                    this.updateChunk(var6 + var3, var7 + var4);
                }
            }
        }

        if (this.delayUntilNextMoodSound > 0) {
            --this.delayUntilNextMoodSound;
        }
    }

    protected void updateChunk(int var1, int var2) {
        LevelChunk var3 = this.getChunk(var1, var2);
        if (((ExChunk) var3).getLastUpdated() == this.getTime()) {
            return;
        }

        int var4 = var1 * 16;
        int var5 = var2 * 16;
        ((ExChunk) var3).setLastUpdated(this.getTime());
        int var6;
        int var7;
        int var8;
        int var9;
        if (this.random.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
            this.randValue = this.randValue * 3 + 1013904223;
            var6 = this.randValue >> 2;
            var7 = var4 + (var6 & 15);
            var8 = var5 + (var6 >> 8 & 15);
            var9 = this.getTopSolidBlock(var7, var8);
            if (this.isRainingAt(var7, var9, var8)) {
                this.addGlobalEntity(new LightningBolt((Level) (Object) this, var7, var9, var8));
                this.lightingCooldown = 2;
            }
        }

        for (var6 = 0; var6 < 80; ++var6) {
            this.randValue = this.randValue * 3 + 1013904223;
            var7 = this.randValue >> 2;
            var8 = var7 & 15;
            var9 = var7 >> 8 & 15;
            int var10 = var7 >> 16 & 127;
            int var11 = var3.blocks[var8 << 11 | var9 << 7 | var10] & 255;
            if (Tile.shouldTick[var11]) {
                Tile.tiles[var11].tick((Level) (Object) this, var8 + var4, var10, var9 + var5, this.random);
            }
        }
    }

    @Overwrite
    public boolean tickPendingTicks(boolean var1) {
        int var2 = this.tickNextTickList.size();
        if (var2 > 1000) {
            var2 = 1000;
        }

        for (int var3 = 0; var3 < var2; ++var3) {
            TickNextTickData var4 = this.tickNextTickList.first();
            if (!var1 && var4.delay > this.levelData.getTime()) {
                break;
            }

            this.tickNextTickList.remove(var4);
            if (this.tickNextTickSet.remove(var4)) {
                byte var5 = 8;
                if (this.hasChunksAt(var4.x - var5, var4.y - var5, var4.z - var5, var4.x + var5, var4.y + var5, var4.z + var5)) {
                    int var6 = this.getTile(var4.x, var4.y, var4.z);
                    if (var6 == var4.priority && var6 > 0) {
                        Tile.tiles[var6].tick((Level) (Object) this, var4.x, var4.y, var4.z, this.random);
                        AABB.resetPool();
                    }
                }
            }
        }

        return this.tickNextTickList.size() != 0;
    }

    @Overwrite
    public void checkSession() {
        if (this.levelIo != null) {
            this.levelIo.checkSession();
        } else {
            this.mapHandler.checkSession();
        }
    }

    private void DoSnowModUpdate() {
        if (this.isClientSide) {
            return;
        }

        if (this.coordOrder == null) {
            this.initCoordOrder();
        }

        for (Player var3 : this.players) {
            int var4 = Mth.floor(var3.x / 16.0D);
            int var5 = Mth.floor(var3.z / 16.0D);
            byte var6 = 9;

            for (int var7 = -var6; var7 <= var6; ++var7) {
                for (int var8 = -var6; var8 <= var6; ++var8) {
                    long var9 = (long) (var7 + var8 * 2) + this.getTime();
                    if (var9 % 14L == 0L && this.hasChunk(var7 + var4, var8 + var5)) {
                        var9 /= 14L;
                        int var11 = var7 + var4;
                        int var12 = var8 + var5;
                        var9 += var11 * var11 * 3121 + var11 * 45238971 + var12 * var12 * 418711 + var12 * 13761;
                        var9 = Math.abs(var9);
                        int var13 = var11 * 16 + this.coordOrder[(int) (var9 % 256L)] % 16;
                        int var14 = var12 * 16 + this.coordOrder[(int) (var9 % 256L)] / 16;
                        this.SnowModUpdate(var13, var14);
                    }
                }
            }
        }
    }

    public boolean SnowModUpdate(int var1, int var2) {
        int var3 = this.getTopSolidBlock(var1, var2);
        if (var3 < 0) {
            var3 = 0;
        }

        int var4 = this.getTile(var1, var3 - 1, var2);
        if (this.getTemperatureValue(var1, var2) < 0.5D) {
            if (!this.isEmptyTile(var1, var3, var2)) {
                return false;
            } else {
                if (var4 != 0 && Tile.tiles[var4].isSolidRender()) {
                    if (!this.getMaterial(var1, var3 - 1, var2).blocksMotion()) {
                        return false;
                    }

                    if (this.getBrightness(LightLayer.BLOCK, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setTile(var1, var3, var2, Tile.SNOW_LAYER.id);
                } else if (var4 == Tile.FLOWING_WATER.id && this.getData(var1, var3 - 1, var2) == 0) {
                    if (this.getBrightness(LightLayer.BLOCK, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setTile(var1, var3 - 1, var2, Tile.ICE.id);
                }

                return true;
            }
        } else {
            int var5 = this.getTile(var1, var3, var2);
            if (var5 == Tile.SNOW_LAYER.id) {
                this.setTile(var1, var3, var2, 0);
                return true;
            } else if (var4 == Tile.SNOW.id) {
                this.setTile(var1, var3 - 1, var2, Tile.SNOW_LAYER.id);
                return true;
            } else if (var4 == Tile.ICE.id) {
                this.setTile(var1, var3 - 1, var2, Tile.FLOWING_WATER.id);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void cancelBlockUpdate(int x, int y, int z, int id) {
        TickNextTickData entry = new TickNextTickData(x, y, z, id);
        this.tickNextTickSet.remove(entry);
    }

    @Override
    public void loadMapMusic() {
        this.musicList.clear();

        File musicDir = new File(this.levelDir, "music");
        File[] files = musicDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".ogg")) {
                    String fileName = file.getName().toLowerCase();
                    String name = String.format("music/%s", fileName);
                    Minecraft.instance.soundEngine.addStreaming(name, file);

                    String musicName = String.format("music.%s", fileName.replace(".ogg", ""));
                    this.musicList.add(musicName);
                }
            }
        }

        String playingMusic = ((ExWorldProperties) this.levelData).getPlayingMusic();
        if (!playingMusic.equals("")) {
            ((ExSoundHelper) Minecraft.instance.soundEngine).playMusicFromStreaming(playingMusic, 0, 0);
        }
    }

    public void loadMapSounds() {
        this.soundList.clear();

        File soundDir = new File(this.levelDir, "sound");
        File[] files = soundDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".ogg")) {
                String fileName = file.getName().toLowerCase();
                String name = String.format("sound/%s", fileName);
                Minecraft.instance.soundEngine.add(name, file);

                String soundName = String.format("sound.%s", fileName.replace(".ogg", ""));
                this.soundList.add(soundName);
            }
        }
    }

    public void loadSoundOverrides() {
        // TODO: unload sounds?

        Minecraft.instance.bgLoader.forceReload();
        File var1 = new File(this.levelDir, "soundOverrides");
        if (var1.exists()) {
            Minecraft.instance.bgLoader.loadAll(var1, "");
        }
    }

    @Override
    public void loadBrightness() {
        // TODO: add brightness control per dimension
        float[] dimBrightness = ((ExWorldProperties) this.levelData).getBrightness().clone();
        float[] lightTable = new float[dimBrightness.length];
        float baseValue = 0.05F; // TODO: based on dimension

        float ofBrightness = ((ExGameOptions) Minecraft.instance.options).ofBrightness();
        float factor = 3.0F * (1.0F - ofBrightness);

        for (int i = 0; i < lightTable.length; ++i) {
            float original = LightHelper.solveLightValue(dimBrightness[i], 3.0F, 0.05F);
            lightTable[i] = LightHelper.calculateLight(original, factor, baseValue);
        }

        AoHelper.setLightLevels(lightTable[0], lightTable[1]);

        System.arraycopy(lightTable, 0, this.dimension.brightnessRamp, 0, lightTable.length);
    }

    @Override
    public Entity getEntityByID(int id) {
        for (Entity entity : this.entities) {
            if (entity.id == id) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public float getFogStart(float start, float deltaTime) {
        var props = (ExWorldProperties) this.levelData;
        if (props.isOverrideFogDensity()) {
            if (this.fogDensityOverridden) {
                return props.getFogStart();
            }
            return deltaTime * props.getFogStart() + (1.0F - deltaTime) * start;
        }
        return start;
    }

    @Override
    public float getFogEnd(float end, float deltaTime) {
        var props = (ExWorldProperties) this.levelData;
        if (props.isOverrideFogDensity()) {
            if (this.fogDensityOverridden) {
                return props.getFogEnd();
            }
            return deltaTime * props.getFogEnd() + (1.0F - deltaTime) * end;
        }
        return end;
    }

    @Override
    public TileEntity getBlockTileEntityDontCreate(int x, int y, int z) {
        LevelChunk chunk = this.getChunk(x >> 4, z >> 4);
        if (chunk != null) {
            return ((ExChunk) chunk).getChunkBlockTileEntityDontCreate(x & 15, y, z & 15);
        }
        return null;
    }

    @Override
    public double getTemperatureValue(int x, int z) {
        if (x >= -32000000 && z >= -32000000 && x < 32000000 && z <= 32000000) {
            var chunk = (ExChunk) this.getChunk(x >> 4, z >> 4);
            double tempValue = chunk.getTemperatureValue(x & 15, z & 15);
            double tempOffset = ((ExWorldProperties) this.levelData).getTempOffset();
            return tempValue + tempOffset;
        }
        return 0.0D;
    }

    @Override
    public void setTemperatureValue(int x, int z, double value) {
        if (x >= -32000000 && z >= -32000000 && x < 32000000 && z <= 32000000) {
            var chunk = (ExChunk) this.getChunk(x >> 4, z >> 4);
            if (chunk.getTemperatureValue(x & 15, z & 15) != value) {
                chunk.setTemperatureValue(x & 15, z & 15, value);
            }
        }
    }

    @Override
    public void resetCoordOrder() {
        this.coordOrder = null;
    }

    private void initCoordOrder() {
        Random var1 = new Random();
        var1.setSeed(this.getTime());
        this.coordOrder = new int[256];

        int var2 = 0;
        while (var2 < 256) {
            this.coordOrder[var2] = var2++;
        }

        for (var2 = 0; var2 < 255; ++var2) {
            int var3 = var1.nextInt(256 - var2);
            int var4 = this.coordOrder[var2];
            this.coordOrder[var2] = this.coordOrder[var2 + var3];
            this.coordOrder[var2 + var3] = var4;
        }
    }

    @Override
    public File getLevelDir() {
        return this.levelDir;
    }

    @Override
    public String[] getScriptFiles() {
        return this.scriptHandler.getFileNames();
    }

    @Override
    public float getTimeOfDay() {
        return (float) ((ExWorldProperties) this.levelData).getTimeOfDay();
    }

    @Override
    public void setTimeOfDay(long value) {
        ((ExWorldProperties) this.levelData).setTimeOfDay((float) value);
    }

    @Override
    public float getSpawnYaw() {
        return ((ExWorldProperties) this.levelData).getSpawnYaw();
    }

    @Override
    public void setSpawnYaw(float value) {
        ((ExWorldProperties) this.levelData).setSpawnYaw(value);
    }

    @Override
    public AC_UndoStack getUndoStack() {
        return this.undoStack;
    }

    @Override
    public ArrayList<String> getMusicList() {
        return this.musicList;
    }

    @Override
    public ArrayList<String> getSoundList() {
        return this.soundList;
    }

    @Override
    public AC_TriggerManager getTriggerManager() {
        return this.triggerManager;
    }

    @Override
    public Script getScript() {
        return this.script;
    }

    @Override
    public AC_JScriptHandler getScriptHandler() {
        return this.scriptHandler;
    }

    @Override
    public AC_MusicScripts getMusicScripts() {
        return this.musicScripts;
    }

    @Override
    public Scriptable getScope() {
        return this.scope;
    }

    @Override
    public ArrayList<CollisionList> getCollisionDebugLists() {
        return this.collisionDebugLists;
    }

    @Override
    public ArrayList<RayDebugList> getRayDebugLists() {
        return this.rayDebugLists;
    }
}
