#version 330

uniform vec4 color;
uniform sampler2D texture_sampler;

in  vec2 texCoord;
in  vec4 fragColor;
out vec4 finalColor;

void main() {
    finalColor = color * fragColor * texture(texture_sampler, texCoord);
}