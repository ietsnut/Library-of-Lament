#version 410 core

layout(location = 0) out float color;

in vec3 pos;

uniform float   time;
uniform float   scale;

void main(void) {
    float pattern = mod((pos.x + pos.y + pos.z) * (100.0 / scale) + time * 5.0, 20) / 20;
    if (pattern < 0.5) {
        color = 5.0 / 6.0;
    } else {
        discard;
    }
}
