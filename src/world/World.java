package world;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static util.MathUtils.mod;
import util.Noise;
import util.vectors.Vec3d;

public class World {

    public static final int CHUNK_SIZE = 16;
    public static final int RENDER_DISTANCE = 32;

    private final Map<ChunkPos, ConstructedChunk> constructedChunks = new ConcurrentHashMap();
    private final Map<ChunkPos, PlannedChunk> plannedChunks = new ConcurrentHashMap();
    private final Map<ChunkPos, RenderedChunk> renderedChunks = new ConcurrentHashMap();

    private final Noise noise = new Noise(Math.random() * 1e6);

    public void deleteRenderedChunk(ChunkPos pos) {
        renderedChunks.remove(pos);
    }

    public BlockType getBlock(Vec3d pos) {
        return getConstructedChunk(getChunkPos(pos)).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z));
    }

    public ChunkPos getChunkPos(Vec3d pos) {
        return new ChunkPos((int) Math.floor(pos.x / CHUNK_SIZE), (int) Math.floor(pos.y / CHUNK_SIZE));
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

    public RenderedChunk getRenderedChunk(ChunkPos pos) {
        if (!renderedChunks.containsKey(pos)) {
            renderedChunks.putIfAbsent(pos, generateRenderedChunk(pos));
        }
        return renderedChunks.get(pos);
    }

    public boolean hasConstructedChunk(ChunkPos pos) {
        return constructedChunks.containsKey(pos);
    }

    public boolean hasPlannedChunk(ChunkPos pos) {
        return plannedChunks.containsKey(pos);
    }

    public boolean hasRenderedChunk(ChunkPos pos) {
        return renderedChunks.containsKey(pos);
    }

    //
    // -------------------- WORLD GENERATION --------------------
    //
    private ConstructedChunk generateConstructedChunk(ChunkPos pos) {
        PlannedChunk pc = getPlannedChunk(pos);
        ConstructedChunk c = new ConstructedChunk();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                int elevation = (int) heightmapPrecise(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE);
                c.blockStorage.setRangeInfinite(x, y, elevation, BlockType.GRASS);
                c.blockStorage.setRangeInfinite(x, y, elevation - 1, BlockType.DIRT);
                c.blockStorage.setRangeInfinite(x, y, elevation - 3, BlockType.STONE);
            }
        }
//        c.fillWith(BlockType.GRASS, (int) pc.elevation);
//        c.fillWith(BlockType.DIRT, (int) pc.elevation - 1);
//        c.fillWith(BlockType.STONE, (int) pc.elevation - 3);
        return c;
    }

    private PlannedChunk generatePlannedChunk(ChunkPos pos) {
        PlannedChunk c = new PlannedChunk();
        c.elevation = heightmapEstimate(pos.x * CHUNK_SIZE, pos.y * CHUNK_SIZE);
        return c;
    }

    private RenderedChunk generateRenderedChunk(ChunkPos pos) {
        RenderedChunk r = new RenderedChunk();
        r.generateAtPos(this, pos);
        r.create();
        return r;
    }

    private double heightmapEstimate(int x, int y) {
        return 100 * noise.perlin(x, y, .003);
    }

    private double heightmapPrecise(int x, int y) {
        return 100 * noise.perlin(x, y, .003)
                + 20 * noise.perlin(x, y, .015)
                + 4 * noise.perlin(x, y, .075);
    }
}
