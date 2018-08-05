package world.chunks;

import static engine.Activatable.using;
import engine.Core;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opengl.BufferObject;
import opengl.Camera;
import opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import world.BlockType;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;
import static world.World.TERRAIN_SHADER;

public class RenderedChunk extends AbstractChunk {

    private static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    private Map<Vec3d, Integer> numQuadsMap;
    private Map<Vec3d, VertexArrayObject> vaoMap;
    private boolean ready;

    public RenderedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    public void cleanup() {
        for (VertexArrayObject vao : vaoMap.values()) {
            vao.destroy();
        }
    }

    @Override
    protected void generate() {
        numQuadsMap = new HashMap();
        vaoMap = new HashMap();
        ready = false;

        ConstructedChunk[][] ccs = new ConstructedChunk[3][3];
        int maxZ = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                ccs[x + 1][y + 1] = world.constructedChunks.get(new ChunkPos(pos.x + x, pos.y + y));
                maxZ = Math.max(maxZ, ccs[x + 1][y + 1].blockStorage.maxZ());
                minZ = Math.min(minZ, ccs[x + 1][y + 1].blockStorage.minZ());
            }
        }

        Map<Vec3d, List<Quad>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Vec3d worldPos = new Vec3d(x, y, z);
                    BlockType bt = getBlock(ccs, worldPos);
                    if (bt != null) {
                        for (Vec3d dir : DIRS) {
                            if (getBlock(ccs, worldPos.add(dir)) == null) {
                                Quad q = new Quad();
                                q.positionDir(x, y, z, dir);
                                q.texCoordFromBlockType(bt, dir);
                                q.colorAmbientOcclusion(getOccludingBlocks(ccs, worldPos, dir));
                                quads.get(dir).add(q);
                            }
                        }
                    }
                }
            }
        }

        Map<Vec3d, float[]> verticesMap = new HashMap();
        Map<Vec3d, int[]> indicesMap = new HashMap();
        for (Vec3d dir : DIRS) {
            numQuadsMap.put(dir, quads.get(dir).size());
            float[] vertices = new float[40 * quads.get(dir).size()];
            int[] indices = new int[6 * quads.get(dir).size()];
            for (int i = 0; i < quads.get(dir).size(); i++) {
                Quad q = quads.get(dir).get(i);
                for (int j = 0; j < 4; j++) {
                    System.arraycopy(new float[]{
                        (float) q.positions[j].x, (float) q.positions[j].y, (float) q.positions[j].z,
                        (float) q.texCoords[j].x, (float) q.texCoords[j].y,
                        (float) q.texPositions[j].x, (float) q.texPositions[j].y,
                        (float) q.colors[j].x, (float) q.colors[j].y, (float) q.colors[j].z // Vertex data for one vertex
                    }, 0, vertices, 40 * i + 10 * j, 10);
                }
                System.arraycopy(new int[]{
                    4 * i, 4 * i + 1, 4 * i + 3,
                    4 * i + 1, 4 * i + 2, 4 * i + 3 // Index data for one quad
                }, 0, indices, 6 * i, 6);
            }
            verticesMap.put(dir, vertices);
            indicesMap.put(dir, indices);
        }
        Core.onMainThread(() -> {
            for (Vec3d dir : DIRS) {
                vaoMap.put(dir, VertexArrayObject.createVAO(() -> {
                    BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, verticesMap.get(dir));
                    BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indicesMap.get(dir));
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, 40, 0);
                    glEnableVertexAttribArray(0);
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, 40, 12);
                    glEnableVertexAttribArray(1);
                    glVertexAttribPointer(2, 2, GL_FLOAT, false, 40, 20);
                    glEnableVertexAttribArray(2);
                    glVertexAttribPointer(3, 3, GL_FLOAT, false, 40, 28);
                    glEnableVertexAttribArray(3);
                }));
                ready = true;
            }
        });
    }

    private BlockType getBlock(ConstructedChunk[][] ccs, Vec3d worldPos) {
        int ccx = 1 + floor(worldPos.x / CHUNK_SIZE), ccy = 1 + floor(worldPos.y / CHUNK_SIZE);
        int x = mod(floor(worldPos.x), CHUNK_SIZE), y = mod(floor(worldPos.y), CHUNK_SIZE), z = floor(worldPos.z);
        return ccs[ccx][ccy].blockStorage.get(x, y, z);
    }

    private boolean[][] getOccludingBlocks(ConstructedChunk[][] ccs, Vec3d worldPos, Vec3d dir) {
        Vec3d otherDir1, otherDir2;
        if (dir.x != 0) {
            otherDir1 = new Vec3d(0, 1, 0);
            otherDir2 = new Vec3d(0, 0, 1);
        } else if (dir.y != 0) {
            otherDir1 = new Vec3d(0, 0, 1);
            otherDir2 = new Vec3d(1, 0, 0);
        } else {
            otherDir1 = new Vec3d(1, 0, 0);
            otherDir2 = new Vec3d(0, 1, 0);
        }
        boolean[][] r = new boolean[3][3];
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                r[i + 1][j + 1] = getBlock(ccs, worldPos.add(dir).add(otherDir1.mul(i)).add(otherDir2.mul(j))) != null;
            }
        }
        return r;
    }

    private boolean intersectsFrustum() {
        return Camera.camera.getViewFrustum().testAab(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, world.constructedChunks.get(pos).blockStorage.minZ(),
                CHUNK_SIZE * (pos.x + 1), CHUNK_SIZE * (pos.y + 1), world.constructedChunks.get(pos).blockStorage.maxZ() + 1);
    }

//    @Override
    public void render() {
        if (!ready || !intersectsFrustum()) {
            return;
        }
        Vec3d worldPos = new Vec3d(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, 0);
        Vec3d min = worldPos.add(new Vec3d(0, 0, world.constructedChunks.get(pos).blockStorage.minZ()));
        Vec3d max = worldPos.add(new Vec3d(CHUNK_SIZE, CHUNK_SIZE, world.constructedChunks.get(pos).blockStorage.maxZ()));
        TERRAIN_SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(worldPos));
        for (Vec3d dir : DIRS) {
            if (Camera.camera.position.sub(min).dot(dir) > 0 || Camera.camera.position.sub(max).dot(dir) > 0) {
                using(Arrays.asList(vaoMap.get(dir)), () -> {
                    glDrawElements(GL_TRIANGLES, 6 * numQuadsMap.get(dir), GL_UNSIGNED_INT, 0);
                });
            }
        }
    }

    private static class Quad {

        public Vec3d[] positions;
        public Vec2d[] texCoords;
        public Vec2d[] texPositions;
        public Vec3d[] colors;

        private void colorWhite() {
            colors = new Vec3d[]{new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1)};
        }

        private void colorAmbientOcclusion(boolean[][] occluders) {
            double ao00 = getAmbientOcclusion(occluders, 0, 0);
            double ao10 = getAmbientOcclusion(occluders, 1, 0);
            double ao11 = getAmbientOcclusion(occluders, 1, 1);
            double ao01 = getAmbientOcclusion(occluders, 0, 1);
            colors = new Vec3d[]{new Vec3d(ao00, ao00, ao00), new Vec3d(ao10, ao10, ao10), new Vec3d(ao11, ao11, ao11), new Vec3d(ao01, ao01, ao01)};
        }

        private static double getAmbientOcclusion(boolean[][] a, int i, int j) {
//        if (a[0][0] || a[1][0] || a[0][1] || a[1][1]) {
//            return .75f;
//        }
            if ((a[i][j] && a[i + 1][j + 1]) || (a[i][j + 1] && a[i + 1][j])) {
                return .55f;
            }
            int numSolid = 0;
            for (int i2 = i; i2 < i + 2; i2++) {
                for (int j2 = j; j2 < j + 2; j2++) {
                    if (a[i2][j2]) {
                        numSolid++;
                    }
                }
            }
            switch (numSolid) {
                case 2:
                    return .7f;
                case 1:
                    return .85f;
                default:
                    return 1;
            }
        }

        private void positionDir(int x, int y, int z, Vec3d dir) {
            int dirID = DIRS.indexOf(dir);
            switch (dirID) {
                case 0:
                    positionNormalX(x, y, z);
                    break;
                case 1:
                    positionNormalX(x + 1, y, z);
                    break;
                case 2:
                    positionNormalY(x, y, z);
                    break;
                case 3:
                    positionNormalY(x, y + 1, z);
                    break;
                case 4:
                    positionNormalZ(x, y, z);
                    break;
                case 5:
                    positionNormalZ(x, y, z + 1);
                    break;
                case 6:
                    throw new RuntimeException("Unknown direction " + dir);
            }
        }

        private void positionNormalX(int x, int y, int z) {
            positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x, y + 1, z), new Vec3d(x, y + 1, z + 1), new Vec3d(x, y, z + 1)};
        }

        private void positionNormalY(int x, int y, int z) {
            positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x, y, z + 1), new Vec3d(x + 1, y, z + 1), new Vec3d(x + 1, y, z)};
        }

        private void positionNormalZ(int x, int y, int z) {
            positions = new Vec3d[]{new Vec3d(x, y, z), new Vec3d(x + 1, y, z), new Vec3d(x + 1, y + 1, z), new Vec3d(x, y + 1, z)};
        }

        private void texCoordFromBlockType(BlockType bt, Vec3d dir) {
            if (dir.x == 0) {
                texCoords = new Vec2d[]{new Vec2d(1, 1), new Vec2d(1, 0), new Vec2d(0, 0), new Vec2d(0, 1)};
            } else {
                texCoords = new Vec2d[]{new Vec2d(0, 1), new Vec2d(1, 1), new Vec2d(1, 0), new Vec2d(0, 0)};
            }
            Vec2d pos = BlockType.spritesheetPos(bt, dir);
            texPositions = new Vec2d[]{pos, pos, pos, pos};
        }
    }
}
