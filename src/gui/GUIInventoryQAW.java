package gui;

import game.items.ItemSlot;
import graphics.Graphics;
import util.MathUtils;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class GUIInventoryQAW extends GUIItem {

    public GUIInventoryQAW() {
        for (int i = 0; i < 8; i++) {
            GUIInventorySquare s = new GUIInventorySquare(ItemSlot.QAW[i]);
            s.offset = MathUtils.rotate(new Vec2d(120, 0), Math.PI / 4 * i);
            add(s);
        }
    }

    @Override
    protected void render() {
        Graphics.drawCircleOutline(center(), 170, new Vec4d(0, 0, 0, 1));
    }
}
