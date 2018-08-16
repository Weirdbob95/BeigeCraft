package game.gui;

import game.items.ItemSlot;
import util.vectors.Vec2d;

public class GUIInventorySquare extends GUIRectangle {

    public static int INVENTORY_SQUARE_SIZE = 64;

    public ItemSlot itemSlot;
    public GUIText count;

    public GUIInventorySquare(ItemSlot itemSlot) {
        this.itemSlot = itemSlot;
        size = new Vec2d(INVENTORY_SQUARE_SIZE, INVENTORY_SQUARE_SIZE);
        color = null;

        count = new GUIText(null);
        count.offset = new Vec2d(-30, -18);
        count.centered = false;

        add(count);
    }

    @Override
    protected void render() {
        super.render();
        if (itemSlot != null && itemSlot.item() != null) {
            itemSlot.item().renderGUI(center());
            if (itemSlot.count() > 1) {
                count.setText("" + itemSlot.count());
            } else {
                count.setText(null);
            }
        } else {
            count.setText(null);
        }
    }
}
