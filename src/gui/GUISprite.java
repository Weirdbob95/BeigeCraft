package gui;

import graphics.Sprite;
import util.math.Vec4d;

public class GUISprite extends GUIItem {

    public Sprite sprite;
    public double rotation;
    public double scale = 1;
    public Vec4d color = new Vec4d(1, 1, 1, 1);

    public GUISprite(String fileName) {
        sprite = Sprite.load(fileName);
    }

    @Override
    protected void render() {
        sprite.draw2d(center(), rotation, scale, color);
    }
}
