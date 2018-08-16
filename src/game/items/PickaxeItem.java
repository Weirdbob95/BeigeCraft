package game.items;

import util.vectors.Vec2d;

public class PickaxeItem extends Item {

    @Override
    public String description() {
        return "This tool is great at mining blocks, especially stone.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Pickaxe";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_pickaxe.png", pos);
    }
}
