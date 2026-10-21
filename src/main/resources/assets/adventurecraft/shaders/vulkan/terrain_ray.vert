#version 460
#include "ray_lighting.glsl"
#define READ_ONLY_CACHE
#define TEMPORAL_TERRAIN_VERTEX
#include "irradiance_cache.glsl"
#include "temporal_jitter.glsl"
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec4 color;
layout(location = 4) in uint surfaceMaterial;
layout(location = 0) out vec3 textureUv;
layout(location = 1) out vec4 vertexColor;
layout(location = 2) out vec4 eyePosition;
layout(location = 3) out vec3 worldPosition;
layout(location = 4) flat out uint materialTag;
layout(location = 5) out vec3 fallbackNormal;
layout(location = 6) flat out vec3 eyeOrigin;
layout(location = 7) out vec3 indirectIrradiance;
layout(location = 8) out float terrainSkyLight;
layout(location = 9) flat out uint surfaceFlags;
#include "draw_constants.glsl"
layout(constant_id = 0) const bool USE_LIGHTING = false;
layout(constant_id = 1) const bool USE_FOG = true;
void main() {
    vec4 world = draw.model * vec4(position, 1.0);
    vec4 eye = frame.view * world;
    gl_Position = frame.projection * eye;
    gl_Position = temporalClipPosition(gl_Position,cacheJob.temporal);
    gl_PointSize = 1.0;
    vec4 tex = draw.textureMatrix * vec4(uv, 0.0, 1.0);
    textureUv = vec3(tex.xy, tex.w);
    vertexColor = color * draw.tint;
    eyePosition = vec4(eye.xyz, length(eye.xyz));
    worldPosition = world.xyz;
    materialTag = surfaceMaterial;
    eyeOrigin = inverse(frame.view)[3].xyz;
    uint vertex = uint(gl_VertexIndex)-cacheJob.baseVertex;
    Vertices vertices = Vertices(cacheJob.vertices);
    uint corner = vertex%4u;
    uint expanded = vertex/4u*6u+(corner==3u ? 5u : corner);
    uint skyTag = vertices.words[expanded*8u+6u]>>24u;
    surfaceFlags = skyTag;
    terrainSkyLight = (skyTag & 16u)!=0u ? float(skyTag & 15u)/15.0 : 0.0;
    vec3 geometricNormal = normalize(mat3(draw.model)*cacheNormal(vertices,vertex/4u));
    fallbackNormal = geometricNormal;
    VertexIrradiance cached = Irradiance(cacheJob.irradiance).values[vertex];
    bool front = dot(geometricNormal,world.xyz-eyeOrigin)<=0.0;
    vec4 facing = front ? cached.front : cached.back;
    vec4 opposite = front ? cached.back : cached.front;
    vec3 oriented = front ? geometricNormal : -geometricNormal;
    vec3 ambientFallback = skyRadiance(oriented)*0.25;
    indirectIrradiance = facing.w>0.0 ? facing.rgb : ambientFallback;
    if ((surfaceMaterial & DECORATIVE_GRASS)!=0u) {
        indirectIrradiance = cached.front.w>0.0 ? cached.front.rgb : skyRadiance(vec3(0,1,0))*0.25;
    } else if (thinSurface(surfaceMaterial)) {
        vec3 transmitted = opposite.w>0.0 ? opposite.rgb : skyRadiance(-oriented)*0.25;
        indirectIrradiance = (indirectIrradiance+transmitted)*0.5;
    }
}
