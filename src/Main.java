
import engine.Core;
import engine.Input;
import engine.MiscBehaviors.FPSBehavior;
import static engine.MiscBehaviors.onRender;
import static engine.MiscBehaviors.onUpdate;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.opengl.GL11.*;

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

        new FPSBehavior().create();

        Core.run();
    }
}
