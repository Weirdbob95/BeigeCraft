package world;

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
            bt.name = block.name;
            processParams(bt, root, block.contents);
            BLOCK_MAP.put(block.name, bt);
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
                    bt.name = param.value;
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
                            bt.onBreak = get(param.value);
                            break;
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown parameter: " + param.name);
            }
        }
    }

    private int id;
    private String name;
    private String textureType;
    private Vec2d pos, side, top, bottom;
    //private Map<Vec3d, Integer> texIdMap;
    private double durability;
    private String tool;
    private int toolLevel;
    private BlockType onBreak;

    public static BlockType getByID(int id) {
        return BLOCK_LIST.get(id);
    }

    public static BlockType get(String name) {
        if (!BLOCK_MAP.containsKey(name)) {
            throw new RuntimeException("Unknown block type: " + name);
        }
        return BLOCK_MAP.get(name);
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
        //return texIdMap.get(dir);
    }

    public int id() {
        return id;
    }
}

//public enum BlockType {
//
//    GRASS,
//    DIRT,
//    STONE,
//    WOOD,
//    LOG,
//    LEAVES,
//    SAND,
//    SNOWY_GRASS,
//    TUNDRA_GRASS,
//    CACTUS,
//    IRON_ORE,
//    LAVA;
//
//    public static final BlockType[] VALUES = BlockType.values();
//
//    public static int getTexID(BlockType bt, Vec3d dir) {
//        Vec2d pos = spritesheetPos(bt, dir);
//        return (int) pos.x + (int) pos.y * 256;
//    }
//
//    private static Vec2d spritesheetPos(BlockType bt, Vec3d dir) {
//        switch (bt) {
//            case GRASS:
//                if (dir.z > 0) {
//                    return new Vec2d(0, 0);
//                } else if (dir.z < 0) {
//                    return new Vec2d(1, 0);
//                } else {
//                    return new Vec2d(3, 0);
//                }
//            case DIRT:
//                return new Vec2d(1, 0);
//            case STONE:
//                return new Vec2d(2, 0);
//            case WOOD:
//                return new Vec2d(4, 0);
//            case LOG:
//                if (dir.z == 0) {
//                    return new Vec2d(0, 1);
//                } else {
//                    return new Vec2d(1, 1);
//                }
//            case LEAVES:
//                return new Vec2d(2, 1);
//            case SAND:
//                return new Vec2d(3, 1);
//            case SNOWY_GRASS:
//                if (dir.z > 0) {
//                    return new Vec2d(0, 2);
//                } else if (dir.z < 0) {
//                    return new Vec2d(1, 0);
//                } else {
//                    return new Vec2d(1, 2);
//                }
//            case TUNDRA_GRASS:
//                if (dir.z > 0) {
//                    return new Vec2d(2, 2);
//                } else if (dir.z < 0) {
//                    return new Vec2d(1, 0);
//                } else {
//                    return new Vec2d(3, 2);
//                }
//            case CACTUS:
//                if (dir.z == 0) {
//                    return new Vec2d(4, 1);
//                } else {
//                    return new Vec2d(4, 2);
//                }
//            case IRON_ORE:
//                return new Vec2d(0, 3);
//            case LAVA:
//                return new Vec2d(1, 3);
//            default:
//                throw new RuntimeException("Unknown BlockType: " + bt);
//        }
//    }
//}
