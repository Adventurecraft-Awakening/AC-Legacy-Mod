// Independent KHR ray-query implementation. All positions are camera-relative.
#extension GL_EXT_ray_query : require
#extension GL_EXT_buffer_reference2 : require
#extension GL_EXT_shader_explicit_arithmetic_types_int64 : require
#extension GL_EXT_nonuniform_qualifier : require

layout(set = 4, binding = 0) uniform accelerationStructureEXT worldScene;
struct InstanceData { uint64_t vertices; uint textureIndex; uint flags; };
layout(set = 4, binding = 1, std430) readonly buffer InstanceTable { InstanceData instances[]; };
layout(set = 4, binding = 2) uniform sampler2D sceneTextures[16];
struct PointLight { vec4 positionRadius; vec4 colorIntensity; };
layout(set = 4, binding = 3, std430) readonly buffer SceneLighting {
    vec4 sunDirectionIntensity;
    vec4 sunColor; // RGB radiance, user brightness
    vec4 skyColorIntensity;
    vec4 parameters; // point light count, world-space camera origin XYZ
    vec4 envSun;
    vec4 envSky;
    vec4 envWeather;
    vec4 envCamera;
    vec4 envClouds;
    vec4 envFog;
    PointLight pointLights[];
} lighting;
layout(buffer_reference, std430, buffer_reference_align = 4) readonly buffer Vertices { uint words[]; };
#include "environment.glsl"

Environment rayEnvironment() {
    return Environment(lighting.envSun,lighting.envSky,lighting.envWeather,
        lighting.envCamera,lighting.envClouds,lighting.envFog);
}

vec3 materialTint(uint tag) {
    return tag == 0u ? vec3(1.0) : vec3((tag >> 16u) & 255u, (tag >> 8u) & 255u, tag & 255u) / 255.0;
}
const uint THIN_FOLIAGE = 0x20000000u;
const uint DECORATIVE_GRASS = 0x40000000u;
const uint LEAF_VOLUME = 0x80000000u;
bool thinSurface(uint tag) { return (tag & THIN_FOLIAGE)!=0u && (tag & LEAF_VOLUME)==0u; }
// Reserved emission code31 marks collision-only clip geometry, independent of atlas alpha.
bool solidOccluder(uint tag) { return (tag&0x1f000000u)==0x1f000000u; }
float materialEmission(uint tag) {
    return solidOccluder(tag) ? 0.0 : float(max(int((tag >> 24u) & 31u) - 1, 0)) / 15.0;
}
vec3 linearColor(vec3 color) { return srgbToLinear(color); }

// Positions are camera-relative. A small precision-scaled offset avoids self hits without
// lifting the ray past touching block faces, as the old 0.006-0.02 block bias did.
vec3 rayOrigin(vec3 position, vec3 normal, vec3 direction) {
    vec3 oriented = dot(normal,direction)>=0.0 ? normal : -normal;
    float error = max(0.00002, dot(abs(oriented),abs(position))*0.0000005);
    return position+oriented*error;
}

struct Surface {
    vec3 albedo;
    vec3 normal;
    vec3 geometricNormal;
    vec3 position;
    float emission;
    float alpha;
    float cutoff;
    float skyLight;
    uint surfaceFlags;
    uint tag;
    bool thin;
    bool decorative;
    vec3 f0;
    float roughness;
    float metallic;
    float ao;
    float sss;
    float porosity;
    float height;
    float transmission;
    float ior;
    vec3 absorption;
};

bool surfaceReceivesLight(Surface surface, vec3 direction) {
    return !solidOccluder(surface.tag) && (surface.thin || surface.decorative
        || dot(surface.geometricNormal,direction)>0.0);
}

vec3 calculateF0(vec3 n, vec3 k) {
    vec3 k2 = max(k*k, vec3(1e-6));
    return ((n-1.0)*(n-1.0)+k2)/((n+1.0)*(n+1.0)+k2);
}

vec3 conductorF0(int index) {
    if (index==230) return calculateF0(vec3(2.9114,2.9497,2.5845),vec3(3.0893,2.9318,2.7670));
    if (index==231) return calculateF0(vec3(0.18299,0.42108,1.3734),vec3(3.4242,2.3459,1.7704));
    if (index==232) return calculateF0(vec3(1.3456,0.96521,0.61722),vec3(7.4746,6.3995,5.3031));
    if (index==233) return calculateF0(vec3(3.1071,3.1812,2.3230),vec3(3.3314,3.3291,3.1350));
    if (index==234) return calculateF0(vec3(0.27105,0.67693,1.3164),vec3(3.6092,2.6248,2.2921));
    if (index==235) return calculateF0(vec3(1.9100,1.8300,1.4400),vec3(3.5100,3.4000,3.1800));
    if (index==236) return calculateF0(vec3(2.3757,2.0847,1.8453),vec3(4.2655,3.7153,3.1365));
    if (index==237) return calculateF0(vec3(0.15943,0.14512,0.13547),vec3(3.9291,3.1900,2.3808));
    return vec3(0.04);
}

vec3 decodeTangentNormal(vec4 texel) {
    vec2 xy = texel.xy*2.0-1.0;
    xy.y = -xy.y;
    return vec3(xy,sqrt(max(1.0-dot(xy,xy),0.0)));
}

bool triangleTbn(vec3 a, vec3 b, vec3 c, vec2 ta, vec2 tb, vec2 tc, vec3 n, out mat3 tbn) {
    vec3 e1 = b-a, e2 = c-a;
    vec2 d1 = tb-ta, d2 = tc-ta;
    float det = d1.x*d2.y-d1.y*d2.x;
    if (abs(det)<1e-10) return false;
    vec3 t = (e1*d2.y-e2*d1.y)/det;
    t = t-n*dot(n,t);
    if (dot(t,t)<1e-12) return false;
    t = normalize(t);
    vec3 bt = normalize(cross(n,t));
    tbn = mat3(t,bt,n);
    return true;
}

void decodeLabPbr(vec4 normalTexel, vec4 specTexel, inout Surface s) {
    int metalIndex = int(round(specTexel.g*255.0));
    if (metalIndex<230) {
        s.metallic = 0.0;
        s.f0 = vec3(max(specTexel.g,0.02));
    } else if (metalIndex<=237) {
        s.metallic = 1.0;
        s.f0 = conductorF0(metalIndex);
        s.albedo = s.f0;
    } else {
        s.metallic = 1.0;
        s.f0 = s.albedo;
    }
    s.roughness = clamp(pow(1.0-specTexel.r,2.0),0.02,1.0);
    s.ao = normalTexel.b;
    s.height = 1.0-normalTexel.a;
    s.porosity = specTexel.b<=64.0/255.0 ? specTexel.b/(64.0/255.0) : 0.0;
    s.sss = specTexel.b>=65.0/255.0 ? (specTexel.b-65.0/255.0)/(1.0-65.0/255.0) : 0.0;
    s.emission = max(s.emission,specTexel.a>=0.999 ? 0.0 : specTexel.a);
    s.transmission = (s.surfaceFlags&(0x80u|0x20u))!=0u ? 1.0
        : (s.alpha<0.99 && !s.thin && (s.surfaceFlags&0x20u)==0u ? 1.0 : 0.0);
}

#include "tile_materials.glsl"

Surface surfaceAt(uint instanceIndex, uint triangle, vec2 barycentric, mat4x3 objectToWorld) {
    InstanceData instance = instances[instanceIndex];
    Vertices vertices = Vertices(instance.vertices);
    uint start = triangle * 24u; // three vertices, eight uints each
    vec3 a = uintBitsToFloat(uvec3(vertices.words[start], vertices.words[start+1u], vertices.words[start+2u]));
    vec3 b = uintBitsToFloat(uvec3(vertices.words[start+8u], vertices.words[start+9u], vertices.words[start+10u]));
    vec3 c = uintBitsToFloat(uvec3(vertices.words[start+16u], vertices.words[start+17u], vertices.words[start+18u]));
    vec2 ta = uintBitsToFloat(uvec2(vertices.words[start+3u], vertices.words[start+4u]));
    vec2 tb = uintBitsToFloat(uvec2(vertices.words[start+11u], vertices.words[start+12u]));
    vec2 tc = uintBitsToFloat(uvec2(vertices.words[start+19u], vertices.words[start+20u]));
    vec3 weights = vec3(1.0-barycentric.x-barycentric.y, barycentric);
    vec2 uv = ta*weights.x+tb*weights.y+tc*weights.z;
    uint tag = vertices.words[start+7u];
    vec3 wa = objectToWorld * vec4(a, 1.0);
    vec3 wb = objectToWorld * vec4(b, 1.0);
    vec3 wc = objectToWorld * vec4(c, 1.0);
    Surface result;
    result.tag = tag;
    result.geometricNormal = normalize(cross(wb-wa, wc-wa));
    result.normal = result.geometricNormal;
    result.position = wa*weights.x+wb*weights.y+wc*weights.z;
    if (solidOccluder(tag)) {
        // An invisible collision proxy absorbs transport. Its editor icon and
        // any authored companion maps must not introduce holes, light or glass.
        result.albedo = vec3(0.0);
        result.emission = 0.0;
        result.alpha = 1.0;
        result.cutoff = 0.1;
        result.skyLight = 0.0;
        result.surfaceFlags = 0u;
        result.thin = false;
        result.decorative = false;
        result.f0 = vec3(0.0);
        result.roughness = 1.0;
        result.metallic = 0.0;
        result.ao = 1.0;
        result.sss = 0.0;
        result.porosity = 0.0;
        result.height = 0.0;
        result.transmission = 0.0;
        result.ior = 1.0;
        result.absorption = vec3(0.0);
        return result;
    }
    vec4 texel = textureLod(sceneTextures[nonuniformEXT(instance.textureIndex)], uv, 0.0);
    vec4 normalTexel = textureLod(sceneTextures[nonuniformEXT(instance.textureIndex+1u)], uv, 0.0);
    vec4 specTexel = textureLod(sceneTextures[nonuniformEXT(instance.textureIndex+2u)], uv, 0.0);
    vec3 authoredAlbedo = linearColor(texel.rgb*materialTint(tag));
    result.albedo = authoredAlbedo;
    result.geometricNormal = normalize(cross(wb-wa, wc-wa));
    result.normal = result.geometricNormal;
    result.position = wa*weights.x+wb*weights.y+wc*weights.z;
    result.emission = materialEmission(tag);
    vec3 vertexAlpha = vec3(vertices.words[start+5u] >> 24u, vertices.words[start+13u] >> 24u,
        vertices.words[start+21u] >> 24u) / 255.0;
    result.alpha = texel.a*dot(vertexAlpha,weights);
    uvec3 skyTags = uvec3(vertices.words[start+6u],vertices.words[start+14u],vertices.words[start+22u])>>24u;
    result.skyLight = dot(vec3(skyTags & 15u)/15.0,weights);
    result.surfaceFlags = skyTags.x;
    result.tag = tag;
    result.cutoff = (instance.flags & 1u) != 0u ? 0.99 : 0.1;
    result.thin = thinSurface(tag);
    result.decorative = (tag & DECORATIVE_GRASS) != 0u;
    decodeLabPbr(normalTexel,specTexel,result);
    applyTileMaterial(instance.textureIndex,uv,authoredAlbedo,result);
    if (!result.thin && !result.decorative) {
        mat3 tbn;
        if (triangleTbn(wa,wb,wc,ta,tb,tc,result.geometricNormal,tbn)) {
            result.normal = normalize(tbn*decodeTangentNormal(normalTexel));
        }
    }
    return result;
}

bool traceSurfaceScene(vec3 origin, vec3 direction, float distance, bool colorOnly, out Surface result) {
    rayQueryEXT query;
    rayQueryInitializeEXT(query, worldScene, gl_RayFlagsNoneEXT, 255, origin, 0.0, direction, distance);
    while (rayQueryProceedEXT(query)) {
        if (rayQueryGetIntersectionTypeEXT(query, false) == gl_RayQueryCandidateIntersectionTriangleEXT) {
            Surface candidate = surfaceAt(rayQueryGetIntersectionInstanceCustomIndexEXT(query, false),
                rayQueryGetIntersectionPrimitiveIndexEXT(query, false), rayQueryGetIntersectionBarycentricsEXT(query, false),
                rayQueryGetIntersectionObjectToWorldEXT(query, false));
            // Leaves, ropes, fences, and custom map atlases retain their cutout silhouette.
            // Blended surfaces transmit light; opaque texels still participate in visibility.
            bool proxy = solidOccluder(candidate.tag);
            if (proxy ? !colorOnly : (!candidate.decorative && (candidate.surfaceFlags & 0x20u)==0u
                && candidate.alpha > candidate.cutoff)) rayQueryConfirmIntersectionEXT(query);
        }
    }
    if (rayQueryGetIntersectionTypeEXT(query, true) == gl_RayQueryCommittedIntersectionNoneEXT) return false;
    result = surfaceAt(rayQueryGetIntersectionInstanceCustomIndexEXT(query, true),
        rayQueryGetIntersectionPrimitiveIndexEXT(query, true), rayQueryGetIntersectionBarycentricsEXT(query, true),
        rayQueryGetIntersectionObjectToWorldEXT(query, true));
    if (dot(result.normal, direction) > 0.0) result.normal = -result.normal;
    if (dot(result.geometricNormal, direction) > 0.0) result.geometricNormal = -result.geometricNormal;
    return true;
}

bool traceScene(vec3 origin, vec3 direction, float distance, out Surface result) {
    return traceSurfaceScene(origin,direction,distance,false,result);
}

bool traceColorScene(vec3 origin, vec3 direction, float distance, out Surface result) {
    // Reflections and refractions transport light too: an invisible collision
    // wall must absorb that path rather than reveal a lamp on its other side.
    return traceSurfaceScene(origin,direction,distance,false,result);
}

bool traceRasterScene(vec3 origin, vec3 direction, float distance, out Surface result) {
    // Only material recovery for an already rasterized surface skips proxies.
    return traceSurfaceScene(origin,direction,distance,true,result);
}

bool visibility(vec3 origin, vec3 direction, float distance, vec3 emitter, bool skipEmitter) {
    rayQueryEXT query;
    rayQueryInitializeEXT(query, worldScene, gl_RayFlagsTerminateOnFirstHitEXT, 255, origin, 0.0,
        direction, max(distance, 0.00001));
    while (rayQueryProceedEXT(query)) {
        if (rayQueryGetIntersectionTypeEXT(query, false) == gl_RayQueryCandidateIntersectionTriangleEXT) {
            Surface candidate = surfaceAt(rayQueryGetIntersectionInstanceCustomIndexEXT(query, false),
                rayQueryGetIntersectionPrimitiveIndexEXT(query, false), rayQueryGetIntersectionBarycentricsEXT(query, false),
                rayQueryGetIntersectionObjectToWorldEXT(query, false));
            bool sourceSurface = skipEmitter && candidate.emission > 0.0
                && all(lessThanEqual(abs(candidate.position-emitter),vec3(0.501)));
            if (solidOccluder(candidate.tag) || (!sourceSurface && !candidate.decorative
                && (candidate.surfaceFlags & 0x20u)==0u && candidate.alpha > candidate.cutoff)) {
                rayQueryConfirmIntersectionEXT(query);
            }
        }
    }
    return rayQueryGetIntersectionTypeEXT(query, true) == gl_RayQueryCommittedIntersectionNoneEXT;
}

bool visible(vec3 origin, vec3 direction, float distance) {
    return visibility(origin,direction,distance,vec3(0.0),false);
}

vec3 skyRadiance(vec3 direction) {
    if (lighting.envFog.x>0.5) return environmentSky(rayEnvironment(),direction)*0.9;
    float horizon = mix(0.5, 1.0, clamp(direction.y, 0.0, 1.0));
    return mix(vec3(0.5, 0.65, 1.0), linearColor(lighting.skyColorIntensity.rgb), 0.35)
        * lighting.skyColorIntensity.w * horizon * 0.65;
}

vec3 sunlightTransport(vec3 position, vec3 normal, bool thin) {
    if (lighting.sunDirectionIntensity.w <= 0.0 || max(max(lighting.sunColor.r,lighting.sunColor.g),lighting.sunColor.b) <= 0.0
        || dot(lighting.sunDirectionIntensity.xyz,lighting.sunDirectionIntensity.xyz)<0.000001) return vec3(0.0);
    vec3 direction = normalize(lighting.sunDirectionIntensity.xyz);
    float cosine = dot(normal,direction);
    // Thin diffuse sheets have the same appearance from either side. Orienting a
    // leaf toward the eye must not switch its sunlight between full and transmitted.
    float diffuse = thin ? abs(cosine)*0.675 : max(cosine,0.0);
    if (diffuse <= 0.0) return vec3(0.0);
    return visible(rayOrigin(position,normal,direction), direction, 512.0)
        ? lighting.sunColor.rgb * lighting.sunDirectionIntensity.w * diffuse * 1.8 : vec3(0.0);
}

vec3 sunlight(vec3 position, vec3 normal, bool thin) {
    // Moving cloud shadows belong to direct surface shading. Cached bounce below
    // uses transport lighting so animation never invalidates or dirties mesh history.
    return sunlightTransport(position,normal,thin)*environmentCloudShadow(rayEnvironment(),position);
}

vec3 sunlight(vec3 position, vec3 normal) { return sunlight(position,normal,false); }

vec3 sunlightTransport(Surface surface) {
    if (!surfaceReceivesLight(surface,lighting.sunDirectionIntensity.xyz)) return vec3(0.0);
    return sunlightTransport(surface.position,surface.decorative ? vec3(0,1,0) : surface.normal,
        surface.thin && !surface.decorative);
}

vec3 sunlight(Surface surface) {
    return sunlightTransport(surface)*environmentCloudShadow(rayEnvironment(),surface.position);
}

float atmosphereVisibility(Environment e, vec3 position, vec3 eye) {
    vec3 delta = position-eye;
    float distance = length(delta);
    if (e.fog.x<0.5 || e.fog.y<=0.0 || e.camera.w>0.5 || e.sun.w<=0.001 ||
        distance<16.0 || e.fog.z<0.1) return 1.0;
    int samples = distance<48.0 ? 2 : 4;
    float light = 0.0;
    for (int i=0;i<4;i++) {
        if (i>=samples) break;
        vec3 p = eye+delta*((float(i)+0.5)/float(samples));
        light += visible(p,e.sun.xyz,512.0) ? 1.0 : 0.0;
    }
    return light/float(samples);
}

float distributionGGX(float NdotH, float roughness) {
    float a = roughness*roughness;
    float a2 = a*a;
    float d = NdotH*NdotH*(a2-1.0)+1.0;
    return a2/(3.14159265*d*d+1e-7);
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = roughness+1.0;
    float k = (r*r)/8.0;
    return NdotV/(NdotV*(1.0-k)+k);
}

float geometrySmith(float NdotV, float NdotL, float roughness) {
    return geometrySchlickGGX(NdotV,roughness)*geometrySchlickGGX(NdotL,roughness);
}

vec3 fresnelSchlick(float cosTheta, vec3 f0) {
    return f0+(1.0-f0)*pow(clamp(1.0-cosTheta,0.0,1.0),5.0);
}

vec3 specularBrdf(vec3 N, vec3 V, vec3 L, vec3 f0, float roughness) {
    vec3 H = normalize(V+L);
    float NdotL = max(dot(N,L),0.0);
    float NdotV = max(dot(N,V),1e-4);
    float NdotH = max(dot(N,H),0.0);
    float VdotH = max(dot(V,H),0.0);
    float D = distributionGGX(NdotH,roughness);
    float G = geometrySmith(NdotV,NdotL,roughness);
    vec3 F = fresnelSchlick(VdotH,f0);
    return D*G*F/max(4.0*NdotV*NdotL,1e-4);
}

struct LocalLighting { vec3 diffuse; vec3 specular; };

LocalLighting localLighting(vec3 position, Surface surface, vec3 viewDirection) {
    LocalLighting result;
    result.diffuse = vec3(0.0);
    result.specular = vec3(0.0);
    vec3 diffuseAlbedo = surface.albedo*(1.0-surface.metallic);
    for (int i = 0; i < int(lighting.parameters.x); i++) {
        PointLight light = lighting.pointLights[i];
        vec3 delta = light.positionRadius.xyz-position;
        float distance = length(delta);
        float radius = light.positionRadius.w;
        if (distance >= radius || distance <= 0.02) continue;
        vec3 direction = delta/distance;
        if (!surfaceReceivesLight(surface,direction)) continue;
        float cosine = max(dot(surface.normal, direction), 0.0);
        if (cosine <= 0.0) continue;
        // Ignore only this emitter's own surfaces; nearby walls still occlude it.
        if (visibility(rayOrigin(position,surface.geometricNormal,direction), direction,
                max(distance-0.0001, 0.00001),light.positionRadius.xyz,true)) {
            // A fire lights its immediate surroundings, rather than a radius-wide room.
            float relative = distance/radius;
            float window = max(1.0-relative*relative,0.0);
            float attenuation = 0.9*window*window/(1.0+0.22*distance*distance);
            vec3 radiance = linearColor(light.colorIntensity.rgb)*light.colorIntensity.w*attenuation;
            result.diffuse += radiance*diffuseAlbedo*cosine;
            result.specular += radiance*cosine*specularBrdf(surface.normal,viewDirection,direction,
                surface.f0,surface.roughness);
        }
    }
    // Adjacent fire tiles form one bright hearth; their overlap must not turn a cave into daylight.
    float peak = max(max(result.diffuse.r+result.specular.r,result.diffuse.g+result.specular.g),
        result.diffuse.b+result.specular.b);
    float scale = 1.0/(1.0+peak/1.2);
    result.diffuse *= scale;
    result.specular *= scale;
    return result;
}

vec3 localDiffuse(vec3 position, vec3 normal) {
    Surface surface;
    surface.albedo = vec3(1.0);
    surface.normal = normal;
    surface.geometricNormal = normal;
    surface.position = position;
    surface.emission = 0.0;
    surface.alpha = 1.0;
    surface.cutoff = 0.1;
    surface.skyLight = 1.0;
    surface.surfaceFlags = 0u;
    surface.tag = 0u;
    surface.thin = false;
    surface.decorative = false;
    surface.f0 = vec3(0.04);
    surface.roughness = 1.0;
    surface.metallic = 0.0;
    surface.ao = 1.0;
    surface.sss = 0.0;
    surface.porosity = 0.0;
    surface.height = 0.0;
    surface.transmission = 0.0;
    surface.ior = 1.5;
    surface.absorption = vec3(0.02);
    return localLighting(position,surface,vec3(0.0,0.0,1.0)).diffuse;
}

vec3 hemisphere(vec3 normal, float u, float v) {
    vec3 tangent = normalize(cross(normal, abs(normal.y)<0.99 ? vec3(0,1,0) : vec3(1,0,0)));
    vec3 bitangent = cross(normal, tangent);
    float angle = 6.28318530718*u;
    float radius = sqrt(v);
    return normalize(tangent*cos(angle)*radius + bitangent*sin(angle)*radius + normal*sqrt(1.0-v));
}

// Progressive low-discrepancy sequence: neighbouring coplanar vertices share directions.
// Spatial interpolation and accumulation happen on diffuse irradiance, never on texture pixels.
vec2 irradianceSample(uint index) {
    // Fixed-point R2 recurrence retains its distribution during long sessions;
    // multiplying a growing float index loses fractional precision over time.
    uvec2 point = uvec2(index)*uvec2(0xc13fa9a9u,0x91e10da5u)+uvec2(0x80000000u);
    return vec2(point>>8u)*(1.0/16777216.0);
}

vec3 diffuseSample(vec3 position, vec3 normal, uint index) {
    vec2 samplePoint = irradianceSample(index);
    vec3 direction = hemisphere(normal,samplePoint.x,samplePoint.y);
    Surface hit;
    if (!traceScene(rayOrigin(position,normal,direction),direction,256.0,hit)) return skyRadiance(direction);
    vec3 secondaryDirection = hemisphere(hit.normal,fract(samplePoint.x+0.381966),fract(samplePoint.y+0.414214));
    vec3 ambient = visible(rayOrigin(hit.position,hit.geometricNormal,secondaryDirection),secondaryDirection,256.0)
        ? skyRadiance(secondaryDirection) : vec3(0.0);
    if (hit.thin) {
        vec3 transmitted = visible(rayOrigin(hit.position,hit.geometricNormal,-secondaryDirection),-secondaryDirection,256.0)
            ? skyRadiance(-secondaryDirection) : vec3(0.0);
        ambient += transmitted*0.35;
    }
    vec3 diffuseAlbedo = hit.albedo*(1.0-hit.metallic);
    return diffuseAlbedo*(sunlightTransport(hit)*0.65+ambient*0.65+hit.emission);
}

vec3 readableAmbient(vec3 indirect, float skyLight) {
    float brightness = clamp(lighting.sunColor.w,0.0,1.0);
    // Keep shadowed terrain readable without filling sealed caves with outdoor light.
    // Raw skylight is independent of bonfires, camera fog, and irradiance cache age.
    float ambientMinimum = 0.035+brightness*0.12
        +0.20*clamp(skyLight,0.0,1.0)*lighting.skyColorIntensity.w;
    float indirectLuminance = dot(indirect,vec3(0.2126,0.7152,0.0722));
    return indirect+vec3(max(ambientMinimum-indirectLuminance,0.0));
}

vec3 displayRadiance(vec3 color) {
    if (LINEAR_OUTPUT) return max(color,vec3(0.0));
    float brightness = clamp(lighting.sunColor.w,0.0,1.0);
    // Devices without the HDR attachment/post path retain the legacy output.
    float exposure = lighting.envFog.z>0.001 ? clamp(lighting.envFog.z,0.35,1.6) : 1.0;
    vec3 x = max(color,vec3(0.0))*exposure;
    return pow(clamp(x/(x+vec3(0.6)),0.0,1.0),vec3(1.0/(2.2+brightness*0.6)));
}

#include "water_lighting.glsl"

vec4 shadeSurface(Surface s, vec3 incoming, vec3 indirect, float alpha, bool reflective, float skyLight,
                  vec3 directOverride, bool directOverrideValid) {
    bool advancedEnvironment = lighting.envFog.x>0.5;
    if (advancedEnvironment && (s.surfaceFlags & SURFACE_WATER)!=0u) {
        return shadeAdvancedWater(s,incoming,indirect,alpha,skyLight);
    }
    vec3 viewDirection = normalize(-incoming);
    vec3 diffuseAlbedo = s.albedo*(1.0-s.metallic);
    // Short decorative blades follow ground lighting, rather than alternating dark
    // vertical slivers as their crossed planes face toward and away from the camera.
    vec3 lightingNormal = s.decorative ? vec3(0,1,0) : s.normal;
    Surface lightingSurface = s;
    lightingSurface.normal = lightingNormal;
    vec3 sun = sunlight(lightingSurface);
    LocalLighting local;
    if (directOverrideValid) {
        local.diffuse = directOverride;
        local.specular = vec3(0.0);
    } else {
        local = localLighting(s.position,lightingSurface,viewDirection);
    }
    float ao = mix(1.0,clamp(s.ao,0.0,1.0),0.8);
    vec3 ambient = readableAmbient(indirect,skyLight)*diffuseAlbedo*ao;
    vec3 sunSpecular = vec3(0.0);
    if (!s.thin && !s.decorative) {
        vec3 sunDirection = normalize(lighting.sunDirectionIntensity.xyz);
        float sunNdotL = max(dot(s.normal,sunDirection),0.0);
        if (sunNdotL>0.0 && max(max(sun.r,sun.g),sun.b)>0.0) {
            vec3 sunRadiance = lighting.sunColor.rgb*lighting.sunDirectionIntensity.w*1.8;
            sunSpecular = specularBrdf(s.normal,viewDirection,sunDirection,s.f0,s.roughness)
                *sunRadiance*sunNdotL;
        }
    }
    vec3 environmentSpecular = skyRadiance(reflect(incoming,s.normal))*s.f0
        *(1.0-s.roughness*s.roughness)*0.5;
    vec3 color = sun*diffuseAlbedo+local.diffuse+local.specular+sunSpecular
        +environmentSpecular+ambient+s.albedo*s.emission*4.0;
    float wetness = advancedEnvironment
        ? surfaceWetness(s.surfaceFlags,s.tag,alpha,s.normal)*clamp(s.porosity+0.15,0.0,1.0)
        : 0.0;
    if (s.metallic>0.5 || s.emission>0.0) wetness = 0.0;
    color *= (1.0-0.18*wetness);
    // Water and glass receive a true scene reflection; opaque terrain remains diffuse.
    if (reflective && !s.thin && !s.decorative && (alpha < 0.99 || s.transmission > 0.01)) {
        vec3 direction = reflect(incoming,s.normal);
        vec3 reflection = surfaceReflection(s.position,s.geometricNormal,direction);
        vec3 fresnel = s.f0+(1.0-s.f0)*pow(1.0-clamp(dot(-incoming,s.normal),0.0,1.0),5.0);
        vec3 transmitted = vec3(0.0);
        if (s.transmission > 0.01) {
            vec3 refractedDirection = refract(incoming,s.normal,1.0/s.ior);
            if (dot(refractedDirection,refractedDirection) > 1e-4) {
                Surface behind;
                if (traceColorScene(rayOrigin(s.position,s.geometricNormal,refractedDirection),
                        refractedDirection,128.0,behind)) {
                    float distance = length(behind.position-s.position);
                    transmitted = tracedSurfaceRadiance(behind,refractedDirection)*exp(-s.absorption*distance);
                }
            }
        }
        // Translucent surfaces scatter less light than an opaque painted block.
        vec3 body = color*0.25+transmitted*s.transmission+s.albedo*s.emission*4.0;
        // Compose linear body and reflection, then tone map once. Texture opacity
        // must attenuate the body, not Fresnel.
        float maxFresnel = max(max(fresnel.r,fresnel.g),fresnel.b);
        float bodyWeight = (1.0-maxFresnel)*clamp(max(alpha,s.transmission),0.0,1.0);
        float opacity = maxFresnel+bodyWeight;
        vec3 composed = (body*bodyWeight+reflection*fresnel)/max(opacity,0.0001);
        return vec4(displayRadiance(composed),opacity);
    }
    vec3 displayed = displayRadiance(color);
    if (wetness>0.01) displayed = shadeWetSurface(displayed,s.position,s.normal,incoming,wetness);
    return vec4(displayed,alpha);
}
