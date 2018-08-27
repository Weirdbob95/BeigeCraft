package game.items;

import game.Player;
import graphics.Sprite;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public abstract class Item {

    public abstract String description();

    public abstract int maxStackSize();

    public abstract String name();

    public abstract void renderGUI(Vec2d pos);

    protected static void renderSprite(String fileName, Vec2d pos) {
        Sprite.load(fileName).draw2d(pos, 0, 4, new Vec4d(1, 1, 1, 1));
    }

    public void useItemHold(Player player, boolean isMainHand, double dt) {
    }

    public void useItemPress(Player player, boolean isMainHand) {
    }
}
