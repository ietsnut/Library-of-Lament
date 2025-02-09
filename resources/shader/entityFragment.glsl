#version 410 core

in vec2     fragUV;
in vec3     fragPosition;
in vec3     fragNormal;

layout(location = 0) out uint   color;
layout(location = 1) out vec3   normal;

uniform sampler2D texture1;

int t(sampler2D s, vec2 v) {
    return int(texture(s,v).x*255.)>>(3-int(mod(v.x/(1./textureSize(s,0).x)*4.,4.)))*2&3;
}

void main(void) {
    int albedo = t(texture1, fragUV);
    if (albedo == 0) {
        discard;
    }
    color = (uint(albedo) - 1u) & 0x3u;
    normal = fragNormal;
}




