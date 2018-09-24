package world;

import definitions.BlockType;
import definitions.TerrainObjectType;
import engine.Behavior;
import static game.Settings.RENDER_DISTANCE;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import opengl.Camera;
import static opengl.GLObject.bindAll;
import opengl.ShaderProgram;
import opengl.Texture;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import util.Multithreader;
import util.Resources;
import static util.math.MathUtils.floor;
import static util.math.MathUtils.mod;
import util.math.Vec3d;
import util.math.Vec4d;
import util.noise.Noise;
import world.regions.AbstractRegion;
import world.regions.RegionMap;
import world.regions.RegionPos;
import world.regions.chunks.AbstractChunk;
import world.regions.chunks.ConstructedChunk;
import world.regions.chunks.HeightmappedChunk;
import world.regions.chunks.RenderedChunk;

public class World extends Behavior {

    public static final int CHUNK_SIZE = 32;
    public static final int PROVINCE_SIZE = 32;

    public static final int UNLOAD_DISTANCE = 8;

    public static final ShaderProgram TERRAIN_SHADER = Resources.loadShaderProgramGeom("terrain");
    public static final Texture TERRAIN_TEXTURE = Texture.load("blockSpritesheet.png");
    public static final Texture TERRAIN_BLOOM_TEXTURE = Texture.load("blockSpritesheet_bloom.png");

    static {
        TERRAIN_BLOOM_TEXTURE.num = 1;
        TERRAIN_SHADER.setUniform("bloom_sampler", 1);
    }

    public final long seed = new Random().nextLong();
    public final WaterManager waterManager = new WaterManager();
    private final Map<Class<? extends AbstractRegion>, RegionMap> regions = new HashMap();
    private final HashMap<String, Noise> noiseMap = new HashMap();

    public final RegionMap<ConstructedChunk> constructedChunks = getRegionMap(ConstructedChunk.class);
    public final RegionMap<HeightmappedChunk> heightmappedChunks = getRegionMap(HeightmappedChunk.class);
    public final RegionMap<RenderedChunk> renderedChunks = getRegionMap(RenderedChunk.class);

    @Override
    public void createInner() {
        waterManager.world = this;
        waterManager.create();
    }

    public <T extends AbstractChunk> T getChunk(Class<T> c, RegionPos pos) {
        return getRegionMap(c).get(pos);
    }

    private RegionPos getChunkPos(Vec3d pos) {
        return new RegionPos(floor(pos.x / CHUNK_SIZE), floor(pos.y / CHUNK_SIZE));
    }

    public <T extends AbstractRegion> RegionMap<T> getRegionMap(Class<T> c) {
        regions.putIfAbsent(c, new RegionMap(this, (w, rp) -> {
            try {
                return c.getConstructor(World.class, RegionPos.class).newInstance(w, rp);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }));
        return regions.get(c);
    }

    public Noise noise(String id) {
        if (!noiseMap.containsKey(id)) {
            noiseMap.put(id, new Noise(new Random(seed + id.hashCode())));
        }
        return noiseMap.get(id);
    }

    @Override
    public void render() {
        renderedChunks.get(Camera.camera3d.position);
        bindAll(TERRAIN_TEXTURE, TERRAIN_BLOOM_TEXTURE, TERRAIN_SHADER);
        TERRAIN_SHADER.setUniform("projectionMatrix", Camera.camera3d.getProjectionMatrix());
        TERRAIN_SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        for (RegionPos pos : getRegionMap(RenderedChunk.class).allGenerated()) {
            renderedChunks.get(pos).render();
        }
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
        for (RegionMap cm : regions.values()) {
            cm.removeDistant(getChunkPos(Camera.camera3d.position), RENDER_DISTANCE + UNLOAD_DISTANCE);
        }
    }

    @Override
    public void update(double dt) {
        if (Multithreader.isFree()) {
            RegionPos camera = getChunkPos(Camera.camera3d.position);
            Optional<RegionPos> toRender = renderedChunks.border().stream()
                    .min(Comparator.comparingDouble(camera::distance));
            if (toRender.isPresent() && camera.distance(toRender.get()) <= RENDER_DISTANCE) {
                renderedChunks.lazyGenerate(toRender.get());
            }
        }
    }

    //
    // -------------------- WORLD INTERACTION FUNCTIONS --------------------
    //
    public boolean addTerrainObject(Vec3d pos, TerrainObjectType tot) {
        TerrainObjectInstance toi = new TerrainObjectInstance(tot, getChunkPos(pos), (int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z));
        for (Vec3d v : toi.getOccupancy()) {
            v = v.add(new Vec3d(toi.chunkPos.x, toi.chunkPos.y, 0).mul(CHUNK_SIZE));
            if (getBlock(v) != null || getTerrainObject(v) != null) {
                return false;
            }
        }
        for (Vec3d v : toi.getOccupancy()) {
            v = v.add(new Vec3d(toi.chunkPos.x, toi.chunkPos.y, 0).mul(CHUNK_SIZE));
            constructedChunks.get(v).terrainObjectOccupancyMap.put(new Vec3d((int) mod(v.x, CHUNK_SIZE), (int) mod(v.y, CHUNK_SIZE), floor(v.z)), toi);
        }
        constructedChunks.get(toi.chunkPos).terrainObjects.add(toi);
        return true;
    }

    public BlockType getBlock(Vec3d pos) {
        return constructedChunks.get(pos).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z));
    }

    public TerrainObjectInstance getTerrainObject(Vec3d pos) {
        return constructedChunks.get(pos).terrainObjectOccupancyMap.get(new Vec3d((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z)));
    }

    public void removeTerrainObject(Vec3d pos) {
        TerrainObjectInstance toi = getTerrainObject(pos);
        if (toi != null) {
            for (Vec3d v : toi.getOccupancy()) {
                v = v.add(new Vec3d(toi.chunkPos.x, toi.chunkPos.y, 0).mul(CHUNK_SIZE));
                constructedChunks.get(v).terrainObjectOccupancyMap.remove(new Vec3d((int) mod(v.x, CHUNK_SIZE), (int) mod(v.y, CHUNK_SIZE), floor(v.z)));
            }
            constructedChunks.get(toi.chunkPos).terrainObjects.remove(toi);
        }
    }

    public void setBlock(Vec3d pos, BlockType bt) {
        if (bt != null) {
            removeTerrainObject(pos);
        }
        if (bt != getBlock(pos)) {
            constructedChunks.get(pos).blockStorage.set((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z), bt);

            Set<RegionPos> toRedraw = new HashSet();
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    toRedraw.add(getChunkPos(pos.add(new Vec3d(i, j, 0))));
                }
            }
            for (RegionPos c : toRedraw) {
                if (getRegionMap(RenderedChunk.class).has(c)) {
                    renderedChunks.get(c).shouldRegenerate = true;
                }
            }
        }
    }
}
