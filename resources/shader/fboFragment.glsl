#version 410 core

in vec2 fragUV;
out vec4 color;

uniform usampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;
const vec2 center = vec2(0.5, 0.5);

const vec3[] palette = vec3[] (
    vec3(0.0),
    vec3(0.47, 0.5, 0.35),
    vec3(1.0)
);

float sobel(sampler2D sampler, int channel) {
    float mCenter   = texture(sampler, fragUV)[channel] * 2.0 - 1.0;
    return max(max(
            abs(mCenter - (texture(sampler, fragUV + vec2(0.0, -dy))[channel] * 2.0 - 1.0)),
            abs(mCenter - (texture(sampler, fragUV + vec2(dx, 0.0))[channel] * 2.0 - 1.0))),
            abs(mCenter - (texture(sampler, fragUV + vec2(dx, -dy))[channel] * 2.0 - 1.0)));
}

void main(void) {
    vec2 uvDist = fragUV - center;
    float distance = length(uvDist);
    if (distance > 0.5) {
        discard;
    }
    if (max(sobel(texture2, 0), max(sobel(texture2, 1), sobel(texture2, 2))) > 0.1) {
        color = vec4(vec3(0.0), 1.0);
    } else if (texture(texture3, fragUV).r > 0.99999) {
        color = vec4(palette[1], 1.0);
    } else {
        color = vec4(palette[uint(texture(texture1, fragUV).r) & 0x3u], 1.0);
    }
    if (distance > 0.499) {
        color = vec4(1.0);
    }
    uvDist.x *= WIDTH / HEIGHT;
    float distance2 = length(uvDist);
    if(distance2 < 0.01 && distance2 > 0.01 - 0.002 || distance2 < 0.002) {
        color = dot(color.rgb, GRAYSCALE) > 0.5 ? vec4(0.0, 0.0, 0.0, 1.0) : vec4(1.0);
    }
}
