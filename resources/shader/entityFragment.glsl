#version 410 core

in vec2 fragUV;
in vec3 fragPosition;

layout(location = 0) out uint color;

uniform usampler2D texture1;

uniform int state;
uniform int states;

void main(void) {
    vec2 atlasUV = fragUV;

    // If we have multiple states, adjust UV coordinates for atlas
    if (states > 1) {
        float stateWidth = 1.0 / float(states);
        atlasUV.x = (fragUV.x * stateWidth) + float(state) * stateWidth;
    }

    color = texture(texture1, atlasUV).r;
    if (color == 16u) {
        discard;
    }
    color += 1;  // Range: 1-15 (base colors with fog level 0)
}