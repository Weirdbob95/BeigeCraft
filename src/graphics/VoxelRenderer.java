package graphics;

import static engine.Activatable.using;
import engine.Core;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.VertexArrayObject;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import util.vectors.Vec3d;

public abstract class VoxelRenderer<T> {

    static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    private Map<Vec3d, Integer> numQuadsMap;
    private Map<Vec3d, VertexArrayObject> vaoMap;
    private boolean ready;

    public void cleanup() {
        for (VertexArrayObject vao : vaoMap.values()) {
            vao.destroy();
        }
    }

    protected abstract Quad createQuad(int x, int y, int z, T voxel, Vec3d dir);

    public void generate() {
        numQuadsMap = new HashMap();
        vaoMap = new HashMap();
        ready = false;

        Map<Vec3d, List<Quad>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }

        int maxX = (int) max().x, maxY = (int) max().y, maxZ = (int) max().z;
        int minX = (int) min().x, minY = (int) min().y, minZ = (int) min().z;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    T bt = voxelAt(x, y, z);
                    if (bt != null) {
                        for (Vec3d dir : DIRS) {
                            if (voxelAt(x + (int) dir.x, y + (int) dir.y, z + (int) dir.z) == null) {
                                quads.get(dir).add(createQuad(x, y, z, bt, dir));
                            }
                        }
                    }
                }
            }
        }

        int vertexSize = vertexAttribs().stream().mapToInt(i -> i).sum();

        Map<Vec3d, float[]> verticesMap = new HashMap();
        Map<Vec3d, int[]> indicesMap = new HashMap();
        for (Vec3d dir : DIRS) {
            numQuadsMap.put(dir, quads.get(dir).size());
            float[] vertices = new float[4 * vertexSize * quads.get(dir).size()];
            int[] indices = new int[6 * quads.get(dir).size()];
            for (int i = 0; i < quads.get(dir).size(); i++) {
                Quad q = quads.get(dir).get(i);
                for (int j = 0; j < 4; j++) {
                    System.arraycopy(q.toData(j), 0, vertices, vertexSize * (4 * i + j), vertexSize);
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
                    int total = 0;
                    for (int i = 0; i < vertexAttribs().size(); i++) {
                        glVertexAttribPointer(i, vertexAttribs().get(i), GL_FLOAT, false, vertexSize * 4, total * 4);
                        glEnableVertexAttribArray(i);
                        total += vertexAttribs().get(i);
                    }
                }));
                ready = true;
            }
        });
    }

    protected boolean[][] getOccludingVoxels(int x, int y, int z, Vec3d dir) {
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
                r[i + 1][j + 1] = voxelAt(new Vec3d(x, y, z).add(dir).add(otherDir1.mul(i)).add(otherDir2.mul(j))) != null;
            }
        }
        return r;
    }

    protected abstract Vec3d max();

    protected abstract Vec3d min();

    public void render(Vec3d position, double rotation, double scale, Vec3d origin) {
        if (!ready) {
            return;
        }
        setShaderUniforms();
        Matrix4d worldMat = Camera.camera.getWorldMatrix(position, rotation, scale).translate(origin.toJOML().mul(-1));
        shader().setUniform("modelViewMatrix", worldMat);
        for (Vec3d dir : DIRS) {
            Vector4d newDir = new Vector4d(dir.x, dir.y, dir.z, 0).mul(worldMat);
            boolean check = new Vector4d(min().x, min().y, min().z, 1).mul(worldMat).dot(newDir) < 0
                    || new Vector4d(max().x, max().y, max().z, 1).mul(worldMat).dot(newDir) < 0;
            if (check) {
                using(Arrays.asList(vaoMap.get(dir)), () -> {
                    glDrawElements(GL_TRIANGLES, 6 * numQuadsMap.get(dir), GL_UNSIGNED_INT, 0);
                });
            }
        }
    }

    protected abstract void setShaderUniforms();

    //protected abstract void setVertexAttribs();
    protected abstract ShaderProgram shader();

    protected abstract List<Integer> vertexAttribs();

    //protected abstract int vertexSize();
    protected abstract T voxelAt(int x, int y, int z);

    private T voxelAt(Vec3d pos) {
        return voxelAt((int) pos.x, (int) pos.y, (int) pos.z);
    }
}
