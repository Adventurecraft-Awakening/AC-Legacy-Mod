// Application-owned guide ABI. Motion is unjittered current-to-previous pixels;
// positions are camera-relative, with origin translation included only for surfaces.
layout(set = 0, binding = 10, std140) uniform ReconstructionParameters {
    mat4 inverseJitteredViewProjection;
    mat4 previousViewProjection;
    mat4 worldToView;
    vec4 originDelta;
    uvec4 dimensions; // width, height, history valid, reserved
    vec4 jitter; // current clip-space jitter XY, unused ZW
    vec4 eyeOrigin;
} reconstruction;

vec2 reconstructionMotion(vec3 position, vec2 rawUv, bool background) {
    if (reconstruction.dimensions.z==0u) return vec2(0.0);
    vec4 previous = reconstruction.previousViewProjection*(background
        ? vec4(position-reconstruction.eyeOrigin.xyz,0.0)
        : vec4(position+reconstruction.originDelta.xyz,1.0));
    if (any(isnan(previous)) || any(isinf(previous)) || previous.w<=0.000001) return vec2(0.0);
    vec2 previousUv = previous.xy/previous.w*0.5+0.5;
    vec2 currentUv = rawUv-reconstruction.jitter.xy*0.5;
    return clamp((previousUv-currentUv)*vec2(reconstruction.dimensions.xy),vec2(-32752.0),vec2(32752.0));
}
