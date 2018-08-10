#version 330

uniform vec4 color;
uniform sampler2D texture_sampler;

in vec2 texCoordsConst;
in vec2 texCoordsInterp;
in float fragOcclusion;
in float fog;

out vec4 finalColor;

void main() {
    vec2 texBlockSize = 16. / textureSize(texture_sampler, 0);
    vec4 textureColor = texture(texture_sampler, texCoordsConst + clamp(texCoordsInterp, vec2(.001, .001), texBlockSize - .001));

    //fragColor = textureColor * vec4(fragOcclusion, fragOcclusion, fragOcclusion, 1);
    //float maxDist = 2000;
    //float fog = pow(.01, pow(length(gViewSpace) / maxDist, 2));
    vec4 preFogColor = color * vec4(vec3(mix(1, fragOcclusion, fog)), 1) * textureColor;
    finalColor = mix(vec4(.6, .8, 1., 1.), preFogColor, fog);
}