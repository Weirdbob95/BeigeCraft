package gui;

import engine.Input;
import game.inventory.ItemSlot;
import graphics.Font;
import static util.math.MathUtils.ceil;
import static org.lwjgl.glfw.GLFW.*;
import util.math.Vec2d;
import util.math.Vec4d;

public class InventoryRoot extends GUIRoot {

    public ItemSlot[] craftingSlots = ItemSlot.makeItemSlots(4);
    public ItemSlot dragSource;

    public InventoryRoot(GUIManager manager) {
        super(manager);
        this.color = new Vec4d(.6, .6, .6, .95);

        GUIInventoryGrid grid = new GUIInventoryGrid(12, 4, ItemSlot.INVENTORY);
        grid.offset = new Vec2d(0, -175);

        GUIInventoryQAW qaw = new GUIInventoryQAW();
        qaw.offset = new Vec2d(0, 160);

        GUIInventoryGrid craftingGrid = new GUIInventoryGrid(2, 2, craftingSlots);
        craftingGrid.offset = new Vec2d(300, 160);

        GUICraftingOutput craftingOutput = new GUICraftingOutput(craftingSlots);
        craftingOutput.offset = new Vec2d(450, 160);

        DraggedItem draggedItem = new DraggedItem();

        add(grid, qaw, craftingGrid, craftingOutput, draggedItem);
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
        if (Input.keyDown(GLFW_KEY_LEFT_SHIFT)) {

            if (Input.mouseJustPressed(0)) {
                if (manager.selected instanceof GUIInventorySquare) {
                    ItemSlot.MAIN_HAND = ((GUIInventorySquare) manager.selected).itemSlot;
                }

            }

            if (Input.mouseJustPressed(1)) {
                if (manager.selected instanceof GUIInventorySquare) {
                    ItemSlot.OFF_HAND = ((GUIInventorySquare) manager.selected).itemSlot;
                }

            }

        } else {
            if (Input.mouseJustPressed(0)) {
                if (manager.selected instanceof GUIInventorySquare) {
                    if (ItemSlot.GRABBED.isEmpty()) {
                        dragSource = ((GUIInventorySquare) manager.selected).itemSlot;
                        dragSource.moveItemsTo(ItemSlot.GRABBED);
                    } else {
                        ItemSlot newSlot = ((GUIInventorySquare) manager.selected).itemSlot;
                        newSlot.moveItemsTo(ItemSlot.GRABBED);
                        ItemSlot.GRABBED.swapItems(newSlot);
                    }
                } else if (manager.selected instanceof GUICraftingOutput) {
                    GUICraftingOutput gco = (GUICraftingOutput) manager.selected;
                    if (gco.outputItem != null) {
                        for (int i = 0; i < gco.outputNum; i++) {
                            ItemSlot.addToInventory(gco.outputItem);
                        }
                        for (ItemSlot is : gco.craftingSlots) {
                            if (!is.isEmpty()) {
                                is.removeItem();
                            }
                        }
                    }
                }
            }
            if (Input.mouseJustPressed(1)) {
                if (manager.selected instanceof GUIInventorySquare) {
                    if (ItemSlot.GRABBED.isEmpty()) {
                        dragSource = ((GUIInventorySquare) manager.selected).itemSlot;
                        dragSource.moveItemsTo(ItemSlot.GRABBED, ceil(dragSource.count() / 2.));
                    } else {
                        ItemSlot newSlot = ((GUIInventorySquare) manager.selected).itemSlot;
                        ItemSlot.GRABBED.moveItemsTo(newSlot, 1);
                    }
                }
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
