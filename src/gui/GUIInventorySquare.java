package gui;

import game.inventory.ItemSlot;
import util.math.Vec2d;
import util.math.Vec4d;

public class GUIInventorySquare extends GUIRectangle {

    public static int INVENTORY_SQUARE_SIZE = 64;

    public ItemSlot itemSlot;
    public GUIText count;

    public GUIInventorySquare(ItemSlot itemSlot) {
        this.itemSlot = itemSlot;
        size = new Vec2d(INVENTORY_SQUARE_SIZE, INVENTORY_SQUARE_SIZE);
        color = new Vec4d(1, 1, 1, .1);

        count = new GUIText(null);
        count.offset = new Vec2d(-30, -18);
        count.centered = false;

        add(count);
    }

    @Override
    protected void onHoverStart() {
        color = new Vec4d(1, 1, 1, .3);
    }

    @Override
    protected void onHoverStop() {
        color = new Vec4d(1, 1, 1, .1);
    }

    @Override
    protected void render() {
        if (itemSlot == null) {
            borderColor = new Vec4d(0, 0, 0, 1);
        } else if (itemSlot == ItemSlot.MAIN_HAND) {
            if (itemSlot == ItemSlot.OFF_HAND) {
                borderColor = new Vec4d(.5, 0, .5, 1);
            } else {
                borderColor = new Vec4d(1, 0, 0, 1);
            }
        } else {
            if (itemSlot == ItemSlot.OFF_HAND) {
                borderColor = new Vec4d(0, 0, 1, 1);
            } else {
                borderColor = new Vec4d(0, 0, 0, 1);
            }
        }
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
