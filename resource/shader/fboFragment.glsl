#version 400 core

in vec2 textureCoord;
out vec4 color;

uniform sampler2D textureSampler;

void main(void) {
    
    color = texture(textureSampler, textureCoord);

    float grayscale = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    if (grayscale > 0.5) {
        color = vec4(1.0);
    } else {
        discard;
        //color = vec4(0.0, 0.0, 0.0, 1.0);
    }

}
