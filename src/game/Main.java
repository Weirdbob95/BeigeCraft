package game;

import behaviors.MiscBehaviors.FPSBehavior;
import static behaviors.MiscBehaviors.onRender;
import static behaviors.MiscBehaviors.onUpdate;
import engine.Core;
import engine.Input;
import static game.Settings.*;
import game.creatures.Doggo;
import game.creatures.Goblin;
import game.creatures.Kitteh;
import game.creatures.Skeletor;
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
import static util.math.MathUtils.max;
import util.math.Vec3d;
import util.math.Vec4d;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public abstract class Main {

    public static void main(String[] args) {
        Core.init();

        Framebuffer f = new Framebuffer(true, true, true);

        Framebuffer blur1 = new Framebuffer(true, false, false);

        onRender(-10, () -> {
            f.clear(new Vec4d(0, 0, 0, 0));
            GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        });

        ShaderProgram blurShader = Resources.loadShaderProgram("simple2d", "blur");
        ShaderProgram simpleShader = Resources.loadShaderProgram("simple2d");

        onRender(5, () -> {
            GLState.inTempState(() -> {
                GLState.disable(GL_DEPTH_TEST);

                if (BLOOM) {
                    blurShader.setUniform("horizontal", true);
                    blur1.drawToSelf(f.colorBuffer2, blurShader);
                }

                Framebuffer.clearWindow(new Vec4d(.6, .8, 1, 1));
                Framebuffer.drawToWindow(f.colorBuffer, simpleShader);

                if (BLOOM) {
                    GLState.setBlendFunc(GL_ONE, GL_ONE);
                    blurShader.setUniform("horizontal", false);
                    Framebuffer.drawToWindow(blur1.colorBuffer, blurShader);
                    GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                }
            });

            GLState.setBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GLState.bindFramebuffer(null);
        });

        World world = new World();
        world.create();

        new FPSBehavior().create();

        GUIManager gui = new GUIManager();
        gui.create();

        Player p = new Player();
        int maxHeight = max(world.heightmappedChunks.get(new ChunkPos(0, 0)).elevationAt(0, 0),
                world.heightmappedChunks.get(new ChunkPos(-1, 0)).elevationAt(CHUNK_SIZE - 1, 0),
                world.heightmappedChunks.get(new ChunkPos(0, -1)).elevationAt(0, CHUNK_SIZE - 1),
                world.heightmappedChunks.get(new ChunkPos(-1, -1)).elevationAt(CHUNK_SIZE - 1, CHUNK_SIZE - 1));
        p.position.position = new Vec3d(.4 * Math.random() - .2, .4 * Math.random() - .2, maxHeight + 4);
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
                doot.monster.position.position = p.position.position;
                doot.monster.model.rotation = Camera.camera3d.horAngle;
                doot.monster.physics.world = world;
                doot.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_H)) {
                Kitteh shadow = new Kitteh();
                shadow.monster.position.position = p.position.position;
                shadow.monster.model.rotation = Camera.camera3d.horAngle;
                shadow.monster.physics.world = world;
                shadow.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_J) || Input.keyDown(GLFW_KEY_K)) {
                Doggo ziggy = new Doggo();
                ziggy.monster.position.position = p.position.position;
                ziggy.monster.model.rotation = Camera.camera3d.horAngle;
                ziggy.monster.physics.world = world;
                ziggy.create();
            }
            if (Input.keyJustPressed(GLFW_KEY_M)) {
                Goblin squee = new Goblin();
                squee.enemy.monster.position.position = p.position.position;
                squee.enemy.monster.model.rotation = Camera.camera3d.horAngle;
                squee.enemy.monster.physics.world = world;
                squee.create();
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
