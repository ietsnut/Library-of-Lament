#version 410 core

in vec2     fragUV;
in vec3     fragNormal;

layout(location = 0) out uint color;
layout(location = 1) out vec3 normal;

uniform sampler2D texture1;

uint t(sampler2D s, vec2 v) {
    return (int(texture(s, v).x * 255.0) >> (1 - int(mod(v.x * float(textureSize(s, 0).x * 2), 2.0))) * 4) & 0x07;
}


void main(void) {
    color = t(texture1, fragUV) & 0x7u;
    if (color == 0) {
        discard;
    }
    normal = fragNormal;
}




