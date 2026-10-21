#ifndef AC_RESTIR_GBUFFER_GLSL
#define AC_RESTIR_GBUFFER_GLSL
const uint RESTIR_VALID = 1u;
const uint RESTIR_THIN = 2u;
const uint RESTIR_DECORATIVE = 4u;

vec2 restirSign(vec2 value) {
    return mix(vec2(-1.0),vec2(1.0),greaterThanEqual(value,vec2(0.0)));
}

vec2 restirOctEncode(vec3 normal) {
    normal /= max(dot(abs(normal),vec3(1.0)),0.000001);
    return normal.z>=0.0 ? normal.xy : (1.0-abs(normal.yx))*restirSign(normal.xy);
}

vec3 restirOctDecode(vec2 encoded) {
    vec3 normal = vec3(encoded,1.0-abs(encoded.x)-abs(encoded.y));
    if (normal.z<0.0) normal.xy = (1.0-abs(normal.yx))*restirSign(normal.xy);
    return normalize(normal);
}
#endif
