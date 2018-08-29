package game;

import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import static game.Settings.ENABLE_LOD;
import static game.Settings.RENDER_DISTANCE;
import game.creatures.Doggo;
import game.creatures.Kitteh;
import game.creatures.Skeletor;
import static graphics.Sprite.SPRITE_SHADER;
import gui.GUIManager;
import java.util.Comparator;
import java.util.Optional;
import opengl.Camera;
import opengl.Framebuffer;
import opengl.GLState;
import opengl.ShaderProgram;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import util.Multithreader;
import util.Resources;
import util.vectors.Vec3d;
import world.ChunkPos;
import world.World;

public abstract class Main {

    public static void main(String[] args) {
        Core.init();

        Framebuffer f = new Framebuffer(true, true, true);

        Framebuffer blur1 = new Framebuffer(true, false, false);
        Framebuffer blur2 = new Framebuffer(true, false, false);

        onRender(-10, () -> {
            glClearColor(.6f, .8f, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            f.bind();
            glClearColor(0, 0, 0, 0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        });

        ShaderProgram blurShader = Resources.loadShaderProgram("blur");

        onRender(5, () -> {
            GLState.disable(GL_DEPTH_TEST);

            blur1.bind();
            blurShader.setUniform("horizontal", true);
            Framebuffer.draw(f.colorBuffer2, blurShader);

            for (int i = 0; i < 1; i++) {
                blur2.bind();
                blurShader.setUniform("horizontal", false);
                Framebuffer.draw(blur1.colorBuffer, blurShader);
                blur1.bind();
                blurShader.setUniform("horizontal", true);
                Framebuffer.draw(blur2.colorBuffer, blurShader);
            }

            GLState.bindFramebuffer(null);
            Framebuffer.draw(f.colorBuffer, SPRITE_SHADER);

            GLState.setBlendFunc(GL_ONE, GL_ONE);
            blurShader.setUniform("horizontal", false);
            Framebuffer.draw(blur1.colorBuffer, blurShader);
            GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            GLState.enable(GL_DEPTH_TEST);
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
            if (Input.keyJustPressed(GLFW_KEY_L)) {
                ENABLE_LOD = !ENABLE_LOD;
                System.out.println(ENABLE_LOD);
            }
            if (Input.keyDown(GLFW_KEY_R)) {
                world.waterManager.spawnWater = Camera.camera3d.position.floor();
            }
            if (Input.keyJustReleased(GLFW_KEY_R)) {
                world.waterManager.spawnWater = null;
            }
            if (Input.keyDown(GLFW_KEY_T)) {
                world.waterManager.spawnWater = Camera.camera3d.position.floor();
            }
            if (Input.keyDown(GLFW_KEY_Y)) {
                world.waterManager.spawnWater = null;
            }
        });

        Core.run();
    }
}
