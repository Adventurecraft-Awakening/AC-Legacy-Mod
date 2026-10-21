#version 450
layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec4 color;
layout(location = 3) in vec4 normal;
layout(location = 0) out vec3 textureUv;
layout(location = 1) out vec4 vertexColor;
layout(location = 2) out vec4 eyePosition;
layout(location = 3) out vec3 worldPosition;
layout(location = 4) flat out vec3 eyeOrigin;
#include "draw_constants.glsl"
#include "state_modes.glsl"
#include "color_space.glsl"
#include "temporal_jitter.glsl"
layout(push_constant, std430) uniform TemporalDrawPush {
    layout(offset = 64) vec4 temporal;
} temporalDraw;
layout(constant_id = 0) const bool USE_LIGHTING = true;
layout(constant_id = 1) const bool USE_FOG = true;
void main() {
    vec4 world = draw.model * vec4(position, 1.0);
    vec4 eye = frame.view * world;
    gl_Position = frame.projection * eye;
    gl_Position = temporalClipPosition(gl_Position,temporalDraw.temporal);
    if (material.params.z>0.5) gl_Position.z = gl_Position.w*0.999999;
    worldPosition = world.xyz;
    eyeOrigin = inverse(frame.view)[3].xyz;
    gl_PointSize = 1.0;
    vec4 tex = draw.textureMatrix * vec4(uv, 0.0, 1.0);
    // Keep Q until fragment sampling; dividing before interpolation distorts
    // projective texture matrices and loses the sign of negative Q values.
    textureUv = vec3(tex.xy, tex.w);
    vertexColor = vec4(authoredColor(color.rgb)*authoredColor(draw.tint.rgb),color.a*draw.tint.a);
    // The interpolated radius is the fast, rotation-invariant fog option.
    eyePosition = USE_FOG ? vec4(eye.xyz, length(eye.xyz)) : vec4(0.0);
    if (USE_LIGHTING && material.alpha.w > 0.5) {
        mat3 normalMatrix = mat3(draw.normal0.xyz, draw.normal1.xyz, draw.normal2.xyz);
        vec3 transformedNormal = normalMatrix * normal.xyz;
        float normalLength = length(transformedNormal);
        vec3 n = normalLength > 0.000001 ? transformedNormal / normalLength : vec3(0.0, 1.0, 0.0);
        int enabled = int(frame.modes.x);
        vec3 diffuseLight = vec3(0.0);
        if ((enabled & 1) != 0) {
            vec3 direction = frame.light0Position.xyz - eye.xyz * frame.light0Position.w;
            float lengthSquared = dot(direction, direction);
            if (lengthSquared > 0.000001) diffuseLight += frame.light0Diffuse.rgb * max(0.0, dot(n, direction * inversesqrt(lengthSquared)));
        }
        if ((enabled & 2) != 0) {
            vec3 direction = frame.light1Position.xyz - eye.xyz * frame.light1Position.w;
            float lengthSquared = dot(direction, direction);
            if (lengthSquared > 0.000001) diffuseLight += frame.light1Diffuse.rgb * max(0.0, dot(n, direction * inversesqrt(lengthSquared)));
        }
        // Fixed-function material defaults are ambient .2 and diffuse .8.
        // Minecraft switches between AMBIENT and AMBIENT_AND_DIFFUSE tracking.
        int materialMode = int(material.params.x);
        vec3 ambientMaterial = vec3(0.2);
        vec3 diffuseMaterial = vec3(0.8);
        vec3 emission = vec3(0.0);
        if (materialMode == MATERIAL_AMBIENT || materialMode == MATERIAL_AMBIENT_DIFFUSE) ambientMaterial = vertexColor.rgb;
        if (materialMode == MATERIAL_DIFFUSE || materialMode == MATERIAL_AMBIENT_DIFFUSE) diffuseMaterial = vertexColor.rgb;
        if (materialMode == MATERIAL_EMISSION) emission = vertexColor.rgb;
        float alpha = materialMode == MATERIAL_DIFFUSE || materialMode == MATERIAL_AMBIENT_DIFFUSE ? vertexColor.a : 1.0;
        vertexColor = vec4(clamp(emission + ambientMaterial * frame.lightAmbient.rgb + diffuseMaterial * diffuseLight, 0.0, 1.0), alpha);
    }
}
