package dev.adventurecraft.awakening.client.render;

import dev.adventurecraft.awakening.client.render.vulkan.VulkanRenderer;

/** Camera-independent cloud lattice; animation is supplied by pose and UV transforms. */
public final class CloudMeshes {
    private CloudMeshes() { }
    public static VulkanRenderer.Mesh flat() {
        return EntityMeshes.mesh("clouds:flat", () -> {
            var out = new QuadBuilder(16 * 16 * 4);
            for (int x = -256; x < 256; x += 32) for (int z = -256; z < 256; z += 32) {
                out.vertexUV(x,0,z+32,x/2048.0,(z+32)/2048.0);
                out.vertexUV(x+32,0,z+32,(x+32)/2048.0,(z+32)/2048.0);
                out.vertexUV(x+32,0,z,(x+32)/2048.0,z/2048.0);
                out.vertexUV(x,0,z,x/2048.0,z/2048.0);
            }
            return out.vertices();
        });
    }
    public static VulkanRenderer.Mesh lattice(boolean bottom, boolean top) {
        return EntityMeshes.mesh("clouds:"+bottom+":"+top, () -> {
            var ts = new QuadBuilder(5000);
            int tileHeight=4, patchWidth=8, patchBound=3;
            double cloudFracX=0,cloudFracZ=0,baseU=0,baseV=0,xOffset=0,uvScale=1.0/256;
            double y0 = 0;
            double y1 = y0 + tileHeight;

            for (int xPatch = -patchBound + 1; xPatch <= patchBound; ++xPatch) {

                double startU = xPatch * patchWidth;
                double x0 = startU - cloudFracX;
                double x1 = x0 + patchWidth;

                double u0 = startU * uvScale + baseU;
                double u1 = (startU + patchWidth) * uvScale + baseU;

                for (int zPatch = -patchBound + 1; zPatch <= patchBound; ++zPatch) {

                    double startV = zPatch * patchWidth;
                    double z0 = startV - cloudFracZ;
                    double z1 = z0 + patchWidth;

                    double v0 = startV * uvScale + baseV;
                    double v1 = (startV + patchWidth) * uvScale + baseV;

                    ts.color(.9F, .9F, .9F, 0.8F);
                    if (xPatch > -1) {
                        ts.normal(-1.0F, 0.0F, 0.0F);

                        for (int pX = 0; pX < patchWidth; ++pX) {
                            double vX = x0 + pX;
                            double vU = (startU + pX + 0.5) * uvScale + baseU;
                            ts.vertexUV(vX, y0, z1, vU, v1);
                            ts.vertexUV(vX, y1, z1, vU, v1);
                            ts.vertexUV(vX, y1, z0, vU, v0);
                            ts.vertexUV(vX, y0, z0, vU, v0);
                        }
                    }

                    if (xPatch <= 1) {
                        ts.normal(1.0F, 0.0F, 0.0F);

                        for (int pX = 0; pX < patchWidth; ++pX) {
                            double vX = x0 + pX + 1.0 - xOffset;
                            double vU = (startU + pX + 0.5) * uvScale + baseU;
                            ts.vertexUV(vX, y0, z1, vU, v1);
                            ts.vertexUV(vX, y1, z1, vU, v1);
                            ts.vertexUV(vX, y1, z0, vU, v0);
                            ts.vertexUV(vX, y0, z0, vU, v0);
                        }
                    }

                    ts.color(.8F, .8F, .8F, 0.8F);
                    if (zPatch > -1) {
                        ts.normal(0.0F, 0.0F, -1.0F);

                        for (int pZ = 0; pZ < patchWidth; ++pZ) {
                            double vZ = z0 + pZ;
                            double vV = (startV + pZ + 0.5) * uvScale + baseV;
                            ts.vertexUV(x0, y1, vZ, u0, vV);
                            ts.vertexUV(x1, (y1), vZ, u1, vV);
                            ts.vertexUV(x1, y0, vZ, u1, vV);
                            ts.vertexUV(x0, y0, vZ, u0, vV);
                        }
                    }

                    if (zPatch <= 1) {
                        ts.normal(0.0F, 0.0F, 1.0F);

                        for (int pZ = 0; pZ < patchWidth; ++pZ) {
                            double vZ = z0 + pZ + 1.0 - xOffset;
                            double vV = (startV + pZ + 0.5) * uvScale + baseV;
                            ts.vertexUV(x0, y1, vZ, u0, vV);
                            ts.vertexUV(x1, y1, vZ, u1, vV);
                            ts.vertexUV(x1, y0, vZ, u1, vV);
                            ts.vertexUV(x0, y0, vZ, u0, vV);
                        }
                    }

                    if (bottom) {
                        ts.color(.7F, .7F, .7F, 0.8F);
                        ts.normal(0.0F, -1.0F, 0.0F);

                        ts.vertexUV(x0, y0, z1, u0, v1);
                        ts.vertexUV(x1, y0, z1, u1, v1);
                        ts.vertexUV(x1, y0, z0, u1, v0);
                        ts.vertexUV(x0, y0, z0, u0, v0);
                    }

                    if (top) {
                        ts.color(1, 1, 1, .8F);
                        ts.normal(0.0F, 1.0F, 0.0F);

                        double vY = y1 - xOffset;
                        ts.vertexUV(x0, vY, z1, u0, v1);
                        ts.vertexUV(x1, vY, z1, u1, v1);
                        ts.vertexUV(x1, vY, z0, u1, v0);
                        ts.vertexUV(x0, vY, z0, u0, v0);
                    }
                }
            }


            return ts.vertices();
        });
    }
}
