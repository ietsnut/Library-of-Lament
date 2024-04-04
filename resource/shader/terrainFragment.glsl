#version 430 core
#define LIGHTS 2

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in float fragDepth;

layout(location = 0) out float  color;
layout(location = 1) out vec4   normal;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightPosition[LIGHTS];
uniform vec3 lightAttenuation[LIGHTS];
uniform float lightIntensity[LIGHTS];

void main(void) {

    vec4 blendMapColour = texture(blendMap, fragUV);
    float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoords = fragUV * 40;
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoords) * blendMapColour.r;
    vec4 gTextureColour = texture(gTexture, tiledCoords) * blendMapColour.g;
    vec4 bTextureColour = texture(bTexture, tiledCoords) * blendMapColour.b;
    vec4 albedo = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

    vec3 lightEffect = vec3(0.0);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation * lightIntensity[i]);
    }
    lightEffect = clamp(lightEffect, 0.2, 1.2);


    vec3 diffuse = albedo.rgb * lightEffect * fragDepth;

    color = dot(diffuse, GRAYSCALE);

    normal = vec4(vec3(0.0), fragDepth);

}
