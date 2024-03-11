#version 400 core

in vec2 textureCoord;

out vec4 color;

uniform sampler2D modelTexture;

void main(void){

    color = texture(modelTexture, textureCoord);
    if (color.a < 0.5) {
        discard;
    }

}