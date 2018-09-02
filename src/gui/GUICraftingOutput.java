package gui;

import definitions.ItemType;
import definitions.Recipe;
import game.inventory.ItemSlot;
import static gui.GUIInventorySquare.INVENTORY_SQUARE_SIZE;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class GUICraftingOutput extends GUIRectangle {

    public ItemSlot[] craftingSlots;
    public GUIText count;
    public ItemType outputItem;
    public int outputNum;

    public GUICraftingOutput(ItemSlot[] craftingSlots) {
        this.craftingSlots = craftingSlots;
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
    public void render() {
        super.render();
        Recipe r = Recipe.findMatching(craftingSlots);
        if (r != null) {
            outputItem = r.output();
            outputNum = r.outputNum();
            outputItem.renderGUI(center());
            if (outputNum > 1) {
                count.setText("" + outputNum);
            } else {
                count.setText(null);
            }
        } else {
            outputItem = null;
            count.setText(null);
        }
    }
}
