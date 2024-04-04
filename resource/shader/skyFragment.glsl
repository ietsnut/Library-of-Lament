#version 430 core

in vec2 textureCoord;

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

uniform sampler2D modelTexture;

void main(void) {

    vec4 albedo = texture(modelTexture, textureCoord);
    normal = vec4(1.0);
    if (albedo.a < 0.5) {
        discard;
    }
    color = dot(albedo.rgb, GRAYSCALE);

}