#version 430 core

layout(location = 0) out float color;
layout(location = 1) out vec4 normal;

in vec3 pos;
in float scale;

uniform float   time;

void main(void) {
    float pattern = mod((pos.x + pos.y + pos.z) * 10.0 * scale + time * 5.0, 20.0) / 20.0;
    if (pattern < 0.5) {
        color = 1.0;
    } else {
        discard;
    }
}
