package graphics;

import definitions.BlockType;
import graphics.Quad.TexturedQuad;
import java.util.Arrays;
import java.util.List;
import opengl.ShaderProgram;
import util.math.MathUtils;
import static util.math.MathUtils.floor;
import static util.math.MathUtils.lerp;
import static util.math.MathUtils.mod;
import util.math.Vec3d;
import util.math.Vec4d;
import util.rlestorage.RLEColumn;
import world.World;
import static world.World.CHUNK_SIZE;
import static world.World.TERRAIN_SHADER;
import world.regions.RegionPos;
import world.regions.chunks.ConstructedChunk;

public class ChunkRenderer extends VoxelRenderer<BlockType> {

    private ConstructedChunk[][] ccs;
    private double maxZ, minZ;

    public ChunkRenderer(World world, RegionPos pos) {
        ccs = new ConstructedChunk[3][3];
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                ccs[x + 1][y + 1] = world.constructedChunks.get(new RegionPos(pos.x + x, pos.y + y));
                maxZ = Math.max(maxZ, ccs[x + 1][y + 1].blockStorage.maxZ());
                minZ = Math.min(minZ, ccs[x + 1][y + 1].blockStorage.minZ());
            }
        }
        generate();
        //ccs = null;
    }

    @Override
    protected RLEColumn<BlockType> columnAt(int x, int y) {
        int ccx = 1 + floor((double) x / CHUNK_SIZE), ccy = 1 + floor((double) y / CHUNK_SIZE);
        return ccs[ccx][ccy].blockStorage.columnAt(mod(x, CHUNK_SIZE), mod(y, CHUNK_SIZE));
    }

    private double computeShadow(int x, int y, int z) {
        double light = 0;
        int shadowSize = 3;
        for (int i = -shadowSize; i < shadowSize; i++) {
            for (int j = -shadowSize; j < shadowSize; j++) {
                if (columnAt(x + i, y + j).maxPos() <= z) {
                    light = Math.max(light, lerp(1, 0, (Math.sqrt((i + .5) * (i + .5) + (j + .5) * (j + .5)) - .5) / shadowSize));
                }
            }
        }
        return lerp(.8, 1, light);
    }

    @Override
    protected Quad createQuad(int x, int y, int z, BlockType voxel, Vec3d dir) {
        TexturedQuad q = new TexturedQuad();
        q.positionDir(x, y, z, dir);
        q.texCoordFromBlockType(voxel, dir);
        q.colorAmbientOcclusion(getOccludingVoxels(x, y, z, dir));

        //q.colorShadow(computeShadow(x + (int) dir.x, y + (int) dir.y, z));
        if (dir.x == -1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x, y, z));
            q.occlusion[1] *= MathUtils.max(computeShadow(x, y, z));
            q.occlusion[2] *= MathUtils.max(computeShadow(x, y + 1, z));
            q.occlusion[3] *= MathUtils.max(computeShadow(x, y + 1, z));
        } else if (dir.x == 1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x + 1, y, z));
            q.occlusion[1] *= MathUtils.max(computeShadow(x + 1, y, z));
            q.occlusion[2] *= MathUtils.max(computeShadow(x + 1, y + 1, z));
            q.occlusion[3] *= MathUtils.max(computeShadow(x + 1, y + 1, z));
        } else if (dir.y == -1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x, y, z));
            q.occlusion[1] *= MathUtils.max(computeShadow(x + 1, y, z));
            q.occlusion[2] *= MathUtils.max(computeShadow(x + 1, y, z));
            q.occlusion[3] *= MathUtils.max(computeShadow(x, y, z));
        } else if (dir.y == 1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x, y + 1, z));
            q.occlusion[1] *= MathUtils.max(computeShadow(x + 1, y + 1, z));
            q.occlusion[2] *= MathUtils.max(computeShadow(x + 1, y + 1, z));
            q.occlusion[3] *= MathUtils.max(computeShadow(x, y + 1, z));
        } else if (dir.z == -1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x, y, z - 1));
            q.occlusion[1] *= MathUtils.max(computeShadow(x + 1, y, z - 1));
            q.occlusion[2] *= MathUtils.max(computeShadow(x + 1, y + 1, z - 1));
            q.occlusion[3] *= MathUtils.max(computeShadow(x, y + 1, z - 1));
        } else if (dir.z == 1) {
            q.occlusion[0] *= MathUtils.max(computeShadow(x, y, z));
            q.occlusion[1] *= MathUtils.max(computeShadow(x + 1, y, z));
            q.occlusion[2] *= MathUtils.max(computeShadow(x + 1, y + 1, z));
            q.occlusion[3] *= MathUtils.max(computeShadow(x, y + 1, z));
        }
        return q;
    }

    @Override
    public Vec3d max() {
//        int maxZ = Integer.MIN_VALUE;
//        for (int x = 0; x < 3; x++) {
//            for (int y = 0; y < 3; y++) {
//                maxZ = Math.max(maxZ, ccs[x][y].blockStorage.maxZ());
//            }
//        }
        return new Vec3d(CHUNK_SIZE, CHUNK_SIZE, maxZ);
    }

    @Override
    public Vec3d min() {
//        int minZ = Integer.MAX_VALUE;
//        for (int x = 0; x < 3; x++) {
//            for (int y = 0; y < 3; y++) {
//                minZ = Math.min(minZ, ccs[x][y].blockStorage.minZ());
//            }
//        }
        return new Vec3d(0, 0, minZ);
    }

    @Override
    protected void setShaderUniforms(Vec4d color) {
    }

    @Override
    protected ShaderProgram shader() {
        return TERRAIN_SHADER;
    }

    @Override
    protected List<Integer> vertexAttribSizes() {
        return Arrays.asList(3, 1, 1, 4);
    }

    @Override
    protected BlockType voxelAt(int x, int y, int z) {
        int ccx = 1 + floor((double) x / CHUNK_SIZE), ccy = 1 + floor((double) y / CHUNK_SIZE);
        return ccs[ccx][ccy].blockStorage.get(mod(x, CHUNK_SIZE), mod(y, CHUNK_SIZE), z);
    }
}
