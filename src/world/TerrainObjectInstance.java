package world;

import definitions.TerrainObjectType;
import java.util.LinkedList;
import java.util.List;
import util.math.Vec3d;
import util.math.Vec4d;

public class TerrainObjectInstance {

    public final TerrainObjectType type;
    public final ChunkPos chunkPos;
    public final int x, y, z;

    public TerrainObjectInstance(TerrainObjectType type, ChunkPos chunkPos, int x, int y, int z) {
        this.type = type;
        this.chunkPos = chunkPos;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3d center() {
        return new Vec3d(x, y, z).add(type.getSize().div(2));
    }

    public List<Vec3d> getOccupancy() {
        List<Vec3d> r = new LinkedList();
        for (int i = 0; i < type.getSize().x; i++) {
            for (int j = 0; j < type.getSize().y; j++) {
                for (int k = 0; k < type.getSize().z; k++) {
                    r.add(new Vec3d(x + i, y + j, z + k));
                }
            }
        }
        return r;
    }

    public void render(Vec3d pos) {
        type.getModel().render(pos.add(center()), 0, 0, 1 / 16., type.getModel().originalSize().div(2), new Vec4d(1, 1, 1, 1));
    }
}
