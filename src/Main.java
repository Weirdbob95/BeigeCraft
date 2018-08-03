
import engine.Core;
import engine.Input;
import engine.MiscBehaviors.FPSBehavior;
import static engine.MiscBehaviors.onRender;
import static engine.MiscBehaviors.onUpdate;
import static opengl.Camera.camera;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.opengl.GL11.*;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.RenderedChunk;
import world.World;

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

            if (Input.keyDown(GLFW_KEY_SPACE)) {
                camera.position = camera.position.add(new Vec3d(0, 0, 10 * dt));
            }
            if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
                camera.position = camera.position.add(new Vec3d(0, 0, -10 * dt));
            }
        });

        new FPSBehavior().create();

        World w = new World();
        for (int x = -15; x < 15; x++) {
            for (int y = -15; y < 15; y++) {
                RenderedChunk r = new RenderedChunk();
                r.generateAtPos(w, new ChunkPos(x, y));
                r.create();
            }
        }

        Core.run();
    }
}
