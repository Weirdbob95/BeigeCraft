#version 330

in  vec2 outTexCoord;
in  vec2 outTexPosition;
in  vec3 outColor;
in  vec4 viewSpace;
out vec4 fragColor;

uniform vec4 color;
uniform sampler2D texture_sampler;

void main() {
    vec2 boundedTexCoord = clamp(outTexCoord, .001, .999) * 16;
    vec2 finalTexCoord = (boundedTexCoord + outTexPosition) / textureSize(texture_sampler, 0);
    vec4 textureColor = texture(texture_sampler, finalTexCoord);

    float fog = pow(.01, pow(length(viewSpace) * .001, 2));
    vec4 preFogColor = color * mix(vec4(1.), vec4(outColor, 1.), fog) * textureColor;
    fragColor = mix(vec4(.6, .8, 1., 1.), preFogColor, fog);
}