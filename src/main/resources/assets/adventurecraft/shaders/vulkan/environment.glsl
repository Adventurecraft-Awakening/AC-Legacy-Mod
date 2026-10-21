// Shared analytical atmosphere and deterministic cloud volume. No vendor extensions,
// frame history or camera-dependent random sampling are required.
#include "color_space.glsl"
struct Environment {
    vec4 sun;
    vec4 sky;
    vec4 weather;
    vec4 camera;
    vec4 clouds;
    vec4 fog;
};
const float ENV_PI = 3.14159265359;

vec3 environmentDisplay(vec3 radiance) {
    radiance = max(radiance,vec3(0.0));
    if (LINEAR_OUTPUT) return radiance;
    return pow(radiance/(radiance+vec3(0.6)),vec3(1.0/2.2));
}

float environmentHash(vec2 p) {
    vec3 q = fract(vec3(p.xyx)*vec3(0.1031,0.1030,0.0973));
    q += dot(q,q.yzx+33.33);
    return fract((q.x+q.y)*q.z);
}

float environmentNoise(vec2 p, float period) {
    vec2 cell = floor(p), f = fract(p);
    f = f*f*(3.0-2.0*f);
    vec2 a = mod(cell,period), b = mod(cell+1.0,period);
    return mix(mix(environmentHash(a),environmentHash(vec2(b.x,a.y)),f.x),
        mix(environmentHash(vec2(a.x,b.y)),environmentHash(b),f.x),f.y);
}

float environmentCloudField(Environment e, vec2 world) {
    // Both the world anchor and animation wrap every 4096 units. All octaves have
    // the same period, so crossing the wrap cannot pop clouds or their shadows.
    vec2 p = (world+vec2(1.0,-1.0)*e.weather.w)/128.0;
    return environmentNoise(p,32.0)*0.6
        +environmentNoise(p*2.0+vec2(17.3,9.1),64.0)*0.3
        +environmentNoise(p*4.0+vec2(3.7,23.8),128.0)*0.1;
}

float environmentCloudThickness(Environment e) { return 48.0+16.0*e.weather.x; }

float environmentCloudDensity(Environment e, vec3 world) {
    float thickness = environmentCloudThickness(e);
    float h = (world.y-e.clouds.x)/thickness;
    if (h<=0.0 || h>=1.0) return 0.0;
    float field = environmentCloudField(e,world.xz);
    float threshold = 1.0-e.clouds.y;
    float coverage = smoothstep(threshold-0.12,threshold+0.15,field);
    if (coverage<0.001) return 0.0;
    vec2 wind = vec2(1.0,-1.0)*e.weather.w;
    vec2 position = world.xz+wind;
    vec2 cell = floor(position/64.0);
    float puffs = 0.0;
    // Rounded volumes have independent bases and tops. Periodic cell keys keep
    // cloud geometry continuous across the world's 4096-block/time wrap.
    for (int z=-1;z<=1;z++) for (int x=-1;x<=1;x++) {
        vec2 neighbor = cell+vec2(x,z);
        vec2 key = mod(neighbor,64.0);
        float a = environmentHash(key), b = environmentHash(key+vec2(17.7,3.1));
        vec4 random = fract(vec4(a,b,a+b,a-b)*vec4(1.0,1.0,13.37,31.71));
        vec2 center = (neighbor+vec2(0.15)+random.xy*0.7)*64.0;
        vec2 radii = vec2(28.0)+fract(random.zw*vec2(7.13,11.71))*20.0;
        float centerY = e.clouds.x+thickness*(0.40+0.09*random.w);
        float radiusY = thickness*(0.27+0.10*random.z);
        vec2 horizontal = (position-center)/radii;
        vec3 q = vec3(horizontal,(world.y-centerY)/radiusY);
        float puff = 1.0-smoothstep(0.45,1.0,length(q));
        // A smaller elevated lobe breaks the flat ellipsoidal silhouette into
        // a cumulus top while keeping every lobe strictly within the slab.
        vec2 offset = (random.xy-0.5)*0.65;
        vec3 top = vec3((horizontal-offset)/0.68,
            (world.y-centerY-thickness*0.18)/(radiusY*0.82));
        float crown = 1.0-smoothstep(0.45,1.0,length(top));
        puff += (1.0-puff)*crown;
        vec3 peak = vec3((horizontal+offset*0.8)/0.48,
            (world.y-centerY-thickness*0.31)/(radiusY*0.55));
        float billow = 1.0-smoothstep(0.4,1.0,length(peak));
        puff += (1.0-puff)*billow;
        puffs += (1.0-puffs)*puff;
    }
    return puffs*coverage*mix(0.12,0.19,e.weather.x);
}

vec3 environmentSky(Environment e, vec3 direction) {
    if (e.sky.a<=0.0) return vec3(0.0);
    vec3 d = normalize(direction);
    float up = max(d.y,0.0);
    float horizon = exp(-up*5.0);
    float day = smoothstep(-0.16,0.3,e.sun.y);
    float storm = clamp(e.weather.x*0.65+e.weather.y*0.25,0.0,0.9);
    vec3 biomeSky = srgbToLinear(e.sky.rgb);
    // Rayleigh-colored zenith and the longer, desaturated optical path at the horizon.
    vec3 zenith = biomeSky*0.75+vec3(0.003,0.008,0.025)*(0.2+day);
    vec3 horizonColor = mix(biomeSky,vec3(0.55,0.62,0.72)*day,0.55);
    vec3 color = mix(zenith,horizonColor,horizon);
    float sunCos = clamp(dot(d,e.sun.xyz),-1.0,1.0);
    float twilight = exp(-pow(e.sun.y/0.2,2.0))*(1.0-storm);
    color += vec3(0.65,0.14,0.025)*twilight*horizon*pow(max(sunCos,0.0),4.0);
    // Broad forward Mie glow complements the map's own sun sprite.
    color += mix(vec3(1.0,0.48,0.16),vec3(1.0,0.93,0.8),smoothstep(0.0,0.5,e.sun.y))
        *pow(max(sunCos,0.0),32.0)*e.sun.w*0.16;
    vec3 overcast = vec3(dot(color,vec3(0.2126,0.7152,0.0722)))*vec3(0.84,0.9,1.0);
    color = mix(color,overcast,storm)*mix(1.0,0.7,storm);
    // A dim blue night sky leaves stars visible without turning night into daylight.
    color += vec3(0.001,0.002,0.005)*(1.0-day);
    return color*mix(0.35,1.0,smoothstep(-0.25,0.03,d.y));
}

float environmentCloudShadow(Environment e, vec3 relativePosition) {
    if (e.fog.x<0.5 || e.clouds.z<0.5 || e.sun.y<=0.03) return 1.0;
    vec3 world = relativePosition+e.camera.xyz;
    float distance = (e.clouds.x+environmentCloudThickness(e)*0.4-world.y)/e.sun.y;
    if (distance<=0.0) return 1.0;
    float field = environmentCloudField(e,world.xz+e.sun.xz*distance);
    float coverage = smoothstep(1.0-e.clouds.y-0.09,1.0-e.clouds.y+0.13,field);
    return 1.0-coverage*mix(0.35,0.65,e.weather.x);
}

// Returns linear straight RGB and opacity. The same bounded integration is used
// for the sky, reflections and terrain intersecting a cloud.
vec4 environmentCloudVolume(Environment e, vec3 direction, float maxDistance) {
    if (e.fog.x<0.5 || e.clouds.z<0.5 || e.sky.a<=0.0 || maxDistance<=0.0) return vec4(0.0);
    vec3 d = normalize(direction);
    float thickness = environmentCloudThickness(e);
    float nearDistance, farDistance;
    if (abs(d.y)<0.0001) {
        if (e.camera.y<=e.clouds.x || e.camera.y>=e.clouds.x+thickness) return vec4(0.0);
        nearDistance = 0.0;
        farDistance = min(maxDistance,768.0);
    } else {
        float a = (e.clouds.x-e.camera.y)/d.y;
        float b = (e.clouds.x+thickness-e.camera.y)/d.y;
        nearDistance = max(min(a,b),0.0);
        farDistance = min(max(a,b),min(maxDistance,1536.0));
    }
    if (farDistance<=nearDistance) return vec4(0.0);
    float totalDistance = farDistance-nearDistance;
    float stepLength = min(totalDistance/32.0,12.0);
    float marchedDistance = min(totalDistance,stepLength*32.0);
    vec3 ambient = environmentSky(e,vec3(0,1,0))*0.65;
    vec3 sunColor = mix(vec3(1.0,0.45,0.15),vec3(1.0,0.94,0.82),smoothstep(0.0,0.5,e.sun.y));
    float silver = pow(max(dot(d,e.sun.xyz),0.0),12.0)*0.3;
    float transmission = 1.0;
    vec3 radiance = vec3(0.0);
    for (int i=0;i<32;i++) {
        vec3 p = e.camera.xyz+d*(nearDistance+(float(i)+0.5)*stepLength);
        float density = environmentCloudDensity(e,p);
        if (density<=0.00001) continue;
        float height = clamp((p.y-e.clouds.x)/thickness,0.0,1.0);
        float selfShadow = exp(-environmentCloudDensity(e,p+e.sun.xyz*thickness*0.35)
            *thickness*mix(0.85,0.15,height));
        // A bounded multiple-scattering floor keeps sunlit cloud bases readable;
        // the direct term still varies through each puff's rounded volume.
        vec3 light = ambient*mix(1.0,1.25,height)
            +sunColor*e.sun.w*(1.2+silver)*(0.75+0.25*selfShadow);
        float opacity = 1.0-exp(-density*stepLength);
        radiance += transmission*opacity*light;
        transmission *= 1.0-opacity;
        if (transmission<0.01) break;
    }
    if (transmission>0.01 && totalDistance>marchedDistance) {
        // Very shallow rays may cross more than a kilometre of cloud. Resolve
        // nearby puffs with bounded steps, then integrate the distant remainder
        // as a smooth medium instead of spreading sixteen samples into bands.
        float remainder = totalDistance-marchedDistance;
        vec3 start = e.camera.xyz+d*(nearDistance+marchedDistance);
        float density = (environmentCloudDensity(e,start+d*remainder*0.2)
            +environmentCloudDensity(e,start+d*remainder*0.5)
            +environmentCloudDensity(e,start+d*remainder*0.8))/3.0;
        float distantOpacity = 1.0-exp(-density*remainder);
        vec3 light = ambient+sunColor*e.sun.w*(0.95+silver);
        radiance += transmission*distantOpacity*light;
        transmission *= 1.0-distantOpacity;
    }
    float opacity = 1.0-transmission;
    if (opacity<0.0001) return vec4(0.0);
    float horizonFade = exp(-nearDistance*0.00045);
    return vec4(radiance/opacity,opacity*horizonFade);
}

vec4 environmentClouds(Environment e, vec3 direction, float maxDistance) {
    vec4 cloud = environmentCloudVolume(e,direction,maxDistance);
    return vec4(environmentDisplay(cloud.rgb),cloud.a);
}

vec3 environmentFog(Environment e, vec3 sceneColor, vec3 relativePosition, vec3 eyePosition,
                    float mapVisibility, vec3 mapFogColor, float sunVisibility) {
    mapFogColor = authoredColor(mapFogColor);
    float distance = length(eyePosition);
    vec3 result = sceneColor;
    if (e.fog.x>0.5 && distance>0.001) {
        float squareLength = dot(relativePosition,relativePosition);
        vec3 direction = squareLength>1e-12 ? relativePosition*inversesqrt(squareLength) : vec3(0,1,0);
        if (e.camera.w>0.5 && e.camera.w<1.5) {
            // Red is absorbed sooner than blue/green; retain the existing clear-water
            // and map fog distance through the ordinary visibility applied below.
            vec3 transmission = exp(-vec3(0.07,0.025,0.012)*distance);
            vec3 deepWater = mix(mapFogColor,authoredColor(vec3(0.025,0.12,0.17)),0.65);
            result = result*transmission+deepWater*(1.0-transmission);
        } else if (e.camera.w<0.5) {
            // Surface skylight is a readability hint sampled around an entire block:
            // a thin roof can carry outside sky on its inside face. Only the eye's
            // captured exposure may introduce an atmospheric medium into a room.
            float exposure = clamp(e.fog.z,0.0,1.0);
            // Integrate a height-dependent medium along the ray; clamp the exponent
            // to avoid overflow below the map and leave sealed caves free of sky fog.
            float y = e.camera.y+relativePosition.y*0.5;
            float heightDensity = exp(clamp((64.0-y)*0.012,-3.0,2.0));
            float opticalDepth = e.fog.y*distance*heightDensity*exposure;
            float opacity = min(1.0-exp(-opticalDepth),0.85);
            vec3 scatter = mix(mapFogColor,environmentDisplay(environmentSky(e,direction)),0.65);
            float phase = pow(max(dot(direction,e.sun.xyz),0.0),12.0);
            scatter += vec3(0.14,0.105,0.065)*phase*e.sun.w*sunVisibility;
            result = mix(result,scatter,opacity);
            if (exposure>0.001) {
                vec4 cloud = environmentClouds(e,direction,distance);
                result = mix(result,cloud.rgb,cloud.a);
            }
        }
    }
    // Authored fog remains the final boundary, rather than being replaced by weather.
    return mix(mapFogColor,result,clamp(mapVisibility,0.0,1.0));
}
