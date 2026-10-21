// Matches the uncapped 16-byte-header/48-byte emitter table used by ReSTIR DI.
struct SharcEmitter {
    vec4 positionRadius;
    vec4 colorIntensity;
    uvec2 stableId;
    float halfExtent;
    float cdf;
};
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer SharcEmitterTable {
    uvec4 header;
    SharcEmitter values[];
};
vec3 sharcLocalIrradiance(Surface surface, inout uint rng) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    if (p.emitters==uint64_t(0)) return vec3(0.0);
    SharcEmitterTable source = SharcEmitterTable(p.emitters);
    if (source.header.x==0u) return vec3(0.0);
    vec3 result = vec3(0.0);
    for (int candidate=0;candidate<2;candidate++) {
        // Stratify the CDF domain, retaining the original selection PDF and sample mean.
        float uniformValue = (float(candidate)+sharcRandom(rng))*0.5;
        uint lo = 0u, hi = source.header.x;
        while (lo<hi) {
            uint mid = lo+(hi-lo)/2u;
            if (source.values[mid].cdf<=uniformValue) lo = mid+1u;
            else hi = mid;
        }
        uint index = min(lo,source.header.x-1u);
        SharcEmitter light = source.values[index];
        float pdf = light.cdf-(index==0u ? 0.0 : source.values[index-1u].cdf);
        if (pdf<=0.0) continue;
        vec3 emitterPosition = light.positionRadius.xyz, emitterNormal = vec3(0.0);
        if (light.halfExtent>0.0) {
            int face = min(int(sharcRandom(rng)*6.0),5);
            vec2 uv = vec2(sharcRandom(rng),sharcRandom(rng))*2.0-1.0;
            float side = (face&1)==0 ? -1.0 : 1.0;
            vec3 offset;
            if (face<2) { emitterNormal = vec3(side,0,0); offset = vec3(side,uv); }
            else if (face<4) { emitterNormal = vec3(0,side,0); offset = vec3(uv.x,side,uv.y); }
            else { emitterNormal = vec3(0,0,side); offset = vec3(uv,side); }
            emitterPosition += offset*light.halfExtent;
            pdf *= 1.0/6.0;
        }
        vec3 delta = emitterPosition-surface.position;
        float distance = length(delta), radius = light.positionRadius.w;
        if (distance<=0.00001 || radius<=0.0 || distance>=radius) continue;
        vec3 direction = delta/distance;
        if (!surfaceReceivesLight(surface,direction)) continue;
        float cosine = dot(sharcPathNormal(surface),direction);
        cosine = surface.thin && !surface.decorative ? abs(cosine)*0.675 : max(cosine,0.0);
        if (cosine<=0.0) continue;
        float window = max(1.0-(distance/radius)*(distance/radius),0.0);
        vec3 radiance = linearColor(light.colorIntensity.rgb)*max(light.colorIntensity.w,0.0)
            *(0.9*window*window/(1.0+0.22*distance*distance));
        if (light.halfExtent>0.0) radiance *= (2.0/3.0)*max(dot(emitterNormal,-direction),0.0);
        if (visibility(rayOrigin(surface.position,surface.geometricNormal,direction),direction,
            max(distance-0.0001,0.00001),light.positionRadius.xyz,true)) result += radiance*cosine/pdf;
    }
    return result*0.5;
}
vec3 sharcDirectDiffuse(Surface surface, inout uint rng) {
    return sharcDiffuseAlbedo(surface)*(sunlightTransport(surface)+sharcLocalIrradiance(surface,rng));
}
