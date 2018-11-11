package world;

import definitions.TerrainObjectType;
import java.util.LinkedList;
import java.util.List;
import util.math.Quaternion;
import util.math.Vec3d;
import util.math.Vec4d;
import world.regions.RegionPos;

public class TerrainObjectInstance {

    public final TerrainObjectType type;
    public final RegionPos chunkPos;
    public final int x, y, z;
    public final int rotation;

    public TerrainObjectInstance(TerrainObjectType type, RegionPos chunkPos, int x, int y, int z) {
        this(type, chunkPos, x, y, z, 0);
    }

    public TerrainObjectInstance(TerrainObjectType type, RegionPos chunkPos, int x, int y, int z, int rotation) {
        this.type = type;
        this.chunkPos = chunkPos;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    public Vec3d center() {
        return new Vec3d(x, y, z).add(type.getSize().div(2));
    }

    public List<Vec3d> getOccupancy() {
        List<Vec3d> r = new LinkedList();
        for (int i = 0; i < type.getSize().x; i++) {
            for (int j = 0; j < type.getSize().y; j++) {
                for (int k = 0; k < type.getSize().z; k++) {
                    switch (rotation) {
                        case 0:
                            r.add(new Vec3d(x + i, y + j, z + k));
                            break;
                        case 1:
                            r.add(new Vec3d(x - j, y + i, z + k));
                            break;
                        case 2:
                            r.add(new Vec3d(x - i, y - j, z + k));
                            break;
                        case 3:
                            r.add(new Vec3d(x + j, y - i, z + k));
                            break;
                    }
                }
            }
        }
        return r;
    }

    public void render(Vec3d pos) {
        //type.getModel().render(pos.add(center()), rotation * Math.PI / 2, 0, 1 / 16., type.getModel().originalSize().div(2), new Vec4d(1, 1, 1, 1));
        Quaternion quat = Quaternion.fromEulerAngles(rotation * Math.PI / 2, 0, 0);
        type.getModel().render(pos.add(new Vec3d(x + .5, y + .5, z + .5)), quat, 1 / 16., new Vec3d(8, 8, 8), new Vec4d(1, 1, 1, 1));
    }
}
