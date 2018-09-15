package world.chunks;

import definitions.BlockType;
import static definitions.Loader.getBlock;
import util.math.Vec3d;
import util.noise.NoiseInterpolator;
import util.noise.ZeroCrossing;
import util.rlestorage.IntConverter.BlockTypeConverter;
import util.rlestorage.RLEArrayStorage;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public class ConstructedChunk extends AbstractChunk {

    public final RLEArrayStorage<BlockType> blockStorage = new RLEArrayStorage(CHUNK_SIZE, new BlockTypeConverter());

    public ConstructedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        double caveDensity = 2;
        double ironDensity = 1;

        NoiseInterpolator caves1 = new NoiseInterpolator(world.noise("constructedchunk1"), 8, 8, 128);
        NoiseInterpolator caves2 = new NoiseInterpolator(world.noise("constructedchunk2"), 8, 8, 128);
        caves1.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        caves2.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        caves1.generate(6, .003 * caveDensity);
        caves2.generate(6, .003 * caveDensity);

        NoiseInterpolator iron = new NoiseInterpolator(world.noise("constructedchunk3"), 8, 8, 128);
        iron.setTransform(worldPos(), new Vec3d(1, 1, 2).mul(CHUNK_SIZE / 8.));
        iron.generate(1, .05 * ironDensity);

        HeightmappedChunk hc = world.heightmappedChunks.get(pos);
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                BlockType surface, nearSurface;
                switch (hc.biomemap[x][y].plurality()) {
                    case FOREST:
                    case PLAINS:
                    case JUNGLE:
                        surface = getBlock("grass0");
                        nearSurface = getBlock("dirt");
                        break;
                    case TAIGA:
                    case TUNDRA:
                        surface = getBlock("grass1");
                        nearSurface = getBlock("dirt");
                        break;
                    case SNOW:
                        surface = getBlock("grass2");
                        nearSurface = getBlock("dirt");
                        break;
                    case DESERT:
                    case COLD_DESERT:
                        surface = nearSurface = getBlock("sand");
                        break;
                    case ROCK:
                        surface = nearSurface = getBlock("stone");
                        break;
                    default:
                        throw new RuntimeException("Unknown biome");
                }
                blockStorage.setRangeInfinite(x, y, 0, getBlock("stone"));
                for (ZeroCrossing zc : hc.heightmap[x][y]) {
                    if (zc.positive) {
                        if (zc.start <= zc.end - 3) {
                            blockStorage.setRange(x, y, zc.start, zc.end - 3, getBlock("stone"));
                        }
                        if (zc.start <= zc.end - 1) {
                            blockStorage.setRange(x, y, Math.max(zc.end - 2, zc.start), zc.end - 1, nearSurface);
                        }
//                        if (zc.end > 175) {
//                            blockStorage.set(x, y, zc.end, SNOWY_GRASS);
//                        } else if (zc.end > 150) {
//                            blockStorage.set(x, y, zc.end, TUNDRA_GRASS);
//                        } else {
                        blockStorage.set(x, y, zc.end, surface);
//                        }
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
                // CAVES
                /*int caveStart = minZ + 3;
                for (int z = minZ; z <= elevation; z++) {
                    int wx = x + pos.x * CHUNK_SIZE;
                    int wy = y + pos.y * CHUNK_SIZE;
                    int wz = (z - minZ) * 2;
                    if (Math.abs(caves1.get(wx, wy, wz) - .5) + Math.abs(caves2.get(wx, wy, wz) - .5)
                            < .04 * caveDensity * (1 - 17 / (elevation - z + 20.))) {
                        if (z < caveStart) {
                            blockStorage.set(x, y, z, LAVA);
                        }
                    } else {
                        if (caveStart < z) {
                            blockStorage.setRange(x, y, caveStart, z - 1, null);
                        }
                        caveStart = Math.max(caveStart, z + 1);
                        if (iron.get(wx, wy, wz) * (1 - 15 / (elevation - z + 20.))
                                > .5 + .25 / ironDensity) {
                            blockStorage.set(x, y, z, IRON_ORE);
                        }
                    }
                }
                if (caveStart <= elevation) {
                    blockStorage.setRange(x, y, caveStart, elevation, null);
                }*/
            }
        }

        for (ChunkPos cp : world.getChunksNearby(pos)) {
            StructuredChunk sc = world.structuredChunks.get(cp);
            sc.constructIn(this);
        }

        hc.cavemap = null;
        hc.heightmap = null;

        //world.heightmappedChunks.remove(pos);
    }
}
