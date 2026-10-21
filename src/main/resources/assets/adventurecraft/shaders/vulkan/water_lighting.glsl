// Surface optics for the optional ray-query environment. No sampled noise or
// frame-local random values: camera motion must not move the material pattern.
const uint SURFACE_WATER = 0x20u;
const uint SURFACE_RAIN_EXPOSED = 0x40u;
const float WATER_TAU = 6.28318530718;

vec3 surfaceWorldPosition(vec3 position) {
    return position+lighting.envCamera.xyz;
}

vec3 tracedSurfaceRadiance(Surface hit, vec3 incoming) {
    // Collision proxies do not have a visible material and absorb every color
    // path, including the otherwise unoccluded ambient/environment terms.
    if (solidOccluder(hit.tag)) return vec3(0.0);
    vec3 diffuseAlbedo = hit.albedo*(1.0-hit.metallic);
    vec3 viewDirection = normalize(-incoming);
    LocalLighting local = localLighting(hit.position,hit,viewDirection);
    vec3 ambient = readableAmbient(skyRadiance(hit.normal)*0.3*hit.skyLight,hit.skyLight)*diffuseAlbedo;
    vec3 environmentSpecular = skyRadiance(reflect(incoming,hit.normal))*hit.f0
        *(1.0-hit.roughness*hit.roughness)*0.5;
    return sunlight(hit)*diffuseAlbedo
        +local.diffuse+local.specular+ambient+hit.albedo*hit.emission*4.0+environmentSpecular;
}

vec3 surfaceReflection(vec3 position, vec3 geometricNormal, vec3 direction) {
    Surface hit;
    if (traceColorScene(rayOrigin(position,geometricNormal,direction),direction,256.0,hit)) {
        return tracedSurfaceRadiance(hit,direction);
    }
    vec3 sky = skyRadiance(direction);
    if (lighting.envFog.x>0.5) {
        Environment environment = rayEnvironment();
        environment.camera.xyz += position;
        vec4 cloud = environmentCloudVolume(environment,direction,1536.0);
        sky = mix(sky,cloud.rgb*0.9,cloud.a);
    }
    return sky;
}

vec2 rainRippleSlope(vec2 world, float time, float rain) {
    vec2 cell = floor(world*0.5);
    vec2 local = fract(world*0.5)-0.5;
    float radius = length(local);
    float envelope = 1.0-smoothstep(0.18,0.49,radius);
    // Integer cycle counts keep both the 4096-block position wrap and bounded
    // animation clock continuous. Each two-block patch has a fixed wave phase.
    float phase = radius*35.0+dot(cell,vec2(61.0,137.0))*(WATER_TAU/2048.0)
        -time*(WATER_TAU*1024.0/4096.0);
    return local/max(radius,0.001)*cos(phase)*envelope*rain*0.016;
}

const vec2 WATER_MAIN_DIRECTION[4] = vec2[4](
    vec2(0.93969,0.34202),vec2(0.60182,0.79864),vec2(0.85717,-0.51504),vec2(-0.15643,0.98769));
const float WATER_MAIN_FREQUENCY[4] = float[4](0.24544,0.36530,0.51502,0.72221);
const float WATER_MAIN_AMPLITUDE[4] = float[4](0.2200,0.1400,0.0850,0.0500);
const vec2 WATER_DETAIL_DIRECTION[4] = vec2[4](
    vec2(0.99756,-0.06976),vec2(0.79864,0.60182),vec2(0.20791,0.97815),vec2(-0.75471,0.65606));
const float WATER_DETAIL_FREQUENCY[4] = float[4](1.34640,1.96350,2.73182,3.92699);
const float WATER_DETAIL_AMPLITUDE[4] = float[4](0.0100,0.0060,0.0035,0.0018);

vec3 waterSurfaceNormal(vec3 position, vec3 geometricNormal, float rain) {
    vec2 world = surfaceWorldPosition(position).xz;
    float time = lighting.envWeather.w;
    vec2 slope = vec2(0.0);
    for (int i=0;i<4;i++) {
        float phase = dot(world,WATER_MAIN_DIRECTION[i])*WATER_MAIN_FREQUENCY[i]+time*(3.0+float(i)*1.7);
        slope += WATER_MAIN_DIRECTION[i]*cos(phase)*WATER_MAIN_FREQUENCY[i]*WATER_MAIN_AMPLITUDE[i];
    }
    for (int i=0;i<4;i++) {
        float phase = dot(world,WATER_DETAIL_DIRECTION[i])*WATER_DETAIL_FREQUENCY[i]-time*(7.0+float(i)*2.3);
        slope += WATER_DETAIL_DIRECTION[i]*cos(phase)*WATER_DETAIL_FREQUENCY[i]*WATER_DETAIL_AMPLITUDE[i];
    }
    slope *= 0.38;
    slope += rainRippleSlope(world,time,rain);
    // Preserve the actual geometry for visibility offsets and side boundaries.
    // A small shading-only normal change avoids moving silhouettes/contact edges.
    return normalize(geometricNormal-vec3(slope.x,0.0,slope.y)*geometricNormal.y);
}

float waterCaustic(vec2 world, float time, float depth) {
    vec2 p = world*0.62;
    float a = sin(dot(p,vec2(3.1,1.7))*WATER_TAU+time*1.7);
    float b = sin(dot(p,vec2(-2.3,4.1))*WATER_TAU-time*2.3);
    float c = sin(dot(p,vec2(5.7,-3.9))*WATER_TAU+time*1.1);
    float pattern = clamp((a+b+c)*0.1667+0.5,0.0,1.0);
    float focus = smoothstep(0.0,3.0,depth);
    return mix(0.75,1.35,pow(pattern,1.8))*focus;
}

vec4 shadeAdvancedWater(Surface surface, vec3 incoming, vec3 indirect, float alpha, float skyLight) {
    vec3 albedo = surface.albedo;
    vec3 position = surface.position;
    vec3 geometricNormal = surface.normal;
    float rain = (surface.surfaceFlags & SURFACE_RAIN_EXPOSED)!=0u ? lighting.envWeather.x : 0.0;
    vec3 normal = waterSurfaceNormal(position,geometricNormal,rain);
    bool underwater = lighting.envCamera.w>0.5 && lighting.envCamera.w<1.5;
    float cosine = clamp(dot(-incoming,normal),0.0,1.0);
    float f0 = pow((surface.ior-1.0)/(surface.ior+1.0),2.0);
    float fresnel = f0+(1.0-f0)*pow(1.0-cosine,5.0);
    vec3 reflectionDirection = reflect(incoming,normal);
    // The displacement is a shading normal only; keep its reflected ray above
    // the actual flat interface at grazing angles rather than sampling its floor.
    float reflectedSide = dot(reflectionDirection,geometricNormal);
    if (reflectedSide<0.001) {
        reflectionDirection = normalize(reflectionDirection+geometricNormal*(0.001-reflectedSide));
    }
    vec3 reflected = surfaceReflection(position,geometricNormal,reflectionDirection);
    vec3 direction = refract(incoming,normal,underwater ? surface.ior : 1.0/surface.ior);
    if (dot(direction,direction)<0.00001) return vec4(displayRadiance(reflected),1.0);

    vec3 illumination = readableAmbient(indirect,skyLight)
        +sunlight(position,geometricNormal)*0.08+localDiffuse(position,geometricNormal)*0.2;
    // The transmission/body split below already bounds this lit texture by the
    // non-reflected, non-transmitted share. Dimming it again hides authored
    // animated albedo even on opaque custom water. Keep the inherited underside
    // response while restoring the full lit surface color when viewed from air.
    vec3 scatter = albedo*illumination*(underwater ? 0.28 : 1.0);
    Surface bottom;
    if (traceColorScene(rayOrigin(position,geometricNormal,direction),direction,128.0,bottom)) {
        float depth = underwater ? 0.0 : min(length(bottom.position-position),64.0);
        // Texture color and opacity remain material inputs for custom map water.
        // Red is absorbed faster in blue/green water, rather than applying a flat
        // screen tint that would make shallow and deep pools look identical.
        vec3 beer = exp(-surface.absorption*depth);
        float transmission = max(max(beer.r,beer.g),beer.b)*(1.0-0.35*clamp(alpha,0.0,1.0))
            *surface.transmission;
        float caustic = waterCaustic(bottom.position.xz,lighting.envWeather.w,length(bottom.position-position));
        vec3 tintedRefraction = tracedSurfaceRadiance(bottom,direction)*beer*caustic/max(max(max(beer.r,beer.g),beer.b),0.0001);
        vec3 refracted = tintedRefraction;
        // Only a bounded portion of the transmitted image uses the terrain ray.
        // The rest retains the raster background, including entities/particles
        // absent from the terrain acceleration structure.
        float refractionWeight = 0.3;
        float scatterWeight = (1.0-fresnel)*(1.0-transmission);
        float tracedWeight = (1.0-fresnel)*transmission*refractionWeight;
        float opacity = fresnel+scatterWeight+tracedWeight;
        vec3 composed = (reflected*fresnel+scatter*scatterWeight+refracted*tracedWeight)
            /max(opacity,0.0001);
        return vec4(displayRadiance(composed),opacity);
    }

    // Keep the inherited raster transmission when the terrain query has no
    // background: a miss must not replace actors or unloaded terrain with sky.
    float bodyWeight = (1.0-fresnel)*mix(1.0,clamp(alpha,0.0,1.0),surface.transmission);
    float opacity = fresnel+bodyWeight;
    vec3 composed = (reflected*fresnel+scatter*bodyWeight)/max(opacity,0.0001);
    return vec4(displayRadiance(composed),opacity);
}

float surfaceWetness(uint surfaceFlags, uint tag, float alpha, vec3 normal) {
    if ((surfaceFlags & SURFACE_RAIN_EXPOSED)==0u || (surfaceFlags & SURFACE_WATER)!=0u
        || (surfaceFlags & 0x80u)!=0u
        || (tag & (THIN_FOLIAGE|DECORATIVE_GRASS))!=0u || materialEmission(tag)>0.0 || alpha<0.99) {
        return 0.0;
    }
    return clamp(lighting.envWeather.z,0.0,1.0)*smoothstep(0.45,0.95,normal.y);
}

vec3 shadeWetSurface(vec3 base, vec3 position, vec3 normal, vec3 incoming, float wetness) {
    vec2 world = surfaceWorldPosition(position).xz;
    float pattern = sin(dot(world,vec2(173.0,271.0))*(WATER_TAU/4096.0))
        *sin(dot(world,vec2(-397.0,127.0))*(WATER_TAU/4096.0));
    float puddle = smoothstep(-0.2,0.65,pattern);
    vec2 ripple = rainRippleSlope(world,lighting.envWeather.w,lighting.envWeather.x)*0.3;
    vec3 filmNormal = normalize(normal-vec3(ripple.x,0.0,ripple.y));
    vec3 reflected = displayRadiance(surfaceReflection(position,normal,reflect(incoming,filmNormal)));
    float fresnel = 0.04+0.96*pow(1.0-clamp(dot(-incoming,filmNormal),0.0,1.0),5.0);
    float film = wetness*mix(0.25,0.8,puddle);
    return mix(base,reflected,fresnel*film);
}
