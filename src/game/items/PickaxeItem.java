package game.items;

import game.Player;
import java.util.ArrayList;
import java.util.List;
import static util.MathUtils.ceil;
import util.vectors.Vec2d;
import util.vectors.Vec3d;
import world.BlockType;
import world.Raycast;

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

    @Override
    public void useItemHold(Player player, boolean isMainHand, double dt) {
        // Break block
        player.breakingBlocks = true;
        Raycast.RaycastHit block = player.firstSolid();
        if (block != null) {
            int handSize = ceil(player.PLAYER_SCALE);
            Vec3d origin = block.hitPos
                    //.add(block.hitDir.mul(handSize / 2.))
                    .sub(new Vec3d(1, 1, 1).mul(.5 * (handSize - 1)));
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
                        ItemSlot.addToInventory(new BlockItem(player.physics.world.getBlock(v).getOnBreak()));
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
