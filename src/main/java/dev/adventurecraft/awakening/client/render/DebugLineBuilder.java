package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.state.DrawParameters;
import dev.adventurecraft.awakening.image.Rgba;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** A scoped line path writer feeding the shared debug collector without GPU or global state. */
public final class DebugLineBuilder {
    private final DebugDraw draw;
    private float width=1;
    private boolean strip, pending;
    private double x,y,z;
    private int color=-1, previousColor;
    private final boolean depthWrite;
    public DebugLineBuilder(DebugDraw draw,float width,boolean depthWrite) {
        this.draw=java.util.Objects.requireNonNull(draw);this.width=width;this.depthWrite=depthWrite;
    }
    public static void with(Supplier<DrawParameters> root,Consumer<DebugLineBuilder> geometry) {
        DebugDraw.with(root,draw -> geometry.accept(new DebugLineBuilder(draw,1,true)));
    }
    public void width(float value) { this.width=value; }
    public void lines() { this.strip=false;this.pending=false; }
    public void strip() { this.strip=true;this.pending=false; }
    public void end() { this.pending=false; }
    public void color(int rgb) { this.color=rgb|0xff000000; }
    public void color(float r,float g,float b,float a) {
        this.color=Rgba.fromRgba8(Rgba.saturateToByte(r),Rgba.saturateToByte(g),Rgba.saturateToByte(b),Rgba.saturateToByte(a));
    }
    public void vertex(double x,double y,double z) {
        if(this.pending) this.draw.line(this.x,this.y,this.z,x,y,z,this.previousColor,this.color,this.width,this.depthWrite);
        boolean continuePath=this.strip||!this.pending;
        this.x=x;this.y=y;this.z=z;this.previousColor=this.color;this.pending=continuePath;
    }
}
