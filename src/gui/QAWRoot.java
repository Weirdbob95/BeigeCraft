package gui;

import engine.Input;
import game.inventory.ItemSlot;
import graphics.Graphics;
import util.MathUtils;
import static util.MathUtils.mod;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class QAWRoot extends GUIRoot {

    private static final int MAX_DELTA = 100;
    private static final int MIN_DELTA = 50;

    private final GUIInventorySquare[] squares = new GUIInventorySquare[8];
    private GUIInventorySquare selected;
    private Vec2d mouseDelta;

    public QAWRoot(GUIManager manager) {
        super(manager);

        for (int i = 0; i < 8; i++) {
            GUIInventorySquare s = new GUIInventorySquare(ItemSlot.QAW[i]);
            s.offset = MathUtils.rotate(new Vec2d(120, 0), Math.PI / 4 * i);
            s.color = new Vec4d(.3, .3, .3, 1);
            squares[i] = s;
            add(s);
        }
    }

    @Override
    protected boolean freezeMovement() {
        return false;
    }

    @Override
    protected void open() {
        mouseDelta = new Vec2d(0, 0);
    }

    @Override
    protected void render() {
        Graphics.drawCircle(center(), 200, color);
        mouseDelta = mouseDelta.add(Input.mouseDelta().mul(new Vec2d(1, -1)));
        if (mouseDelta.length() > MAX_DELTA) {
            mouseDelta = mouseDelta.normalize().mul(MAX_DELTA);
        }
        GUIInventorySquare newSelected = null;
        if (mouseDelta.length() >= MIN_DELTA) {
            newSelected = squares[(int) mod(MathUtils.direction(mouseDelta) * 4 / Math.PI + .5, 8)];
        }
        if (newSelected != selected) {
            if (selected != null) {
                selected.color = new Vec4d(.3, .3, .3, 1);
            }
            if (newSelected != null) {
                newSelected.color = new Vec4d(.5, .5, .5, 1);
            }
            selected = newSelected;
        }
        if (selected != null) {
            if (Input.mouseJustPressed(0)) {
                if (ItemSlot.MAIN_HAND == selected.itemSlot) {
                    ItemSlot.MAIN_HAND = null;
                } else {
                    ItemSlot.MAIN_HAND = selected.itemSlot;
                }
            }
            if (Input.mouseJustPressed(1)) {
                if (ItemSlot.OFF_HAND == selected.itemSlot) {
                    ItemSlot.OFF_HAND = null;
                } else {
                    ItemSlot.OFF_HAND = selected.itemSlot;
                }
            }
        }
    }

    @Override
    protected boolean showMouse() {
        return false;
    }
}
