
import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import game.Doggo;
import game.Hamster;
import game.Player;
import java.util.Comparator;
import java.util.Optional;
import opengl.Camera;
import static org.lwjgl.glfw.GLFW.*;
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
            glClearColor(.6f, .8f, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        });

        new FPSBehavior().create();

        World world = new World();
        world.create();

        Player p = new Player();
        p.position.position = new Vec3d(Math.random() - .5, Math.random() - .5, 150);
        p.physics.world = world;
        p.create();

        int initialWorldSize = 1;

        for (int x = -initialWorldSize; x < initialWorldSize; x++) {
            for (int y = -initialWorldSize; y < initialWorldSize; y++) {
                //world.prerenderedChunks.get(new ChunkPos(x, y));
                world.renderedChunks.get(new ChunkPos(x, y));
            }
        }

        onUpdate(0, dt -> {
            if (Multithreader.isFree()) {
                ChunkPos camera = world.getChunkPos(Camera.camera.position);
                Optional<ChunkPos> toRender = world.renderedChunks.border().stream()
                        .min(Comparator.comparingDouble(camera::distance));
                if (toRender.isPresent() && camera.distance(toRender.get()) <= RENDER_DISTANCE) {
                    world.renderedChunks.lazyGenerate(toRender.get());
                }
            }
        });

        onUpdate(0, dt -> {
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                Hamster hammy = new Hamster();
                hammy.model.position.position = p.position.position;
                hammy.model.rotation = Camera.camera.horAngle;
                hammy.physics.world = world;
                hammy.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_J)) {
                Doggo ziggy = new Doggo();
                ziggy.model.position.position = p.position.position;
                ziggy.model.rotation = Camera.camera.horAngle;
                ziggy.physics.world = world;
                ziggy.create();
            }
//            if (Input.keyJustPressed(GLFW_KEY_Y)) {
//                Hamster hammy = new Hamster();
//                hammy.model.position.position = p.position.position;
//                hammy.model.rotation = Camera.camera.horAngle;
//                hammy.physics.world = world;
//                hammy.create();
//                hammy.model.loadModel("skelelarge.vox");
//            }
//            if (Input.keyJustPressed(GLFW_KEY_U)) {
//                Hamster hammy = new Hamster();
//                hammy.model.position.position = p.position.position;
//                hammy.model.rotation = Camera.camera.horAngle;
//                hammy.physics.world = world;
//                hammy.create();
//                hammy.model.loadModel("skelesmall.vox");
//            }
//            if (Input.keyJustPressed(GLFW_KEY_J)) {
//                Hamster hammy = new Hamster();
//                hammy.model.position.position = p.position.position;
//                hammy.model.rotation = Camera.camera.horAngle;
//                hammy.physics.world = world;
//                hammy.create();
//                hammy.model.loadModel("ziggy2.vox");
//            }
        });

        // MEMORY ALLOCATION DEBUG INFO
//        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
//            if (mpBean.getType() == MemoryType.HEAP) {
//                System.out.printf(
//                        "Name: %s: %s\n",
//                        mpBean.getName(), mpBean.getUsage()
//                );
//            }
//        }
        Core.run();
    }
}
