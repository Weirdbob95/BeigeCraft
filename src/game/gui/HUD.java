package game.gui;

import util.vectors.Vec4d;

public class HUD extends GUIItem {

    public HUD() {
        GUISprite crosshares = new GUISprite("crosshares.png");
        crosshares.color = new Vec4d(1, 1, 1, .5);

        add(crosshares);
    }

    @Override
    protected void render() {
    }
}
