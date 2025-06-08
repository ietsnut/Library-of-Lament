#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragViewPosition;

layout(location = 0) out uint color;

uniform usampler2D texture1;

uniform float fogDensity;
uniform float fogGradient;

void main(void) {
    uint baseColor = texture(texture1, fragUV).r;
    if (baseColor == 16u) {
        discard;
    }
    color = (baseColor + 1u) + (uint(clamp(exp(-pow((length(fragViewPosition) * fogDensity), fogGradient)) + ((fract(sin(dot(fragUV, vec2(12.9898, 78.233))) * 43758.5453)) - 0.5) * 0.1, 0.0, 1.0) * 13.0 + 0.5) * 16u);
}