package world;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import static util.MathUtils.mod;
import util.Multithreader;
import util.Noise;
import util.vectors.Vec3d;

public class World {

    public static final int CHUNK_SIZE = 32;
    public static final int RENDER_DISTANCE = 4;

    public final ChunkMap<ConstructedChunk> constructedChunks = new ChunkMap<>(this::generateConstructedChunk);
    public final ChunkMap<PlannedChunk> plannedChunks = new ChunkMap<>(this::generatePlannedChunk);
    public final ChunkMap<PrerenderedChunk> prerenderedChunks = new ChunkMap<>(this::generatePrerenderedChunk, this::destroyPrerenderedChunk);
    public final ChunkMap<RenderedChunk> renderedChunks = new ChunkMap<>(this::generateRenderedChunk, this::destroyRenderedChunk);

    private final Noise noise = new Noise(Math.random() * 1e6);

    public BlockType getBlock(Vec3d pos) {
        return constructedChunks.get(getChunkPos(pos)).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z));
    }

    public ChunkPos getChunkPos(Vec3d pos) {
        return new ChunkPos((int) Math.floor(pos.x / CHUNK_SIZE), (int) Math.floor(pos.y / CHUNK_SIZE));
    }

    //
    // -------------------- WORLD GENERATION --------------------
    //
    private void destroyPrerenderedChunk(ChunkPos pos, PrerenderedChunk prc) {
        prc.destroy();
    }

    private void destroyRenderedChunk(ChunkPos pos, RenderedChunk rc) {
        rc.destroy();
        prerenderedChunks.get(pos);
    }

    private ConstructedChunk generateConstructedChunk(ChunkPos pos) {
        PlannedChunk pc = plannedChunks.get(pos);
        ConstructedChunk c = new ConstructedChunk();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                int elevation = (int) heightmapPrecise(x + pos.x * CHUNK_SIZE, y + pos.y * CHUNK_SIZE);
                c.blockStorage.setRangeInfinite(x, y, elevation, BlockType.GRASS);
                c.blockStorage.setRangeInfinite(x, y, elevation - 1, BlockType.DIRT);
                c.blockStorage.setRangeInfinite(x, y, elevation - 3, BlockType.STONE);
            }
        }
        return c;
    }

    private PlannedChunk generatePlannedChunk(ChunkPos pos) {
        PlannedChunk c = new PlannedChunk();
        c.elevation = heightmapEstimate(pos.x * CHUNK_SIZE, pos.y * CHUNK_SIZE);
        return c;
    }

    private PrerenderedChunk generatePrerenderedChunk(ChunkPos pos) {
        PrerenderedChunk prc = new PrerenderedChunk();
        prc.generateAtPos(this, pos);
        return prc;
    }

    private RenderedChunk generateRenderedChunk(ChunkPos pos) {
        RenderedChunk rc = new RenderedChunk();
        rc.generateAtPos(this, pos);
        prerenderedChunks.remove(pos);
        return rc;
    }

    private double heightmapEstimate(int x, int y) {
        return 100 * noise.perlin(x, y, .003)
                + 20 * noise.perlin(x, y, .015);
    }

    private double heightmapPrecise(int x, int y) {
        return 100 * noise.perlin(x, y, .003)
                + 20 * noise.perlin(x, y, .015)
                + 4 * noise.perlin(x, y, .075);
    }

    public static class ChunkMap<T> {

        private final Map<ChunkPos, T> chunks = new ConcurrentHashMap();
        private final Map<ChunkPos, Object> locks = new ConcurrentHashMap();
        public final Set<ChunkPos> border = Collections.newSetFromMap(new ConcurrentHashMap());
        private final Function<ChunkPos, T> generator;
        private final BiConsumer<ChunkPos, T> destructor;

        public ChunkMap(Function<ChunkPos, T> generator) {
            this.generator = generator;
            this.destructor = (cp, t) -> {
            };
        }

        public ChunkMap(Function<ChunkPos, T> generator, BiConsumer<ChunkPos, T> destructor) {
            this.generator = generator;
            this.destructor = destructor;
        }

        public boolean anyUnfinished() {
            for (T t : chunks.values()) {
                if (t.getClass().equals(Object.class)) {
                    return true;
                }
            }
            return false;
        }

        public Set<ChunkPos> getAllKeys() {
            return chunks.keySet();
        }

        public T get(ChunkPos pos) {
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                if (!chunks.containsKey(pos)) {
                    chunks.put(pos, (T) new Object());
                    updateBorder(pos);
                    chunks.put(pos, generator.apply(pos));
                }
                return chunks.get(pos);
            }
        }

        public boolean has(ChunkPos pos) {
            synchronized (border) {
                return chunks.containsKey(pos);
            }
        }

        public void lazyGenerate(ChunkPos pos) {
            if (has(pos)) {
                return;
            }
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                chunks.put(pos, (T) new Object());
                updateBorder(pos);
                Multithreader.run(() -> chunks.put(pos, generator.apply(pos)));
            }
        }

        public void remove(ChunkPos pos) {
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                if (chunks.containsKey(pos)) {
                    T t = chunks.remove(pos);
                    updateBorder(pos);
                    destructor.accept(pos, t);
                }
            }
        }

        private boolean shouldBeBorder(ChunkPos pos) {
            if (has(pos)) {
                return false;
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (has(new ChunkPos(pos.x + i, pos.y + j))) {
                        return true;
                    }
                }
            }
            return false;
        }

        private void updateBorder(ChunkPos pos) {
            synchronized (border) {
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (shouldBeBorder(new ChunkPos(pos.x + i, pos.y + j))) {
                            border.add(new ChunkPos(pos.x + i, pos.y + j));
                        } else {
                            border.remove(new ChunkPos(pos.x + i, pos.y + j));
                        }
                    }
                }
            }
        }
    }
}
