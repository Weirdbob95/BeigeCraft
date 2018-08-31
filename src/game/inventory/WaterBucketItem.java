package game.inventory;

import game.Player;
import world.Raycast;

public class WaterBucketItem extends UsableItem {

    @Override
    public void useItemPress(Player player, boolean isMainHand) {
        Raycast.RaycastHit block = player.lastEmpty();
        if (block != null) {
            player.physics.world.waterManager.addWater(block.hitPos, 1);
        }
    }
}
