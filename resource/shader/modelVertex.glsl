#version 400 core
#define LIGHTS 2

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 textureCoord;
out float visibility;
out vec3 fragPosition;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

const float fogDensity = 0.007;
const float fogGradient = 1.5;

void main(void) {
	vec4 worldPosition = modelMatrix * vec4(position, 1.0);
	fragPosition = worldPosition.xyz;
	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	textureCoord = uv;
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance * fogDensity), fogGradient));
	visibility = clamp(visibility, 0.0, 1.0);
}
