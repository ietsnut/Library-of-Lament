#version 430 core

layout(location = 0) in vec3 position;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main(void) {
    vec4 worldSpace = model * vec4(position, 1.0);
    gl_Position = projection * view * worldSpace;
}