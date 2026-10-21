// Sparse live overrides for four 16x16 terrain atlases. This std430 record is
// exactly 64 bytes and mirrors the CPU material upload at set 4, binding 4.
struct TileMaterial {
    vec4 appearance; // smoothness, F0, metallic, porosity
    vec4 optics; // SSS, emission, transmission, IOR
    vec4 absorption; // RGB absorption, parallax enabled
    uvec4 control; // explicit-field mask, reserved
};
layout(set = 4, binding = 4, std430) readonly buffer TileMaterialTable {
    TileMaterial tileMaterials[];
};

const uint TILE_SMOOTHNESS = 1u;
const uint TILE_F0 = 2u;
const uint TILE_METALLIC = 4u;
const uint TILE_POROSITY = 8u;
const uint TILE_SSS = 16u;
const uint TILE_EMISSION = 32u;
const uint TILE_TRANSMISSION = 64u;
const uint TILE_IOR = 128u;
const uint TILE_ABSORPTION_R = 256u;
const uint TILE_ABSORPTION_G = 512u;
const uint TILE_ABSORPTION_B = 1024u;
const uint TILE_PARALLAX = 2048u;

uint tileMaterialIndex(uint textureBase, vec2 uv) {
    // Match the repeating atlas sampler, including shared tile boundaries.
    uvec2 tile = uvec2(clamp(floor(fract(uv)*16.0),vec2(0.0),vec2(15.0)));
    return min(textureBase/3u,3u)*256u+tile.y*16u+tile.x;
}

bool tileParallaxEnabled(uint textureBase, vec2 uv) {
    TileMaterial tile = tileMaterials[tileMaterialIndex(textureBase,uv)];
    return (tile.control.x&TILE_PARALLAX)==0u || tile.absorption.w>0.5;
}

void applyTileMaterial(uint textureBase, vec2 uv, vec3 authoredAlbedo, inout Surface s) {
    TileMaterial tile = tileMaterials[tileMaterialIndex(textureBase,uv)];
    uint mask = tile.control.x;
    if ((mask&TILE_SMOOTHNESS)!=0u) {
        s.roughness = clamp(pow(1.0-clamp(tile.appearance.x,0.0,1.0),2.0),0.02,1.0);
    }
    if ((mask&TILE_METALLIC)!=0u) {
        bool wasMetal = s.metallic>0.5;
        s.metallic = tile.appearance.z>0.5 ? 1.0 : 0.0;
        // Fixed-conductor LabPBR entries replace albedo during decoding. An
        // explicit material choice must recover the map's original color.
        s.albedo = authoredAlbedo;
        if (s.metallic>0.5) s.f0 = authoredAlbedo;
        else if (wasMetal) s.f0 = vec3(0.04);
    }
    if ((mask&TILE_F0)!=0u) s.f0 = vec3(clamp(tile.appearance.y,0.0,1.0));
    if ((mask&TILE_POROSITY)!=0u) s.porosity = clamp(tile.appearance.w,0.0,1.0);
    if ((mask&TILE_SSS)!=0u) s.sss = clamp(tile.optics.x,0.0,1.0);
    if ((mask&TILE_EMISSION)!=0u) {
        // Replace texture emission, while preserving required block emission.
        s.emission = max(materialEmission(s.tag),clamp(tile.optics.y,0.0,1.0));
    }
    if ((mask&TILE_TRANSMISSION)!=0u) s.transmission = clamp(tile.optics.z,0.0,1.0);

    bool water = (s.surfaceFlags&0x20u)!=0u;
    s.ior = water ? 1.333 : 1.5;
    s.absorption = water
        ? (vec3(0.045)+(vec3(1.0)-sqrt(clamp(s.albedo,0.0,1.0)))*0.24)
            *mix(0.7,1.35,clamp(s.alpha,0.0,1.0))
        : (vec3(1.0)-clamp(s.albedo,0.0,1.0))*0.2+vec3(0.02);
    if ((mask&TILE_IOR)!=0u) s.ior = max(tile.optics.w,1.0);
    if ((mask&TILE_ABSORPTION_R)!=0u) s.absorption.r = max(tile.absorption.r,0.0);
    if ((mask&TILE_ABSORPTION_G)!=0u) s.absorption.g = max(tile.absorption.g,0.0);
    if ((mask&TILE_ABSORPTION_B)!=0u) s.absorption.b = max(tile.absorption.b,0.0);
}
