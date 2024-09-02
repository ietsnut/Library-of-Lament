#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2    fragUV;
out vec3    fragPosition;
out vec3    fragNormal;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

void main(void) {
    fragUV = uv;
    fragNormal = 0.5 * (normalize(normal) + vec3(1.0));
    vec4 worldSpace = model * vec4(position, 1.0);
    fragPosition = worldSpace.xyz;
    gl_Position = projection * view * worldSpace;
}
