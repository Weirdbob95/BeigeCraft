#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec2 texPosition;
layout (location=3) in vec3 color;

out vec2 outTexCoord;
out vec2 outTexPosition;
out vec3 outColor;
out vec4 viewSpace;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main() {
    viewSpace = modelViewMatrix * vec4(position, 1.);
    gl_Position = projectionMatrix * viewSpace;
    outTexCoord = texCoord;
    outTexPosition = texPosition;
    outColor = color;
}