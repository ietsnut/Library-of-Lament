#version 430 core
#define LIGHTS 2

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in float fragDepth;

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

layout(location = 0) uniform sampler2D terrainTexture;

uniform vec3 lightPosition[LIGHTS];
uniform vec3 lightAttenuation[LIGHTS];
uniform float lightIntensity[LIGHTS];

uniform float tiling;

vec4 tile(int i, float size) {
    vec2 atlasUV = vec2(mod(fragUV.x * size, 1.0), mod(fragUV.y * size, 1.0));
    float offsetX = float(i) / 5.0;
    atlasUV.x = atlasUV.x / 5.0 + offsetX;
    return texture(terrainTexture, atlasUV);
}

void main(void) {
    vec4 blendMapColour = tile(4, 1.0);
    float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoords = fragUV * 40;
    vec4 backgroundTextureColour = tile(0, tiling) * backTextureAmount;
    vec4 rTextureColour = tile(1, tiling) * blendMapColour.r;
    vec4 gTextureColour = tile(2, tiling) * blendMapColour.g;
    vec4 bTextureColour = tile(3, tiling) * blendMapColour.b;
    vec4 albedo = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;
    vec3 lightEffect = vec3(0.0);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation * lightIntensity[i]);
    }
    lightEffect = clamp(lightEffect, 0.0, 1.0);
    vec3 diffuse = albedo.rgb * lightEffect;
    color = dot(diffuse, GRAYSCALE);
    normal = vec4(vec3(0.0), fragDepth);
}
