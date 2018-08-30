package gui;

import game.items.ItemSlot;
import static gui.GUIInventorySquare.INVENTORY_SQUARE_SIZE;
import util.vectors.Vec2d;

public class GUIInventoryGrid extends GUIRectangle {

    public static Vec2d spacing = new Vec2d(8, 8);

    public GUIInventoryGrid(int width, int height, ItemSlot[] backing) {
        this.color = null;
        size = new Vec2d(width, height).mul(INVENTORY_SQUARE_SIZE)
                .add(new Vec2d(width + 1, height + 1).mul(spacing));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GUIInventorySquare s = new GUIInventorySquare(backing[x + y * width]);
                s.offset = size.mul(-.5).add(new Vec2d(x + .5, y + .5).mul(INVENTORY_SQUARE_SIZE)).add(new Vec2d(x + 1, y + 1).mul(spacing));
                add(s);
            }
        }
    }
}
