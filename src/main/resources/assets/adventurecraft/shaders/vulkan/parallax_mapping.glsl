#ifndef AC_PARALLAX_MAPPING_GLSL
#define AC_PARALLAX_MAPPING_GLSL

// Primary-hit UV cavities only: no geometry, raster depth, shadow or secondary-ray displacement.
// Height one is the original plane. Lower values descend into a shallow world-space layer.
const float PARALLAX_DEPTH = 0.08;
const float PARALLAX_MAX_DEPTH = 0.25;

bool parallaxFinite(vec2 value) { return !any(isnan(value)) && !any(isinf(value)); }
bool parallaxFinite(vec3 value) { return !any(isnan(value)) && !any(isinf(value)); }

vec2 parallaxTexelMargin(ivec2 extent) {
    // Singleton axes are constant maps (including the neutral 1x1 companion), not neighboring tiles.
    return vec2(extent.x>1 ? 0.5/float(extent.x) : 0.0,
        extent.y>1 ? 0.5/float(extent.y) : 0.0);
}

vec2 parallaxMappedUv(sampler2D heightImage, sampler2D specularImage, vec2 originalUv,
                      vec2 uvDx, vec2 uvDy, vec3 positionDx, vec3 positionDy,
                      vec3 geometricNormal, vec3 toEye, float distanceToEye,
                      ivec2 albedoSampleExtent, float originalHeight, uint quality) {
    if (quality==0u || originalHeight>=1.0 || !parallaxFinite(originalUv)
        || !parallaxFinite(uvDx) || !parallaxFinite(uvDy)
        || !parallaxFinite(positionDx) || !parallaxFinite(positionDy)
        || !parallaxFinite(geometricNormal) || !parallaxFinite(toEye)
        || isnan(distanceToEye) || isinf(distanceToEye)
        || any(greaterThan(abs(originalUv),vec2(65536.0)))) return originalUv;
    float facing = dot(geometricNormal,toEye);
    float fadeEnd = quality==1u ? 48.0 : 64.0;
    if (facing<0.15 || distanceToEye>=fadeEnd || any(lessThan(albedoSampleExtent,ivec2(1)))) return originalUv;
    float worldArea = length(cross(positionDx,positionDy));
    float worldScale = length(positionDx)*length(positionDy);
    float uvArea = abs(uvDx.x*uvDy.y-uvDx.y*uvDy.x);
    float uvScale = length(uvDx)*length(uvDy);
    if (worldArea<=max(worldScale*0.000001,1e-24)
        || uvArea<=max(uvScale*0.000001,1e-24)) return originalUv;

    // Dual world derivatives recover UV units per world block without assuming a square,
    // axis-aligned or unmirrored block face. The oriented normal's sign cancels in the solve.
    vec3 dualX = cross(positionDy,geometricNormal);
    vec3 dualY = cross(geometricNormal,positionDx);
    float determinant = dot(positionDx,dualX);
    if (abs(determinant)<=max(worldScale*0.000001,1e-24)) return originalUv;
    float depthScale = min(PARALLAX_DEPTH,PARALLAX_MAX_DEPTH)
        *(1.0-smoothstep(24.0,fadeEnd,distanceToEye))*smoothstep(0.15,0.3,facing);
    vec3 worldOffset = -toEye*(depthScale/facing);
    vec2 screenOffset = vec2(dot(worldOffset,dualX),dot(worldOffset,dualY))/determinant;
    vec2 uvOffset = uvDx*screenOffset.x+uvDy*screenOffset.y;
    if (!parallaxFinite(uvOffset) || dot(uvOffset,uvOffset)<=1e-24) return originalUv;

    vec2 margin = max(parallaxTexelMargin(albedoSampleExtent),
        max(parallaxTexelMargin(textureSize(heightImage,0)),parallaxTexelMargin(textureSize(specularImage,0))));
    // A nonconstant image smaller than the tile grid cannot isolate all sixteen cells.
    if (any(greaterThan(margin,vec2(0.5/16.0)))) return originalUv;
    // Large repeated UV coordinates can no longer represent the required edge margin precisely.
    if ((margin.x>0.0 && abs(originalUv.x)*0.00000012>=margin.x*0.5)
        || (margin.y>0.0 && abs(originalUv.y)*0.00000012>=margin.y*0.5)) return originalUv;
    vec2 tile = floor(originalUv*16.0)/16.0;
    vec2 lower = tile+margin;
    vec2 upper = tile+vec2(1.0/16.0)-margin;
    vec2 previousUv = clamp(originalUv,lower,upper);
    float previousLayer = 0.0;
    float previousDepth = 1.0-textureLod(heightImage,previousUv,0.0).a;
    if (previousDepth<=0.0) return originalUv;
    int steps = distanceToEye>24.0 ? 4 : quality==1u ? 8 : 16;
    for (int step=1;step<=steps;step++) {
        float layer = float(step)/float(steps);
        vec2 candidate = clamp(originalUv+uvOffset*layer,lower,upper);
        // Explicit LOD keeps the divergent march independent of derivative availability.
        float sampledDepth = 1.0-textureLod(heightImage,candidate,0.0).a;
        if (layer>=sampledDepth) {
            float before = previousDepth-previousLayer;
            float after = sampledDepth-layer;
            float fraction = clamp(before/max(before-after,0.000001),0.0,1.0);
            return clamp(mix(previousUv,candidate,fraction),lower,upper);
        }
        previousUv = candidate;
        previousLayer = layer;
        previousDepth = sampledDepth;
    }
    return previousUv;
}

#endif
