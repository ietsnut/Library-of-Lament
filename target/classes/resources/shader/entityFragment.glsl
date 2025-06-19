#version 410 core

in vec2 fragUV;
in vec3 fragPosition;

layout(location = 0) out uint color;

uniform usampler2D texture1;

void main(void) {
    color = texture(texture1, fragUV).r;
    if (color == 16u) {
        discard;
    }
    color += 1;  // Range: 1-15 (base colors with fog level 0)
}