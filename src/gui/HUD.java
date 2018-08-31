package gui;

import engine.Input;
import static game.Settings.SHOW_DEBUG_HUD;
import game.inventory.ItemSlot;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.biomes.Biome;

public class HUD extends GUIItem {

    private final GUIText position;
    private final GUIText biome;
    private final GUIInventorySquare mainHand, offHand;

    public HUD() {
        GUISprite crosshares = new GUISprite("crosshares.png");
        crosshares.color = new Vec4d(1, 1, 1, .5);

        position = new GUIText(null);
        position.offset = new Vec2d(-790, 425);
        position.centered = false;

        biome = new GUIText(null);
        biome.offset = new Vec2d(-790, 390);
        biome.centered = false;

        mainHand = new GUIInventorySquare(ItemSlot.MAIN_HAND);
        mainHand.offset = new Vec2d(-50, -400);

        offHand = new GUIInventorySquare(ItemSlot.OFF_HAND);
        offHand.offset = new Vec2d(50, -400);

        add(crosshares, position, biome, mainHand, offHand);
    }

    @Override
    protected void render() {
        mainHand.color = new Vec4d(.6, .6, .6, Input.mouseDown(0) ? .8 : .5);
        mainHand.itemSlot = ItemSlot.MAIN_HAND;
        offHand.color = new Vec4d(.6, .6, .6, Input.mouseDown(1) ? .8 : .5);
        offHand.itemSlot = ItemSlot.OFF_HAND;
    }

    public void setBiome(Biome b) {
        if (SHOW_DEBUG_HUD) {
            biome.setText(b.toString());
        } else {
            biome.setText(null);
        }
    }

    public void setPos(Vec3d pos) {
        if (SHOW_DEBUG_HUD) {
            position.setText(String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z));
        } else {
            position.setText(null);
        }
    }
}
