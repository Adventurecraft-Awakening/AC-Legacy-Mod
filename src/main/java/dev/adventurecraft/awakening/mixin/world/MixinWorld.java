package dev.adventurecraft.awakening.mixin.world;

import ;
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
import net.minecraft.client.render.*;
import net.minecraft.client.renderer.ptexture.FireTexture;
import net.minecraft.client.renderer.ptexture.LavaSideTexture;
import net.minecraft.client.renderer.ptexture.LavaTexture;
import net.minecraft.client.renderer.ptexture.PortalTexture;
import net.minecraft.client.renderer.ptexture.WaterSideTexture;
import net.minecraft.client.renderer.ptexture.WaterTexture;
import net.minecraft.locale.I18n;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.*;
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
import java.util.Map.Entry;

@Mixin(Level.class)
public abstract class MixinWorld implements ExWorld, LevelSource {

    @Shadow
    static int field_179;

    @Shadow
    public LevelData properties;

    @Shadow
    public DimensionDataStorage mapTracker;

    @Shadow
    public boolean field_221;

    @Shadow
    @Final
    @Mutable
    public Dimension dimension;

    @Shadow
    public Random rand;

    @Shadow
    public ChunkSource worldSource;

    @Shadow
    @Final
    @Mutable
    protected LevelIO dimensionData;

    @Shadow
    public int field_202;

    @Shadow
    private ArrayList<AABB> field_189;

    @Shadow
    private Set<TickNextTickData> field_184;

    @Shadow
    protected List<LevelListener> worldListeners;

    @Shadow
    public int autoSaveInterval;

    @Shadow
    protected float prevRainGradient;

    @Shadow
    protected float rainGradient;

    @Shadow
    protected float prevThunderGradient;

    @Shadow
    protected float thunderGradient;

    @Shadow
    public List<Player> players;

    @Shadow
    public boolean isClient;

    @Shadow
    private int field_195;

    @Shadow
    protected int field_203;

    @Shadow
    protected int field_209;

    @Shadow
    public boolean field_215;

    @Shadow
    private TreeSet<TickNextTickData> treeSet;

    @Shadow
    public List<Entity> entities;

    @Shadow
    private List<LightUpdate> lightingUpdates;

    @Shadow
    @Final
    @Mutable
    protected int unusedIncrement;

    @Shadow
    private List unloadedEntities;

    @Shadow
    public List blockEntities;

    @Shadow
    private List field_185;

    @Shadow
    public List weatherEntities;

    @Shadow
    private long field_186;

    @Shadow
    private List field_196;

    @Shadow
    private boolean field_193;

    @Shadow
    private boolean field_192;

    @Shadow
    private long time;

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
    public abstract boolean isAir(int i, int j, int k);

    @Shadow
    public abstract LevelChunk getChunkFromCache(int i, int j);

    @Shadow
    public abstract int placeBlock(int i, int j, int k);

    @Shadow
    public abstract boolean isBlockLoaded(int i, int j, int k);

    @Shadow
    public abstract boolean isAboveGround(int i, int j, int k);

    @Shadow
    public abstract int method_164(LightLayer arg, int i, int j, int k);

    @Shadow
    public abstract void method_166(LightLayer arg, int i, int j, int k, int l, int m, int n);

    @Shadow
    public abstract LevelChunk getChunk(int i, int j);

    @Shadow
    public abstract boolean method_155(int i, int j, int k, int l, int m, int n);

    @Shadow
    protected abstract boolean isChunkLoaded(int i, int j);

    @Shadow
    public abstract void method_241(Entity arg);

    @Shadow
    public abstract void removeBlockEntity(int i, int j, int k);

    @Shadow
    public abstract int method_151(float f);

    @Shadow
    public abstract void saveLevel(boolean bl, ProgressListener arg);

    @Shadow
    public abstract long getWorldTime();

    @Shadow
    public abstract boolean isRaining();

    @Shadow
    public abstract boolean isThundering();

    @Shadow
    public abstract boolean canRainAt(int i, int j, int k);

    @Shadow
    public abstract boolean summonWeatherEntity(Entity arg);

    @Shadow
    public abstract boolean setBlock(int i, int j, int k, int l);

    @Shadow
    public abstract void method_237();

    @Shadow
    protected abstract void method_212();

    @Override
    public void initWorld(
        String mapName, LevelIO dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener) {
        this.unusedIncrement = 1013904223;
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
        this.lightingUpdates = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.unloadedEntities = new ArrayList<>();
        this.treeSet = new TreeSet<>();
        this.field_184 = new HashSet<>();
        this.blockEntities = new ArrayList<>();
        this.field_185 = new ArrayList<>();
        this.players = new ArrayList<>();
        this.weatherEntities = new ArrayList<>();
        this.field_186 = 16777215L;
        this.field_203 = (new Random()).nextInt();
        this.time = System.currentTimeMillis();
        this.autoSaveInterval = 40;
        this.rand = new Random();
        this.worldListeners = new ArrayList<>();
        this.field_189 = new ArrayList<>();
        this.field_192 = true;
        this.field_193 = true;
        this.field_195 = this.rand.nextInt(12000);
        this.field_196 = new ArrayList<>();
        this.dimensionData = dimData;
        if (dimData != null) {
            this.mapTracker = new DimensionDataStorage(dimData);
            this.properties = dimData.getLevelData();
        } else {
            this.mapTracker = new DimensionDataStorage(this.mapHandler);
        }

        if (this.properties == null) {
            this.newSave = true;
            this.properties = this.mapHandler.getLevelData();
        }

        if (!AC_TerrainImage.loadMap(levelDir)) {
            AC_TerrainImage.loadMap(new File(new File(gameDir, "saves"), saveName));
        }

        this.field_215 = this.properties == null;
        if (dimension != null) {
            this.dimension = dimension;
        } else if (this.properties != null && this.properties.getDimension() == -1) {
            this.dimension = Dimension.getNew(-1);
        } else {
            this.dimension = Dimension.getNew(0);
        }

        boolean newProps = false;
        if (this.properties == null) {
            this.properties = new LevelData(seed, saveName);
            newProps = true;
        } else {
            this.properties.setLevelName(saveName);
        }

        var props = (ExWorldProperties) this.properties;
        // Load current hud status
        ((ExInGameHud)Minecraft.instance.gui).setHudEnabled(props.getHudEnabled());

        props.getWorldGenProps().useImages = AC_TerrainImage.isLoaded;
        if (props.getTriggerData() != null) {
            this.triggerManager.loadFromTagCompound(props.getTriggerData());
        }

        this.dimension.setLevel((Level) (Object) this);
        this.loadBrightness();
        this.worldSource = this.getChunkCache();
        if (newProps) {
            this.method_212();
            this.field_221 = true;
            int var11 = 0;

            int var12;
            for (var12 = 0; !this.dimension.isValidSpawn(var11, var12); var12 += this.rand.nextInt(64) - this.rand.nextInt(64)) {
                var11 += this.rand.nextInt(64) - this.rand.nextInt(64);
            }

            this.properties.setSpawnXYZ(var11, this.getSurfaceBlockId(var11, var12), var12);
            this.field_221 = false;
        }

        this.method_237();
        this.initWeatherGradients();

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
        ((ExWorldProperties) this.properties).loadTextureReplacements(world);
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
        this.worldSource = this.getChunkCache();
    }

    @Overwrite
    public ChunkSource getChunkCache() {
        ChunkStorage io;
        if (this.dimensionData == null) {
            io = this.mapHandler.readDimension(this.dimension);
        } else {
            io = this.dimensionData.readDimension(this.dimension);
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

    @Redirect(method = "method_212", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/WorldProperties;setSpawnPosition(III)V"))
    private void spawnAtUncoveredBlock(LevelData instance, int x, int y, int z) {
        this.properties.setSpawnXYZ(x, this.getFirstUncoveredBlockY(x, z), z);
    }

    @Inject(method = "initSpawnPoint", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/WorldProperties;setSpawnZ(I)V",
        shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void spawnAtUncoveredBlock(CallbackInfo ci, int x, int z) {
        this.properties.setSpawnY(this.getFirstUncoveredBlockY(x, z));
    }

    public int getFirstUncoveredBlockY(int x, int z) {
        int y = 127;
        while (this.isAir(x, y, z) && y > 0) {
            --y;
        }
        return y;
    }

    @Overwrite
    public int getSurfaceBlockId(int x, int z) {
        int y = this.getFirstUncoveredBlockY(x, z);
        return this.getTile(x, y, z);
    }

    @Redirect(method = "method_271", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/dimension/DimensionData;saveWorldDataOnServer(Lnet/minecraft/world/WorldProperties;Ljava/util/List;)V"))
    private void modifySave(LevelIO instance, LevelData worldProperties, List<Player> list) {
        var exProps = (ExWorldProperties) worldProperties;
        exProps.setGlobalScope(ScopeTag.getTagFromScope(this.script.globalScope));
        exProps.setWorldScope(ScopeTag.getTagFromScope(this.scope));
        exProps.setMusicScope(ScopeTag.getTagFromScope(this.musicScripts.scope));

        if (this.dimensionData != null) {
            this.dimensionData.saveWithPlayers(worldProperties, list);
        }

        if (AC_DebugMode.levelEditing || this.dimensionData == null) {
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
            LevelChunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
            return ((ExChunk) chunk).setBlockIDWithMetadataTemp(x & 15, y, z & 15, id, meta);
        }
    }

    @Overwrite
    public int placeBlock(int x, int y, int z, boolean var4) {
        if (x < -32000000 || z < -32000000 || x >= 32000000 || z > 32000000) {
            return 15;
        }

        if (var4) {
            int id = this.getTile(x, y, z);
            if (id != 0 && (id == Tile.SLAB.id || id == Tile.FARMLAND.id || id == Tile.COBBLESTONE_STAIRS.id || id == Tile.WOOD_STAIRS.id || Tile.tiles[id] instanceof AC_BlockStairMulti)) {
                int topId = this.placeBlock(x, y + 1, z, false);
                int rightId = this.placeBlock(x + 1, y, z, false);
                int leftId = this.placeBlock(x - 1, y, z, false);
                int frontId = this.placeBlock(x, y, z + 1, false);
                int backId = this.placeBlock(x, y, z - 1, false);
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

        LevelChunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
        x &= 15;
        z &= 15;
        return chunk.getRawBrightness(x, y, z, this.field_202);
    }

    @Overwrite
    public void method_165(LightLayer lightType, int x, int y, int z, int value) {
        if (this.dimension.hasCeiling && lightType == LightLayer.SKY) {
            return;
        }
        if (this.isBlockLoaded(x, y, z)) {
            if (lightType == LightLayer.SKY) {
                if (this.isAboveGround(x, y, z)) {
                    value = 15;
                }
            } else if (lightType == LightLayer.BLOCK) {
                int var6 = this.getTile(x, y, z);
                if (Tile.tiles[var6] != null && ((ExBlock) Tile.tiles[var6]).getBlockLightValue(this, x, y, z) < value) {
                    value = ((ExBlock) Tile.tiles[var6]).getBlockLightValue(this, x, y, z);
                }
            }

            if (this.method_164(lightType, x, y, z) != value) {
                this.method_166(lightType, x, y, z, x, y, z);
            }
        }
    }

    @Override
    public float getLightValue(int x, int y, int z) {
        int var4 = this.placeBlock(x, y, z);
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
        int var1 = 15 - this.field_202;
        return this.dimension.brightnessRamp[var1];
    }

    @Overwrite
    public HitResult method_162(Vec3 pointA, Vec3 pointB, boolean var3, boolean var4) {
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

    @Redirect(method = "spawnEntity", at = @At(
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
        method = "method_190",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;doesBoxCollide(Lnet/minecraft/world/World;IIILnet/minecraft/util/math/AxixAlignedBoundingBox;Ljava/util/ArrayList;)V"))
    private void guardBoxCollide(Tile block, Level world, int x, int y, int z, AABB aabb, ArrayList<AABB> output, @Local(argsOnly = true) Entity entity) {
        if ((((ExEntity) entity).getCollidesWithClipBlocks() || block.id != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(block.id))) {
            block.addAABBs(world, x, y, z, aabb, output);
        }
    }

    @Inject(
        method = "method_190",
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
    public float method_198(float var1) {
        ExWorldProperties props = (ExWorldProperties) this.properties;
        return this.dimension.getTimeOfDay(props.getTimeOfDay(), var1 * props.getTimeRate());
    }

    @Inject(method = "method_284", at = @At("RETURN"))
    private void changeFogColor(float var1, CallbackInfoReturnable<Vec3> cir) {
        Vec3 var3 = cir.getReturnValue();
        ExWorldProperties props = (ExWorldProperties) this.properties;
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
    public int method_228(int x, int z) {
        LevelChunk chunk = this.getChunk(x, z);

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
        method = "method_227",
        at = @At("HEAD"))
    private void clearCollisionList(CallbackInfo ci) {
        this.collisionDebugLists.clear();
    }

    // This injection will be inverted at the target since the expression only captured the field access
    @ModifyExpressionValue(
        method = "method_227",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;removed:Z",
            ordinal = 2))
    private boolean fixupRemoveCondition(boolean value, @Local Entity entity) {
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        return !(!entity.removed && (!mc.isCameraActive() || !mc.isCameraPause()) && (!AC_DebugMode.active || entity instanceof Player));
    }

    @Inject(
        method = "method_227",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;method_241(Lnet/minecraft/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void fixupBoundingBox(CallbackInfo ci) {
        AABB.resetPool();
    }

    @Redirect(
        method = "method_227",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getChunkFromCache(II)Lnet/minecraft/world/chunk/Chunk;"))
    private LevelChunk ignoreIfKilledOnSave(Level instance, int j, int i, @Local TileEntity var5) {
        if (((ExBlockEntity) var5).isKilledFromSaving()) {
            return null;
        }
        LevelChunk chunk = this.getChunkFromCache(var5.x >> 4, var5.z >> 4);
        return chunk;
    }

    @Overwrite
    public void method_193(Entity entity, boolean var2) {
        int eX = Mth.floor(entity.x);
        int eZ = Mth.floor(entity.z);
        int var5 = 32;
        if (!var2 || this.method_155(eX - var5, 0, eZ - var5, eX + var5, 128, eZ + var5)) {
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
                if (entity.inChunk && this.isChunkLoaded(entity.xChunk, entity.zChunk)) {
                    this.getChunkFromCache(entity.xChunk, entity.zChunk).removeEntity(entity, entity.yChunk);
                }

                if (this.isChunkLoaded(ecX, ecZ)) {
                    entity.inChunk = true;
                    this.getChunkFromCache(ecX, ecZ).addEntity(entity);
                } else {
                    entity.inChunk = false;
                }
            }

            if (var2 && entity.inChunk && entity.rider != null) {
                if (!entity.rider.removed && entity.rider.riding == entity) {
                    this.method_241(entity.rider);
                } else {
                    entity.rider.riding = null;
                    entity.rider = null;
                }
            }
        }
    }

    @Inject(method = "setBlockEntity", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/World;field_190:Z",
        shift = At.Shift.BEFORE))
    private void removeBlockEntityOnSet(int x, int y, int z, TileEntity var4, CallbackInfo ci) {
        this.removeBlockEntity(x, y, z);
    }

    @Redirect(method = "removeBlockEntity", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/World;getBlockEntity(III)Lnet/minecraft/entity/BlockEntity;"))
    private TileEntity removeBlockEntityDontCreate(Level instance, int x, int y, int z) {
        return this.getBlockTileEntityDontCreate(x, y, z);
    }

    @Overwrite
    public void method_167(LightLayer lightType, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        if (this.dimension.hasCeiling && lightType == LightLayer.SKY) {
            return;
        }
        ++field_179;

        try {
            if (field_179 == 50) {
                return;
            }

            int x = (var5 + var2) / 2;
            int z = (var7 + var4) / 2;
            if (!this.isBlockLoaded(x, 64, z)) {
                return;
            }

            if (this.getChunk(x, z).isEmpty()) {
                return;
            }

            if (var8) {
                int count = Math.min(this.lightingUpdates.size(), 5);
                for (int i = 0; i < count; ++i) {
                    LightUpdate var14 = this.lightingUpdates.get(this.lightingUpdates.size() - i - 1);
                    if (var14.type == lightType && var14.expandToContain(var2, var3, var4, var5, var6, var7)) {
                        return;
                    }
                }
            }

            this.lightingUpdates.add(new LightUpdate(lightType, var2, var3, var4, var5, var6, var7));

            final int maxUpdates = 1000000;
            if (this.lightingUpdates.size() > maxUpdates) {
                System.out.println("More than " + maxUpdates + " updates, aborting lighting updates");
                this.lightingUpdates.clear();
            }
        } finally {
            --field_179;
        }
    }

    @Override
    public void ac$preTick() {
        if (this.firstTick) {
            var props = (ExWorldProperties) this.properties;

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
    public void method_242() {
        var props = (ExWorldProperties) this.properties;

        ScriptModel.updateAll();
        if (!props.getOnUpdateScript().equals("")) {
            this.scriptHandler.runScript(props.getOnUpdateScript(), this.scope);
        }

        this.fogColorOverridden = props.isOverrideFogColor();
        this.fogDensityOverridden = props.isOverrideFogDensity();
        this.method_245();
        this.worldSource.tick();
        int var1 = this.method_151(1.0F);
        if (var1 != this.field_202) {
            this.field_202 = var1;

            for (LevelListener worldListener : this.worldListeners) {
                worldListener.skyColorChanged();
            }
        }

        long var4 = this.properties.getTime() + 1L;
        if (var4 % (long) this.autoSaveInterval == 0L) {
            this.saveLevel(false, null);
        }

        this.properties.setTime(var4);
        props.addToTimeOfDay(props.getTimeRate());
        this.method_194(false);
        this.method_248();
        if (this.properties.isRaining()) {
            this.DoSnowModUpdate();
        }

        this.script.wakeupScripts(var4);
    }

    @Overwrite
    private void initWeatherGradients() {
        if (this.properties.isRaining()) {
            this.rainGradient = 1.0F;
            if (this.properties.isThundering()) {
                this.thunderGradient = 1.0F;
            }
        }
    }

    @Overwrite
    public void method_245() {
        if (this.dimension.hasCeiling) {
            return;
        }

        if (this.field_209 > 0) {
            --this.field_209;
        }

        this.prevRainGradient = this.rainGradient;
        if (this.properties.isRaining()) {
            this.rainGradient = (float) ((double) this.rainGradient + 0.01D);
        } else {
            this.rainGradient = (float) ((double) this.rainGradient - 0.01D);
        }

        if (this.rainGradient < 0.0F) {
            this.rainGradient = 0.0F;
        }

        if (this.rainGradient > 1.0F) {
            this.rainGradient = 1.0F;
        }

        this.prevThunderGradient = this.thunderGradient;
        if (this.properties.isThundering()) {
            this.thunderGradient = (float) ((double) this.thunderGradient + 0.01D);
        } else {
            this.thunderGradient = (float) ((double) this.thunderGradient - 0.01D);
        }

        if (this.thunderGradient < 0.0F) {
            this.thunderGradient = 0.0F;
        }

        if (this.thunderGradient > 1.0F) {
            this.thunderGradient = 1.0F;
        }
    }

    @Overwrite
    public void method_248() {
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

        if (this.field_195 > 0) {
            --this.field_195;
        }
    }

    protected void updateChunk(int var1, int var2) {
        LevelChunk var3 = this.getChunkFromCache(var1, var2);
        if (((ExChunk) var3).getLastUpdated() == this.getWorldTime()) {
            return;
        }

        int var4 = var1 * 16;
        int var5 = var2 * 16;
        ((ExChunk) var3).setLastUpdated(this.getWorldTime());
        int var6;
        int var7;
        int var8;
        int var9;
        if (this.rand.nextInt(100000) == 0 && this.isRaining() && this.isThundering()) {
            this.field_203 = this.field_203 * 3 + 1013904223;
            var6 = this.field_203 >> 2;
            var7 = var4 + (var6 & 15);
            var8 = var5 + (var6 >> 8 & 15);
            var9 = this.method_228(var7, var8);
            if (this.canRainAt(var7, var9, var8)) {
                this.summonWeatherEntity(new LightningBolt((Level) (Object) this, var7, var9, var8));
                this.field_209 = 2;
            }
        }

        for (var6 = 0; var6 < 80; ++var6) {
            this.field_203 = this.field_203 * 3 + 1013904223;
            var7 = this.field_203 >> 2;
            var8 = var7 & 15;
            var9 = var7 >> 8 & 15;
            int var10 = var7 >> 16 & 127;
            int var11 = var3.blocks[var8 << 11 | var9 << 7 | var10] & 255;
            if (Tile.shouldTick[var11]) {
                Tile.tiles[var11].tick((Level) (Object) this, var8 + var4, var10, var9 + var5, this.rand);
            }
        }
    }

    @Overwrite
    public boolean method_194(boolean var1) {
        int var2 = this.treeSet.size();
        if (var2 > 1000) {
            var2 = 1000;
        }

        for (int var3 = 0; var3 < var2; ++var3) {
            TickNextTickData var4 = this.treeSet.first();
            if (!var1 && var4.delay > this.properties.getTime()) {
                break;
            }

            this.treeSet.remove(var4);
            if (this.field_184.remove(var4)) {
                byte var5 = 8;
                if (this.method_155(var4.x - var5, var4.y - var5, var4.z - var5, var4.x + var5, var4.y + var5, var4.z + var5)) {
                    int var6 = this.getTile(var4.x, var4.y, var4.z);
                    if (var6 == var4.priority && var6 > 0) {
                        Tile.tiles[var6].tick((Level) (Object) this, var4.x, var4.y, var4.z, this.rand);
                        AABB.resetPool();
                    }
                }
            }
        }

        return this.treeSet.size() != 0;
    }

    @Overwrite
    public void checkSessionLock() {
        if (this.dimensionData != null) {
            this.dimensionData.checkSession();
        } else {
            this.mapHandler.checkSession();
        }
    }

    private void DoSnowModUpdate() {
        if (this.isClient) {
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
                    long var9 = (long) (var7 + var8 * 2) + this.getWorldTime();
                    if (var9 % 14L == 0L && this.isChunkLoaded(var7 + var4, var8 + var5)) {
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
        int var3 = this.method_228(var1, var2);
        if (var3 < 0) {
            var3 = 0;
        }

        int var4 = this.getTile(var1, var3 - 1, var2);
        if (this.getTemperatureValue(var1, var2) < 0.5D) {
            if (!this.isAir(var1, var3, var2)) {
                return false;
            } else {
                if (var4 != 0 && Tile.tiles[var4].isSolidRender()) {
                    if (!this.getMaterial(var1, var3 - 1, var2).blocksMotion()) {
                        return false;
                    }

                    if (this.method_164(LightLayer.BLOCK, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setBlock(var1, var3, var2, Tile.SNOW_LAYER.id);
                } else if (var4 == Tile.FLOWING_WATER.id && this.getData(var1, var3 - 1, var2) == 0) {
                    if (this.method_164(LightLayer.BLOCK, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setBlock(var1, var3 - 1, var2, Tile.ICE.id);
                }

                return true;
            }
        } else {
            int var5 = this.getTile(var1, var3, var2);
            if (var5 == Tile.SNOW_LAYER.id) {
                this.setBlock(var1, var3, var2, 0);
                return true;
            } else if (var4 == Tile.SNOW.id) {
                this.setBlock(var1, var3 - 1, var2, Tile.SNOW_LAYER.id);
                return true;
            } else if (var4 == Tile.ICE.id) {
                this.setBlock(var1, var3 - 1, var2, Tile.FLOWING_WATER.id);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void cancelBlockUpdate(int x, int y, int z, int id) {
        TickNextTickData entry = new TickNextTickData(x, y, z, id);
        this.field_184.remove(entry);
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

        String playingMusic = ((ExWorldProperties) this.properties).getPlayingMusic();
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
        float[] dimBrightness = ((ExWorldProperties) this.properties).getBrightness().clone();
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
        var props = (ExWorldProperties) this.properties;
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
        var props = (ExWorldProperties) this.properties;
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
        LevelChunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
        if (chunk != null) {
            return ((ExChunk) chunk).getChunkBlockTileEntityDontCreate(x & 15, y, z & 15);
        }
        return null;
    }

    @Override
    public double getTemperatureValue(int x, int z) {
        if (x >= -32000000 && z >= -32000000 && x < 32000000 && z <= 32000000) {
            var chunk = (ExChunk) this.getChunkFromCache(x >> 4, z >> 4);
            double tempValue = chunk.getTemperatureValue(x & 15, z & 15);
            double tempOffset = ((ExWorldProperties) this.properties).getTempOffset();
            return tempValue + tempOffset;
        }
        return 0.0D;
    }

    @Override
    public void setTemperatureValue(int x, int z, double value) {
        if (x >= -32000000 && z >= -32000000 && x < 32000000 && z <= 32000000) {
            var chunk = (ExChunk) this.getChunkFromCache(x >> 4, z >> 4);
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
        var1.setSeed(this.getWorldTime());
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
        return (float) ((ExWorldProperties) this.properties).getTimeOfDay();
    }

    @Override
    public void setTimeOfDay(long value) {
        ((ExWorldProperties) this.properties).setTimeOfDay((float) value);
    }

    @Override
    public float getSpawnYaw() {
        return ((ExWorldProperties) this.properties).getSpawnYaw();
    }

    @Override
    public void setSpawnYaw(float value) {
        ((ExWorldProperties) this.properties).setSpawnYaw(value);
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
