#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;

out vec2 fragUV;
out vec3 fragPosition;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main(void) {
    fragUV = uv;
    vec4 worldSpace = model * vec4(position, 1.0);
    fragPosition = worldSpace.xyz;
    gl_Position = projection * view * worldSpace;
}