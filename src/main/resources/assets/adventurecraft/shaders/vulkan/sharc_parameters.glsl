// Application-owned ABI. NVIDIA SDK declarations are restricted to optional compute kernels.
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer SharcFrameParameters {
    uint64_t hashWrite;
    uint64_t accumulation;
    uint64_t resolvedWrite;
    uint64_t queryHash;
    uint64_t queryResolved;
    uint64_t emitters;
    uint64_t rawGI;
    uint64_t filteredGI;
    uint64_t previousFilteredGI;
    uint64_t stats;
    vec4 cacheOriginBias;
    vec4 gridCamera;
    vec4 previousGridCamera;
    mat4 previousViewProjection;
    vec4 originDelta;
    vec4 eyeOrigin;
    uvec4 dimensions; // width, height, cache capacity, sequence
    vec4 tuning; // scene scale, level bias, radiance quantization scale, query blend
    uvec4 budget; // update downscale, diffuse path depth, accumulation frames, stale frames
    uvec4 control; // flags: screen history 1/query 2/TAA 4; resolve start, count, debug mode
    uvec4 quality; // diffuse query samples, spatial filter radius, reserved, reserved
};
layout(buffer_reference, std430, buffer_reference_align = 16) buffer SharcRadianceBuffer { vec4 values[]; };
layout(buffer_reference, std430, buffer_reference_align = 16) buffer SharcStatistics { uint values[]; };
layout(push_constant, std430) uniform SharcApplicationPush { uint64_t parameters; } sharcPush;

uint sharcHash(uint value) {
    value ^= value>>16u;
    value *= 0x7feb352du;
    value ^= value>>15u;
    value *= 0x846ca68bu;
    return value^(value>>16u);
}
float sharcRandom(inout uint state) {
    state = sharcHash(state+0x9e3779b9u);
    return float(state>>8u)*(1.0/16777216.0);
}
// A pixel/bounce scramble stays fixed while the R2 sequence advances through frames.
// Integer recurrence avoids the fractional precision loss of a growing float index.
vec2 sharcDiffuseSample(uint sampleIndex, uint scramble) {
    uvec2 point = sampleIndex*uvec2(0xc13fa9a9u,0x91e10da5u)
        +uvec2(sharcHash(scramble),sharcHash(scramble^0x68bc21ebu));
    return vec2(point>>8u)*(1.0/16777216.0);
}
void sharcStatistic(uint index) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    if (p.stats!=uint64_t(0)) atomicAdd(SharcStatistics(p.stats).values[index],1u);
}
bool sharcInBounds(ivec2 pixel) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    return all(greaterThanEqual(pixel,ivec2(0))) && all(lessThan(pixel,ivec2(p.dimensions.xy)));
}
uint sharcPixelIndex(ivec2 pixel) {
    return uint(pixel.y)*SharcFrameParameters(sharcPush.parameters).dimensions.x+uint(pixel.x);
}
