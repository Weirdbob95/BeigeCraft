package game.gui;

import engine.Input;
import game.items.ItemSlot;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class InventoryRoot extends GUIRoot {

    public ItemSlot dragSource;

    public InventoryRoot(GUIManager manager) {
        super(manager);
        this.color = new Vec4d(.6, .6, .6, .95);

        GUIInventoryGrid grid = new GUIInventoryGrid(12, 4);
        grid.offset = new Vec2d(0, -175);

        GUIInventoryQAW qaw = new GUIInventoryQAW();
        qaw.offset = new Vec2d(0, 160);

        DraggedItem draggedItem = new DraggedItem();

        add(grid, qaw, draggedItem);
    }

    @Override
    protected void close() {
        if (dragSource != null) {
            ItemSlot.GRABBED.moveItemsTo(dragSource);
            dragSource = null;
        }
    }

    @Override
    protected void render() {
        super.render();
        if (Input.mouseJustPressed(0)) {
            if (manager.selected instanceof GUIInventorySquare) {
                dragSource = ((GUIInventorySquare) manager.selected).itemSlot;
                dragSource.moveItemsTo(ItemSlot.GRABBED);
            }
        }
        if (Input.mouseJustReleased(0)) {
            if (dragSource != null) {
                if (manager.selected instanceof GUIInventorySquare) {
                    ItemSlot newSlot = ((GUIInventorySquare) manager.selected).itemSlot;
                    ItemSlot.GRABBED.moveItemsTo(newSlot);
                }
                ItemSlot.GRABBED.moveItemsTo(dragSource);
                dragSource = null;
            }
        }
    }

    private class DraggedItem extends GUIItem {

        @Override
        protected void render() {
            if (ItemSlot.GRABBED.item() != null) {
                ItemSlot.GRABBED.item().renderGUI(manager.mouse);
            }
        }
    }
}
