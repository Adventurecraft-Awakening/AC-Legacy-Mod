#ifndef AC_TEXTURE_REACTIVITY_GLSL
#define AC_TEXTURE_REACTIVITY_GLSL
#ifdef WORLD_REACTIVE_OUTPUTS

vec2 changedTextureFootprint(sampler2D image, vec2 uv) {
    // Both axes share implicit LOD. A V gradient can therefore widen the U
    // filter support, even when U is constant. Include the coarser trilinear
    // mip after sampler/view LOD clamping, using its actual (possibly odd) size.
    vec2 footprint = fwidth(uv)*0.5;
    float lod = textureQueryLod(image,uv).x;
    if (isnan(lod) || isinf(lod)) return vec2(1.0);
    int mip = int(clamp(ceil(lod),0.0,float(textureQueryLevels(image)-1)));
    // A full texel also covers the support of successively blitted odd-size mips.
    return footprint+1.0/vec2(textureSize(image,mip));
}

bool reactivePeriodicOverlap(float lower, float upper, float first, float last, float period) {
    return ceil((lower-last)/period)<=floor((upper-first)/period);
}

// Each bit denotes an addressed atlas column/row touched by the filter footprint.
// Periodic interval overlap also covers wide, negative, and seam-crossing UVs.
uint reactiveTextureAxis(float lower, float upper, uint addressing) {
    if (addressing==2u) {
        lower = clamp(lower,0.0,1.0);
        upper = clamp(upper,0.0,1.0);
    } else if (addressing==3u) {
        if (upper<0.0 || lower>1.0) return 0u;
        lower = max(lower,0.0);
        upper = min(upper,1.0);
    }
    uint result = 0u;
    for (uint tile=0u;tile<16u;tile++) {
        float first = float(tile)/16.0, last = float(tile+1u)/16.0;
        bool overlaps;
        if (addressing==0u) overlaps = reactivePeriodicOverlap(lower,upper,first,last,1.0);
        else if (addressing==1u) overlaps = reactivePeriodicOverlap(lower,upper,first,last,2.0)
            || reactivePeriodicOverlap(lower,upper,2.0-last,2.0-first,2.0);
        else overlaps = lower<=last && upper>=first;
        if (overlaps) result |= 1u<<tile;
    }
    return result;
}

// halfFootprint includes half a pixel's UV span and a sampled-mip texel radius.
// Large footprints intersect only the changed cells they actually cover; water
// animation must not discard unrelated minified foliage elsewhere in the atlas.
float changedTextureReactivity(uvec4 first, uvec4 second, uvec4 addressing,
                               vec2 uv, vec2 halfFootprint, bool validProjection) {
    if (all(equal(first,uvec4(0u))) && all(equal(second,uvec4(0u)))) return 0.0;
    // Incompatible companion views cannot use the albedo's addressing or footprint.
    if (addressing.z!=0u) return 1.0;
    if (!validProjection || any(isnan(uv)) || any(isinf(uv))
        || any(isnan(halfFootprint)) || any(isinf(halfFootprint))
        || any(lessThan(halfFootprint,vec2(0.0)))
        || any(greaterThan(addressing.xy,uvec2(3u)))) return 1.0;
    vec2 lower = uv-halfFootprint;
    vec2 upper = uv+halfFootprint;
    if (any(isnan(lower)) || any(isinf(lower)) || any(isnan(upper)) || any(isinf(upper))) return 1.0;
    uint columns = reactiveTextureAxis(lower.x,upper.x,addressing.x);
    uint rows = reactiveTextureAxis(lower.y,upper.y,addressing.y);
    if (columns==0u || rows==0u) return 0.0;
    for (uint word=0u;word<8u;word++) {
        uint mask = word<4u ? first[word] : second[word-4u];
        uint covered = ((rows&(1u<<(word*2u)))!=0u ? columns : 0u)
            | ((rows&(1u<<(word*2u+1u)))!=0u ? columns<<16u : 0u);
        if ((mask&covered)!=0u) return 1.0;
    }
    return 0.0;
}

#endif
#endif
