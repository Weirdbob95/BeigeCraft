package game;

import behaviors.PositionBehavior;
import static engine.Activatable.using;
import engine.Behavior;
import engine.Core;
import java.util.*;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.RLEColumnStorage;
import util.Resources;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.chunks.RenderedChunk;

public class ModelBehavior extends Behavior {

    public static final ShaderProgram MODEL_SHADER = Resources.loadShaderProgram("model");

    private static final int[] DEFAULT_COLOR_PALETTE = {
        0x00000000, 0xffffffff, 0xffccffff, 0xff99ffff, 0xff66ffff, 0xff33ffff, 0xff00ffff, 0xffffccff, 0xffccccff, 0xff99ccff, 0xff66ccff, 0xff33ccff, 0xff00ccff, 0xffff99ff, 0xffcc99ff, 0xff9999ff,
        0xff6699ff, 0xff3399ff, 0xff0099ff, 0xffff66ff, 0xffcc66ff, 0xff9966ff, 0xff6666ff, 0xff3366ff, 0xff0066ff, 0xffff33ff, 0xffcc33ff, 0xff9933ff, 0xff6633ff, 0xff3333ff, 0xff0033ff, 0xffff00ff,
        0xffcc00ff, 0xff9900ff, 0xff6600ff, 0xff3300ff, 0xff0000ff, 0xffffffcc, 0xffccffcc, 0xff99ffcc, 0xff66ffcc, 0xff33ffcc, 0xff00ffcc, 0xffffcccc, 0xffcccccc, 0xff99cccc, 0xff66cccc, 0xff33cccc,
        0xff00cccc, 0xffff99cc, 0xffcc99cc, 0xff9999cc, 0xff6699cc, 0xff3399cc, 0xff0099cc, 0xffff66cc, 0xffcc66cc, 0xff9966cc, 0xff6666cc, 0xff3366cc, 0xff0066cc, 0xffff33cc, 0xffcc33cc, 0xff9933cc,
        0xff6633cc, 0xff3333cc, 0xff0033cc, 0xffff00cc, 0xffcc00cc, 0xff9900cc, 0xff6600cc, 0xff3300cc, 0xff0000cc, 0xffffff99, 0xffccff99, 0xff99ff99, 0xff66ff99, 0xff33ff99, 0xff00ff99, 0xffffcc99,
        0xffcccc99, 0xff99cc99, 0xff66cc99, 0xff33cc99, 0xff00cc99, 0xffff9999, 0xffcc9999, 0xff999999, 0xff669999, 0xff339999, 0xff009999, 0xffff6699, 0xffcc6699, 0xff996699, 0xff666699, 0xff336699,
        0xff006699, 0xffff3399, 0xffcc3399, 0xff993399, 0xff663399, 0xff333399, 0xff003399, 0xffff0099, 0xffcc0099, 0xff990099, 0xff660099, 0xff330099, 0xff000099, 0xffffff66, 0xffccff66, 0xff99ff66,
        0xff66ff66, 0xff33ff66, 0xff00ff66, 0xffffcc66, 0xffcccc66, 0xff99cc66, 0xff66cc66, 0xff33cc66, 0xff00cc66, 0xffff9966, 0xffcc9966, 0xff999966, 0xff669966, 0xff339966, 0xff009966, 0xffff6666,
        0xffcc6666, 0xff996666, 0xff666666, 0xff336666, 0xff006666, 0xffff3366, 0xffcc3366, 0xff993366, 0xff663366, 0xff333366, 0xff003366, 0xffff0066, 0xffcc0066, 0xff990066, 0xff660066, 0xff330066,
        0xff000066, 0xffffff33, 0xffccff33, 0xff99ff33, 0xff66ff33, 0xff33ff33, 0xff00ff33, 0xffffcc33, 0xffcccc33, 0xff99cc33, 0xff66cc33, 0xff33cc33, 0xff00cc33, 0xffff9933, 0xffcc9933, 0xff999933,
        0xff669933, 0xff339933, 0xff009933, 0xffff6633, 0xffcc6633, 0xff996633, 0xff666633, 0xff336633, 0xff006633, 0xffff3333, 0xffcc3333, 0xff993333, 0xff663333, 0xff333333, 0xff003333, 0xffff0033,
        0xffcc0033, 0xff990033, 0xff660033, 0xff330033, 0xff000033, 0xffffff00, 0xffccff00, 0xff99ff00, 0xff66ff00, 0xff33ff00, 0xff00ff00, 0xffffcc00, 0xffcccc00, 0xff99cc00, 0xff66cc00, 0xff33cc00,
        0xff00cc00, 0xffff9900, 0xffcc9900, 0xff999900, 0xff669900, 0xff339900, 0xff009900, 0xffff6600, 0xffcc6600, 0xff996600, 0xff666600, 0xff336600, 0xff006600, 0xffff3300, 0xffcc3300, 0xff993300,
        0xff663300, 0xff333300, 0xff003300, 0xffff0000, 0xffcc0000, 0xff990000, 0xff660000, 0xff330000, 0xff0000ee, 0xff0000dd, 0xff0000bb, 0xff0000aa, 0xff000088, 0xff000077, 0xff000055, 0xff000044,
        0xff000022, 0xff000011, 0xff00ee00, 0xff00dd00, 0xff00bb00, 0xff00aa00, 0xff008800, 0xff007700, 0xff005500, 0xff004400, 0xff002200, 0xff001100, 0xffee0000, 0xffdd0000, 0xffbb0000, 0xffaa0000,
        0xff880000, 0xff770000, 0xff550000, 0xff440000, 0xff220000, 0xff110000, 0xffeeeeee, 0xffdddddd, 0xffbbbbbb, 0xffaaaaaa, 0xff888888, 0xff777777, 0xff555555, 0xff444444, 0xff222222, 0xff111111
    };
    private static final List<Vec3d> DIRS = Arrays.asList(
            new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0),
            new Vec3d(0, -1, 0), new Vec3d(0, 1, 0),
            new Vec3d(0, 0, -1), new Vec3d(0, 0, 1));

    public final PositionBehavior position = require(PositionBehavior.class);

    public double rotation = 0;
    public double scale = 1 / 16.;

    private Vec3d size;
    private Map<Vec3d, Integer> numQuadsMap;
    private Map<Vec3d, VertexArrayObject> vaoMap;
    private boolean ready;

    public void loadModel(String fileName) {
        RLEColumnStorage<Integer> colors = null;
        int[] colorPalette = DEFAULT_COLOR_PALETTE;

        byte[] bytes = Resources.loadFileAsBytes("models/" + fileName);
        int pos = 8;
        while (pos < bytes.length) {
            String chunkName = new String(bytes, pos, 4);
            int chunkSize = readInt(bytes, pos + 4);
            if (chunkName.equals("SIZE")) {
                size = new Vec3d(readInt(bytes, pos + 12), readInt(bytes, pos + 16), readInt(bytes, pos + 20));
                colors = new RLEColumnStorage(Math.max((int) size.x, (int) size.y));
            }
            if (chunkName.equals("XYZI")) {
                int numBlocks = readInt(bytes, pos + 12);
                for (int i = 0; i < numBlocks; i++) {
                    int x = mod(bytes[pos + 16 + 4 * i], 256);
                    int y = mod(bytes[pos + 16 + 4 * i + 1], 256);
                    int z = mod(bytes[pos + 16 + 4 * i + 2], 256);
                    int colorID = mod(bytes[pos + 16 + 4 * i + 3], 256);
                    colors.set(x, y, z, colorID);
                }
            }
            if (chunkName.equals("RGBA")) {
                for (int i = 0; i < 255; i++) {
                    colorPalette[i + 1] = readInt(bytes, pos + 12 + 4 * i);
                }
            }
            pos += 12 + chunkSize;
        }

        numQuadsMap = new HashMap();
        vaoMap = new HashMap();
        ready = false;

        Map<Vec3d, List<RenderedChunk.Quad>> quads = new HashMap();
        for (Vec3d dir : DIRS) {
            quads.put(dir, new ArrayList());
        }

        for (int x = 0; x < size.x; x++) {
            for (int y = 0; y < size.y; y++) {
                for (int z = 0; z <= size.z; z++) {
                    Vec3d worldPos = new Vec3d(x, y, z);
                    Integer bt = getBlock(colors, worldPos);
                    if (bt != null) {
                        for (Vec3d dir : DIRS) {
                            if (getBlock(colors, worldPos.add(dir)) == null) {
                                RenderedChunk.Quad q = new RenderedChunk.Quad();
                                q.positionDir(x, y, z, dir);
                                //q.texCoordFromBlockType(bt, dir);
                                q.colorAmbientOcclusion(getOccludingBlocks(colors, worldPos, dir));
                                int colorHex = colorPalette[bt];
                                Vec3d color = new Vec3d(mod(colorHex, 256) / 255., mod(colorHex >> 8, 256) / 255., mod(colorHex >> 16, 256) / 255.);
                                for (int i = 0; i < 4; i++) {
                                    q.colors[i] = q.colors[i].mul(color);
                                }
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
            float[] vertices = new float[24 * quads.get(dir).size()];
            int[] indices = new int[6 * quads.get(dir).size()];
            for (int i = 0; i < quads.get(dir).size(); i++) {
                RenderedChunk.Quad q = quads.get(dir).get(i);
                for (int j = 0; j < 4; j++) {
                    System.arraycopy(new float[]{
                        (float) q.positions[j].x, (float) q.positions[j].y, (float) q.positions[j].z,
                        (float) q.colors[j].x, (float) q.colors[j].y, (float) q.colors[j].z // Vertex data for one vertex
                    }, 0, vertices, 24 * i + 6 * j, 6);
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
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, 24, 0);
                    glEnableVertexAttribArray(0);
                    glVertexAttribPointer(1, 3, GL_FLOAT, false, 24, 12);
                    glEnableVertexAttribArray(1);
                }));
                ready = true;
            }
        });
    }

    private Integer getBlock(RLEColumnStorage<Integer> colors, Vec3d worldPos) {
        if (worldPos.x < 0 || worldPos.x >= size.x || worldPos.y < 0 || worldPos.y >= size.y || worldPos.z < 0 || worldPos.z >= size.z) {
            return null;
        }
        return colors.get(floor(worldPos.x), floor(worldPos.y), floor(worldPos.z));
    }

    private boolean[][] getOccludingBlocks(RLEColumnStorage<Integer> colors, Vec3d worldPos, Vec3d dir) {
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
                r[i + 1][j + 1] = getBlock(colors, worldPos.add(dir).add(otherDir1.mul(i)).add(otherDir2.mul(j))) != null;
            }
        }
        return r;
    }

    private int readInt(byte[] bytes, int pos) {
        return mod(bytes[pos], 256)
                + (mod(bytes[pos + 1], 256) << 8)
                + (mod(bytes[pos + 2], 256) << 16)
                + (mod(bytes[pos + 3], 256) << 24);
    }

    @Override
    public void render() {
        if (!ready) {
            return;
        }
        MODEL_SHADER.setUniform("projectionMatrix", Camera.getProjectionMatrix());
        MODEL_SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(position.position.sub(size.mul(.5 * scale)), rotation, scale));
        MODEL_SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
        for (Vec3d dir : DIRS) {
            using(Arrays.asList(vaoMap.get(dir)), () -> {
                glDrawElements(GL_TRIANGLES, 6 * numQuadsMap.get(dir), GL_UNSIGNED_INT, 0);
            });
        }
    }
}
