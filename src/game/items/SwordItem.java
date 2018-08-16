package game.items;

import util.vectors.Vec2d;

public class SwordItem extends Item {

    @Override
    public String description() {
        return "This melee weapon is common and easy to use.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Sword";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_sword.png", pos);
    }
}
