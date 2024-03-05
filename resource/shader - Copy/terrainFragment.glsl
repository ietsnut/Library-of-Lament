#version 400 core
#define MAX_LIGHTS 8

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Color;

uniform sampler2D backgroundTexture;
uniform sampler2D rTexture;
uniform sampler2D gTexture;
uniform sampler2D bTexture;
uniform sampler2D blendMap;

uniform vec3 lightIntensity[4];

const float shineDamper = 0;
const float reflectivity = 0;

void main(void){

    vec4 blendMapColour = texture(blendMap, pass_textureCoordinates);

    float backTextureAmount = 1 - (blendMapColour.r + blendMapColour.g + blendMapColour.b);
    vec2 tiledCoords = pass_textureCoordinates * 40;
    vec4 backgroundTextureColour = texture(backgroundTexture, tiledCoords) * backTextureAmount;
    vec4 rTextureColour = texture(rTexture, tiledCoords) * blendMapColour.r;
    vec4 gTextureColour = texture(gTexture, tiledCoords) * blendMapColour.g;
    vec4 bTextureColour = texture(bTexture, tiledCoords) * blendMapColour.b;

    vec4 totalColour = backgroundTextureColour + rTextureColour + gTextureColour + bTextureColour;

	vec3 unitNormal = normalize(surfaceNormal);
	vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0; i < MAX_LIGHTS; i++) {
        float distance = length(toLightVector[i]);
        float attFactor = (lightIntensity[i].x) + (lightIntensity[i].y * distance) + (lightIntensity[i].z * distance * distance);
        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal,unitLightVector);
        float brightness = max(nDotl,0.0);
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection,unitNormal);
        float specularFactor = dot(reflectedLightDirection , unitVectorToCamera);
        specularFactor = max(specularFactor,0.0);
        float dampedFactor = pow(specularFactor,shineDamper);
        totalDiffuse = totalDiffuse + (brightness * vec3(1.0)) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * vec3(1.0)) / attFactor;
    }

	totalDiffuse = max(totalDiffuse, 0.2);

	out_Color =  vec4(totalDiffuse,1.0) * totalColour + vec4(totalSpecular,1.0);
	out_Color = mix(vec4(1.0), out_Color, visibility);

}