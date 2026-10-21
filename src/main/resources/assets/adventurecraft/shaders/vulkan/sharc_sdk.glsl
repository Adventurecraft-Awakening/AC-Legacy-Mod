// MIT application integration. The following headers come from a separately supplied NVIDIA SDK.
#extension GL_EXT_shader_explicit_arithmetic_types_float16 : require
#extension GL_EXT_shader_16bit_storage : require
#extension GL_EXT_shader_atomic_int64 : require
#extension GL_EXT_control_flow_attributes : require
#define SHARC_ENABLE_GLSL 1
#define HASH_GRID_ENABLE_64_BIT_ATOMICS 1
#define HASH_GRID_COMPACT 0
#define SHARC_ENABLE_RESPONSIVE_LIGHTING 0
#define SHARC_ENABLE_SH_ENCODING 0
#define SHARC_MATERIAL_DEMODULATION 1
#define SHARC_SEPARATE_EMISSIVE 1
#define SHARC_USE_FP16 0
#define SHARC_ENABLE_CACHE_RESAMPLING 0
#define SHARC_PROPAGATION_DEPTH 4
// Resolve operates on independent entries. Avoid neighbor recovery reads racing
// another invocation's in-place writes; immutable snapshots are used by queries.
#define SHARC_BLEND_ADJACENT_LEVELS 0
#define SHARC_LINEAR_PROBE_WINDOW_SIZE 0
#include <SharcGlslHelpers.h>
#include <SharcCommon.h>

SharcParameters sharcCacheParameters(bool writeCache) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    SharcParameters cache;
    cache.hashGridParameters.cameraPosition = p.gridCamera.xyz;
    cache.hashGridParameters.logarithmBase = 2.0;
    cache.hashGridParameters.sceneScale = max(p.tuning.x,0.001);
    cache.hashGridParameters.levelBias = p.tuning.y;
    cache.hashGridData.capacity = p.dimensions.z;
    cache.hashGridData.hashEntriesBuffer = RWStructuredBuffer_uint64_t(writeCache ? p.hashWrite : p.queryHash);
    cache.radianceScale = max(p.tuning.z,1.0);
    cache.accumulationBuffer = RWStructuredBuffer_SharcAccumulationData(p.accumulation);
    cache.resolvedBuffer = RWStructuredBuffer_SharcPackedData(writeCache ? p.resolvedWrite : p.queryResolved);
    return cache;
}
SharcHitData sharcHit(Surface surface) {
    SharcFrameParameters p = SharcFrameParameters(sharcPush.parameters);
    SharcHitData hit;
    hit.positionWorld = surface.position+p.cacheOriginBias.xyz;
    hit.normalWorld = surface.geometricNormal;
    // Positive demodulation is required even for black and fully metallic texels.
    hit.materialDemodulation = max(sharcDiffuseAlbedo(surface),vec3(0.05));
    hit.emissive = sharcEmission(surface);
    return hit;
}
bool sharcLookup(SharcParameters cache, Surface surface, float segmentLength, out vec3 radiance) {
    if (solidOccluder(surface.tag)) { radiance = vec3(0.0); return false; }
    SharcHitData hit = sharcHit(surface);
    uint level = HashGridGetLevel(hit.positionWorld,cache.hashGridParameters);
    float voxelSize = HashGridGetVoxelSize(level,cache.hashGridParameters);
    // Only diffuse continuations use the cache. A ray must span more than the
    // cell diagonal, so a nearby surface cannot feed its own outgoing radiance
    // back into the same voxel and erase contact shadows or texture detail.
    if (segmentLength<=voxelSize*1.732050808) { radiance = vec3(0.0); return false; }
    bool found = SharcGetCachedRadiance(cache,hit,radiance,true);
    sharcStatistic(found ? 0u : 1u);
    return found;
}
