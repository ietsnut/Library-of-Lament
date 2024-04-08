#version 430 core

in vec2 fragUV;
out vec3 color;

layout(location = 0) uniform sampler2D colorTexture;
layout(location = 1) uniform sampler2D normalTexture;

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;

float sobel(sampler2D sampler, int channel) {
    float mCenter   = texture(sampler, fragUV)[channel] * 2.0 - 1.0;
    float dT        = abs(mCenter - (texture(sampler, fragUV + vec2(0.0, -dy))[channel] * 2.0 - 1.0));
    float dR        = abs(mCenter - (texture(sampler, fragUV + vec2(dx, 0.0))[channel] * 2.0 - 1.0));
    float dTR       = abs(mCenter - (texture(sampler, fragUV + vec2(dx, -dy))[channel] * 2.0 - 1.0));
    return max(max(dT, dR), dTR);
}

void main(void) {
    float albedoDelta   = sobel(colorTexture, 0);
    float depthDelta    = sobel(normalTexture, 3);
    float normalDelta   = max(sobel(normalTexture, 0), max(sobel(normalTexture, 1), sobel(normalTexture, 2)));
    float albedo        = texture(colorTexture, fragUV).r;
    color               = vec3(albedo);
    if ((normalDelta > 0.01 && albedoDelta > 0.4) || (depthDelta > 0.1)) {
        color = vec3(1.0);
    }
    vec2 center         = vec2(0.5, 0.5);
    float distance      = length(fragUV - center);
    if(distance < 0.01 + 0.001 && distance > 0.01 - 0.001 || distance < 0.002) {
        color = color.r > 0.5 ? vec3(0.0) : vec3(1.0);
    }
}
