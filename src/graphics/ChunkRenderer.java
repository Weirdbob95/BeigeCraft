package graphics;

import graphics.Quad.TexturedQuad;
import java.util.Arrays;
import java.util.List;
import opengl.ShaderProgram;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.vectors.Vec3d;
import world.BlockType;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import static world.World.TERRAIN_SHADER;
import world.chunks.ConstructedChunk;

public class ChunkRenderer extends VoxelRenderer<BlockType> {

    private ConstructedChunk[][] ccs;
    private int maxZ = Integer.MIN_VALUE;
    private int minZ = Integer.MAX_VALUE;

    public ChunkRenderer(World world, ChunkPos pos) {
        ccs = new ConstructedChunk[3][3];
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                ccs[x + 1][y + 1] = world.constructedChunks.get(new ChunkPos(pos.x + x, pos.y + y));
                maxZ = Math.max(maxZ, ccs[x + 1][y + 1].blockStorage.maxZ());
                minZ = Math.min(minZ, ccs[x + 1][y + 1].blockStorage.minZ());
            }
        }

        generate();

        ccs = null;
    }

    @Override
    protected Quad createQuad(int x, int y, int z, BlockType voxel, Vec3d dir) {
        TexturedQuad q = new TexturedQuad();
        q.positionDir(x, y, z, dir);
        q.texCoordFromBlockType(voxel, dir);
        q.colorAmbientOcclusion(getOccludingVoxels(x, y, z, dir));
        return q;
    }

    @Override
    protected Vec3d max() {
        return new Vec3d(CHUNK_SIZE, CHUNK_SIZE, maxZ);
    }

    @Override
    protected Vec3d min() {
        return new Vec3d(0, 0, minZ);
    }

    @Override
    protected void setShaderUniforms() {
    }

    @Override
    protected ShaderProgram shader() {
        return TERRAIN_SHADER;
    }

    @Override
    protected List<Integer> vertexAttribs() {
        return Arrays.asList(3, 2, 2, 3);
    }

    @Override
    protected BlockType voxelAt(int x, int y, int z) {
        int ccx = 1 + floor((double) x / CHUNK_SIZE), ccy = 1 + floor((double) y / CHUNK_SIZE);
        return ccs[ccx][ccy].blockStorage.get(mod(x, CHUNK_SIZE), mod(y, CHUNK_SIZE), z);
    }
}
