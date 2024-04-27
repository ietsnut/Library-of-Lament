#version 410 core

layout(location = 0) out vec4 color;

in vec3 pos;

uniform float   time;

void main(void) {
    float pattern = mod((pos.x + pos.y + pos.z) * 20.0 + time * 5.0, 20.0) / 20.0;
    if (pattern < 0.5) {
        color = vec4(1.0);
    } else {
        discard;
    }
}
