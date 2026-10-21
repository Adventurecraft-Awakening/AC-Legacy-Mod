// Shared by raster and ray-query terrain. UI and hands receive a zero jitter.
vec4 temporalClipPosition(vec4 clipPosition, vec4 temporalDraw) {
    if (temporalDraw.w>0.5) clipPosition.xy += temporalDraw.xy*clipPosition.w;
    return clipPosition;
}
