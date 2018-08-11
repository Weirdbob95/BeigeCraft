package world.chunks;

import util.RLEColumnStorage;
import util.RLEColumnStorage.BlockTypeConverter;
import util.noise.NoiseInterpolator;
import util.vectors.Vec3d;
import world.BlockType;
import static world.BlockType.DIRT;
import static world.BlockType.GRASS;
import static world.BlockType.IRON_ORE;
import static world.BlockType.SAND;
import static world.BlockType.SNOWY_GRASS;
import static world.BlockType.STONE;
import static world.BlockType.TUNDRA_GRASS;
import world.ChunkPos;
import world.World;
import static world.World.CHUNK_SIZE;

public class ConstructedChunk extends AbstractChunk {

    public final RLEColumnStorage<BlockType> blockStorage = new RLEColumnStorage(CHUNK_SIZE, new BlockTypeConverter());

    public ConstructedChunk(World world, ChunkPos pos) {
        super(world, pos);
    }

    @Override
    protected void generate() {
        double caveDensity = 2;
        double ironDensity = 1;
        int minZ = -200;

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
                int elevation = hc.heightmap[x][y];
                switch (hc.biomemap[x][y].plurality()) {
                    case FOREST:
                    case PLAINS:
                    case JUNGLE:
                        blockStorage.setRangeInfinite(x, y, elevation, GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case TAIGA:
                    case TUNDRA:
                        blockStorage.setRangeInfinite(x, y, elevation, TUNDRA_GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case SNOW:
                        blockStorage.setRangeInfinite(x, y, elevation, SNOWY_GRASS);
                        blockStorage.setRangeInfinite(x, y, elevation - 1, DIRT);
                        break;
                    case DESERT:
                        blockStorage.setRangeInfinite(x, y, elevation, SAND);
                        break;
                    case ROCK:
                        blockStorage.setRangeInfinite(x, y, elevation, STONE);
                        break;
                }
                blockStorage.setRangeInfinite(x, y, elevation - 3, STONE);

                // CAVES
                int caveStart = minZ;
                for (int z = minZ; z <= elevation; z++) {
                    int wx = x + pos.x * CHUNK_SIZE;
                    int wy = y + pos.y * CHUNK_SIZE;
                    int wz = (z - minZ) * 2;
                    if (Math.abs(caves1.get(wx, wy, wz) - .5) + Math.abs(caves2.get(wx, wy, wz) - .5)
                            < .04 * caveDensity * (1 - 15 / (elevation - z + 20.))) {
                        // Is cave
                    } else {
                        if (caveStart < z) {
                            blockStorage.setRange(x, y, caveStart, z - 1, null);
                        }
                        caveStart = z + 1;
                        if (iron.get(wx, wy, wz) * (1 - 15 / (elevation - z + 20.))
                                > .5 + .25 / ironDensity) {
                            blockStorage.set(x, y, z, IRON_ORE);
                        }
                    }
                }
                if (caveStart <= elevation) {
                    blockStorage.setRange(x, y, caveStart, elevation, null);
                }
            }
        }
        for (ChunkPos cp : world.getChunksNearby(pos)) {
            StructuredChunk sc = world.structuredChunks.get(cp);
            sc.constructIn(this);
        }
        world.heightmappedChunks.remove(pos);
    }
}
