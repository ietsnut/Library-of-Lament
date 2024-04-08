#version 430 core
#define LIGHTS 2

in vec2     fragUV;
in vec3     fragPosition;
in vec3     fragNormal;
in float    fragDepth;

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

layout(location = 0) uniform sampler2D modelTexture;

uniform vec3    lightPosition[LIGHTS];
uniform vec3    lightAttenuation[LIGHTS];
uniform float   lightIntensity[LIGHTS];

uniform int     frame;
uniform int     frames;

void main(void) {

    float frameW    = 1.0 / float(frames);
    float offset    = frameW * float(frame);
    vec2 atlasUV    = vec2(fragUV.x * frameW + offset, fragUV.y);
    vec4 albedo     = texture(modelTexture, atlasUV);
    normal          = vec4(fragNormal.xyz, fragDepth);

    if (albedo.a < 0.5) {
        discard;
    }

    vec3 lightEffect = vec3(0);

    for(int i = 0; i < LIGHTS; i++) {
        float distance      = length(fragPosition - lightPosition[i]);
        float attenuation   = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect         += vec3(attenuation * lightIntensity[i]);
    }

    lightEffect     = clamp(lightEffect, 0.0, 0.8);
    vec3 diffuse    = albedo.rgb * lightEffect;
    color           = dot(diffuse, GRAYSCALE);

}



