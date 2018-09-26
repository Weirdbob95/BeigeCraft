package world.regions.chunks;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import util.math.Vec3d;
import util.noise.ZeroCrossing;
import util.rlestorage.IntConverter.BlockTypeConverter;
import util.rlestorage.RLEArrayStorage;
import world.TerrainObjectInstance;
import world.World;
import static world.World.CHUNK_SIZE;
import world.biomes.Biome;
import world.regions.RegionPos;

public class ConstructedChunk extends AbstractChunk {

    public final RLEArrayStorage<BlockType> blockStorage = new RLEArrayStorage(CHUNK_SIZE, new BlockTypeConverter());
    public final List<TerrainObjectInstance> terrainObjects = new LinkedList();
    public final Map<Vec3d, TerrainObjectInstance> terrainObjectOccupancyMap = new HashMap();

    public ConstructedChunk(World world, RegionPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        int dirtHeight = 5;

//        double ironDensity = 1;
//
//        NoiseInterpolator iron = new NoiseInterpolator(world.noise("constructedchunk3"), 8, 8, 128);
//        iron.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
//        iron.generate(1, .05 * ironDensity);
        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                Biome b = hc.biomemap[x][y].plurality();
                BlockType surface = surface(b), nearSurface = nearSurface(b);
                blockStorage.setRangeInfinite(x, y, 0, getBlock("stone"));
                for (ZeroCrossing zc : hc.heightmap[x][y]) {
                    if (zc.positive) {
                        if (zc.start <= zc.end - 2 * dirtHeight) {
                            blockStorage.setRange(x, y, zc.start, zc.end - 2 * dirtHeight, getBlock("stone"));
                        }
                        if (zc.start <= zc.end - dirtHeight) {
                            blockStorage.setRange(x, y, Math.max(zc.end - 2 * dirtHeight + 1, zc.start), zc.end - dirtHeight, getBlock("stoneLight"));
                        }
                        if (zc.start <= zc.end - 1) {
                            blockStorage.setRange(x, y, Math.max(zc.end - dirtHeight + 1, zc.start), zc.end - 1, nearSurface);
                        }
                        blockStorage.set(x, y, zc.end, surface);
                    }
                }
                for (ZeroCrossing zc : hc.cavemap[x][y]) {
                    if (!zc.positive) {
                        blockStorage.setRange(x, y, zc.start, zc.end, null);
                        if (zc.start <= hc.zMin + 2) {
                            blockStorage.setRange(x, y, zc.start, hc.zMin + 2, getBlock("lava"));
                        }
                    }
                }
            }
        }

        for (RegionPos cp : pos.nearby(1)) {
            FinalizedStructuredChunk fsc = get(FinalizedStructuredChunk.class, cp);
            fsc.constructIn(this);
        }

        hc.cavemap = null;
        hc.heightmap = null;
    }

    private static BlockType nearSurface(Biome b) {
        switch (b) {
            case FOREST:
            case PLAINS:
            case JUNGLE:
            case TAIGA:
            case TUNDRA:
            case SNOW:
                return getBlock("dirt");
            case DESERT:
            case COLD_DESERT:
                return getBlock("sandStone");
            //return getBlock("sand");
            case ROCK:
                return getBlock("stone");
            default:
                throw new RuntimeException("Unknown biome");
        }
    }

    private static BlockType surface(Biome b) {
        switch (b) {
            case FOREST:
            case PLAINS:
            case JUNGLE:
                return getBlock("grass0");
            case TAIGA:
            case TUNDRA:
                return getBlock("grass1");
            case SNOW:
                return getBlock("grass2");
            case DESERT:
            case COLD_DESERT:
                return getBlock("sand");
            case ROCK:
                return getBlock("stone");
            default:
                throw new RuntimeException("Unknown biome");
        }
    }
}
