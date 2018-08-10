package world;

import util.vectors.Vec2d;
import util.vectors.Vec3d;

public enum BlockType {

    GRASS,
    DIRT,
    STONE,
    WOOD,
    LOG,
    LEAVES,
    SAND,
    SNOWY_GRASS,
    TUNDRA_GRASS,
    CACTUS,
    IRON_ORE;

    public static int getTexID(BlockType bt, Vec3d dir) {
        Vec2d pos = spritesheetPos(bt, dir);
        return (int) pos.x + (int) pos.y * 256;
    }

    private static Vec2d spritesheetPos(BlockType bt, Vec3d dir) {
        switch (bt) {
            case GRASS:
                if (dir.z > 0) {
                    return new Vec2d(0, 0);
                } else if (dir.z < 0) {
                    return new Vec2d(1, 0);
                } else {
                    return new Vec2d(3, 0);
                }
            case DIRT:
                return new Vec2d(1, 0);
            case STONE:
                return new Vec2d(2, 0);
            case WOOD:
                return new Vec2d(4, 0);
            case LOG:
                if (dir.z == 0) {
                    return new Vec2d(0, 1);
                } else {
                    return new Vec2d(1, 1);
                }
            case LEAVES:
                return new Vec2d(2, 1);
            case SAND:
                return new Vec2d(3, 1);
            case SNOWY_GRASS:
                if (dir.z > 0) {
                    return new Vec2d(0, 2);
                } else if (dir.z < 0) {
                    return new Vec2d(1, 0);
                } else {
                    return new Vec2d(1, 2);
                }
            case TUNDRA_GRASS:
                if (dir.z > 0) {
                    return new Vec2d(2, 2);
                } else if (dir.z < 0) {
                    return new Vec2d(1, 0);
                } else {
                    return new Vec2d(3, 2);
                }
            case CACTUS:
                if (dir.z == 0) {
                    return new Vec2d(4, 1);
                } else {
                    return new Vec2d(4, 2);
                }
            case IRON_ORE:
                return new Vec2d(0, 3);
            default:
                throw new RuntimeException("Unknown BlockType: " + bt);
        }
    }
}
