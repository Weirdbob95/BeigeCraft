package graphics;

import static engine.Activatable.using;
import engine.Core;
import static game.Settings.ENABLE_LOD;
import static game.Settings.MULTITHREADED_OPENGL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.VertexArrayObject;
import org.joml.Matrix4d;
import org.joml.Vector4d;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static util.MathUtils.clamp;
import static util.MathUtils.floor;
import util.vectors.Vec3d;
import util.vectors.Vec4d;

public abstract class VoxelRenderer<T> {

    public static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    private static final int MAX_LOD = 3;

    //private final Map<Vec3d, Integer> numQuadsMap = new HashMap();
    private final Map<Vec3d, Map<Integer, Integer>> numQuadsMap = new HashMap();
    private final Map<Vec3d, VertexArrayObject> vaoMap = new HashMap();
    private final Map<Vec3d, BufferObject> vboMap = new HashMap();

    public void cleanup() {
        for (VertexArrayObject vao : vaoMap.values()) {
            vao.destroy();
        }
    }

    protected abstract Iterator<Entry<Integer, T>> columnAt(int x, int y);

    protected abstract Quad createQuad(int x, int y, int z, T voxel, Vec3d dir);

    public void generate() {
        Map<Vec3d, List<Quad>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }

        int maxX = (int) max().x, maxY = (int) max().y;
        int minX = (int) min().x, minY = (int) min().y;

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (Vec3d dir : DIRS.subList(0, 4)) {
                    generateExposedSideFaces(x, y, dir, quads.get(dir));
                }
                generateExposedFaces(x, y, quads.get(new Vec3d(0, 0, -1)), quads.get(new Vec3d(0, 0, 1)));
            }
        }

        int vertexSize = vertexAttribSizes().stream().mapToInt(i -> i).sum();

        for (Vec3d dir : DIRS) {
            //numQuadsMap.put(dir, quads.get(dir).size());
            List<Quad>[] lodLists = new List[MAX_LOD + 1];
            for (int lod = 0; lod <= MAX_LOD; lod++) {
                lodLists[lod] = new ArrayList();
            }
            for (Quad q : quads.get(dir)) {
                lodLists[clamp(q.getLOD(), 0, MAX_LOD)].add(q);
            }
            numQuadsMap.put(dir, new HashMap());
            numQuadsMap.get(dir).put(MAX_LOD, lodLists[MAX_LOD].size());
            for (int lod = MAX_LOD - 1; lod >= 0; lod--) {
                numQuadsMap.get(dir).put(lod, lodLists[lod].size() + numQuadsMap.get(dir).get(lod + 1));
            }

            float[] vertices = new float[vertexSize * quads.get(dir).size()];
            int i = 0;
            for (int lod = MAX_LOD; lod >= 0; lod--) {
                for (Quad q : lodLists[lod]) {
                    System.arraycopy(q.toData(), 0, vertices, vertexSize * i, vertexSize);
                    i++;
                }
            }
            if (MULTITHREADED_OPENGL) {
                if (!vboMap.containsKey(dir)) {
                    vboMap.put(dir, new BufferObject(GL_ARRAY_BUFFER));
                }
                vboMap.get(dir).activate();
                vboMap.get(dir).putData(vertices);
            } else {
                // Workaround for threading issues
                Core.onMainThread(() -> {
                    if (!vboMap.containsKey(dir)) {
                        vboMap.put(dir, new BufferObject(GL_ARRAY_BUFFER));
                    }
                    vboMap.get(dir).activate();
                    vboMap.get(dir).putData(vertices);
                });
            }
        }
        if (vaoMap.isEmpty()) {
            Core.onMainThread(() -> {
                for (Vec3d dir : DIRS) {
                    vaoMap.put(dir, VertexArrayObject.createVAO(() -> {
                        vboMap.get(dir).activate();
                        int total = 0;
                        for (int i = 0; i < vertexAttribSizes().size(); i++) {
                            glVertexAttribPointer(i, vertexAttribSizes().get(i), GL_FLOAT, false, vertexSize * 4, total * 4);
                            glEnableVertexAttribArray(i);
                            total += vertexAttribSizes().get(i);
                        }
                    }));
                }
            });
        }
    }

    private void generateExposedFaces(int x, int y, List<Quad> quads1, List<Quad> quads2) {
        Iterator<Entry<Integer, T>> i = columnAt(x, y);
        if (!i.hasNext()) {
            return;
        }
        Entry<Integer, T> e = i.next();
        while (i.hasNext()) {
            Entry<Integer, T> ne = i.next();
            if (ne.getValue() == null && e.getValue() != null) {
                quads2.add(createQuad(x, y, e.getKey(), e.getValue(), new Vec3d(0, 0, 1)));
            } else if (ne.getValue() != null && e.getValue() == null) {
                quads1.add(createQuad(x, y, e.getKey() + 1, ne.getValue(), new Vec3d(0, 0, -1)));
            }
            e = ne;
        }
        quads2.add(createQuad(x, y, e.getKey(), e.getValue(), new Vec3d(0, 0, 1)));
    }

    private void generateExposedSideFaces(int x, int y, Vec3d dir, List<Quad> quads) {
        Iterator<Entry<Integer, T>> i1 = columnAt(x, y);
        Iterator<Entry<Integer, T>> i2 = columnAt(x + (int) dir.x, y + (int) dir.y);
        Entry<Integer, T> e1 = i1.hasNext() ? i1.next() : null;
        Entry<Integer, T> e2 = i2.hasNext() ? i2.next() : null;
        if (e1 == null) {
            return;
        }
        int pos = Math.min(e1.getKey(), e2 == null ? Integer.MAX_VALUE : e2.getKey());
        while (true) {
            if (e2 != null && e2.getKey() < e1.getKey()) {
                Entry<Integer, T> next_e2 = i2.hasNext() ? i2.next() : null;
                int next_pos = Math.min(e1.getKey(), next_e2 == null ? Integer.MAX_VALUE : next_e2.getKey());
                if (e1.getValue() != null && (next_e2 == null || next_e2.getValue() == null)) {
                    for (int z = pos; z < next_pos; z++) {
                        quads.add(createQuad(x, y, z + 1, e1.getValue(), dir));
                    }
                }
                e2 = next_e2;
                pos = next_pos;
                continue;
            } else if (i1.hasNext()) {
                Entry<Integer, T> next_e1 = i1.next();
                int next_pos = Math.min(next_e1.getKey(), e2 == null ? Integer.MAX_VALUE : e2.getKey());
                if (next_e1.getValue() != null && (e2 == null || e2.getValue() == null)) {
                    for (int z = pos; z < next_pos; z++) {
                        quads.add(createQuad(x, y, z + 1, next_e1.getValue(), dir));
                    }
                }
                e1 = next_e1;
                pos = next_pos;
                continue;
            }
            break;
        }
    }

    protected boolean[][] getOccludingVoxels(int x, int y, int z, Vec3d dir) {
        boolean[][] r = new boolean[3][3];
        if (dir.x != 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    r[i + 1][j + 1] = voxelAt(x + (int) dir.x, y + i, z + j) != null;
                }
            }
        } else if (dir.y != 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    r[i + 1][j + 1] = voxelAt(x + i, y + (int) dir.y, z + j) != null;
                }
            }
        } else {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    r[i + 1][j + 1] = voxelAt(x + i, y + j, z + (int) dir.z) != null;
                }
            }
        }
        return r;
    }

    protected abstract Vec3d max();

    protected abstract Vec3d min();

    public void render(Vec3d position, double rotation, double scale, Vec3d origin, Vec4d color) {
        if (vaoMap.isEmpty()) {
            return;
        }
        setShaderUniforms(color);
        Matrix4d worldMat = Camera.camera3d.getWorldMatrix(position, rotation, scale).translate(origin.toJOML().mul(-1));
        shader().setUniform("modelViewMatrix", worldMat);
        for (Vec3d dir : DIRS) {
            Vector4d newDir = new Vector4d(dir.x, dir.y, dir.z, 0).mul(worldMat);
            boolean check = new Vector4d(min().x, min().y, min().z, 1).mul(worldMat).dot(newDir) < 0
                    || new Vector4d(max().x, max().y, max().z, 1).mul(worldMat).dot(newDir) < 0;
            if (check) {
                double minDist = clamp(Camera.camera3d.position, min().add(position), max().add(position)).sub(Camera.camera3d.position).length();
                int lod = ENABLE_LOD ? clamp(floor(-8 + Math.log(minDist) / Math.log(2)), 0, MAX_LOD) : 0;
                using(Arrays.asList(vaoMap.get(dir)), () -> {
                    glDrawArrays(GL_POINTS, 0, numQuadsMap.get(dir).get(lod));
                });
            }
        }
    }

    protected abstract void setShaderUniforms(Vec4d color);

    protected abstract ShaderProgram shader();

    protected abstract List<Integer> vertexAttribSizes();

    protected abstract T voxelAt(int x, int y, int z);
}
