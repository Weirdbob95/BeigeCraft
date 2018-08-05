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
    SAND;

    public static Vec2d spritesheetPos(BlockType bt, Vec3d dir) {
        switch (bt) {
            case GRASS:
                if (dir.z > 0) {
                    return new Vec2d(0, 0);
                } else if (dir.z < 0) {
                    return new Vec2d(16, 0);
                } else {
                    return new Vec2d(48, 0);
                }
            case DIRT:
                return new Vec2d(16, 0);
            case STONE:
                return new Vec2d(32, 0);
            case WOOD:
                return new Vec2d(64, 0);
            case LOG:
                if (dir.z == 0) {
                    return new Vec2d(0, 16);
                } else {
                    return new Vec2d(16, 16);
                }
            case LEAVES:
                return new Vec2d(32, 16);
            case SAND:
                return new Vec2d(48, 16);
            default:
                throw new RuntimeException("Unknown BlockType");
        }
    }
}
