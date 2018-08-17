package gui;

import engine.Input;
import game.items.ItemSlot;
import graphics.Font;
import static util.MathUtils.ceil;
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
            if (dragSource == null) {
                if (manager.selected instanceof GUIInventorySquare) {
                    dragSource = ((GUIInventorySquare) manager.selected).itemSlot;
                    dragSource.moveItemsTo(ItemSlot.GRABBED);
                }
            }
        }
        if (Input.mouseJustPressed(1)) {
            if (dragSource != null) {
                if (manager.selected instanceof GUIInventorySquare) {
                    ItemSlot newSlot = ((GUIInventorySquare) manager.selected).itemSlot;
                    ItemSlot.GRABBED.moveItemsTo(newSlot, 1);
                }
            } else {
                if (manager.selected instanceof GUIInventorySquare) {
                    dragSource = ((GUIInventorySquare) manager.selected).itemSlot;
                    dragSource.moveItemsTo(ItemSlot.GRABBED, ceil(dragSource.count() / 2.));
                }
            }
        }
        if ((Input.mouseJustReleased(0) && !Input.mouseDown(1)) || (Input.mouseJustReleased(1) && !Input.mouseDown(0))) {
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
                if (ItemSlot.GRABBED.count() > 1) {
                    Font.load("arial_outline").renderText("" + ItemSlot.GRABBED.count())
                            .draw2d(manager.mouse.add(new Vec2d(-30, -18)), 0, 1, new Vec4d(1, 1, 1, 1), new Vec4d(0, 0, 0, 1));
                }
            }
        }
    }
}