#version 410 core

in vec2     fragUV;
in vec3     fragPosition;
in vec3     fragNormal;
in float    fragDepth;

layout(location = 0) out vec2   color;
layout(location = 1) out vec4   normal;

uniform sampler2D modelTexture;

uniform int     tile;
uniform int     tiles;
uniform int     lights;

uniform vec3    lightPosition[LIGHTS];
uniform vec3    lightAttenuation[LIGHTS];
uniform float   lightIntensity[LIGHTS];

int get(sampler2D samp, vec2 uv) {
    float texelSize = 1.0 / textureSize(samp, 0).x;
    float texColor = texture(samp, uv).r;
    float positionInTexel = mod(uv.x / texelSize * 4.0, 4.0);
    int index = int(positionInTexel);
    int value = int(texColor * 255.0);
    int shift = (3 - index) * 2;
    int code = (value >> shift) & 0x03;
    return code;
}

int t(sampler2D s, vec2 v) {
    return int(texture(s,v).x*255.)>>(3-int(mod(v.x/(1./textureSize(s,0).x)*4.,4.)))*2&3;
}

void main(void) {

    int albedo = t(modelTexture, fragUV);
    if (albedo == 0) {
        discard;
    }
    int level = albedo - 1;

    float lightEffect = 0.0;
    for(int i = 0; i < lights; i++) {
        float distance      = length(fragPosition - lightPosition[i]);
        float attenuation   = lightIntensity[i] / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect         += attenuation;
    }
    lightEffect = clamp(lightEffect, 0.0, 1.0);

    float dither = (fract(sin(dot(fragUV.xy ,vec2(12.9898,78.233))) * 43758.5453) - 0.5) * 0.1;
    lightEffect += dither;

    if (level == 0) {
        if (lightEffect > 0.25) {
            level += 1;
        }
    } else if (level == 1) {
        if (lightEffect > 0.5) {
            level += 2;
        } else if (lightEffect > 0.25) {
            level += 1;
        }
    } else if (level == 2) {
        if (lightEffect > 0.75) {
            level += 3;
        } else if (lightEffect > 0.5) {
            level += 2;
        } else if (lightEffect > 0.25) {
            level += 1;
        }
    }

    color = vec2(float(level) / 6, lightEffect);

    normal = vec4(fragNormal, fragDepth);

}




