#version 410 core

in vec2 fragUV;
in vec3 fragPosition;

layout(location = 0) out uint sceneColor;
layout(location = 1) out vec4 brightColor;

uniform usampler2D texture1;

uniform int state;
uniform int states;

const float threshold = 0.75;

void main(void) {
    vec2 atlasUV = fragUV;

    if (states > 1) {
        float stateWidth = 1.0 / float(states);
        atlasUV.x = (fragUV.x * stateWidth) + float(state) * stateWidth;
    }

    uint baseColor = texture(texture1, atlasUV).r;
    if (baseColor == 16u) {
        discard;
    }

    sceneColor = baseColor + 1u;

    vec4 actualColor = PALETTE[baseColor];
    float brightness = dot(actualColor.rgb, GRAYSCALE);
    if (brightness > threshold) {
        float bloomStrength = (brightness - threshold) / (1.0 - threshold);
        bloomStrength = pow(bloomStrength, 0.5) * 2.0;
        brightColor = vec4(actualColor.rgb * bloomStrength, 1.0);
    } else {
        brightColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}