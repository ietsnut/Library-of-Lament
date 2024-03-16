#version 400 core
#define LIGHTS 2

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in float visibility;

layout(location = 0) out vec4 color;
layout(location = 1) out vec4 normal;

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
    color = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

    vec3 lightEffect = vec3(0.2);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation * lightIntensity[i]);
    }

    color = vec4(color.rgb * lightEffect, color.a) * visibility;

    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    color = vec4(gray, gray, gray, color.a);
    normal = vec4(1.0);

}
