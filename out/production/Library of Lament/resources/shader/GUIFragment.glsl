#version 410 core

layout(location = 0) out vec4 color;

in vec2 fragUV;

uniform sampler2D texture1;

uint t(sampler2D s, vec2 v) {
    return int(texture(s,v).x*255.)>>(3-int(mod(v.x/(1./textureSize(s,0).x)*4.,4.)))*2&3;
}

void main() {
    uint albedo = t(texture1, fragUV);
    if (albedo == 0) {
        discard;
    }
    if (albedo == 1) {
        color = PALETTE[0];
    }
    if (albedo == 2) {
        color = PALETTE[4];
    }
    if (albedo == 3) {
        color = PALETTE[5];
    }
}
