package definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.Resources;

public class Loader {

    private static final List<BlockType> BLOCK_LIST = new ArrayList();
    private static final Map<String, BlockType> BLOCK_MAP = new HashMap();

    private static final List<ItemType> ITEM_LIST = new ArrayList();
    private static final Map<String, ItemType> ITEM_MAP = new HashMap();

    static {
        BLOCK_LIST.add(null);
        ITEM_LIST.add(null);

        for (BlockType bt : Resources.loadYamlFile("definitions/block_definitions.yml", BlockType.class)) {
            bt.id = BLOCK_LIST.size();
            BLOCK_LIST.add(bt);
            BLOCK_MAP.put(bt.gameName, bt);

            ItemType it = new ItemType();
            it.gameName = "block." + bt.gameName;
            it.displayName = bt.displayName;
            it.blockType = bt;

            it.id = ITEM_LIST.size();
            ITEM_LIST.add(it);
            ITEM_MAP.put(it.gameName, it);
        }
        for (ItemType it : Resources.loadYamlFile("definitions/item_definitions.yml", ItemType.class)) {
            it.id = ITEM_LIST.size();
            ITEM_LIST.add(it);
            ITEM_MAP.put(it.gameName, it);
        }
    }

    public static BlockType getBlock(String name) {
        if (!BLOCK_MAP.containsKey(name)) {
            throw new RuntimeException("Unknown block type: " + name);
        }
        return BLOCK_MAP.get(name);
    }

    public static BlockType getBlockByID(int id) {
        return BLOCK_LIST.get(id);
    }

    public static ItemType getItem(String name) {
        if (!ITEM_MAP.containsKey(name)) {
            System.out.println(ITEM_MAP);
            throw new RuntimeException("Unknown item type: " + name);
        }
        return ITEM_MAP.get(name);
    }

    public static ItemType getItemByBlock(BlockType bt) {
        for (ItemType it : ITEM_MAP.values()) {
            if (it != null && it.blockType == bt) {
                return it;
            }
        }
        throw new RuntimeException("Could not find item corresponding to block type: " + bt.gameName);
    }
}
