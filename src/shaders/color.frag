#version 330

out vec4 fragColor;

uniform vec4 color;

void main() {
    fragColor = color;

    float fog = pow(.999, gl_FragCoord.z / gl_FragCoord.w);
    fragColor = mix(vec4(.6, .6, .6, 1.), fragColor, fog);
}