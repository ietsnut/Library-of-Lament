#version 410 core

layout(location = 0) out uint color;
layout(location = 1) out vec3 normal;

in vec2 fragUV;

uniform usampler2D texture1;

uint t(usampler2D s, vec2 v) {
    ivec2 p = ivec2(fract(v) * vec2(textureSize(s,0) * ivec2(4,1)));
    return (texelFetch(s, ivec2(p.x/4, p.y), 0).r >> (6 - (p.x%4)*2)) & 3u;
}

void main() {
    color = t(texture1, fragUV);
    if (color == 0) {
        discard;
    }
}
