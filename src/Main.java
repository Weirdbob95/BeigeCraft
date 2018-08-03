
import engine.Core;
import engine.Input;
import engine.MiscBehaviors.FPSBehavior;
import static engine.MiscBehaviors.onRender;
import static engine.MiscBehaviors.onUpdate;
import opengl.Camera;
import static opengl.Camera.camera;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.opengl.GL11.*;
import util.Multithreader;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;
import static world.World.RENDER_DISTANCE;

public abstract class Main {

    public static void main(String[] args) {
        Core.init();

        onUpdate(0, dt -> {
            if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
                Core.stopGame();
            }
        });

        onRender(-10, () -> {
            glClearColor(0.2f, 0.2f, 0.2f, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        });

        onUpdate(0, dt -> {
            // Look around
            camera.horAngle -= Input.mouseDelta().x / 500;
            camera.vertAngle += Input.mouseDelta().y / 500;

            if (camera.vertAngle > 1.5) {
                camera.vertAngle = 1.5f;
            }
            if (camera.vertAngle < -1.5) {
                camera.vertAngle = -1.5f;
            }

            double speed = 100;
            if (Input.keyDown(GLFW_KEY_W)) {
                camera.position = camera.position.add(camera.facing().mult(speed * dt));
            }
            if (Input.keyDown(GLFW_KEY_S)) {
                camera.position = camera.position.add(camera.facing().mult(-speed * dt));
            }
            if (Input.keyDown(GLFW_KEY_A)) {
                camera.position = camera.position.add(camera.up.cross(camera.facing()).normalize().mult(speed * dt));
            }
            if (Input.keyDown(GLFW_KEY_D)) {
                camera.position = camera.position.add(camera.up.cross(camera.facing()).normalize().mult(-speed * dt));
            }
            if (Input.keyDown(GLFW_KEY_SPACE)) {
                camera.position = camera.position.add(camera.up.mult(speed * dt));
            }
            if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
                camera.position = camera.position.add(camera.up.mult(-speed * dt));
            }
        });

        new FPSBehavior().create();

        World world = new World();

        Camera.camera.position = new Vec3d(0, 0, 50);

        int initialWorldSize = 1;

        for (int x = -initialWorldSize; x < initialWorldSize; x++) {
            for (int y = -initialWorldSize; y < initialWorldSize; y++) {
                world.getRenderedChunk(new ChunkPos(x, y));
            }
        }

        onUpdate(0, dt -> {
            ChunkPos toLoad = null;
            ChunkPos camera = world.getChunkPos(Camera.camera.position);
            for (int x = camera.x - RENDER_DISTANCE; x < camera.x + RENDER_DISTANCE; x++) {
                for (int y = camera.y - RENDER_DISTANCE; y < camera.y + RENDER_DISTANCE; y++) {
                    if (!world.hasRenderedChunk(new ChunkPos(x, y))) {
                        if (toLoad == null || camera.distance(new ChunkPos(x, y)) < camera.distance(toLoad)) {
                            toLoad = new ChunkPos(x, y);
                        }
                    }
                }
            }
            if (toLoad != null && camera.distance(toLoad) <= RENDER_DISTANCE) {
                ChunkPos toLoadFinal = toLoad;
                Multithreader.runIfConvenient(() -> world.getRenderedChunk(toLoadFinal));
            }
        });

        Core.run();
    }
}
