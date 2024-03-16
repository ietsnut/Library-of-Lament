#version 400 core
#define LIGHTS 2

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in float visibility;
in float depth;

layout(location = 0) out vec4 color;
layout(location = 1) out vec4 normal;

uniform sampler2D modelTexture;
uniform vec3 lightPosition[LIGHTS];
uniform vec3 lightAttenuation[LIGHTS];
uniform float lightIntensity[LIGHTS];

void main(void) {

    vec4 baseColor = texture(modelTexture, fragUV);

    if (baseColor.a < 0.5) {
        discard;
    }

    vec3 lightEffect = vec3(0.2);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition  - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation * lightIntensity[i]);
    }

    color = vec4(baseColor.rgb * lightEffect, baseColor.a) * visibility;

    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    color = vec4(gray, gray, gray, color.a);

    normal = vec4((fragNormal.xyz * 0.5) + 0.5, depth);

}
