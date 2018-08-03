#version 330

in  vec2 outTexCoord;
in  vec3 outColor;
out vec4 fragColor;

uniform vec4 color;
uniform sampler2D texture_sampler;

void main() {
    fragColor = color * vec4(outColor, 1.) * texture(texture_sampler, outTexCoord);
}