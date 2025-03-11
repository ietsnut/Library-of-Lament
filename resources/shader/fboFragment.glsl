#version 410 core

in vec2 fragUV;
out vec4 color;

uniform usampler2D texture1; // color (with stored indices: 0..6)
uniform sampler2D texture2;  // normals
uniform sampler2D texture3;  // depth

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;
const vec2 center = vec2(0.5, 0.5);

float sobel(sampler2D sampler, int channel) {
    float mCenter = texture(sampler, fragUV)[channel];
    return max(max(
        abs(mCenter - texture(sampler, fragUV + vec2(0.0, -dy))[channel]),
        abs(mCenter - texture(sampler, fragUV + vec2(dx, 0.0))[channel])),
        abs(mCenter - texture(sampler, fragUV + vec2(dx, -dy))[channel]));
}

void main(void) {
    uint index = uint(texture(texture1, fragUV).r) & 0x07u;
    if (sobel(texture3, 0) > 0.01 || max(sobel(texture2, 0), max(sobel(texture2, 1), sobel(texture2, 2))) > 0.01) {
        color = LINE;
    } else if (index > 0u) {
        color = vec4(PALETTE[index - 1], 1.0);
    } else {
        discard;
    }
}
