#version 410 core

in vec2     fragUV;
in vec3     fragPosition;
in vec3     fragNormal;
in float    fragDepth;
in float    size;

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

uniform sampler2D modelTexture;

uniform int     tile;
uniform int     tiles;
uniform int     lights;

uniform vec3    lightPosition[LIGHTS];
uniform vec3    lightAttenuation[LIGHTS];
uniform float   lightIntensity[LIGHTS];

/*
int tex(sampler2D samp, vec2 uv) {
    float texelSize = 1.0 / textureSize(samp, 0).x;
    float texColor = texture(samp, uv).r;
    float positionInTexel = mod(uv.x / texelSize * 4.0, 4.0);
    int index = int(positionInTexel);
    int value = int(texColor.r * 255.0);
    int shift = (3 - index) * 2;
    int code = (value >> shift) & 0x03;
    if (code == 0) {
        discard;
    }
    return code;
}*/

void main(void) {
    vec4 albedo     = texture(modelTexture, fragUV);
    if (albedo.a < 0.5) {
        discard;
    }
    normal          = vec4(fragNormal.xyz, fragDepth);
    color = dot(albedo.rgb, GRAYSCALE);
    /*
    vec2 atlasUV    = vec2(mod(fragUV.x, 1.0), mod(fragUV.y, 1.0));
    atlasUV.x       = atlasUV.x / tiles + (float(tile) / tiles);
    vec4 albedo     = texture(modelTexture, atlasUV);
    normal          = vec4(fragNormal.xyz, fragDepth);
    if (albedo.a < 0.5) {
        discard;
    }
    color = dot(albedo.rgb, GRAYSCALE);*/
    float lightEffect = 0.0;
    for(int i = 0; i < lights; i++) {
        float distance      = length(fragPosition - lightPosition[i]);

        //float attenuation   = lightIntensity[i] / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect         += distance;
    }
    /*
    switch(albedo) {
        case 1:
            color = 0.0;
            break;
        case 2:
            color = 0.5;
            break;
        case 3:
            color = 1.0;
            break;
    }*/
    //lightEffect     = clamp(lightEffect, 0.0, 1.0);
    //color           *= lightEffect;
}




