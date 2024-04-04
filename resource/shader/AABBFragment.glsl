#version 430 core

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

uniform bool selected;

void main(void) {
    if (selected) {
        color = 1.0;
        normal = vec4(1.0);
    } else {
        discard;
    }
}