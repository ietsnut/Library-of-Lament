#version 410 core

in vec2 fragUV;

layout(location = 0) out float color;
layout(location = 1) out vec4  normal;

uniform sampler2D modelTexture;

void main(void) {
    float texelSize = 1.0 / textureSize(modelTexture, 0).x;
    vec4 texColor = texture(modelTexture, fragUV);

    float positionInTexel = mod(fragUV.x / texelSize * 4.0, 4.0);
    int index = int(positionInTexel);

    int value = int(texColor.r * 255.0);
    int shift = (3 - index) * 2;
    int code = (value >> shift) & 0x03;

    if (code == 0) {
        discard;
    } else if (code == 1) {
        color = 0.0; // Black
    } else if (code == 2) {
        color = 0.5; // Black
    } else if (code == 3) {
        color = 1.0; // White
    }

    normal = vec4(1.0);
    //color = dot(albedo.rgb, GRAYSCALE);
}