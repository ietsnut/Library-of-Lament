#version 410 core

in vec2 fragUV;

layout(location = 0) out vec4 color;

uniform usampler2D texture1;      // Main scene (attachment 0 from framebuffer)
uniform sampler2D texture2;       // Bright pixels (attachment 1 from framebuffer)
uniform sampler2D texture3;       // Depth buffer
uniform sampler2D texture4;       // Blurred bright pixels from bloom pass

uniform int width;
uniform int height;

float linearizeDepth(float depth) {
    float z = depth * 2.0 - 1.0; // Convert to NDC
    return (2.0 * NEAR * FAR) / (FAR + NEAR - z * (FAR - NEAR));
}

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
        m[i] = linearizeDepth(texture(sampler, fragUV + offset[i]).r);
    }

    return abs(m[2] + 2.0 * m[4] + m[7] - (m[0] + 2.0 * m[3] + m[5])) +
    abs(m[7] + 2.0 * m[6] + m[5] - (m[0] + 2.0 * m[1] + m[2]));
}

void main(void) {

    vec2 center = vec2(0.5, 0.5);
    vec2  uvDist   = fragUV - center;
    float distance = length(uvDist);
    if (distance > 0.38) {
        discard;
    }
    uvDist.x      *= width / height;
    float distance2 = length(uvDist);
    if(distance2 < 0.01 && distance2 > 0.01 - 0.002 || distance2 < 0.002) {
        color = PALETTE[6];
        return;
    }

    float depthDelta = depth(texture3);

    if (depthDelta > 50) {
        color = PALETTE[2];
        return;
    }

    uint value = texture(texture1, fragUV).r;

    vec4 sceneColor;
    if (value == 0u) {
        sceneColor = PALETTE[5];
    } else {
        uint adjustedValue = value - 1u;
        uint baseIndex = adjustedValue % 16u;
        uint fogLevel = adjustedValue / 16u;
        vec4 baseColor = PALETTE[baseIndex];
        float fogFactor = float(fogLevel) / 14.0;
        sceneColor = mix(baseColor, PALETTE[5], fogFactor);
    }

    vec3 bloomColor = texture(texture4, fragUV).rgb;
    sceneColor.rgb += bloomColor * 0.7;

    color = sceneColor;
}