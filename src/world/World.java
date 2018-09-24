package world;

import definitions.BlockType;
import definitions.TerrainObjectType;
import engine.Behavior;
import static game.Settings.RENDER_DISTANCE;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import opengl.Camera;
import static opengl.GLObject.bindAll;
import opengl.ShaderProgram;
import opengl.Texture;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
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

    private final Map<Class<? extends AbstractRegion>, RegionMap> regions = new HashMap();

    public final long seed = new Random().nextLong();
    public WaterManager waterManager;

    private final HashMap<String, Noise> noiseMap = new HashMap();

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
            getChunk(ConstructedChunk.class, v).terrainObjectOccupancyMap.put(new Vec3d((int) mod(v.x, CHUNK_SIZE), (int) mod(v.y, CHUNK_SIZE), floor(v.z)), toi);
        }
        getChunk(ConstructedChunk.class, toi.chunkPos).terrainObjects.add(toi);
        return true;
    }

    @Override
    public void createInner() {
        waterManager = new WaterManager();
        waterManager.world = this;
        waterManager.create();
    }

    public BlockType getBlock(Vec3d pos) {
        return getChunk(ConstructedChunk.class, pos).blockStorage.get((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z));
    }

    public <T extends AbstractChunk> T getChunk(Class<T> c, Vec3d pos) {
        return getChunk(c, getChunkPos(pos));
    }

    public <T extends AbstractChunk> T getChunk(Class<T> c, RegionPos pos) {
        return getRegionMap(c).get(pos);
    }

    public RegionPos getChunkPos(Vec3d pos) {
        return new RegionPos(floor(pos.x / CHUNK_SIZE), floor(pos.y / CHUNK_SIZE));
    }

    public Set<RegionPos> getChunksNearby(Vec3d pos) {
        Set<RegionPos> r = new HashSet();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                r.add(getChunkPos(pos.add(new Vec3d(i, j, 0))));
            }
        }
        return r;
    }

    public Set<RegionPos> getChunksNearby(RegionPos pos) {
        Set<RegionPos> r = new HashSet();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                r.add(new RegionPos(pos.x + i, pos.y + j));
            }
        }
        return r;
    }

    public <T extends AbstractRegion> RegionMap<T> getRegionMap(Class<T> c) {
        regions.putIfAbsent(c, new RegionMap(this, CHUNK_SIZE, (w, rp) -> {
            try {
                return c.getConstructor(World.class, RegionPos.class).newInstance(w, rp);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }));
        return regions.get(c);
    }

    public TerrainObjectInstance getTerrainObject(Vec3d pos) {
        return getChunk(ConstructedChunk.class, pos).terrainObjectOccupancyMap.get(new Vec3d((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), floor(pos.z)));
    }

    public Noise noise(String id) {
        if (!noiseMap.containsKey(id)) {
            noiseMap.put(id, new Noise(new Random(seed + id.hashCode())));
        }
        return noiseMap.get(id);
    }

    public void removeTerrainObject(Vec3d pos) {
        TerrainObjectInstance toi = getTerrainObject(pos);
        if (toi != null) {
            for (Vec3d v : toi.getOccupancy()) {
                v = v.add(new Vec3d(toi.chunkPos.x, toi.chunkPos.y, 0).mul(CHUNK_SIZE));
                getChunk(ConstructedChunk.class, v).terrainObjectOccupancyMap.remove(new Vec3d((int) mod(v.x, CHUNK_SIZE), (int) mod(v.y, CHUNK_SIZE), floor(v.z)));
            }
            getChunk(ConstructedChunk.class, toi.chunkPos).terrainObjects.remove(toi);
        }
    }

    @Override
    public void render() {
        getChunk(RenderedChunk.class, Camera.camera3d.position);
        bindAll(TERRAIN_TEXTURE, TERRAIN_BLOOM_TEXTURE, TERRAIN_SHADER);
        TERRAIN_SHADER.setUniform("projectionMatrix", Camera.camera3d.getProjectionMatrix());
        TERRAIN_SHADER.setUniform("color", new Vec4d(1, 1, 1, 1));
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1});
        for (RegionPos pos : getRegionMap(RenderedChunk.class).allGenerated()) {
            getChunk(RenderedChunk.class, pos).render();
        }
        glDrawBuffers(new int[]{GL_COLOR_ATTACHMENT0});
        for (RegionMap rm : regions.values()) {
            rm.removeDistant(getChunkPos(Camera.camera3d.position), RENDER_DISTANCE + UNLOAD_DISTANCE);
        }
    }

    public void setBlock(Vec3d pos, BlockType bt) {
        if (bt != null) {
            removeTerrainObject(pos);
        }
        if (bt != getBlock(pos)) {
            getChunk(ConstructedChunk.class, pos).blockStorage.set((int) mod(pos.x, CHUNK_SIZE), (int) mod(pos.y, CHUNK_SIZE), (int) Math.floor(pos.z), bt);

            for (RegionPos c : getChunksNearby(pos)) {
                if (getRegionMap(RenderedChunk.class).has(c)) {
                    getChunk(RenderedChunk.class, c).shouldRegenerate = true;
                }
            }
        }
    }
}
