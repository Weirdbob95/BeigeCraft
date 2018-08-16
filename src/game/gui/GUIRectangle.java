package game.gui;

import graphics.Graphics;
import util.vectors.Vec4d;

public class GUIRectangle extends GUIItem {

    public Vec4d color = new Vec4d(.4, .4, .4, 1);
    public Vec4d borderColor = new Vec4d(0, 0, 0, 1);

    @Override
    protected void render() {
        if (color != null) {
            Graphics.drawRectangle(getLowerLeft(), 0, size, color);
        }
        if (borderColor != null) {
            Graphics.drawRectangleOutline(getLowerLeft(), 0, size, borderColor);
        }
    }
}
