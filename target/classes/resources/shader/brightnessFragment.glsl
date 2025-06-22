#version 410 core

in vec2 fragUV;

layout(location = 0) out vec4 color;

uniform usampler2D texture1;
uniform float brightnessThreshold;

void main() {
    uint value = texture(texture1, fragUV).r;

    // Convert indexed color to actual color
    vec4 sceneColor;
    if (value == 0u) {
        sceneColor = PALETTE[5]; // Background color
    } else {
        uint adjustedValue = value - 1u;
        uint baseIndex = adjustedValue % 16u;
        uint fogLevel = adjustedValue / 16u;
        vec4 baseColor = PALETTE[baseIndex];
        float fogFactor = float(fogLevel) / 14.0;
        sceneColor = mix(baseColor, PALETTE[5], fogFactor);
    }

    // Calculate brightness using luminance
    float brightness = dot(sceneColor.rgb, GRAYSCALE);

    // Extract bright areas with more aggressive scaling
    if (brightness > brightnessThreshold) {
        float bloomStrength = (brightness - brightnessThreshold) / (1.0 - brightnessThreshold);
        // Boost the bloom strength significantly
        bloomStrength = pow(bloomStrength, 0.5) * 2.0;  // Power curve + multiplier for more dramatic effect
        color = vec4(sceneColor.rgb * bloomStrength, 1.0);
    } else {
        color = vec4(0.0, 0.0, 0.0, 1.0);
    }
}