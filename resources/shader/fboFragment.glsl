#version 410 core

in vec2 fragUV;
out vec4 color;

uniform usampler2D texture1;
uniform sampler2D texture2;

uniform int width;
uniform int height;

float depth(sampler2D sampler) {
    float dx = 1.0 / width;
    float dy = 1.0 / height;
    vec2 offset[8] = vec2[](
    vec2(-dx, -dy), vec2( 0, -dy), vec2( dx, -dy),
    vec2(-dx,  0),                 vec2( dx,  0),
    vec2(-dx,  dy), vec2( 0,  dy), vec2( dx,  dy)
    );
    float m[8];
    for (int i = 0; i < 8; ++i) {
        m[i] = texture(sampler, fragUV + offset[i]).r;
    }
    return abs(m[2] + 2.0 * m[4] + m[7] - (m[0] + 2.0 * m[3] + m[5])) + abs(m[7] + 2.0 * m[6] + m[5] - (m[0] + 2.0 * m[1] + m[2]));
}

void main(void) {

    vec2 center = vec2(0.5, 0.5);
    vec2  uvDist   = fragUV - center;
    float distance = length(uvDist);
    if (distance > 0.5) {
        discard;
    }
    if (distance > 0.499) {
        color = LINE;
        return;
    }
    uvDist.x      *= width / height;
    float distance2 = length(uvDist);
    if(distance2 < 0.01 && distance2 > 0.01 - 0.002 || distance2 < 0.002) {
        color = dot(color.rgb, GRAYSCALE) > 0.5 ? vec4(0.0, 0.0, 0.0, 1.0) : vec4(1.0);
        return;
    }
    float depthDelta    = depth(texture2);
    if (depthDelta > 0.01) {
        color = LINE;
        return;
    }

    uint value = texture(texture1, fragUV).r;

    if (value == 0u) {
        color = PALETTE[5];  // Background/sky color
    } else if (value <= 16u) {
        color = PALETTE[value - 1u];  // Direct colors 1-16 -> palette 0-15
    } else {
        color = mix(PALETTE[(value - 17u) % 16u], PALETTE[5], 1.0 - float((value - 17u) / 16u) / 13.0);
    }
}