// Layout and component packing are shared with VulkanDrawConstants. Authored
// fog/sky/tint and legacy light colors enter as sRGB. World shader variants
// decode at use sites; normal/specular atlases always remain linear data.
layout(set = 0, binding = 0, std140) uniform ViewConstants {
    mat4 projection;
    mat4 view;
    vec4 fogColor;
    vec4 fog; // mode, start, end, density
    vec4 light0Position;
    vec4 light1Position;
    vec4 light0Diffuse;
    vec4 light1Diffuse;
    vec4 lightAmbient;
    vec4 modes; // enabled lights bit mask, fog distance, unused, unused
    vec4 envSun;
    vec4 envSky;
    vec4 envWeather;
    vec4 envCamera;
    vec4 envClouds;
    vec4 envFog;
} frame;
layout(set = 1, binding = 0, std140) uniform ObjectConstants {
    mat4 model;
    mat4 textureMatrix;
    vec4 tint;
    vec4 normal0;
    vec4 normal1;
    vec4 normal2;
} draw;
layout(set = 2, binding = 0, std140) uniform MaterialConstants {
    vec4 alpha; // alpha test enabled, reference, comparison, lighting enabled
    vec4 params; // material tracking, ordered masked clear, effect mode, unused
} material;
