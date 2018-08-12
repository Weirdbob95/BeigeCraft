package game.gui;

import util.vectors.Vec4d;

public class GUIButton extends GUIRectangle {

//    public Vec2d center = new Vec2d(0, 0);
//    public Vec2d size = new Vec2d(100, 100);
//    public Vec4d color = new Vec4d(.4, .4, .4, 1);
//    public Vec4d selectedColor = new Vec4d(.6, .6, .6, 1);
//    public Vec4d borderColor = new Vec4d(0, 0, 0, 1);
//    public Runnable onClick;
//    public FontText text;
//
//    public boolean contains(Vec2d pos) {
//        return pos.x >= center.x - size.x / 2 && pos.x < center.x + size.x / 2 && pos.y >= center.y - size.y / 2 && pos.y < center.y + size.y / 2;
//    }
//
//    public void render(boolean selected) {
//        Graphics.drawRectangle(center.sub(size.mul(.5)), 0, size, selected ? selectedColor : color);
//        if (borderColor != null) {
//            Graphics.drawRectangleOutline(center.sub(size.mul(.5)), 0, size, borderColor);
//        }
//        if (text != null) {
//            text.draw2dCentered(center, 0, 2, new Vec4d(1, 1, 1, 1), new Vec4d(0, 0, 0, 1));
//        }
//    }
    private final Runnable onClick;

    public GUIButton(String s, Runnable onClick) {
        if (s != null && !s.isEmpty()) {
            add(new GUIText(s));
        }
        this.onClick = onClick;
    }

    @Override
    protected void onClick() {
        onClick.run();
    }

    @Override
    protected void onHoverStart() {
        color = new Vec4d(.6, .6, .6, 1);
    }

    @Override
    protected void onHoverStop() {
        color = new Vec4d(.4, .4, .4, 1);
    }

    @Override
    public void render() {
        super.render();
    }
}
