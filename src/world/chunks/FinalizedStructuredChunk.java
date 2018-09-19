package world.chunks;

import java.util.HashSet;
import java.util.Set;
import util.math.Vec3d;
import world.ChunkPos;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
import world.structures.Structure;

public class FinalizedStructuredChunk extends AbstractChunk {

    private final Set<Structure> structures = new HashSet();

    public FinalizedStructuredChunk(World world, ChunkPos pos) {
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
        StructuredChunk sc = world.structuredChunks.get(pos);
        structures.addAll(sc.structures);
        structures.removeIf(s -> {
            if (s.canBeOverwritten) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        StructuredChunk sc2 = world.structuredChunks.get(new ChunkPos(pos.x + i, pos.y + j));
                        for (Structure s2 : sc2.structures) {
                            if (s != s2 && s.intersects(s2)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }
}
