package definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.YAMLObject;
import util.vectors.Vec2d;
import util.vectors.Vec3d;

public class BlockType {

    private static final Map<String, BlockType> BLOCK_MAP = new HashMap();
    private static final ArrayList<BlockType> BLOCK_LIST = new ArrayList();

    static {
        YAMLObject root = YAMLObject.parse("definitions/block_definitions.txt");
        BLOCK_LIST.add(null);
        for (YAMLObject block : root.contents) {
            BlockType bt = new BlockType();
            bt.id = BLOCK_LIST.size();
            bt.gameName = block.name;
            processParams(bt, root, block.contents);
            BLOCK_MAP.put(bt.gameName, bt);
            BLOCK_LIST.add(bt);
        }
    }

    private static void processParams(BlockType bt, YAMLObject root, List<YAMLObject> params) {
        for (YAMLObject param : params) {
            switch (param.name) {
                case "include":
                    processParams(bt, root, root.getSubObject(param.value).contents);
                    break;
                case "name":
                    bt.displayName = param.value;
                    break;
                case "texture":
                    for (YAMLObject texParam : param.contents) {
                        switch (texParam.name) {
                            case "type":
                                bt.textureType = texParam.value;
                                break;
                            case "pos":
                                bt.pos = texParam.valueToVec2d();
                                break;
                            case "side":
                                bt.side = texParam.valueToVec2d();
                                break;
                            case "top":
                                bt.top = texParam.valueToVec2d();
                                break;
                            case "bottom":
                                bt.bottom = texParam.valueToVec2d();
                                break;
                        }
                    }
                    break;
                case "durability":
                    bt.durability = param.valueToDouble();
                    break;
                case "tool":
                    bt.tool = param.value;
                    break;
                case "tool_level":
                    bt.toolLevel = param.valueToInt();
                    break;
                case "on_break":
                    switch (param.value) {
                        case "none":
                            bt.onBreak = null;
                            break;
                        case "self":
                            bt.onBreak = bt;
                            break;
                        default:
                            bt.onBreak = getBlock(param.value);
                            break;
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
    private String textureType;
    private Vec2d pos, side, top, bottom;
    private double durability;
    private String tool;
    private int toolLevel;
    private BlockType onBreak;

    private BlockType() {
    }

    public String displayName() {
        return displayName;
    }

    public String gameName() {
        return gameName;
    }

    public static List<BlockType> getAllBlocks() {
        return BLOCK_LIST.subList(1, BLOCK_LIST.size());
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

    public BlockType getOnBreak() {
        return onBreak;
    }

    public int getTexID(Vec3d dir) {
        switch (textureType) {
            case "uniform":
                return (int) pos.x + (int) pos.y * 256;
            case "diff_top_bot":
                if (dir.z > 0) {
                    return (int) top.x + (int) top.y * 256;
                } else if (dir.z < 0) {
                    return (int) bottom.x + (int) bottom.y * 256;
                } else {
                    return (int) side.x + (int) side.y * 256;
                }
            default:
                throw new RuntimeException("Unknown texture type: " + textureType);
        }
    }

    public int id() {
        return id;
    }
}
