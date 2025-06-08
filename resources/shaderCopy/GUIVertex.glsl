#version 410 core

layout(location = 0) in vec2 position;

uniform vec2 guiPosition;
uniform vec2 guiScale;

out vec2 fragUV;

void main() {
    vec2 scaledPos = guiPosition + (guiScale * position);
    fragUV = position * 0.5 + 0.5;
    gl_Position = vec4(scaledPos * 2.0 - 1.0, 0.0, 1.0); // from [0,1] to NDC
}