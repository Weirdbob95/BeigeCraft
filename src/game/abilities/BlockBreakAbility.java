package game.abilities;

import definitions.BlockType;
import static definitions.ItemType.getItemByBlock;
import engine.Behavior;
import game.Player;
import game.abilities.Ability.ContinuousAbility;
import game.items.ItemSlot;
import java.util.ArrayList;
import java.util.List;
import util.math.Vec3d;
import world.Raycast;

public class BlockBreakAbility extends ContinuousAbility {

    public final Player player = user.get(Player.class);

    public int handSize = 2;

    public BlockBreakAbility(Behavior user) {
        super(user);
    }

    @Override
    public void use(double dt) {
        player.breakingBlocks = true;
        Raycast.RaycastHit block = player.firstSolid();
        if (block != null) {
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
