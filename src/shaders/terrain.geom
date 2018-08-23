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
    vec3(1., 0., 0.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.),
    vec3(1., 0., 0.)
);
vec3 NORMAL_TO_DIR2[6] = vec3[](
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 0., 1.),
    vec3(0., 1., 0.),
    vec3(0., 1., 0.)
);

float maxFogDist = 2000;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform sampler2D texture_sampler;

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

in int[] normal;
in int[] texID;
in vec4[] occlusion;

out vec2 texCoordsConst;
out vec2 texCoordsInterp;
out float fragOcclusion;
out float fog;

vec4 lodTransform(vec3 pos)
{
    //return modelViewMatrix * vec4(pos, 1.);
    vec4 viewPos = modelViewMatrix * vec4(pos, 1.);
    float lodF = max(0, -8 + log2(length(viewPos)));
    int lod = int(lodF);
    vec3 pos1 = pow(2,lod) * ceil(pos / pow(2,lod));
    vec3 pos2 = pow(2,lod+1) * ceil(pos / pow(2,lod+1));
    pos = mix(pos1, pos2, clamp((lodF - lod - .5) * 2 + .5, 0, 1));
    return modelViewMatrix * vec4(pos, 1.);
}

void main()
{
    vec3 pos = gl_in[0].gl_Position.xyz + OFFSET[normal[0]];
    vec3 dir1 = NORMAL_TO_DIR1[normal[0]];
    vec3 dir2 = NORMAL_TO_DIR2[normal[0]];

    vec2 texBlockSize = 16. / textureSize(texture_sampler, 0);
    vec2 baseTexCoords = vec2(texID[0] % 256, texID[0] / 256) * texBlockSize;

    vec4 viewPos = lodTransform(pos);
    //vec4 viewPos = modelViewMatrix * vec4(pos, 1.);
    gl_Position = projectionMatrix * viewPos;
    texCoordsConst = baseTexCoords;
    texCoordsInterp = vec2(0., 0.);
    fragOcclusion = occlusion[0].x;
    fog = pow(.01, pow(length(viewPos) / maxFogDist, 2));
    EmitVertex();

    viewPos = lodTransform(pos + dir1);
    //viewPos = modelViewMatrix * vec4(pos + dir1, 1.);
    gl_Position = projectionMatrix * viewPos;
    texCoordsConst = baseTexCoords;
    texCoordsInterp = texBlockSize * vec2(1., 0.);
    fragOcclusion = occlusion[0].y;
    fog = pow(.01, pow(length(viewPos) / maxFogDist, 2));
    EmitVertex();

    viewPos = lodTransform(pos + dir2);
    //viewPos = modelViewMatrix * vec4(pos + dir2, 1.);
    gl_Position = projectionMatrix * viewPos;
    texCoordsConst = baseTexCoords;
    texCoordsInterp = texBlockSize * vec2(0., 1.);
    fragOcclusion = occlusion[0].w;
    fog = pow(.01, pow(length(viewPos) / maxFogDist, 2));
    EmitVertex();

    viewPos = lodTransform(pos + dir1 + dir2);
    //viewPos = modelViewMatrix * vec4(pos + dir1 + dir2, 1.);
    gl_Position = projectionMatrix * viewPos;
    texCoordsConst = baseTexCoords;
    texCoordsInterp = texBlockSize;
    fragOcclusion = occlusion[0].z;
    fog = pow(.01, pow(length(viewPos) / maxFogDist, 2));
    EmitVertex();

    EndPrimitive();
}