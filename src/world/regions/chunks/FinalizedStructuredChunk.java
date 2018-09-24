package world.regions.chunks;

import java.util.HashSet;
import java.util.Set;
import util.math.Vec3d;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
import world.regions.RegionPos;
import world.structures.Structure;

public class FinalizedStructuredChunk extends AbstractChunk {

    private final Set<Structure> structures = new HashSet();

    public FinalizedStructuredChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    public void constructIn(ConstructedChunk cc) {
        Vec3d translation = new Vec3d(-(cc.pos.x - pos.x) * CHUNK_SIZE, -(cc.pos.y - pos.y) * CHUNK_SIZE, 0);
        for (Structure s : structures) {
            s.blocks.copyTo(cc.blockStorage, s.x + (int) translation.x, s.y + (int) translation.y, s.z);
            for (TerrainObjectInstance toi : s.terrainObjects) {
                for (Vec3d v : toi.getOccupancy()) {
                    v = v.add(translation);
                    if (v.x >= 0 && v.x < CHUNK_SIZE && v.y >= 0 && v.y < CHUNK_SIZE) {
                        cc.blockStorage.set((int) v.x, (int) v.y, (int) v.z, null);
                        cc.terrainObjectOccupancyMap.put(v, toi);
                    }
                }
                if (toi.chunkPos.equals(cc.pos)) {
                    cc.terrainObjects.add(toi);
                }
            }
        }
    }

    @Override
    protected void generate() {
        structures.addAll(world.getChunk(StructuredChunk.class, pos).structures);
        structures.removeIf(s -> {
            for (RegionPos rp : pos.nearby(1)) {
                StructuredChunk structuredChunk = world.getChunk(StructuredChunk.class, rp);
                for (Structure s2 : structuredChunk.structures) {
                    if (s2.priority > s.priority && s.intersects(s2)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }
}
