#version 410 core

in vec2 fragUV;
in vec3 fragPosition;
in vec3 fragNormal;
in vec3 fragViewPosition; // View space position for fog calculation

layout(location = 0) out uint color;

uniform usampler2D texture1;

uniform int     lights;
uniform vec3    lightPosition[MAX_LIGHTS];
uniform vec3    lightAttenuation[MAX_LIGHTS]; // (constant, linear, quadratic)
uniform float   lightIntensity[MAX_LIGHTS];

uniform float fogDensity;
uniform float fogGradient;
uniform float skyColor; // Changed back to vec3 as it should be

void main(void) {

    color = texture(texture1, fragUV).r;

    if (color == 0u) {
        discard;
    }

    float normalizedGray = float(color - 1u) / 254.0;
    vec3 normal = normalize(fragNormal);

    // Ambient lighting term
    float ambient = 0.0; // Changed back to 0.2 to match your original

    // Accumulate diffuse lighting from all lights
    float diffuse = 0.0;

    for (int i = 0; i < lights; i++) {
        vec3 lightDir = lightPosition[i] - fragPosition;
        float distance = length(lightDir);
        lightDir = normalize(lightDir);

        // Diffuse term (Lambertian)
        float diff = max(dot(normal, lightDir), 0.0);

        // Attenuation: 1 / (constant + linear * d + quadratic * d^2)
        float attenuation = 1.0 / (
        lightAttenuation[i].x +
        lightAttenuation[i].y * distance +
        lightAttenuation[i].z * distance * distance
        );

        diffuse += diff * attenuation * lightIntensity[i];
    }

    float lighting = clamp(ambient + diffuse, 0.0, 1.0);
    float litColor = normalizedGray * lighting;
    float distance = length(fragViewPosition);
    float visibility = exp(-pow((distance * fogDensity), fogGradient));
    visibility = clamp(visibility, 0.0, 1.0);
    float finalColor = mix(skyColor, litColor, visibility);
    color = uint(finalColor * 246.0) + 9;
}