#include "ray_lighting.glsl"
#include "restir_gbuffer.glsl"
#include "sharc_parameters.glsl"
layout(set = 0, binding = 0) uniform sampler2D sharcGbuffer[8];

struct SharcScreenSurface {
    vec3 position;
    vec3 normal;
    vec3 geometricNormal;
    vec3 albedo;
    vec3 f0;
    float metallic;
    float roughness;
    uint flags;
    bool valid;
};
SharcScreenSurface sharcScreenSurface(ivec2 pixel, bool previous) {
    vec4 position = previous ? texelFetch(sharcGbuffer[4],pixel,0) : texelFetch(sharcGbuffer[0],pixel,0);
    vec4 normal = previous ? texelFetch(sharcGbuffer[5],pixel,0) : texelFetch(sharcGbuffer[1],pixel,0);
    vec4 albedo = previous ? texelFetch(sharcGbuffer[6],pixel,0) : texelFetch(sharcGbuffer[2],pixel,0);
    vec4 f0 = previous ? texelFetch(sharcGbuffer[7],pixel,0) : texelFetch(sharcGbuffer[3],pixel,0);
    SharcScreenSurface s;
    s.position = position.xyz;
    s.normal = restirOctDecode(normal.xy);
    s.geometricNormal = restirOctDecode(normal.zw);
    s.albedo = albedo.rgb;
    s.metallic = albedo.a;
    s.f0 = f0.rgb;
    s.roughness = max(f0.a,0.02);
    s.flags = uint(round(max(position.w,0.0)));
    s.valid = (s.flags&RESTIR_VALID)!=0u;
    return s;
}
bool sharcCompatible(SharcScreenSurface current, SharcScreenSurface other) {
    if (!current.valid || !other.valid || current.flags!=other.flags) return false;
    if (dot(current.normal,other.normal)<0.92 || dot(current.geometricNormal,other.geometricNormal)<0.95) return false;
    // This buffer stores incoming diffuse irradiance, before primary albedo or
    // BRDF. Texture colors and surface roughness are not transport boundaries:
    // gating on them leaves detailed legacy atlases with almost no denoising.
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    float distance = length(current.position-p.eyeOrigin.xyz);
    vec3 delta = other.position-current.position;
    return abs(dot(delta,current.geometricNormal))<=0.025+distance*0.0015
        && length(delta)<=max(0.2,distance*0.04);
}
bool sharcDenoiseReproject(SharcScreenSurface current, out vec2 previousUv) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    previousUv = vec2(0.0);
    if ((p.control.x&1u)==0u) return false;
    vec4 clip = p.previousViewProjection*vec4(current.position+p.originDelta.xyz,1.0);
    if (any(isnan(clip)) || any(isinf(clip)) || clip.w<=0.000001) return false;
    vec3 ndc = clip.xyz/clip.w;
    if (ndc.z<0.0 || ndc.z>1.0 || any(greaterThan(abs(ndc.xy),vec2(1.0)))) return false;
    previousUv = ndc.xy*0.5+0.5;
    return true;
}
Surface sharcRecoverSurface(SharcScreenSurface screen, bool recoverMaterial) {
    Surface s;
    // Recover authored emission and transport fields absent from the compact MRT.
    bool recovered = false;
    if (recoverMaterial) recovered = traceRasterScene(screen.position+screen.geometricNormal*0.002,
        -screen.geometricNormal,0.004,s);
    if (!recovered) {
        s.emission = 0.0;
        s.alpha = 1.0;
        s.cutoff = 0.1;
        s.skyLight = 1.0;
        s.surfaceFlags = 0u;
        s.tag = 0u;
        s.ao = 1.0;
        s.sss = 0.0;
        s.porosity = 0.0;
        s.height = 0.0;
        s.transmission = 0.0;
        s.ior = 1.5;
        s.absorption = vec3(0.02);
    }
    s.position = screen.position;
    s.normal = screen.normal;
    s.geometricNormal = screen.geometricNormal;
    s.albedo = screen.albedo;
    s.f0 = screen.f0;
    s.metallic = screen.metallic;
    s.roughness = screen.roughness;
    s.thin = (screen.flags&RESTIR_THIN)!=0u;
    s.decorative = (screen.flags&RESTIR_DECORATIVE)!=0u;
    return s;
}
vec3 sharcDiffuseAlbedo(Surface s) { return s.albedo*(1.0-s.metallic); }
vec3 sharcEmission(Surface s) { return s.albedo*max(s.emission,0.0)*4.0; }
vec3 sharcPathNormal(Surface s) { return s.decorative ? vec3(0,1,0) : s.normal; }
vec3 sharcScatterSample(Surface s, vec2 samplePoint, float side) {
    vec3 normal = sharcPathNormal(s);
    if (s.thin && !s.decorative && side<0.5) normal = -normal;
    return hemisphere(normal,samplePoint.x,samplePoint.y);
}
vec3 sharcScatter(Surface s, inout uint rng) {
    vec2 samplePoint = vec2(sharcRandom(rng),sharcRandom(rng));
    return sharcScatterSample(s,samplePoint,sharcRandom(rng));
}
