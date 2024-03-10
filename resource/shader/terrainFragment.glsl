#version 400 core
#define LIGHTS 2

in vec2 textureCoord;
in float visibility;
in vec3 fragPosition;

out vec4 color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;
uniform vec3 lightPosition[LIGHTS];
uniform vec3 lightAttenuation[LIGHTS];

void main(void) {
    vec4 blendMapColour = texture(blendMap, textureCoord);
    float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoords = textureCoord * 40;
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoords) * blendMapColour.r;
    vec4 gTextureColour = texture(gTexture, tiledCoords) * blendMapColour.g;
    vec4 bTextureColour = texture(bTexture, tiledCoords) * blendMapColour.b;
    vec4 baseColor = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

    vec3 lightEffect = vec3(0.2);
    for(int i = 0; i < LIGHTS; i++) {
        float distance = length(fragPosition - lightPosition[i]);
        float attenuation = 1.0 / (lightAttenuation[i].x + lightAttenuation[i].y * distance + lightAttenuation[i].z * distance * distance);
        lightEffect += vec3(attenuation);
    }

    color = vec4(baseColor.rgb * lightEffect, baseColor.a) * visibility;
}
