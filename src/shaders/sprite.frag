#version 330

in  vec2 outTexCoord;
in  vec3 outColor;
out vec4 fragColor;

uniform vec4 color;
uniform sampler2D texture_sampler;

void main() {
    float distanceFade = pow(.995, gl_FragCoord.z / gl_FragCoord.w);
    vec3 outColor2 = 1 - (1 - outColor) * distanceFade;
    fragColor = color * vec4(outColor2, 1.) * texture(texture_sampler, outTexCoord);

    float fog = pow(.999, gl_FragCoord.z / gl_FragCoord.w);
    fragColor = mix(vec4(.6, .6, .6, 1.), fragColor, fog);
}