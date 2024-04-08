#version 430 core

layout(location = 0) in vec3 position;

out vec3 pos;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main(void) {
    gl_Position = projection * view * model * vec4(position, 1.0);
    pos = position;
}