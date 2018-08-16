package game.items;

import graphics.BlockGUI;
import util.vectors.Vec2d;
import world.BlockType;

public class BlockItem extends Item {

    public final BlockType blockType;

    public BlockItem(BlockType blockType) {
        this.blockType = blockType;
    }

    @Override
    public String description() {
        return "A block of some type! More info to come later.";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockItem other = (BlockItem) obj;
        if (this.blockType != other.blockType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public int maxStackSize() {
        return 64;
    }

    @Override
    public String name() {
        return blockType.toString();
    }

    @Override
    public void renderGUI(Vec2d pos) {
        BlockGUI.load(blockType).draw(pos, 20);
    }
}
