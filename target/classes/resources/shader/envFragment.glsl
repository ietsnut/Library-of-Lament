#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragViewPosition;

layout(location = 0) out uint color;

uniform usampler2D texture1;

uniform float fogDensity;
uniform float fogGradient;

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

void main(void) {
    uint baseColor = texture(texture1, fragUV).r;
    if (baseColor == 16u) {
        discard;
    }
    float viewDistance = length(fragViewPosition);
    float visibility = exp(-pow((viewDistance * fogDensity), fogGradient));
    float noise = rand(fragUV) - 0.5;
    visibility += noise * 0.1;
    visibility = clamp(visibility, 0.0, 1.0);

    // 14 fog levels: visibility 1.0->0.0 maps to fogLevel 0->13
    uint fogLevel = uint((1.0 - visibility) * 13.0 + 0.5);  // Inverted: 0-13 fog levels
    uint stride = 16u;  // Cleaner stride
    color = (baseColor + 1u) + (fogLevel * stride);  // Range: 1-255 (fog levels 0-13)
}