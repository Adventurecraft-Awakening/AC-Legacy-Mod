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
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.class_366;
import net.minecraft.class_417;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.AxixAlignedBoundingBox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkCache;
import net.minecraft.world.chunk.ChunkIO;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionData;
import net.minecraft.world.dimension.McRegionDimensionFile;
import net.minecraft.world.source.WorldSource;
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

@Mixin(World.class)
public abstract class MixinWorld implements ExWorld, BlockView {

    @Shadow
    static int field_179;

    @Shadow
    public WorldProperties properties;

    @Shadow
    public MapTracker mapTracker;

    @Shadow
    public boolean field_221;

    @Shadow
    @Final
    @Mutable
    public Dimension dimension;

    @Shadow
    public Random rand;

    @Shadow
    public WorldSource worldSource;

    @Shadow
    @Final
    @Mutable
    protected DimensionData dimensionData;

    @Shadow
    public int field_202;

    @Shadow
    private ArrayList<AxixAlignedBoundingBox> field_189;

    @Shadow
    private Set<class_366> field_184;

    @Shadow
    protected List<WorldListener> worldListeners;

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
    public List<PlayerEntity> players;

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
    private TreeSet<class_366> treeSet;

    @Shadow
    public List<Entity> entities;

    @Shadow
    private List<class_417> lightingUpdates;

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
    private DimensionData mapHandler;
    public AC_TriggerManager triggerManager = new AC_TriggerManager((World) (Object) this);
    public boolean fogColorOverridden;
    public boolean fogDensityOverridden;
    boolean firstTick = true;
    boolean newSave;
    public AC_UndoStack undoStack = new AC_UndoStack();
    private ArrayList<CollisionList> collisionDebugLists = new ArrayList<>();
    private ArrayList<AxixAlignedBoundingBox> rayCheckedBlocks = new ArrayList<>();
    private ArrayList<RayDebugList> rayDebugLists = new ArrayList<>();
    public Script script = new Script((World) (Object) this);
    public AC_JScriptHandler scriptHandler;
    public AC_MusicScripts musicScripts;
    public Scriptable scope;

    @Shadow
    public abstract int getBlockId(int i, int j, int k);

    @Shadow
    public abstract boolean isAir(int i, int j, int k);

    @Shadow
    public abstract Chunk getChunkFromCache(int i, int j);

    @Shadow
    public abstract int placeBlock(int i, int j, int k);

    @Shadow
    public abstract boolean isBlockLoaded(int i, int j, int k);

    @Shadow
    public abstract boolean isAboveGround(int i, int j, int k);

    @Shadow
    public abstract int method_164(LightType arg, int i, int j, int k);

    @Shadow
    public abstract void method_166(LightType arg, int i, int j, int k, int l, int m, int n);

    @Shadow
    public abstract Chunk getChunk(int i, int j);

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
        String mapName, DimensionData dimData, String saveName, long seed, Dimension dimension, ProgressListener progressListener) {
        this.unusedIncrement = 1013904223;
        this.fogColorOverridden = false;
        this.fogDensityOverridden = false;
        this.firstTick = true;
        this.newSave = false;
        this.musicList = new ArrayList<>();
        this.soundList = new ArrayList<>();
        this.triggerManager = new AC_TriggerManager((World) (Object) this);
        this.undoStack = new AC_UndoStack();
        this.collisionDebugLists = new ArrayList<>();
        this.rayCheckedBlocks = new ArrayList<>();
        this.rayDebugLists = new ArrayList<>();
        File gameDir = Minecraft.getGameDirectory();
        File mapsDir = ACMainThread.getMapsDirectory();
        File levelDir = new File(mapsDir, mapName);
        ((ExTranslationStorage) TranslationStorage.getInstance()).loadMapTranslation(levelDir);
        this.mapHandler = new McRegionDimensionFile(mapsDir, mapName, false);
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
            this.mapTracker = new MapTracker(dimData);
            this.properties = dimData.getLevelProperties();
        } else {
            this.mapTracker = new MapTracker(this.mapHandler);
        }

        if (this.properties == null) {
            this.newSave = true;
            this.properties = this.mapHandler.getLevelProperties();
        }

        if (!AC_TerrainImage.loadMap(levelDir)) {
            AC_TerrainImage.loadMap(new File(new File(gameDir, "saves"), saveName));
        }

        this.field_215 = this.properties == null;
        if (dimension != null) {
            this.dimension = dimension;
        } else if (this.properties != null && this.properties.getDimensionId() == -1) {
            this.dimension = Dimension.getByID(-1);
        } else {
            this.dimension = Dimension.getByID(0);
        }

        boolean newProps = false;
        if (this.properties == null) {
            this.properties = new WorldProperties(seed, saveName);
            newProps = true;
        } else {
            this.properties.setName(saveName);
        }

        var props = (ExWorldProperties) this.properties;

        props.getWorldGenProps().useImages = AC_TerrainImage.isLoaded;
        if (props.getTriggerData() != null) {
            this.triggerManager.loadFromTagCompound(props.getTriggerData());
        }

        this.dimension.initDimension((World) (Object) this);
        this.loadBrightness();
        this.worldSource = this.getChunkCache();
        if (newProps) {
            this.method_212();
            this.field_221 = true;
            int var11 = 0;

            int var12;
            for (var12 = 0; !this.dimension.canSpawnOn(var11, var12); var12 += this.rand.nextInt(64) - this.rand.nextInt(64)) {
                var11 += this.rand.nextInt(64) - this.rand.nextInt(64);
            }

            this.properties.setSpawnPosition(var11, this.getSurfaceBlockId(var11, var12), var12);
            this.field_221 = false;
        }

        this.method_237();
        this.initWeatherGradients();

        this.loadMapMusic();
        this.loadMapSounds();

        this.script = new Script((World) (Object) this);

        if (props.getGlobalScope() != null) {
            ScopeTag.loadScopeFromTag(this.script.globalScope, props.getGlobalScope());
        }

        this.scriptHandler = new AC_JScriptHandler((World) (Object) this, levelDir);
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
        var texManager = ((ExTextureManager) Minecraft.instance.textureManager);
        Minecraft.instance.textureManager.reloadTexturesFromTexturePack();

        for (Object oEntry : Minecraft.instance.textureManager.textures.entrySet()) {
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
        World world = (World) (Object) this;
        AC_TextureBinder.loadImages(texManager, AC_TextureFanFX.class, world);
        AC_TextureBinder.loadImages(texManager, FireTextureBinder.class, world);
        AC_TextureBinder.loadImages(texManager, FlowingLavaTextureBinder.class, world);
        AC_TextureBinder.loadImages(texManager, FlowingLavaTextureBinder2.class, world);
        AC_TextureBinder.loadImages(texManager, PortalTextureBinder.class, world);
        AC_TextureBinder.loadImages(texManager, FlowingWaterTextureBinder2.class, world);
        AC_TextureBinder.loadImages(texManager, FlowingWaterTextureBinder.class, world);
        ExGrassColor.loadGrass("/misc/grasscolor.png", world);
        ExFoliageColor.loadFoliage("/misc/foliagecolor.png", world);
        ((ExWorldProperties) this.properties).loadTextureReplacements(world);
    }

    private void loadTextureAnimations() {
        var texManager = ((ExTextureManager) Minecraft.instance.textureManager);
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
                            ((AC_TextureBinder) instance).loadImage(imageName, (World) (Object) this);
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
    public WorldSource getChunkCache() {
        ChunkIO io;
        if (this.dimensionData == null) {
            io = this.mapHandler.getChunkIO(this.dimension);
        } else {
            io = this.dimensionData.getChunkIO(this.dimension);
            if (this.mapHandler != null) {
                io = new MapChunkLoader(this.mapHandler.getChunkIO(this.dimension), io);
            }
        }

        try {
            var cache = (ChunkCache) ACMod.UNSAFE.allocateInstance(ChunkCache.class);
            ((ExChunkCache) cache).init((World) (Object) this, io, this.dimension.createWorldSource());
            return cache;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    @Redirect(method = "method_212", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/WorldProperties;setSpawnPosition(III)V"))
    private void spawnAtUncoveredBlock(WorldProperties instance, int x, int y, int z) {
        this.properties.setSpawnPosition(x, this.getFirstUncoveredBlockY(x, z), z);
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
        return this.getBlockId(x, y, z);
    }

    @Redirect(method = "method_271", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/dimension/DimensionData;saveWorldDataOnServer(Lnet/minecraft/world/WorldProperties;Ljava/util/List;)V"))
    private void modifySave(DimensionData instance, WorldProperties worldProperties, List<PlayerEntity> list) {
        var exProps = (ExWorldProperties) worldProperties;
        exProps.setGlobalScope(ScopeTag.getTagFromScope(this.script.globalScope));
        exProps.setWorldScope(ScopeTag.getTagFromScope(this.scope));
        exProps.setMusicScope(ScopeTag.getTagFromScope(this.musicScripts.scope));

        if (this.dimensionData != null) {
            this.dimensionData.saveWorldDataOnServer(worldProperties, list);
        }

        if (AC_DebugMode.levelEditing || this.dimensionData == null) {
            this.mapHandler.saveWorldDataOnServer(worldProperties, list);
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
            Chunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
            return ((ExChunk) chunk).setBlockIDWithMetadataTemp(x & 15, y, z & 15, id, meta);
        }
    }

    @Overwrite
    public int placeBlock(int x, int y, int z, boolean var4) {
        if (x < -32000000 || z < -32000000 || x >= 32000000 || z > 32000000) {
            return 15;
        }

        if (var4) {
            int id = this.getBlockId(x, y, z);
            if (id != 0 && (id == Block.STONE_SLAB.id || id == Block.FARMLAND.id || id == Block.COBBLESTONE_STAIRS.id || id == Block.WOOD_STAIRS.id || Block.BY_ID[id] instanceof AC_BlockStairMulti)) {
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

        Chunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
        x &= 15;
        z &= 15;
        return chunk.method_880(x, y, z, this.field_202);
    }

    @Overwrite
    public void method_165(LightType lightType, int x, int y, int z, int value) {
        if (this.dimension.halvesMapping && lightType == LightType.field_2757) {
            return;
        }
        if (this.isBlockLoaded(x, y, z)) {
            if (lightType == LightType.field_2757) {
                if (this.isAboveGround(x, y, z)) {
                    value = 15;
                }
            } else if (lightType == LightType.field_2758) {
                int var6 = this.getBlockId(x, y, z);
                if (Block.BY_ID[var6] != null && ((ExBlock) Block.BY_ID[var6]).getBlockLightValue(this, x, y, z) < value) {
                    value = ((ExBlock) Block.BY_ID[var6]).getBlockLightValue(this, x, y, z);
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
        float var5 = AC_PlayerTorch.getTorchLight((World) (Object) this, x, y, z);
        return (float) var4 < var5 ? Math.min(var5, 15.0F) : (float) var4;
    }

    private float getBrightnessLevel(float var1) {
        int var2 = (int) Math.floor(var1);
        if ((float) var2 != var1) {
            int var3 = (int) Math.ceil(var1);
            float var4 = var1 - (float) var2;
            return (1.0F - var4) * this.dimension.lightTable[var2] + var4 * this.dimension.lightTable[var3];
        } else {
            return this.dimension.lightTable[var2];
        }
    }

    @Environment(EnvType.CLIENT)
    @Overwrite
    public float getNaturalBrightness(int x, int y, int z, int var4) {
        float var5 = this.getLightValue(x, y, z);
        if (var5 < (float) var4) {
            var5 = (float) var4;
        }

        return this.getBrightnessLevel(var5);
    }

    @Overwrite
    public float method_1782(int x, int y, int z) {
        float var4 = this.getLightValue(x, y, z);
        return this.getBrightnessLevel(var4);
    }

    public float getDayLight() {
        int var1 = 15 - this.field_202;
        return this.dimension.lightTable[var1];
    }

    @Overwrite
    public HitResult method_162(Vec3d pointA, Vec3d pointB, boolean var3, boolean var4) {
        return this.rayTraceBlocks2(pointA, pointB, var3, var4, true);
    }

    @Override
    public HitResult rayTraceBlocks2(Vec3d pointA, Vec3d pointB, boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip) {
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
    public HitResult rayTraceBlocksCore(Vec3d pointA, Vec3d pointB, boolean blockCollidableFlag, boolean useCollisionShapes, boolean collideWithClip) {
        int bX = MathHelper.floor(pointB.x);
        int bY = MathHelper.floor(pointB.y);
        int bZ = MathHelper.floor(pointB.z);
        int aX = MathHelper.floor(pointA.x);
        int aY = MathHelper.floor(pointA.y);
        int aZ = MathHelper.floor(pointA.z);
        int aId = this.getBlockId(aX, aY, aZ);
        Block aBlock = Block.BY_ID[aId];
        AxixAlignedBoundingBox aAabb = null;
        if (aBlock != null &&
            (!useCollisionShapes || (aAabb = aBlock.getCollisionShape((World) (Object) this, aX, aY, aZ)) != null) &&
            (aId > 0 && (collideWithClip || aId != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(aId)) && aBlock.isCollidable(this.getBlockMeta(aX, aY, aZ), blockCollidableFlag))) {

            if (aAabb != null && AC_DebugMode.renderRays) {
                this.rayCheckedBlocks.add(aAabb);
            }

            HitResult hit = aBlock.method_1564((World) (Object) this, aX, aY, aZ, pointA, pointB);
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

            aX = MathHelper.floor(pointA.x);
            if (side == 5) {
                --aX;
            }

            aY = MathHelper.floor(pointA.y);
            if (side == 1) {
                --aY;
            }

            aZ = MathHelper.floor(pointA.z);
            if (side == 3) {
                --aZ;
            }

            int id = this.getBlockId(aX, aY, aZ);
            Block block = Block.BY_ID[id];
            AxixAlignedBoundingBox aabb = null;
            if (block != null &&
                (!useCollisionShapes || (aabb = block.getCollisionShape((World) (Object) this, aX, aY, aZ)) != null) &&
                id != 0 && block.isCollidable(this.getBlockMeta(aX, aY, aZ), blockCollidableFlag) && ((ExBlock) block).shouldRender(this, aX, aY, aZ)) {

                if (aabb != null && AC_DebugMode.renderRays) {
                    this.rayCheckedBlocks.add(aabb);
                }

                HitResult hit = block.method_1564((World) (Object) this, aX, aY, aZ, pointA, pointB);
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
    private void guardBoxCollide(Block block, World world, int x, int y, int z, AxixAlignedBoundingBox aabb, ArrayList<AxixAlignedBoundingBox> output, @Local(argsOnly = true) Entity entity) {
        if ((((ExEntity) entity).getCollidesWithClipBlocks() || block.id != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(block.id))) {
            block.doesBoxCollide(world, x, y, z, aabb, output);
        }
    }

    @Inject(
        method = "method_190",
        at = @At("RETURN"))
    private void recordCollision(Entity entity, AxixAlignedBoundingBox aabb, CallbackInfoReturnable<List> cir) {
        if (!AC_DebugMode.renderCollisions) {
            return;
        }

        var collisionsArray = saveAsDoubleArray((List<AxixAlignedBoundingBox>) cir.getReturnValue());
        this.collisionDebugLists.add(new CollisionList(entity, aabb, collisionsArray));
    }

    private static double[] saveAsDoubleArray(List<AxixAlignedBoundingBox> boxList) {
        int size = boxList.size();
        if (size == 0) {
            return null;
        }

        var boxArray = new double[size * 6];
        for (int i = 0; i < size; i++) {
            AxixAlignedBoundingBox box = boxList.get(i);
            boxArray[i * 6 + 0] = box.minX;
            boxArray[i * 6 + 1] = box.minY;
            boxArray[i * 6 + 2] = box.minZ;
            boxArray[i * 6 + 3] = box.maxX;
            boxArray[i * 6 + 4] = box.maxY;
            boxArray[i * 6 + 5] = box.maxZ;
        }
        return boxArray;
    }

    @Overwrite
    public float method_198(float var1) {
        ExWorldProperties props = (ExWorldProperties) this.properties;
        return this.dimension.getSunPosition(props.getTimeOfDay(), var1 * props.getTimeRate());
    }

    @Inject(method = "method_284", at = @At("RETURN"))
    private void changeFogColor(float var1, CallbackInfoReturnable<Vec3d> cir) {
        Vec3d var3 = cir.getReturnValue();
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
        Chunk chunk = this.getChunk(x, z);

        int y = 127;
        while (this.getMaterial(x, y, z).blocksMovement() && y > 0) {
            --y;
        }

        x &= 15;
        z &= 15;

        while (y > 0) {
            int id = chunk.getBlockId(x, y, z);
            Material mat = id != 0 ? Block.BY_ID[id].material : Material.AIR;
            if (mat.blocksMovement() || mat.isLiquid()) {
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
        return !(!entity.removed && (!mc.isCameraActive() || !mc.isCameraPause()) && (!AC_DebugMode.active || entity instanceof PlayerEntity));
    }

    @Inject(
        method = "method_227",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;method_241(Lnet/minecraft/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void fixupBoundingBox(CallbackInfo ci) {
        AxixAlignedBoundingBox.method_85();
    }

    @Redirect(
        method = "method_227",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getChunkFromCache(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk ignoreIfKilledOnSave(World instance, int j, int i, @Local BlockEntity var5) {
        if (((ExBlockEntity) var5).isKilledFromSaving()) {
            return null;
        }
        Chunk chunk = this.getChunkFromCache(var5.x >> 4, var5.z >> 4);
        return chunk;
    }

    @Overwrite
    public void method_193(Entity entity, boolean var2) {
        int eX = MathHelper.floor(entity.x);
        int eZ = MathHelper.floor(entity.z);
        int var5 = 32;
        if (!var2 || this.method_155(eX - var5, 0, eZ - var5, eX + var5, 128, eZ + var5)) {
            entity.prevRenderX = entity.x;
            entity.prevRenderY = entity.y;
            entity.prevRenderZ = entity.z;
            entity.prevYaw = entity.yaw;
            entity.prevPitch = entity.pitch;
            if (var2 && entity.field_1618) {
                int stunned = ((ExEntity) entity).getStunned();
                if (stunned > 0) {
                    ((ExEntity) entity).setStunned(stunned - 1);
                } else if (entity.vehicle != null) {
                    entity.tickRiding();
                } else {
                    entity.tick();
                }
            }

            if (Double.isNaN(entity.x) || Double.isInfinite(entity.x)) {
                entity.x = entity.prevRenderX;
            }

            if (Double.isNaN(entity.y) || Double.isInfinite(entity.y)) {
                entity.y = entity.prevRenderY;
            }

            if (Double.isNaN(entity.z) || Double.isInfinite(entity.z)) {
                entity.z = entity.prevRenderZ;
            }

            if (Double.isNaN(entity.pitch) || Double.isInfinite(entity.pitch)) {
                entity.pitch = entity.prevPitch;
            }

            if (Double.isNaN(entity.yaw) || Double.isInfinite(entity.yaw)) {
                entity.yaw = entity.prevYaw;
            }

            int ecX = MathHelper.floor(entity.x / 16.0D);
            int ecY = MathHelper.floor(entity.y / 16.0D);
            int ecZ = MathHelper.floor(entity.z / 16.0D);
            if (!entity.field_1618 || entity.chunkX != ecX || entity.chunkIndex != ecY || entity.chunkZ != ecZ) {
                if (entity.field_1618 && this.isChunkLoaded(entity.chunkX, entity.chunkZ)) {
                    this.getChunkFromCache(entity.chunkX, entity.chunkZ).removeEntity(entity, entity.chunkIndex);
                }

                if (this.isChunkLoaded(ecX, ecZ)) {
                    entity.field_1618 = true;
                    this.getChunkFromCache(ecX, ecZ).addEntity(entity);
                } else {
                    entity.field_1618 = false;
                }
            }

            if (var2 && entity.field_1618 && entity.passenger != null) {
                if (!entity.passenger.removed && entity.passenger.vehicle == entity) {
                    this.method_241(entity.passenger);
                } else {
                    entity.passenger.vehicle = null;
                    entity.passenger = null;
                }
            }
        }
    }

    @Inject(method = "setBlockEntity", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/world/World;field_190:Z",
        shift = At.Shift.BEFORE))
    private void removeBlockEntityOnSet(int x, int y, int z, BlockEntity var4, CallbackInfo ci) {
        this.removeBlockEntity(x, y, z);
    }

    @Redirect(method = "removeBlockEntity", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/World;getBlockEntity(III)Lnet/minecraft/entity/BlockEntity;"))
    private BlockEntity removeBlockEntityDontCreate(World instance, int x, int y, int z) {
        return this.getBlockTileEntityDontCreate(x, y, z);
    }

    @Overwrite
    public void method_167(LightType lightType, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        if (this.dimension.halvesMapping && lightType == LightType.field_2757) {
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

            if (this.getChunk(x, z).method_886()) {
                return;
            }

            if (var8) {
                int count = Math.min(this.lightingUpdates.size(), 5);
                for (int i = 0; i < count; ++i) {
                    class_417 var14 = this.lightingUpdates.get(this.lightingUpdates.size() - i - 1);
                    if (var14.field_1673 == lightType && var14.method_1401(var2, var3, var4, var5, var6, var7)) {
                        return;
                    }
                }
            }

            this.lightingUpdates.add(new class_417(lightType, var2, var3, var4, var5, var6, var7));

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
        this.worldSource.unloadOldestChunks();
        int var1 = this.method_151(1.0F);
        if (var1 != this.field_202) {
            this.field_202 = var1;

            for (WorldListener worldListener : this.worldListeners) {
                worldListener.method_1148();
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
        if (this.dimension.halvesMapping) {
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
        for (PlayerEntity var2 : this.players) {
            int var3 = MathHelper.floor(var2.x / 16.0D);
            int var4 = MathHelper.floor(var2.z / 16.0D);
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
        Chunk var3 = this.getChunkFromCache(var1, var2);
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
                this.summonWeatherEntity(new LightningEntity((World) (Object) this, var7, var9, var8));
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
            if (Block.TICKS_RANDOMLY[var11]) {
                Block.BY_ID[var11].onScheduledTick((World) (Object) this, var8 + var4, var10, var9 + var5, this.rand);
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
            class_366 var4 = this.treeSet.first();
            if (!var1 && var4.field_1404 > this.properties.getTime()) {
                break;
            }

            this.treeSet.remove(var4);
            if (this.field_184.remove(var4)) {
                byte var5 = 8;
                if (this.method_155(var4.field_1400 - var5, var4.field_1401 - var5, var4.field_1402 - var5, var4.field_1400 + var5, var4.field_1401 + var5, var4.field_1402 + var5)) {
                    int var6 = this.getBlockId(var4.field_1400, var4.field_1401, var4.field_1402);
                    if (var6 == var4.field_1403 && var6 > 0) {
                        Block.BY_ID[var6].onScheduledTick((World) (Object) this, var4.field_1400, var4.field_1401, var4.field_1402, this.rand);
                        AxixAlignedBoundingBox.method_85();
                    }
                }
            }
        }

        return this.treeSet.size() != 0;
    }

    @Overwrite
    public void checkSessionLock() {
        if (this.dimensionData != null) {
            this.dimensionData.checkSessionLock();
        } else {
            this.mapHandler.checkSessionLock();
        }
    }

    private void DoSnowModUpdate() {
        if (this.isClient) {
            return;
        }

        if (this.coordOrder == null) {
            this.initCoordOrder();
        }

        for (PlayerEntity var3 : this.players) {
            int var4 = MathHelper.floor(var3.x / 16.0D);
            int var5 = MathHelper.floor(var3.z / 16.0D);
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

        int var4 = this.getBlockId(var1, var3 - 1, var2);
        if (this.getTemperatureValue(var1, var2) < 0.5D) {
            if (!this.isAir(var1, var3, var2)) {
                return false;
            } else {
                if (var4 != 0 && Block.BY_ID[var4].isFullOpaque()) {
                    if (!this.getMaterial(var1, var3 - 1, var2).blocksMovement()) {
                        return false;
                    }

                    if (this.method_164(LightType.field_2758, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setBlock(var1, var3, var2, Block.SNOW.id);
                } else if (var4 == Block.FLOWING_WATER.id && this.getBlockMeta(var1, var3 - 1, var2) == 0) {
                    if (this.method_164(LightType.field_2758, var1, var3, var2) > 11) {
                        return false;
                    }

                    this.setBlock(var1, var3 - 1, var2, Block.ICE.id);
                }

                return true;
            }
        } else {
            int var5 = this.getBlockId(var1, var3, var2);
            if (var5 == Block.SNOW.id) {
                this.setBlock(var1, var3, var2, 0);
                return true;
            } else if (var4 == Block.SNOW_BLOCK.id) {
                this.setBlock(var1, var3 - 1, var2, Block.SNOW.id);
                return true;
            } else if (var4 == Block.ICE.id) {
                this.setBlock(var1, var3 - 1, var2, Block.FLOWING_WATER.id);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void cancelBlockUpdate(int x, int y, int z, int id) {
        class_366 entry = new class_366(x, y, z, id);
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
                    Minecraft.instance.soundHelper.addStreaming(name, file);

                    String musicName = String.format("music.%s", fileName.replace(".ogg", ""));
                    this.musicList.add(musicName);
                }
            }
        }

        String playingMusic = ((ExWorldProperties) this.properties).getPlayingMusic();
        if (!playingMusic.equals("")) {
            ((ExSoundHelper) Minecraft.instance.soundHelper).playMusicFromStreaming(playingMusic, 0, 0);
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
                Minecraft.instance.soundHelper.addSound(name, file);

                String soundName = String.format("sound.%s", fileName.replace(".ogg", ""));
                this.soundList.add(soundName);
            }
        }
    }

    public void loadSoundOverrides() {
        // TODO: unload sounds?

        Minecraft.instance.resourceDownloadThread.method_107();
        File var1 = new File(this.levelDir, "soundOverrides");
        if (var1.exists()) {
            Minecraft.instance.resourceDownloadThread.method_108(var1, "");
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

        System.arraycopy(lightTable, 0, this.dimension.lightTable, 0, lightTable.length);
    }

    @Override
    public Entity getEntityByID(int id) {
        for (Entity entity : this.entities) {
            if (entity.entityId == id) {
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
    public BlockEntity getBlockTileEntityDontCreate(int x, int y, int z) {
        Chunk chunk = this.getChunkFromCache(x >> 4, z >> 4);
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
