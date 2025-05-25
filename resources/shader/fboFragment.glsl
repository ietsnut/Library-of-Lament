#version 410 core

in vec2 fragUV;
out vec4 color;

uniform usampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;

const float dx = 1.0 / WIDTH;
const float dy = 1.0 / HEIGHT;

float sobel(sampler2D sampler, int channel) {
    float mCenter   = texture(sampler, fragUV)[channel] * 2.0 - 1.0;
    float dT        = abs(mCenter - (texture(sampler, fragUV + vec2(0.0, -dy))[channel] * 2.0 - 1.0));
    float dR        = abs(mCenter - (texture(sampler, fragUV + vec2(dx, 0.0))[channel] * 2.0 - 1.0));
    float dTR       = abs(mCenter - (texture(sampler, fragUV + vec2(dx, -dy))[channel] * 2.0 - 1.0));
    return max(max(dT, dR), dTR);
}

float depth(sampler2D sampler) {
    float m00 = texture(sampler, fragUV + vec2(-dx, -dy)).r;
    float m01 = texture(sampler, fragUV + vec2( 0,  -dy)).r;
    float m02 = texture(sampler, fragUV + vec2( dx, -dy)).r;
    float m10 = texture(sampler, fragUV + vec2(-dx,  0)).r;
    float m12 = texture(sampler, fragUV + vec2( dx,  0)).r;
    float m20 = texture(sampler, fragUV + vec2(-dx,  dy)).r;
    float m21 = texture(sampler, fragUV + vec2( 0,   dy)).r;
    float m22 = texture(sampler, fragUV + vec2( dx,  dy)).r;

    float dx = m02 + 2.0 * m12 + m22 - (m00 + 2.0 * m10 + m20);
    float dy = m22 + 2.0 * m21 + m20 - (m00 + 2.0 * m01 + m02);

    return sqrt(dx * dx + dy * dy);  // Magnitude of gradient
}

void main(void) {
    vec2 center = vec2(0.5, 0.5);
    vec2  uvDist   = fragUV - center;
    float distance = length(uvDist);
    if (distance > 0.5) {
        discard;
    }
    float depthDelta    = depth(texture3);
    float depth         = texture(texture3, fragUV).r;
    float normalDelta   = max(sobel(texture2, 0), max(sobel(texture2, 1), sobel(texture2, 2)));
    float dither        = (fract(sin(dot(fragUV.xy ,vec2(12.9898,78.233))) * 43758.5453) - 0.5) * 2;
    if (normalDelta > 0.1 && dither > 0.01) {
        color = vec4(vec3(0.25), 1.0);
        if (depth < 0.99) {
            color = vec4(vec3(0.5), 1.0);
        } if (depth < 0.98) {
            color = vec4(vec3(0.75), 1.0);
        }
    } else if (depth > 0.99999) {
        color = vec4(vec3(0.0), 1.0);
    } else {
        color = vec4(PALETTE[texture(texture1, fragUV).r], 1.0);
    }
    if (distance > 0.499) {
        color = vec4(1.0);
    }
    uvDist.x      *= WIDTH / HEIGHT;
    float distance2 = length(uvDist);
    if(distance2 < 0.01 && distance2 > 0.01 - 0.002 || distance2 < 0.002) {
        color = dot(color.rgb, GRAYSCALE) > 0.5 ? vec4(0.0, 0.0, 0.0, 1.0) : vec4(1.0);
    }
}
