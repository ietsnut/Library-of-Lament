#version 410 core

layout(location = 0) in vec3 position;
layout(location = 1) in vec2 uv;
layout(location = 2) in vec3 normal;

out vec2 fragUV;
out vec3 fragNormal;

uniform mat4 model;
uniform mat4 vp;

void main(void) {

    fragUV = uv;
    fragNormal = normalize(normal);

    vec4 pos = vp * model * vec4(position, 1.0);

    if (ASPECT > 1.0) {
        pos.x /= ASPECT;
    } else {
        pos.y *= ASPECT;
    }

    gl_Position = pos;

}
