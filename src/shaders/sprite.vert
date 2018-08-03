#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=1) in vec3 color;

out vec2 outTexCoord;
out vec3 outColor;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    outTexCoord = texCoord;
    outColor = color;
}