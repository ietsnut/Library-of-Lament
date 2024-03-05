#version 400 core
#define MAX_LIGHTS 8

in vec2 pass_textureCoordinates;
in vec3 surfaceNormal;
in vec3 toLightVector[4];
in vec3 toCameraVector;
in float visibility;

out vec4 out_Color;

uniform sampler2D modelTexture;
uniform vec3 lightIntensity[4];
uniform float shineDamper;
uniform float reflectivity;
uniform vec3 skyColour;

const float density = 0.007;
const float gradient = 1.5;

void main(void) {

    vec4 textureColour = texture(modelTexture, pass_textureCoordinates);

    if (textureColour.a < 0.5) {
        discard;
    }

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitVectorToCamera = normalize(toCameraVector);

    vec3 totalDiffuse = vec3(0.0);
    vec3 totalSpecular = vec3(0.0);

    for (int i = 0; i < MAX_LIGHTS; i++) {
        float distance = length(toLightVector[i]);
        float attFactor = (lightIntensity[i].x) + (lightIntensity[i].y * distance) + (lightIntensity[i].z * distance * distance);
        vec3 unitLightVector = normalize(toLightVector[i]);
        float nDotl = dot(unitNormal, unitLightVector);
        float brightness = max(nDotl, 0.0);
        vec3 lightDirection = -unitLightVector;
        vec3 reflectedLightDirection = reflect(lightDirection, unitNormal);
        float specularFactor = dot(reflectedLightDirection, unitVectorToCamera);
        specularFactor = max(specularFactor, 0.0);
        float dampedFactor = pow(specularFactor, shineDamper);
        totalDiffuse = totalDiffuse + (brightness * vec3(1.0)) / attFactor;
        totalSpecular = totalSpecular + (dampedFactor * reflectivity * vec3(1.0)) / attFactor;
    }

    totalDiffuse = max(totalDiffuse, 0.2);

    out_Color = vec4(totalDiffuse, 1.0) * textureColour + vec4(totalSpecular, 1.0);
    out_Color = mix(vec4(1.0), out_Color, visibility);

}
