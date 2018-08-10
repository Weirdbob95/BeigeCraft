#version 330

vec3 OFFSET[6] = vec3[](
    vec3(0., 0., 0.),
    vec3(1., 0., 0.),
    vec3(0., 0., 0.),
    vec3(0., 1., 0.),
    vec3(0., 0., 0.),
    vec3(0., 0., 1.)
);
vec3 NORMAL_TO_DIR1[6] = vec3[](
    vec3(0., 1., 0.),
    vec3(0., 1., 0.),
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.)
);
vec3 NORMAL_TO_DIR2[6] = vec3[](
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.),
    vec3(0., 1., 0.),
    vec3(0., 1., 0.)
);

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform sampler2D texture_sampler;

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

in int[] normal;
in int[] texID;
in vec4[] occlusion;

out vec2 texCoords;
out float fragOcclusion;
out float fog;

void main()
{
    vec3 pos = gl_in[0].gl_Position.xyz + OFFSET[normal[0]];
    vec3 dir1 = NORMAL_TO_DIR1[normal[0]];
    vec3 dir2 = NORMAL_TO_DIR2[normal[0]];
    mat4 mvp = projectionMatrix * modelViewMatrix;

    int width = int(textureSize(texture_sampler, 0).x / 16);
    vec2 texBlockSize = 16. / textureSize(texture_sampler, 0);
    vec2 baseTexCoords = vec2(texID[0] % width, texID[0] / width) * texBlockSize;

    gl_Position = mvp * vec4(pos, 1.);
    texCoords = baseTexCoords;
    fragOcclusion = occlusion[0].x;
    fog = 1;
    EmitVertex();

    gl_Position = mvp * vec4(pos + dir1, 1.);
    texCoords = baseTexCoords + texBlockSize * vec2(1., 0.);
    fragOcclusion = occlusion[0].y;
    fog = 1;
    EmitVertex();

    gl_Position = mvp * vec4(pos + dir2, 1.);
    texCoords = baseTexCoords + texBlockSize * vec2(0., 1.);
    fragOcclusion = occlusion[0].w;
    fog = 1;
    EmitVertex();

    gl_Position = mvp * vec4(pos + dir1 + dir2, 1.);
    texCoords = baseTexCoords + texBlockSize;
    fragOcclusion = occlusion[0].z;
    fog = 1;
    EmitVertex();

    EndPrimitive();
}