package engine;

import java.util.BitSet;
import opengl.Window;
import org.joml.Vector2d;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public abstract class Input {

    static void init() {
        Window.window.setCursorPosCallback((window, xpos, ypos) -> {
            mouse = new Vector2d(xpos, ypos);
        });
        Window.window.setKeyCallback((window, key, scancode, action, mods) -> {
            if (key >= 0) {
                keys.set(key, action != GLFW_RELEASE);
            }
        });
        Window.window.setMouseButtonCallback((window, button, action, mods) -> {
            buttons.set(button, action != GLFW_RELEASE);
        });
        Window.window.setScrollCallback((window, xoffset, yoffset) -> {
            mouseWheel = yoffset;
        });
    }

    static void nextFrame() {
        prevKeys = (BitSet) keys.clone();
        prevMouse = mouse;
        prevButtons = (BitSet) buttons.clone();
        mouseWheel = 0;
    }

    private static BitSet keys = new BitSet();
    private static BitSet prevKeys = new BitSet();

    private static Vector2d mouse = new Vector2d();
    private static Vector2d prevMouse = new Vector2d();

    private static BitSet buttons = new BitSet();
    private static BitSet prevButtons = new BitSet();

    private static double mouseWheel;

    public static boolean keyDown(int key) {
        return keys.get(key);
    }

    public static boolean keyJustPressed(int key) {
        return keys.get(key) && !prevKeys.get(key);
    }

    public static boolean keyJustReleased(int key) {
        return !keys.get(key) && prevKeys.get(key);
    }

    public static Vector2d mouse() {
        return new Vector2d(mouse);
    }

    public static Vector2d mouseDelta() {
        return mouse.sub(prevMouse, new Vector2d());
    }

    public static boolean mouseDown(int button) {
        return buttons.get(button);
    }

    public static boolean mouseJustPressed(int button) {
        return buttons.get(button) && !prevButtons.get(button);
    }

    public static boolean mouseJustReleased(int button) {
        return !buttons.get(button) && prevButtons.get(button);
    }

    public static double mouseWheel() {
        return mouseWheel;
    }
}
