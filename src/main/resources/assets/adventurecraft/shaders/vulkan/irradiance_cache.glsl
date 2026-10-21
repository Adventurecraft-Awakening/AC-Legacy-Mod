// The same push ABI is used by the compute updater and terrain vertex shader.
layout(push_constant, std430) uniform IrradianceJob {
    uint64_t irradiance;
    uint64_t vertices;
    uint baseVertex;
    uint sampleOffset;
    uint sampleCount;
    uint vertexCount;
#ifdef TEMPORAL_TERRAIN_VERTEX
    layout(offset = 64) vec4 temporal;
#endif
} cacheJob;
struct VertexIrradiance { vec4 front; vec4 back; };
#ifdef READ_ONLY_CACHE
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer Irradiance {
#else
layout(buffer_reference, std430, buffer_reference_align = 16) buffer Irradiance {
#endif
    VertexIrradiance values[];
};

vec3 cachePosition(Vertices vertices, uint index) {
    uint at = index*8u;
    return uintBitsToFloat(uvec3(vertices.words[at],vertices.words[at+1u],vertices.words[at+2u]));
}

vec3 cacheNormal(Vertices vertices, uint quad) {
    vec3 a = cachePosition(vertices,quad*6u);
    vec3 b = cachePosition(vertices,quad*6u+1u);
    vec3 c = cachePosition(vertices,quad*6u+2u);
    vec3 normal = cross(b-a,c-a);
    float squareLength = dot(normal,normal);
    if (squareLength<=1e-20) {
        vec3 d = cachePosition(vertices,quad*6u+5u);
        normal = cross(c-a,d-a);
        squareLength = dot(normal,normal);
    }
    return squareLength>1e-20 ? normal*inversesqrt(squareLength) : vec3(0,1,0);
}
