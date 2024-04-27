#version 410 core

in vec3 position;
in vec2 uv;

out vec2 fragUV;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main(void){
	gl_Position = projection * view * model * vec4(position, 1.0);
	fragUV = uv;
}