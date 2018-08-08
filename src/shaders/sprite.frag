#version 330

in  vec2 outTexCoord;
out vec4 fragColor;

uniform vec4 color;
uniform sampler2D texture_sampler;

void main() {
    fragColor = color * texture(texture_sampler, outTexCoord);
}