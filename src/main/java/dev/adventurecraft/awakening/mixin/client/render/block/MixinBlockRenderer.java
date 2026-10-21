package dev.adventurecraft.awakening.mixin.client.render.block;

import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.client.render.compat.LegacyPassBridge;
import dev.adventurecraft.awakening.client.render.state.Color4;
import dev.adventurecraft.awakening.client.render.block.BlockMesher;
import dev.adventurecraft.awakening.client.render.block.BlockMeshing;
import dev.adventurecraft.awakening.extension.client.render.block.ExBlockRenderer;
import net.minecraft.client.renderer.Tesselator;
import net.minecraft.client.renderer.TileRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSource;
import net.minecraft.world.level.tile.Tile;
import net.minecraft.world.level.tile.RailTile;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** Compatibility entry points only. All block geometry recipes live in the CPU BlockMesher. */
@Mixin(value=TileRenderer.class,priority=999)
public abstract class MixinBlockRenderer implements ExBlockRenderer {
    @Shadow public LevelSource level;
    @Shadow private int fixedTexture;
    @Shadow private boolean noCulling;
    @Shadow public boolean field_81;
    @Unique private Tesselator tesselator;

    @SuppressWarnings("MixinAnnotationTarget")
    @Redirect(method="*",at=@At(value="FIELD",target="Lnet/minecraft/client/renderer/Tesselator;instance:Lnet/minecraft/client/renderer/Tesselator;",opcode=Opcodes.GETSTATIC))
    private Tesselator tesselator() { return this.tesselator!=null?this.tesselator:Tesselator.instance; }
    @Override public Tesselator ac$getTesselator() { return this.tesselator; }
    @Override public void ac$setTesselator(Tesselator tesselator) { this.tesselator=tesselator; }

    @Unique private BlockMesher ac$mesher(LevelSource source,double x,double y,double z) {
        var mesher=new BlockMesher(source,(VertexSink)this.tesselator(),x,y,z,BlockMeshing.options(source));
        mesher.fixedTexture(this.fixedTexture).unculled(this.noCulling); mesher.inventoryColors(this.field_81);
        return mesher;
    }
    @Override public void startRenderingBlocks(Level world) {
        this.tesselator().begin();
        this.level=world;
        this.noCulling=true;
    }
    @Override public void stopRenderingBlocks() {
        try { this.tesselator().end(); }
        finally { this.noCulling=false;this.level=null; }
    }

    @Overwrite public boolean tesselateInWorld(Tile tile,int x,int y,int z) { return this.ac$mesher(this.level,0,0,0).world(tile,x,y,z); }
    @Overwrite public boolean tesselateBlockInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateBlockInWorld(tile,x,y,z);
    }
    @Overwrite public void renderBlock(Tile tile,Level world,int x,int y,int z) {
        var vertices=this.tesselator();vertices.begin();
        try { this.ac$mesher(world,x+.5,y+.5,z+.5).worldUnculled(tile,x,y,z); }
        finally { vertices.end(); }
    }
    @Overwrite public void renderTile(Tile tile,int metadata,float brightness) {
        var parameters=LegacyPassBridge.capture(false);
        parameters=parameters.withObject(parameters.object().withTint(Color4.WHITE));
        LegacyPassBridge.drawLegacy(parameters,LegacyPassBridge.currentTexture(),() -> {
            var vertices=this.tesselator();vertices.begin();
            try { this.ac$mesher(null,0,0,0).inventory(tile,metadata,brightness); }
            finally { vertices.end(); }
        });
    }
    @Overwrite public void tesselateInWorld(Tile tile,int x,int y,int z,int destroyProgress) {
        this.ac$mesher(this.level,0,0,0).tesselateInWorld(tile,x,y,z,destroyProgress);
    }
    @Overwrite public void tesselateInWorldNoCulling(Tile tile,int x,int y,int z) {
        this.ac$mesher(this.level,0,0,0).tesselateInWorldNoCulling(tile,x,y,z);
    }
    @Overwrite public boolean tesselateTorchInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateTorchInWorld(tile,x,y,z);
    }
    @Overwrite public void tesselatePistonHeadInWorld(Tile tile,int x,int y,int z) {
        this.ac$mesher(this.level,0,0,0).tesselatePistonHeadInWorld(tile,x,y,z);
    }
    @Overwrite public void tesselateHeadPistonInWorldNoCulling(Tile tile,int x,int y,int z,boolean extended) {
        this.ac$mesher(this.level,0,0,0).tesselateHeadPistonInWorldNoCulling(tile,x,y,z,extended);
    }
    @Overwrite public boolean tesselateLeverInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateLeverInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateFireInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateFireInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateDustInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateDustInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateRailInWorld(RailTile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateRailInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateLadderInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateLadderInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateCrossInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateCrossInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateRowInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateRowInWorld(tile,x,y,z);
    }
    @Overwrite public void tesselateTorch(Tile tile,double x,double y,double z,double xOffset,double zOffset) {
        this.ac$mesher(this.level,0,0,0).tesselateTorch(tile,x,y,z,xOffset,zOffset);
    }
    @Overwrite public void tesselateCrossTexture(Tile tile,int metadata,double x,double y,double z) {
        this.ac$mesher(this.level,0,0,0).tesselateCrossTexture(tile,metadata,x,y,z);
    }
    @Overwrite public void tesselateRowTexture(Tile tile,int metadata,double x,double y,double z) {
        this.ac$mesher(this.level,0,0,0).tesselateRowTexture(tile,metadata,x,y,z);
    }
    @Overwrite public boolean tesselateWaterInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateWaterInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateBlockInWorldWithAmbienceOcclusion(Tile tile,int x,int y,int z,float red,float green,float blue) {
        return this.ac$mesher(this.level,0,0,0).tesselateBlockInWorldWithAmbienceOcclusion(tile,x,y,z,red,green,blue);
    }
    @Overwrite public boolean tesselateBlockInWorld(Tile tile,int x,int y,int z,float red,float green,float blue) {
        return this.ac$mesher(this.level,0,0,0).tesselateBlockInWorld(tile,x,y,z,red,green,blue);
    }
    @Overwrite public boolean tesselateCactusInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateCactusInWorld(tile,x,y,z);
    }
    @Overwrite public boolean method_63(Tile tile,int x,int y,int z,float red,float green,float blue) {
        return this.ac$mesher(this.level,0,0,0).method_63(tile,x,y,z,red,green,blue);
    }
    @Overwrite public boolean tesselateFenceInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateFenceInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateStairsInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateStairsInWorld(tile,x,y,z);
    }
    @Overwrite public boolean tesselateDoorInWorld(Tile tile,int x,int y,int z) {
        return this.ac$mesher(this.level,0,0,0).tesselateDoorInWorld(tile,x,y,z);
    }
    @Overwrite public void renderFaceDown(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderFaceDown(tile,x,y,z,texture);
    }
    @Overwrite public void renderFaceUp(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderFaceUp(tile,x,y,z,texture);
    }
    @Overwrite public void renderNorth(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderNorth(tile,x,y,z,texture);
    }
    @Overwrite public void renderSouth(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderSouth(tile,x,y,z,texture);
    }
    @Overwrite public void renderWest(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderWest(tile,x,y,z,texture);
    }
    @Overwrite public void renderEast(Tile tile,double x,double y,double z,int texture) {
        this.ac$mesher(this.level,0,0,0).renderEast(tile,x,y,z,texture);
    }
    @Overwrite public static boolean canRender(int shape) { return BlockMesher.canRender(shape); }
}
