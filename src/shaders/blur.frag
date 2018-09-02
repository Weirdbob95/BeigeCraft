#version 330

uniform sampler2D texture_sampler;
uniform bool horizontal;
uniform float weight[26] = float[] (0.029193, 0.029128, 0.028935, 0.028615, 0.028174, 0.027616, 0.02695, 0.026182, 0.025324, 0.024386, 0.023378, 0.022313, 0.021201, 0.020056, 0.018888, 0.01771, 0.016531, 0.015363, 0.014214, 0.013092, 0.012006, 0.010961, 0.009962, 0.009014, 0.008121, 0.007283);

in  vec2 texCoord;

out vec4 finalColor;

void main() {
    vec2 tex_offset = 1.0 / textureSize(texture_sampler, 0); // gets size of single texel
    vec3 result = texture(texture_sampler, texCoord).rgb * weight[0]; // current fragment's contribution
    if(horizontal)
    {
        for(int i = 1; i < 26; ++i)
        {
            result += texture(texture_sampler, texCoord + vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
            result += texture(texture_sampler, texCoord - vec2(tex_offset.x * i, 0.0)).rgb * weight[i];
        }
    }
    else
    {
        for(int i = 1; i < 26; ++i)
        {
            result += texture(texture_sampler, texCoord + vec2(0.0, tex_offset.y * i)).rgb * weight[i];
            result += texture(texture_sampler, texCoord - vec2(0.0, tex_offset.y * i)).rgb * weight[i];
        }
    }
    finalColor = vec4(result, 1.0);
}