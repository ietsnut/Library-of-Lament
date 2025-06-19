#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;

out vec2 fragUV;
out vec3 fragPosition;
out vec3 fragViewPosition; // Pass view space position for fog calculation

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main(void) {
    fragUV = uv;
    vec4 worldSpace = model * vec4(position, 1.0);
    fragPosition = worldSpace.xyz;
    vec4 viewSpace = view * worldSpace;
    fragViewPosition = viewSpace.xyz;
    gl_Position = projection * viewSpace;
}