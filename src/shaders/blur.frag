#version 330

uniform sampler2D texture_sampler;
uniform bool horizontal;
uniform float weight[9] = float[] (0.08745, 0.085724, 0.080748, 0.073088, 0.06357, 0.05313, 0.042669, 0.032928, 0.024418);

in  vec2 texCoord;
in  vec4 fragColor;
out vec4 finalColor;

void main() {
    vec2 tex_offset = 1.0 / textureSize(texture_sampler, 0); // gets size of single texel
    vec3 result = texture(texture_sampler, texCoord).rgb * weight[0]; // current fragment's contribution
    if(horizontal)
    {
        for(int i = 1; i < 9; ++i)
        {
            result += texture(texture_sampler, texCoord + vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
            result += texture(texture_sampler, texCoord - vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 9; ++i)
        {
            result += texture(texture_sampler, texCoord + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
            result += texture(texture_sampler, texCoord - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
        }
    }
    finalColor = fragColor * vec4(result, 1.0);
}