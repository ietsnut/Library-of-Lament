#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;

out vec2 textureCoord;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
	textureCoord = uv;
	
}