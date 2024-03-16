#version 400 core

in vec2 fragUV;
out vec4 color;

uniform sampler2D colorTexture;
uniform sampler2D normalTexture;

uniform int width;
uniform int height;

void main(void) {

    vec4 decodedNormalDepth = texture(normalTexture, fragUV) * 2.0 - 1.0;
    vec3 decodedNormal = decodedNormalDepth.xyz;
    float depth = decodedNormalDepth.w;

    float dx = (1.0 / width);
    float dy = (1.0 / height);

    vec2 uvCenter   = fragUV;
    vec2 uvRight    = vec2(uvCenter.x + dx, uvCenter.y);
    vec2 uvTop      = vec2(uvCenter.x, uvCenter.y - dx);
    vec2 uvTopRight = vec2(uvCenter.x + dx, uvCenter.y - dx);

    vec4 mCenter    = texture(normalTexture, uvCenter) * 2.0 - 1.0;
    vec4 mTop       = texture(normalTexture, uvTop) * 2.0 - 1.0;
    vec4 mRight     = texture(normalTexture, uvRight) * 2.0 - 1.0;
    vec4 mTopRight  = texture(normalTexture, uvTopRight) * 2.0 - 1.0;

    vec4 dT         = abs(mCenter - mTop);
    vec4 dR         = abs(mCenter - mRight);
    vec4 dTR        = abs(mCenter - mTopRight);

    float dTmax     = max(dT.x, max(dT.y, max(dT.z, dT.w)));
    float dRmax     = max(dR.x, max(dR.y, max(dR.z, dR.w)));
    float dTRmax    = max(dTR.x, max(dTR.y, max(dTR.z, dTR.w)));

    float deltaRaw = max(max(dTmax, dRmax), dTRmax);

    // Lower threshold values will discard fewer samples and give darker/thicker lines.
    float threshold = 0.8;
    float deltaClipped = clamp((deltaRaw * 2.0) - threshold, 0.0, 1.0);

    float noise = fract(sin(dot((uvCenter * 0.001).xy, vec2(12.9898,78.233))) * 43758.5453);
    float edgeIntensity = deltaClipped * (noise > 0.9 ? 1.0 : 0.0);

    vec4 outline = vec4(edgeIntensity, edgeIntensity, edgeIntensity, 1.0);
    vec4 albedo = texture(colorTexture, fragUV);
    color = min(albedo + outline, vec4(1.0));

    if (color.r > 0.5) {
        color = vec4(1.0);
    } else {
        discard;
    }
}
