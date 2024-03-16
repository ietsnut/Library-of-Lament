#version 400 core

in vec2 textureCoord;

layout(location = 0) out vec4 color;
layout(location = 1) out vec4 normal;

uniform sampler2D modelTexture;

void main(void){

    color = texture(modelTexture, textureCoord);
    normal = vec4(1.0);
    if (color.a < 0.5) {
        discard;
    }

}