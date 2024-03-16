#version 400 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 fragUV;
out vec3 fragPosition;
out vec3 fragNormal;
out float visibility;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

const float fogDensity = 0.07;
const float fogGradient = 1.5;

void main(void) {
	fragUV = uv;
	fragNormal = normalize(normal);
	vec4 worldPosition = modelMatrix * vec4(position, 1.0);
	fragPosition = worldPosition.xyz;
	vec4 positionRelativeToCam = viewMatrix * worldPosition;
	gl_Position = projectionMatrix * positionRelativeToCam;
	float distance = length(positionRelativeToCam.xyz);
	visibility = exp(-pow((distance * fogDensity), fogGradient));
	visibility = clamp(visibility, 0.0, 1.0);
}
