#version 330

layout (location=0) in vec3 position_in;
layout (location=1) in float normal_in;
layout (location=2) in float texID_in;
layout (location=3) in vec4 occlusion_in;

out int normal;
out int texID;
out vec4 occlusion;

void main() {
    gl_Position = vec4(position_in, 1);
    normal = int(normal_in);
    texID = int(texID_in);
    occlusion = occlusion_in;
}