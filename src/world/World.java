package world;

import java.util.HashMap;
import java.util.Map;
import static util.MathUtils.mod;
import util.Noise;
import util.vectors.Vec3d;

public class World {

    public static final int CHUNK_SIZE = 32;

    private final Map<ChunkPos, PlannedChunk> plannedChunks = new HashMap();
    private final Map<ChunkPos, ConstructedChunk> constructedChunks = new HashMap();

    private final Noise noise = new Noise(Math.random() * 1e6);

    public BlockType getBlock(Vec3d pos) {
        ChunkPos cp = new ChunkPos((int) Math.floor(pos.x / CHUNK_SIZE), (int) Math.floor(pos.y / CHUNK_SIZE));
        return getConstructedChunk(cp).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z));
    }

    public ConstructedChunk getConstructedChunk(ChunkPos pos) {
        if (!constructedChunks.containsKey(pos)) {
            constructedChunks.putIfAbsent(pos, generateConstructedChunk(pos));
        }
        return constructedChunks.get(pos);
    }

    public PlannedChunk getPlannedChunk(ChunkPos pos) {
        if (!plannedChunks.containsKey(pos)) {
            plannedChunks.putIfAbsent(pos, generatePlannedChunk(pos));
        }
        return plannedChunks.get(pos);
    }

    //
    // -------------------- WORLD GENERATION --------------------
    //
    private ConstructedChunk generateConstructedChunk(ChunkPos pos) {
        PlannedChunk pc = getPlannedChunk(pos);
        ConstructedChunk c = new ConstructedChunk();
        c.fillWith(BlockType.GRASS, (int) pc.elevation);
        c.fillWith(BlockType.DIRT, (int) pc.elevation - 1);
        c.fillWith(BlockType.STONE, (int) pc.elevation - 3);
        return c;
    }

    private PlannedChunk generatePlannedChunk(ChunkPos pos) {
        PlannedChunk c = new PlannedChunk();
        c.elevation = 50 * noise.perlin(pos.x, pos.y, .1);
        return c;
    }
}
