#version 330

uniform vec4 color;
uniform sampler2D texture_sampler;

in vec2 texCoords;
in float fragOcclusion;
in float fog;

out vec4 fragColor;

void main() {
    vec4 textureColor = texture(texture_sampler, texCoords);

    fragColor = textureColor * vec4(fragOcclusion, fragOcclusion, fragOcclusion, 1);
    //float maxDist = 2000;
    //float fog = pow(.01, pow(length(gViewSpace) / maxDist, 2));
    //vec4 preFogColor = color * mix(1, fragOcclusion, fog) * textureColor;
    //fragColor = mix(vec4(.6, .8, 1., 1.), preFogColor, fog);
}