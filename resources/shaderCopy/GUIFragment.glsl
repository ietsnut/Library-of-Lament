#version 410 core

layout(location = 0) out vec4 color;

in vec2 fragUV;

uniform usampler2D texture1;

void main() {
    uint albedo = texture(texture1, fragUV).r;
    if (albedo == 16u) {
        discard;
    }
    color = PALETTE[albedo];
}
