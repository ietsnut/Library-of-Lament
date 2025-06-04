// Fragment Shader
#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in float visibility;

layout(location = 0) out uint color;
layout(location = 1) out vec3 normal;

uniform usampler2D texture1;

uniform int     lights;
uniform vec3    lightPosition[MAX_LIGHTS];
uniform vec3    lightAttenuation[MAX_LIGHTS];
uniform float   lightIntensity[MAX_LIGHTS];

uniform float   illumination;

uint t(usampler2D s, vec2 v) {
    ivec2 p = ivec2(fract(v) * vec2(textureSize(s,0) * ivec2(4,1)));
    return (texelFetch(s, ivec2(p.x/4, p.y), 0).r >> (6 - (p.x%4)*2)) & 3u;
}

void main(void) {

    color = t(texture1, fragUV);

    if (color == 0) {
        discard;
    }
    float lightEffect = illumination;
    for(int i = 0; i < lights; i++) {
        vec3 lightDir = lightPosition[i] - fragPosition;
        float distance = length(lightDir);
        lightDir = normalize(lightDir);
        float attenuation = lightIntensity[i] / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        vec3 worldNormal = normalize(fragNormal * 2.0 - 1.0);
        float diffuse = max(dot(worldNormal, lightDir), 0.0);
        float lightingFactor = mix(1.0, diffuse, 0.8);
        lightEffect += attenuation * lightingFactor;
    }
    lightEffect += (fract(sin(dot(fragUV.xy ,vec2(12.9898,78.233))) * 43758.5453) - 0.5) * 0.1;
    lightEffect = clamp(lightEffect, 0.0, 1.0);
    color += int(lightEffect * 3.0);
    color = int(mix(0.0, color * 255.0, visibility) / 255.0);
    normal = fragNormal;
}