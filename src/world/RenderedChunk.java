package world;

import static engine.Activatable.using;
import engine.Behavior;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.Texture;
import opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import util.Resources;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import static world.World.CHUNK_SIZE;

public class RenderedChunk extends Behavior {

    private static final ShaderProgram SHADER = Resources.loadShaderProgram("sprite");
    private static final Texture TEXTURE = new Texture("sprites/blockSpritesheet.png");

    private static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    private World world;
    private ChunkPos pos;
    private int numQuads;
    private Map<Vec3d, VertexArrayObject> vaoMap;

    public void generateAtPos(World world, ChunkPos pos) {
        this.world = world;
        this.pos = pos;

        int maxZ = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                ConstructedChunk cc = world.getConstructedChunk(new ChunkPos(pos.x + x, pos.y + y));
                maxZ = Math.max(maxZ, cc.blockStorage.maxZ());
                minZ = Math.min(minZ, cc.blockStorage.minZ());
            }
        }

        Map<Vec3d, List<Quad>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Vec3d worldPos = new Vec3d(x + CHUNK_SIZE * pos.x, y + CHUNK_SIZE * pos.y, z);
                    BlockType bt = world.getBlock(worldPos);
                    if (bt != null) {
                        for (Vec3d dir : DIRS) {
                            if (world.getBlock(worldPos.add(dir)) == null) {
                                Quad q = new Quad();
                                q.positionDir(x, y, z, dir);
                                q.texCoordFromBlockType(bt);
                                q.colorWhite();
                                quads.get(dir).add(q);
                            }
                        }
                    }
                }
            }
        }
        for (Vec3d dir : DIRS) {
            setData(dir, quads.get(dir));
        }
    }

    private void setData(Vec3d dir, List<Quad> quads) {
        numQuads = quads.size();
        float[] vertices = new float[8 * 4 * numQuads];
        int[] indices = new int[6 * numQuads];
        for (int i = 0; i < numQuads; i++) {
            Quad q = quads.get(i);
            for (int j = 0; j < 4; j++) {
                System.arraycopy(new float[]{
                    (float) q.positions[j].x, (float) q.positions[j].y, (float) q.positions[j].z,
                    (float) q.texCoords[j].x, (float) q.texCoords[j].y,
                    (float) q.colors[j].x, (float) q.colors[j].y, (float) q.colors[j].z // Vertex data for one vertex
                }, 0, vertices, 32 * i + 8 * j, 8);
            }
            System.arraycopy(new int[]{
                4 * i, 4 * i + 1, 4 * i + 3,
                4 * i + 1, 4 * i + 2, 4 * i + 3 // Index data for one quad
            }, 0, indices, 6 * i, 6);
        }
        vaoMap.put(dir, VertexArrayObject.createVAO(() -> {
            BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, vertices);
            BufferObject ebo = new BufferObject(GL_ELEMENT_ARRAY_BUFFER, indices);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 32, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 32, 12);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 32, 20);
            glEnableVertexAttribArray(2);
        }));
    }

    @Override
    public void render() {
        if (numQuads == 0) {
            return;
        }
        Vec3d worldPos = new Vec3d(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, 0);
        Vec3d min = worldPos.add(new Vec3d(0, 0, world.getConstructedChunk(pos).blockStorage.minZ()));
        Vec3d max = worldPos.add(new Vec3d(CHUNK_SIZE, CHUNK_SIZE, world.getConstructedChunk(pos).blockStorage.maxZ()));
        SHADER.setUniform("projectionMatrix", Camera.getProjectionMatrix());
        SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(worldPos));
        SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
        using(Arrays.asList(TEXTURE, SHADER), () -> {
            for (Vec3d dir : DIRS) {
                if (Camera.camera.position.sub(min).dot(dir) > 0 || Camera.camera.position.sub(max).dot(dir) > 0) {
                    vaoMap.get(dir).activate();
                    glDrawElements(GL_TRIANGLES, 6 * numQuads, GL_UNSIGNED_INT, 0);
                }
            }
        });
    }

    private static class Quad {

        public Vec3d[] positions;
        public Vec2d[] texCoords;
        public Vec3d[] colors;

        private void colorWhite() {
            colors = new Vec3d[]{new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 1)};
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

        private void texCoordFromBlockType(BlockType bt) {
            Vec2d pos, size;
            switch (bt) {
                case GRASS:
                    pos = new Vec2d(0, 0);
                    size = new Vec2d(16, 16);
                    break;
                case DIRT:
                    pos = new Vec2d(16, 0);
                    size = new Vec2d(16, 16);
                    break;
                case STONE:
                    pos = new Vec2d(32, 0);
                    size = new Vec2d(16, 16);
                    break;
                default:
                    throw new RuntimeException("Unknown BlockType");
            }
            Vec2d textureSize = new Vec2d(48, 16);
            pos = pos.div(textureSize);
            size = size.div(textureSize);
            texCoords = new Vec2d[]{pos, pos.add(size.setY(0)), pos.add(size), pos.add(size.setX(0))};
        }
    }
}
