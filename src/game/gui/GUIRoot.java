package game.gui;

import opengl.Window;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class GUIRoot extends GUIRectangle {

    public final GUIManager manager;

    public GUIRoot(GUIManager manager) {
        this.manager = manager;
        size = new Vec2d(Window.WIDTH, Window.HEIGHT).mul(.8);
        color = new Vec4d(.2, .2, .2, .95);
    }
}
