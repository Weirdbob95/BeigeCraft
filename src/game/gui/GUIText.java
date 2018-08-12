package game.gui;

import graphics.Font;
import graphics.Font.FontText;
import util.vectors.Vec4d;

public class GUIText extends GUIItem {

    private FontText text;
    public boolean centered = true;
    public double scale = 1;
    public Vec4d color = new Vec4d(1, 1, 1, 1);
    public Vec4d outlineColor = new Vec4d(0, 0, 0, 1);

    public GUIText(String s) {
        setText(s);
    }

    @Override
    protected void render() {
        if (centered) {
            if (outlineColor == null) {
                text.draw2dCentered(center(), 0, scale, color);
            } else {
                text.draw2dCentered(center(), 0, scale, color, outlineColor);
            }
        } else {
            if (outlineColor == null) {
                text.draw2d(center(), 0, scale, color);
            } else {
                text.draw2d(center(), 0, scale, color, outlineColor);
            }
        }
    }

    public final void setText(String s) {
        text = Font.load("arial_outline").renderText(s);
    }
}
