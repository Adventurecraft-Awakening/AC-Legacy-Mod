#ifndef AC_COLOR_SPACE_GLSL
#define AC_COLOR_SPACE_GLSL
// UI and the compatibility fallback retain display-space colors. Only world
// pipelines specialize this constant to produce linear HDR radiance.
layout(constant_id = 4) const bool LINEAR_OUTPUT = false;
vec3 srgbToLinear(vec3 color) {
    color = max(color,vec3(0.0));
    return mix(pow((color+0.055)/1.055,vec3(2.4)),color/12.92,
        lessThanEqual(color,vec3(0.04045)));
}
vec3 authoredColor(vec3 color) { return LINEAR_OUTPUT ? srgbToLinear(color) : color; }
#endif
