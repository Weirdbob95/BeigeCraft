package game.items;

import util.vectors.Vec2d;

public class WandItem extends Item {

    @Override
    public String description() {
        return "This item can cast powerful magical spells.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Wand";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_wand.png", pos);
    }
}
