package game.items;

import game.Player;
import util.vectors.Vec2d;
import world.Raycast;

public class WaterBucketItem extends Item {

    @Override
    public String description() {
        return "This bucket can store, transport, and place water.";
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public String name() {
        return "Water Bucket";
    }

    @Override
    public void renderGUI(Vec2d pos) {
        renderSprite("item_waterbucket.png", pos);
    }

    @Override
    public void useItemPress(Player player, boolean isMainHand) {
        Raycast.RaycastHit block = player.lastEmpty();
        if (block != null) {
            player.physics.world.waterManager.addWater(block.hitPos, 1);
        }
    }
}
