package engine;

import java.util.BitSet;
import opengl.Window;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import util.math.Vec2d;

public abstract class Input {

    static void init() {
        Window.window.setCursorPosCallback((window, xpos, ypos) -> {
            mouse = new Vec2d(xpos, ypos);
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

    private static Vec2d mouse = new Vec2d(0, 0);
    private static Vec2d prevMouse = new Vec2d(0, 0);

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

    public static Vec2d mouse() {
        return mouse;
    }

    public static Vec2d mouseDelta() {
        return mouse.sub(prevMouse);
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
