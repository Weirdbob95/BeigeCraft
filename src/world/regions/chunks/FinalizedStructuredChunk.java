package world.regions.chunks;

import java.util.HashSet;
import java.util.Set;
import util.math.Vec3d;
import world.regions.RegionPos;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
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
        structures.addAll(world.structuredChunks.get(pos).structures);
        structures.removeIf(s -> {
            int rangeToCheck = 1;
            for (int i = -rangeToCheck; i <= rangeToCheck; i++) {
                for (int j = -rangeToCheck; j <= rangeToCheck; j++) {
                    StructuredChunk structuredChunk = world.structuredChunks.get(new RegionPos(pos.x + i, pos.y + j));
                    for (Structure s2 : structuredChunk.structures) {
                        if (s2.priority > s.priority && s.intersects(s2)) {
                            //System.out.println("rejected type " + s.getClass());
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }
}
