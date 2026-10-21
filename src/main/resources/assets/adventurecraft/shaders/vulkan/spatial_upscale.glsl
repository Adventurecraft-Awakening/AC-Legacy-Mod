// Original spatial reconstruction: cubic detail with a local anti-ringing bound.
// Work in the source color domain; exposure, gamma and temporal history belong to other passes.
layout(local_size_x = 8, local_size_y = 8) in;
layout(set = 0, binding = 0) uniform sampler2D sourceImage;
layout(UPSCALE_OUTPUT_FORMAT, set = 0, binding = 1) uniform writeonly image2D outputImage;

vec4 sourceAt(ivec2 pixel, ivec2 extent) {
    return texelFetch(sourceImage, clamp(pixel, ivec2(0), extent - 1), 0);
}

// Hermite form of Catmull-Rom: constant inputs are preserved exactly and integer positions copy a texel.
vec4 cubic(vec4 a, vec4 b, vec4 c, vec4 d, float t) {
    vec4 delta = c - b;
    vec4 left = 0.5 * (c - a);
    vec4 right = 0.5 * (d - b);
    return b + t * (left + t * (3.0 * delta - 2.0 * left - right
        + t * (left + right - 2.0 * delta)));
}

void main() {
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    ivec2 targetExtent = imageSize(outputImage);
    if (any(greaterThanEqual(pixel, targetExtent))) return;
    ivec2 sourceExtent = textureSize(sourceImage, 0);
    vec2 position = (vec2(pixel) + 0.5) * vec2(sourceExtent) / vec2(targetExtent) - 0.5;
    ivec2 origin = ivec2(floor(position));
    vec2 fraction = fract(position);
    vec4 rows[4];
    for (int y = 0; y < 4; ++y) {
        ivec2 row = origin + ivec2(0, y - 1);
        rows[y] = cubic(sourceAt(row + ivec2(-1, 0), sourceExtent), sourceAt(row, sourceExtent),
            sourceAt(row + ivec2(1, 0), sourceExtent), sourceAt(row + ivec2(2, 0), sourceExtent), fraction.x);
    }
    vec4 reconstructed = cubic(rows[0], rows[1], rows[2], rows[3], fraction.y);
    vec4 a = sourceAt(origin, sourceExtent);
    vec4 b = sourceAt(origin + ivec2(1, 0), sourceExtent);
    vec4 c = sourceAt(origin + ivec2(0, 1), sourceExtent);
    vec4 d = sourceAt(origin + ivec2(1, 1), sourceExtent);
    // Bound negative cubic lobes at hard edges without blurring detail or clipping HDR highlights.
    vec4 lower = min(min(a, b), min(c, d));
    vec4 upper = max(max(a, b), max(c, d));
    imageStore(outputImage, pixel, clamp(reconstructed, lower, upper));
}
