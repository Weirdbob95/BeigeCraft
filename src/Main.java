
import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import game.Player;
import java.util.Comparator;
import java.util.Optional;
import opengl.Camera;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;
import util.Multithreader;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.PrerenderedChunk;
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

//        onUpdate(0, dt -> {
//            double speed = 100;
//            if (Input.keyDown(GLFW_KEY_W)) {
//                camera.position = camera.position.add(camera.facing().mul(speed * dt));
//            }
//            if (Input.keyDown(GLFW_KEY_S)) {
//                camera.position = camera.position.add(camera.facing().mul(-speed * dt));
//            }
//            if (Input.keyDown(GLFW_KEY_A)) {
//                camera.position = camera.position.add(camera.up.cross(camera.facing()).normalize().mul(speed * dt));
//            }
//            if (Input.keyDown(GLFW_KEY_D)) {
//                camera.position = camera.position.add(camera.up.cross(camera.facing()).normalize().mul(-speed * dt));
//            }
//            if (Input.keyDown(GLFW_KEY_SPACE)) {
//                camera.position = camera.position.add(camera.up.mul(speed * dt));
//            }
//            if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {
//                camera.position = camera.position.add(camera.up.mul(-speed * dt));
//            }
//        });
        new FPSBehavior().create();

        World world = new World();

        Player p = new Player();
        p.position.position = new Vec3d(0, 0, 50);
        p.physics.world = world;
        p.create();

        int initialWorldSize = 1;

        new PrerenderedChunk();

        for (int x = -initialWorldSize; x < initialWorldSize; x++) {
            for (int y = -initialWorldSize; y < initialWorldSize; y++) {
                //world.prerenderedChunks.get(new ChunkPos(x, y));
                world.renderedChunks.get(new ChunkPos(x, y));
            }
        }

        onUpdate(0, dt -> {
            if (Multithreader.isFree()) {
                ChunkPos camera = world.getChunkPos(Camera.camera.position);
                Optional<ChunkPos> toRender = world.renderedChunks.border.stream()
                        .min(Comparator.comparingInt(camera::distance));
                if (toRender.isPresent() && camera.distance(toRender.get()) <= RENDER_DISTANCE) {
                    world.renderedChunks.lazyGenerate(toRender.get());
                }
//                else {
//                    for (int i = 0; i < 2; i++) {
//                        Optional<ChunkPos> toPrerender = Stream.concat(world.renderedChunks.border.stream(), world.prerenderedChunks.border.stream())
//                                .filter(cp -> !world.renderedChunks.has(cp))
//                                .filter(cp -> !world.prerenderedChunks.has(cp))
//                                .min(Comparator.comparingInt(camera::distance));
//                        if (toPrerender.isPresent() && camera.distance(toPrerender.get()) <= 3 * RENDER_DISTANCE) {
//                            world.prerenderedChunks.lazyGenerate(toPrerender.get());
//                        }
//                    }
//            }
            }
        });

        Core.run();
    }
}
