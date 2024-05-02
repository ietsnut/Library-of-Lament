#version 410 core

in vec2 fragUV;
out vec4 color;

uniform sampler2D texture1;
uniform sampler2D texture2;

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;

const vec3[] palette = vec3[] (
    vec3(0.227,0.212,0.208),
    vec3(0.278,0.278,0.263),
    vec3(0.341,0.365,0.357),
    vec3(0.494,0.525,0.525),
    vec3(0.667,0.675,0.643),
    vec3(0.902,0.906,0.871)
);

float sobel(sampler2D sampler, int channel) {
    float mCenter   = texture(sampler, fragUV)[channel] * 2.0 - 1.0;
    float dT        = abs(mCenter - (texture(sampler, fragUV + vec2(0.0, -dy))[channel] * 2.0 - 1.0));
    float dR        = abs(mCenter - (texture(sampler, fragUV + vec2(dx, 0.0))[channel] * 2.0 - 1.0));
    float dTR       = abs(mCenter - (texture(sampler, fragUV + vec2(dx, -dy))[channel] * 2.0 - 1.0));
    return max(max(dT, dR), dTR);
}

void main(void) {
    //TODO: don't draw outside the circle of window
    vec2 center = vec2(0.5, 0.5);
    float distance = length(fragUV - center);
    if (distance > 0.5) {
        discard;
    }
    float depthDelta    = sobel(texture1, 2);
    float normalDelta   = max(sobel(texture2, 0), max(sobel(texture2, 1), sobel(texture2, 2)));
    float light         = texture(texture1, fragUV).g;
    if ((depthDelta > 0.1) && light < 1.0) {
        color = vec4(0.5, 0.5, 0.5, 1.0);
    } else if (normalDelta > 0.1 && light > 1.0) {
        color = vec4(1.0);
    } else {
        color = vec4(vec3(texture(texture1, fragUV).r).rgb, 1.0);
    }
    if(distance < 0.01 && distance > 0.01 - 0.001 || distance < 0.002) {
        color = color.r > 0.5 ? vec4(0.0) : vec4(1.0);
    }
    if (distance > 0.499) {
        color = vec4(1.0);
    }
}
