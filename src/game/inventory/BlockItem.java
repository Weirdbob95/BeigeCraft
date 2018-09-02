package game.inventory;

import game.Player;
import definitions.BlockType;
import world.Raycast.RaycastHit;

public class BlockItem extends UsableItem {

    private final BlockType blockType;

    public BlockItem(BlockType blockType) {
        this.blockType = blockType;
    }

    @Override
    public void useItemPress(Player player, boolean isMainHand) {
        RaycastHit block = player.lastEmpty();
        if (block != null) {
            (isMainHand ? ItemSlot.MAIN_HAND : ItemSlot.OFF_HAND).removeItem();
            player.physics.world.setBlock(block.hitPos, blockType);
        }
    }
}
