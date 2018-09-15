package game.abilities;

import definitions.BlockType;
import engine.Behavior;
import game.Player;
import game.abilities.Ability.InstantAbility;
import game.items.ItemSlot;
import world.Raycast;

public class BlockPlaceAbility extends InstantAbility {

    public final Player player = user.get(Player.class);

    public final BlockType blockType;

    public BlockPlaceAbility(Behavior user, BlockType blockType) {
        super(user);
        this.blockType = blockType;
    }

    @Override
    public void use() {
        Raycast.RaycastHit block = player.lastEmpty();
        if (block != null) {
            //(isMainHand ? ItemSlot.MAIN_HAND : ItemSlot.OFF_HAND).removeItem();
            ItemSlot.MAIN_HAND.removeItem();
            player.physics.world.setBlock(block.hitPos, blockType);
        }
    }
}
