#version 410 core

in vec2 fragUV;
out vec4 color;

uniform usampler2D texture1; // color (with stored indices: 0..6)
uniform sampler2D texture2;  // normals
uniform sampler2D texture3;  // depth

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;

float sobel(sampler2D sampler, int channel) {
    float mCenter = texture(sampler, fragUV)[channel];
    return max(max(
        abs(mCenter - texture(sampler, fragUV + vec2(0.0, -dy))[channel]),
        abs(mCenter - texture(sampler, fragUV + vec2(dx, 0.0))[channel])),
        abs(mCenter - texture(sampler, fragUV + vec2(dx, -dy))[channel]));
}

void main(void) {
    ivec2 texSize = textureSize(texture1, 0);
    ivec2 coord = ivec2(fragUV * vec2(texSize));
    coord = clamp(coord, ivec2(0), texSize - ivec2(1));
    uint current = texelFetch(texture1, coord, 0).r & 0x07u;
    uint top     = texelFetch(texture1, coord + ivec2(0, -1), 0).r & 0x07u;
    uint bottom  = texelFetch(texture1, coord + ivec2(0, 1), 0).r & 0x07u;
    uint left    = texelFetch(texture1, coord + ivec2(-1, 0), 0).r & 0x07u;
    uint right   = texelFetch(texture1, coord + ivec2(1, 0), 0).r & 0x07u;
    bool isEdge = (current != top) || (current != bottom) || (current != left) || (current != right);
    if (sobel(texture3, 0) > 0.01 || max(sobel(texture2, 0), max(sobel(texture2, 1), sobel(texture2, 2))) > 0.01 || isEdge) {
        color = LINE;
    } else if (current > 0u) {
        color = vec4(PALETTE[current - 1], 1.0);
    } else {
        discard;
    }
}
