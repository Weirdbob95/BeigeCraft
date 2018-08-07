package world;

import static engine.Activatable.using;
import engine.Behavior;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import opengl.Camera;
import opengl.ShaderProgram;
import opengl.Texture;
import static util.MathUtils.floor;
import static util.MathUtils.mod;
import util.Multithreader;
import util.Noise;
import util.Resources;
import util.vectors.Vec3d;
import util.vectors.Vec4d;
import world.chunks.AbstractChunk;
import world.chunks.ConstructedChunk;
import world.chunks.HeightmappedChunk;
import world.chunks.PlannedChunk;
import world.chunks.RenderedChunk;
import world.chunks.StructuredChunk;

public class World extends Behavior {

    public static final int CHUNK_SIZE = 32;
    public static final int RENDER_DISTANCE = 32;
    public static final int UNLOAD_DISTANCE = RENDER_DISTANCE + 4;

    public static final ShaderProgram TERRAIN_SHADER = Resources.loadShaderProgram("terrain");
    public static final Texture TERRAIN_TEXTURE = new Texture("sprites/blockSpritesheet.png");

    public final ChunkMap<ConstructedChunk> constructedChunks = new ChunkMap<>(this, ConstructedChunk::new);
    public final ChunkMap<HeightmappedChunk> heightmappedChunks = new ChunkMap<>(this, HeightmappedChunk::new);
    public final ChunkMap<PlannedChunk> plannedChunks = new ChunkMap<>(this, PlannedChunk::new);
    public final ChunkMap<RenderedChunk> renderedChunks = new ChunkMap<>(this, RenderedChunk::new);
    public final ChunkMap<StructuredChunk> structuredChunks = new ChunkMap<>(this, StructuredChunk::new);

    public final double seed = Math.random() * 1e6;
    public final Noise noise = new Noise(seed);

    public BlockType getBlock(Vec3d pos) {
        return constructedChunks.get(getChunkPos(pos)).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z));
    }

    public ChunkPos getChunkPos(Vec3d pos) {
        return new ChunkPos(floor(pos.x / CHUNK_SIZE), floor(pos.y / CHUNK_SIZE));
    }

    public Set<ChunkPos> getChunksNearby(Vec3d pos) {
        Set<ChunkPos> r = new HashSet();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                r.add(getChunkPos(pos.add(new Vec3d(i, j, 0))));
            }
        }
        return r;
    }

    public Set<ChunkPos> getChunksNearby(ChunkPos pos) {
        Set<ChunkPos> r = new HashSet();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                r.add(new ChunkPos(pos.x + i, pos.y + j));
            }
        }
        return r;
    }

    @Override
    public void render() {
        renderedChunks.get(getChunkPos(Camera.camera.position));
        using(Arrays.asList(TERRAIN_TEXTURE, TERRAIN_SHADER), () -> {
            TERRAIN_SHADER.setUniform("projectionMatrix", Camera.getProjectionMatrix());
            TERRAIN_SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
            for (ChunkPos pos : renderedChunks.allGenerated()) {
                renderedChunks.get(pos).render();
            }
        });
        ChunkPos camera = getChunkPos(Camera.camera.position);
        constructedChunks.removeDistant(camera);
        heightmappedChunks.removeDistant(camera);
        plannedChunks.removeDistant(camera);
        renderedChunks.removeDistant(camera);
        structuredChunks.removeDistant(camera);
    }

    public void setBlock(Vec3d pos, BlockType bt) {
        if (bt != getBlock(pos)) {
            constructedChunks.get(getChunkPos(pos)).blockStorage.set((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z), bt);

            for (ChunkPos c : getChunksNearby(pos)) {
                if (renderedChunks.has(c)) {
                    renderedChunks.get(c).generateOuter();
                }
            }
        }
    }

    public static class ChunkMap<T extends AbstractChunk> {

        private final Map<ChunkPos, T> chunks = new ConcurrentHashMap();
        private final Map<ChunkPos, Object> locks = new ConcurrentHashMap();
        private final Set<ChunkPos> border = Collections.newSetFromMap(new ConcurrentHashMap());
        private final World world;
        private final BiFunction<World, ChunkPos, T> constructor;

        public ChunkMap(World world, BiFunction<World, ChunkPos, T> constructor) {
            this.world = world;
            this.constructor = constructor;
        }

        public List<ChunkPos> allGenerated() {
            return chunks.entrySet().stream().filter(e -> e.getValue().isGenerated())
                    .map(Entry::getKey).collect(Collectors.toList());
        }

        public Set<ChunkPos> border() {
            return border;
        }

        public T get(ChunkPos pos) {
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                if (!chunks.containsKey(pos)) {
                    chunks.put(pos, constructor.apply(world, pos));
                    updateBorder(pos);
                    chunks.get(pos).generateOuter();
                }
                return chunks.get(pos);
            }
        }

        public boolean has(ChunkPos pos) {
            return chunks.containsKey(pos);
        }

        public void lazyGenerate(ChunkPos pos) {
            if (has(pos)) {
                return;
            }
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                chunks.put(pos, constructor.apply(world, pos));
                updateBorder(pos);
                Multithreader.run(() -> chunks.get(pos).generateOuter());
            }
        }

        public void remove(ChunkPos pos) {
            locks.putIfAbsent(pos, new Object());
            synchronized (locks.get(pos)) {
                if (chunks.containsKey(pos)) {
                    T t = chunks.remove(pos);
                    updateBorder(pos);
                    t.cleanup();
                }
            }
        }

        public void removeDistant(ChunkPos camera) {
            for (ChunkPos pos : chunks.keySet()) {
                if (camera.distance(pos) > UNLOAD_DISTANCE) {
                    remove(pos);
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
