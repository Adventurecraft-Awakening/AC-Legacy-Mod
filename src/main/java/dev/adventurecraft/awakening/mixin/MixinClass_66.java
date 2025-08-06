package dev.adventurecraft.awakening.mixin;

import dev.adventurecraft.awakening.ACMod;
import dev.adventurecraft.awakening.client.rendering.MemoryTesselator;
import dev.adventurecraft.awakening.collections.IdentityHashSet;
import dev.adventurecraft.awakening.extension.ExClass_66;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.client.options.ExGameOptions;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import dev.adventurecraft.awakening.extension.client.util.ExCameraView;
import dev.adventurecraft.awakening.extension.world.chunk.ExChunk;
import dev.adventurecraft.awakening.extension.world.level.ExRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Chunk;
import net.minecraft.client.renderer.Textures;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderDispatcher;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Region;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.phys.AABB;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mixin(
    value = Chunk.class,
    priority = 999
)
public abstract class MixinClass_66 implements ExClass_66 {

    private static final int MAX_RENDER_LAYERS = 2;
    private static final int MAX_TEXTURES = 4;

    @Shadow public Level level;
    @Shadow private int lists;
    @Shadow public int x;
    @Shadow public int y;
    @Shadow public int z;
    @Shadow public int xs;
    @Shadow public int ys;
    @Shadow public int zs;
    @Shadow public int xRenderOffs;
    @Shadow public int yRenderOffs;
    @Shadow public int zRenderOffs;
    @Shadow public boolean visible;
    @Shadow public boolean[] empty;
    @Shadow public boolean dirty;
    @Shadow public AABB bb;
    @Shadow public boolean occlusion_visible;
    @Shadow public boolean skyLit;
    @Shadow private boolean compiled;
    @Shadow private List<TileEntity> globalRenderableTileEntities;

    @Unique public boolean isVisibleFromPosition = false;
    @Unique public double visibleFromX;
    @Unique public double visibleFromY;
    @Unique public double visibleFromZ;
    @Unique private boolean needsBoxUpdate = false;
    @Unique public boolean isInFrustrumFully = false;

    @Unique private final IntBuffer vertexBuffers = MemoryUtil.memAllocInt(MAX_RENDER_LAYERS * MAX_TEXTURES);

    @Unique private final Set<TileEntity> tileEntities = new IdentityHashSet<>();

    @Shadow
    public abstract void setDirty();

    @Inject(
        method = "setPos",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/phys/AABB;create(DDDDDD)Lnet/minecraft/world/phys/AABB;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    public void setNeedsBoxUpdate(int x, int y, int z, CallbackInfo ci) {
        this.bb = AABB.create(x, y, z, x + this.xs, y + this.ys, z + this.zs);
        this.needsBoxUpdate = true;
        this.setDirty();
        this.isVisibleFromPosition = false;
        ci.cancel();
    }

    @Unique
    private static void printTime(@Nullable StringBuilder builder, String prefix, long startTime) {
        if (builder == null) {
            return;
        }

        double millis = (System.nanoTime() - startTime) / 1000000.0;
        builder.append(prefix).append(String.format(": %.3f ms\n", millis));
    }

    private @Unique void deleteBuffers() {
        if (this.vertexBuffers.hasRemaining()) {
            GL15.glDeleteBuffers(this.vertexBuffers);
        }
        this.vertexBuffers.clear();
    }

    @Inject(
        method = "reset",
        at = @At("HEAD")
    )
    private void onReset(CallbackInfo ci) {
        this.deleteBuffers();
    }

    @Overwrite
    public void rebuild() {
        if (!this.dirty) {
            return;
        }
        long timeStart = System.nanoTime();
        var timeBuilder = ACMod.LOGGER.isTraceEnabled() ? new StringBuilder() : null;

        ++Chunk.updates;
        if (this.needsBoxUpdate && Minecraft.instance.options.advancedOpengl) {
            GL11.glNewList(this.lists + 2, GL11.GL_COMPILE);
            ItemRenderer.renderFlat(AABB.newTemp(
                this.xRenderOffs,
                this.yRenderOffs,
                this.zRenderOffs,
                this.xRenderOffs + this.xs,
                this.yRenderOffs + this.ys,
                this.zRenderOffs + this.zs
            ));
            GL11.glEndList();
            this.needsBoxUpdate = false;

            printTime(timeBuilder, "Box Update", timeStart);
        }

        this.occlusion_visible = true;
        this.isVisibleFromPosition = false;
        final int startX = this.x;
        final int startY = this.y;
        final int startZ = this.z;
        final int endX = startX + this.xs;
        final int endY = startY + this.ys;
        final int endZ = startZ + this.zs;

        Arrays.fill(this.empty, true);

        this.deleteBuffers();

        LevelChunk.touchedSky = false;

        int regionPadding = 1;
        var region = new Region(
            this.level,
            startX - regionPadding,
            startY - regionPadding,
            startZ - regionPadding,
            endX + regionPadding,
            endY + regionPadding,
            endZ + regionPadding
        );
        printTime(timeBuilder, "Region Setup", timeStart);

        Textures texMan = Minecraft.instance.textures;
        int[] textures = new int[] {
            texMan.loadTexture("/terrain.png"), 0, texMan.loadTexture("/terrain2.png"),
            texMan.loadTexture("/terrain3.png")
        };

        var renderers = new TileRenderer[MAX_RENDER_LAYERS * textures.length];
        var renderTracker = new boolean[MAX_RENDER_LAYERS];

        var newSet = new IdentityHashSet<TileEntity>();
        var oldSet = new IdentityHashSet<>(this.tileEntities);

        var blockBuffer = ByteBuffer.allocate((endX - startX) * (endZ - startZ) * (endY - startY));

        for (int x = startX; x < endX; ++x) {
            for (int z = startZ; z < endZ; ++z) {
                int start = blockBuffer.position();
                ((ExRegion) region).getTileColumn(blockBuffer, x, startY, z, endY);

                ByteBuffer column = blockBuffer.slice(start, endY - startY);
                for (int y = startY; y < endY; ++y) {
                    int blockId = ExChunk.translate256(column.get());
                    if (!Tile.isEntityTile[blockId]) {
                        continue;
                    }

                    TileEntity entity = region.getTileEntity(x, y, z);
                    if (TileEntityRenderDispatcher.instance.hasTileEntityRenderer(entity)) {
                        if (this.tileEntities.add(entity)) {
                            newSet.add(entity);
                        }
                        else {
                            oldSet.remove(entity);
                        }
                    }
                }
            }
        }
        blockBuffer.flip();
        printTime(timeBuilder, "Tile Copy", timeStart);

        if (!oldSet.isEmpty()) {
            this.tileEntities.removeAll(oldSet);
            // TODO: turn global List into Set
            this.globalRenderableTileEntities.removeAll(oldSet);
        }
        this.globalRenderableTileEntities.addAll(newSet);

        for (int x = startX; x < endX; ++x) {
            for (int z = startZ; z < endZ; ++z) {
                for (int y = startY; y < endY; ++y) {
                    int blockId = ExChunk.translate256(blockBuffer.get());
                    if (blockId <= 0) {
                        continue;
                    }

                    Tile block = Tile.tiles[blockId];
                    int texId = ((ExBlock) block).getTextureNum();
                    int layer = block.getRenderLayer();

                    int meshIndex = (layer * textures.length) + texId;
                    var renderer = renderers[meshIndex];
                    if (renderer == null) {
                        renderer = new TileRenderer(region);
                        var tesselator = MemoryTesselator.create();
                        tesselator.begin();

                        ((ExBlockRenderer) renderer).setTesselator(tesselator);
                        renderers[meshIndex] = renderer;
                    }

                    renderTracker[layer] |= renderer.tesselateInWorld(block, x, y, z);
                }
            }
        }
        printTime(timeBuilder, "Tessellate", timeStart);

        for (int layer = 0; layer < MAX_RENDER_LAYERS; ++layer) {
            if (!renderTracker[layer]) {
                // Clear the old list.
                GL11.glNewList(this.lists + layer, GL11.GL_COMPILE);
                GL11.glEndList();
                continue;
            }

            boolean hasMesh = false;

            for (int texId = 0; texId < textures.length; ++texId) {
                int meshIndex = (layer * textures.length) + texId;
                var renderer = renderers[meshIndex];
                if (renderer == null) {
                    continue;
                }

                var tesselator = (MemoryTesselator) ((ExBlockRenderer) renderer).getTesselator();
                tesselator.end();
                if (tesselator.isEmpty()) {
                    continue;
                }

                if (!hasMesh) {
                    hasMesh = true;
                    GL11.glNewList(this.lists + layer, GL11.GL_COMPILE);

                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                }

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures[texId]);

                int vboId = GL15.glGenBuffers();
                this.vertexBuffers.put(vboId);

                final int target = GL15.GL_ARRAY_BUFFER;
                GL15.glBindBuffer(target, vboId);
                tesselator.render(target);

                printTime(timeBuilder, "  Render with Texture " + texId, timeStart);
            }

            if (hasMesh) {
                this.empty[layer] = false;

                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL11.glEndList();

                printTime(timeBuilder, "End Render Layer " + layer, timeStart);
            }
        }
        this.vertexBuffers.flip();

        this.skyLit = LevelChunk.touchedSky; // TODO: move global into Region/TileRenderer
        this.compiled = true;

        if (timeBuilder != null) {
            ACMod.LOGGER.trace("Chunk at X:{} Y:{} Z:{} - build time: \n{}", this.x, this.y, this.z, timeBuilder);
        }
    }

    @Inject(
        method = "cull",
        at = @At("TAIL")
    )
    private void fancyOcclusionCulling(Culler var1, CallbackInfo ci) {
        if (this.visible && ((ExGameOptions) Minecraft.instance.options).isOcclusionFancy()) {
            this.isInFrustrumFully = ((ExCameraView) var1).isBoundingBoxInFrustumFully(this.bb);
        }
        else {
            this.isInFrustrumFully = false;
        }
    }

    @Override
    public void setVisibleFromPosition(double x, double y, double z, boolean value) {
        this.visibleFromX = x;
        this.visibleFromY = y;
        this.visibleFromZ = z;
        this.isVisibleFromPosition = value;
    }

    @Override
    public boolean isVisibleFromPosition() {
        return this.isVisibleFromPosition;
    }

    @Override
    public void isVisibleFromPosition(boolean value) {
        this.isVisibleFromPosition = value;
    }

    @Override
    public double visibleFromX() {
        return this.visibleFromX;
    }

    @Override
    public void setVisibleFromX(double x) {
        this.visibleFromX = x;
    }

    @Override
    public double visibleFromY() {
        return this.visibleFromY;
    }

    @Override
    public void setVisibleFromY(double y) {
        this.visibleFromY = y;
    }

    @Override
    public double visibleFromZ() {
        return this.visibleFromZ;
    }

    @Override
    public void setVisibleFromZ(double z) {
        this.visibleFromZ = z;
    }

    @Override
    public boolean isInFrustrumFully() {
        return this.isInFrustrumFully;
    }
}