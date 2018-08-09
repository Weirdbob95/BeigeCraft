package game;


import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import game.Doggo;
import game.Goblin;
import game.Hamster;
import game.Player;
import graphics.Sprite;
import java.util.Comparator;
import java.util.Optional;
import opengl.Camera;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import util.Multithreader;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.ChunkPos;
import world.World;
import static world.World.RENDER_DISTANCE;

public abstract class Main {

    public static final boolean LOW_GRAPHICS = true;

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
        p.position.position = new Vec3d(.4 + .2 * Math.random(), .4 + .2 * Math.random(),
                world.heightmappedChunks.get(new ChunkPos(0, 0)).heightmap[0][0] + 2);
        Camera.camera3d.position = p.position.position;
        p.physics.world = world;
        p.create();

        int initialWorldSize = 1;

        for (int x = -initialWorldSize; x < initialWorldSize; x++) {
            for (int y = -initialWorldSize; y < initialWorldSize; y++) {
                world.renderedChunks.get(new ChunkPos(x, y));
            }
        }

        onUpdate(0, dt -> {
            if (Multithreader.isFree()) {
                ChunkPos camera = world.getChunkPos(Camera.camera3d.position);
                Optional<ChunkPos> toRender = world.renderedChunks.border().stream()
                        .min(Comparator.comparingDouble(camera::distance));
                if (toRender.isPresent() && camera.distance(toRender.get()) <= RENDER_DISTANCE) {
                    world.renderedChunks.lazyGenerate(toRender.get());
                }
            }
        });

        onUpdate(0, dt -> {
            if (Input.keyJustPressed(GLFW_KEY_G)) {
                Goblin gobbo = new Goblin();
                gobbo.model.position.position = p.position.position;
                gobbo.model.rotation = Camera.camera3d.horAngle;
                gobbo.physics.world = world;
                gobbo.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                Hamster hammy = new Hamster();
                hammy.model.position.position = p.position.position;
                hammy.model.rotation = Camera.camera3d.horAngle;
                hammy.physics.world = world;
                hammy.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_J)) {
                Doggo ziggy = new Doggo();
                ziggy.model.position.position = p.position.position;
                ziggy.model.rotation = Camera.camera3d.horAngle;
                ziggy.physics.world = world;
                ziggy.create();
            }
        });

        onRender(10, () -> {
            glDisable(GL_DEPTH_TEST);
            Sprite.load("crosshares.png").draw2d(new Vec2d(0, 0), 0, 1, new Vec4d(1, 1, 1, .5));
            glEnable(GL_DEPTH_TEST);
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
