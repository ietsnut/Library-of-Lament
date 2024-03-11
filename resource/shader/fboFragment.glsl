#version 400 core

in vec2 textureCoord;
out vec4 color;

uniform sampler2D textureSampler;

void main(void) {
    vec4 texColor = texture(textureSampler, textureCoord);
    float grayscale = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    if (grayscale > 0.5) {
        color = vec4(1.0);
    } else {
        discard;
    }
}
