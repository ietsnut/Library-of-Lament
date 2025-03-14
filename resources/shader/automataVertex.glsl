#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 fragUV;
out vec3 fragNormal;

uniform mat4 model;
uniform mat4 vp;

const vec2 resolution = vec2(WIDTH, HEIGHT);  // screen resolution in pixels

void main(void) {

    fragUV = uv;
    fragNormal = normalize(normal);

    vec4 pos = vp * model * vec4(position, 1.0);

    // Aspect ratio correction (assuming ASPECT is defined as a macro or uniform)
    if (ASPECT > 1.0) {
        pos.x /= ASPECT;
    } else {
        pos.y *= ASPECT;
    }

    // Convert clip-space position to normalized device coordinates (NDC)
    vec3 ndc = pos.xyz / pos.w;

    // Map NDC ([-1,1]) to screen coordinates ([0,resolution])
    vec2 screenPos = ((ndc.xy * 0.5) + 0.5) * resolution;

    // Snap screen coordinates to the nearest integer pixel
    screenPos = floor(screenPos + 0.5);

    // Convert snapped screen coordinates back to NDC
    vec2 snappedNDC = ((screenPos / resolution) - 0.5) * 2.0;

    // Apply the new snapped coordinates back to our clip-space position
    pos.xy = snappedNDC * pos.w;

    gl_Position = pos;
}
