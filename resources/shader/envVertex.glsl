#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 fragUV;
out vec3 fragPosition;
out vec3 fragNormal;
out vec3 fragViewPosition; // Pass view space position for fog calculation

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main(void) {
    fragUV = uv;

    // Transform normal to world space and normalize
    fragNormal = normalize(mat3(model) * normal);

    vec4 worldSpace = model * vec4(position, 1.0);
    fragPosition = worldSpace.xyz;

    vec4 viewSpace = view * worldSpace;
    fragViewPosition = viewSpace.xyz; // Pass view space position to fragment shader

    gl_Position = projection * viewSpace;
}