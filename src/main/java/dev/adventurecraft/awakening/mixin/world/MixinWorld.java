package dev.adventurecraft.awakening.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.adventurecraft.awakening.client.render.AC_TextureBinder;
import dev.adventurecraft.awakening.common.*;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExLadderBlock;
import dev.adventurecraft.awakening.extension.client.ExMinecraft;
import dev.adventurecraft.awakening.extension.client.ExTextureManager;
import dev.adventurecraft.awakening.extension.client.render.block.ExFoliageColor;
import dev.adventurecraft.awakening.extension.client.render.block.ExGrassColor;
import dev.adventurecraft.awakening.extension.client.resource.language.ExTranslationStorage;
import dev.adventurecraft.awakening.extension.entity.ExBlockEntity;
import dev.adventurecraft.awakening.extension.entity.ExEntity;
import dev.adventurecraft.awakening.extension.world.ExWorld;
import dev.adventurecraft.awakening.extension.world.ExWorldProperties;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunkCache;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

@Mixin(World.class)
public abstract class MixinWorld implements ExWorld, BlockView {

    @Shadow
    static int field_179;

    @Shadow
    private Set field_194;

    @Shadow
    public WorldProperties properties;

    @Shadow
    public MapTracker mapTracker;

    @Shadow
    public boolean field_221;

    @Shadow
    @Final
    public Dimension dimension;

    @Shadow
    public Random rand;

    @Shadow
    public WorldSource worldSource;

    @Shadow
    @Final
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
    protected float rainGradient;

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
    private TreeSet<class_366> treeSet;

    @Shadow
    public List<Entity> entities;

    @Shadow
    private List<class_417> lightingUpdates;

    public File levelDir;
    private int[] coordOrder;
    public String[] musicList;
    public String[] soundList;
    private DimensionData mapHandler;
    public AC_TriggerManager triggerManager = new AC_TriggerManager((World) (Object) this);
    public boolean fogColorOverridden;
    public boolean fogDensityOverridden;
    boolean firstTick = true;
    boolean newSave;
    public AC_UndoStack undoStack = new AC_UndoStack();
    /* TODO
    public Script script = new Script(this);
    public AC_JScriptHandler scriptHandler;
    public AC_MusicScripts musicScripts;
    public Scriptable scope;
    */

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
    protected abstract void method_245();

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

    @Override
    public void initWorld(String var1, DimensionData var2, String var3) {
        File var7 = Minecraft.getGameDirectory();
        File var8 = new File(var7, "../maps");
        File var9 = new File(var8, var1);
        ((ExTranslationStorage) TranslationStorage.getInstance()).loadMapTranslation(var9);
        this.mapHandler = new McRegionDimensionFile(var8, var1, false);
        this.levelDir = var9;
        this.field_194 = null;
        if (var2 != null) {
            this.mapTracker = new MapTracker(var2);
            this.properties = var2.getLevelProperties();
        } else {
            this.mapTracker = new MapTracker(this.mapHandler);
        }

        if (this.properties == null) {
            this.newSave = true;
            this.properties = this.mapHandler.getLevelProperties();
        }

        if (!AC_TerrainImage.loadMap(var9)) {
            AC_TerrainImage.loadMap(new File(new File(var7, "saves"), var3));
        }

        ((ExWorldProperties) this.properties).getWorldGenProps().useImages = AC_TerrainImage.isLoaded;
        if (((ExWorldProperties) this.properties).getTriggerData() != null) {
            this.triggerManager.loadFromTagCompound(((ExWorldProperties) this.properties).getTriggerData());
        }

        this.loadBrightness();

        if (this.newSave) {
            this.field_221 = true;
            int var11 = 0;

            int var12;
            for (var12 = 0; !this.dimension.canSpawnOn(var11, var12); var12 += this.rand.nextInt(64) - this.rand.nextInt(64)) {
                var11 += this.rand.nextInt(64) - this.rand.nextInt(64);
            }

            this.properties.setSpawnPosition(var11, this.getSurfaceBlockId(var11, var12), var12);
            this.field_221 = false;
        }

        /* TODO
        this.loadMapMusic();
        this.loadMapSounds();
        this.script = new Script(this);
        if(this.properties.globalScope != null) {
            ScopeTag.loadScopeFromTag(this.script.globalScope, this.properties.globalScope);
        }

        this.scriptHandler = new AC_JScriptHandler(this, var9);
        this.musicScripts = new AC_MusicScripts(this.script, var9, this.scriptHandler);
        if(this.properties.musicScope != null) {
            ScopeTag.loadScopeFromTag(this.musicScripts.scope, this.properties.musicScope);
        }

        this.scope = this.script.getNewScope();
        if(this.properties.worldScope != null) {
            ScopeTag.loadScopeFromTag(this.scope, this.properties.worldScope);
        }

        this.loadSoundOverrides();
        EntityDescriptions.loadDescriptions(new File(var9, "entitys"));
        AC_ItemCustom.loadItems(new File(var9, "items"));
        AC_TileEntityNpcPath.lastEntity = null;
        */
    }

    @Override
    public void loadMapTextures() {
        ExTextureManager texManager = ((ExTextureManager) Minecraft.instance.textureManager);
        Minecraft.instance.textureManager.reloadTexturesFromTexturePack();

        for (Object entry : Minecraft.instance.textureManager.textures.entrySet()) {
            Map.Entry<String, Integer> var3 = (Map.Entry<String, Integer>) entry;
            String var4 = var3.getKey();
            int var5 = var3.getValue();

            try {
                texManager.loadTexture(var5, var4);
            } catch (IllegalArgumentException var7) {
            }
        }

        this.loadTextureAnimations();
        AC_TextureFanFX.loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(FireTextureBinder.class)).loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(FlowingLavaTextureBinder.class)).loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(FlowingLavaTextureBinder2.class)).loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(PortalTextureBinder.class)).loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(FlowingWaterTextureBinder2.class)).loadImage();
        ((AC_TextureBinder) texManager.getTextureBinder(FlowingWaterTextureBinder.class)).loadImage();
        ExGrassColor.loadGrass("/misc/grasscolor.png");
        ExFoliageColor.loadFoliage("/misc/foliagecolor.png");
        ((ExWorldProperties) this.properties).loadTextureReplacements((World) (Object) this);
    }

    private void loadTextureAnimations() {
        ExTextureManager texManager = ((ExTextureManager) Minecraft.instance.textureManager);
        texManager.clearTextureAnimations();
        File var1 = new File(this.levelDir, "animations.txt");
        if (var1.exists()) {
            try {
                BufferedReader var2 = new BufferedReader(new FileReader(var1));

                try {
                    while (var2.ready()) {
                        String var3 = var2.readLine();
                        String[] var4 = var3.split(",", 7);
                        if (var4.length == 7) {
                            try {
                                String var5 = var4[1].trim();
                                String var6 = var4[2].trim();
                                int var7 = Integer.parseInt(var4[3].trim());
                                int var8 = Integer.parseInt(var4[4].trim());
                                int var9 = Integer.parseInt(var4[5].trim());
                                int var10 = Integer.parseInt(var4[6].trim());
                                AC_TextureAnimated var11 = new AC_TextureAnimated(var5, var6, var7, var8, var9, var10);
                                texManager.registerTextureAnimation(var4[0].trim(), var11);
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
    }

    @Override
    public BufferedImage loadMapTexture(String var1) {
        File var2 = new File(this.levelDir, var1);
        if (var2.exists()) {
            try {
                BufferedImage var3 = ImageIO.read(var2);
                return var3;
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
        ChunkIO var1;
        if (this.dimensionData == null) {
            var1 = this.mapHandler.getChunkIO(this.dimension);
        } else {
            var1 = this.dimensionData.getChunkIO(this.dimension);
            if (this.mapHandler != null) {
                var1 = new MapChunkLoader(this.mapHandler.getChunkIO(this.dimension), var1);
            }
        }

        try {
            ChunkCache cache = (ChunkCache) getUnsafe().allocateInstance(ChunkCache.class);
            ((ExChunkCache) cache).init((World) (Object) this, var1, this.dimension.createWorldSource());
            return cache;
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
        singleoneInstanceField.setAccessible(true);
        return (Unsafe) singleoneInstanceField.get(null);
    }

    @Redirect(method = "method_212", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldProperties;setSpawnPosition(III)V"))
    private void spawnAtUncoveredBlock(WorldProperties instance, int var1, int var2, int var3) {
        this.properties.setSpawnPosition(var1, this.getFirstUncoveredBlockY(var1, var3), var3);
    }

    @Inject(method = "initSpawnPoint", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldProperties;setSpawnZ(I)V",
            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void spawnAtUncoveredBlock(CallbackInfo ci, int var1, int var2) {
        this.properties.setSpawnY(this.getFirstUncoveredBlockY(var1, var2));
    }

    public int getFirstUncoveredBlockY(int var1, int var2) {
        int var3;
        var3 = 127;
        while (this.isAir(var1, var3, var2) && var3 > 0) {
            --var3;
        }
        return var3;
    }

    @Overwrite
    public int getSurfaceBlockId(int var1, int var2) {
        int var3 = 127;
        while (this.isAir(var1, var3, var2) && var3 > 0) {
            --var3;
        }
        return this.getBlockId(var1, var3, var2);
    }

    @Redirect(method = "method_271", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/dimension/DimensionData;saveWorldDataOnServer(Lnet/minecraft/world/WorldProperties;Ljava/util/List;)V"))
    private void modifySave(DimensionData instance, WorldProperties worldProperties, List<PlayerEntity> list) {
        /* TODO
        worldProperties.globalScope = ScopeTag.getTagFromScope(this.script.globalScope);
        worldProperties.worldScope = ScopeTag.getTagFromScope(this.scope);
        worldProperties.musicScope = ScopeTag.getTagFromScope(this.musicScripts.scope);
        */
        if (this.dimensionData != null) {
            this.dimensionData.saveWorldDataOnServer(worldProperties, list);
        }

        if (AC_DebugMode.levelEditing || this.dimensionData == null) {
            this.mapHandler.saveWorldDataOnServer(worldProperties, list);
        }
    }

    public boolean setBlockAndMetadataTemp(int var1, int var2, int var3, int var4, int var5) {
        if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
            if (var2 < 0) {
                return false;
            } else if (var2 >= 128) {
                return false;
            } else {
                Chunk var6 = this.getChunkFromCache(var1 >> 4, var3 >> 4);
                return ((ExChunk) var6).setBlockIDWithMetadataTemp(var1 & 15, var2, var3 & 15, var4, var5);
            }
        } else {
            return false;
        }
    }

    @Overwrite
    public int placeBlock(int var1, int var2, int var3, boolean var4) {
        if (var1 >= -32000000 && var3 >= -32000000 && var1 < 32000000 && var3 <= 32000000) {
            if (var4) {
                int var5 = this.getBlockId(var1, var2, var3);
                if (var5 != 0 && (var5 == Block.STONE_SLAB.id || var5 == Block.FARMLAND.id || var5 == Block.COBBLESTONE_STAIRS.id || var5 == Block.WOOD_STAIRS.id || Block.BY_ID[var5] instanceof AC_BlockStairMulti)) {
                    int var6 = this.placeBlock(var1, var2 + 1, var3, false);
                    int var7 = this.placeBlock(var1 + 1, var2, var3, false);
                    int var8 = this.placeBlock(var1 - 1, var2, var3, false);
                    int var9 = this.placeBlock(var1, var2, var3 + 1, false);
                    int var10 = this.placeBlock(var1, var2, var3 - 1, false);
                    if (var7 > var6) {
                        var6 = var7;
                    }

                    if (var8 > var6) {
                        var6 = var8;
                    }

                    if (var9 > var6) {
                        var6 = var9;
                    }

                    if (var10 > var6) {
                        var6 = var10;
                    }

                    return var6;
                }
            }

            if (var2 < 0) {
                return 0;
            } else {
                if (var2 >= 128) {
                    var2 = 127;
                }

                Chunk var11 = this.getChunkFromCache(var1 >> 4, var3 >> 4);
                var1 &= 15;
                var3 &= 15;
                return var11.method_880(var1, var2, var3, this.field_202);
            }
        } else {
            return 15;
        }
    }

    @Overwrite
    public void method_165(LightType var1, int var2, int var3, int var4, int var5) {
        if (!this.dimension.halvesMapping || var1 != LightType.field_2757) {
            if (this.isBlockLoaded(var2, var3, var4)) {
                if (var1 == LightType.field_2757) {
                    if (this.isAboveGround(var2, var3, var4)) {
                        var5 = 15;
                    }
                } else if (var1 == LightType.field_2758) {
                    int var6 = this.getBlockId(var2, var3, var4);
                    if (Block.BY_ID[var6] != null && ((ExBlock) Block.BY_ID[var6]).getBlockLightValue(this, var2, var3, var4) < var5) {
                        var5 = ((ExBlock) Block.BY_ID[var6]).getBlockLightValue(this, var2, var3, var4);
                    }
                }

                if (this.method_164(var1, var2, var3, var4) != var5) {
                    this.method_166(var1, var2, var3, var4, var2, var3, var4);
                }
            }
        }
    }

    public float getLightValue(int var1, int var2, int var3) {
        int var4 = this.placeBlock(var1, var2, var3);
        float var5 = AC_PlayerTorch.getTorchLight((World) (Object) this, var1, var2, var3);
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
    public float getNaturalBrightness(int var1, int var2, int var3, int var4) {
        float var5 = this.getLightValue(var1, var2, var3);
        if (var5 < (float) var4) {
            var5 = (float) var4;
        }

        return this.getBrightnessLevel(var5);
    }

    @Overwrite
    public float method_1782(int var1, int var2, int var3) {
        float var4 = this.getLightValue(var1, var2, var3);
        return this.getBrightnessLevel(var4);
    }

    public float getDayLight() {
        int var1 = 15 - this.field_202;
        return this.dimension.lightTable[var1];
    }

    @Overwrite
    public HitResult method_162(Vec3d var1, Vec3d var2, boolean var3, boolean var4) {
        return this.rayTraceBlocks2(var1, var2, var3, var4, true);
    }

    public HitResult rayTraceBlocks2(Vec3d var1, Vec3d var2, boolean var3, boolean var4, boolean var5) {
        if (!Double.isNaN(var1.x) && !Double.isNaN(var1.y) && !Double.isNaN(var1.z)) {
            if (!Double.isNaN(var2.x) && !Double.isNaN(var2.y) && !Double.isNaN(var2.z)) {
                int var6 = MathHelper.floor(var2.x);
                int var7 = MathHelper.floor(var2.y);
                int var8 = MathHelper.floor(var2.z);
                int var9 = MathHelper.floor(var1.x);
                int var10 = MathHelper.floor(var1.y);
                int var11 = MathHelper.floor(var1.z);
                int var12 = this.getBlockId(var9, var10, var11);
                int var13 = this.getBlockMeta(var9, var10, var11);
                Block var14 = Block.BY_ID[var12];
                if ((!var4 || var14 == null || var14.getCollisionShape((World) (Object) this, var9, var10, var11) != null) && var12 > 0 && var14.isCollidable(var13, var3) && (var5 || var12 != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(var12))) {
                    HitResult var15 = var14.method_1564((World) (Object) this, var9, var10, var11, var1, var2);
                    if (var15 != null) {
                        return var15;
                    }
                }

                int var43 = 200;

                while (var43-- >= 0) {
                    if (!Double.isNaN(var1.x) && !Double.isNaN(var1.y) && !Double.isNaN(var1.z)) {
                        if (var9 == var6 && var10 == var7 && var11 == var8) {
                            return null;
                        }

                        boolean var16 = true;
                        boolean var17 = true;
                        boolean var18 = true;
                        double var19 = 999.0D;
                        double var21 = 999.0D;
                        double var23 = 999.0D;
                        if (var6 > var9) {
                            var19 = (double) var9 + 1.0D;
                        } else if (var6 < var9) {
                            var19 = (double) var9 + 0.0D;
                        } else {
                            var16 = false;
                        }

                        if (var7 > var10) {
                            var21 = (double) var10 + 1.0D;
                        } else if (var7 < var10) {
                            var21 = (double) var10 + 0.0D;
                        } else {
                            var17 = false;
                        }

                        if (var8 > var11) {
                            var23 = (double) var11 + 1.0D;
                        } else if (var8 < var11) {
                            var23 = (double) var11 + 0.0D;
                        } else {
                            var18 = false;
                        }

                        double var25 = 999.0D;
                        double var27 = 999.0D;
                        double var29 = 999.0D;
                        double var31 = var2.x - var1.x;
                        double var33 = var2.y - var1.y;
                        double var35 = var2.z - var1.z;
                        if (var16) {
                            var25 = (var19 - var1.x) / var31;
                        }

                        if (var17) {
                            var27 = (var21 - var1.y) / var33;
                        }

                        if (var18) {
                            var29 = (var23 - var1.z) / var35;
                        }

                        boolean var37 = false;
                        byte var44;
                        if (var25 < var27 && var25 < var29) {
                            if (var6 > var9) {
                                var44 = 4;
                            } else {
                                var44 = 5;
                            }

                            var1.x = var19;
                            var1.y += var33 * var25;
                            var1.z += var35 * var25;
                        } else if (var27 < var29) {
                            if (var7 > var10) {
                                var44 = 0;
                            } else {
                                var44 = 1;
                            }

                            var1.x += var31 * var27;
                            var1.y = var21;
                            var1.z += var35 * var27;
                        } else {
                            if (var8 > var11) {
                                var44 = 2;
                            } else {
                                var44 = 3;
                            }

                            var1.x += var31 * var29;
                            var1.y += var33 * var29;
                            var1.z = var23;
                        }

                        Vec3d var38 = Vec3d.from(var1.x, var1.y, var1.z);
                        var9 = (int) (var38.x = MathHelper.floor(var1.x));
                        if (var44 == 5) {
                            --var9;
                            ++var38.x;
                        }

                        var10 = (int) (var38.y = MathHelper.floor(var1.y));
                        if (var44 == 1) {
                            --var10;
                            ++var38.y;
                        }

                        var11 = (int) (var38.z = MathHelper.floor(var1.z));
                        if (var44 == 3) {
                            --var11;
                            ++var38.z;
                        }

                        int var39 = this.getBlockId(var9, var10, var11);
                        int var40 = this.getBlockMeta(var9, var10, var11);
                        Block var41 = Block.BY_ID[var39];
                        if (var4 && var41 != null && var41.getCollisionShape((World) (Object) this, var9, var10, var11) == null || var39 <= 0 || !var41.isCollidable(var40, var3) || !((ExBlock) var41).shouldRender(this, var9, var10, var11)) {
                            continue;
                        }

                        HitResult var42 = var41.method_1564((World) (Object) this, var9, var10, var11, var1, var2);
                        if (var42 == null || !var5 && (var41.id == AC_Blocks.clipBlock.id || ExLadderBlock.isLadderID(var41.id))) {
                            continue;
                        }

                        return var42;
                    }

                    return null;
                }

                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Redirect(method = "spawnEntity", at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
            ordinal = 1))
    private <E> boolean spawnIfNotExisting(List<E> instance, E var1) {
        if (!instance.contains(var1)) {
            return instance.add(var1);
        }
        return false;
    }

    @Inject(method = "method_190", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;doesBoxCollide(Lnet/minecraft/world/World;IIILnet/minecraft/util/math/AxixAlignedBoundingBox;Ljava/util/ArrayList;)V",
            shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void addBoxCollide(Entity var1, AxixAlignedBoundingBox var2, CallbackInfoReturnable<List<AxixAlignedBoundingBox>> cir, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, Block var12) {
        if ((((ExEntity) var1).getCollidesWithClipBlocks() || var12.id != AC_Blocks.clipBlock.id && !ExLadderBlock.isLadderID(var12.id))) {
            var12.doesBoxCollide((World) (Object) this, var9, var11, var10, var2, this.field_189);
        }
    }

    @Redirect(method = "method_190", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;doesBoxCollide(Lnet/minecraft/world/World;IIILnet/minecraft/util/math/AxixAlignedBoundingBox;Ljava/util/ArrayList;)V"))
    private void redirectBoxCollide(Block instance, World var1, int var2, int var3, int var4, AxixAlignedBoundingBox var5, ArrayList<AxixAlignedBoundingBox> var6) {
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
    public int method_228(int var1, int var2) {
        Chunk var3 = this.getChunk(var1, var2);

        int var4 = 127;
        while (this.getMaterial(var1, var4, var2).blocksMovement() && var4 > 0) {
            --var4;
        }

        var1 &= 15;

        for (var2 &= 15; var4 > 0; --var4) {
            int var5 = var3.getBlockId(var1, var4, var2);
            Material var6 = var5 != 0 ? Block.BY_ID[var5].material : Material.AIR;
            if (var6.blocksMovement() || var6.isLiquid()) {
                return var4 + 1;
            }
        }

        return -1;
    }

    @ModifyExpressionValue(method = "method_227", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;removed:Z",
            ordinal = 2))
    private boolean fixupRemoveCondition(boolean value, @Local Entity var2) {
        ExMinecraft mc = (ExMinecraft) Minecraft.instance;
        return value && !var2.removed && (!mc.isCameraActive() || !mc.isCameraPause()) && (!AC_DebugMode.active || var2 instanceof PlayerEntity);
    }

    @Inject(method = "method_227", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;method_241(Lnet/minecraft/entity/Entity;)V",
            shift = At.Shift.AFTER))
    private void fixupBoundingBox(CallbackInfo ci) {
        AxixAlignedBoundingBox.method_85();
    }

    @Redirect(method = "method_227", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getChunkFromCache(II)Lnet/minecraft/world/chunk/Chunk;"))
    private Chunk ignoreIfKilledOnSave(World instance, int j, int i, @Local BlockEntity var5) {
        if (((ExBlockEntity) var5).isKilledFromSaving()) {
            return null;
        }
        Chunk var7 = this.getChunkFromCache(var5.x >> 4, var5.z >> 4);
        return var7;
    }

    @Overwrite
    public void method_193(Entity var1, boolean var2) {
        int var3 = MathHelper.floor(var1.x);
        int var4 = MathHelper.floor(var1.z);
        byte var5 = 32;
        if (!var2 || this.method_155(var3 - var5, 0, var4 - var5, var3 + var5, 128, var4 + var5)) {
            var1.prevRenderX = var1.x;
            var1.prevRenderY = var1.y;
            var1.prevRenderZ = var1.z;
            var1.prevYaw = var1.yaw;
            var1.prevPitch = var1.pitch;
            if (var2 && var1.field_1618) {
                int stunned = ((ExEntity) var1).getStunned();
                if (stunned > 0) {
                    ((ExEntity) var1).setStunned(stunned - 1);
                } else if (var1.vehicle != null) {
                    var1.tickRiding();
                } else {
                    var1.tick();
                }
            }

            if (Double.isNaN(var1.x) || Double.isInfinite(var1.x)) {
                var1.x = var1.prevRenderX;
            }

            if (Double.isNaN(var1.y) || Double.isInfinite(var1.y)) {
                var1.y = var1.prevRenderY;
            }

            if (Double.isNaN(var1.z) || Double.isInfinite(var1.z)) {
                var1.z = var1.prevRenderZ;
            }

            if (Double.isNaN(var1.pitch) || Double.isInfinite(var1.pitch)) {
                var1.pitch = var1.prevPitch;
            }

            if (Double.isNaN(var1.yaw) || Double.isInfinite(var1.yaw)) {
                var1.yaw = var1.prevYaw;
            }

            int var6 = MathHelper.floor(var1.x / 16.0D);
            int var7 = MathHelper.floor(var1.y / 16.0D);
            int var8 = MathHelper.floor(var1.z / 16.0D);
            if (!var1.field_1618 || var1.chunkX != var6 || var1.chunkIndex != var7 || var1.chunkZ != var8) {
                if (var1.field_1618 && this.isChunkLoaded(var1.chunkX, var1.chunkZ)) {
                    this.getChunkFromCache(var1.chunkX, var1.chunkZ).removeEntity(var1, var1.chunkIndex);
                }

                if (this.isChunkLoaded(var6, var8)) {
                    var1.field_1618 = true;
                    this.getChunkFromCache(var6, var8).addEntity(var1);
                } else {
                    var1.field_1618 = false;
                }
            }

            if (var2 && var1.field_1618 && var1.passenger != null) {
                if (!var1.passenger.removed && var1.passenger.vehicle == var1) {
                    this.method_241(var1.passenger);
                } else {
                    var1.passenger.vehicle = null;
                    var1.passenger = null;
                }
            }
        }
    }

    @Inject(method = "setBlockEntity", at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/World;field_190:Z",
            shift = At.Shift.BEFORE))
    private void removeBlockEntityOnSet(int var1, int var2, int var3, BlockEntity var4, CallbackInfo ci) {
        this.removeBlockEntity(var1, var2, var3);
    }

    @Redirect(method = "removeBlockEntity", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getBlockEntity(III)Lnet/minecraft/entity/BlockEntity;"))
    private BlockEntity removeBlockEntityDontCreate(World instance, int var1, int var2, int var3) {
        return this.getBlockTileEntityDontCreate(var1, var2, var3);
    }

    @Overwrite
    public void method_167(LightType var1, int var2, int var3, int var4, int var5, int var6, int var7, boolean var8) {
        if (!this.dimension.halvesMapping || var1 != LightType.field_2757) {
            ++field_179;

            try {
                if (field_179 == 50) {
                    return;
                }

                int var9 = (var5 + var2) / 2;
                int var10 = (var7 + var4) / 2;
                if (!this.isBlockLoaded(var9, 64, var10)) {
                    return;
                }

                if (this.getChunk(var9, var10).method_886()) {
                    return;
                }

                int var11 = this.lightingUpdates.size();
                int var12;
                if (var8) {
                    var12 = 5;
                    if (var12 > var11) {
                        var12 = var11;
                    }

                    for (int var13 = 0; var13 < var12; ++var13) {
                        class_417 var14 = this.lightingUpdates.get(this.lightingUpdates.size() - var13 - 1);
                        if (var14.field_1673 == var1 && var14.method_1401(var2, var3, var4, var5, var6, var7)) {
                            return;
                        }
                    }
                }

                this.lightingUpdates.add(new class_417(var1, var2, var3, var4, var5, var6, var7));
                var12 = 1000000;
                if (this.lightingUpdates.size() > 1000000) {
                    System.out.println("More than " + var12 + " updates, aborting lighting updates");
                    this.lightingUpdates.clear();
                }
            } finally {
                --field_179;
            }
        }
    }

    @Overwrite
    public void method_242() {
        ExWorldProperties props = (ExWorldProperties) this.properties;
        /* TODO
        if (this.firstTick) {
            if (this.newSave && !props.onNewSaveScript.equals("")) {
                this.scriptHandler.runScript(props.onNewSaveScript, this.scope);
            }

            if (!props.onLoadScript.equals("")) {
                this.scriptHandler.runScript(props.onLoadScript, this.scope);
            }

            this.firstTick = false;
        }

        ScriptModel.updateAll();
        if (!this.properties.onUpdateScript.equals("")) {
            this.scriptHandler.runScript(props.onUpdateScript, this.scope);
        }
        */

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

        // this.script.wakeupScripts(var4); TODO
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
    public void cancelBlockUpdate(int var1, int var2, int var3, int var4) {
        class_366 var5 = new class_366(var1, var2, var3, var4);
        this.field_184.remove(var5);
    }

    @Override
    public void loadMapMusic() {
        File var1 = new File(this.levelDir, "music");
        if (var1.exists() && var1.isDirectory()) {
            int var2 = 0;
            File[] var3 = var1.listFiles();
            File[] var4 = var3;
            int var5 = var3.length;

            int var6;
            File var7;
            String var8;
            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                if (var7.isFile() && var7.getName().endsWith(".ogg")) {
                    var8 = String.format("music/%s", var7.getName().toLowerCase());
                    Minecraft.instance.soundHelper.addStreaming(var8, var7);
                    ++var2;
                }
            }

            this.musicList = new String[var2];
            var2 = 0;
            var4 = var3;
            var5 = var3.length;

            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                if (var7.isFile() && var7.getName().endsWith(".ogg")) {
                    var8 = String.format("music.%s", var7.getName().toLowerCase().replace(".ogg", ""));
                    this.musicList[var2] = var8;
                    ++var2;
                }
            }
        } else {
            this.musicList = new String[0];
        }

        String playingMusic = ((ExWorldProperties) this.properties).getPlayingMusic();
        if (!playingMusic.equals("")) {
            //Minecraft.instance.soundHelper.playMusicFromStreaming(playingMusic, 0, 0); TODO
        }
    }

    public void loadMapSounds() {
        File var1 = new File(this.levelDir, "sound");
        if (var1.exists() && var1.isDirectory()) {
            int var2 = 0;
            File[] var3 = var1.listFiles();
            File[] var4 = var3;
            int var5 = var3.length;

            int var6;
            File var7;
            String var8;
            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                if (var7.isFile() && var7.getName().endsWith(".ogg")) {
                    var8 = String.format("sound/%s", var7.getName().toLowerCase());
                    Minecraft.instance.soundHelper.addSound(var8, var7);
                    ++var2;
                }
            }

            this.soundList = new String[var2];
            var2 = 0;
            var4 = var3;
            var5 = var3.length;

            for (var6 = 0; var6 < var5; ++var6) {
                var7 = var4[var6];
                if (var7.isFile() && var7.getName().endsWith(".ogg")) {
                    var8 = String.format("sound.%s", var7.getName().toLowerCase().replace(".ogg", ""));
                    this.soundList[var2] = var8;
                    ++var2;
                }
            }
        } else {
            this.soundList = new String[0];
        }
    }

    public void loadSoundOverrides() {
        Minecraft.instance.resourceDownloadThread.method_107();
        File var1 = new File(this.levelDir, "soundOverrides");
        if (var1.exists()) {
            Minecraft.instance.resourceDownloadThread.method_108(var1, "");
        }
    }

    @Override
    public void loadBrightness() {
        float[] brightness = ((ExWorldProperties) this.properties).getBrightness();
        System.arraycopy(brightness, 0, this.dimension.lightTable, 0, 16);
    }

    @Override
    public void undo() {
        this.undoStack.undo((World) (Object) this);
    }

    @Override
    public void redo() {
        this.undoStack.redo((World) (Object) this);
    }

    public Entity getEntityByID(int var1) {
        Iterator<Entity> var2 = this.entities.iterator();

        Entity var3;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            var3 = var2.next();
        } while (var3.entityId != var1);

        return var3;
    }

    public float getFogStart(float var1, float var2) {
        ExWorldProperties props = (ExWorldProperties) this.properties;
        if (props.isOverrideFogDensity()) {
            if (this.fogDensityOverridden) {
                return props.getFogStart();
            }
            return var2 * props.getFogStart() + (1.0F - var2) * var1;
        }
        return var1;
    }

    public float getFogEnd(float var1, float var2) {
        ExWorldProperties props = (ExWorldProperties) this.properties;
        if (props.isOverrideFogDensity()) {
            if (this.fogDensityOverridden) {
                return props.getFogEnd();
            }
            return var2 * props.getFogEnd() + (1.0F - var2) * var1;
        }
        return var1;
    }

    public BlockEntity getBlockTileEntityDontCreate(int var1, int var2, int var3) {
        Chunk var4 = this.getChunkFromCache(var1 >> 4, var3 >> 4);
        if (var4 != null) {
            return ((ExChunk) var4).getChunkBlockTileEntityDontCreate(var1 & 15, var2, var3 & 15);
        }
        return null;
    }

    public double getTemperatureValue(int var1, int var2) {
        if (var1 >= -32000000 && var2 >= -32000000 && var1 < 32000000 && var2 <= 32000000) {
            ExChunk chunk = (ExChunk) this.getChunkFromCache(var1 >> 4, var2 >> 4);
            double tempValue = chunk.getTemperatureValue(var1 & 15, var2 & 15);
            double tempOffset = ((ExWorldProperties) this.properties).getTempOffset();
            return tempValue + tempOffset;
        }
        return 0.0D;
    }

    public void setTemperatureValue(int var1, int var2, double var3) {
        if (var1 >= -32000000 && var2 >= -32000000 && var1 < 32000000 && var2 <= 32000000) {
            ExChunk var5 = (ExChunk) this.getChunkFromCache(var1 >> 4, var2 >> 4);
            if (var5.getTemperatureValue(var1 & 15, var2 & 15) != var3) {
                var5.setTemperatureValue(var1 & 15, var2 & 15, var3);
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
    public float getTimeOfDay() {
        return (float) ((ExWorldProperties) this.properties).getTimeOfDay();
    }

    @Override
    public void setTimeOfDay(long var1) {
        ((ExWorldProperties) this.properties).setTimeOfDay((float) var1);
    }

    @Override
    public float getSpawnYaw() {
        return ((ExWorldProperties) this.properties).getSpawnYaw();
    }

    @Override
    public void setSpawnYaw(float var1) {
        ((ExWorldProperties) this.properties).setSpawnYaw(var1);
    }

    @Override
    public AC_UndoStack getUndoStack() {
        return this.undoStack;
    }

    @Override
    public String[] getMusicList() {
        return this.musicList;
    }

    @Override
    public String[] getSoundList() {
        return this.soundList;
    }

    @Override
    public AC_TriggerManager getTriggerManager() {
        return this.triggerManager;
    }
}
