// Original implementation of the visible-GGX spherical-cap construction described
// by Dupuy and Benyoub (2023). Only the optional native guide path casts these rays.
vec3 reconstructionSpecularDirection(RestirSurface surface, vec3 view, vec2 randomPoint) {
    vec3 n = surface.normal;
    vec3 tangent = normalize(cross(abs(n.z)<0.95 ? vec3(0,0,1) : vec3(0,1,0),n));
    vec3 bitangent = cross(n,tangent);
    vec3 localView = vec3(dot(view,tangent),dot(view,bitangent),max(dot(view,n),0.00001));
    float alpha = max(surface.roughness*surface.roughness,0.0004);
    vec3 stretched = normalize(vec3(localView.xy*alpha,localView.z));
    float z = mix(-stretched.z,1.0,randomPoint.y);
    float radius = sqrt(max(1.0-z*z,0.0)), angle = randomPoint.x*6.28318530718;
    vec3 halfway = stretched+vec3(radius*cos(angle),radius*sin(angle),z);
    halfway = normalize(vec3(halfway.xy*alpha,max(halfway.z,0.00001)));
    vec3 h = tangent*halfway.x+bitangent*halfway.y+n*halfway.z;
    vec3 direction = reflect(-view,h);
    if (dot(n,direction)<=0.0 || dot(surface.geometricNormal,direction)<=0.0)
        direction = reflect(-view,surface.geometricNormal);
    if (dot(surface.geometricNormal,direction)<=0.00001) direction = surface.geometricNormal;
    return normalize(direction);
}
float reconstructionHitDistance(RestirSurface surface, vec3 direction) {
    Surface hit;
    if (!traceScene(rayOrigin(surface.position,surface.geometricNormal,direction),direction,256.0,hit)) return 65504.0;
    return max(length(hit.position-surface.position),0.00001);
}
