package gui;

import engine.Behavior;
import engine.Input;
import opengl.Window;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import util.vectors.Vec2d;

public class GUIManager extends Behavior {

    public final MenuRoot menuRoot = new MenuRoot(this);
    public final OptionsRoot optionsRoot = new OptionsRoot(this);
    public final InventoryRoot inventoryRoot = new InventoryRoot(this);
    public final QAWRoot qawRoot = new QAWRoot(this);
    public final HUD hud = new HUD();

    public GUIRoot root;
    public GUIItem selected;
    public Vec2d mouse;

    public boolean freezeMouse() {
        return root != null && root.freezeMouse();
    }

    public boolean freezeMovement() {
        return root != null && root.freezeMovement();
    }

    @Override
    public void render() {
        glDisable(GL_DEPTH_TEST);
        hud.renderOuter();
        if (root != null) {
            root.renderOuter();
        }
        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public double renderLayer() {
        return 10;
    }

    public void setRoot(GUIRoot newRoot) {
        if (root != newRoot) {
            if (root != null) {
                root.close();
            }
            root = newRoot;
            if (root != null) {
                root.open();
                Window.window.setCursorEnabled(root.showMouse());
            } else {
                Window.window.setCursorEnabled(false);
            }
        }
    }

    @Override
    public void update(double dt) {
        if (Input.keyJustPressed(GLFW_KEY_ESCAPE)) {
            if (root == null) {
                setRoot(menuRoot);
            } else {
                setRoot(null);
            }
        }
        if (Input.keyJustPressed(GLFW_KEY_E)) {
            if (root != inventoryRoot) {
                setRoot(inventoryRoot);
            } else {
                setRoot(null);
            }
        }
        if (Input.keyJustPressed(GLFW_KEY_Q)) {
            if (root == null) {
                setRoot(qawRoot);
            }
        }
        if (Input.keyJustReleased(GLFW_KEY_Q)) {
            if (root == qawRoot) {
                setRoot(null);
            }
        }

        if (root != null) {
            mouse = new Vec2d(Input.mouse().x - Window.WIDTH / 2, Window.HEIGHT / 2 - Input.mouse().y);
            GUIItem newSelected = root.allChildren().filter(i -> mouse.x >= i.getLowerLeft().x && mouse.x < i.getUpperRight().x
                    && mouse.y >= i.getLowerLeft().y && mouse.y < i.getUpperRight().y).findFirst().orElse(null);
            if (newSelected != selected) {
                if (selected != null) {
                    selected.onHoverStop();
                }
                if (newSelected != null) {
                    newSelected.onHoverStart();
                }
            }
            selected = newSelected;
            if (Input.mouseJustReleased(0) && selected != null) {
                selected.onClick();
            }
        }
    }
}
