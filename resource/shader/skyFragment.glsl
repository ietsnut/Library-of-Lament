#version 400 core

in vec3 textureCoord;

out vec4 color;

uniform samplerCube cubeMap;

void main(void){

    color = texture(cubeMap, textureCoord);

}