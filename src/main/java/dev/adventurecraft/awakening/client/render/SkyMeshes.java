package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;
import java.util.Random;

/** Native atmosphere enclosure, legacy sky planes and the original deterministic star field. */
public final class SkyMeshes {
    private SkyMeshes() { }

    /** Camera-centered direction carrier for atmosphere and cloud ray marching. */
    public static VulkanRenderer.Mesh atmosphere() {
        return EntityMeshes.mesh("sky:atmosphere", () -> {
            var out = new QuadBuilder(24);
            double radius = 128;
            double[][] corners = {
                {-radius, -radius, -radius}, {radius, -radius, -radius},
                {radius, radius, -radius}, {-radius, radius, -radius},
                {-radius, -radius, radius}, {radius, -radius, radius},
                {radius, radius, radius}, {-radius, radius, radius}
            };
            int[][] faces = {{0, 1, 2, 3}, {5, 4, 7, 6}, {4, 0, 3, 7},
                {1, 5, 6, 2}, {3, 2, 6, 7}, {4, 5, 1, 0}};
            for (int[] face : faces) {
                for (int index : face) {
                    double[] point = corners[index];
                    out.vertexUV(point[0], point[1], point[2], 0, 0);
                }
            }
            return out.vertices();
        });
    }

    public static VulkanRenderer.Mesh plane(boolean dark) {
        return EntityMeshes.mesh("sky:"+dark, () -> {
            var out = new QuadBuilder(13*13*4); double y=dark?-16:16;
            for(int x=-384;x<=384;x+=64) for(int z=-384;z<=384;z+=64) {
                if(dark) {
                    out.vertexUV(x+64,y,z,0,0);out.vertexUV(x,y,z,0,0);
                    out.vertexUV(x,y,z+64,0,0);out.vertexUV(x+64,y,z+64,0,0);
                } else {
                    out.vertexUV(x,y,z,0,0);out.vertexUV(x+64,y,z,0,0);
                    out.vertexUV(x+64,y,z+64,0,0);out.vertexUV(x,y,z+64,0,0);
                }
            }
            return out.vertices();
        });
    }
    public static VulkanRenderer.Mesh stars() {
        return EntityMeshes.mesh("stars", () -> {
            var out = new QuadBuilder(6000); var random = new Random(10842L);
            for(int star=0;star<1500;star++) {
                double x=random.nextFloat()*2-1,y=random.nextFloat()*2-1,z=random.nextFloat()*2-1;
                double size=.25f+random.nextFloat()*.25f, length=x*x+y*y+z*z;
                if(length>=1||length<=.01) continue;
                length=1/Math.sqrt(length);x*=length;y*=length;z*=length;
                double px=x*100,py=y*100,pz=z*100;
                double yaw=Math.atan2(x,z),sy=Math.sin(yaw),cy=Math.cos(yaw);
                double pitch=Math.atan2(Math.sqrt(x*x+z*z),y),sp=Math.sin(pitch),cp=Math.cos(pitch);
                double spin=random.nextDouble()*Math.PI*2,ss=Math.sin(spin),cs=Math.cos(spin);
                for(int corner=0;corner<4;corner++) {
                    double u=((corner&2)-1)*size,v=(((corner+1)&2)-1)*size;
                    double a=u*cs-v*ss,b=v*cs+u*ss;
                    double vertical=a*sp,radial=-a*cp;
                    out.vertexUV(px+radial*sy-b*cy,py+vertical,pz+b*sy+radial*cy,0,0);
                }
            }
            return out.vertices();
        });
    }

    public static VulkanRenderer.Mesh celestial(boolean moon) {
        return EntityMeshes.mesh("celestial:" + moon, () -> {
            var out = new QuadBuilder(4);
            double size = moon ? 20 : 30, y = moon ? -100 : 100;
            if (moon) {
                out.vertexUV(-size,y,size,1,1); out.vertexUV(size,y,size,0,1);
                out.vertexUV(size,y,-size,0,0); out.vertexUV(-size,y,-size,1,0);
            } else {
                out.vertexUV(-size,y,-size,0,0); out.vertexUV(size,y,-size,1,0);
                out.vertexUV(size,y,size,1,1); out.vertexUV(-size,y,size,0,1);
            }
            return out.vertices();
        });
    }
}
