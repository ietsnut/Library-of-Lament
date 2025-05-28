#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;

layout(location = 0) out uint color;
layout(location = 1) out vec3 normal;

uniform sampler2D texture1;

uniform int     lights;
uniform vec3    lightPosition[MAX_LIGHTS];
uniform vec3    lightAttenuation[MAX_LIGHTS];
uniform float   lightIntensity[MAX_LIGHTS];

uniform float   illumination;

uint t(sampler2D s, vec2 v) {
    return int(texture(s,v).x*255.)>>(3-int(mod(v.x/(1./textureSize(s,0).x)*4.,4.)))*2&3;
}

void main(void) {
    color = t(texture1, fragUV);
    if (color == 0) {
        discard;
    }
    float lightEffect = illumination;
    for(int i = 0; i < lights; i++) {
        float distance      = length(fragPosition - lightPosition[i]);
        float attenuation   = lightIntensity[i] / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect         += attenuation;
    }
    lightEffect += (fract(sin(dot(fragUV.xy ,vec2(12.9898,78.233))) * 43758.5453) - 0.5) * 0.1;
    lightEffect = clamp(lightEffect, 0.0, 1.0);
    color += int(lightEffect * 3.0);
    normal = fragNormal;
}




