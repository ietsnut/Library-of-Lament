#version 410 core

layout(location = 0) in vec2 position;

uniform vec2 guiPosition;
uniform vec2 guiScale;
uniform float guiRotation;

out vec2 fragUV;

void main() {
    // Scale first
    vec2 scaled = guiScale * position;

    // Apply 2D rotation
    float cosR = cos(guiRotation);
    float sinR = sin(guiRotation);
    vec2 rotated = vec2(
    cosR * scaled.x - sinR * scaled.y,
    sinR * scaled.x + cosR * scaled.y
    );

    vec2 finalPos = guiPosition + rotated;

    fragUV = position * 0.5 + 0.5;
    gl_Position = vec4(finalPos * 2.0 - 1.0, 0.0, 1.0); // from [0,1] to NDC
}