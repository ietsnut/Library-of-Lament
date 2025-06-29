#version 410 core

in vec2 fragUV;

layout(location = 0) out vec4 color;

uniform sampler2D texture1;
uniform bool horizontal;
uniform int width;
uniform int height;

float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 tex_offset = 1.0 / textureSize(texture1, 0);
    vec3 result = texture(texture1, fragUV).rgb * weight[0];

    if (horizontal) {
        for (int i = 1; i < 5; ++i) {
            result += texture(texture1, fragUV + vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
            result += texture(texture1, fragUV - vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
        }
    } else {
        for (int i = 1; i < 5; ++i) {
            result += texture(texture1, fragUV + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
            result += texture(texture1, fragUV - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
        }
    }

    color = vec4(result, 1.0);
}