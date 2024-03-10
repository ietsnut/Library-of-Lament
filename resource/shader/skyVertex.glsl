#version 400 core

layout(location = 0) in vec3 position;

out vec3 textureCoord;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void){

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
	textureCoord = position;
	
}