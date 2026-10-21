#include "ray_lighting.glsl"
#include "restir_gbuffer.glsl"
layout(set = 3, binding = 0) uniform sampler2D terrainTexture;
layout(location = 0) in vec3 textureUv;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in vec4 eyePosition;
layout(location = 3) in vec3 worldPosition;
layout(location = 4) flat in uint materialTag;
layout(location = 5) in vec3 fallbackNormal;
layout(location = 6) flat in vec3 eyeOrigin;
layout(location = 7) in vec3 indirectIrradiance;
layout(location = 8) in float terrainSkyLight;
layout(location = 9) flat in uint surfaceFlags;
layout(location = 0) out vec4 outColor;
#ifdef WORLD_REACTIVE_OUTPUTS
layout(location = 1) out vec4 outReactive;
#endif
#ifdef RESTIR_CAPTURE_OUTPUTS
layout(location = 1) out vec4 outRestirNormal;
layout(location = 2) out vec4 outRestirAlbedo;
layout(location = 3) out vec4 outRestirF0;
#endif
#include "draw_constants.glsl"
#include "state_modes.glsl"
layout(constant_id = 0) const bool USE_LIGHTING = false;
layout(constant_id = 1) const bool USE_FOG = true;
layout(constant_id = 2) const bool USE_REFLECTIONS = false;
layout(constant_id = 3) const bool WRITE_COLOR = true;
layout(constant_id = 5) const bool RESTIR_CAPTURE = false;
layout(push_constant, std430) uniform AtlasPush {
    layout(offset = 28) uint parallaxQuality;
    layout(offset = 32) uint atlasBase;
    layout(offset = 36) uint restirEnabled;
    layout(offset = 40) uint64_t restirResultAddress;
    layout(offset = 48) uint restirWidth;
    layout(offset = 52) uint restirHeight;
    layout(offset = 56) uint64_t sharcResultAddress;
    layout(offset = 64) vec4 temporal;
#ifdef WORLD_REACTIVE_OUTPUTS
    layout(offset = 80) uvec4 changedTiles0;
    layout(offset = 96) uvec4 changedTiles1;
    layout(offset = 112) uvec4 textureAddressing;
#endif
} atlasPush;
#include "texture_reactivity.glsl"
#include "parallax_mapping.glsl"
layout(buffer_reference, std430, buffer_reference_align = 16) readonly buffer RestirDirectResult {
    vec4 values[];
};
void main() {
    outColor = vec4(0.0);
#ifdef WORLD_REACTIVE_OUTPUTS
    float textureReactive = 0.0;
    // The mask is uniform for this draw. Skip LOD queries for unchanged images;
    // evaluate derivatives before any alpha discard or early return.
    if (any(notEqual(atlasPush.changedTiles0,uvec4(0u)))
        || any(notEqual(atlasPush.changedTiles1,uvec4(0u)))) {
        vec2 reactiveUv = textureUv.xy/textureUv.z;
        vec2 reactiveFootprint = changedTextureFootprint(terrainTexture,reactiveUv);
        textureReactive = changedTextureReactivity(atlasPush.changedTiles0,atlasPush.changedTiles1,
            atlasPush.textureAddressing,reactiveUv,reactiveFootprint,
            !any(isnan(textureUv)) && !any(isinf(textureUv)) && textureUv.z>=0.000001);
    }
    outReactive = vec4(max(clamp(atlasPush.temporal.z,0.0,1.0),textureReactive),0.0,0.0,1.0);
#endif
#ifdef RESTIR_CAPTURE_OUTPUTS
    outRestirNormal = vec4(0.0);
    outRestirAlbedo = vec4(0.0);
    outRestirF0 = vec4(0.0);
#endif
    // Prepare the original mapping derivatives before alpha discard or the divergent POM march.
    vec3 positionDx = dFdx(worldPosition), positionDy = dFdy(worldPosition);
    vec2 uv = textureUv.xy/max(textureUv.z,0.000001);
    vec2 uvDx = dFdx(uv), uvDy = dFdy(uv);
    ivec2 parallaxAlbedoExtent = textureSize(terrainTexture,0);
    if (atlasPush.parallaxQuality!=0u && WRITE_COLOR) {
        float lod = textureQueryLod(terrainTexture,uv).x;
        if (isnan(lod) || isinf(lod)) parallaxAlbedoExtent = ivec2(0);
        else {
            int mip = int(clamp(ceil(lod),0.0,float(textureQueryLevels(terrainTexture)-1)));
            parallaxAlbedoExtent = textureSize(terrainTexture,mip);
        }
    }
    vec3 normal = cross(positionDx,positionDy);
    vec4 texel = textureProj(terrainTexture, textureUv);
    float alpha = texel.a*vertexColor.a;
    if (material.alpha.x>0.5 && alpha<=material.alpha.y) discard;
    // Fancy water first writes only depth. Preserve its cutout coverage without
    // tracing lighting, reflection or refraction that cannot contribute color.
    if (!WRITE_COLOR) { outColor = vec4(0.0); return; }
    // Derivative magnitude depends on distance and resolution; only a truly degenerate
    // triangle may use the mesh normal. The old 1e-6 threshold rejected ordinary 4K faces.
    float squareLength = dot(normal,normal);
    normal = squareLength>1e-24 ? normal*inversesqrt(squareLength) : normalize(fallbackNormal);
    vec3 incoming = normalize(worldPosition-eyeOrigin);
    if (dot(normal,incoming)>0.0) normal = -normal;
    vec3 albedo = linearColor(texel.rgb)*linearColor(materialTint(materialTag))*linearColor(draw.tint.rgb);
    vec4 normalTexel = textureLod(sceneTextures[nonuniformEXT(atlasPush.atlasBase+1u)],uv,0.0);
    vec4 specTexel = textureLod(sceneTextures[nonuniformEXT(atlasPush.atlasBase+2u)],uv,0.0);
    Surface surfaceData;
    surfaceData.albedo = albedo;
    surfaceData.normal = normal;
    surfaceData.geometricNormal = normal;
    surfaceData.position = worldPosition;
    surfaceData.emission = materialEmission(materialTag);
    surfaceData.alpha = alpha;
    surfaceData.cutoff = material.alpha.y;
    surfaceData.skyLight = terrainSkyLight;
    surfaceData.surfaceFlags = surfaceFlags;
    surfaceData.tag = materialTag;
    surfaceData.thin = thinSurface(materialTag);
    surfaceData.decorative = (materialTag & DECORATIVE_GRASS)!=0u;
    decodeLabPbr(normalTexel,specTexel,surfaceData);
    applyTileMaterial(atlasPush.atlasBase,uv,albedo,surfaceData);
    if (atlasPush.parallaxQuality!=0u && textureUv.z>=0.000001
        && !any(isnan(textureUv)) && !any(isinf(textureUv))
        && !surfaceData.thin && !surfaceData.decorative && !solidOccluder(materialTag)
        && (surfaceFlags&(0x80u|0x20u))==0u && surfaceData.transmission<=0.0
        && alpha>=0.99 && tileParallaxEnabled(atlasPush.atlasBase,uv)) {
        vec2 displacedUv = parallaxMappedUv(sceneTextures[nonuniformEXT(atlasPush.atlasBase+1u)],
            sceneTextures[nonuniformEXT(atlasPush.atlasBase+2u)],uv,uvDx,uvDy,positionDx,positionDy,
            normal,-incoming,length(worldPosition-eyeOrigin),parallaxAlbedoExtent,normalTexel.a,atlasPush.parallaxQuality);
        if (any(notEqual(displacedUv,uv))) {
            vec4 displacedTexel = textureGrad(terrainTexture,displacedUv,uvDx,uvDy);
            normalTexel = textureLod(sceneTextures[nonuniformEXT(atlasPush.atlasBase+1u)],displacedUv,0.0);
            specTexel = textureLod(sceneTextures[nonuniformEXT(atlasPush.atlasBase+2u)],displacedUv,0.0);
            albedo = linearColor(displacedTexel.rgb)*linearColor(materialTint(materialTag))*linearColor(draw.tint.rgb);
            surfaceData.albedo = albedo;
            // Decode again from block emission, not the unshifted texel's previously decoded emission.
            surfaceData.emission = materialEmission(materialTag);
            decodeLabPbr(normalTexel,specTexel,surfaceData);
            // The material and alpha coverage belong to the original tile and raster surface.
            applyTileMaterial(atlasPush.atlasBase,uv,albedo,surfaceData);
        }
    }
    if (!surfaceData.thin && !surfaceData.decorative) {
        vec3 tangent = positionDx*uvDy.y-positionDy*uvDx.y;
        if (dot(tangent,tangent)>1e-12) {
            tangent = normalize(tangent);
            vec3 bitangent = normalize(cross(normal,tangent));
            surfaceData.normal = normalize(mat3(tangent,bitangent,normal)*decodeTangentNormal(normalTexel));
        }
    }
#ifdef RESTIR_CAPTURE_OUTPUTS
    if (RESTIR_CAPTURE) {
        uint flags = RESTIR_VALID | (surfaceData.thin ? RESTIR_THIN : 0u)
            | (surfaceData.decorative ? RESTIR_DECORATIVE : 0u);
        vec3 shadingNormal = surfaceData.decorative ? vec3(0,1,0) : surfaceData.normal;
        outColor = vec4(surfaceData.position,float(flags));
        outRestirNormal = vec4(restirOctEncode(shadingNormal),restirOctEncode(surfaceData.geometricNormal));
        outRestirAlbedo = vec4(surfaceData.albedo,surfaceData.metallic);
        outRestirF0 = vec4(surfaceData.f0,surfaceData.roughness);
        return;
    }
#endif
    vec4 direct = vec4(0.0);
    uvec2 pixel = uvec2(gl_FragCoord.xy);
    if (atlasPush.restirEnabled!=0u && atlasPush.restirResultAddress!=uint64_t(0)
        && pixel.x<atlasPush.restirWidth && pixel.y<atlasPush.restirHeight) {
        direct = RestirDirectResult(atlasPush.restirResultAddress).values[pixel.y*atlasPush.restirWidth+pixel.x];
    }
    vec3 indirect = indirectIrradiance;
    if (atlasPush.sharcResultAddress!=uint64_t(0)
        && pixel.x<atlasPush.restirWidth && pixel.y<atlasPush.restirHeight) {
        vec4 cached = RestirDirectResult(atlasPush.sharcResultAddress).values[pixel.y*atlasPush.restirWidth+pixel.x];
        if (cached.a>0.0) indirect = cached.rgb;
    }
    vec4 surface = shadeSurface(surfaceData,incoming,indirect,alpha,USE_REFLECTIONS,terrainSkyLight,
        direct.rgb,direct.a>0.0);
    float visibility = 1.0;
    if (USE_FOG) {
        int distanceMode = int(frame.modes.y);
        float distance = distanceMode==FOG_DISTANCE_VERTEX ? eyePosition.w
            : distanceMode==FOG_DISTANCE_FRAGMENT ? length(eyePosition.xyz) : abs(eyePosition.z);
        int mode = int(frame.fog.x);
        if (mode==FOG_LINEAR) {
            float range = frame.fog.z-frame.fog.y;
            visibility = abs(range)>0.000001 ? (frame.fog.z-distance)/range : (distance<=frame.fog.z ? 1.0 : 0.0);
        } else if (mode==FOG_EXPONENTIAL) visibility = exp(-frame.fog.w*distance);
        else if (mode==FOG_EXPONENTIAL_SQUARED) visibility = exp(-pow(frame.fog.w*distance,2.0));
    }
    if (USE_FOG) {
        Environment environment = Environment(frame.envSun,frame.envSky,frame.envWeather,
            frame.envCamera,frame.envClouds,frame.envFog);
        environment.camera.xyz += eyeOrigin;
        float sunVisibility = atmosphereVisibility(environment,worldPosition,eyeOrigin);
        outColor = vec4(environmentFog(environment,surface.rgb,worldPosition-eyeOrigin,eyePosition.xyz,
            visibility,frame.fogColor.rgb,sunVisibility),surface.a);
    } else outColor = surface;
}
