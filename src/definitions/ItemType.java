package definitions;

import game.inventory.BlockItem;
import game.inventory.PickaxeItem;
import game.inventory.SwordItem;
import game.inventory.UsableItem;
import game.inventory.WandItem;
import game.inventory.WaterBucketItem;
import graphics.BlockGUI;
import graphics.Sprite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.YAMLObject;
import util.vectors.Vec2d;
import util.vectors.Vec4d;

public class ItemType {

    private static final Map<String, ItemType> ITEM_MAP = new HashMap();
    private static final ArrayList<ItemType> ITEM_LIST = new ArrayList();

    static {
        YAMLObject root = YAMLObject.parse("definitions/item_definitions.txt");
        ITEM_MAP.put("none", null);
        ITEM_LIST.add(null);
        for (BlockType bt : BlockType.getAllBlocks()) {
            ItemType it = new ItemType();
            it.id = ITEM_LIST.size();
            it.gameName = "block." + bt.gameName();
            it.maxStackSize = 64;
            it.useType = "block";
            it.blockType = bt;
            ITEM_MAP.put(it.gameName, it);
            ITEM_LIST.add(it);
        }
        for (YAMLObject item : root.contents) {
            ItemType it = new ItemType();
            it.id = ITEM_LIST.size();
            it.gameName = item.name;
            processParams(it, root, item.contents);
            ITEM_MAP.put(item.name, it);
            ITEM_LIST.add(it);
        }
    }

    private static void processParams(ItemType it, YAMLObject root, List<YAMLObject> params) {
        for (YAMLObject param : params) {
            switch (param.name) {
                case "include":
                    processParams(it, root, root.getSubObject(param.value).contents);
                    break;
                case "name":
                    it.displayName = param.value;
                    break;
                case "texture":
                    if (param.value.equals("none")) {
                        it.texture = null;
                    } else {
                        it.texture = Sprite.load(param.value);
                    }
                    break;
                case "max_stack_size":
                    it.maxStackSize = param.valueToInt();
                    break;
                case "on_use":
                    for (YAMLObject useParam : param.contents) {
                        switch (useParam.name) {
                            case "type":
                                it.useType = useParam.value;
                                break;
                            case "tool_type":
                                it.toolType = useParam.value;
                                break;
                            case "tool_level":
                                it.toolLevel = useParam.valueToInt();
                                break;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown parameter: " + param.name);
            }
        }
    }

    private int id;
    private String gameName;
    private String displayName;
    private Sprite texture;
    private int maxStackSize;
    private String useType;
    private String toolType;
    private int toolLevel;
    private BlockType blockType;

    private ItemType() {
    }

    public String displayName() {
        return displayName;
    }

    public String gameName() {
        return gameName;
    }

    public static ItemType getItem(String name) {
        if (!ITEM_MAP.containsKey(name)) {
            System.out.println(ITEM_MAP);
            throw new RuntimeException("Unknown item type: " + name);
        }
        return ITEM_MAP.get(name);
    }

    public static ItemType getItemByBlock(BlockType bt) {
        for (ItemType it : ITEM_LIST) {
            if (it != null && it.blockType == bt) {
                return it;
            }
        }
        throw new RuntimeException("Could not find item corresponding to block type: " + bt.gameName());
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public void renderGUI(Vec2d pos) {
        if (texture != null) {
            texture.draw2d(pos, 0, 4, new Vec4d(1, 1, 1, 1));
        }
        if (blockType != null) {
            BlockGUI.load(blockType).draw(pos, 24);
        }
    }

    public UsableItem use() {
        switch (useType) {
            case "none":
                return new UsableItem();
            case "tool":
                return new PickaxeItem();
            case "sword":
                return new SwordItem();
            case "wand":
                return new WandItem();
            case "water_bucket":
                return new WaterBucketItem();
            case "block":
                return new BlockItem(blockType);
            default:
                throw new RuntimeException("Unknown use type: " + useType);
        }
    }
}
