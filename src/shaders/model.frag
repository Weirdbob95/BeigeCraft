#version 330

uniform vec4 color;

in vec3 fragColor;

out vec4 finalColor;

void main() {
   finalColor = vec4(fragColor, 1) * color;
}