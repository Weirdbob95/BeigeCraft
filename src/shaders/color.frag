#version 330

//out vec4 fragColor;
layout (location = 0) out vec4 finalColor;
layout (location = 1) out vec4 brightColor;

uniform vec4 color;

void main() {
    finalColor = color;
    brightColor = vec4(0, 0, 0, 0);
}