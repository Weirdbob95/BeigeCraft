package game;

import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import static game.Settings.RENDER_DISTANCE;
import game.creatures.Doggo;
import game.creatures.Kitteh;
import game.creatures.Skeletor;
import gui.GUIManager;
import java.util.Comparator;
import java.util.Optional;
import opengl.Camera;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import util.Multithreader;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;

public abstract class Main {

    public static void main(String[] args) {
        Core.init();

        onRender(-10, () -> {
            glClearColor(.6f, .8f, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        });

        new FPSBehavior().create();

        World world = new World();
        world.create();

        GUIManager gui = new GUIManager();
        gui.create();

        Player p = new Player();
        p.position.position = new Vec3d(.4 + .2 * Math.random(), .4 + .2 * Math.random(),
                world.heightmappedChunks.get(new ChunkPos(0, 0)).elevationAt(0, 0) + 2);
        Camera.camera3d.position = p.position.position;
        p.physics.world = world;
        p.gui = gui;
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
                Skeletor doot = new Skeletor();
                doot.creature.position.position = p.position.position;
                doot.creature.model.rotation = Camera.camera3d.horAngle;
                doot.creature.physics.world = world;
                doot.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                Kitteh shadow = new Kitteh();
                shadow.creature.position.position = p.position.position;
                shadow.creature.model.rotation = Camera.camera3d.horAngle;
                shadow.creature.physics.world = world;
                shadow.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_J) || Input.keyDown(GLFW_KEY_K)) {
                Doggo ziggy = new Doggo();
                ziggy.creature.position.position = p.position.position;
                ziggy.creature.model.rotation = Camera.camera3d.horAngle;
                ziggy.creature.physics.world = world;
                ziggy.create();
            }
        });

        Core.run();
    }
}
