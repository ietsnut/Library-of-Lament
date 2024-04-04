#version 430 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 fragUV;
out vec3 fragPosition;
out vec3 fragNormal;
out float fragDepth;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

uniform vec3 AABBMin;
uniform vec3 AABBMax;

const float fogDensity = 0.01;

void main(void) {
	fragUV = uv;
	fragNormal = 0.5 * (normalize(normal) + vec3(1.0));
	//fragNormal = normalize(normal) * 0.5 + 0.5;
	vec4 worldSpace = model * vec4(position, 1.0);
	fragPosition = worldSpace.xyz;
	vec4 viewSpace = view * worldSpace;
	gl_Position = projection * viewSpace;

	vec4 pos = projection * view * model * vec4(AABBMin, 1.0);

	float distance = length(viewSpace.xyz);
	fragDepth = exp(-(distance * fogDensity));
	fragDepth = clamp(fragDepth, 0.0, 1.0);
}
