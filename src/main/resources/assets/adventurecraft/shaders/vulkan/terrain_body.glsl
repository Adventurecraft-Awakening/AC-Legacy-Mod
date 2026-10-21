layout(set = 3, binding = 0) uniform sampler2D terrainTexture;
layout(location = 0) in vec3 textureUv;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in vec4 eyePosition;
layout(location = 3) in vec3 worldPosition;
layout(location = 4) flat in vec3 eyeOrigin;
layout(location = 0) out vec4 outColor;
#ifdef WORLD_REACTIVE_OUTPUTS
layout(location = 1) out vec4 outReactive;
layout(push_constant, std430) uniform TemporalDrawPush {
    layout(offset = 64) vec4 temporal;
    layout(offset = 80) uvec4 changedTiles0;
    layout(offset = 96) uvec4 changedTiles1;
    layout(offset = 112) uvec4 textureAddressing;
} temporalDraw;
#endif
#include "texture_reactivity.glsl"
#include "draw_constants.glsl"
#include "state_modes.glsl"
#include "environment.glsl"
layout(constant_id = 0) const bool USE_LIGHTING = true;
layout(constant_id = 1) const bool USE_FOG = true;
bool alphaPasses(float alpha, float reference, int comparison) {
    if (comparison == COMPARE_NEVER) return false;
    if (comparison == COMPARE_LESS) return alpha < reference;
    if (comparison == COMPARE_EQUAL) return alpha == reference;
    if (comparison == COMPARE_LESS_EQUAL) return alpha <= reference;
    if (comparison == COMPARE_GREATER) return alpha > reference;
    if (comparison == COMPARE_NOT_EQUAL) return alpha != reference;
    if (comparison == COMPARE_GREATER_EQUAL) return alpha >= reference;
    return true;
}
void main() {
#ifdef WORLD_REACTIVE_OUTPUTS
    float textureReactive = 0.0;
    // The mask is uniform for this draw. Skip LOD queries for unchanged images;
    // evaluate derivatives before any alpha discard or early return.
    if (any(notEqual(temporalDraw.changedTiles0,uvec4(0u)))
        || any(notEqual(temporalDraw.changedTiles1,uvec4(0u)))) {
        vec2 reactiveUv = textureUv.xy/textureUv.z;
        vec2 reactiveFootprint = changedTextureFootprint(terrainTexture,reactiveUv);
        textureReactive = changedTextureReactivity(temporalDraw.changedTiles0,temporalDraw.changedTiles1,
            temporalDraw.textureAddressing,reactiveUv,reactiveFootprint,
            !any(isnan(textureUv)) && !any(isinf(textureUv)) && textureUv.z!=0.0);
    }
    float baseReactive = material.params.z< -0.5 ? 0.5 : clamp(temporalDraw.temporal.z,0.0,1.0);
    outReactive = vec4(max(baseReactive,textureReactive),0.0,0.0,1.0);
#endif
    if (material.params.y > 0.5) {
        outColor = vec4(authoredColor(draw.tint.rgb),draw.tint.a);
        return;
    }
    Environment environment = Environment(frame.envSun,frame.envSky,frame.envWeather,
        frame.envCamera,frame.envClouds,frame.envFog);
    if (material.params.z>1.5) {
        outColor = environmentClouds(environment,worldPosition,1536.0);
#ifdef WORLD_REACTIVE_OUTPUTS
        // Procedural clouds share the distant background class so foreground cutout coverage can
        // accumulate over them. Zero-opacity samples preserve the destination mask under MAX blending.
        // Camera motion still rejects mixed silhouette history; cloud flow uses bounded color clipping.
        outReactive = vec4(outColor.a>0.0 ? 0.5 : 0.0,0.0,0.0,1.0);
#endif
        return;
    }
    if (material.params.z>0.5) {
        outColor = vec4(environmentDisplay(environmentSky(environment,worldPosition)),1.0);
#ifdef WORLD_REACTIVE_OUTPUTS
        // The atmosphere is infinite background. Celestial geometry keeps its ordinary reactive mask.
        outReactive = vec4(max(0.5,textureReactive),0.0,0.0,1.0);
#endif
        return;
    }
    vec4 texel = textureProj(terrainTexture, textureUv);
    vec4 color = vec4(authoredColor(texel.rgb),texel.a) * vertexColor;
    if (material.alpha.x > 0.5 && !alphaPasses(color.a, material.alpha.y, int(material.alpha.z))) discard;
    if (!USE_FOG) { outColor = color; return; }
    float visibility = 1.0;
    int distanceMode = int(frame.modes.y);
    float fogDistance = distanceMode == FOG_DISTANCE_VERTEX ? eyePosition.w
        : distanceMode == FOG_DISTANCE_FRAGMENT ? length(eyePosition.xyz) : abs(eyePosition.z);
    int mode = int(frame.fog.x);
    if (mode == FOG_LINEAR) {
        float range = frame.fog.z - frame.fog.y;
        visibility = abs(range) > 0.000001 ? (frame.fog.z - fogDistance) / range : (fogDistance <= frame.fog.z ? 1.0 : 0.0);
    } else if (mode == FOG_EXPONENTIAL) {
        visibility = exp(-frame.fog.w * fogDistance);
    } else if (mode == FOG_EXPONENTIAL_SQUARED) {
        float density = frame.fog.w * fogDistance;
        visibility = exp(-density * density);
    }
    environment.camera.xyz += eyeOrigin;
    outColor = vec4(environmentFog(environment,color.rgb,worldPosition-eyeOrigin,eyePosition.xyz,
        visibility,frame.fogColor.rgb,environment.fog.z),color.a);
}
