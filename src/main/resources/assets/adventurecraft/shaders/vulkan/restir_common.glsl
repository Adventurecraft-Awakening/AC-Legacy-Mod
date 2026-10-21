#include "ray_lighting.glsl"
#include "restir_gbuffer.glsl"

layout(set = 0, binding = 0) uniform sampler2D restirGbuffer[8];

struct RestirEmitter {
    vec4 positionRadius;
    vec4 colorIntensity;
    uvec2 stableId;
    float halfExtent;
    float cdf;
};
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer RestirEmitters {
    uvec4 header;
    RestirEmitter emitters[];
};

// W is the reservoir's final unbiased RIS normalization, not its selection
// weight. A stored sampleData is a stable emitter ID and unit-cube face coordinates.
struct RestirReservoir {
    uvec4 key; // stable ID low/high, emitter index hint, effective candidate count
    vec4 sampleData; // unit face U/V, face number (negative for point sources), reserved
    vec4 weights; // streaming weight sum, W, selected target, temporal age
};
layout(buffer_reference, std430, buffer_reference_align = 16) buffer RestirReservoirs {
    RestirReservoir values[];
};
layout(buffer_reference, std430, buffer_reference_align = 16) buffer RestirRadiance {
    vec4 values[];
};
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer RestirParameters {
    mat4 previousViewProjection;
    vec4 originDelta;
    vec4 eyeOrigin;
    uvec4 dimensions; // width, height, reservoir bit 0/TAA bit 1/color history bit 2/debug bits 8..9, sequence
    vec4 settings; // initial candidates, temporal candidate cap, spatial neighbors, pixel radius
};
layout(push_constant, std430) uniform RestirPush {
    uint64_t emitterAddress;
    uint64_t initialAddress;
    uint64_t previousAddress;
    uint64_t spatialAddress;
    uint64_t rawAddress;
    uint64_t filteredAddress;
    uint64_t previousFilteredAddress;
    uint64_t parameterAddress;
} restirPush;

struct RestirSurface {
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

uint restirHash(uint value) {
    value ^= value>>16u;
    value *= 0x7feb352du;
    value ^= value>>15u;
    value *= 0x846ca68bu;
    return value^(value>>16u);
}

float restirRandom(inout uint state) {
    state = restirHash(state+0x9e3779b9u);
    return float(state>>8u)*(1.0/16777216.0);
}

uint restirPixelIndex(ivec2 pixel) {
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    return uint(pixel.y)*parameters.dimensions.x+uint(pixel.x);
}

bool restirInBounds(ivec2 pixel) {
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    return all(greaterThanEqual(pixel,ivec2(0)))
        && all(lessThan(pixel,ivec2(parameters.dimensions.xy)));
}

RestirSurface restirSurface(ivec2 pixel, bool previous) {
    // Fixed descriptor indices need no additional dynamic-indexing feature.
    vec4 position = previous ? texelFetch(restirGbuffer[4],pixel,0) : texelFetch(restirGbuffer[0],pixel,0);
    vec4 normal = previous ? texelFetch(restirGbuffer[5],pixel,0) : texelFetch(restirGbuffer[1],pixel,0);
    vec4 albedo = previous ? texelFetch(restirGbuffer[6],pixel,0) : texelFetch(restirGbuffer[2],pixel,0);
    vec4 f0 = previous ? texelFetch(restirGbuffer[7],pixel,0) : texelFetch(restirGbuffer[3],pixel,0);
    RestirSurface result;
    result.position = position.xyz;
    result.normal = restirOctDecode(normal.xy);
    result.geometricNormal = restirOctDecode(normal.zw);
    result.albedo = albedo.rgb;
    result.metallic = albedo.a;
    result.f0 = f0.rgb;
    result.roughness = max(f0.a,0.02);
    result.flags = uint(round(max(position.w,0.0)));
    result.valid = (result.flags&RESTIR_VALID)!=0u;
    return result;
}

bool restirCompatible(RestirSurface current, RestirSurface other) {
    if (!current.valid || !other.valid || current.flags!=other.flags) return false;
    // Reservoirs store light proposals, not shaded colors. Different texels,
    // shading normals and BRDFs remain reusable through target reevaluation and
    // MIS; rejecting them destroys history on detailed walls during motion.
    if (dot(current.geometricNormal,other.geometricNormal)<0.95) return false;
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    float distance = length(current.position-parameters.eyeOrigin.xyz);
    vec3 delta = other.position-current.position;
    float planeTolerance = 0.025+distance*0.0015;
    return abs(dot(delta,current.geometricNormal))<=planeTolerance
        && length(delta)<=max(0.2,distance*0.04);
}

bool restirFilterCompatible(RestirSurface current, RestirSurface other) {
    if (!restirCompatible(current,other) || dot(current.normal,other.normal)<0.92) return false;
    if (abs(current.metallic-other.metallic)>0.05 || abs(current.roughness-other.roughness)>0.12) return false;
    if (dot(current.albedo-other.albedo,current.albedo-other.albedo)>0.025
        || dot(current.f0-other.f0,current.f0-other.f0)>0.015) return false;
    return true;
}

// Color history needs the fractional footprint, independently of which single
// reservoir is selected for reuse. Validate every contributing history tap.
bool restirDenoiseReproject(RestirSurface surface, out vec2 previousUv) {
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    previousUv = vec2(0.0);
    if ((parameters.dimensions.z&4u)==0u) return false;
    vec4 clip = parameters.previousViewProjection*vec4(surface.position+parameters.originDelta.xyz,1.0);
    if (any(isnan(clip)) || any(isinf(clip)) || clip.w<=0.000001) return false;
    vec3 ndc = clip.xyz/clip.w;
    if (ndc.z<0.0 || ndc.z>1.0 || any(greaterThan(abs(ndc.xy),vec2(1.0)))) return false;
    previousUv = ndc.xy*0.5+0.5;
    return true;
}

bool restirReproject(RestirSurface surface, out ivec2 previousPixel) {
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    previousPixel = ivec2(-1);
    if ((parameters.dimensions.z&1u)==0u) return false;
    vec4 clip = parameters.previousViewProjection*vec4(surface.position+parameters.originDelta.xyz,1.0);
    if (clip.w<=0.000001) return false;
    vec3 ndc = clip.xyz/clip.w;
    if (ndc.z<0.0 || ndc.z>1.0 || any(greaterThan(abs(ndc.xy),vec2(1.0)))) return false;
    ivec2 base = ivec2(floor((ndc.xy*0.5+0.5)*vec2(parameters.dimensions.xy)-0.5));
    float closest = 1e30;
    for (int y=0;y<2;y++) for (int x=0;x<2;x++) {
        ivec2 candidate = base+ivec2(x,y);
        if (!restirInBounds(candidate)) continue;
        RestirSurface previous = restirSurface(candidate,true);
        previous.position -= parameters.originDelta.xyz;
        if (!restirCompatible(surface,previous)) continue;
        float distance = dot(previous.position-surface.position,previous.position-surface.position);
        if (distance<closest) { closest = distance; previousPixel = candidate; }
    }
    return previousPixel.x>=0;
}

bool restirIdLess(uvec2 left, uvec2 right) {
    // CPU records are sorted by signed Java long, with unsigned low halves.
    uint a = left.y^0x80000000u, b = right.y^0x80000000u;
    return a<b || (a==b && left.x<right.x);
}

int restirFindEmitter(uvec2 id, uint hint) {
    RestirEmitters source = RestirEmitters(restirPush.emitterAddress);
    uint count = source.header.x;
    if (hint<count && all(equal(source.emitters[hint].stableId,id))) return int(hint);
    uint lo = 0u, hi = count;
    while (lo<hi) {
        uint mid = lo+(hi-lo)/2u;
        if (restirIdLess(source.emitters[mid].stableId,id)) lo = mid+1u;
        else hi = mid;
    }
    return lo<count && all(equal(source.emitters[lo].stableId,id)) ? int(lo) : -1;
}

uint restirSampleEmitter(float uniformSample, out float pdf) {
    RestirEmitters source = RestirEmitters(restirPush.emitterAddress);
    uint lo = 0u, hi = source.header.x;
    while (lo<hi) {
        uint mid = lo+(hi-lo)/2u;
        if (source.emitters[mid].cdf<=uniformSample) lo = mid+1u;
        else hi = mid;
    }
    uint index = min(lo,source.header.x-1u);
    pdf = max(source.emitters[index].cdf-(index==0u ? 0.0 : source.emitters[index-1u].cdf),0.0);
    return index;
}

vec3 restirEmitterPosition(RestirEmitter light, vec4 sampleData, out vec3 emitterNormal) {
    emitterNormal = vec3(0.0);
    if (light.halfExtent<=0.0) return light.positionRadius.xyz;
    int face = clamp(int(sampleData.z),0,5);
    vec2 uv = clamp(sampleData.xy,vec2(0.0),vec2(1.0))*2.0-1.0;
    float side = (face&1)==0 ? -1.0 : 1.0;
    vec3 offset;
    if (face<2) { emitterNormal = vec3(side,0,0); offset = vec3(side,uv); }
    else if (face<4) { emitterNormal = vec3(0,side,0); offset = vec3(uv.x,side,uv.y); }
    else { emitterNormal = vec3(0,0,side); offset = vec3(uv,side); }
    return light.positionRadius.xyz+offset*light.halfExtent;
}

vec3 restirEvaluate(RestirSurface surface, RestirEmitter light, vec4 sampleData) {
    vec3 emitterNormal;
    vec3 emitterPosition = restirEmitterPosition(light,sampleData,emitterNormal);
    vec3 delta = emitterPosition-surface.position;
    float distance = length(delta), radius = light.positionRadius.w;
    if (distance<=0.00001 || radius<=0.0 || distance>=radius) return vec3(0.0);
    vec3 direction = delta/distance;
    if ((surface.flags&(RESTIR_THIN|RESTIR_DECORATIVE))==0u
        && dot(surface.geometricNormal,direction)<=0.0) return vec3(0.0);
    float cosine = max(dot(surface.normal,direction),0.0);
    if (cosine<=0.0) return vec3(0.0);
    float relative = distance/radius;
    float window = max(1.0-relative*relative,0.0);
    float attenuation = 0.9*window*window/(1.0+0.22*distance*distance);
    vec3 radiance = linearColor(light.colorIntensity.rgb)*max(light.colorIntensity.w,0.0)*attenuation;
    // Unit face UVs use a discrete 1/6 face PDF. The physical face-area
    // Jacobian cancels a constant-power cube's area normalization. This keeps
    // emitter-size changes and temporal sampleData retargeting in the same domain.
    if (light.halfExtent>0.0) radiance *= (2.0/3.0)*max(dot(emitterNormal,-direction),0.0);
    RestirParameters parameters = RestirParameters(restirPush.parameterAddress);
    vec3 view = parameters.eyeOrigin.xyz-surface.position;
    view = length(view)>0.000001 ? normalize(view) : surface.normal;
    vec3 brdf = surface.albedo*(1.0-surface.metallic)
        +specularBrdf(surface.normal,view,direction,surface.f0,surface.roughness);
    return max(radiance*cosine*brdf,vec3(0.0));
}

float restirTarget(vec3 contribution) {
    return max(dot(contribution,vec3(0.2126,0.7152,0.0722)),0.0);
}

float restirVisibleTarget(RestirSurface surface, RestirEmitter light, vec4 sampleData) {
    float target = restirTarget(restirEvaluate(surface,light,sampleData));
    if (target<=0.0) return 0.0;
    vec3 emitterNormal;
    vec3 point = restirEmitterPosition(light,sampleData,emitterNormal);
    vec3 delta = point-surface.position;
    float distance = length(delta);
    return visibility(rayOrigin(surface.position,surface.geometricNormal,delta/distance),delta/distance,
        max(distance-0.0001,0.00001),light.positionRadius.xyz,true) ? target : 0.0;
}

RestirReservoir restirEmpty() {
    RestirReservoir result;
    result.key = uvec4(0u);
    result.sampleData = vec4(0.0);
    result.weights = vec4(0.0);
    return result;
}

bool restirUpdate(inout RestirReservoir reservoir, uvec2 id, uint index, vec4 sampleData,
                  float target, float weight, uint candidates, float age, inout uint rng) {
    reservoir.key.w += candidates;
    if (weight<=0.0 || isnan(weight) || isinf(weight)) return false;
    reservoir.weights.x += weight;
    if (restirRandom(rng)*reservoir.weights.x<weight) {
        reservoir.key.xyz = uvec3(id,index);
        reservoir.sampleData = sampleData;
        reservoir.weights.z = target;
        reservoir.weights.w = age;
        return true;
    }
    return false;
}

void restirNormalize(inout RestirReservoir reservoir) {
    reservoir.weights.y = reservoir.key.w>0u && reservoir.weights.z>0.0
        ? reservoir.weights.x/(float(reservoir.key.w)*reservoir.weights.z) : 0.0;
    if (isnan(reservoir.weights.y) || isinf(reservoir.weights.y)) reservoir = restirEmpty();
}

void restirNormalizeMis(inout RestirReservoir reservoir, float numerator, float denominator) {
    // Source-aware balance normalization for reservoir reuse. Unlike 1/M,
    // this handles zero BRDF/range/visibility support at contributing pixels
    // without a tiny artificial target and its unbounded retargeting weights.
    // Bitterli et al. (2020), supplemental Algorithm 1:
    // https://benedikt-bitterli.me/restir/bitterli20restir-supplemental.pdf
    reservoir.weights.y = denominator>0.0 && reservoir.weights.z>0.0
        ? reservoir.weights.x*numerator/(denominator*reservoir.weights.z) : 0.0;
    if (isnan(reservoir.weights.y) || isinf(reservoir.weights.y)) reservoir = restirEmpty();
}

uint restirReuseCount(RestirReservoir source, uint countCap) {
    uint count = min(source.key.w,countCap);
    if (count==0u || source.weights.z<=0.0) return count;
    int index = restirFindEmitter(source.key.xy,source.key.z);
    if (index<0) return 0u;
    RestirEmitters emitters = RestirEmitters(restirPush.emitterAddress);
    // Point samples and cube face samples have different proposal domains.
    // Keep current candidates when a stable source changes between those domains.
    return (source.sampleData.z<0.0)!=(emitters.emitters[index].halfExtent<=0.0) ? 0u : count;
}

bool restirMerge(inout RestirReservoir result, RestirReservoir source, RestirSurface surface,
                 uint countCap, inout uint rng) {
    uint count = restirReuseCount(source,countCap);
    if (count==0u) return false;
    if (source.weights.y<=0.0) { result.key.w += count; return false; }
    int index = restirFindEmitter(source.key.xy,source.key.z);
    RestirEmitters emitters = RestirEmitters(restirPush.emitterAddress);
    float target = restirTarget(restirEvaluate(surface,emitters.emitters[index],source.sampleData));
    return restirUpdate(result,source.key.xy,uint(index),source.sampleData,target,
        target*source.weights.y*float(count),count,source.weights.w+1.0,rng);
}
