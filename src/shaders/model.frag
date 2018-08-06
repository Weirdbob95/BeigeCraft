#version 330

in  vec3 outColor;
out vec4 fragColor;

uniform vec4 color;

void main() {
    fragColor =  color * vec4(outColor, 1.);
}