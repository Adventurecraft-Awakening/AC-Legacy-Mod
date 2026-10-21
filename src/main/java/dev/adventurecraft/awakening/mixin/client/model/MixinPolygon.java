package dev.adventurecraft.awakening.mixin.client.model;

import dev.adventurecraft.awakening.client.render.VertexSink;
import dev.adventurecraft.awakening.extension.client.model.ExTexturedQuad;
import net.minecraft.client.model.Polygon;
import net.minecraft.client.model.Vertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Polygon.class)
public abstract class MixinPolygon implements ExTexturedQuad {
    @Shadow public Vertex[] vertices;
    @Shadow private boolean flipNormal;
    @Override public void ac$write(VertexSink sink,float scale) {
        var p0=vertices[0].pos; var p1=vertices[1].pos; var p2=vertices[2].pos;
        double ax=p0.x-p1.x, ay=p0.y-p1.y, az=p0.z-p1.z;
        double bx=p2.x-p1.x, by=p2.y-p1.y, bz=p2.z-p1.z;
        double nx=by*az-bz*ay, ny=bz*ax-bx*az, nz=bx*ay-by*ax;
        double length=Math.sqrt(nx*nx+ny*ny+nz*nz);
        double factor=length<1E-4?0:(this.flipNormal?-1:1)/length;
        sink.ac$normal32((float)(nx*factor),(float)(ny*factor),(float)(nz*factor));
        for(int i=0;i<4;i++) {
            var vertex=vertices[i];
            sink.ac$vertexUV((float)vertex.pos.x*scale,(float)vertex.pos.y*scale,(float)vertex.pos.z*scale,vertex.u,vertex.v);
        }
    }
}
