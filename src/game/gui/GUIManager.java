package game.gui;

import engine.Behavior;
import engine.Input;
import opengl.Window;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import util.vectors.Vec2d;

public class GUIManager extends Behavior {

    public final MenuRoot menuRoot = new MenuRoot(this);
    public final OptionsRoot optionsRoot = new OptionsRoot(this);
    public final InventoryRoot inventoryRoot = new InventoryRoot(this);
    public final HUD hud = new HUD();

    private GUIItem root;
    private GUIItem selected;

    public boolean inMenu() {
        return root != null;
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

    public void setRoot(GUIItem newRoot) {
        if (root != newRoot) {
            Window.window.setCursorEnabled(newRoot != null);
            root = newRoot;
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
        if (Input.keyJustPressed(GLFW_KEY_I)) {
            if (root != inventoryRoot) {
                setRoot(inventoryRoot);
            } else {
                setRoot(null);
            }
        }

        if (root != null) {
            Vec2d mouse = new Vec2d(Input.mouse().x - Window.WIDTH / 2, Window.HEIGHT / 2 - Input.mouse().y);
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
