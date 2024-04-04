#version 430 core

in  vec2 fragUV;
out vec3 color;

uniform sampler2D colorTexture;
uniform sampler2D normalTexture;
uniform sampler2D noiseTexture;

uniform int width;
uniform int height;

uniform bool noise;

/*
uniform float stoneX;
uniform float stoneY;
uniform float stoneW;
uniform float stoneH;
*/

float sobel3(sampler2D sampler) {

    float dx        = (1.0 / width);
    float dy        = (1.0 / height);

    vec2 uvCenter   = fragUV;
    vec2 uvRight    = vec2(uvCenter.x + dx, uvCenter.y);
    vec2 uvTop      = vec2(uvCenter.x,      uvCenter.y - dx);
    vec2 uvTopRight = vec2(uvCenter.x + dx, uvCenter.y - dx);

    vec4 mCenter    = texture(sampler, uvCenter)      * 2.0 - vec4(1.0);
    vec4 mTop       = texture(sampler, uvTop)         * 2.0 - vec4(1.0);
    vec4 mRight     = texture(sampler, uvRight)       * 2.0 - vec4(1.0);
    vec4 mTopRight  = texture(sampler, uvTopRight)    * 2.0 - vec4(1.0);

    vec4 dT         = abs(mCenter - mTop);
    vec4 dR         = abs(mCenter - mRight);
    vec4 dTR        = abs(mCenter - mTopRight);

    float dTmax     = max(dT.x,     max(dT.y,   max(dT.z,   dT.w)));
    float dRmax     = max(dR.x,     max(dR.y,   max(dR.z,   dR.w)));
    float dTRmax    = max(dTR.x,    max(dTR.y,  max(dTR.z,  dTR.w)));

    return max(max(dTmax, dRmax), dTRmax);

}

float sobel1(sampler2D sampler) {

    float dx        = (1.0 / width);
    float dy        = (1.0 / height);

    vec2 uvCenter   = fragUV;
    vec2 uvRight    = vec2(uvCenter.x + dx, uvCenter.y);
    vec2 uvTop      = vec2(uvCenter.x,      uvCenter.y - dy);
    vec2 uvTopRight = vec2(uvCenter.x + dx, uvCenter.y - dy);

    float mCenter   = texture(sampler, uvCenter).r      * 2.0 - 1.0;
    float mTop      = texture(sampler, uvTop).r         * 2.0 - 1.0;
    float mRight    = texture(sampler, uvRight).r       * 2.0 - 1.0;
    float mTopRight = texture(sampler, uvTopRight).r    * 2.0 - 1.0;

    float dT        = abs(mCenter - mTop);
    float dR        = abs(mCenter - mRight);
    float dTR       = abs(mCenter - mTopRight);

    return max(max(dT, dR), dTR);

}

float sobel2(sampler2D sampler) {

    float dx        = (1.0 / width);
    float dy        = (1.0 / height);

    vec2 uvCenter   = fragUV;
    vec2 uvRight    = vec2(uvCenter.x + dx, uvCenter.y);
    vec2 uvTop      = vec2(uvCenter.x,      uvCenter.y - dy);
    vec2 uvTopRight = vec2(uvCenter.x + dx, uvCenter.y - dy);

    float mCenter   = texture(sampler, uvCenter).a      * 2.0 - 1.0;
    float mTop      = texture(sampler, uvTop).a         * 2.0 - 1.0;
    float mRight    = texture(sampler, uvRight).a       * 2.0 - 1.0;
    float mTopRight = texture(sampler, uvTopRight).a    * 2.0 - 1.0;

    float dT        = abs(mCenter - mTop);
    float dR        = abs(mCenter - mRight);
    float dTR       = abs(mCenter - mTopRight);

    return max(max(dT, dR), dTR);

}
/*
void main(void) {
    float albedo = texture(colorTexture, fragUV).r;
    color = albedo > 0.5 ? vec3(1.0) : vec3(0.0);
}*/

void main(void) {

/*
    vec2 normalizedFragPos = gl_FragCoord.xy / vec2(width, height);

    if (normalizedFragPos.x >= stoneX + 0.05 && normalizedFragPos.x <= (stoneX + stoneW - 0.05) &&
    normalizedFragPos.y >= stoneY + 0.05 && normalizedFragPos.y <= (stoneY + stoneH -0.05)) {

    }*/

    float albedoDelta   = sobel1(colorTexture);
    float depthDelta    = sobel2(normalTexture);
    float normalDelta   = sobel3(normalTexture);

    if (normalDelta > 0.01 && albedoDelta > 0.35) {
        color           = vec3(1.0);
    } else if (depthDelta > 0.2) {
        color           = vec3(1.0);
    } else  {
        color           = texture(colorTexture, fragUV).r > 0.5 ? vec3(1.0) : vec3(0.0);
    }

    vec2 centeredUV = (fragUV - 0.5) * 2.0;
    centeredUV.x *= width / height; // Correct for aspect ratio

    // Scale down the pattern to make the crosshair small
    float scale = 0.05;
    centeredUV /= scale;

    // Pattern creation:
    // Calculate distance from the center
    float dist = length(centeredUV);

    // Calculate the angle and use it to create a repeating pattern
    float angle = atan(centeredUV.y, centeredUV.x);
    float pattern = cos(6.0 * angle) * sin(dist); // The 8.0 controls the number of repetitions

    // Normalize the pattern to [0, 1] range
    pattern = pattern * 0.5 + 0.5;

    // Use step function to create a sharp pattern
    float sharpPattern = step(0.5, pattern);

    // Calculate a mask to ensure the pattern is only drawn in the center of the screen
    float mask = step(0.95, 1.0 - dist * scale);

    // Combine the pattern with the mask
    sharpPattern *= mask;

    // Set the color of the crosshair: white pattern, transparent background
    color = sharpPattern > 0.5 ? vec3(1.0) : color;

}
