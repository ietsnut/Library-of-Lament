#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragViewPosition;

layout(location = 0) out uint sceneColor;
layout(location = 1) out vec4 brightColor;

uniform usampler2D texture1;

uniform int state;
uniform int states;

uniform float fogDensity;
uniform float fogGradient;

const float threshold = 0.75;

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

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

    float viewDistance = length(fragViewPosition);
    float visibility = exp(-pow((viewDistance * fogDensity), fogGradient));
    float noise = rand(fragUV) - 0.5;
    visibility += noise * 0.1;
    visibility = clamp(visibility, 0.0, 1.0);
    uint fogLevel = uint((1.0 - visibility) * 13.0 + 0.5);
    uint stride = 16u;
    sceneColor = (baseColor + 1u) + (fogLevel * stride);

    vec4 baseColorRGB = PALETTE[baseColor];
    float fogFactor = float(fogLevel) / 14.0;
    vec4 actualColor = mix(baseColorRGB, PALETTE[5], fogFactor);
    if (fogLevel <= 12u) {
        float brightness = dot(actualColor.rgb, GRAYSCALE);
        if (brightness > threshold) {
            float bloomStrength = (brightness - threshold) / (1.0 - threshold);
            bloomStrength = pow(bloomStrength, 0.5) * 2.0;
            brightColor = vec4(actualColor.rgb * bloomStrength, 1.0);
        } else {
            brightColor = vec4(0.0, 0.0, 0.0, 1.0);
        }
    } else {
        brightColor = vec4(0.0, 0.0, 0.0, 1.0);
    }
}