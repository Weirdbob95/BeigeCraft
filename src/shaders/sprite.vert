#version 330

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

layout (location=0) in vec3 position_in;
layout (location=2) in vec2 texCoord_in;
layout (location=3) in vec4 color_in;

out vec2 texCoord;
out vec4 fragColor;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position_in, 1.0);
    texCoord = texCoord_in;
    fragColor = color_in;
}