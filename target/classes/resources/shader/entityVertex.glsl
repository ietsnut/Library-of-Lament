#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;

out vec2 fragUV;
out vec3 fragPosition;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;
uniform float time; // Add this uniform for animated jittering

// PS2 vertex jittering function
vec4 ps2Jitter(vec4 position) {
    // Convert to screen space for jittering
    vec4 clipPos = position;

    // Simulate PS2's limited precision by snapping to a grid
    // PS2 had ~12-bit precision for screen coordinates
    float pre = 64.0; // Lower values = more jittering

    // Apply jittering in screen space
    clipPos.x = floor(clipPos.x * pre + 0.5) / pre;
    clipPos.y = floor(clipPos.y * pre + 0.5) / pre;

    // Optional: Add slight temporal jittering for more authentic feel
    float jitterStrength = 0.5;
    clipPos.x += sin(time * 60.0 + clipPos.y * 100.0) * jitterStrength / pre;
    clipPos.y += cos(time * 60.0 + clipPos.x * 100.0) * jitterStrength / pre;

    return clipPos;
}

void main(void) {
    fragUV = uv;
    vec4 worldSpace = model * vec4(position, 1.0);
    fragPosition = worldSpace.xyz;

    // Calculate position normally first
    vec4 clipSpace = projection * view * worldSpace;

    // Apply PS2 jittering effect
    gl_Position = ps2Jitter(clipSpace);
}