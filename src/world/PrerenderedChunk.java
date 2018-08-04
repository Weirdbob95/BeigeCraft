package world;

import static engine.Activatable.using;
import engine.Behavior;
import static engine.Core.onMainThread;
import java.util.Arrays;
import opengl.BufferObject;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.VertexArrayObject;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static util.MathUtils.vecsToArray;
import util.Resources;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import static world.World.CHUNK_SIZE;

public class PrerenderedChunk extends Behavior {

    private static final ShaderProgram SHADER = Resources.loadShaderProgram("color");

    private World world;
    public ChunkPos pos;
    private VertexArrayObject vao;

    @Override
    public void destroyInner() {
        if (vao != null) {
            vao.destroy();
        }
    }

    public void generateAtPos(World world, ChunkPos pos) {
        this.world = world;
        this.pos = pos;

        float[] vertices = vecsToArray(Arrays.asList(
                new Vec3d(CHUNK_SIZE / 2., CHUNK_SIZE / 2., world.plannedChunks.get(pos).elevation),
                new Vec3d(0, 0, getCornerElevation(pos)),
                new Vec3d(CHUNK_SIZE, 0, getCornerElevation(new ChunkPos(pos.x + 1, pos.y))),
                new Vec3d(CHUNK_SIZE, CHUNK_SIZE, getCornerElevation(new ChunkPos(pos.x + 1, pos.y + 1))),
                new Vec3d(0, CHUNK_SIZE, getCornerElevation(new ChunkPos(pos.x, pos.y + 1))),
                new Vec3d(0, 0, getCornerElevation(pos))
        ));
        onMainThread(() -> {
            vao = VertexArrayObject.createVAO(() -> {
                BufferObject vbo = new BufferObject(GL_ARRAY_BUFFER, vertices);
                glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);
                glEnableVertexAttribArray(0);
            });
            create();
        });
    }

    private double getCornerElevation(ChunkPos pos) {
        return .25 * (world.plannedChunks.get(pos).elevation
                + world.plannedChunks.get(new ChunkPos(pos.x - 1, pos.y)).elevation
                + world.plannedChunks.get(new ChunkPos(pos.x, pos.y - 1)).elevation
                + world.plannedChunks.get(new ChunkPos(pos.x - 1, pos.y - 1)).elevation);
    }

    @Override
    public void render() {
        Vec3d worldPos = new Vec3d(CHUNK_SIZE * pos.x, CHUNK_SIZE * pos.y, 0);
        SHADER.setUniform("projectionMatrix", Camera.getProjectionMatrix());
        SHADER.setUniform("modelViewMatrix", Camera.camera.getWorldMatrix(worldPos));
        SHADER.setUniform("color", new Vec4d(12, 135, 10, 255).div(255));
        using(Arrays.asList(SHADER, vao), () -> {
            glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        });
    }

    @Override
    public void update(double dt) {
//        if (world.getChunkPos(Camera.camera.position).distance(pos) > RENDER_DISTANCE + 2) {
//            destroy();
//        }
    }
}
