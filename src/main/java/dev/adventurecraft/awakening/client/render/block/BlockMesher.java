package dev.adventurecraft.awakening.client.render.block;

import dev.adventurecraft.awakening.client.render.OffsetVertexSink;
import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.client.render.VertexFormat;
import dev.adventurecraft.awakening.client.render.RayLighting;
import dev.adventurecraft.awakening.client.render.material.MapMaterials;
import dev.adventurecraft.awakening.client.render.material.MaterialProfile;
import dev.adventurecraft.awakening.client.render.chunk.LevelRegion;
import dev.adventurecraft.awakening.common.AoHelper;
import dev.adventurecraft.awakening.extension.block.AC_TexturedBlock;
import dev.adventurecraft.awakening.extension.block.ExBlock;
import dev.adventurecraft.awakening.extension.block.ExGrassBlock;
import dev.adventurecraft.awakening.tile.AC_BlockOverlay;
import dev.adventurecraft.awakening.tile.AC_BlockShapes;
import dev.adventurecraft.awakening.tile.AC_Blocks;
import dev.adventurecraft.awakening.tile.entity.AC_TileEntityTree;
import dev.adventurecraft.awakening.util.Xoshiro128PP;
import net.minecraft.client.renderer.BlockShapes;
import net.minecraft.util.Directions;
import net.minecraft.util.Facing;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.tile.BedTile;
import net.minecraft.world.level.tile.DoorTile;
import net.minecraft.world.level.tile.LiquidTile;
import net.minecraft.world.level.tile.RailTile;
import net.minecraft.world.level.tile.RedStoneDustTile;
import net.minecraft.world.level.tile.RepeaterTile;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.entity.TileEntity;
import net.minecraft.world.level.tile.piston.PistonBaseTile;
import net.minecraft.world.level.tile.piston.PistonHeadTile;
import net.minecraft.world.phys.Vec3;

/** CPU block geometry, independent of the graphics API and vanilla's shared tessellator.
 * Geometry preserves the game's original face/AO/custom-shape recipes. Draw state belongs to callers.
 */
public final class BlockMesher {
    @FunctionalInterface public interface Activation { boolean active(int x, int y, int z); }
    public record Options(boolean ambientOcclusion, boolean anaglyph, boolean fancyGrass, float aoLevel, Activation activation) {
        public Options { java.util.Objects.requireNonNull(activation); }
    }
    private final VertexSink writer;
    private int materialTint = 0xFFFFFF;
    private int materialEmission;
    private int materialFlags;
    private int materialX, materialY, materialZ;
    private boolean terrainMaterial;
    private final Options options;
    private final float aoLightMin;
    private final float aoLightMax;
    private final Xoshiro128PP rand;
    private LevelSource level;
    private int fixedTexture = -1;
    private boolean xFlipTexture = false;
    private boolean noCulling = false;
    private final boolean fancy;
    private boolean inventoryColors = true;
    private int northUvRotation = 0;
    private int southUvRotation = 0;
    private int eastUvRotation = 0;
    private int westUvRotation = 0;
    private int topUvRotation = 0;
    private int bottomUvRotation = 0;
    private boolean blen;
    private float ll000;
    private float llx00;
    private float ll0y0;
    private float ll00z;
    private float llX00;
    private float ll0Y0;
    private float ll00Z;
    private float ll0yZ;
    private float llxyz;
    private float llXyz;
    private float llxy0;
    private float llxyZ;
    private float llXy0;
    private float ll0yz;
    private float llXyZ;
    private float llxYz;
    private float llxY0;
    private float llxYZ;
    private float ll0Yz;
    private float llXYz;
    private float llXY0;
    private float ll0YZ;
    private float llXYZ;
    private float llx0z;
    private float llX0z;
    private float llx0Z;
    private float llX0Z;
    private int blsmooth = 1;
    private float c1r;
    private float c2r;
    private float c3r;
    private float c4r;
    private float c1g;
    private float c2g;
    private float c3g;
    private float c4g;
    private float c1b;
    private float c2b;
    private float c3b;
    private float c4b;
    private boolean field_69;
    private boolean field_70;
    private boolean field_71;
    private boolean field_72;
    private boolean field_73;
    private boolean field_74;
    private boolean field_75;
    private boolean field_76;
    private boolean field_77;
    private boolean field_78;
    private boolean field_79;
    private boolean field_80;


    public BlockMesher(LevelSource world, VertexSink output, double originX, double originY, double originZ, Options options) {
        this(world, output, originX, originY, originZ, options, new Xoshiro128PP(),
            AoHelper.lightLevel0, AoHelper.lightLevel1);
    }

    /** Terrain builders supply captured AO values and an already-seeded random source. */
    public BlockMesher(LevelSource world, VertexSink output, double originX, double originY, double originZ,
                       Options options, Xoshiro128PP random, float aoLightMin, float aoLightMax) {
        this.level = world;
        this.writer = new OffsetVertexSink(output, -originX, -originY, -originZ);
        this.options = java.util.Objects.requireNonNull(options);
        this.rand = java.util.Objects.requireNonNull(random);
        this.aoLightMin = aoLightMin;
        this.aoLightMax = aoLightMax;
        this.fancy = options.fancyGrass();
    }
    private VertexSink tesselator() { return this.writer; }
    public VertexSink writer() { return this.writer; }
    public BlockMesher fixedTexture(int texture) { this.fixedTexture=texture;return this; }
    public BlockMesher inventoryColors(boolean enabled) { this.inventoryColors=enabled;return this; }
    public BlockMesher unculled(boolean value) { this.noCulling=value;return this; }
    public boolean world(Tile tile, int x, int y, int z) { return this.tesselateInWorld(tile,x,y,z); }
    public boolean worldUnculled(Tile tile, int x, int y, int z) {
        boolean previous=this.noCulling; this.noCulling=true;
        try { return this.world(tile,x,y,z); } finally { this.noCulling=previous; }
    }
    public boolean preview(Tile tile,int x,int y,int z,int metadata) {
        LevelSource previous=this.level;
        this.level=new BlockPreviewView(previous,x,y,z,tile.id,metadata);
        try { return this.worldUnculled(tile,x,y,z); } finally { this.level=previous; }
    }
    public void inventory(Tile tile, int metadata, float brightness) {
        // Inventory recipes adjust shape temporarily; never mutate the shared tile definition.
        this.renderTile(((ExBlock)tile).ac$clone(),metadata,brightness);
    }
    public void tesselateInWorld(Tile tile, int x, int y, int z, int destroyProgress) {
        int previous = this.fixedTexture;
        this.fixedTexture = destroyProgress;
        try { this.world(tile, x, y, z); }
        finally { this.fixedTexture = previous; }
    
    }

    public void tesselateInWorldNoCulling(Tile tile, int x, int y, int z) {
        this.worldUnculled(tile, x, y, z);
    
    }

    public boolean tesselateInWorld(Tile block, int x, int y, int z) {
        return this.ac$tesselateInWorld(block, x, y, z);
    }

    private boolean tesselateBedInWorld(Tile tile, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        int n = this.level.getData(x, y, z);
        int n2 = BedTile.getBedOrientation(n);
        boolean bl = BedTile.isHeadPiece(n);
        float f = 0.5f;
        float f2 = 1.0f;
        float f3 = 0.8f;
        float f4 = 0.6f;
        float f5 = f2;
        float f6 = f2;
        float f7 = f2;
        float f8 = f;
        float f9 = f3;
        float f10 = f4;
        float f11 = f;
        float f12 = f3;
        float f13 = f4;
        float f14 = f;
        float f15 = f3;
        float f16 = f4;
        float f17 = tile.getBrightness(this.level, x, y, z);
        tesselator.color(f8 * f17, f11 * f17, f14 * f17);
        int n3 = tile.getTexture(this.level, x, y, z, 0);
        int n4 = (n3 & 0xF) << 4;
        int n5 = n3 & 0xF0;
        double d = (float)n4 / 256.0f;
        double d2 = ((double)(n4 + 16) - 0.01) / 256.0;
        double d3 = (float)n5 / 256.0f;
        double d4 = ((double)(n5 + 16) - 0.01) / 256.0;
        double d5 = (double)x + tile.xx0;
        double d6 = (double)x + tile.xx1;
        double d7 = (double)y + tile.yy0 + 0.1875;
        double d8 = (double)z + tile.zz0;
        double d9 = (double)z + tile.zz1;
        tesselator.vertexUV(d5, d7, d9, d, d4);
        tesselator.vertexUV(d5, d7, d8, d, d3);
        tesselator.vertexUV(d6, d7, d8, d2, d3);
        tesselator.vertexUV(d6, d7, d9, d2, d4);
        float f18 = tile.getBrightness(this.level, x, y + 1, z);
        tesselator.color(f5 * f18, f6 * f18, f7 * f18);
        n4 = tile.getTexture(this.level, x, y, z, 1);
        n5 = (n4 & 0xF) << 4;
        int n6 = n4 & 0xF0;
        double d10 = (float)n5 / 256.0f;
        double d11 = ((double)(n5 + 16) - 0.01) / 256.0;
        double d12 = (float)n6 / 256.0f;
        double d13 = ((double)(n6 + 16) - 0.01) / 256.0;
        double d14 = d10;
        double d15 = d11;
        double d16 = d12;
        double d17 = d12;
        double d18 = d10;
        double d19 = d11;
        double d20 = d13;
        double d21 = d13;
        if (n2 == 0) {
            d15 = d10;
            d16 = d13;
            d18 = d11;
            d21 = d12;
        } else if (n2 == 2) {
            d14 = d11;
            d17 = d13;
            d19 = d10;
            d20 = d12;
        } else if (n2 == 3) {
            d14 = d11;
            d17 = d13;
            d19 = d10;
            d20 = d12;
            d15 = d10;
            d16 = d13;
            d18 = d11;
            d21 = d12;
        }
        double d22 = (double)x + tile.xx0;
        double d23 = (double)x + tile.xx1;
        double d24 = (double)y + tile.yy1;
        double d25 = (double)z + tile.zz0;
        double d26 = (double)z + tile.zz1;
        tesselator.vertexUV(d23, d24, d26, d18, d20);
        tesselator.vertexUV(d23, d24, d25, d14, d16);
        tesselator.vertexUV(d22, d24, d25, d15, d17);
        tesselator.vertexUV(d22, d24, d26, d19, d21);
        int n7 = Directions.field_792[n2];
        if (bl) {
            n7 = Directions.field_792[Directions.field_793[n2]];
        }
        n4 = 4;
        switch (n2) {
            case 2: {
                break;
            }
            case 0: {
                n4 = 5;
                break;
            }
            case 3: {
                n4 = 2;
                break;
            }
            case 1: {
                n4 = 3;
            }
        }
        if (n7 != 2 && (this.noCulling || tile.shouldRenderFace(this.level, x, y, z - 1, 2))) {
            float f19 = tile.getBrightness(this.level, x, y, z - 1);
            if (tile.zz0 > 0.0) {
                f19 = f17;
            }
            tesselator.color(f9 * f19, f12 * f19, f15 * f19);
            this.xFlipTexture = n4 == 2;
            this.renderNorth(tile, x, y, z, tile.getTexture(this.level, x, y, z, 2));
        }
        if (n7 != 3 && (this.noCulling || tile.shouldRenderFace(this.level, x, y, z + 1, 3))) {
            float f20 = tile.getBrightness(this.level, x, y, z + 1);
            if (tile.zz1 < 1.0) {
                f20 = f17;
            }
            tesselator.color(f9 * f20, f12 * f20, f15 * f20);
            this.xFlipTexture = n4 == 3;
            this.renderSouth(tile, x, y, z, tile.getTexture(this.level, x, y, z, 3));
        }
        if (n7 != 4 && (this.noCulling || tile.shouldRenderFace(this.level, x - 1, y, z, 4))) {
            float f21 = tile.getBrightness(this.level, x - 1, y, z);
            if (tile.xx0 > 0.0) {
                f21 = f17;
            }
            tesselator.color(f10 * f21, f13 * f21, f16 * f21);
            this.xFlipTexture = n4 == 4;
            this.renderWest(tile, x, y, z, tile.getTexture(this.level, x, y, z, 4));
        }
        if (n7 != 5 && (this.noCulling || tile.shouldRenderFace(this.level, x + 1, y, z, 5))) {
            float f22 = tile.getBrightness(this.level, x + 1, y, z);
            if (tile.xx1 < 1.0) {
                f22 = f17;
            }
            tesselator.color(f10 * f22, f13 * f22, f16 * f22);
            this.xFlipTexture = n4 == 5;
            this.renderEast(tile, x, y, z, tile.getTexture(this.level, x, y, z, 5));
        }
        this.xFlipTexture = false;
        return true;
    
    }

    public boolean tesselateTorchInWorld(Tile tile, int x, int y, int z) {
        int n = this.level.getData(x, y, z);
        VertexSink tesselator = this.writer;
        float f = tile.getBrightness(this.level, x, y, z);
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f = 1.0f;
        }
        tesselator.color(f, f, f);
        double d = 0.4f;
        double d2 = 0.5 - d;
        double d3 = 0.2f;
        if (n == 1) {
            this.tesselateTorch(tile, (double)x - d2, (double)y + d3, z, -d, 0.0);
        } else if (n == 2) {
            this.tesselateTorch(tile, (double)x + d2, (double)y + d3, z, d, 0.0);
        } else if (n == 3) {
            this.tesselateTorch(tile, x, (double)y + d3, (double)z - d2, 0.0, -d);
        } else if (n == 4) {
            this.tesselateTorch(tile, x, (double)y + d3, (double)z + d2, 0.0, d);
        } else {
            this.tesselateTorch(tile, x, y, z, 0.0, 0.0);
        }
        return true;
    
    }

    private boolean tesselateRepeaterInWorld(Tile tile, int x, int y, int z) {
        int n = this.level.getData(x, y, z);
        int n2 = n & 3;
        int n3 = (n & 0xC) >> 2;
        this.tesselateBlockInWorld(tile, x, y, z);
        VertexSink tesselator = this.writer;
        float f = tile.getBrightness(this.level, x, y, z);
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f = (f + 1.0f) * 0.5f;
        }
        tesselator.color(f, f, f);
        double d = -0.1875;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = 0.0;
        switch (n2) {
            case 0: {
                d5 = -0.3125;
                d3 = RepeaterTile.PARTICLE_OFFSETS[n3];
                break;
            }
            case 2: {
                d5 = 0.3125;
                d3 = -RepeaterTile.PARTICLE_OFFSETS[n3];
                break;
            }
            case 3: {
                d4 = -0.3125;
                d2 = RepeaterTile.PARTICLE_OFFSETS[n3];
                break;
            }
            case 1: {
                d4 = 0.3125;
                d2 = -RepeaterTile.PARTICLE_OFFSETS[n3];
            }
        }
        this.tesselateTorch(tile, (double)x + d2, (double)y + d, (double)z + d3, 0.0, 0.0);
        this.tesselateTorch(tile, (double)x + d4, (double)y + d, (double)z + d5, 0.0, 0.0);
        int n4 = tile.getTexture(1);
        int n5 = (n4 & 0xF) << 4;
        int n6 = n4 & 0xF0;
        double d6 = (float)n5 / 256.0f;
        double d7 = ((float)n5 + 15.99f) / 256.0f;
        double d8 = (float)n6 / 256.0f;
        double d9 = ((float)n6 + 15.99f) / 256.0f;
        float f2 = 0.125f;
        float f3 = x + 1;
        float f4 = x + 1;
        float f5 = x + 0;
        float f6 = x + 0;
        float f7 = z + 0;
        float f8 = z + 1;
        float f9 = z + 1;
        float f10 = z + 0;
        float f11 = (float)y + f2;
        if (n2 == 2) {
            f3 = f4 = (float)(x + 0);
            f5 = f6 = (float)(x + 1);
            f7 = f10 = (float)(z + 1);
            f8 = f9 = (float)(z + 0);
        } else if (n2 == 3) {
            f3 = f6 = (float)(x + 0);
            f4 = f5 = (float)(x + 1);
            f7 = f8 = (float)(z + 0);
            f9 = f10 = (float)(z + 1);
        } else if (n2 == 1) {
            f3 = f6 = (float)(x + 1);
            f4 = f5 = (float)(x + 0);
            f7 = f8 = (float)(z + 1);
            f9 = f10 = (float)(z + 0);
        }
        tesselator.vertexUV(f6, f11, f10, d6, d8);
        tesselator.vertexUV(f5, f11, f9, d6, d9);
        tesselator.vertexUV(f4, f11, f8, d7, d9);
        tesselator.vertexUV(f3, f11, f7, d7, d8);
        return true;
    
    }

    public void tesselatePistonHeadInWorld(Tile tile, int x, int y, int z) {
        boolean previous = this.noCulling;
        this.noCulling = true;
        try { this.tesselatePistonInWorld(((ExBlock)tile).ac$clone(), x, y, z, true); }
        finally { this.noCulling = previous; }
    
    }

    private boolean tesselatePistonInWorld(Tile tile, int x, int y, int z, boolean head) {
        int n = this.level.getData(x, y, z);
        boolean bl = head || (n & 8) != 0;
        int n2 = PistonBaseTile.getFacing(n);
        if (bl) {
            switch (n2) {
                case 0: {
                    this.northUvRotation = 3;
                    this.southUvRotation = 3;
                    this.eastUvRotation = 3;
                    this.westUvRotation = 3;
                    tile.setShape(0.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 1: {
                    tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 1.0f);
                    break;
                }
                case 2: {
                    this.eastUvRotation = 1;
                    this.westUvRotation = 2;
                    tile.setShape(0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 3: {
                    this.eastUvRotation = 2;
                    this.westUvRotation = 1;
                    this.topUvRotation = 3;
                    this.bottomUvRotation = 3;
                    tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f);
                    break;
                }
                case 4: {
                    this.northUvRotation = 1;
                    this.southUvRotation = 2;
                    this.topUvRotation = 2;
                    this.bottomUvRotation = 1;
                    tile.setShape(0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 5: {
                    this.northUvRotation = 2;
                    this.southUvRotation = 1;
                    this.topUvRotation = 1;
                    this.bottomUvRotation = 2;
                    tile.setShape(0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f);
                }
            }
            this.tesselateBlockInWorld(tile, x, y, z);
            this.northUvRotation = 0;
            this.southUvRotation = 0;
            this.eastUvRotation = 0;
            this.westUvRotation = 0;
            this.topUvRotation = 0;
            this.bottomUvRotation = 0;
            tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        } else {
            switch (n2) {
                case 0: {
                    this.northUvRotation = 3;
                    this.southUvRotation = 3;
                    this.eastUvRotation = 3;
                    this.westUvRotation = 3;
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    this.eastUvRotation = 1;
                    this.westUvRotation = 2;
                    break;
                }
                case 3: {
                    this.eastUvRotation = 2;
                    this.westUvRotation = 1;
                    this.topUvRotation = 3;
                    this.bottomUvRotation = 3;
                    break;
                }
                case 4: {
                    this.northUvRotation = 1;
                    this.southUvRotation = 2;
                    this.topUvRotation = 2;
                    this.bottomUvRotation = 1;
                    break;
                }
                case 5: {
                    this.northUvRotation = 2;
                    this.southUvRotation = 1;
                    this.topUvRotation = 1;
                    this.bottomUvRotation = 2;
                }
            }
            this.tesselateBlockInWorld(tile, x, y, z);
            this.northUvRotation = 0;
            this.southUvRotation = 0;
            this.eastUvRotation = 0;
            this.westUvRotation = 0;
            this.topUvRotation = 0;
            this.bottomUvRotation = 0;
        }
        return true;
    
    }

    private void method_41(double d, double e, double f, double g, double h, double i, float j, double k) {
        int n = 108;
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        VertexSink tesselator = this.writer;
        double d2 = (float)(n2 + 0) / 256.0f;
        double d3 = (float)(n3 + 0) / 256.0f;
        double d4 = ((double)n2 + k - 0.01) / 256.0;
        double d5 = ((double)((float)n3 + 4.0f) - 0.01) / 256.0;
        tesselator.color(j, j, j);
        tesselator.vertexUV(d, g, h, d4, d3);
        tesselator.vertexUV(d, f, h, d2, d3);
        tesselator.vertexUV(e, f, i, d2, d5);
        tesselator.vertexUV(e, g, i, d4, d5);
    
    }

    private void method_54(double d, double e, double f, double g, double h, double i, float j, double k) {
        int n = 108;
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        VertexSink tesselator = this.writer;
        double d2 = (float)(n2 + 0) / 256.0f;
        double d3 = (float)(n3 + 0) / 256.0f;
        double d4 = ((double)n2 + k - 0.01) / 256.0;
        double d5 = ((double)((float)n3 + 4.0f) - 0.01) / 256.0;
        tesselator.color(j, j, j);
        tesselator.vertexUV(d, f, i, d4, d3);
        tesselator.vertexUV(d, f, h, d2, d3);
        tesselator.vertexUV(e, g, h, d2, d5);
        tesselator.vertexUV(e, g, i, d4, d5);
    
    }

    private void method_60(double d, double e, double f, double g, double h, double i, float j, double k) {
        int n = 108;
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        VertexSink tesselator = this.writer;
        double d2 = (float)(n2 + 0) / 256.0f;
        double d3 = (float)(n3 + 0) / 256.0f;
        double d4 = ((double)n2 + k - 0.01) / 256.0;
        double d5 = ((double)((float)n3 + 4.0f) - 0.01) / 256.0;
        tesselator.color(j, j, j);
        tesselator.vertexUV(e, f, h, d4, d3);
        tesselator.vertexUV(d, f, h, d2, d3);
        tesselator.vertexUV(d, g, i, d2, d5);
        tesselator.vertexUV(e, g, i, d4, d5);
    
    }

    public void tesselateHeadPistonInWorldNoCulling(Tile tile, int x, int y, int z, boolean extended) {
        boolean previous = this.noCulling;
        this.noCulling = true;
        try { this.tesselateHeadPistonInWorld(((ExBlock)tile).ac$clone(), x, y, z, extended); }
        finally { this.noCulling = previous; }
    
    }

    private boolean tesselateHeadPistonInWorld(Tile tile, int x, int y, int z, boolean extended) {
        int n = this.level.getData(x, y, z);
        int n2 = PistonHeadTile.getDirection(n);
        float f = tile.getBrightness(this.level, x, y, z);
        float f2 = extended ? 1.0f : 0.5f;
        double d = extended ? 16.0 : 8.0;
        switch (n2) {
            case 0: {
                this.northUvRotation = 3;
                this.southUvRotation = 3;
                this.eastUvRotation = 3;
                this.westUvRotation = 3;
                tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_41((float)x + 0.375f, (float)x + 0.625f, (float)y + 0.25f, (float)y + 0.25f + f2, (float)z + 0.625f, (float)z + 0.625f, f * 0.8f, d);
                this.method_41((float)x + 0.625f, (float)x + 0.375f, (float)y + 0.25f, (float)y + 0.25f + f2, (float)z + 0.375f, (float)z + 0.375f, f * 0.8f, d);
                this.method_41((float)x + 0.375f, (float)x + 0.375f, (float)y + 0.25f, (float)y + 0.25f + f2, (float)z + 0.375f, (float)z + 0.625f, f * 0.6f, d);
                this.method_41((float)x + 0.625f, (float)x + 0.625f, (float)y + 0.25f, (float)y + 0.25f + f2, (float)z + 0.625f, (float)z + 0.375f, f * 0.6f, d);
                break;
            }
            case 1: {
                tile.setShape(0.0f, 0.75f, 0.0f, 1.0f, 1.0f, 1.0f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_41((float)x + 0.375f, (float)x + 0.625f, (float)y - 0.25f + 1.0f - f2, (float)y - 0.25f + 1.0f, (float)z + 0.625f, (float)z + 0.625f, f * 0.8f, d);
                this.method_41((float)x + 0.625f, (float)x + 0.375f, (float)y - 0.25f + 1.0f - f2, (float)y - 0.25f + 1.0f, (float)z + 0.375f, (float)z + 0.375f, f * 0.8f, d);
                this.method_41((float)x + 0.375f, (float)x + 0.375f, (float)y - 0.25f + 1.0f - f2, (float)y - 0.25f + 1.0f, (float)z + 0.375f, (float)z + 0.625f, f * 0.6f, d);
                this.method_41((float)x + 0.625f, (float)x + 0.625f, (float)y - 0.25f + 1.0f - f2, (float)y - 0.25f + 1.0f, (float)z + 0.625f, (float)z + 0.375f, f * 0.6f, d);
                break;
            }
            case 2: {
                this.eastUvRotation = 1;
                this.westUvRotation = 2;
                tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.25f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_54((float)x + 0.375f, (float)x + 0.375f, (float)y + 0.625f, (float)y + 0.375f, (float)z + 0.25f, (float)z + 0.25f + f2, f * 0.6f, d);
                this.method_54((float)x + 0.625f, (float)x + 0.625f, (float)y + 0.375f, (float)y + 0.625f, (float)z + 0.25f, (float)z + 0.25f + f2, f * 0.6f, d);
                this.method_54((float)x + 0.375f, (float)x + 0.625f, (float)y + 0.375f, (float)y + 0.375f, (float)z + 0.25f, (float)z + 0.25f + f2, f * 0.5f, d);
                this.method_54((float)x + 0.625f, (float)x + 0.375f, (float)y + 0.625f, (float)y + 0.625f, (float)z + 0.25f, (float)z + 0.25f + f2, f, d);
                break;
            }
            case 3: {
                this.eastUvRotation = 2;
                this.westUvRotation = 1;
                this.topUvRotation = 3;
                this.bottomUvRotation = 3;
                tile.setShape(0.0f, 0.0f, 0.75f, 1.0f, 1.0f, 1.0f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_54((float)x + 0.375f, (float)x + 0.375f, (float)y + 0.625f, (float)y + 0.375f, (float)z - 0.25f + 1.0f - f2, (float)z - 0.25f + 1.0f, f * 0.6f, d);
                this.method_54((float)x + 0.625f, (float)x + 0.625f, (float)y + 0.375f, (float)y + 0.625f, (float)z - 0.25f + 1.0f - f2, (float)z - 0.25f + 1.0f, f * 0.6f, d);
                this.method_54((float)x + 0.375f, (float)x + 0.625f, (float)y + 0.375f, (float)y + 0.375f, (float)z - 0.25f + 1.0f - f2, (float)z - 0.25f + 1.0f, f * 0.5f, d);
                this.method_54((float)x + 0.625f, (float)x + 0.375f, (float)y + 0.625f, (float)y + 0.625f, (float)z - 0.25f + 1.0f - f2, (float)z - 0.25f + 1.0f, f, d);
                break;
            }
            case 4: {
                this.northUvRotation = 1;
                this.southUvRotation = 2;
                this.topUvRotation = 2;
                this.bottomUvRotation = 1;
                tile.setShape(0.0f, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_60((float)x + 0.25f, (float)x + 0.25f + f2, (float)y + 0.375f, (float)y + 0.375f, (float)z + 0.625f, (float)z + 0.375f, f * 0.5f, d);
                this.method_60((float)x + 0.25f, (float)x + 0.25f + f2, (float)y + 0.625f, (float)y + 0.625f, (float)z + 0.375f, (float)z + 0.625f, f, d);
                this.method_60((float)x + 0.25f, (float)x + 0.25f + f2, (float)y + 0.375f, (float)y + 0.625f, (float)z + 0.375f, (float)z + 0.375f, f * 0.6f, d);
                this.method_60((float)x + 0.25f, (float)x + 0.25f + f2, (float)y + 0.625f, (float)y + 0.375f, (float)z + 0.625f, (float)z + 0.625f, f * 0.6f, d);
                break;
            }
            case 5: {
                this.northUvRotation = 2;
                this.southUvRotation = 1;
                this.topUvRotation = 1;
                this.bottomUvRotation = 2;
                tile.setShape(0.75f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                this.tesselateBlockInWorld(tile, x, y, z);
                this.method_60((float)x - 0.25f + 1.0f - f2, (float)x - 0.25f + 1.0f, (float)y + 0.375f, (float)y + 0.375f, (float)z + 0.625f, (float)z + 0.375f, f * 0.5f, d);
                this.method_60((float)x - 0.25f + 1.0f - f2, (float)x - 0.25f + 1.0f, (float)y + 0.625f, (float)y + 0.625f, (float)z + 0.375f, (float)z + 0.625f, f, d);
                this.method_60((float)x - 0.25f + 1.0f - f2, (float)x - 0.25f + 1.0f, (float)y + 0.375f, (float)y + 0.625f, (float)z + 0.375f, (float)z + 0.375f, f * 0.6f, d);
                this.method_60((float)x - 0.25f + 1.0f - f2, (float)x - 0.25f + 1.0f, (float)y + 0.625f, (float)y + 0.375f, (float)z + 0.625f, (float)z + 0.625f, f * 0.6f, d);
            }
        }
        this.northUvRotation = 0;
        this.southUvRotation = 0;
        this.eastUvRotation = 0;
        this.westUvRotation = 0;
        this.topUvRotation = 0;
        this.bottomUvRotation = 0;
        tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        return true;
    
    }

    public boolean tesselateLeverInWorld(Tile tile, int x, int y, int z) {
        boolean bl;
        int n = this.level.getData(x, y, z);
        int n2 = n & 7;
        boolean bl2 = (n & 8) > 0;
        VertexSink tesselator = this.writer;
        boolean bl3 = bl = this.fixedTexture >= 0;
        if (!bl) {
            this.fixedTexture = Tile.COBBLESTONE.tex;
        }
        float f = 0.25f;
        float f2 = 0.1875f;
        float f3 = 0.1875f;
        if (n2 == 5) {
            tile.setShape(0.5f - f2, 0.0f, 0.5f - f, 0.5f + f2, f3, 0.5f + f);
        } else if (n2 == 6) {
            tile.setShape(0.5f - f, 0.0f, 0.5f - f2, 0.5f + f, f3, 0.5f + f2);
        } else if (n2 == 4) {
            tile.setShape(0.5f - f2, 0.5f - f, 1.0f - f3, 0.5f + f2, 0.5f + f, 1.0f);
        } else if (n2 == 3) {
            tile.setShape(0.5f - f2, 0.5f - f, 0.0f, 0.5f + f2, 0.5f + f, f3);
        } else if (n2 == 2) {
            tile.setShape(1.0f - f3, 0.5f - f, 0.5f - f2, 1.0f, 0.5f + f, 0.5f + f2);
        } else if (n2 == 1) {
            tile.setShape(0.0f, 0.5f - f, 0.5f - f2, f3, 0.5f + f, 0.5f + f2);
        }
        this.tesselateBlockInWorld(tile, x, y, z);
        if (!bl) {
            this.fixedTexture = -1;
        }
        float f4 = tile.getBrightness(this.level, x, y, z);
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f4 = 1.0f;
        }
        tesselator.color(f4, f4, f4);
        int n3 = tile.getTexture(0);
        if (this.fixedTexture >= 0) {
            n3 = this.fixedTexture;
        }
        int n4 = (n3 & 0xF) << 4;
        int n5 = n3 & 0xF0;
        float f5 = (float)n4 / 256.0f;
        float f6 = ((float)n4 + 15.99f) / 256.0f;
        float f7 = (float)n5 / 256.0f;
        float f8 = ((float)n5 + 15.99f) / 256.0f;
        Vec3[] vec3Array = new Vec3[8];
        float f9 = 0.0625f;
        float f10 = 0.0625f;
        float f11 = 0.625f;
        vec3Array[0] = Vec3.newTemp(-f9, 0.0, -f10);
        vec3Array[1] = Vec3.newTemp(f9, 0.0, -f10);
        vec3Array[2] = Vec3.newTemp(f9, 0.0, f10);
        vec3Array[3] = Vec3.newTemp(-f9, 0.0, f10);
        vec3Array[4] = Vec3.newTemp(-f9, f11, -f10);
        vec3Array[5] = Vec3.newTemp(f9, f11, -f10);
        vec3Array[6] = Vec3.newTemp(f9, f11, f10);
        vec3Array[7] = Vec3.newTemp(-f9, f11, f10);
        for (int i = 0; i < 8; ++i) {
            if (bl2) {
                vec3Array[i].z -= 0.0625;
                vec3Array[i].xRot(0.69813174f);
            } else {
                vec3Array[i].z += 0.0625;
                vec3Array[i].xRot(-0.69813174f);
            }
            if (n2 == 6) {
                vec3Array[i].yRot(1.5707964f);
            }
            if (n2 < 5) {
                vec3Array[i].y -= 0.375;
                vec3Array[i].xRot(1.5707964f);
                if (n2 == 4) {
                    vec3Array[i].yRot(0.0f);
                }
                if (n2 == 3) {
                    vec3Array[i].yRot((float)Math.PI);
                }
                if (n2 == 2) {
                    vec3Array[i].yRot(1.5707964f);
                }
                if (n2 == 1) {
                    vec3Array[i].yRot(-1.5707964f);
                }
                vec3Array[i].x += (double)x + 0.5;
                vec3Array[i].y += (double)((float)y + 0.5f);
                vec3Array[i].z += (double)z + 0.5;
                continue;
            }
            vec3Array[i].x += (double)x + 0.5;
            vec3Array[i].y += (double)((float)y + 0.125f);
            vec3Array[i].z += (double)z + 0.5;
        }
        Vec3 vec3 = null;
        Vec3 vec32 = null;
        Vec3 vec33 = null;
        Vec3 vec34 = null;
        for (int i = 0; i < 6; ++i) {
            if (i == 0) {
                f5 = (float)(n4 + 7) / 256.0f;
                f6 = ((float)(n4 + 9) - 0.01f) / 256.0f;
                f7 = (float)(n5 + 6) / 256.0f;
                f8 = ((float)(n5 + 8) - 0.01f) / 256.0f;
            } else if (i == 2) {
                f5 = (float)(n4 + 7) / 256.0f;
                f6 = ((float)(n4 + 9) - 0.01f) / 256.0f;
                f7 = (float)(n5 + 6) / 256.0f;
                f8 = ((float)(n5 + 16) - 0.01f) / 256.0f;
            }
            if (i == 0) {
                vec3 = vec3Array[0];
                vec32 = vec3Array[1];
                vec33 = vec3Array[2];
                vec34 = vec3Array[3];
            } else if (i == 1) {
                vec3 = vec3Array[7];
                vec32 = vec3Array[6];
                vec33 = vec3Array[5];
                vec34 = vec3Array[4];
            } else if (i == 2) {
                vec3 = vec3Array[1];
                vec32 = vec3Array[0];
                vec33 = vec3Array[4];
                vec34 = vec3Array[5];
            } else if (i == 3) {
                vec3 = vec3Array[2];
                vec32 = vec3Array[1];
                vec33 = vec3Array[5];
                vec34 = vec3Array[6];
            } else if (i == 4) {
                vec3 = vec3Array[3];
                vec32 = vec3Array[2];
                vec33 = vec3Array[6];
                vec34 = vec3Array[7];
            } else if (i == 5) {
                vec3 = vec3Array[0];
                vec32 = vec3Array[3];
                vec33 = vec3Array[7];
                vec34 = vec3Array[4];
            }
            tesselator.vertexUV(vec3.x, vec3.y, vec3.z, f5, f8);
            tesselator.vertexUV(vec32.x, vec32.y, vec32.z, f6, f8);
            tesselator.vertexUV(vec33.x, vec33.y, vec33.z, f6, f7);
            tesselator.vertexUV(vec34.x, vec34.y, vec34.z, f5, f7);
        }
        return true;
    
    }

    public boolean tesselateFireInWorld(Tile tile, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        int n = tile.getTexture(0);
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        float f = tile.getBrightness(this.level, x, y, z);
        tesselator.color(f, f, f);
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        double d = (float)n2 / 256.0f;
        double d2 = ((float)n2 + 15.99f) / 256.0f;
        double d3 = (float)n3 / 256.0f;
        double d4 = ((float)n3 + 15.99f) / 256.0f;
        float f2 = 1.4f;
        if (this.level.isSolidBlockingTile(x, y - 1, z) || Tile.FIRE.canBurn(this.level, x, y - 1, z)) {
            double d5 = (double)x + 0.5 + 0.2;
            double d6 = (double)x + 0.5 - 0.2;
            double d7 = (double)z + 0.5 + 0.2;
            double d8 = (double)z + 0.5 - 0.2;
            double d9 = (double)x + 0.5 - 0.3;
            double d10 = (double)x + 0.5 + 0.3;
            double d11 = (double)z + 0.5 - 0.3;
            double d12 = (double)z + 0.5 + 0.3;
            tesselator.vertexUV(d9, (float)y + f2, z + 1, d2, d3);
            tesselator.vertexUV(d5, y + 0, z + 1, d2, d4);
            tesselator.vertexUV(d5, y + 0, z + 0, d, d4);
            tesselator.vertexUV(d9, (float)y + f2, z + 0, d, d3);
            tesselator.vertexUV(d10, (float)y + f2, z + 0, d2, d3);
            tesselator.vertexUV(d6, y + 0, z + 0, d2, d4);
            tesselator.vertexUV(d6, y + 0, z + 1, d, d4);
            tesselator.vertexUV(d10, (float)y + f2, z + 1, d, d3);
            d = (float)n2 / 256.0f;
            d2 = ((float)n2 + 15.99f) / 256.0f;
            d3 = (float)(n3 + 16) / 256.0f;
            d4 = ((float)n3 + 15.99f + 16.0f) / 256.0f;
            tesselator.vertexUV(x + 1, (float)y + f2, d12, d2, d3);
            tesselator.vertexUV(x + 1, y + 0, d8, d2, d4);
            tesselator.vertexUV(x + 0, y + 0, d8, d, d4);
            tesselator.vertexUV(x + 0, (float)y + f2, d12, d, d3);
            tesselator.vertexUV(x + 0, (float)y + f2, d11, d2, d3);
            tesselator.vertexUV(x + 0, y + 0, d7, d2, d4);
            tesselator.vertexUV(x + 1, y + 0, d7, d, d4);
            tesselator.vertexUV(x + 1, (float)y + f2, d11, d, d3);
            d5 = (double)x + 0.5 - 0.5;
            d6 = (double)x + 0.5 + 0.5;
            d7 = (double)z + 0.5 - 0.5;
            d8 = (double)z + 0.5 + 0.5;
            d9 = (double)x + 0.5 - 0.4;
            d10 = (double)x + 0.5 + 0.4;
            d11 = (double)z + 0.5 - 0.4;
            d12 = (double)z + 0.5 + 0.4;
            tesselator.vertexUV(d9, (float)y + f2, z + 0, d, d3);
            tesselator.vertexUV(d5, y + 0, z + 0, d, d4);
            tesselator.vertexUV(d5, y + 0, z + 1, d2, d4);
            tesselator.vertexUV(d9, (float)y + f2, z + 1, d2, d3);
            tesselator.vertexUV(d10, (float)y + f2, z + 1, d, d3);
            tesselator.vertexUV(d6, y + 0, z + 1, d, d4);
            tesselator.vertexUV(d6, y + 0, z + 0, d2, d4);
            tesselator.vertexUV(d10, (float)y + f2, z + 0, d2, d3);
            d = (float)n2 / 256.0f;
            d2 = ((float)n2 + 15.99f) / 256.0f;
            d3 = (float)n3 / 256.0f;
            d4 = ((float)n3 + 15.99f) / 256.0f;
            tesselator.vertexUV(x + 0, (float)y + f2, d12, d, d3);
            tesselator.vertexUV(x + 0, y + 0, d8, d, d4);
            tesselator.vertexUV(x + 1, y + 0, d8, d2, d4);
            tesselator.vertexUV(x + 1, (float)y + f2, d12, d2, d3);
            tesselator.vertexUV(x + 1, (float)y + f2, d11, d, d3);
            tesselator.vertexUV(x + 1, y + 0, d7, d, d4);
            tesselator.vertexUV(x + 0, y + 0, d7, d2, d4);
            tesselator.vertexUV(x + 0, (float)y + f2, d11, d2, d3);
        } else {
            double d13;
            float f3 = 0.2f;
            float f4 = 0.0625f;
            if ((x + y + z & 1) == 1) {
                d = (float)n2 / 256.0f;
                d2 = ((float)n2 + 15.99f) / 256.0f;
                d3 = (float)(n3 + 16) / 256.0f;
                d4 = ((float)n3 + 15.99f + 16.0f) / 256.0f;
            }
            if ((x / 2 + y / 2 + z / 2 & 1) == 1) {
                d13 = d2;
                d2 = d;
                d = d13;
            }
            if (Tile.FIRE.canBurn(this.level, x - 1, y, z)) {
                tesselator.vertexUV((float)x + f3, (float)y + f2 + f4, z + 1, d2, d3);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 1, d2, d4);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV((float)x + f3, (float)y + f2 + f4, z + 0, d, d3);
                tesselator.vertexUV((float)x + f3, (float)y + f2 + f4, z + 0, d, d3);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 1, d2, d4);
                tesselator.vertexUV((float)x + f3, (float)y + f2 + f4, z + 1, d2, d3);
            }
            if (Tile.FIRE.canBurn(this.level, x + 1, y, z)) {
                tesselator.vertexUV((float)(x + 1) - f3, (float)y + f2 + f4, z + 0, d, d3);
                tesselator.vertexUV(x + 1 - 0, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV(x + 1 - 0, (float)(y + 0) + f4, z + 1, d2, d4);
                tesselator.vertexUV((float)(x + 1) - f3, (float)y + f2 + f4, z + 1, d2, d3);
                tesselator.vertexUV((float)(x + 1) - f3, (float)y + f2 + f4, z + 1, d2, d3);
                tesselator.vertexUV(x + 1 - 0, (float)(y + 0) + f4, z + 1, d2, d4);
                tesselator.vertexUV(x + 1 - 0, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV((float)(x + 1) - f3, (float)y + f2 + f4, z + 0, d, d3);
            }
            if (Tile.FIRE.canBurn(this.level, x, y, z - 1)) {
                tesselator.vertexUV(x + 0, (float)y + f2 + f4, (float)z + f3, d2, d3);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 0, d2, d4);
                tesselator.vertexUV(x + 1, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV(x + 1, (float)y + f2 + f4, (float)z + f3, d, d3);
                tesselator.vertexUV(x + 1, (float)y + f2 + f4, (float)z + f3, d, d3);
                tesselator.vertexUV(x + 1, (float)(y + 0) + f4, z + 0, d, d4);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 0, d2, d4);
                tesselator.vertexUV(x + 0, (float)y + f2 + f4, (float)z + f3, d2, d3);
            }
            if (Tile.FIRE.canBurn(this.level, x, y, z + 1)) {
                tesselator.vertexUV(x + 1, (float)y + f2 + f4, (float)(z + 1) - f3, d, d3);
                tesselator.vertexUV(x + 1, (float)(y + 0) + f4, z + 1 - 0, d, d4);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 1 - 0, d2, d4);
                tesselator.vertexUV(x + 0, (float)y + f2 + f4, (float)(z + 1) - f3, d2, d3);
                tesselator.vertexUV(x + 0, (float)y + f2 + f4, (float)(z + 1) - f3, d2, d3);
                tesselator.vertexUV(x + 0, (float)(y + 0) + f4, z + 1 - 0, d2, d4);
                tesselator.vertexUV(x + 1, (float)(y + 0) + f4, z + 1 - 0, d, d4);
                tesselator.vertexUV(x + 1, (float)y + f2 + f4, (float)(z + 1) - f3, d, d3);
            }
            if (Tile.FIRE.canBurn(this.level, x, y + 1, z)) {
                d13 = (double)x + 0.5 + 0.5;
                double d14 = (double)x + 0.5 - 0.5;
                double d15 = (double)z + 0.5 + 0.5;
                double d16 = (double)z + 0.5 - 0.5;
                double d17 = (double)x + 0.5 - 0.5;
                double d18 = (double)x + 0.5 + 0.5;
                double d19 = (double)z + 0.5 - 0.5;
                double d20 = (double)z + 0.5 + 0.5;
                d = (float)n2 / 256.0f;
                d2 = ((float)n2 + 15.99f) / 256.0f;
                d3 = (float)n3 / 256.0f;
                d4 = ((float)n3 + 15.99f) / 256.0f;
                f2 = -0.2f;
                if ((x + ++y + z & 1) == 0) {
                    tesselator.vertexUV(d17, (float)y + f2, z + 0, d2, d3);
                    tesselator.vertexUV(d13, y + 0, z + 0, d2, d4);
                    tesselator.vertexUV(d13, y + 0, z + 1, d, d4);
                    tesselator.vertexUV(d17, (float)y + f2, z + 1, d, d3);
                    d = (float)n2 / 256.0f;
                    d2 = ((float)n2 + 15.99f) / 256.0f;
                    d3 = (float)(n3 + 16) / 256.0f;
                    d4 = ((float)n3 + 15.99f + 16.0f) / 256.0f;
                    tesselator.vertexUV(d18, (float)y + f2, z + 1, d2, d3);
                    tesselator.vertexUV(d14, y + 0, z + 1, d2, d4);
                    tesselator.vertexUV(d14, y + 0, z + 0, d, d4);
                    tesselator.vertexUV(d18, (float)y + f2, z + 0, d, d3);
                } else {
                    tesselator.vertexUV(x + 0, (float)y + f2, d20, d2, d3);
                    tesselator.vertexUV(x + 0, y + 0, d16, d2, d4);
                    tesselator.vertexUV(x + 1, y + 0, d16, d, d4);
                    tesselator.vertexUV(x + 1, (float)y + f2, d20, d, d3);
                    d = (float)n2 / 256.0f;
                    d2 = ((float)n2 + 15.99f) / 256.0f;
                    d3 = (float)(n3 + 16) / 256.0f;
                    d4 = ((float)n3 + 15.99f + 16.0f) / 256.0f;
                    tesselator.vertexUV(x + 1, (float)y + f2, d19, d2, d3);
                    tesselator.vertexUV(x + 1, y + 0, d15, d2, d4);
                    tesselator.vertexUV(x + 0, y + 0, d15, d, d4);
                    tesselator.vertexUV(x + 0, (float)y + f2, d19, d, d3);
                }
            }
        }
        return true;
    
    }

    public boolean tesselateDustInWorld(Tile tile, int x, int y, int z) {
        boolean bl;
        VertexSink tesselator = this.writer;
        int n = this.level.getData(x, y, z);
        int n2 = tile.getTexture(1, n);
        if (this.fixedTexture >= 0) {
            n2 = this.fixedTexture;
        }
        float f = tile.getBrightness(this.level, x, y, z);
        float f2 = (float)n / 15.0f;
        float f3 = f2 * 0.6f + 0.4f;
        if (n == 0) {
            f3 = 0.3f;
        }
        float f4 = f2 * f2 * 0.7f - 0.5f;
        float f5 = f2 * f2 * 0.6f - 0.7f;
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f5 < 0.0f) {
            f5 = 0.0f;
        }
        if (this.terrainMaterial && this.fixedTexture < 0) {
            int tint = (int) (f3 * 255) << 16 | (int) (f4 * 255) << 8 | (int) (f5 * 255);
            tesselator.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(tint, this.materialEmission), this.materialFlags));
        }
        tesselator.color(f * f3, f * f4, f * f5);
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d = (float)n3 / 256.0f;
        double d2 = ((float)n3 + 15.99f) / 256.0f;
        double d3 = (float)n4 / 256.0f;
        double d4 = ((float)n4 + 15.99f) / 256.0f;
        boolean bl2 = RedStoneDustTile.isPowerSourceAt(this.level, x - 1, y, z, 1) || !this.level.isSolidBlockingTile(x - 1, y, z) && RedStoneDustTile.isPowerSourceAt(this.level, x - 1, y - 1, z, -1);
        boolean bl3 = RedStoneDustTile.isPowerSourceAt(this.level, x + 1, y, z, 3) || !this.level.isSolidBlockingTile(x + 1, y, z) && RedStoneDustTile.isPowerSourceAt(this.level, x + 1, y - 1, z, -1);
        boolean bl4 = RedStoneDustTile.isPowerSourceAt(this.level, x, y, z - 1, 2) || !this.level.isSolidBlockingTile(x, y, z - 1) && RedStoneDustTile.isPowerSourceAt(this.level, x, y - 1, z - 1, -1);
        boolean bl5 = bl = RedStoneDustTile.isPowerSourceAt(this.level, x, y, z + 1, 0) || !this.level.isSolidBlockingTile(x, y, z + 1) && RedStoneDustTile.isPowerSourceAt(this.level, x, y - 1, z + 1, -1);
        if (!this.level.isSolidBlockingTile(x, y + 1, z)) {
            if (this.level.isSolidBlockingTile(x - 1, y, z) && RedStoneDustTile.isPowerSourceAt(this.level, x - 1, y + 1, z, -1)) {
                bl2 = true;
            }
            if (this.level.isSolidBlockingTile(x + 1, y, z) && RedStoneDustTile.isPowerSourceAt(this.level, x + 1, y + 1, z, -1)) {
                bl3 = true;
            }
            if (this.level.isSolidBlockingTile(x, y, z - 1) && RedStoneDustTile.isPowerSourceAt(this.level, x, y + 1, z - 1, -1)) {
                bl4 = true;
            }
            if (this.level.isSolidBlockingTile(x, y, z + 1) && RedStoneDustTile.isPowerSourceAt(this.level, x, y + 1, z + 1, -1)) {
                bl = true;
            }
        }
        float f6 = x + 0;
        float f7 = x + 1;
        float f8 = z + 0;
        float f9 = z + 1;
        int n5 = 0;
        if ((bl2 || bl3) && !bl4 && !bl) {
            n5 = 1;
        }
        if ((bl4 || bl) && !bl3 && !bl2) {
            n5 = 2;
        }
        if (n5 != 0) {
            d = (float)(n3 + 16) / 256.0f;
            d2 = ((float)(n3 + 16) + 15.99f) / 256.0f;
            d3 = (float)n4 / 256.0f;
            d4 = ((float)n4 + 15.99f) / 256.0f;
        }
        if (n5 == 0) {
            if (bl3 || bl4 || bl || bl2) {
                if (!bl2) {
                    f6 += 0.3125f;
                }
                if (!bl2) {
                    d += 0.01953125;
                }
                if (!bl3) {
                    f7 -= 0.3125f;
                }
                if (!bl3) {
                    d2 -= 0.01953125;
                }
                if (!bl4) {
                    f8 += 0.3125f;
                }
                if (!bl4) {
                    d3 += 0.01953125;
                }
                if (!bl) {
                    f9 -= 0.3125f;
                }
                if (!bl) {
                    d4 -= 0.01953125;
                }
            }
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d2, d3);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d, d4);
            tesselator.color(f, f, f);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4 + 0.0625);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d2, d3 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d, d4 + 0.0625);
        } else if (n5 == 1) {
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d2, d3);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d, d4);
            tesselator.color(f, f, f);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4 + 0.0625);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d2, d3 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d, d4 + 0.0625);
        } else if (n5 == 2) {
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d, d4);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d2, d3);
            tesselator.color(f, f, f);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f9, d2, d4 + 0.0625);
            tesselator.vertexUV(f7, (float)y + 0.015625f, f8, d, d4 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f8, d, d3 + 0.0625);
            tesselator.vertexUV(f6, (float)y + 0.015625f, f9, d2, d3 + 0.0625);
        }
        if (!this.level.isSolidBlockingTile(x, y + 1, z)) {
            d = (float)(n3 + 16) / 256.0f;
            d2 = ((float)(n3 + 16) + 15.99f) / 256.0f;
            d3 = (float)n4 / 256.0f;
            d4 = ((float)n4 + 15.99f) / 256.0f;
            if (this.level.isSolidBlockingTile(x - 1, y, z) && this.level.getTile(x - 1, y + 1, z) == Tile.REDSTONE.id) {
                tesselator.color(f * f3, f * f4, f * f5);
                tesselator.vertexUV((float)x + 0.015625f, (float)(y + 1) + 0.021875f, z + 1, d2, d3);
                tesselator.vertexUV((float)x + 0.015625f, y + 0, z + 1, d, d3);
                tesselator.vertexUV((float)x + 0.015625f, y + 0, z + 0, d, d4);
                tesselator.vertexUV((float)x + 0.015625f, (float)(y + 1) + 0.021875f, z + 0, d2, d4);
                tesselator.color(f, f, f);
                tesselator.vertexUV((float)x + 0.015625f, (float)(y + 1) + 0.021875f, z + 1, d2, d3 + 0.0625);
                tesselator.vertexUV((float)x + 0.015625f, y + 0, z + 1, d, d3 + 0.0625);
                tesselator.vertexUV((float)x + 0.015625f, y + 0, z + 0, d, d4 + 0.0625);
                tesselator.vertexUV((float)x + 0.015625f, (float)(y + 1) + 0.021875f, z + 0, d2, d4 + 0.0625);
            }
            if (this.level.isSolidBlockingTile(x + 1, y, z) && this.level.getTile(x + 1, y + 1, z) == Tile.REDSTONE.id) {
                tesselator.color(f * f3, f * f4, f * f5);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, y + 0, z + 1, d, d4);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 1, d2, d4);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 0, d2, d3);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, y + 0, z + 0, d, d3);
                tesselator.color(f, f, f);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, y + 0, z + 1, d, d4 + 0.0625);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 1, d2, d4 + 0.0625);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, (float)(y + 1) + 0.021875f, z + 0, d2, d3 + 0.0625);
                tesselator.vertexUV((float)(x + 1) - 0.015625f, y + 0, z + 0, d, d3 + 0.0625);
            }
            if (this.level.isSolidBlockingTile(x, y, z - 1) && this.level.getTile(x, y + 1, z - 1) == Tile.REDSTONE.id) {
                tesselator.color(f * f3, f * f4, f * f5);
                tesselator.vertexUV(x + 1, y + 0, (float)z + 0.015625f, d, d4);
                tesselator.vertexUV(x + 1, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, d2, d4);
                tesselator.vertexUV(x + 0, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, d2, d3);
                tesselator.vertexUV(x + 0, y + 0, (float)z + 0.015625f, d, d3);
                tesselator.color(f, f, f);
                tesselator.vertexUV(x + 1, y + 0, (float)z + 0.015625f, d, d4 + 0.0625);
                tesselator.vertexUV(x + 1, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, d2, d4 + 0.0625);
                tesselator.vertexUV(x + 0, (float)(y + 1) + 0.021875f, (float)z + 0.015625f, d2, d3 + 0.0625);
                tesselator.vertexUV(x + 0, y + 0, (float)z + 0.015625f, d, d3 + 0.0625);
            }
            if (this.level.isSolidBlockingTile(x, y, z + 1) && this.level.getTile(x, y + 1, z + 1) == Tile.REDSTONE.id) {
                tesselator.color(f * f3, f * f4, f * f5);
                tesselator.vertexUV(x + 1, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, d2, d3);
                tesselator.vertexUV(x + 1, y + 0, (float)(z + 1) - 0.015625f, d, d3);
                tesselator.vertexUV(x + 0, y + 0, (float)(z + 1) - 0.015625f, d, d4);
                tesselator.vertexUV(x + 0, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, d2, d4);
                tesselator.color(f, f, f);
                tesselator.vertexUV(x + 1, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, d2, d3 + 0.0625);
                tesselator.vertexUV(x + 1, y + 0, (float)(z + 1) - 0.015625f, d, d3 + 0.0625);
                tesselator.vertexUV(x + 0, y + 0, (float)(z + 1) - 0.015625f, d, d4 + 0.0625);
                tesselator.vertexUV(x + 0, (float)(y + 1) + 0.021875f, (float)(z + 1) - 0.015625f, d2, d4 + 0.0625);
            }
        }
        return true;
    
    }

    public boolean tesselateRailInWorld(RailTile rail, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        int n = this.level.getData(x, y, z);
        int n2 = rail.getTexture(0, n);
        if (this.fixedTexture >= 0) {
            n2 = this.fixedTexture;
        }
        if (rail.isStraight()) {
            n &= 7;
        }
        float f = rail.getBrightness(this.level, x, y, z);
        tesselator.color(f, f, f);
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d = (float)n3 / 256.0f;
        double d2 = ((float)n3 + 15.99f) / 256.0f;
        double d3 = (float)n4 / 256.0f;
        double d4 = ((float)n4 + 15.99f) / 256.0f;
        float f2 = 0.0625f;
        float f3 = x + 1;
        float f4 = x + 1;
        float f5 = x + 0;
        float f6 = x + 0;
        float f7 = z + 0;
        float f8 = z + 1;
        float f9 = z + 1;
        float f10 = z + 0;
        float f11 = (float)y + f2;
        float f12 = (float)y + f2;
        float f13 = (float)y + f2;
        float f14 = (float)y + f2;
        if (n == 1 || n == 2 || n == 3 || n == 7) {
            f3 = f6 = (float)(x + 1);
            f4 = f5 = (float)(x + 0);
            f7 = f8 = (float)(z + 1);
            f9 = f10 = (float)(z + 0);
        } else if (n == 8) {
            f3 = f4 = (float)(x + 0);
            f5 = f6 = (float)(x + 1);
            f7 = f10 = (float)(z + 1);
            f8 = f9 = (float)(z + 0);
        } else if (n == 9) {
            f3 = f6 = (float)(x + 0);
            f4 = f5 = (float)(x + 1);
            f7 = f8 = (float)(z + 0);
            f9 = f10 = (float)(z + 1);
        }
        if (n == 2 || n == 4) {
            f11 += 1.0f;
            f14 += 1.0f;
        } else if (n == 3 || n == 5) {
            f12 += 1.0f;
            f13 += 1.0f;
        }
        tesselator.vertexUV(f3, f11, f7, d2, d3);
        tesselator.vertexUV(f4, f12, f8, d2, d4);
        tesselator.vertexUV(f5, f13, f9, d, d4);
        tesselator.vertexUV(f6, f14, f10, d, d3);
        tesselator.vertexUV(f6, f14, f10, d, d3);
        tesselator.vertexUV(f5, f13, f9, d, d4);
        tesselator.vertexUV(f4, f12, f8, d2, d4);
        tesselator.vertexUV(f3, f11, f7, d2, d3);
        return true;
    
    }

    public boolean tesselateLadderInWorld(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(0, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        int direction = meta % 4;
        double a = 0.0F;
        double b = 0.025F;
        if (direction == 3) {
            double x0 = x + b;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z - a;
            double z1 = (z + 1) + a;
            ts.vertexUV(x0, y1, z1, u0, v0);
            ts.vertexUV(x0, y0, z1, u0, v1);
            ts.vertexUV(x0, y0, z0, u1, v1);
            ts.vertexUV(x0, y1, z0, u1, v0);
            ts.vertexUV(x0, y0, z1, u0, v1);
            ts.vertexUV(x0, y1, z1, u0, v0);
            ts.vertexUV(x0, y1, z0, u1, v0);
            ts.vertexUV(x0, y0, z0, u1, v1);
        }
        else if (direction == 2) {
            double x1 = (x + 1) - b;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z - a;
            double z1 = (z + 1) + a;
            ts.vertexUV(x1, y0, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z0, u0, v0);
            ts.vertexUV(x1, y0, z0, u0, v1);
            ts.vertexUV(x1, y0, z0, u0, v1);
            ts.vertexUV(x1, y1, z0, u0, v0);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y0, z1, u1, v1);
        }
        else if (direction == 1) {
            double x0 = x - a;
            double x1 = (x + 1) + a;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z0 = z + b;
            ts.vertexUV(x1, y0, z0, u1, v1);
            ts.vertexUV(x1, y1, z0, u1, v0);
            ts.vertexUV(x0, y1, z0, u0, v0);
            ts.vertexUV(x0, y0, z0, u0, v1);
            ts.vertexUV(x0, y0, z0, u0, v1);
            ts.vertexUV(x0, y1, z0, u0, v0);
            ts.vertexUV(x1, y1, z0, u1, v0);
            ts.vertexUV(x1, y0, z0, u1, v1);
        }
        else if (direction == 0) {
            double x0 = x - a;
            double x1 = (x + 1) + a;
            double y0 = y - a;
            double y1 = (y + 1) + a;
            double z1 = (z + 1) - b;
            ts.vertexUV(x1, y1, z1, u0, v0);
            ts.vertexUV(x1, y0, z1, u0, v1);
            ts.vertexUV(x0, y0, z1, u1, v1);
            ts.vertexUV(x0, y1, z1, u1, v0);
            ts.vertexUV(x0, y1, z1, u1, v0);
            ts.vertexUV(x0, y0, z1, u1, v1);
            ts.vertexUV(x1, y0, z1, u0, v1);
            ts.vertexUV(x1, y1, z1, u0, v0);
        }
        return true;
    
    }

    public boolean tesselateCrossInWorld(Tile tile, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        float f = tile.getBrightness(this.level, x, y, z);
        int n = this.foliageColor(tile, x, y, z);
        float f2 = (float)(n >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n & 0xFF) / 255.0f;
        if (this.options.anaglyph()) {
            float f5 = (f2 * 30.0f + f3 * 59.0f + f4 * 11.0f) / 100.0f;
            float f6 = (f2 * 30.0f + f3 * 70.0f) / 100.0f;
            float f7 = (f2 * 30.0f + f4 * 70.0f) / 100.0f;
            f2 = f5;
            f3 = f6;
            f4 = f7;
        }
        tesselator.color(f * f2, f * f3, f * f4);
        double d = x;
        double d2 = y;
        double d3 = z;
        if (tile.id == Tile.TALL_GRASS.id) {
            long l = (long)(x * 3129871) ^ (long)z * 116129781L ^ (long)y;
            l = l * l * 42317861L + l * 11L;
            d += ((double)((float)(l >> 16 & 0xFL) / 15.0f) - 0.5) * 0.5;
            d2 += ((double)((float)(l >> 20 & 0xFL) / 15.0f) - 1.0) * 0.2;
            d3 += ((double)((float)(l >> 24 & 0xFL) / 15.0f) - 0.5) * 0.5;
        }
        this.tesselateCrossTexture(tile, this.level.getData(x, y, z), d, d2, d3);
        return true;
    
    }

    public boolean tesselateRowInWorld(Tile tile, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        float f = tile.getBrightness(this.level, x, y, z);
        tesselator.color(f, f, f);
        this.tesselateRowTexture(tile, this.level.getData(x, y, z), x, (float)y - 0.0625f, z);
        return true;
    
    }

    public void tesselateTorch(Tile tile, double x, double y, double z, double xxa, double zza) {
        VertexSink tesselator = this.writer;
        int n = tile.getTexture(0);
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        float f = (float)n2 / 256.0f;
        float f2 = ((float)n2 + 15.99f) / 256.0f;
        float f3 = (float)n3 / 256.0f;
        float f4 = ((float)n3 + 15.99f) / 256.0f;
        double d = (double)f + 0.02734375;
        double d2 = (double)f3 + 0.0234375;
        double d3 = (double)f + 0.03515625;
        double d4 = (double)f3 + 0.03125;
        double d5 = (x += 0.5) - 0.5;
        double d6 = x + 0.5;
        double d7 = (z += 0.5) - 0.5;
        double d8 = z + 0.5;
        double d9 = 0.0625;
        double d10 = 0.625;
        tesselator.vertexUV(x + xxa * (1.0 - d10) - d9, y + d10, z + zza * (1.0 - d10) - d9, d, d2);
        tesselator.vertexUV(x + xxa * (1.0 - d10) - d9, y + d10, z + zza * (1.0 - d10) + d9, d, d4);
        tesselator.vertexUV(x + xxa * (1.0 - d10) + d9, y + d10, z + zza * (1.0 - d10) + d9, d3, d4);
        tesselator.vertexUV(x + xxa * (1.0 - d10) + d9, y + d10, z + zza * (1.0 - d10) - d9, d3, d2);
        tesselator.vertexUV(x - d9, y + 1.0, d7, f, f3);
        tesselator.vertexUV(x - d9 + xxa, y + 0.0, d7 + zza, f, f4);
        tesselator.vertexUV(x - d9 + xxa, y + 0.0, d8 + zza, f2, f4);
        tesselator.vertexUV(x - d9, y + 1.0, d8, f2, f3);
        tesselator.vertexUV(x + d9, y + 1.0, d8, f, f3);
        tesselator.vertexUV(x + xxa + d9, y + 0.0, d8 + zza, f, f4);
        tesselator.vertexUV(x + xxa + d9, y + 0.0, d7 + zza, f2, f4);
        tesselator.vertexUV(x + d9, y + 1.0, d7, f2, f3);
        tesselator.vertexUV(d5, y + 1.0, z + d9, f, f3);
        tesselator.vertexUV(d5 + xxa, y + 0.0, z + d9 + zza, f, f4);
        tesselator.vertexUV(d6 + xxa, y + 0.0, z + d9 + zza, f2, f4);
        tesselator.vertexUV(d6, y + 1.0, z + d9, f2, f3);
        tesselator.vertexUV(d6, y + 1.0, z - d9, f, f3);
        tesselator.vertexUV(d6 + xxa, y + 0.0, z - d9 + zza, f, f4);
        tesselator.vertexUV(d5 + xxa, y + 0.0, z - d9 + zza, f2, f4);
        tesselator.vertexUV(d5, y + 1.0, z - d9, f2, f3);
    
    }

    public void tesselateCrossTexture(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.5D - 0.45;
        double x1 = x + 0.5D + 0.45;
        double z0 = z + 0.5D - 0.45;
        double z1 = z + 0.5D + 0.45;
        double y0 = y + 0.0D;
        double y1 = y + 1.0D;
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }

        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
    
    }

    public void tesselateRowTexture(Tile tile, int data, double x, double y, double z) {
        VertexSink tesselator = this.writer;
        int n = tile.getTexture(0, data);
        if (this.fixedTexture >= 0) {
            n = this.fixedTexture;
        }
        int n2 = (n & 0xF) << 4;
        int n3 = n & 0xF0;
        double d = (float)n2 / 256.0f;
        double d2 = ((float)n2 + 15.99f) / 256.0f;
        double d3 = (float)n3 / 256.0f;
        double d4 = ((float)n3 + 15.99f) / 256.0f;
        double d5 = x + 0.5 - 0.25;
        double d6 = x + 0.5 + 0.25;
        double d7 = z + 0.5 - 0.5;
        double d8 = z + 0.5 + 0.5;
        tesselator.vertexUV(d5, y + 1.0, d7, d, d3);
        tesselator.vertexUV(d5, y + 0.0, d7, d, d4);
        tesselator.vertexUV(d5, y + 0.0, d8, d2, d4);
        tesselator.vertexUV(d5, y + 1.0, d8, d2, d3);
        tesselator.vertexUV(d5, y + 1.0, d8, d, d3);
        tesselator.vertexUV(d5, y + 0.0, d8, d, d4);
        tesselator.vertexUV(d5, y + 0.0, d7, d2, d4);
        tesselator.vertexUV(d5, y + 1.0, d7, d2, d3);
        tesselator.vertexUV(d6, y + 1.0, d8, d, d3);
        tesselator.vertexUV(d6, y + 0.0, d8, d, d4);
        tesselator.vertexUV(d6, y + 0.0, d7, d2, d4);
        tesselator.vertexUV(d6, y + 1.0, d7, d2, d3);
        tesselator.vertexUV(d6, y + 1.0, d7, d, d3);
        tesselator.vertexUV(d6, y + 0.0, d7, d, d4);
        tesselator.vertexUV(d6, y + 0.0, d8, d2, d4);
        tesselator.vertexUV(d6, y + 1.0, d8, d2, d3);
        d5 = x + 0.5 - 0.5;
        d6 = x + 0.5 + 0.5;
        d7 = z + 0.5 - 0.25;
        d8 = z + 0.5 + 0.25;
        tesselator.vertexUV(d5, y + 1.0, d7, d, d3);
        tesselator.vertexUV(d5, y + 0.0, d7, d, d4);
        tesselator.vertexUV(d6, y + 0.0, d7, d2, d4);
        tesselator.vertexUV(d6, y + 1.0, d7, d2, d3);
        tesselator.vertexUV(d6, y + 1.0, d7, d, d3);
        tesselator.vertexUV(d6, y + 0.0, d7, d, d4);
        tesselator.vertexUV(d5, y + 0.0, d7, d2, d4);
        tesselator.vertexUV(d5, y + 1.0, d7, d2, d3);
        tesselator.vertexUV(d6, y + 1.0, d8, d, d3);
        tesselator.vertexUV(d6, y + 0.0, d8, d, d4);
        tesselator.vertexUV(d5, y + 0.0, d8, d2, d4);
        tesselator.vertexUV(d5, y + 1.0, d8, d2, d3);
        tesselator.vertexUV(d5, y + 1.0, d8, d, d3);
        tesselator.vertexUV(d5, y + 0.0, d8, d, d4);
        tesselator.vertexUV(d6, y + 0.0, d8, d2, d4);
        tesselator.vertexUV(d6, y + 1.0, d8, d2, d3);
    
    }

    public boolean tesselateWaterInWorld(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        boolean faceUp = block.shouldRenderFace(this.level, x, y + 1, z, 1);
        boolean faceDown = block.shouldRenderFace(this.level, x, y - 1, z, 0);
        boolean[] facesH = new boolean[] {
            block.shouldRenderFace(this.level, x, y, z - 1, 2), block.shouldRenderFace(this.level, x, y, z + 1, 3),
            block.shouldRenderFace(this.level, x - 1, y, z, 4), block.shouldRenderFace(this.level, x + 1, y, z, 5)
        };
        if (!faceUp && !faceDown && !facesH[0] && !facesH[1] && !facesH[2] && !facesH[3]) {
            return false;
        }

        int foliageColor = this.foliageColor(block, x, y, z);
        float red = (foliageColor >> 16 & 255) / 255.0F;
        float green = (foliageColor >> 8 & 255) / 255.0F;
        float blue = (foliageColor & 255) / 255.0F;

        Material material = block.material;
        int meta = this.level.getData(x, y, z);
        double selfH = this.getWaterHeight(x, y, z, material);
        double frontH = this.getWaterHeight(x, y, z + 1, material);
        double frontRightH = this.getWaterHeight(x + 1, y, z + 1, material);
        double rightH = this.getWaterHeight(x + 1, y, z, material);

        boolean hasFaceUp = false;
        if (this.noCulling || faceUp) {
            ts.normal(0.0f, 1.0f, 0.0f);
            hasFaceUp = true;
            float angle = (float) LiquidTile.getSlopeAngle(this.level, x, y, z, material);
            int texture = block.getTexture(angle > -999.0F ? Facing.NORTH : Facing.UP, meta);

            int texAngle = 8;
            double uSin = 0.0 * 8.0F / 256.0F;
            double uCos = 1.0 * 8.0F / 256.0F;
            if (angle >= -999.0F) {
                texAngle = 16;
                uSin = Mth.sin(angle) * 8.0F / 256.0F;
                uCos = Mth.cos(angle) * 8.0F / 256.0F;
            }

            int texX = (texture & 15) << 4;
            int texY = texture & 240;
            double u = (texX + texAngle) / 256.0;
            double v = (texY + texAngle) / 256.0;

            double u0 = uCos - uSin;
            double u1 = uCos + uSin;
            float light = block.getBrightness(this.level, x, y, z);

            ts.color(light * red, light * green, light * blue);
            ts.vertexUV(x, y + selfH, z, u - u1, v - u0);
            ts.vertexUV(x, y + frontH, z + 1, u - u0, v + u1);
            ts.vertexUV(x + 1, y + frontRightH, z + 1, u + u1, v + u0);
            ts.vertexUV(x + 1, y + rightH, z, u + u0, v - u1);
        }

        if (this.noCulling || faceDown) {
            float light = block.getBrightness(this.level, x, y - 1, z) * 0.5F;
            ts.color(red * light, green * light, blue * light);
            this.renderFaceDown(block, x, y, z, block.getTexture(Facing.DOWN));
            hasFaceUp = true;
        }

        for (int side = 0; side < 4; ++side) {
            if (!this.noCulling && !facesH[side]) {
                continue;
            }

            int sideX = x;
            int sideZ = z;
            if (side == 0) {
                sideZ = z - 1;
            }
            else if (side == 1) {
                ++sideZ;
            }
            else if (side == 2) {
                sideX = x - 1;
            }
            else if (side == 3) {
                ++sideX;
            }

            int texture = block.getTexture(side + 2, meta);
            double texX = (texture & 15) << 4;
            double texY = texture & 240;
            double y0;
            double y1;
            double x0;
            double x1;
            double z0;
            double z1;

            if (side == 0) {
                y0 = selfH;
                y1 = rightH;
                x0 = x;
                x1 = x + 1;
                z0 = z;
                z1 = z;
            }
            else if (side == 1) {
                y0 = frontRightH;
                y1 = frontH;
                x0 = (x + 1);
                x1 = x;
                z0 = (z + 1);
                z1 = (z + 1);
            }
            else if (side == 2) {
                y0 = frontH;
                y1 = selfH;
                x0 = x;
                x1 = x;
                z0 = (z + 1);
                z1 = z;
            }
            else {
                y0 = rightH;
                y1 = frontRightH;
                x0 = (x + 1);
                x1 = (x + 1);
                z0 = z;
                z1 = (z + 1);
            }

            hasFaceUp = true;
            double u0 = texX / 256.0;
            double u1 = (texX + 15.99) / 256.0D;
            double v0 = (texY + (1.0 - y0) * 16.0) / 256.0;
            double v1 = (texY + (1.0 - y1) * 16.0) / 256.0;
            double v2 = (texY + 15.99) / 256.0D;
            float light = block.getBrightness(this.level, sideX, y, sideZ);
            light *= side < 2 ? 0.8F : 0.6F;

            ts.color(light * red, light * green, light * blue);
            switch (side) {
                case 0 -> ts.normal(0.0f, 0.0f, -1.0f);
                case 1 -> ts.normal(0.0f, 0.0f, 1.0f);
                case 2 -> ts.normal(-1.0f, 0.0f, 0.0f);
                case 3 -> ts.normal(1.0f, 0.0f, 0.0f);
            }
            ts.vertexUV(x0, y + y0, z0, u0, v0);
            ts.vertexUV(x1, y + y1, z1, u1, v1);
            ts.vertexUV(x1, y, z1, u1, v2);
            ts.vertexUV(x0, y, z0, u0, v2);
        }

        return hasFaceUp;
    
    }

    private float getWaterHeight(int x, int y, int z, Material m) {
        int n = 0;
        float f = 0.0f;
        for (int i = 0; i < 4; ++i) {
            int n2 = x - (i & 1);
            int n3 = y;
            int n4 = z - (i >> 1 & 1);
            if (this.level.getMaterial(n2, n3 + 1, n4) == m) {
                return 1.0f;
            }
            Material material = this.level.getMaterial(n2, n3, n4);
            if (material == m) {
                int n5 = this.level.getData(n2, n3, n4);
                if (n5 >= 8 || n5 == 0) {
                    f += LiquidTile.getHeight(n5) * 10.0f;
                    n += 10;
                }
                f += LiquidTile.getHeight(n5);
                ++n;
                continue;
            }
            if (material.isSolid()) continue;
            f += 1.0f;
            ++n;
        }
        return 1.0f - f / (float)n;
    
    }



    public boolean tesselateBlockInWorld(Tile tile, int x, int y, int z) {
        int n = this.foliageColor(tile, x, y, z);
        float f = (float)(n >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n & 0xFF) / 255.0f;
        if (this.options.anaglyph()) {
            float f4 = (f * 30.0f + f2 * 59.0f + f3 * 11.0f) / 100.0f;
            float f5 = (f * 30.0f + f2 * 70.0f) / 100.0f;
            float f6 = (f * 30.0f + f3 * 70.0f) / 100.0f;
            f = f4;
            f2 = f5;
            f3 = f6;
        }
        if (this.options.ambientOcclusion()) {
            return this.tesselateBlockInWorldWithAmbienceOcclusion(tile, x, y, z, f, f2, f3);
        }
        return this.tesselateBlockInWorld(tile, x, y, z, f, f2, f3);
    
    }

    public boolean tesselateBlockInWorldWithAmbienceOcclusion(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b
    ) {
        this.blen = true;
        float aoLevel = this.options.aoLevel();
        boolean renderAny = false;

        boolean renderBottom = this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN);
        boolean renderTop = this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, Facing.UP);
        boolean renderEast = this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH);
        boolean renderWest = this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH);
        boolean renderNorth = this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST);
        boolean renderSouth = this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST);

        if (renderTop || renderSouth) {
            this.field_70 = this.isTranslucent(x + 1, y + 1, z);
        }

        if (renderBottom || renderSouth) {
            this.field_78 = this.isTranslucent(x + 1, y - 1, z);
        }

        if (renderWest || renderSouth) {
            this.field_74 = this.isTranslucent(x + 1, y, z + 1);
        }

        if (renderEast || renderSouth) {
            this.field_76 = this.isTranslucent(x + 1, y, z - 1);
        }

        if (renderTop || renderNorth) {
            this.field_71 = this.isTranslucent(x - 1, y + 1, z);
        }

        if (renderBottom || renderNorth) {
            this.field_79 = this.isTranslucent(x - 1, y - 1, z);
        }

        if (renderEast || renderNorth) {
            this.field_73 = this.isTranslucent(x - 1, y, z - 1);
        }

        if (renderWest || renderNorth) {
            this.field_75 = this.isTranslucent(x - 1, y, z + 1);
        }

        if (renderTop || renderWest) {
            this.field_72 = this.isTranslucent(x, y + 1, z + 1);
        }

        if (renderTop || renderEast) {
            this.field_69 = this.isTranslucent(x, y + 1, z - 1);
        }

        if (renderBottom || renderWest) {
            this.field_80 = this.isTranslucent(x, y - 1, z + 1);
        }

        if (renderBottom || renderEast) {
            this.field_77 = this.isTranslucent(x, y - 1, z - 1);
        }

        boolean doGrassEdges = fancy && block.id == Tile.GRASS.id;
        boolean useColor = block.id != Tile.GRASS.id && this.fixedTexture < 0;

        if (renderBottom) {
            renderAny |= this.renderBottomSide(block, x, y, z, r, g, b, aoLevel, useColor);
        }
        if (renderTop) {
            renderAny |= this.renderTopSide(block, x, y, z, r, g, b, aoLevel, useColor);
        }
        if (renderEast) {
            renderAny |= this.renderEastSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderWest) {
            renderAny |= this.renderWestSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderNorth) {
            renderAny |= this.renderNorthSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }
        if (renderSouth) {
            renderAny |= this.renderSouthSide(block, x, y, z, r, g, b, aoLevel, useColor, doGrassEdges);
        }

        this.blen = false;
        return renderAny;
    
    }

    public boolean tesselateBlockInWorld(Tile block, int x, int y, int z, float r, float g, float b) {
        this.blen = false;
        boolean doGrassEdges = fancy && block.id == Tile.GRASS.id;
        VertexSink ts = this.tesselator();
        boolean renderAny = false;
        float colorFactor0 = 0.5F;
        float colorFactor1 = 0.8F;
        float colorFactor2 = 0.6F;
        float r_cf0 = colorFactor0;
        float r_cf1 = colorFactor1;
        float r_cf2 = colorFactor2;
        float g_cf0 = colorFactor0;
        float g_cf1 = colorFactor1;
        float g_cf2 = colorFactor2;
        float b_cf0 = colorFactor0;
        float b_cf1 = colorFactor1;
        float b_cf2 = colorFactor2;

        if (block.id != Tile.GRASS.id) {
            r_cf0 *= r;
            r_cf1 *= r;
            r_cf2 *= r;
            g_cf0 *= g;
            g_cf1 *= g;
            g_cf2 *= g;
            b_cf0 *= b;
            b_cf1 *= b;
            b_cf2 *= b;
        }

        float coreBrightness = block.getBrightness(this.level, x, y, z);

        if (this.noCulling || block.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN)) {
            float brightness = block.getBrightness(this.level, x, y - 1, z);
            ts.color(r_cf0 * brightness, g_cf0 * brightness, b_cf0 * brightness);
            this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.DOWN));
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y + 1, z, Facing.UP)) {
            float brightness = block.yy1 != 1.0D && !block.material.isLiquid()
                ? coreBrightness
                : block.getBrightness(this.level, x, y + 1, z);

            ts.color(r * brightness, g * brightness, b * brightness);
            this.renderFaceUp(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.UP));
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH)) {
            float brightness = block.zz0 > 0.0D ? coreBrightness : block.getBrightness(this.level, x, y, z - 1);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.NORTH);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
            }
            else {
                ts.color(r_cf1 * brightness, g_cf1 * brightness, b_cf1 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderNorth(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
                this.renderNorth(block, x, y, z, 38);
            }
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH)) {
            float brightness = block.zz1 < 1.0D ? coreBrightness : block.getBrightness(this.level, x, y, z + 1);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.SOUTH);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
            }
            else {
                ts.color(r_cf1 * brightness, g_cf1 * brightness, b_cf1 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderSouth(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf1 * brightness * r, g_cf1 * brightness * g, b_cf1 * brightness * b);
                this.renderSouth(block, x, y, z, 38);
            }

            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST)) {
            float brightness = block.xx0 > 0.0D ? coreBrightness : block.getBrightness(this.level, x - 1, y, z);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.WEST);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
            }
            else {
                ts.color(r_cf2 * brightness, g_cf2 * brightness, b_cf2 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderWest(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
                this.renderWest(block, x, y, z, 38);
            }
            renderAny = true;
        }

        if (this.noCulling || block.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST)) {
            float brightness = block.xx1 < 1.0D ? coreBrightness : block.getBrightness(this.level, x + 1, y, z);

            long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.EAST);
            if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
            }
            else {
                ts.color(r_cf2 * brightness, g_cf2 * brightness, b_cf2 * brightness);
            }

            int texture = AC_TexturedBlock.toTexture(textureKey);
            this.renderEast(block, x, y, z, texture);

            if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
                ts.color(r_cf2 * brightness * r, g_cf2 * brightness * g, b_cf2 * brightness * b);
                this.renderEast(block, x, y, z, 38);
            }
            renderAny = true;
        }

        return renderAny;
    
    }

    public boolean tesselateCactusInWorld(Tile tile, int x, int y, int z) {
        int n = this.foliageColor(tile, x, y, z);
        float f = (float)(n >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n & 0xFF) / 255.0f;
        if (this.options.anaglyph()) {
            float f4 = (f * 30.0f + f2 * 59.0f + f3 * 11.0f) / 100.0f;
            float f5 = (f * 30.0f + f2 * 70.0f) / 100.0f;
            float f6 = (f * 30.0f + f3 * 70.0f) / 100.0f;
            f = f4;
            f2 = f5;
            f3 = f6;
        }
        return this.method_63(tile, x, y, z, f, f2, f3);
    
    }

    public boolean method_63(Tile tile, int x, int y, int z, float r, float g, float b) {
        VertexSink tesselator = this.tesselator();
        boolean hasFaces = false;

        final float luma_lo = 0.5f;
        final float luma_hi = 1.0f;
        final float luma_z = 0.8f;
        final float luma_x = 0.6f;

        float offset = 0.0625f;
        float luma = tile.getBrightness(this.level, x, y, z);

        if (this.noCulling || tile.shouldRenderFace(this.level, x, y - 1, z, Facing.DOWN)) {
            float f = luma_lo * tile.getBrightness(this.level, x, y - 1, z);
            tesselator.color(r * f, g * f, b * f);
            this.renderFaceDown(tile, x, y, z, tile.getTexture(this.level, x, y, z, Facing.DOWN));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y + 1, z, Facing.UP)) {
            boolean contained = tile.yy1 != 1.0 && !tile.material.isLiquid();
            float f = luma_hi * (contained ? luma : tile.getBrightness(this.level, x, y + 1, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderFaceUp(tile, x, y, z, tile.getTexture(this.level, x, y, z, Facing.UP));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y, z - 1, Facing.NORTH)) {
            float f = luma_z * (tile.zz0 > 0.0 ? luma : tile.getBrightness(this.level, x, y, z - 1));
            tesselator.color(r * f, g * f, b * f);
            this.renderNorth(tile, x, y, z + offset, tile.getTexture(this.level, x, y, z, Facing.NORTH));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x, y, z + 1, Facing.SOUTH)) {
            float f = luma_z * (tile.zz1 < 1.0 ? luma : tile.getBrightness(this.level, x, y, z + 1));
            tesselator.color(r * f, g * f, b * f);
            this.renderSouth(tile, x, y, z - offset, tile.getTexture(this.level, x, y, z, Facing.SOUTH));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x - 1, y, z, Facing.WEST)) {
            float f = luma_x * (tile.xx0 > 0.0 ? luma : tile.getBrightness(this.level, x - 1, y, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderWest(tile, x + offset, y, z, tile.getTexture(this.level, x, y, z, Facing.WEST));
            hasFaces = true;
        }
        if (this.noCulling || tile.shouldRenderFace(this.level, x + 1, y, z, Facing.EAST)) {
            float f = luma_x * (tile.xx1 < 1.0 ? luma : tile.getBrightness(this.level, x + 1, y, z));
            tesselator.color(r * f, g * f, b * f);
            this.renderEast(tile, x - offset, y, z, tile.getTexture(this.level, x, y, z, Facing.EAST));
            hasFaces = true;
        }
        return hasFaces;
    
    }

    public boolean tesselateFenceInWorld(Tile block, int x, int y, int z) {
        float minZ = 6.0F / 16.0F;
        float maxZ = 10.0F / 16.0F;
        block.setShape(minZ, 0.0F, minZ, maxZ, 1.0F, maxZ);
        this.tesselateBlockInWorld(block, x, y, z);

        boolean renderAny = true;

        boolean connectLeft = this.level.getTile(x - 1, y, z) == block.id;
        boolean connectRight = this.level.getTile(x + 1, y, z) == block.id;
        boolean connectBack = this.level.getTile(x, y, z - 1) == block.id;
        boolean connectFront = this.level.getTile(x, y, z + 1) == block.id;

        boolean connectX = connectLeft || connectRight;
        boolean connectZ = connectBack || connectFront;

        if (!connectX && !connectZ) {
            connectX = true;
        }

        minZ = 7.0F / 16.0F;
        maxZ = 9.0F / 16.0F;
        float minY = 12.0F / 16.0F;
        float maxY = 15.0F / 16.0F;
        float minXX = connectLeft ? 0.0F : minZ;
        float maxXX = connectRight ? 1.0F : maxZ;
        float minXZ = connectBack ? 0.0F : minZ;
        float maxXZ = connectFront ? 1.0F : maxZ;

        if (connectX) {
            block.setShape(minXX, minY, minZ, maxXX, maxY, maxZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (connectZ) {
            block.setShape(minZ, minY, minXZ, maxZ, maxY, maxXZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        minY = 6.0F / 16.0F;
        maxY = 9.0F / 16.0F;
        if (connectX) {
            block.setShape(minXX, minY, minZ, maxXX, maxY, maxZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (connectZ) {
            block.setShape(minZ, minY, minXZ, maxZ, maxY, maxXZ);
            this.tesselateBlockInWorld(block, x, y, z);
            renderAny = true;
        }

        if (!connectFront && !connectLeft && this.level.getTile(x - 1, y, z + 1) == block.id) {
            this.tesselateFenceFrontLeft(block, x, y, z, maxZ, minZ);
        }

        if (!connectFront && !connectRight && this.level.getTile(x + 1, y, z + 1) == block.id) {
            this.tesselateFenceFrontRight(block, x, y, z, maxZ, minZ);
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    
    }

    public boolean tesselateStairsInWorld(Tile block, int x, int y, int z) {
        boolean renderAny = false;
        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        this.tesselateBlockInWorld(block, x, y, z);

        int coreMeta = this.level.getData(x, y, z) & 3;
        if (coreMeta == 0) {
            this.tesselateStairs0(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 1) {
            this.tesselateStairs1(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 2) {
            this.tesselateStairs2(block, x, y, z);
            renderAny = true;
        }
        else if (coreMeta == 3) {
            this.tesselateStairs3(block, x, y, z);
            renderAny = true;
        }

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    
    }

    public boolean tesselateDoorInWorld(Tile tile, int x, int y, int z) {
        VertexSink tesselator = this.writer;
        DoorTile doorTile = (DoorTile)tile;
        boolean bl = false;
        float f = 0.5f;
        float f2 = 1.0f;
        float f3 = 0.8f;
        float f4 = 0.6f;
        float f5 = tile.getBrightness(this.level, x, y, z);
        float f6 = tile.getBrightness(this.level, x, y - 1, z);
        if (doorTile.yy0 > 0.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f * f6, f * f6, f * f6);
        this.renderFaceDown(tile, x, y, z, tile.getTexture(this.level, x, y, z, 0));
        bl = true;
        f6 = tile.getBrightness(this.level, x, y + 1, z);
        if (doorTile.yy1 < 1.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f2 * f6, f2 * f6, f2 * f6);
        this.renderFaceUp(tile, x, y, z, tile.getTexture(this.level, x, y, z, 1));
        bl = true;
        f6 = tile.getBrightness(this.level, x, y, z - 1);
        if (doorTile.zz0 > 0.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f3 * f6, f3 * f6, f3 * f6);
        int n = tile.getTexture(this.level, x, y, z, 2);
        if (n < 0) {
            this.xFlipTexture = true;
            n = -n;
        }
        this.renderNorth(tile, x, y, z, n);
        bl = true;
        this.xFlipTexture = false;
        f6 = tile.getBrightness(this.level, x, y, z + 1);
        if (doorTile.zz1 < 1.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f3 * f6, f3 * f6, f3 * f6);
        n = tile.getTexture(this.level, x, y, z, 3);
        if (n < 0) {
            this.xFlipTexture = true;
            n = -n;
        }
        this.renderSouth(tile, x, y, z, n);
        bl = true;
        this.xFlipTexture = false;
        f6 = tile.getBrightness(this.level, x - 1, y, z);
        if (doorTile.xx0 > 0.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f4 * f6, f4 * f6, f4 * f6);
        n = tile.getTexture(this.level, x, y, z, 4);
        if (n < 0) {
            this.xFlipTexture = true;
            n = -n;
        }
        this.renderWest(tile, x, y, z, n);
        bl = true;
        this.xFlipTexture = false;
        f6 = tile.getBrightness(this.level, x + 1, y, z);
        if (doorTile.xx1 < 1.0) {
            f6 = f5;
        }
        if (((ExBlock)tile).getBlockLightValue(this.level,x,y,z) > 0) {
            f6 = 1.0f;
        }
        tesselator.color(f4 * f6, f4 * f6, f4 * f6);
        n = tile.getTexture(this.level, x, y, z, 5);
        if (n < 0) {
            this.xFlipTexture = true;
            n = -n;
        }
        this.renderEast(tile, x, y, z, n);
        bl = true;
        this.xFlipTexture = false;
        return bl;
    
    }

    public void renderFaceDown(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.DOWN, texture);
        this.writer.ac$normal32(0,-1,0);
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d = ((double)n + tile.xx0 * 16.0) / 256.0;
        double d2 = ((double)n + tile.xx1 * 16.0 - 0.01) / 256.0;
        double d3 = ((double)n2 + tile.zz0 * 16.0) / 256.0;
        double d4 = ((double)n2 + tile.zz1 * 16.0 - 0.01) / 256.0;
        if (tile.xx0 < 0.0 || tile.xx1 > 1.0) {
            d = ((float)n + 0.0f) / 256.0f;
            d2 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.zz0 < 0.0 || tile.zz1 > 1.0) {
            d3 = ((float)n2 + 0.0f) / 256.0f;
            d4 = ((float)n2 + 15.99f) / 256.0f;
        }
        double d5 = d2;
        double d6 = d;
        double d7 = d3;
        double d8 = d4;
        if (this.bottomUvRotation == 2) {
            d = ((double)n + tile.zz0 * 16.0) / 256.0;
            d3 = ((double)(n2 + 16) - tile.xx1 * 16.0) / 256.0;
            d2 = ((double)n + tile.zz1 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.xx0 * 16.0) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
            d5 = d;
            d6 = d2;
            d3 = d4;
            d4 = d7;
        } else if (this.bottomUvRotation == 1) {
            d = ((double)(n + 16) - tile.zz1 * 16.0) / 256.0;
            d3 = ((double)n2 + tile.xx0 * 16.0) / 256.0;
            d2 = ((double)(n + 16) - tile.zz0 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.xx1 * 16.0) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
            d = d5;
            d2 = d6;
            d7 = d4;
            d8 = d3;
        } else if (this.bottomUvRotation == 3) {
            d = ((double)(n + 16) - tile.xx0 * 16.0) / 256.0;
            d2 = ((double)(n + 16) - tile.xx1 * 16.0 - 0.01) / 256.0;
            d3 = ((double)(n2 + 16) - tile.zz0 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.zz1 * 16.0 - 0.01) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
        }
        double d9 = x + tile.xx0;
        double d10 = x + tile.xx1;
        double d11 = y + tile.yy0;
        double d12 = z + tile.zz0;
        double d13 = z + tile.zz1;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d9, d11, d13, d6, d8);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d9, d11, d12, d, d3);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d10, d11, d12, d5, d7);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d10, d11, d13, d2, d4);
        } else {
            tesselator.vertexUV(d9, d11, d13, d6, d8);
            tesselator.vertexUV(d9, d11, d12, d, d3);
            tesselator.vertexUV(d10, d11, d12, d5, d7);
            tesselator.vertexUV(d10, d11, d13, d2, d4);
        }
    
    }

    public void renderFaceUp(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.UP, texture);
        this.writer.ac$normal32(0,1,0);
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d = ((double)n + tile.xx0 * 16.0) / 256.0;
        double d2 = ((double)n + tile.xx1 * 16.0 - 0.01) / 256.0;
        double d3 = ((double)n2 + tile.zz0 * 16.0) / 256.0;
        double d4 = ((double)n2 + tile.zz1 * 16.0 - 0.01) / 256.0;
        if (tile.xx0 < 0.0 || tile.xx1 > 1.0) {
            d = ((float)n + 0.0f) / 256.0f;
            d2 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.zz0 < 0.0 || tile.zz1 > 1.0) {
            d3 = ((float)n2 + 0.0f) / 256.0f;
            d4 = ((float)n2 + 15.99f) / 256.0f;
        }
        double d5 = d2;
        double d6 = d;
        double d7 = d3;
        double d8 = d4;
        if (this.topUvRotation == 1) {
            d = ((double)n + tile.zz0 * 16.0) / 256.0;
            d3 = ((double)(n2 + 16) - tile.xx1 * 16.0) / 256.0;
            d2 = ((double)n + tile.zz1 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.xx0 * 16.0) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
            d5 = d;
            d6 = d2;
            d3 = d4;
            d4 = d7;
        } else if (this.topUvRotation == 2) {
            d = ((double)(n + 16) - tile.zz1 * 16.0) / 256.0;
            d3 = ((double)n2 + tile.xx0 * 16.0) / 256.0;
            d2 = ((double)(n + 16) - tile.zz0 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.xx1 * 16.0) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
            d = d5;
            d2 = d6;
            d7 = d4;
            d8 = d3;
        } else if (this.topUvRotation == 3) {
            d = ((double)(n + 16) - tile.xx0 * 16.0) / 256.0;
            d2 = ((double)(n + 16) - tile.xx1 * 16.0 - 0.01) / 256.0;
            d3 = ((double)(n2 + 16) - tile.zz0 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.zz1 * 16.0 - 0.01) / 256.0;
            d5 = d2;
            d6 = d;
            d7 = d3;
            d8 = d4;
        }
        double d9 = x + tile.xx0;
        double d10 = x + tile.xx1;
        double d11 = y + tile.yy1;
        double d12 = z + tile.zz0;
        double d13 = z + tile.zz1;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d10, d11, d13, d2, d4);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d10, d11, d12, d5, d7);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d9, d11, d12, d, d3);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d9, d11, d13, d6, d8);
        } else {
            tesselator.vertexUV(d10, d11, d13, d2, d4);
            tesselator.vertexUV(d10, d11, d12, d5, d7);
            tesselator.vertexUV(d9, d11, d12, d, d3);
            tesselator.vertexUV(d9, d11, d13, d6, d8);
        }
    
    }

    public void renderNorth(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.NORTH, texture);
        this.writer.ac$normal32(0,0,-1);
        double d;
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d2 = ((double)n + tile.xx0 * 16.0) / 256.0;
        double d3 = ((double)n + tile.xx1 * 16.0 - 0.01) / 256.0;
        double d4 = ((double)(n2 + 16) - tile.yy1 * 16.0) / 256.0;
        double d5 = ((double)(n2 + 16) - tile.yy0 * 16.0 - 0.01) / 256.0;
        if (this.xFlipTexture) {
            d = d2;
            d2 = d3;
            d3 = d;
        }
        if (tile.xx0 < 0.0 || tile.xx1 > 1.0) {
            d2 = ((float)n + 0.0f) / 256.0f;
            d3 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.yy0 < 0.0 || tile.yy1 > 1.0) {
            d4 = ((float)n2 + 0.0f) / 256.0f;
            d5 = ((float)n2 + 15.99f) / 256.0f;
        }
        d = d3;
        double d6 = d2;
        double d7 = d4;
        double d8 = d5;
        if (this.northUvRotation == 2) {
            d2 = ((double)n + tile.yy0 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.xx0 * 16.0) / 256.0;
            d3 = ((double)n + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)(n2 + 16) - tile.xx1 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d = d2;
            d6 = d3;
            d4 = d5;
            d5 = d7;
        } else if (this.northUvRotation == 1) {
            d2 = ((double)(n + 16) - tile.yy1 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.xx1 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.yy0 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.xx0 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d2 = d;
            d3 = d6;
            d7 = d5;
            d8 = d4;
        } else if (this.northUvRotation == 3) {
            d2 = ((double)(n + 16) - tile.xx0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.xx1 * 16.0 - 0.01) / 256.0;
            d4 = ((double)n2 + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.yy0 * 16.0 - 0.01) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
        }
        double d9 = x + tile.xx0;
        double d10 = x + tile.xx1;
        double d11 = y + tile.yy0;
        double d12 = y + tile.yy1;
        double d13 = z + tile.zz0;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d9, d12, d13, d, d7);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d10, d12, d13, d2, d4);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d10, d11, d13, d6, d8);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d9, d11, d13, d3, d5);
        } else {
            tesselator.vertexUV(d9, d12, d13, d, d7);
            tesselator.vertexUV(d10, d12, d13, d2, d4);
            tesselator.vertexUV(d10, d11, d13, d6, d8);
            tesselator.vertexUV(d9, d11, d13, d3, d5);
        }
    
    }

    public void renderSouth(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.SOUTH, texture);
        this.writer.ac$normal32(0,0,1);
        double d;
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d2 = ((double)n + tile.xx0 * 16.0) / 256.0;
        double d3 = ((double)n + tile.xx1 * 16.0 - 0.01) / 256.0;
        double d4 = ((double)(n2 + 16) - tile.yy1 * 16.0) / 256.0;
        double d5 = ((double)(n2 + 16) - tile.yy0 * 16.0 - 0.01) / 256.0;
        if (this.xFlipTexture) {
            d = d2;
            d2 = d3;
            d3 = d;
        }
        if (tile.xx0 < 0.0 || tile.xx1 > 1.0) {
            d2 = ((float)n + 0.0f) / 256.0f;
            d3 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.yy0 < 0.0 || tile.yy1 > 1.0) {
            d4 = ((float)n2 + 0.0f) / 256.0f;
            d5 = ((float)n2 + 15.99f) / 256.0f;
        }
        d = d3;
        double d6 = d2;
        double d7 = d4;
        double d8 = d5;
        if (this.southUvRotation == 1) {
            d2 = ((double)n + tile.yy0 * 16.0) / 256.0;
            d5 = ((double)(n2 + 16) - tile.xx0 * 16.0) / 256.0;
            d3 = ((double)n + tile.yy1 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.xx1 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d = d2;
            d6 = d3;
            d4 = d5;
            d5 = d7;
        } else if (this.southUvRotation == 2) {
            d2 = ((double)(n + 16) - tile.yy1 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.xx0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.yy0 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.xx1 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d2 = d;
            d3 = d6;
            d7 = d5;
            d8 = d4;
        } else if (this.southUvRotation == 3) {
            d2 = ((double)(n + 16) - tile.xx0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.xx1 * 16.0 - 0.01) / 256.0;
            d4 = ((double)n2 + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.yy0 * 16.0 - 0.01) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
        }
        double d9 = x + tile.xx0;
        double d10 = x + tile.xx1;
        double d11 = y + tile.yy0;
        double d12 = y + tile.yy1;
        double d13 = z + tile.zz1;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d9, d12, d13, d2, d4);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d9, d11, d13, d6, d8);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d10, d11, d13, d3, d5);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d10, d12, d13, d, d7);
        } else {
            tesselator.vertexUV(d9, d12, d13, d2, d4);
            tesselator.vertexUV(d9, d11, d13, d6, d8);
            tesselator.vertexUV(d10, d11, d13, d3, d5);
            tesselator.vertexUV(d10, d12, d13, d, d7);
        }
    
    }

    public void renderWest(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.WEST, texture);
        this.writer.ac$normal32(-1,0,0);
        double d;
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d2 = ((double)n + tile.zz0 * 16.0) / 256.0;
        double d3 = ((double)n + tile.zz1 * 16.0 - 0.01) / 256.0;
        double d4 = ((double)(n2 + 16) - tile.yy1 * 16.0) / 256.0;
        double d5 = ((double)(n2 + 16) - tile.yy0 * 16.0 - 0.01) / 256.0;
        if (this.xFlipTexture) {
            d = d2;
            d2 = d3;
            d3 = d;
        }
        if (tile.zz0 < 0.0 || tile.zz1 > 1.0) {
            d2 = ((float)n + 0.0f) / 256.0f;
            d3 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.yy0 < 0.0 || tile.yy1 > 1.0) {
            d4 = ((float)n2 + 0.0f) / 256.0f;
            d5 = ((float)n2 + 15.99f) / 256.0f;
        }
        d = d3;
        double d6 = d2;
        double d7 = d4;
        double d8 = d5;
        if (this.westUvRotation == 1) {
            d2 = ((double)n + tile.yy0 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.zz1 * 16.0) / 256.0;
            d3 = ((double)n + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)(n2 + 16) - tile.zz0 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d = d2;
            d6 = d3;
            d4 = d5;
            d5 = d7;
        } else if (this.westUvRotation == 2) {
            d2 = ((double)(n + 16) - tile.yy1 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.zz0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.yy0 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.zz1 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d2 = d;
            d3 = d6;
            d7 = d5;
            d8 = d4;
        } else if (this.westUvRotation == 3) {
            d2 = ((double)(n + 16) - tile.zz0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.zz1 * 16.0 - 0.01) / 256.0;
            d4 = ((double)n2 + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.yy0 * 16.0 - 0.01) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
        }
        double d9 = x + tile.xx0;
        double d10 = y + tile.yy0;
        double d11 = y + tile.yy1;
        double d12 = z + tile.zz0;
        double d13 = z + tile.zz1;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d9, d11, d13, d, d7);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d9, d11, d12, d2, d4);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d9, d10, d12, d6, d8);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d9, d10, d13, d3, d5);
        } else {
            tesselator.vertexUV(d9, d11, d13, d, d7);
            tesselator.vertexUV(d9, d11, d12, d2, d4);
            tesselator.vertexUV(d9, d10, d12, d6, d8);
            tesselator.vertexUV(d9, d10, d13, d3, d5);
        }
    
    }

    public void renderEast(Tile tile, double x, double y, double z, int texture) {
        this.materialForFace(tile, Facing.EAST, texture);
        this.writer.ac$normal32(1,0,0);
        double d;
        VertexSink tesselator = this.writer;
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }
        int n = (texture & 0xF) << 4;
        int n2 = texture & 0xF0;
        double d2 = ((double)n + tile.zz0 * 16.0) / 256.0;
        double d3 = ((double)n + tile.zz1 * 16.0 - 0.01) / 256.0;
        double d4 = ((double)(n2 + 16) - tile.yy1 * 16.0) / 256.0;
        double d5 = ((double)(n2 + 16) - tile.yy0 * 16.0 - 0.01) / 256.0;
        if (this.xFlipTexture) {
            d = d2;
            d2 = d3;
            d3 = d;
        }
        if (tile.zz0 < 0.0 || tile.zz1 > 1.0) {
            d2 = ((float)n + 0.0f) / 256.0f;
            d3 = ((float)n + 15.99f) / 256.0f;
        }
        if (tile.yy0 < 0.0 || tile.yy1 > 1.0) {
            d4 = ((float)n2 + 0.0f) / 256.0f;
            d5 = ((float)n2 + 15.99f) / 256.0f;
        }
        d = d3;
        double d6 = d2;
        double d7 = d4;
        double d8 = d5;
        if (this.eastUvRotation == 2) {
            d2 = ((double)n + tile.yy0 * 16.0) / 256.0;
            d4 = ((double)(n2 + 16) - tile.zz0 * 16.0) / 256.0;
            d3 = ((double)n + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)(n2 + 16) - tile.zz1 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d = d2;
            d6 = d3;
            d4 = d5;
            d5 = d7;
        } else if (this.eastUvRotation == 1) {
            d2 = ((double)(n + 16) - tile.yy1 * 16.0) / 256.0;
            d4 = ((double)n2 + tile.zz1 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.yy0 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.zz0 * 16.0) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
            d2 = d;
            d3 = d6;
            d7 = d5;
            d8 = d4;
        } else if (this.eastUvRotation == 3) {
            d2 = ((double)(n + 16) - tile.zz0 * 16.0) / 256.0;
            d3 = ((double)(n + 16) - tile.zz1 * 16.0 - 0.01) / 256.0;
            d4 = ((double)n2 + tile.yy1 * 16.0) / 256.0;
            d5 = ((double)n2 + tile.yy0 * 16.0 - 0.01) / 256.0;
            d = d3;
            d6 = d2;
            d7 = d4;
            d8 = d5;
        }
        double d9 = x + tile.xx1;
        double d10 = y + tile.yy0;
        double d11 = y + tile.yy1;
        double d12 = z + tile.zz0;
        double d13 = z + tile.zz1;
        if (this.blen) {
            tesselator.color(this.c1r, this.c1g, this.c1b);
            tesselator.vertexUV(d9, d10, d13, d6, d8);
            tesselator.color(this.c2r, this.c2g, this.c2b);
            tesselator.vertexUV(d9, d10, d12, d3, d5);
            tesselator.color(this.c3r, this.c3g, this.c3b);
            tesselator.vertexUV(d9, d11, d12, d, d7);
            tesselator.color(this.c4r, this.c4g, this.c4b);
            tesselator.vertexUV(d9, d11, d13, d2, d4);
        } else {
            tesselator.vertexUV(d9, d10, d13, d6, d8);
            tesselator.vertexUV(d9, d10, d12, d3, d5);
            tesselator.vertexUV(d9, d11, d12, d, d7);
            tesselator.vertexUV(d9, d11, d13, d2, d4);
        }
    
    }

    public void renderTile(Tile tile, int meta, float f) {
        if (this.inventoryColors) {
            int c = tile.getColor(meta);
            float rcp = f / 255.0f;
            float r = (float) (c >> 16 & 0xFF) * rcp;
            float g = (float) (c >> 8 & 0xFF) * rcp;
            float b = (float) (c & 0xFF) * rcp;
            this.writer.ac$color32(r, g, b, 1.0f);
        }

        VertexSink ts = this.tesselator();
        double o = -0.5;

        int shape = tile.getRenderShape();
        if (shape == BlockShapes.BLOCK || shape == BlockShapes.PISTON) {
            if (shape == BlockShapes.PISTON) {
                meta = 1;
            }
            tile.updateDefaultShape();

            ts.normal(0.0f, -1.0f, 0.0f);
            this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN, meta));

            ts.normal(0.0f, 1.0f, 0.0f);
            this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP, meta));

            ts.normal(0.0f, 0.0f, -1.0f);
            this.renderNorth(tile, o, o, o, tile.getTexture(Facing.NORTH, meta));

            ts.normal(0.0f, 0.0f, 1.0f);
            this.renderSouth(tile, o, o, o, tile.getTexture(Facing.SOUTH, meta));

            ts.normal(-1.0f, 0.0f, 0.0f);
            this.renderWest(tile, o, o, o, tile.getTexture(Facing.WEST, meta));

            ts.normal(1.0f, 0.0f, 0.0f);
            this.renderEast(tile, o, o, o, tile.getTexture(Facing.EAST, meta));
        }
        else if (shape == BlockShapes.REEDS) {
            ts.normal(0.0f, -1.0f, 0.0f);
            this.tesselateCrossTexture(tile, meta, o, o, o);
        }
        else if (shape == BlockShapes.CACTUS) {
            tile.updateDefaultShape();

            ts.normal(0.0f, -1.0f, 0.0f);
            this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN, meta));

            ts.normal(0.0f, 1.0f, 0.0f);
            this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP, meta));

            float f3 = 0.0625f;
            ts.normal(0.0f, 0.0f, -1.0f);
            this.renderNorth(tile, o, o, o + f3, tile.getTexture(Facing.NORTH, meta));

            ts.normal(0.0f, 0.0f, 1.0f);
            this.renderSouth(tile, o, o, o - f3, tile.getTexture(Facing.SOUTH, meta));

            ts.normal(-1.0f, 0.0f, 0.0f);
            this.renderWest(tile, o + f3, o, o, tile.getTexture(Facing.WEST, meta));

            ts.normal(1.0f, 0.0f, 0.0f);
            this.renderEast(tile, o - f3, o, o, tile.getTexture(Facing.EAST, meta));
        }
        else if (shape == BlockShapes.CROP) {
            ts.normal(0.0f, -1.0f, 0.0f);
            this.tesselateRowTexture(tile, meta, o, o, o);
        }
        else if (shape == BlockShapes.TORCH) {
            ts.normal(0.0f, -1.0f, 0.0f);
            this.tesselateTorch(tile, o, o, o, 0.0, 0.0);
        }
        else if (shape == BlockShapes.STAIRS) {
            for (int i = 0; i < 2; ++i) {
                switch (i) {
                    case 0 -> tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
                    case 1 -> tile.setShape(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
                }

                ts.normal(0.0f, -1.0f, 0.0f);
                this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN, meta));

                ts.normal(0.0f, 1.0f, 0.0f);
                this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP, meta));

                ts.normal(0.0f, 0.0f, -1.0f);
                this.renderNorth(tile, o, o, o, tile.getTexture(Facing.NORTH, meta));

                ts.normal(0.0f, 0.0f, 1.0f);
                this.renderSouth(tile, o, o, o, tile.getTexture(Facing.SOUTH, meta));

                ts.normal(-1.0f, 0.0f, 0.0f);
                this.renderWest(tile, o, o, o, tile.getTexture(Facing.WEST, meta));

                ts.normal(1.0f, 0.0f, 0.0f);
                this.renderEast(tile, o, o, o, tile.getTexture(Facing.EAST, meta));
            }
        }
        else if (shape == BlockShapes.FENCE) {
            for (int i = 0; i < 4; ++i) {
                float a = 0.125f;
                float b = 0.0625f;
                switch (i) {
                    case 0 -> tile.setShape(0.5f - a, 0.0f, 0.0f, 0.5f + a, 1.0f, a * 2.0f);
                    case 1 -> tile.setShape(0.5f - a, 0.0f, 1.0f - a * 2.0f, 0.5f + a, 1.0f, 1.0f);
                    case 2 -> tile.setShape(0.5f - b, 1.0f - b * 3.0f, -b * 2.0f, 0.5f + b, 1.0f - b, 1.0f + b * 2.0f);
                    case 3 -> tile.setShape(0.5f - b, 0.5f - b * 3.0f, -b * 2.0f, 0.5f + b, 0.5f - b, 1.0f + b * 2.0f);
                }

                ts.normal(0.0f, -1.0f, 0.0f);
                this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN));

                ts.normal(0.0f, 1.0f, 0.0f);
                this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP));

                ts.normal(0.0f, 0.0f, -1.0f);
                this.renderNorth(tile, o, o, o, tile.getTexture(Facing.NORTH));

                ts.normal(0.0f, 0.0f, 1.0f);
                this.renderSouth(tile, o, o, o, tile.getTexture(Facing.SOUTH));

                ts.normal(-1.0f, 0.0f, 0.0f);
                this.renderWest(tile, o, o, o, tile.getTexture(Facing.WEST));

                ts.normal(1.0f, 0.0f, 0.0f);
                this.renderEast(tile, o, o, o, tile.getTexture(Facing.EAST));
            }
            tile.setShape(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
        else if (shape == AC_BlockShapes.REDSTONE_POWER) {
            double minX = tile.xx0;
            double minY = tile.yy0;
            double minZ = tile.zz0;
            double maxX = tile.xx1;
            double maxY = tile.yy1;
            double maxZ = tile.zz1;
            this.renderInventoryBox(tile, meta, 0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
            this.fixedTexture = 115;
            ts.normal(0.0F, -1.0F, 0.0F);
            this.tesselateTorch(tile, o, o + 0.25D, o, 0.0D, 0.0D);
            this.fixedTexture = -1;
            tile.setShape((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        }
        else if (shape == AC_BlockShapes.SPIKES) {
            ts.normal(0.0F, -1.0F, 0.0F);
            this.tesselateCrossTexture(tile, meta, o, o, o);
        }
        else if (shape == AC_BlockShapes.TABLE) {
            double minX = tile.xx0;
            double minY = tile.yy0;
            double minZ = tile.zz0;
            double maxX = tile.xx1;
            double maxY = tile.yy1;
            double maxZ = tile.zz1;
            this.renderInventoryBox(tile, meta, 0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
            this.renderInventoryBox(tile, meta, 0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            this.renderInventoryBox(tile, meta, 13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            this.renderInventoryBox(tile, meta, 13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            this.renderInventoryBox(tile, meta, 0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            tile.setShape((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        }
        else if (shape == AC_BlockShapes.CHAIR) {
            double minX = tile.xx0;
            double minY = tile.yy0;
            double minZ = tile.zz0;
            double maxX = tile.xx1;
            double maxY = tile.yy1;
            double maxZ = tile.zz1;
            this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);
            switch (meta & 3) {
                case 0 -> this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                case 1 -> this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                case 2 -> this.renderInventoryBox(tile, meta, 12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                default -> this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
            }
            this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
            this.renderInventoryBox(tile, meta, 12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
            this.renderInventoryBox(tile, meta, 12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
            this.renderInventoryBox(tile, meta, 2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
            tile.setShape((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        }
        else if (shape == AC_BlockShapes.ROPE) {
            ts.normal(0.0F, -1.0F, 0.0F);
            switch (meta % 3) {
                case 0 -> this.tesselateCrossTexture(tile, meta, o, o, o);
                case 1 -> this.renderCrossedSquaresEast(tile, meta, o, o, o);
                default -> this.renderCrossedSquaresNorth(tile, meta, o, o, o);
            }
        }
        else if (shape == AC_BlockShapes.BLOCK_TREE) {
            ts.normal(0.0F, -1.0F, 0.0F);
            this.tesselateCrossTexture(tile, meta, o, o, o);
        }
        else if (shape == AC_BlockShapes.BLOCK_OVERLAY) {
            double minX = tile.xx0;
            double minY = tile.yy0;
            double minZ = tile.zz0;
            double maxX = tile.xx1;
            double maxY = tile.yy1;
            double maxZ = tile.zz1;
            tile.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F / 64.0F, 1.0F);
            ts.normal(0.0F, 1.0F, 0.0F);
            this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP, meta));
            ts.normal(0.0F, -1.0F, 0.0F);
            this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN, meta));
            tile.setShape((float) minX, (float) minY, (float) minZ, (float) maxX, (float) maxY, (float) maxZ);
        }
        else if (shape == AC_BlockShapes.BLOCK_SLOPE) {
            this.renderInventorySlope(tile, meta);
        }
    
    }

    public static boolean canRender(int model) {
        if (model == 0) {
            return true;
        }
        if (model == 13) {
            return true;
        }
        if (model == 10) {
            return true;
        }
        if (model == 11) {
            return true;
        }
        return model == 16;
    
    }

    private boolean tesselateRailInWorld(Tile block, int x, int y, int z) {
        return tesselateRailInWorld((RailTile) block, x, y, z);
    
    }

    private boolean isTranslucent(int x, int y, int z) {
        int tileId = this.level.getTile(x, y, z);
        return this.level instanceof LevelRegion region ? region.isTranslucent(tileId) : Tile.translucent[tileId];
    
    }

    private void resetColor(boolean useColor, float r, float g, float b, float factor) {
        float cR = factor;
        float cG = factor;
        float cB = factor;
        if (useColor) {
            cR *= r;
            cG *= g;
            cB *= b;
        }
        this.c1r = this.c2r = this.c3r = this.c4r = cR;
        this.c1g = this.c2g = this.c3g = this.c4g = cG;
        this.c1b = this.c2b = this.c3b = this.c4b = cB;
    
    }

    private void applyColorBrightness(float l1, float l2, float l3, float l4) {
        this.c1r *= l1;
        this.c1g *= l1;
        this.c1b *= l1;
        this.c2r *= l2;
        this.c2g *= l2;
        this.c2b *= l2;
        this.c3r *= l3;
        this.c3g *= l3;
        this.c3b *= l3;
        this.c4r *= l4;
        this.c4g *= l4;
        this.c4b *= l4;
    
    }

    private void multiplyColor(float r, float g, float b) {
        this.c1r *= r;
        this.c2r *= r;
        this.c3r *= r;
        this.c4r *= r;
        this.c1g *= g;
        this.c2g *= g;
        this.c3g *= g;
        this.c4g *= g;
        this.c1b *= b;
        this.c2b *= b;
        this.c3b *= b;
        this.c4b *= b;
    
    }

    private boolean renderBottomSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor
    ) {
        this.ll0y0 = block.getBrightness(this.level, x, y - 1, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll0y0;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --y;
            this.llxyz = block.getBrightness(this.level, x - 1, y, z);
            this.llxy0 = block.getBrightness(this.level, x, y, z - 1);
            this.llxyZ = block.getBrightness(this.level, x, y, z + 1);
            this.ll0yz = block.getBrightness(this.level, x + 1, y, z);

            boolean t0 = this.field_77;
            boolean t1 = this.field_80;
            this.ll0yZ = !t0 && !this.field_79 ? this.llxyz : block.getBrightness(this.level, x - 1, y, z - 1);
            this.llXyz = !t1 && !this.field_79 ? this.llxyz : block.getBrightness(this.level, x - 1, y, z + 1);
            this.llXy0 = !t0 && !this.field_78 ? this.ll0yz : block.getBrightness(this.level, x + 1, y, z - 1);
            this.llXyZ = !t1 && !this.field_78 ? this.ll0yz : block.getBrightness(this.level, x + 1, y, z + 1);

            ++y;
            if (aoLevel > 0.0f) {
                this.fixBottomAo(aoLevel);
            }

            l1 = (this.llXyz + this.llxyz + this.llxyZ + this.ll0y0) * (1 / 4F);
            l4 = (this.llxyZ + this.ll0y0 + this.llXyZ + this.ll0yz) * (1 / 4F);
            l3 = (this.ll0y0 + this.llxy0 + this.ll0yz + this.llXy0) * (1 / 4F);
            l2 = (this.llxyz + this.ll0yZ + this.ll0y0 + this.llxy0) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.5F);
        this.applyColorBrightness(l1, l2, l3, l4);

        this.renderFaceDown(block, x, y, z, block.getTexture(this.level, x, y, z, Facing.DOWN));
        return true;
    
    }

    private void fixBottomAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.ll0y0;
        float aoF = 1.0F - aoLevel;
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
        this.llxyz = AoHelper.fixAoLight(min, max, this.llxyz, aoB, aoF);
        this.llxyZ = AoHelper.fixAoLight(min, max, this.llxyZ, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.ll0yz = AoHelper.fixAoLight(min, max, this.ll0yz, aoB, aoF);
        this.llxy0 = AoHelper.fixAoLight(min, max, this.llxy0, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    
    }

    private boolean renderTopSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor
    ) {
        this.ll0Y0 = block.getBrightness(this.level, x, y + 1, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll0Y0;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++y;
            this.llxY0 = block.getBrightness(this.level, x - 1, y, z);
            this.llXY0 = block.getBrightness(this.level, x + 1, y, z);
            this.ll0Yz = block.getBrightness(this.level, x, y, z - 1);
            this.ll0YZ = block.getBrightness(this.level, x, y, z + 1);

            boolean t0 = this.field_69;
            boolean t1 = this.field_72;
            this.llxYz = !t0 && !this.field_71 ? this.llxY0 : block.getBrightness(this.level, x - 1, y, z - 1);
            this.llXYz = !t0 && !this.field_70 ? this.llXY0 : block.getBrightness(this.level, x + 1, y, z - 1);
            this.llxYZ = !t1 && !this.field_71 ? this.llxY0 : block.getBrightness(this.level, x - 1, y, z + 1);
            this.llXYZ = !t1 && !this.field_70 ? this.llXY0 : block.getBrightness(this.level, x + 1, y, z + 1);

            --y;
            if (aoLevel > 0.0f) {
                this.fixTopAo(aoLevel);
            }

            l4 = (this.llxYZ + this.llxY0 + this.ll0YZ + this.ll0Y0) * (1 / 4F);
            l1 = (this.ll0YZ + this.ll0Y0 + this.llXYZ + this.llXY0) * (1 / 4F);
            l2 = (this.ll0Y0 + this.ll0Yz + this.llXY0 + this.llXYz) * (1 / 4F);
            l3 = (this.llxY0 + this.llxYz + this.ll0Y0 + this.ll0Yz) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 1.0F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.UP);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderFaceUp(block, x, y, z, texture);
        return true;
    
    }

    private void fixTopAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.ll0Y0;
        float aoF = 1.0F - aoLevel;
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.llxY0 = AoHelper.fixAoLight(min, max, this.llxY0, aoB, aoF);
        this.ll0YZ = AoHelper.fixAoLight(min, max, this.ll0YZ, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llXY0 = AoHelper.fixAoLight(min, max, this.llXY0, aoB, aoF);
        this.ll0Yz = AoHelper.fixAoLight(min, max, this.ll0Yz, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
    
    }

    private boolean renderEastSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.ll00z = block.getBrightness(this.level, x, y, z - 1);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll00z;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --z;
            this.llx0z = block.getBrightness(this.level, x - 1, y, z);
            this.llxy0 = block.getBrightness(this.level, x, y - 1, z);
            this.ll0Yz = block.getBrightness(this.level, x, y + 1, z);
            this.llX0z = block.getBrightness(this.level, x + 1, y, z);

            boolean t0 = this.field_73;
            boolean t1 = this.field_76;
            this.ll0yZ = !t0 && !this.field_77 ? this.llx0z : block.getBrightness(this.level, x - 1, y - 1, z);
            this.llxYz = !t0 && !this.field_69 ? this.llx0z : block.getBrightness(this.level, x - 1, y + 1, z);
            this.llXy0 = !t1 && !this.field_77 ? this.llX0z : block.getBrightness(this.level, x + 1, y - 1, z);
            this.llXYz = !t1 && !this.field_69 ? this.llX0z : block.getBrightness(this.level, x + 1, y + 1, z);

            ++z;
            if (aoLevel > 0.0f) {
                this.fixEastAo(aoLevel);
            }

            l1 = (this.llx0z + this.llxYz + this.ll00z + this.ll0Yz) * (1 / 4F);
            l2 = (this.ll00z + this.ll0Yz + this.llX0z + this.llXYz) * (1 / 4F);
            l3 = (this.llxy0 + this.ll00z + this.llXy0 + this.llX0z) * (1 / 4F);
            l4 = (this.ll0yZ + this.llx0z + this.llxy0 + this.ll00z) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.8F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.NORTH);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderNorth(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderNorth(block, x, y, z, 38);
        }

        return true;
    
    }

    private void fixEastAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.ll00z;
        float aoF = 1.0F - aoLevel;
        this.llx0z = AoHelper.fixAoLight(min, max, this.llx0z, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
        this.ll0Yz = AoHelper.fixAoLight(min, max, this.ll0Yz, aoB, aoF);
        this.llX0z = AoHelper.fixAoLight(min, max, this.llX0z, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llxy0 = AoHelper.fixAoLight(min, max, this.llxy0, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    
    }

    private boolean renderWestSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.ll00Z = block.getBrightness(this.level, x, y, z + 1);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.ll00Z;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++z;
            this.llx0Z = block.getBrightness(this.level, x - 1, y, z);
            this.llX0Z = block.getBrightness(this.level, x + 1, y, z);
            this.llxyZ = block.getBrightness(this.level, x, y - 1, z);
            this.ll0YZ = block.getBrightness(this.level, x, y + 1, z);

            boolean t0 = this.field_75;
            boolean t1 = this.field_74;
            this.llXyz = !t0 && !this.field_80 ? this.llx0Z : block.getBrightness(this.level, x - 1, y - 1, z);
            this.llxYZ = !t0 && !this.field_72 ? this.llx0Z : block.getBrightness(this.level, x - 1, y + 1, z);
            this.llXyZ = !t1 && !this.field_80 ? this.llX0Z : block.getBrightness(this.level, x + 1, y - 1, z);
            this.llXYZ = !t1 && !this.field_72 ? this.llX0Z : block.getBrightness(this.level, x + 1, y + 1, z);

            --z;
            if (aoLevel > 0.0f) {
                this.fixWestAo(aoLevel);
            }

            l1 = (this.llx0Z + this.llxYZ + this.ll00Z + this.ll0YZ) * (1 / 4F);
            l4 = (this.ll00Z + this.ll0YZ + this.llX0Z + this.llXYZ) * (1 / 4F);
            l3 = (this.llxyZ + this.ll00Z + this.llXyZ + this.llX0Z) * (1 / 4F);
            l2 = (this.llXyz + this.llx0Z + this.llxyZ + this.ll00Z) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.8F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.SOUTH);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderSouth(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderSouth(block, x, y, z, 38);
        }

        return true;
    
    }

    private void fixWestAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.ll00Z;
        float aoF = 1.0F - aoLevel;
        this.llx0Z = AoHelper.fixAoLight(min, max, this.llx0Z, aoB, aoF);
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.ll0YZ = AoHelper.fixAoLight(min, max, this.ll0YZ, aoB, aoF);
        this.llX0Z = AoHelper.fixAoLight(min, max, this.llX0Z, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llxyZ = AoHelper.fixAoLight(min, max, this.llxyZ, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
    
    }

    private boolean renderNorthSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.llx00 = block.getBrightness(this.level, x - 1, y, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.llx00;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            --x;
            this.llxyz = block.getBrightness(this.level, x, y - 1, z);
            this.llx0z = block.getBrightness(this.level, x, y, z - 1);
            this.llx0Z = block.getBrightness(this.level, x, y, z + 1);
            this.llxY0 = block.getBrightness(this.level, x, y + 1, z);

            boolean t0 = this.field_73;
            boolean t1 = this.field_75;
            this.ll0yZ = !t0 && !this.field_79 ? this.llx0z : block.getBrightness(this.level, x, y - 1, z - 1);
            this.llXyz = !t1 && !this.field_79 ? this.llx0Z : block.getBrightness(this.level, x, y - 1, z + 1);
            this.llxYz = !t0 && !this.field_71 ? this.llx0z : block.getBrightness(this.level, x, y + 1, z - 1);
            this.llxYZ = !t1 && !this.field_71 ? this.llx0Z : block.getBrightness(this.level, x, y + 1, z + 1);

            ++x;
            if (aoLevel > 0.0f) {
                this.fixNorthAo(aoLevel);
            }

            l4 = (this.llxyz + this.llXyz + this.llx00 + this.llx0Z) * (1 / 4F);
            l1 = (this.llx00 + this.llx0Z + this.llxY0 + this.llxYZ) * (1 / 4F);
            l2 = (this.llx0z + this.llx00 + this.llxYz + this.llxY0) * (1 / 4F);
            l3 = (this.ll0yZ + this.llxyz + this.llx0z + this.llx00) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.6F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.WEST);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderWest(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderWest(block, x, y, z, 38);
        }

        return true;
    
    }

    private void fixNorthAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.llx00;
        float aoF = 1.0F - aoLevel;
        this.llxyz = AoHelper.fixAoLight(min, max, this.llxyz, aoB, aoF);
        this.llXyz = AoHelper.fixAoLight(min, max, this.llXyz, aoB, aoF);
        this.llx0Z = AoHelper.fixAoLight(min, max, this.llx0Z, aoB, aoF);
        this.llxY0 = AoHelper.fixAoLight(min, max, this.llxY0, aoB, aoF);
        this.llxYZ = AoHelper.fixAoLight(min, max, this.llxYZ, aoB, aoF);
        this.llx0z = AoHelper.fixAoLight(min, max, this.llx0z, aoB, aoF);
        this.llxYz = AoHelper.fixAoLight(min, max, this.llxYz, aoB, aoF);
        this.ll0yZ = AoHelper.fixAoLight(min, max, this.ll0yZ, aoB, aoF);
    
    }

    private boolean renderSouthSide(
        Tile block,
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float aoLevel,
        boolean useColor,
        boolean doGrassEdges
    ) {
        this.llX00 = block.getBrightness(this.level, x + 1, y, z);

        float l1;
        float l2;
        float l3;
        float l4;
        if (this.blsmooth <= 0) {
            l4 = this.llX00;
            l3 = l4;
            l2 = l4;
            l1 = l4;
        }
        else {
            ++x;
            this.ll0yz = block.getBrightness(this.level, x, y - 1, z);
            this.llX0z = block.getBrightness(this.level, x, y, z - 1);
            this.llX0Z = block.getBrightness(this.level, x, y, z + 1);
            this.llXY0 = block.getBrightness(this.level, x, y + 1, z);

            boolean t0 = this.field_78;
            boolean t1 = this.field_70;
            this.llXy0 = !t0 && !this.field_76 ? this.llX0z : block.getBrightness(this.level, x, y - 1, z - 1);
            this.llXyZ = !t0 && !this.field_74 ? this.llX0Z : block.getBrightness(this.level, x, y - 1, z + 1);
            this.llXYz = !t1 && !this.field_76 ? this.llX0z : block.getBrightness(this.level, x, y + 1, z - 1);
            this.llXYZ = !t1 && !this.field_74 ? this.llX0Z : block.getBrightness(this.level, x, y + 1, z + 1);

            --x;
            if (aoLevel > 0.0f) {
                this.fixSouthAo(aoLevel);
            }

            l1 = (this.ll0yz + this.llXyZ + this.llX00 + this.llX0Z) * (1 / 4F);
            l4 = (this.llX00 + this.llX0Z + this.llXY0 + this.llXYZ) * (1 / 4F);
            l3 = (this.llX0z + this.llX00 + this.llXYz + this.llXY0) * (1 / 4F);
            l2 = (this.llXy0 + this.ll0yz + this.llX0z + this.llX00) * (1 / 4F);
        }

        this.resetColor(useColor, r, g, b, 0.6F);
        this.applyColorBrightness(l1, l2, l3, l4);

        long textureKey = ((AC_TexturedBlock) block).getTextureForSideEx(this.level, x, y, z, Facing.EAST);
        if (AC_TexturedBlock.hasBiomeBit(textureKey)) {
            this.multiplyColor(r, g, b);
        }

        int texture = AC_TexturedBlock.toTexture(textureKey);
        this.renderEast(block, x, y, z, texture);
        if (doGrassEdges && texture == 3 && this.fixedTexture < 0) {
            this.multiplyColor(r, g, b);
            this.renderEast(block, x, y, z, 38);
        }

        return true;
    
    }

    private void fixSouthAo(float aoLevel) {
        float min = this.aoLightMin;
        float max = this.aoLightMax;
        float aoB = this.llX00;
        float aoF = 1.0F - aoLevel;
        this.ll0yz = AoHelper.fixAoLight(min, max, this.ll0yz, aoB, aoF);
        this.llXyZ = AoHelper.fixAoLight(min, max, this.llXyZ, aoB, aoF);
        this.llX0Z = AoHelper.fixAoLight(min, max, this.llX0Z, aoB, aoF);
        this.llXY0 = AoHelper.fixAoLight(min, max, this.llXY0, aoB, aoF);
        this.llXYZ = AoHelper.fixAoLight(min, max, this.llXYZ, aoB, aoF);
        this.llX0z = AoHelper.fixAoLight(min, max, this.llX0z, aoB, aoF);
        this.llXYz = AoHelper.fixAoLight(min, max, this.llXYz, aoB, aoF);
        this.llXy0 = AoHelper.fixAoLight(min, max, this.llXy0, aoB, aoF);
    
    }

    private boolean ac$tesselateInWorld(Tile block, int x, int y, int z) {
        this.materialTint = this.foliageColor(block, x, y, z);
        this.materialEmission = RayLighting.vertexEmission(this.level, block, x, y, z);
        int manifestFlags = MapMaterials.flags(this.level, block, x, y, z);
        this.materialFlags = block.material == Material.LEAVES || block.material == Material.PLANT ? VertexFormat.THIN_FOLIAGE : 0;
        if ((manifestFlags & MaterialProfile.FLAG_THIN) != 0) this.materialFlags |= VertexFormat.THIN_FOLIAGE;
        if ((manifestFlags & MaterialProfile.FLAG_DECORATIVE) != 0) this.materialFlags |= VertexFormat.DECORATIVE_GRASS;
        this.materialX = x;
        this.materialY = y;
        this.materialZ = z;
        this.terrainMaterial = true;
        this.writer.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(this.materialTint, this.materialEmission), this.materialFlags));
        this.writer.ac$skyLight(this.skyLightAroundBlock(x, y, z));
        int surfaceFlags = block.material == Material.WATER || (manifestFlags & MaterialProfile.FLAG_WATER) != 0
            ? VertexFormat.WATER : 0;
        if ((manifestFlags & MaterialProfile.FLAG_TRANSMISSIVE) != 0) {
            surfaceFlags |= VertexFormat.TRANSMISSIVE;
        }
        if (this.level instanceof LevelRegion region && region.rainExposed(x, y, z)) {
            surfaceFlags |= VertexFormat.RAIN_EXPOSED;
        }
        this.writer.ac$surfaceFlags(surfaceFlags);
        try {
            return this.tesselateMaterialInWorld(block, x, y, z);
        } finally {
            this.terrainMaterial = false;
            this.writer.ac$material(0);
            this.writer.ac$skyLight(-1);
            this.writer.ac$surfaceFlags(0);
        }
    }

    private int skyLightAroundBlock(int x, int y, int z) {
        int sky = this.rawSkyLight(x, y, z);
        sky = Math.max(sky, this.rawSkyLight(x - 1, y, z));
        sky = Math.max(sky, this.rawSkyLight(x + 1, y, z));
        sky = Math.max(sky, this.rawSkyLight(x, y - 1, z));
        sky = Math.max(sky, this.rawSkyLight(x, y + 1, z));
        sky = Math.max(sky, this.rawSkyLight(x, y, z - 1));
        return Math.max(sky, this.rawSkyLight(x, y, z + 1));
    }

    private int rawSkyLight(int x, int y, int z) {
        if (this.level instanceof LevelRegion region) return region.skyLight(x, y, z);
        if (this.level instanceof Level world) return world.getBrightness(LightLayer.SKY, x, y, z);
        return 0;
    }

    private void materialForFace(Tile tile, int face, int texture) {
        if (!this.terrainMaterial) return;
        int tint = this.materialTint;
        if (this.fixedTexture >= 0) {
            tint = 0xFFFFFF;
        } else if (tile.id == Tile.GRASS.id && texture != 38) {
            long key = ((AC_TexturedBlock) tile).getTextureForSideEx(
                this.level, this.materialX, this.materialY, this.materialZ, face);
            if (!AC_TexturedBlock.hasBiomeBit(key)) tint = 0xFFFFFF;
        }
        this.writer.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(tint, this.materialEmission), this.materialFlags));
    }

    private int foliageColor(Tile tile, int x, int y, int z) {
        return this.level instanceof LevelRegion region ? region.foliageColor(x, y, z)
            : tile.getFoliageColor(this.level, x, y, z);
    }

    private Tile tileAt(int x, int y, int z) {
        int id = this.level.getTile(x, y, z);
        return this.level instanceof LevelRegion region ? region.tile(id) : Tile.tiles[id];
    }

    private int renderShape(Tile tile, int x, int y, int z) {
        return this.level instanceof LevelRegion region ? region.renderShape(x, y, z)
            : ((ExBlock) tile).getRenderShape(this.level, x, y, z);
    }

    private boolean tesselateMaterialInWorld(Tile block, int x, int y, int z) {
        int renderShape = this.renderShape(block, x, y, z);
        if (renderShape == BlockShapes.NONE) {
            return false;
        }
        if (block.material == Material.LEAVES && renderShape == BlockShapes.BLOCK && block.isCubeShaped()) {
            this.materialFlags |= VertexFormat.LEAF_VOLUME;
            this.writer.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(this.materialTint, this.materialEmission), this.materialFlags));
        }
        if (renderShape == BlockShapes.REEDS || renderShape == BlockShapes.CROP || renderShape == AC_BlockShapes.BLOCK_TREE) {
            this.materialFlags |= VertexFormat.THIN_FOLIAGE;
            this.writer.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(this.materialTint, this.materialEmission), this.materialFlags));
        }

        // Use renderShape checks first; they are faster than method calls.
        if (renderShape == BlockShapes.BLOCK || renderShape == BlockShapes.LIQUID || block.isCubeShaped()) {
            // Anything that is cube-shaped should not change shape.
            // Liquids don't change shape; their height is calculated in tessellation.
        }
        else {
            // Clone tiles that change shape to avoid concurrency issues in Tile singletons.
            // TODO: thread-local cache?
            block = ((ExBlock) block).ac$clone();

            block.updateShape(this.level, x, y, z);
        }

        return switch (renderShape) {
            case BlockShapes.BLOCK, AC_BlockShapes.SNOW_LAYER -> this.tesselateBlockInWorld(block, x, y, z);
            case BlockShapes.LIQUID -> this.tesselateWaterInWorld(block, x, y, z);
            case BlockShapes.CACTUS -> this.tesselateCactusInWorld(block, x, y, z);
            case BlockShapes.REEDS -> this.tesselateCrossInWorld(block, x, y, z);
            case BlockShapes.CROP -> this.tesselateRowInWorld(block, x, y, z);
            case BlockShapes.TORCH -> this.tesselateTorchInWorld(block, x, y, z);
            case BlockShapes.FIRE -> this.tesselateFireInWorld(block, x, y, z);
            case BlockShapes.REDSTONE -> this.tesselateDustInWorld(block, x, y, z);
            case BlockShapes.LADDER -> this.tesselateLadderInWorld(block, x, y, z);
            case BlockShapes.DOOR -> this.tesselateDoorInWorld(block, x, y, z);
            case BlockShapes.RAILS -> this.tesselateRailInWorld(block, x, y, z);
            case BlockShapes.STAIRS -> this.tesselateStairsInWorld(block, x, y, z);
            case BlockShapes.FENCE -> this.tesselateFenceInWorld(block, x, y, z);
            case BlockShapes.LEVER -> this.tesselateLeverInWorld(block, x, y, z);
            case BlockShapes.BED -> this.tesselateBedInWorld(block, x, y, z);
            case BlockShapes.REPEATER -> this.tesselateRepeaterInWorld(block, x, y, z);
            case BlockShapes.PISTON -> this.tesselatePistonInWorld(block, x, y, z, false);
            case BlockShapes.PISTON_HEAD -> this.tesselateHeadPistonInWorld(block, x, y, z, true);
            case AC_BlockShapes.GRASS_3D -> this.tesselateGrassOnBlock(block, x, y, z);
            case AC_BlockShapes.REDSTONE_POWER -> this.tesselateRedstonePower(block, x, y, z);
            case AC_BlockShapes.SPIKES -> this.renderSpikes(block, x, y, z);
            case AC_BlockShapes.TABLE -> this.renderTable(block, x, y, z);
            case AC_BlockShapes.CHAIR -> this.renderChair(block, x, y, z);
            case AC_BlockShapes.ROPE -> this.renderRope(block, x, y, z);
            case AC_BlockShapes.BLOCK_TREE -> this.renderBlockTree(block, x, y, z);
            case AC_BlockShapes.BLOCK_OVERLAY -> this.renderBlockOverlay(block, x, y, z);
            case AC_BlockShapes.BLOCK_SLOPE -> this.renderBlockSlope(block, x, y, z);
            default -> false;
        };
    
    }

    private boolean tesselateGrassOnBlock(Tile block, int x, int y, int z) {
        if (this.level != null && this.fixedTexture == -1) {
            int topId = this.level.getTile(x, y + 1, z);
            if (topId == 0 ||
                this.renderShape(this.tileAt(x, y + 1, z), x, y + 1, z) == BlockShapes.NONE) {
                this.renderGrass(block, x, y, z);
            }
        }
        return this.tesselateBlockInWorld(block, x, y, z);
    
    }

    private boolean tesselateRedstonePower(Tile block, int x, int y, int z) {
        boolean hasFaces = this.tesselateBlockInWorld(block, x, y, z);
        // TODO: Change torch tile data on activation instead to rerender,
        //       which will get rid of this hacky level access.
        if (this.options.activation().active(x, y, z)) {
            this.tesselator().color(1.0F, 1.0F, 1.0F);
            this.fixedTexture = 99;
        }
        else {
            this.fixedTexture = 115;
        }
        this.tesselateTorch(block, x, y + 0.25D, z, 0.0D, 0.0D);
        this.fixedTexture = -1;
        return hasFaces;
    
    }

    private static double fixupCrossFenceZ(double z) {
        return (z - 0.5F) * 0.707F + 0.5F;
    
    }

    private void tesselateFenceFrontLeft(Tile block, int x, int y, int z, double maxZ, double minZ) {
        double minY = 6.0 / 16.0;
        double maxY = 9.0 / 16.0;
        minZ = fixupCrossFenceZ(minZ);
        maxZ = fixupCrossFenceZ(maxZ);

        VertexSink ts = this.tesselator();
        var exTs = (VertexSink) ts;
        int texture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (texture & 15) << 4;
        double texY = texture & 240;

        double u0 = texX / 256.0D;
        double u1 = (texX + 16.0D - 0.01D) / 256.0D;
        double v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        double v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;

        float b0 = this.level.getBrightness(x, y, z);
        float b1 = this.level.getBrightness(x - 1, y, z + 1);

        double x1 = x - 1.0D;
        double x2 = minZ + x;
        double x3 = maxZ + x;
        double x4 = minZ + x1;
        double x5 = maxZ + x1;

        double y0 = minY + y;
        double y1 = maxY + y;

        double z1 = z + 1.0D;
        double z2 = minZ + z;
        double z3 = maxZ + z;
        double z4 = minZ + z1;
        double z5 = maxZ + z1;

        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z3, u0, v1);
        ts.vertexUV(x3, y1, z3, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z5, u1, v0);
        ts.vertexUV(x5, y0, z5, u1, v1);
        ts.vertexUV(x4, y0, z4, u1, v1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z2, u0, v0);
        ts.vertexUV(x2, y0, z2, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY + 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z5, u1, v0);
        ts.vertexUV(x4, y0, z4, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z2, u0, v1);
        ts.vertexUV(x3, y0, z3, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        ts.vertexUV(x5, y1, z5, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z3, u0, v1);
        ts.vertexUV(x2, y1, z2, u0, v0);

        minY = 12.0 / 16.0;
        maxY = 15.0 / 16.0;
        y0 = minY + y;
        y1 = maxY + y;

        v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z3, u0, v1);
        ts.vertexUV(x3, y1, z3, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z5, u1, v0);
        ts.vertexUV(x5, y0, z5, u1, v1);
        ts.vertexUV(x4, y0, z4, u1, v1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z2, u0, v0);
        ts.vertexUV(x2, y0, z2, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY - 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z5, u1, v0);
        ts.vertexUV(x4, y0, z4, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z2, u0, v1);
        ts.vertexUV(x3, y0, z3, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z4, u1, v0);
        ts.vertexUV(x5, y1, z5, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z3, u0, v1);
        ts.vertexUV(x2, y1, z2, u0, v0);
    
    }

    private void tesselateFenceFrontRight(Tile block, int x, int y, int z, double maxZ, double minZ) {
        double minY = 6.0 / 16.0;
        double maxY = 9.0 / 16.0;
        minZ = fixupCrossFenceZ(minZ);
        maxZ = fixupCrossFenceZ(maxZ);

        VertexSink ts = this.tesselator();
        var exTs = (VertexSink) ts;
        int texture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0D;
        double u1 = (texX + 16.0D - 0.01D) / 256.0D;
        double v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        double v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;

        float b0 = this.level.getBrightness(x, y, z);
        float b1 = this.level.getBrightness(x - 1, y, z + 1);

        double x1 = x + 1.0D;
        double x2 = minZ + x;
        double x3 = maxZ + x;
        double x4 = minZ + x1;
        double x5 = maxZ + x1;

        double y0 = minY + y;
        double y1 = maxY + y;

        double z1 = z + 1.0D;
        double z2 = minZ + z;
        double z3 = maxZ + z;
        double z4 = minZ + z1;
        double z5 = maxZ + z1;

        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z2, u0, v1);
        ts.vertexUV(x3, y1, z2, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z4, u1, v0);
        ts.vertexUV(x5, y0, z4, u1, v1);
        ts.vertexUV(x4, y0, z5, u1, v1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z3, u0, v0);
        ts.vertexUV(x2, y0, z3, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY + 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z4, u1, v0);
        ts.vertexUV(x4, y0, z5, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z3, u0, v1);
        ts.vertexUV(x3, y0, z2, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        ts.vertexUV(x5, y1, z4, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z2, u0, v1);
        ts.vertexUV(x2, y1, z3, u0, v0);

        minY = 12.0 / 16.0;
        maxY = 15.0 / 16.0;
        y0 = minY + y;
        y1 = maxY + y;

        v0 = (texY + 16.0D * maxY - 1.0D) / 256.0D;
        v1 = (texY + 16.0D * minY - 1.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x3, y0, z2, u0, v1);
        ts.vertexUV(x3, y1, z2, u0, v0);
        exTs.ac$splatColor32(b1 * 0.7F);
        ts.vertexUV(x5, y1, z4, u1, v0);
        ts.vertexUV(x5, y0, z4, u1, v1);
        ts.vertexUV(x4, y0, z5, u1, v1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        exTs.ac$splatColor32(b0 * 0.7F);
        ts.vertexUV(x2, y1, z3, u0, v0);
        ts.vertexUV(x2, y0, z3, u0, v1);

        v0 = (texY + 16.0D * maxY) / 256.0D;
        v1 = (texY + 16.0D * maxY - 2.0D - 0.01D) / 256.0D;
        exTs.ac$splatColor32(b1 * 0.5F);
        ts.vertexUV(x5, y0, z4, u1, v0);
        ts.vertexUV(x4, y0, z5, u1, v1);
        exTs.ac$splatColor32(b0 * 0.5F);
        ts.vertexUV(x2, y0, z3, u0, v1);
        ts.vertexUV(x3, y0, z2, u0, v0);
        exTs.ac$splatColor32(b1);
        ts.vertexUV(x4, y1, z5, u1, v0);
        ts.vertexUV(x5, y1, z4, u1, v1);
        exTs.ac$splatColor32(b0);
        ts.vertexUV(x3, y1, z2, u0, v1);
        ts.vertexUV(x2, y1, z3, u0, v0);
    
    }

    private void tesselateStairs0(Tile block, int x, int y, int z) {
        Tile leftBlock = this.tileAt(x - 1, y, z);
        if (leftBlock != null && this.renderShape(leftBlock, x - 1, y, z) == BlockShapes.STAIRS) {
            int leftMeta = this.level.getData(x - 1, y, z) & 3;
            if (leftMeta == 2) {
                block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (leftMeta == 3) {
                block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }

        int rightMeta = this.level.getData(x + 1, y, z) & 3;
        Tile rightBlock = this.tileAt(x + 1, y, z);
        if (rightBlock != null && this.renderShape(rightBlock, x + 1, y, z) == BlockShapes.STAIRS &&
            (rightMeta == 2 || rightMeta == 3)) {
            if (rightMeta == 2) {
                block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (rightMeta == 3) {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }
        else {
            block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 1.0F);
            this.tesselateBlockInWorld(block, x, y, z);
        }
    
    }

    private void tesselateStairs1(Tile block, int x, int y, int z) {
        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        Tile leftBlock = this.tileAt(x - 1, y, z);
        if (leftBlock != null && this.renderShape(leftBlock, x - 1, y, z) == BlockShapes.STAIRS && (leftMeta == 2 || leftMeta == 3)) {
            if (leftMeta == 3) {
                block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else {
                block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }
        else {
            block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 1.0F);
            this.tesselateBlockInWorld(block, x, y, z);
        }

        Tile rightBlock = this.tileAt(x + 1, y, z);
        if (rightBlock != null && this.renderShape(rightBlock, x + 1, y, z) == BlockShapes.STAIRS) {
            int rightMeta = this.level.getData(x + 1, y, z) & 3;
            if (rightMeta == 2) {
                block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (rightMeta == 3) {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }
    
    }

    private void tesselateStairs2(Tile block, int x, int y, int z) {
        Tile backBlock = this.tileAt(x, y, z - 1);
        if (backBlock != null && this.renderShape(backBlock, x, y, z - 1) == BlockShapes.STAIRS) {
            int backMeta = this.level.getData(x, y, z - 1) & 3;
            if (backMeta == 1) {
                block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (backMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = this.tileAt(x, y, z + 1);
        if (frontBlock != null && this.renderShape(frontBlock, x, y, z + 1) == BlockShapes.STAIRS &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else {
                block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }
        else {
            block.setShape(0.0F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
            this.tesselateBlockInWorld(block, x, y, z);
        }
    
    }

    private void tesselateStairs3(Tile block, int x, int y, int z) {
        Tile frontBlock = this.tileAt(x, y, z + 1);
        if (frontBlock != null && this.renderShape(frontBlock, x, y, z + 1) == BlockShapes.STAIRS) {
            int frontMeta = this.level.getData(x, y, z + 1) & 3;
            if (frontMeta == 1) {
                block.setShape(0.0F, 0.5F, 0.5F, 0.5F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else if (frontMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.5F, 1.0F, 1.0F, 1.0F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = this.tileAt(x, y, z - 1);
        if (backBlock != null && this.renderShape(backBlock, x, y, z - 1) == BlockShapes.STAIRS && (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 0) {
                block.setShape(0.5F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
            else {
                block.setShape(0.0F, 0.5F, 0.0F, 0.5F, 1.0F, 0.5F);
                this.tesselateBlockInWorld(block, x, y, z);
            }
        }
        else {
            block.setShape(0.0F, 0.5F, 0.0F, 1.0F, 1.0F, 0.5F);
            this.tesselateBlockInWorld(block, x, y, z);
        }
    
    }

    private void renderInventoryBox(
        Tile tile,
        int meta,
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
    ) {
        tile.setShape(minX, minY, minZ, maxX, maxY, maxZ);
        VertexSink ts = this.tesselator();
        double o = -0.5D;
        ts.normal(0.0F, -1.0F, 0.0F);
        this.renderFaceDown(tile, o, o, o, tile.getTexture(Facing.DOWN, meta));
        ts.normal(0.0F, 1.0F, 0.0F);
        this.renderFaceUp(tile, o, o, o, tile.getTexture(Facing.UP, meta));
        ts.normal(0.0F, 0.0F, -1.0F);
        this.renderNorth(tile, o, o, o, tile.getTexture(Facing.NORTH, meta));
        ts.normal(0.0F, 0.0F, 1.0F);
        this.renderSouth(tile, o, o, o, tile.getTexture(Facing.SOUTH, meta));
        ts.normal(-1.0F, 0.0F, 0.0F);
        this.renderWest(tile, o, o, o, tile.getTexture(Facing.WEST, meta));
        ts.normal(1.0F, 0.0F, 0.0F);
        this.renderEast(tile, o, o, o, tile.getTexture(Facing.EAST, meta));
    
    }

    private void renderInventorySlope(Tile tile, int meta) {
        VertexSink ts = this.tesselator();
        int texture = tile.getTexture(Facing.DOWN, meta);
        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0D;
        double u1 = (texX + 15.99D) / 256.0D;
        double v0 = texY / 256.0D;
        double v1 = (texY + 15.99D) / 256.0D;
        double a = -0.5D;
        double b = 0.5D;
        ts.normal(0.0F, -1.0F, 0.0F);
        ts.vertexUV(a, a, a, u0, v0);
        ts.vertexUV(b, a, a, u1, v0);
        ts.vertexUV(b, a, b, u1, v1);
        ts.vertexUV(a, a, b, u0, v1);

        switch (meta & 3) {
            case 0 -> {
                this.renderInventorySlopeNorthSouth(ts, a, b, u0, u1, v0, v1, false);
            }
            case 1 -> {
                this.renderInventorySlopeNorthSouth(ts, a, b, u0, u1, v0, v1, true);
            }
            case 2 -> {
                this.renderInventorySlopeEastWest(ts, a, b, u0, u1, v0, v1, false);
            }
            default -> {
                this.renderInventorySlopeEastWest(ts, a, b, u0, u1, v0, v1, true);
            }
        }
    
    }

    private void renderInventorySlopeNorthSouth(
        VertexSink ts,
        double a,
        double b,
        double u0,
        double u1,
        double v0,
        double v1,
        boolean reverse
    ) {
        double lowZ = reverse ? b : a;
        double highZ = reverse ? a : b;
        ts.normal(0.0F, 0.7071F, reverse ? 0.7071F : -0.7071F);
        ts.vertexUV(a, a, lowZ, u0, v1);
        ts.vertexUV(b, a, lowZ, u1, v1);
        ts.vertexUV(b, b, highZ, u1, v0);
        ts.vertexUV(a, b, highZ, u0, v0);
        ts.normal(0.0F, 0.0F, reverse ? -1.0F : 1.0F);
        ts.vertexUV(a, a, highZ, u0, v1);
        ts.vertexUV(a, b, highZ, u0, v0);
        ts.vertexUV(b, b, highZ, u1, v0);
        ts.vertexUV(b, a, highZ, u1, v1);
        ts.normal(-1.0F, 0.0F, 0.0F);
        ts.vertexUV(a, a, lowZ, u0, v1);
        ts.vertexUV(a, b, highZ, u1, v0);
        ts.vertexUV(a, a, highZ, u1, v1);
        ts.vertexUV(a, a, highZ, u1, v1);
        ts.normal(1.0F, 0.0F, 0.0F);
        ts.vertexUV(b, a, highZ, u1, v1);
        ts.vertexUV(b, b, highZ, u0, v0);
        ts.vertexUV(b, a, lowZ, u0, v1);
        ts.vertexUV(b, a, lowZ, u0, v1);
    
    }

    private void renderInventorySlopeEastWest(
        VertexSink ts,
        double a,
        double b,
        double u0,
        double u1,
        double v0,
        double v1,
        boolean reverse
    ) {
        double lowX = reverse ? b : a;
        double highX = reverse ? a : b;
        ts.normal(reverse ? 0.7071F : -0.7071F, 0.7071F, 0.0F);
        ts.vertexUV(lowX, a, a, u0, v1);
        ts.vertexUV(lowX, a, b, u1, v1);
        ts.vertexUV(highX, b, b, u1, v0);
        ts.vertexUV(highX, b, a, u0, v0);
        ts.normal(reverse ? -1.0F : 1.0F, 0.0F, 0.0F);
        ts.vertexUV(highX, a, a, u0, v1);
        ts.vertexUV(highX, b, a, u0, v0);
        ts.vertexUV(highX, b, b, u1, v0);
        ts.vertexUV(highX, a, b, u1, v1);
        ts.normal(0.0F, 0.0F, -1.0F);
        ts.vertexUV(lowX, a, a, u0, v1);
        ts.vertexUV(highX, b, a, u1, v0);
        ts.vertexUV(highX, a, a, u1, v1);
        ts.vertexUV(highX, a, a, u1, v1);
        ts.normal(0.0F, 0.0F, 1.0F);
        ts.vertexUV(highX, a, b, u1, v1);
        ts.vertexUV(highX, b, b, u0, v0);
        ts.vertexUV(lowX, a, b, u0, v1);
        ts.vertexUV(lowX, a, b, u0, v1);
    
    }

    public void renderCrossedSquaresUpsideDown(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y;
        double y1 = y + 1.0D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x1, y0, z0, u1, v0);
        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);
    
    }

    public void renderCrossedSquaresEast(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x;
        double x1 = x + 1.0D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z0, u1, v0);
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
    
    }

    public void renderCrossedSquaresWest(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x;
        double x1 = x + 1.0D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);
    
    }

    public void renderCrossedSquaresNorth(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z;
        double z1 = z + 1.0D;

        ts.vertexUV(x0, y0, z1, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y1, z0, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y1, z0, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y0, z1, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z1, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y1, z0, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y1, z0, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y0, z1, u1, v0);
    
    }

    public void renderCrossedSquaresSouth(Tile block, int meta, double x, double y, double z) {
        VertexSink ts = this.tesselator();
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 0.05D;
        double y1 = y + 0.95D;
        double z0 = z;
        double z1 = z + 1.0D;

        ts.vertexUV(x0, y0, z0, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y1, z1, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y1, z1, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y0, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x1, y0, z0, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y1, z1, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);
        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y1, z1, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y0, z0, u1, v0);
    
    }

    public boolean renderBlockSlope(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        int coreMeta = this.level.getData(x, y, z) & 3;
        int coreTexture = block.getTexture(this.level, x, y, z, Facing.DOWN);
        double texX = (coreTexture & 15) << 4;
        double texY = coreTexture & 240;
        double u0 = texX / 256.0D;
        double u1 = (texX + 15.99D) / 256.0D;
        double v0 = texY / 256.0D;
        double v1 = (texY + 15.99D) / 256.0D;

        block.setShape(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        float brightness = block.getBrightness(this.level, x, y, z);

        double x1 = x + 1;
        double z1 = z + 1;

        ts.color(0.5F * brightness, 0.5F * brightness, 0.5F * brightness);
        ts.vertexUV(x, y, z, u0, v0);
        ts.vertexUV(x1, y, z, u1, v0);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x, y, z1, u0, v1);

        if (coreMeta == 0) {
            this.renderBlockSlope0(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 1) {
            this.renderBlockSlope1(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 2) {
            this.renderBlockSlope2(x, y, z, brightness, u0, u1, v0, v1);
        }
        else if (coreMeta == 3) {
            this.renderBlockSlope3(x, y, z, brightness, u0, u1, v0, v1);
        }
        return true;
    
    }

    private void renderBlockSlope0(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        VertexSink ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        Tile leftBlock = this.tileAt(x - 1, y, z);
        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        if (leftBlock != null && this.renderShape(leftBlock, x - 1, y, z) == AC_BlockShapes.BLOCK_SLOPE &&
            (leftMeta == 2 || leftMeta == 3)) {
            if (leftMeta == 2) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else if (leftMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }

            ts.vertexUV(x, y, z, u0, v1);
            ts.vertexUV(x, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z, u0, v0);
            ts.color(0.6F * b, 0.6F * b, 0.6F * b);
            ts.vertexUV(x1, y, z, u0, v1);
            ts.vertexUV(x1, y1, z, u0, v0);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x1, y, z1, u1, v1);
            return;
        }

        int rightMeta = this.level.getData(x + 1, y, z) & 3;
        Tile rightBlock = this.tileAt(x + 1, y, z);
        if (rightBlock != null && this.renderShape(rightBlock, x + 1, y, z) == AC_BlockShapes.BLOCK_SLOPE &&
            (rightMeta == 2 || rightMeta == 3)) {
            if (rightMeta == 2) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }
            else if (rightMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            return;
        }

        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z, u0, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
    
    }

    private void renderBlockSlope1(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        VertexSink ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        Tile rightBlock = this.tileAt(x + 1, y, z);
        int rightMeta = this.level.getData(x + 1, y, z) & 3;
        if (rightBlock != null && this.renderShape(rightBlock, x + 1, y, z) == AC_BlockShapes.BLOCK_SLOPE &&
            (rightMeta == 2 || rightMeta == 3)) {
            if (rightMeta == 2) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y, z, u0, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }

            ts.vertexUV(x, y1, z, u1, v0);
            ts.vertexUV(x, y1, z1, u0, v0);
            ts.vertexUV(x1, y, z1, u0, v1);
            ts.vertexUV(x1, y, z, u1, v1);
            ts.color(0.6F * b, 0.6F * b, 0.6F * b);
            ts.vertexUV(x, y, z, u0, v1);
            ts.vertexUV(x, y, z1, u1, v1);
            ts.vertexUV(x, y1, z1, u1, v0);
            ts.vertexUV(x, y1, z, u0, v0);
            return;
        }

        int leftMeta = this.level.getData(x - 1, y, z) & 3;
        Tile leftBlock = this.tileAt(x - 1, y, z);
        if (leftBlock != null && this.renderShape(leftBlock, x - 1, y, z) == AC_BlockShapes.BLOCK_SLOPE &&
            (leftMeta == 2 || leftMeta == 3)) {
            if (leftMeta == 3) {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.vertexUV(x, y, z, u1, v1);
            }
            else {
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
            }
            return;
        }

        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y1, z, u1, v0);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x, y1, z, u1, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.vertexUV(x, y, z, u1, v1);
    
    }

    private void renderBlockSlope2(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        VertexSink ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = this.tileAt(x, y, z - 1);
        if (backBlock != null && this.renderShape(backBlock, x, y, z - 1) == AC_BlockShapes.BLOCK_SLOPE &&
            (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 1) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else if (backMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }

            ts.color(0.8F * b, 0.8F * b, 0.8F * b);
            ts.vertexUV(x1, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z1, u1, v0);
            ts.vertexUV(x, y1, z1, u0, v0);
            ts.vertexUV(x, y, z1, u0, v1);
            ts.color(0.9F * b, 0.9F * b, 0.9F * b);
            ts.vertexUV(x, y1, z1, u1, v0);
            ts.vertexUV(x1, y1, z1, u0, v0);
            ts.vertexUV(x1, y, z, u0, v1);
            ts.vertexUV(x, y, z, u1, v1);
            return;
        }

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = this.tileAt(x, y, z + 1);
        if (frontBlock != null && this.renderShape(frontBlock, x, y, z + 1) == AC_BlockShapes.BLOCK_SLOPE &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }
            return;
        }

        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x, y1, z1, u0, v0);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.color(0.9F * b, 0.9F * b, 0.9F * b);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z, u0, v1);
        ts.vertexUV(x, y, z, u1, v1);
        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z1, u1, v0);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u0, v1);
    
    }

    private void renderBlockSlope3(int x, int y, int z, float b, double u0, double u1, double v0, double v1) {
        VertexSink ts = this.tesselator();
        double x1 = x + 1;
        double z1 = z + 1;
        double y1 = y + 1;

        int frontMeta = this.level.getData(x, y, z + 1) & 3;
        Tile frontBlock = this.tileAt(x, y, z + 1);
        if (frontBlock != null && this.renderShape(frontBlock, x, y, z + 1) == AC_BlockShapes.BLOCK_SLOPE &&
            (frontMeta == 0 || frontMeta == 1)) {
            if (frontMeta == 1) {
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z1, u1, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z1, u0, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else if (frontMeta == 0) {
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u0, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z1, u1, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }

            ts.color(0.8F * b, 0.8F * b, 0.8F * b);
            ts.vertexUV(x, y1, z, u0, v0);
            ts.vertexUV(x1, y1, z, u1, v0);
            ts.vertexUV(x1, y, z, u1, v1);
            ts.vertexUV(x, y, z, u0, v1);
            ts.color(0.9F * b, 0.9F * b, 0.9F * b);
            ts.vertexUV(x, y, z1, u0, v1);
            ts.vertexUV(x1, y, z1, u1, v1);
            ts.vertexUV(x1, y1, z, u1, v0);
            ts.vertexUV(x, y1, z, u0, v0);
            return;
        }

        int backMeta = this.level.getData(x, y, z - 1) & 3;
        Tile backBlock = this.tileAt(x, y, z - 1);
        if (backBlock != null && this.renderShape(backBlock, x, y, z - 1) == AC_BlockShapes.BLOCK_SLOPE &&
            (backMeta == 0 || backMeta == 1)) {
            if (backMeta == 0) {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.vertexUV(x1, y1, z, u0, v0);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.vertexUV(x1, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u0, v1);
            }
            else {
                ts.color(0.8F * b, 0.8F * b, 0.8F * b);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x, y1, z, u1, v0);
                ts.vertexUV(x1, y, z1, u0, v1);
                ts.vertexUV(x1, y, z, u1, v1);
                ts.color(0.9F * b, 0.9F * b, 0.9F * b);
                ts.vertexUV(x, y, z1, u0, v1);
                ts.vertexUV(x1, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.color(0.6F * b, 0.6F * b, 0.6F * b);
                ts.vertexUV(x, y, z1, u1, v1);
                ts.vertexUV(x, y1, z, u0, v0);
                ts.vertexUV(x, y, z, u0, v1);
                ts.vertexUV(x, y, z, u0, v1);
            }
            return;
        }

        ts.color(0.8F * b, 0.8F * b, 0.8F * b);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x, y, z, u0, v1);
        ts.color(0.9F * b, 0.9F * b, 0.9F * b);
        ts.vertexUV(x, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u1, v1);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.color(0.6F * b, 0.6F * b, 0.6F * b);
        ts.vertexUV(x1, y, z, u1, v1);
        ts.vertexUV(x1, y1, z, u1, v0);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x1, y, z1, u0, v1);
        ts.vertexUV(x, y, z1, u1, v1);
        ts.vertexUV(x, y1, z, u0, v0);
        ts.vertexUV(x, y, z, u0, v1);
        ts.vertexUV(x, y, z, u0, v1);
    
    }

    public boolean renderGrass(Tile block, int x, int y, int z) {
        int meta = this.level.getData(x, y, z);
        float grassMul = ((ExGrassBlock) Tile.GRASS).getGrassMultiplier(meta);
        if (grassMul < 0.0F) {
            return false;
        }

        VertexSink ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y + 1, z) * grassMul;
        int colorMul = this.foliageColor(block, x, y, z);
        float red = (float) (colorMul >> 16 & 255) / 255.0F;
        float green = (float) (colorMul >> 8 & 255) / 255.0F;
        float blue = (float) (colorMul & 255) / 255.0F;
        if (this.terrainMaterial) {
            int tint = (int) ((colorMul >> 16 & 255) * grassMul) << 16 |
                (int) ((colorMul >> 8 & 255) * grassMul) << 8 | (int) ((colorMul & 255) * grassMul);
            ts.ac$material(VertexFormat.withFlags(VertexFormat.packMaterial(tint, this.materialEmission),
                this.materialFlags | VertexFormat.THIN_FOLIAGE | VertexFormat.DECORATIVE_GRASS));
        }
        ts.color(brightness * red, brightness * green, brightness * blue);

        this.rand.setSeed(x * x * 3121L + x * 45238971L + z * z * 418711L + z * 13761L + y);

        int texId = 168;
        int texX = (texId & 15) << 4;
        int texY = texId & 240;
        texX += this.rand.nextInt(32);
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double x0 = x + 0.05D;
        double x1 = x + 0.95D;
        double y0 = y + 1.0D;
        double y1 = y + 2.0D;
        double z0 = z + 0.05D;
        double z1 = z + 0.95D;

        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        texX = (texId & 15) << 4;
        texY = texId & 240;
        texX += this.rand.nextInt(32);
        u0 = texX / 256.0;
        u1 = (texX + 15.99) / 256.0;
        v0 = texY / 256.0;
        v1 = (texY + 15.99) / 256.0;
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        return true;
    
    }

    public boolean renderSpikes(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        if (this.level.isSolidTile(x, y - 1, z)) {
            this.tesselateCrossTexture(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y + 1, z)) {
            this.renderCrossedSquaresUpsideDown(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x - 1, y, z)) {
            this.renderCrossedSquaresEast(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x + 1, y, z)) {
            this.renderCrossedSquaresWest(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y, z - 1)) {
            this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }
        else if (this.level.isSolidTile(x, y, z + 1)) {
            this.renderCrossedSquaresSouth(block, meta, x, y, z);
        }
        else {
            this.tesselateCrossTexture(block, meta, x, y, z);
        }
        return true;
    
    }

    public boolean renderTable(Tile block, int x, int y, int z) {
        boolean renderAny = this.tesselateBlockInWorld(block, x, y, z);
        boolean renderFront = this.level.getTile(x, y, z + 1) != AC_Blocks.tableBlocks.id;
        boolean renderBack = this.level.getTile(x, y, z - 1) != AC_Blocks.tableBlocks.id;
        boolean renderLeft = this.level.getTile(x - 1, y, z) != AC_Blocks.tableBlocks.id;
        boolean renderRight = this.level.getTile(x + 1, y, z) != AC_Blocks.tableBlocks.id;

        if (renderLeft && renderBack) {
            block.setShape(0.0F, 0.0F, 0.0F, 3.0F / 16.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderRight && renderBack) {
            block.setShape(13.0F / 16.0F, 0.0F, 0.0F, 1.0F, 14.0F / 16.0F, 3.0F / 16.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderRight && renderFront) {
            block.setShape(13.0F / 16.0F, 0.0F, 13.0F / 16.0F, 1.0F, 14.0F / 16.0F, 1.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        if (renderLeft && renderFront) {
            block.setShape(0.0F, 0.0F, 13.0F / 16.0F, 3.0F / 16.0F, 14.0F / 16.0F, 1.0F);
            renderAny |= this.tesselateBlockInWorld(block, x, y, z);
        }

        block.setShape(0.0F, 14.0F / 16.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        return renderAny;
    
    }

    public boolean renderChair(Tile block, int x, int y, int z) {
        boolean renderAny = this.tesselateBlockInWorld(block, x, y, z);
        int meta = this.level.getData(x, y, z) % 4;
        renderAny |= switch (meta) {
            case 0 -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 0.25F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            case 1 -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 0.25F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            case 2 -> {
                block.setShape(12.0F / 16.0F, 10.0F / 16.0F, 2.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
            default -> {
                block.setShape(2.0F / 16.0F, 10.0F / 16.0F, 12.0F / 16.0F, 14.0F / 16.0F, 1.25F, 14.0F / 16.0F);
                yield this.tesselateBlockInWorld(block, x, y, z);
            }
        };

        block.setShape(2.0F / 16.0F, 0.0F, 2.0F / 16.0F, 0.25F, 0.5F, 0.25F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(12.0F / 16.0F, 0.0F, 2.0F / 16.0F, 14.0F / 16.0F, 0.5F, 0.25F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(12.0F / 16.0F, 0.0F, 12.0F / 16.0F, 14.0F / 16.0F, 0.5F, 14.0F / 16.0F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        block.setShape(2.0F / 16.0F, 0.0F, 12.0F / 16.0F, 0.25F, 0.5F, 14.0F / 16.0F);
        renderAny |= this.tesselateBlockInWorld(block, x, y, z);

        // TODO: this doesn't look needed
        block.setShape(2.0F / 16.0F, 0.5F, 2.0F / 16.0F, 14.0F / 16.0F, 10.0F / 16.0F, 14.0F / 16.0F);

        return renderAny;
    
    }

    public boolean renderRope(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int ropeMeta = meta % 3;
        switch (ropeMeta) {
            case 0 -> this.tesselateCrossTexture(block, meta, x, y, z);
            case 1 -> this.renderCrossedSquaresEast(block, meta, x, y, z);
            default -> this.renderCrossedSquaresNorth(block, meta, x, y, z);
        }
        return true;
    
    }

    private float treeSize(int x, int y, int z) {
        if (this.level instanceof LevelRegion region) return region.treeSize(x, y, z);
        TileEntity entity = this.level.getTileEntity(x, y, z);
        return entity instanceof AC_TileEntityTree tree ? tree.size : 1.0F;
    }

    public boolean renderBlockTree(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(Facing.DOWN, meta);
        if (this.fixedTexture >= 0) {
            texture = this.fixedTexture;
        }

        double texX = (texture & 15) << 4;
        double texY = texture & 240;
        double u0 = texX / 256.0;
        double u1 = (texX + 15.99) / 256.0;
        double v0 = texY / 256.0;
        double v1 = (texY + 15.99) / 256.0;

        double size = this.treeSize(x, y, z);

        double x0 = x + 0.5D - 0.45 * size;
        double x1 = x + 0.5D + 0.45 * size;
        double y0 = y;
        double y1 = y + size;
        double z0 = z + 0.5D - 0.45 * size;
        double z1 = z + 0.5D + 0.45 * size;

        ts.vertexUV(x0, y1, z0, u0, v0);
        ts.vertexUV(x0, y0, z0, u0, v1);
        ts.vertexUV(x1, y0, z1, u1, v1);
        ts.vertexUV(x1, y1, z1, u1, v0);
        ts.vertexUV(x1, y1, z1, u0, v0);
        ts.vertexUV(x1, y0, z1, u0, v1);
        ts.vertexUV(x0, y0, z0, u1, v1);
        ts.vertexUV(x0, y1, z0, u1, v0);

        if (this.fixedTexture < 0) {
            texture = block.getTexture(Facing.UP, meta);
            texX = (texture & 15) << 4;
            texY = texture & 240;
            u0 = texX / 256.0;
            u1 = (texX + 15.99) / 256.0;
            v0 = texY / 256.0;
            v1 = (texY + 15.99) / 256.0;
        }
        ts.vertexUV(x0, y1, z1, u0, v0);
        ts.vertexUV(x0, y0, z1, u0, v1);
        ts.vertexUV(x1, y0, z0, u1, v1);
        ts.vertexUV(x1, y1, z0, u1, v0);
        ts.vertexUV(x1, y1, z0, u0, v0);
        ts.vertexUV(x1, y0, z0, u0, v1);
        ts.vertexUV(x0, y0, z1, u1, v1);
        ts.vertexUV(x0, y1, z1, u1, v0);
        return true;
    
    }

    public boolean renderBlockOverlay(Tile block, int x, int y, int z) {
        VertexSink ts = this.tesselator();
        float brightness = block.getBrightness(this.level, x, y, z);
        ts.color(brightness, brightness, brightness);

        int meta = this.level.getData(x, y, z);
        int texture = block.getTexture(Facing.DOWN, meta);

        if (block instanceof AC_BlockOverlay overlay) {
            overlay.updateBounds(this.level, x, y, z);
        }

        if (this.level.isSolidTile(x, y - 1, z)) {
            this.renderFaceUp(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y + 1, z)) {
            this.renderFaceDown(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x - 1, y, z)) {
            this.renderEast(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x + 1, y, z)) {
            this.renderWest(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y, z - 1)) {
            this.renderSouth(block, x, y, z, texture);
        }
        else if (this.level.isSolidTile(x, y, z + 1)) {
            this.renderNorth(block, x, y, z, texture);
        }
        else {
            this.renderFaceUp(block, x, y, z, texture);
        }
        return true;
    
    }
}
