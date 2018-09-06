package game.inventory;

import definitions.BlockType;
import static definitions.ItemType.getItemByBlock;
import game.Player;
import java.util.ArrayList;
import java.util.List;
import util.vectors.Vec3d;
import world.Raycast;

public class PickaxeItem extends UsableItem {

    @Override
    public void useItemHold(Player player, boolean isMainHand, double dt) {
        // Break block
        player.breakingBlocks = true;
        Raycast.RaycastHit block = player.firstSolid();
        if (block != null) {
            int handSize = 2;
            Vec3d origin = block.hitPos.sub(new Vec3d(1, 1, 1).mul(.5 * (handSize - 1)));
            List<Vec3d> targets = new ArrayList();
            for (int x = 0; x < handSize; x++) {
                for (int y = 0; y < handSize; y++) {
                    for (int z = 0; z < handSize; z++) {
                        if (player.physics.world.getBlock(origin.add(new Vec3d(x, y, z))) != null) {
                            targets.add(origin.add(new Vec3d(x, y, z)).floor());
                        }
                    }
                }
            }
            for (Vec3d v : new ArrayList<>(player.blocksToBreak.keySet())) {
                if (!targets.contains(v)) {
                    player.blocksToBreak.remove(v);
                }
            }
            for (Vec3d v : targets) {
                player.blocksToBreak.putIfAbsent(v, 0.);
                player.blocksToBreak.put(v, player.blocksToBreak.get(v) + dt * 8 / targets.size());
                if (player.blocksToBreak.get(v) > 1) {
                    BlockType bt = player.physics.world.getBlock(v).getOnBreak();
                    if (bt != null) {
                        ItemSlot.addToInventory(getItemByBlock(player.physics.world.getBlock(v).getOnBreak()));
                    }
                    player.physics.world.setBlock(v, null);
                    player.blocksToBreak.remove(v);
                }
            }
        } else {
            player.blocksToBreak.clear();
        }
    }
}
