#version 400 core
#define LIGHTS 2

in vec2 textureCoord;
in float visibility;
in vec3 fragPosition;

out vec4 color;

uniform sampler2D modelTexture;
uniform vec3 lightPosition[LIGHTS];
uniform vec3 lightAttenuation[LIGHTS];

void main(void) {

    vec4 baseColor = texture(modelTexture, textureCoord);
    if (baseColor.a < 0.5) {
        discard;
    }

    vec3 lightEffect = vec3(0.2);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition  - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation); // Since light color is white
    }

    color = vec4(baseColor.rgb * lightEffect, baseColor.a) * visibility;
}
