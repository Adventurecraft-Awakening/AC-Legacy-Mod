package dev.adventurecraft.awakening.client.render;

/** A meshing-local origin, never shared through the global tessellator. */
public final class OffsetVertexSink implements VertexSink {
    private final VertexSink target;
    private double x,y,z;
    public OffsetVertexSink(VertexSink target,double x,double y,double z) {
        this.target=java.util.Objects.requireNonNull(target); this.x=x;this.y=y;this.z=z;
    }
    public void ac$vertex(float x,float y,float z) { target.ac$vertex((float)(x+this.x),(float)(y+this.y),(float)(z+this.z)); }
    public void ac$vertexUV(float x,float y,float z,float u,float v) { target.ac$vertexUV((float)(x+this.x),(float)(y+this.y),(float)(z+this.z),u,v); }
    public void vertexUV(double x,double y,double z,double u,double v) { target.ac$vertexUV((float)(x+this.x),(float)(y+this.y),(float)(z+this.z),(float)u,(float)v); }
    public void vertex(double x,double y,double z) { target.ac$vertex((float)(x+this.x),(float)(y+this.y),(float)(z+this.z)); }
    public void ac$tex(float u,float v) { target.ac$tex(u,v); }
    public void ac$color8(int rgba) { target.ac$color8(rgba); }
    public void ac$color32(float r,float g,float b,float a) { target.ac$color32(r,g,b,a); }
    public void ac$normal8(int xyz) { target.ac$normal8(xyz); }
    public void ac$normal32(float x,float y,float z) { target.ac$normal32(x,y,z); }
    public void ac$material(int material) { target.ac$material(material); }
    public void ac$skyLight(int skyLight) { target.ac$skyLight(skyLight); }
    public void ac$surfaceFlags(int flags) { target.ac$surfaceFlags(flags); }
    public void addOffset(float x,float y,float z) { this.x+=x;this.y+=y;this.z+=z; }
}
